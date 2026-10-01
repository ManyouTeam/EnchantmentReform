package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;

public final class PowerConditionRandom extends AbstractPowerCondition {

    public PowerConditionRandom() {
        super("random");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        double chance = condition.getDouble("value", condition.player(), condition.getContext());
        return Math.random() < Math.clamp(chance, 0.0D, 1.0D);
    }
}
