package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;

public final class PowerConditionLightLevel extends AbstractPowerCondition {

    public PowerConditionLightLevel() { super("light_level"); }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null || condition.getContext().source() == null) {
            return false;
        }
        int light = condition.getContext().source().getLocation().getBlock().getLightLevel();
        return light >= condition.getDouble("min", 0, condition.getContext()) && light <= condition.getDouble("max", 15, condition.getContext());
    }
}
