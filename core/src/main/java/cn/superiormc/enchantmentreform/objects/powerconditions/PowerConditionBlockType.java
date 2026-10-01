package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.hooks.BlockPriceUtil;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.block.BlockState;

import java.util.Set;

public final class PowerConditionBlockType extends AbstractPowerCondition {

    public PowerConditionBlockType() {
        super("block_type");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }
        Set<String> types = CommonUtil.values(condition.getSection(), "types", "type-name");
        if (types.isEmpty()) {
            return false;
        }
        // Prefer the pre-break snapshot when present (the live block may already be air, e.g.
        // during on-block-drop-item). Fall back to the live block otherwise.
        BlockState broken = context.brokenBlockState();
        if (broken != null) {
            return BlockPriceUtil.matchesAny(broken, types);
        }
        return context.block() != null && BlockPriceUtil.matchesAny(context.block(), types);
    }
}
