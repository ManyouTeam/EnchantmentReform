package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Location;

public final class PowerConditionHeight extends AbstractPowerCondition {

    public PowerConditionHeight() {
        super("height");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Location location = condition.getContext() == null ? null : condition.getContext().location();
        if (location == null) {
            return false;
        }
        return location.getBlockY() >= condition.getDouble("min", Integer.MIN_VALUE, condition.getContext())
                && location.getBlockY() <= condition.getDouble("max", Integer.MAX_VALUE, condition.getContext());
    }
}
