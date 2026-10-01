package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.hooks.BlockPriceUtil;
import cn.superiormc.enchantmentreform.managers.HookManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public final class PowerConditionBlockTypeOffset extends AbstractPowerCondition {

    public PowerConditionBlockTypeOffset() {
        super("block_type_offset");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null || context.block() == null) {
            return false;
        }

        Set<String> types = CommonUtil.values(condition.getSection(), "types", "type-name");
        if (types.isEmpty()) {
            return false;
        }

        Block block = context.block().getRelative(
                condition.getInt("offset.x", 0, context),
                condition.getInt("offset.y", 0, context),
                condition.getInt("offset.z", 0, context));
        return !types.isEmpty() && BlockPriceUtil.matchesAny(block, types);
    }
}
