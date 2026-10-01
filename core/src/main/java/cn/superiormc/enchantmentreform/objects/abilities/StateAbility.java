package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import org.bukkit.configuration.ConfigurationSection;

public final class StateAbility extends AbstractAbility {

    public StateAbility(ConfigurationSection section) {
        super("State", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        PowerStateStore.Key key = PowerStateStore.key(
                context, section.getString("key", "default"),
                section.getBoolean("per-target", false), EntitySelector.parse(
                        section.getString("owner"), EntitySelector.SOURCE), EntitySelector.parse(
                        section.getString("state-target"), EntitySelector.TARGET));
        String operation = section.getString("operation", "ADD").toUpperCase();
        if (operation.equals("CLEAR")) {
            PowerStateStore.remove(key);
            return false;
        }

        double amount = getDouble("amount", 1.0D, context);
        double maximum = getDouble("maximum", Double.MAX_VALUE, context);
        double duration = getDouble("duration", 0.0D, context);
        PowerStateStore.Update update = PowerStateStore.update(key, current -> switch (operation) {
            case "SET" -> amount;
            case "SUBTRACT" -> current - amount;
            default -> current + amount;
        }, maximum, duration);

        double value = update.current();
        double triggerAt = getDouble("trigger-at", Double.MAX_VALUE, context);
        if (Double.isNaN(triggerAt) || value < triggerAt) {
            return false;
        }

        boolean consumeTriggerValue = section.getBoolean("consume-trigger-value", false);
        if (consumeTriggerValue && (!Double.isFinite(triggerAt) || triggerAt <= 0.0D)) {
            return false;
        }

        ThresholdGroups groups = splitThreshold(value, triggerAt);
        if (consumeTriggerValue) {
            PowerStateStore.set(key, groups.remainder(), maximum, duration);
        }

        PowerContext triggerContext = withStateContext(
                context, update.previous(), value, groups.count(), groups.remainder());
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection("trigger-abilities"), triggerContext);
        String changeCallback = Double.compare(value, update.previous()) > 0
                ? "on-increase"
                : Double.compare(value, update.previous()) < 0
                ? "on-decrease"
                : "on-unchanged";
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection(changeCallback), triggerContext);

        if (!consumeTriggerValue && section.getBoolean("clear-on-trigger", true)) {
            PowerStateStore.remove(key);
        }
        return false;
    }

    private PowerContext withStateContext(PowerContext context, double previous, double current,
                                          long triggerCount, double remainder) {
        TriggerData triggerData = context.triggerData();
        if (triggerData == null) {
            return context;
        }
        TriggerData changed = triggerData.toBuilder()
                .extra(BuiltinContextKeys.STATE_PREVIOUS, previous)
                .extra(BuiltinContextKeys.STATE_CURRENT, current)
                .extra(BuiltinContextKeys.STATE_TRIGGER_COUNT, triggerCount)
                .extra(BuiltinContextKeys.STATE_REMAINDER, remainder)
                .build();
        return context.withTriggerData(changed);
    }

    private ThresholdGroups splitThreshold(double value, double triggerAt) {
        if (!Double.isFinite(triggerAt) || triggerAt <= 0.0D) {
            return new ThresholdGroups(1L, value);
        }
        double quotient = value / triggerAt;
        double nearestInteger = Math.rint(quotient);
        if (Double.isFinite(quotient)
                && Math.abs(quotient - nearestInteger) <= Math.ulp(quotient) * 4.0D) {
            quotient = nearestInteger;
        }
        long count = quotient >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) Math.floor(quotient);
        count = Math.max(1L, count);
        double remainder = value - count * triggerAt;
        double tolerance = Math.max(Math.ulp(value), Math.ulp(triggerAt)) * 4.0D;
        if (!Double.isFinite(remainder) || remainder < 0.0D || remainder >= triggerAt) {
            remainder = value % triggerAt;
        }
        if (Math.abs(remainder) <= tolerance || Math.abs(remainder - triggerAt) <= tolerance) {
            remainder = 0.0D;
        }
        return new ThresholdGroups(count, remainder);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private record ThresholdGroups(long count, double remainder) {
    }
}
