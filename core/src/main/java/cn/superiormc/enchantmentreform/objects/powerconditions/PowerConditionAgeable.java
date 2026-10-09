package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

public final class PowerConditionAgeable extends AbstractPowerCondition {

    public PowerConditionAgeable() {
        super("ageable");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }
        BlockState broken = context.brokenBlockState();
        Block block = context.block();
        BlockData data = broken != null ? broken.getBlockData()
                : block == null ? null : block.getBlockData();
        return (data instanceof Ageable ageable
                && ageable.getAge() >= ageable.getMaximumAge()) == condition.getSection().getBoolean("value", true);
    }
}
