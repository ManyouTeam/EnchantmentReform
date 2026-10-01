package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

public final class PowerConditionAgeable extends AbstractPowerCondition {

    public PowerConditionAgeable() {
        super("ageable");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Block block = condition.getContext().block();
        return (block != null && block.getBlockData() instanceof Ageable ageable
                && ageable.getAge() >= ageable.getMaximumAge()) == condition.getSection().getBoolean("value", true);
    }
}
