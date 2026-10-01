package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;

public final class PowerConditionAny extends AbstractPowerCondition {

    public PowerConditionAny() {
        super("any");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        return PowerConditionsManager.powerConditions.matchesAny(
                condition.getSection().getConfigurationSection("conditions"), condition.getContext(), condition.player());
    }
}
