package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;

import java.util.Locale;

public final class PowerConditionCooldown extends AbstractPowerCondition {

    public PowerConditionCooldown() {
        super("cooldown");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }
        double seconds = condition.getDouble("seconds", 0.0D, context);
        if (seconds <= 0.0D || context.source() == null) {
            return true;
        }
        return PowerStateStore.tryAcquireCooldown(getCooldownKey(condition), seconds);
    }

    @Override
    protected String getResultMessage(ObjectSingleCondition condition, String path) {
        double seconds = PowerStateStore.getRemainingSeconds(getCooldownKey(condition));
        double rounded = Math.ceil(seconds * 10.0D) / 10.0D;
        String remaining = rounded == Math.rint(rounded)
                ? Long.toString((long) rounded)
                : String.format(Locale.ROOT, "%.1f", rounded);
        return condition.getString(path, "", condition.getContext(), "remaining", remaining);
    }

    private PowerStateStore.Key getCooldownKey(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null || context.source() == null) {
            return null;
        }
        boolean perTarget = condition.getSection().getBoolean("per-target", false);
        return PowerStateStore.key(context, "condition-cooldown",
                condition.getSection().getCurrentPath(), perTarget, EntitySelector.parse(
                        condition.getString("owner"), EntitySelector.SOURCE));
    }
}
