package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttributeModifier;
import cn.superiormc.enchantmentreform.objects.ObjectPower;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RefreshAttributeAbility extends AbstractAbility {

    private static final Map<String, ActiveAttribute> ACTIVE = new ConcurrentHashMap<>();

    private static final Map<String, ActiveCustomAttribute> ACTIVE_CUSTOM =
            new ConcurrentHashMap<>();

    private static final Map<String, TransitionAttribute> TRANSITION_ACTIVE =
            new ConcurrentHashMap<>();

    private static final Map<String, TransitionCustomAttribute> TRANSITION_CUSTOM =
            new ConcurrentHashMap<>();

    private static final double MODIFIER_EPSILON = 1.0E-4D;

    public RefreshAttributeAbility(ConfigurationSection section) {
        super("RefreshAttribute", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (section.getBoolean("transition.enabled", false)) {
            return executeTransition(context);
        }
        Entity target = getTargetEntity(context);
        String attributeName = section.getString("attribute", "minecraft:max_health");
        ObjectCustomAttribute customAttribute = AttributeManager.attributeManager == null
                ? null : AttributeManager.attributeManager.resolveAttribute(attributeName);
        if (customAttribute != null) {
            if (target instanceof Player player) {
                refreshCustomAttribute(context, player, customAttribute, attributeName);
            }
            return false;
        }
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        Attribute attribute = Registry.ATTRIBUTE.get(CommonUtil.parseNamespacedKey(attributeName));
        AttributeInstance instance = attribute == null ? null : living.getAttribute(attribute);
        if (instance == null) {
            return false;
        }
        AttributeModifier.Operation operation = operation();
        double amount = getDouble("amount", 0.0D, context);
        String identity = identity("vanilla", living.getUniqueId(), context, attributeName);
        UUID modifierId = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
        instance.getModifiers().stream().filter(modifier -> modifier.getUniqueId().equals(modifierId))
                .findFirst().ifPresent(instance::removeModifier);
        instance.addModifier(new AttributeModifier(modifierId, context.power().getId(), amount, operation));

        ActiveAttribute active = ACTIVE.compute(identity, (ignored, previous) -> new ActiveAttribute(
                previous == null ? 1L : previous.generation + 1L,
                living.getUniqueId(), new WeakReference<>(living), attribute, modifierId,
                sourceBinding(context)));
        scheduleRemoval(identity, active, living, context);
        return false;
    }

    private boolean executeTransition(PowerContext context) {
        Entity target = getTargetEntity(context);
        String attributeName = section.getString("attribute", "minecraft:max_health");
        ObjectCustomAttribute customAttribute = AttributeManager.attributeManager == null
                ? null : AttributeManager.attributeManager.resolveAttribute(attributeName);
        boolean active = PowerConditionsManager.powerConditions.matches(
                section.getConfigurationSection("transition.active-conditions"), context);
        if (customAttribute != null) {
            if (target instanceof Player player) {
                updateTransitionCustomAttribute(context, player, customAttribute,
                        attributeName, active);
            }
            return false;
        }
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        Attribute attribute = Registry.ATTRIBUTE.get(CommonUtil.parseNamespacedKey(attributeName));
        AttributeInstance instance = attribute == null ? null : living.getAttribute(attribute);
        if (instance == null) {
            return false;
        }

        String identity = identity("transition-vanilla", living.getUniqueId(),
                context, attributeName);
        TransitionAttribute state = TRANSITION_ACTIVE.get(identity);
        if (living.isDead()) {
            if (state != null) {
                removeTransitionModifier(state);
                TRANSITION_ACTIVE.remove(identity, state);
            }
            return false;
        }
        if (!active && state == null) {
            return false;
        }
        if (state == null) {
            UUID modifierId = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
            state = new TransitionAttribute(living.getUniqueId(), new WeakReference<>(living),
                    attribute, modifierId, sourceBinding(context));
            TransitionAttribute previous = TRANSITION_ACTIVE.putIfAbsent(identity, state);
            if (previous != null) {
                state = previous;
            }
        }

        double updateInterval = Math.min(20.0D, Math.max(1.0D,
                getDouble("transition.update-interval", 1.0D, context)));
        double elapsedTicks = state.elapsedTicks(context, updateInterval);
        if (elapsedTicks <= 0.0D) {
            return false;
        }
        double previousProgress = state.progress;
        double progress = updateProgress(previousProgress, elapsedTicks, active, context);
        state.progress = progress;
        runTransitionCallbacks(state, previousProgress, progress, active, context);
        if (!active && progress <= 0.0D) {
            removeTransitionModifier(state);
            TRANSITION_ACTIVE.remove(identity, state);
            return false;
        }

        double amount = getDouble("amount", 0.0D, context) * curve(progress);
        if (!approximatelyEqual(amount, state.lastAppliedAmount)) {
            replaceModifier(instance, state.modifierId, context.power().getId(), amount,
                    operation());
            state.lastAppliedAmount = amount;
        }
        return false;
    }

    private void updateTransitionCustomAttribute(PowerContext context, Player player,
                                                 ObjectCustomAttribute attribute,
                                                 String attributeName, boolean active) {
        String identity = identity("transition-custom", player.getUniqueId(),
                context, attributeName);
        TransitionCustomAttribute state = TRANSITION_CUSTOM.get(identity);
        if (player.isDead()) {
            if (state != null) {
                removeTransitionCustomModifier(state);
                TRANSITION_CUSTOM.remove(identity, state);
            }
            return;
        }
        if (!active && state == null) {
            return;
        }
        if (state == null) {
            String modifierId = "enchantmentreform:refresh_attribute/"
                    + UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
            ObjectCustomAttributeModifier original = attribute.getTransientModifier(player, modifierId);
            state = new TransitionCustomAttribute(player.getUniqueId(), new WeakReference<>(player),
                    attribute, modifierId, original, sourceBinding(context));
            TransitionCustomAttribute previous = TRANSITION_CUSTOM.putIfAbsent(identity, state);
            if (previous != null) {
                state = previous;
            }
        }

        double updateInterval = Math.min(20.0D, Math.max(1.0D,
                getDouble("transition.update-interval", 1.0D, context)));
        double elapsedTicks = state.elapsedTicks(context, updateInterval);
        if (elapsedTicks <= 0.0D) {
            return;
        }
        double previousProgress = state.progress;
        double progress = updateProgress(previousProgress, elapsedTicks, active, context);
        state.progress = progress;
        runTransitionCallbacks(state, previousProgress, progress, active, context);
        if (!active && progress <= 0.0D) {
            removeTransitionCustomModifier(state);
            TRANSITION_CUSTOM.remove(identity, state);
            return;
        }

        double amount = getDouble("amount", 0.0D, context) * curve(progress);
        if (!approximatelyEqual(amount, state.lastAppliedAmount)) {
            ObjectCustomAttributeModifier applied = new ObjectCustomAttributeModifier(
                    state.modifierId, amount, customOperation());
            attribute.setTransientModifier(player, applied);
            state.appliedModifier = applied;
            state.lastAppliedAmount = amount;
        }
    }

    private double updateProgress(double current, double elapsedTicks, boolean active,
                                  PowerContext context) {
        double duration = active
                ? getDouble("transition.rise-duration", 20.0D, context)
                : getDouble("transition.fall-duration", 20.0D, context);
        duration = Math.max(1.0D, duration);
        double changed = current + (active ? elapsedTicks / duration : -elapsedTicks / duration);
        return Math.max(0.0D, Math.min(1.0D, changed));
    }

    private double curve(double progress) {
        String configured = section.getString("transition.curve", "LINEAR")
                .toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (configured) {
            case "EASE_IN" -> progress * progress;
            case "EASE_OUT" -> 1.0D - Math.pow(1.0D - progress, 2.0D);
            case "EASE_IN_OUT" -> progress * progress * (3.0D - 2.0D * progress);
            default -> progress;
        };
    }

    private void runTransitionCallbacks(TransitionState state, double previousProgress,
                                        double progress, boolean active,
                                        PowerContext context) {
        if (active && !state.activeLastUpdate) {
            executeTransitionCallback("on-start", context);
        }
        if (active && previousProgress < 1.0D && progress >= 1.0D) {
            executeTransitionCallback("on-full", context);
        }
        if (!active && state.activeLastUpdate && previousProgress > 0.0D) {
            executeTransitionCallback("on-decay-start", context);
        }
        if (!active && previousProgress > 0.0D && progress <= 0.0D) {
            executeTransitionCallback("on-empty", context);
        }
        state.activeLastUpdate = active;
    }

    private void executeTransitionCallback(String name, PowerContext context) {
        ConfigurationSection callback = section.getConfigurationSection("transition." + name);
        if (callback != null) {
            AbilityManager.abilityManager.execute(callback, context);
        }
    }

    private static boolean approximatelyEqual(double first, double second) {
        return Double.isFinite(second) && Math.abs(first - second) < MODIFIER_EPSILON;
    }

    private static void replaceModifier(AttributeInstance instance, UUID modifierId,
                                        String name, double amount,
                                        AttributeModifier.Operation operation) {
        instance.getModifiers().stream()
                .filter(modifier -> modifier.getUniqueId().equals(modifierId))
                .findFirst().ifPresent(instance::removeModifier);
        instance.addModifier(new AttributeModifier(modifierId, name, amount, operation));
    }

    @Override
    public void onPowerSourceDeactivate(Player player, ActivePowerSource source) {
        ACTIVE.entrySet().removeIf(entry -> {
            ActiveAttribute active = entry.getValue();
            if (!matches(active.sourceBinding, player, source)) {
                return false;
            }
            removeModifier(active);
            return true;
        });
        ACTIVE_CUSTOM.entrySet().removeIf(entry -> {
            ActiveCustomAttribute active = entry.getValue();
            if (!matches(active.sourceBinding, player, source)) {
                return false;
            }
            removeCustomModifier(active);
            return true;
        });
        TRANSITION_ACTIVE.entrySet().removeIf(entry -> {
            TransitionAttribute active = entry.getValue();
            if (!matches(active.sourceBinding, player, source)) {
                return false;
            }
            removeTransitionModifier(active);
            return true;
        });
        TRANSITION_CUSTOM.entrySet().removeIf(entry -> {
            TransitionCustomAttribute active = entry.getValue();
            if (!matches(active.sourceBinding, player, source)) {
                return false;
            }
            removeTransitionCustomModifier(active);
            return true;
        });
    }

    @Override
    public void onEntityUnload(UUID entityId) {
        if (entityId == null) {
            return;
        }
        ACTIVE.entrySet().removeIf(entry -> {
            ActiveAttribute active = entry.getValue();
            if (!entityId.equals(active.entityId) && !isOwner(entityId, active.sourceBinding)) {
                return false;
            }
            removeModifier(active);
            return true;
        });
        ACTIVE_CUSTOM.entrySet().removeIf(entry -> {
            ActiveCustomAttribute active = entry.getValue();
            if (!entityId.equals(active.entityId) && !isOwner(entityId, active.sourceBinding)) {
                return false;
            }
            removeCustomModifier(active);
            return true;
        });
        TRANSITION_ACTIVE.entrySet().removeIf(entry -> {
            TransitionAttribute active = entry.getValue();
            if (!entityId.equals(active.entityId) && !isOwner(entityId, active.sourceBinding)) {
                return false;
            }
            removeTransitionModifier(active);
            return true;
        });
        TRANSITION_CUSTOM.entrySet().removeIf(entry -> {
            TransitionCustomAttribute active = entry.getValue();
            if (!entityId.equals(active.entityId) && !isOwner(entityId, active.sourceBinding)) {
                return false;
            }
            removeTransitionCustomModifier(active);
            return true;
        });
    }

    @Override
    public void onUnload() {
        ACTIVE.values().forEach(RefreshAttributeAbility::removeModifier);
        ACTIVE.clear();
        ACTIVE_CUSTOM.values().forEach(RefreshAttributeAbility::removeCustomModifier);
        ACTIVE_CUSTOM.clear();
        TRANSITION_ACTIVE.values().forEach(RefreshAttributeAbility::removeTransitionModifier);
        TRANSITION_ACTIVE.clear();
        TRANSITION_CUSTOM.values().forEach(
                RefreshAttributeAbility::removeTransitionCustomModifier);
        TRANSITION_CUSTOM.clear();
    }

    private void refreshCustomAttribute(PowerContext context,
                                        Player player,
                                        ObjectCustomAttribute attribute,
                                        String attributeName) {
        String identity = identity("custom", player.getUniqueId(), context, attributeName);
        ActiveCustomAttribute previous = ACTIVE_CUSTOM.get(identity);
        String modifierId = "enchantmentreform:refresh_attribute/"
                + UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
        ObjectCustomAttributeModifier current = attribute.getTransientModifier(
                player, modifierId);
        ObjectCustomAttributeModifier original = previous != null
                && Objects.equals(current, previous.appliedModifier)
                ? previous.originalModifier : current;
        double amount = getDouble("amount", 0.0D, context);
        ObjectCustomAttributeModifier applied = new ObjectCustomAttributeModifier(
                modifierId, amount, customOperation());
        attribute.setTransientModifier(player, applied);
        ActiveCustomAttribute active = ACTIVE_CUSTOM.compute(identity,
                (ignored, old) -> new ActiveCustomAttribute(
                        old == null ? 1L : old.generation + 1L,
                        player.getUniqueId(), new WeakReference<>(player), attribute,
                        original, applied, sourceBinding(context)));
        scheduleCustomRemoval(identity, active, player, context);
    }

    private void scheduleRemoval(String identity, ActiveAttribute active,
                                 LivingEntity living, PowerContext context) {
        if (!section.contains("duration")) {
            return;
        }
        long generation = active.generation;
        long duration = Math.max(2L, getInt("duration", 30, context));
        SchedulerUtil.runTaskLater(living, () -> {
            ActiveAttribute current = ACTIVE.get(identity);
            if (current == null || current.generation != generation) {
                return;
            }
            removeModifier(current);
            ACTIVE.remove(identity, current);
        }, duration);
    }

    private void scheduleCustomRemoval(String identity, ActiveCustomAttribute active,
                                       Player player, PowerContext context) {
        if (!section.contains("duration")) {
            return;
        }
        long generation = active.generation;
        long duration = Math.max(2L, getInt("duration", 30, context));
        SchedulerUtil.runTaskLater(player, () -> {
            ActiveCustomAttribute scheduled = ACTIVE_CUSTOM.get(identity);
            if (scheduled == null || scheduled.generation != generation) {
                return;
            }
            removeCustomModifier(scheduled);
            ACTIVE_CUSTOM.remove(identity, scheduled);
        }, duration);
    }

    private String identity(String namespace, UUID targetId, PowerContext context,
                            String attributeName) {
        SourceBinding binding = sourceBinding(context);
        String occurrence = binding == null ? "unbound"
                : binding.slot.name().toLowerCase(Locale.ROOT);
        return namespace + ":" + targetId + ":" + context.power().getId() + ":"
                + section.getCurrentPath() + ":" + occurrence + ":" + attributeName;
    }

    private SourceBinding sourceBinding(PowerContext context) {
        if (context.player() == null || context.power() == null
                || context.contextItem() == null || context.contextSlot() == null) {
            return null;
        }
        return new SourceBinding(context.player().getUniqueId(), context.power(),
                context.contextSlot());
    }

    private static boolean matches(SourceBinding binding, Player player,
                                   ActivePowerSource source) {
        return binding != null
                && binding.ownerId.equals(player.getUniqueId())
                && binding.power == source.power()
                && binding.slot == source.slot();
    }

    private static boolean isOwner(UUID entityId, SourceBinding binding) {
        return binding != null && entityId.equals(binding.ownerId);
    }

    private AttributeModifier.Operation operation() {
        String configured = section.getString("operation", "ADD_NUMBER")
                .toUpperCase(Locale.ROOT).replace('-', '_');
        configured = switch (configured) {
            case "ADD_VALUE" -> "ADD_NUMBER";
            case "ADD_MULTIPLIED_BASE" -> "ADD_SCALAR";
            case "ADD_MULTIPLIED_TOTAL" -> "MULTIPLY_SCALAR_1";
            default -> configured;
        };
        try {
            return AttributeModifier.Operation.valueOf(configured);
        } catch (IllegalArgumentException exception) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
    }

    private ObjectCustomAttributeModifier.Operation customOperation() {
        String configured = section.getString("operation", "ADD_VALUE")
                .toUpperCase(Locale.ROOT).replace('-', '_');
        configured = switch (configured) {
            case "ADD_NUMBER" -> "ADD_VALUE";
            case "ADD_SCALAR" -> "ADD_MULTIPLIED_BASE";
            case "MULTIPLY_SCALAR_1" -> "ADD_MULTIPLIED_TOTAL";
            default -> configured;
        };
        try {
            return ObjectCustomAttributeModifier.Operation.valueOf(configured);
        } catch (IllegalArgumentException exception) {
            return ObjectCustomAttributeModifier.Operation.ADD_VALUE;
        }
    }

    private static void removeModifier(ActiveAttribute active) {
        LivingEntity living = active.entity.get();
        if (living == null) return;
        AttributeInstance instance = living.getAttribute(active.attribute);
        if (instance != null) {
            instance.getModifiers().stream()
                    .filter(modifier -> modifier.getUniqueId().equals(active.modifierId))
                    .findFirst().ifPresent(instance::removeModifier);
        }
        AttributeInstance maximumHealth = living.getAttribute(Attribute.MAX_HEALTH);
        double maximum = maximumHealth == null ? living.getHealth() : maximumHealth.getValue();
        if (living.getHealth() > maximum) {
            living.setHealth(maximum);
        }
    }

    private static void removeCustomModifier(ActiveCustomAttribute active) {
        Player player = active.player.get();
        if (player == null || !java.util.Objects.equals(
                active.attribute.getTransientModifier(player, active.appliedModifier.id()),
                active.appliedModifier)) {
            return;
        }
        if (active.originalModifier == null) {
            active.attribute.removeTransientModifier(player, active.appliedModifier.id());
        } else {
            active.attribute.setTransientModifier(player, active.originalModifier);
        }
    }

    private static void removeTransitionModifier(TransitionAttribute active) {
        LivingEntity living = active.entity.get();
        if (living == null) {
            return;
        }
        AttributeInstance instance = living.getAttribute(active.attribute);
        if (instance != null) {
            instance.getModifiers().stream()
                    .filter(modifier -> modifier.getUniqueId().equals(active.modifierId))
                    .findFirst().ifPresent(instance::removeModifier);
        }
        clampHealth(living);
    }

    private static void removeTransitionCustomModifier(TransitionCustomAttribute active) {
        Player player = active.player.get();
        if (player == null || !Objects.equals(
                active.attribute.getTransientModifier(player, active.modifierId),
                active.appliedModifier)) {
            return;
        }
        if (active.originalModifier == null) {
            active.attribute.removeTransientModifier(player, active.modifierId);
        } else {
            active.attribute.setTransientModifier(player, active.originalModifier);
        }
    }

    private static void clampHealth(LivingEntity living) {
        AttributeInstance maximumHealth = living.getAttribute(Attribute.MAX_HEALTH);
        double maximum = maximumHealth == null ? living.getHealth() : maximumHealth.getValue();
        if (living.getHealth() > maximum) {
            living.setHealth(maximum);
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private record ActiveAttribute(long generation, UUID entityId, WeakReference<LivingEntity> entity,
                                   Attribute attribute, UUID modifierId,
                                   SourceBinding sourceBinding) {
    }

    private record ActiveCustomAttribute(long generation,
                                         UUID entityId,
                                         WeakReference<Player> player,
                                         ObjectCustomAttribute attribute,
                                         ObjectCustomAttributeModifier originalModifier,
                                         ObjectCustomAttributeModifier appliedModifier,
                                         SourceBinding sourceBinding) {
    }

    private record SourceBinding(UUID ownerId, ObjectPower power, EquipmentSlot slot) {
    }

    private abstract static class TransitionState {
        private long lastTick;
        private long lastUpdateAt;
        private double pendingTicks;
        double progress;
        double lastAppliedAmount = Double.NaN;
        private boolean activeLastUpdate;

        final double elapsedTicks(PowerContext context, double updateInterval) {
            long tick = context.triggerData() == null ? 0L : context.triggerData().tick();
            long now = System.currentTimeMillis();
            double elapsed;
            if (tick > 0L && lastTick > 0L) {
                elapsed = tick > lastTick ? tick - lastTick : 0.0D;
            } else if (lastUpdateAt > 0L && now > lastUpdateAt) {
                elapsed = (now - lastUpdateAt) / 50.0D;
            } else {
                elapsed = 1.0D;
            }
            lastTick = tick;
            lastUpdateAt = now;
            pendingTicks = Math.min(20.0D, pendingTicks + Math.max(0.0D, elapsed));
            if (pendingTicks < updateInterval) {
                return 0.0D;
            }
            double result = pendingTicks;
            pendingTicks = 0.0D;
            return result;
        }
    }

    private static final class TransitionAttribute extends TransitionState {
        private final UUID entityId;
        private final WeakReference<LivingEntity> entity;
        private final Attribute attribute;
        private final UUID modifierId;
        private final SourceBinding sourceBinding;

        private TransitionAttribute(UUID entityId, WeakReference<LivingEntity> entity,
                                    Attribute attribute, UUID modifierId,
                                    SourceBinding sourceBinding) {
            this.entityId = entityId;
            this.entity = entity;
            this.attribute = attribute;
            this.modifierId = modifierId;
            this.sourceBinding = sourceBinding;
        }
    }

    private static final class TransitionCustomAttribute extends TransitionState {
        private final UUID entityId;
        private final WeakReference<Player> player;
        private final ObjectCustomAttribute attribute;
        private final String modifierId;
        private final ObjectCustomAttributeModifier originalModifier;
        private final SourceBinding sourceBinding;
        private ObjectCustomAttributeModifier appliedModifier;

        private TransitionCustomAttribute(UUID entityId, WeakReference<Player> player,
                                          ObjectCustomAttribute attribute, String modifierId,
                                          ObjectCustomAttributeModifier originalModifier,
                                          SourceBinding sourceBinding) {
            this.entityId = entityId;
            this.player = player;
            this.attribute = attribute;
            this.modifierId = modifierId;
            this.originalModifier = originalModifier;
            this.sourceBinding = sourceBinding;
        }
    }
}
