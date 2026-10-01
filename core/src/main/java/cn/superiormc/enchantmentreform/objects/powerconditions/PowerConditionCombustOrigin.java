package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

import java.util.Set;

public final class PowerConditionCombustOrigin extends AbstractPowerCondition {

    public PowerConditionCombustOrigin() {
        super("combust_origin");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null
                || !(condition.getContext().event() instanceof EntityCombustEvent event)) {
            return false;
        }
        Set<String> origins = CommonUtil.values(condition.getSection(), "origins", "origin");
        if (origins.isEmpty()) {
            return false;
        }
        String actual = event instanceof EntityCombustByEntityEvent ? "ENTITY"
                : event instanceof EntityCombustByBlockEvent ? "BLOCK" : "OTHER";
        return origins.contains(actual);
    }
}
