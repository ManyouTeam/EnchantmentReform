package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;

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
        boolean perTarget = condition.getSection().getBoolean("per-target", false);
        PowerStateStore.Key key = PowerStateStore.key(context, "condition-cooldown",
                condition.getSection().getCurrentPath(), perTarget, EntitySelector.parse(
                        condition.getString("owner"), EntitySelector.SOURCE));
        return PowerStateStore.tryAcquireCooldown(key, seconds);
    }
}
