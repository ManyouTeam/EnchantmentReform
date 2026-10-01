package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PowerModifierStackDamageModifier extends AbstractPowerModifier {

    private static final String STATE_NAMESPACE = "stack-damage-modifier";

    public PowerModifierStackDamageModifier(ConfigurationSection section) {
        super("stack_damage_modifier", section);
    }

    @Override
    public void onUnload() {
        MessageTasks.clearAll();
    }

    @Override
    public void onEntityUnload(UUID entityId) {
        MessageTasks.clearEntity(entityId);
    }

    @Override
    protected void onApply(PowerContext context) {
        DamageType damageType = DamageType.parse(
                section.getString("damage-type"), DamageType.DEALT);
        StackAction action = StackAction.parse(section.getString("stack-action"), StackAction.ADD);
        StackValue stackValue = StackValue.parse(
                section.getString("stack-value"), StackValue.CURRENT);
        DamageOperation operation = DamageOperation.parse(
                section.getString("operation"), DamageOperation.INCREASE);

        Entity owner = context.entity(EntitySelector.parse(
                section.getString("owner"), EntitySelector.PLAYER));
        if (owner == null) {
            return;
        }

        int maximum = Math.max(1, getInt("max-stacks", 5, context));
        boolean perTarget = section.getBoolean("scope.per-target", false);
        boolean perDamageCause = section.getBoolean("scope.per-damage-cause", false);
        Entity scopedTarget = perTarget ? resolveScopedTarget(context, damageType) : null;
        StateIdentity identity = stateIdentity(context, owner, scopedTarget, perDamageCause);
        PowerStateStore.Key key = identity.key();

        Player recipient = context.player(EntitySelector.parse(
                section.getString("messages.recipient"), EntitySelector.PLAYER));
        double throttleSeconds = Math.max(0.0D,
                getDouble("messages.throttle-seconds", 0.2D, context));
        double perStack = Math.max(0.0D,
                getDouble("percent-per-stack", 5.0D, context));

        if (action == StackAction.CLEAR) {
            double cleared = PowerStateStore.consume(key);
            MessageTasks.cancelExpiry(key);
            if (cleared > 0.0D) {
                sendMessage("clear", "messages.clear", context, identity, recipient,
                        throttleSeconds, cleared, cleared, 0.0D, maximum,
                        perStack, calculatePercent(context, cleared, maximum, perStack),
                        0.0D, "CLEAR", damageType, operation);
            }
            return;
        }

        double resetSeconds = 0.0D;
        StackChange change = new StackChange(0.0D, 0.0D, 0.0D);
        switch (action) {
            case ADD -> {
                double amount = getDouble("stack-amount", 1.0D, context);
                resetSeconds = getDouble("reset.seconds", 5.0D, context);
                PowerStateStore.Update update = PowerStateStore.update(
                        key, value -> value + amount, maximum, resetSeconds);
                double effective = stackValue == StackValue.PREVIOUS
                        ? update.previous() : update.current();
                change = new StackChange(update.previous(), update.current(), effective);
            }
            case READ -> {
                double current = Math.min(PowerStateStore.get(key), maximum);
                change = new StackChange(current, current, current);
            }
            case CONSUME -> {
                double consumed = Math.min(PowerStateStore.consume(key), maximum);
                MessageTasks.cancelExpiry(key);
                change = new StackChange(consumed, 0.0D, consumed);
            }
            case CLEAR -> throw new IllegalStateException("CLEAR was handled before stack evaluation");
        }

        DamageEffect effect = applyDamage(
                context, change.effective(), maximum, perStack, operation);

        if (action == StackAction.ADD && change.current() > change.previous()) {
            boolean reachedMaximum = change.previous() < maximum && change.current() >= maximum;
            String kind = reachedMaximum ? "max" : "add";
            String path = reachedMaximum ? "messages.max" : "messages.add";
            sendMessage(kind, path, context, identity, recipient, throttleSeconds,
                    change.current(), change.previous(),
                    change.current() - change.previous(), maximum, perStack,
                    calculatePercent(context, change.current(), maximum, perStack),
                    resetSeconds, "", damageType, operation);
        } else if (action == StackAction.ADD
                && change.previous() >= maximum && change.current() >= maximum) {
            sendMessage("maxing", "messages.maxing", context, identity, recipient,
                    throttleSeconds, change.current(), change.previous(), 0.0D,
                    maximum, perStack,
                    calculatePercent(context, change.current(), maximum, perStack),
                    resetSeconds, "", damageType, operation);
        } else if (action == StackAction.READ && change.effective() > 0.0D && effect.applied()) {
            sendMessage("apply", "messages.apply", context, identity, recipient,
                    throttleSeconds, change.effective(), change.previous(), 0.0D,
                    maximum, perStack, effect.percent(), resetSeconds, "", damageType, operation);
        } else if (action == StackAction.CONSUME && change.effective() > 0.0D) {
            sendMessage("consume", "messages.consume", context, identity, recipient,
                    throttleSeconds, change.effective(), change.previous(), 0.0D,
                    maximum, perStack, effect.percent(), resetSeconds,
                    "CONSUME", damageType, operation);
        }

        String clearReason = clearReasonAfterApply(context, damageType);
        if (action != StackAction.CONSUME && clearReason != null) {
            PowerStateStore.remove(key);
            MessageTasks.cancelExpiry(key);
            if (change.effective() > 0.0D) {
                sendMessage("clear", "messages.clear", context, identity, recipient,
                        throttleSeconds, change.effective(), change.previous(), 0.0D,
                        maximum, perStack, effect.percent(), resetSeconds,
                        clearReason, damageType, operation);
            }
        } else if (action == StackAction.ADD) {
            String expiryMessage = resolveMessage("messages.expire", context, identity.pool(),
                    change.current(), change.previous(),
                    change.current() - change.previous(), maximum, perStack,
                    calculatePercent(context, change.current(), maximum, perStack),
                    resetSeconds, "TIMEOUT", damageType, operation);
            MessageTasks.scheduleExpiry(
                    key, recipient, expiryMessage, resetSeconds, throttleSeconds);
        }
    }

    private StateIdentity stateIdentity(PowerContext context, Entity owner,
                                        Entity scopedTarget, boolean perDamageCause) {
        String fallbackPool = section.getCurrentPath() == null
                ? "default" : section.getCurrentPath();
        String pool = getString("pool", fallbackPool, context);
        if (pool.isBlank()) {
            pool = fallbackPool;
        }
        if (perDamageCause) {
            String cause = context.event() instanceof EntityDamageEvent damage
                    ? damage.getCause().name() : "UNKNOWN";
            pool += ":" + cause;
        }
        String powerId = context.power() == null ? "none" : context.power().getId();
        return new StateIdentity(
                PowerStateStore.key(owner, powerId, STATE_NAMESPACE, pool, scopedTarget), pool);
    }

    private Entity resolveScopedTarget(PowerContext context, DamageType damageType) {
        EntitySelector fallback = damageType == DamageType.TAKEN
                ? EntitySelector.SOURCE : EntitySelector.TARGET;
        String configured = section.getString("scope.target");
        return context.entity(EntitySelector.parse(configured, fallback));
    }

    private DamageEffect applyDamage(PowerContext context, double stacks, int maximum,
                                     double perStack, DamageOperation operation) {
        double percent = calculatePercent(context, stacks, maximum, perStack);
        if (operation == DamageOperation.NONE
                || !(context.event() instanceof EntityDamageEvent)) {
            return new DamageEffect(false, percent);
        }
        double multiplier = switch (operation) {
            case INCREASE -> 1.0D + percent / 100.0D;
            case DECREASE -> Math.max(0.0D, 1.0D - percent / 100.0D);
            case NONE -> 1.0D;
        };
        double damage = context.result().damage(context.triggerData());
        context.result().damage(Math.max(0.0D, damage * multiplier));
        return new DamageEffect(true, percent);
    }

    private double calculatePercent(PowerContext context, double stacks, int maximum,
                                    double perStack) {
        double defaultMaximum = maximum * perStack;
        double maximumPercent = Math.max(0.0D,
                getDouble("maximum-percent", defaultMaximum, context));
        return Math.min(maximumPercent,
                Math.min(Math.max(0.0D, stacks), maximum) * perStack);
    }

    private String clearReasonAfterApply(PowerContext context, DamageType damageType) {
        if (!(context.event() instanceof EntityDamageEvent)) {
            return null;
        }
        Set<String> clearOn = configuredValues("reset.clear-on");
        if (clearOn.contains("AFTER_APPLY")) {
            return "AFTER_APPLY";
        }
        if (damageType == DamageType.DEALT && clearOn.contains("AFTER_DAMAGE_DEALT")) {
            return "AFTER_DAMAGE_DEALT";
        }
        if (damageType == DamageType.TAKEN && clearOn.contains("AFTER_DAMAGE_TAKEN")) {
            return "AFTER_DAMAGE_TAKEN";
        }
        return null;
    }

    private void sendMessage(String kind, String path, PowerContext context,
                             StateIdentity identity, Player recipient,
                             double throttleSeconds, double stacks,
                             double previousStacks, double addedStacks,
                             int maximum, double perStack, double percent,
                             double resetSeconds, String clearReason,
                             DamageType damageType, DamageOperation operation) {
        String message = resolveMessage(path, context, identity.pool(), stacks,
                previousStacks, addedStacks, maximum, perStack, percent,
                resetSeconds, clearReason, damageType, operation);
        if (message.isBlank() && "messages.max".equals(path)) {
            message = resolveMessage("messages.add", context, identity.pool(), stacks,
                    previousStacks, addedStacks, maximum, perStack, percent,
                    resetSeconds, clearReason, damageType, operation);
        }
        MessageTasks.send(
                identity.key(), recipient, kind, message, throttleSeconds);
    }

    private String resolveMessage(String path, PowerContext context, String pool,
                                  double stacks, double previousStacks,
                                  double addedStacks, int maximum,
                                  double perStack, double percent,
                                  double resetSeconds, String clearReason,
                                  DamageType damageType, DamageOperation operation) {
        return getString(path, "", context,
                "pool", pool,
                "stacks", formatNumber(stacks),
                "previous_stacks", formatNumber(previousStacks),
                "added_stacks", formatNumber(addedStacks),
                "max_stacks", Integer.toString(maximum),
                "percent_per_stack", formatNumber(perStack),
                "percent", formatNumber(percent),
                "reset_seconds", formatNumber(resetSeconds),
                "damage_type", damageType.name(),
                "operation", operation.name(),
                "clear_reason", clearReason == null ? "" : clearReason);
    }

    private String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", "");
    }

    private Set<String> configuredValues(String path) {
        Set<String> result = new LinkedHashSet<>();
        Object raw = section.get(path);
        if (raw instanceof List<?> values) {
            for (Object value : values) {
                addNormalized(result, value);
            }
        } else {
            addNormalized(result, raw);
        }
        return result;
    }

    private void addNormalized(Set<String> values, Object value) {
        if (value == null) {
            return;
        }
        String normalized = String.valueOf(value).trim()
                .toUpperCase(Locale.ROOT).replace('-', '_');
        if (!normalized.isEmpty()) {
            values.add(normalized);
        }
    }

    private enum StackAction {
        ADD,
        READ,
        CONSUME,
        CLEAR;

        private static StackAction parse(String value, StackAction fallback) {
            return parseEnum(StackAction.class, value, fallback);
        }
    }

    private enum StackValue {
        PREVIOUS,
        CURRENT;

        private static StackValue parse(String value, StackValue fallback) {
            return parseEnum(StackValue.class, value, fallback);
        }
    }

    private enum DamageType {
        TAKEN,
        DEALT;

        private static DamageType parse(String value, DamageType fallback) {
            return parseEnum(DamageType.class, value, fallback);
        }
    }

    private enum DamageOperation {
        NONE,
        INCREASE,
        DECREASE;

        private static DamageOperation parse(String value, DamageOperation fallback) {
            return parseEnum(DamageOperation.class, value, fallback);
        }
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static final class MessageTasks {

        private static final Map<PowerStateStore.Key, PendingExpiry> TASKS =
                new ConcurrentHashMap<>();

        private MessageTasks() {
        }

        private static void send(PowerStateStore.Key stateKey, Player recipient,
                                 String kind, String message, double throttleSeconds) {
            if (stateKey == null || recipient == null || message == null || message.isBlank()) {
                return;
            }
            if (throttleSeconds > 0.0D) {
                String target = stateKey.target() == null ? "none" : stateKey.target().toString();
                PowerStateStore.Key messageKey = PowerStateStore.key(
                        recipient,
                        stateKey.powerId(),
                        "stack-modifier-message",
                        stateKey.name() + ":" + target + ":" + kind,
                        null);
                if (!PowerStateStore.tryAcquireCooldown(messageKey, throttleSeconds)) {
                    return;
                }
            }
            TextUtil.sendMessage(recipient, message);
        }

        private static void scheduleExpiry(PowerStateStore.Key stateKey, Player recipient,
                                           String message, double resetSeconds,
                                           double throttleSeconds) {
            cancelExpiry(stateKey);
            if (stateKey == null || recipient == null || message == null || message.isBlank()
                    || resetSeconds <= 0.0D) {
                return;
            }
            PendingExpiry pending = new PendingExpiry(
                    stateKey, recipient.getUniqueId(), message, throttleSeconds);
            TASKS.put(stateKey, pending);
            schedule(pending, resetSeconds);
        }

        private static void cancelExpiry(PowerStateStore.Key stateKey) {
            if (stateKey == null) {
                return;
            }
            PendingExpiry pending = TASKS.remove(stateKey);
            if (pending != null && pending.task != null) {
                pending.task.cancel();
            }
        }

        private static void clearEntity(UUID entityId) {
            if (entityId == null) {
                return;
            }
            TASKS.forEach((key, pending) -> {
                if (entityId.equals(key.owner()) || entityId.equals(key.target())
                        || entityId.equals(pending.recipientId)) {
                    if (TASKS.remove(key, pending) && pending.task != null) {
                        pending.task.cancel();
                    }
                }
            });
        }

        private static void clearAll() {
            TASKS.values().forEach(pending -> {
                if (pending.task != null) {
                    pending.task.cancel();
                }
            });
            TASKS.clear();
        }

        private static void schedule(PendingExpiry pending, double seconds) {
            Player recipient = Bukkit.getPlayer(pending.recipientId);
            if (recipient == null || !recipient.isOnline()) {
                TASKS.remove(pending.stateKey, pending);
                return;
            }
            long ticks = Math.max(1L, (long) Math.ceil(seconds * 20.0D) + 1L);
            pending.task = SchedulerUtil.runTaskLater(
                    recipient, () -> expire(pending), ticks);
        }

        private static void expire(PendingExpiry pending) {
            if (TASKS.get(pending.stateKey) != pending) {
                return;
            }
            double remaining = PowerStateStore.getRemainingSeconds(pending.stateKey);
            if (remaining > 0.0D) {
                schedule(pending, remaining);
                return;
            }
            if (!TASKS.remove(pending.stateKey, pending)) {
                return;
            }
            Player recipient = Bukkit.getPlayer(pending.recipientId);
            if (recipient != null && recipient.isOnline()) {
                send(pending.stateKey, recipient, "expire",
                        pending.message, pending.throttleSeconds);
            }
        }

        private static final class PendingExpiry {

            private final PowerStateStore.Key stateKey;

            private final UUID recipientId;

            private final String message;

            private final double throttleSeconds;

            private volatile SchedulerUtil task;

            private PendingExpiry(PowerStateStore.Key stateKey, UUID recipientId,
                                  String message, double throttleSeconds) {
                this.stateKey = stateKey;
                this.recipientId = recipientId;
                this.message = message;
                this.throttleSeconds = throttleSeconds;
            }
        }
    }

    private record StateIdentity(PowerStateStore.Key key, String pool) {
    }

    private record StackChange(double previous, double current, double effective) {
    }

    private record DamageEffect(boolean applied, double percent) {
    }
}
