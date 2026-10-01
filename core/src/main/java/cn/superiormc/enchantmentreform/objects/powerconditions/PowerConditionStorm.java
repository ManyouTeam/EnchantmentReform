package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Location;

public final class PowerConditionStorm extends AbstractPowerCondition {

    public PowerConditionStorm() {
        super("storm");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Location location = condition.getContext() == null ? null : condition.getContext().location();
        return location != null && location.getWorld() != null
                && location.getWorld().hasStorm() == condition.getSection().getBoolean("value", true);
    }
}
