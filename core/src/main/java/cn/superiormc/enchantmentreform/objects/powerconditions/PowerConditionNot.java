package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;

public final class PowerConditionNot extends AbstractPowerCondition {

    public PowerConditionNot() {
        super("not");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        return !PowerConditionsManager.powerConditions.matches(
                condition.getSection().getConfigurationSection("conditions"), condition.getContext(), condition.player());
    }
}
