package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Set;

public final class PowerConditionDamageOrigin extends AbstractPowerCondition {

    public PowerConditionDamageOrigin() {
        super("damage_origin");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null
                || !(condition.getContext().event() instanceof EntityDamageEvent event)) {
            return false;
        }
        Set<String> origins = CommonUtil.values(condition.getSection(), "origins", "origin");
        if (!origins.isEmpty()) {
            String actual = event instanceof EntityDamageByEntityEvent ? "ENTITY"
                    : event instanceof EntityDamageByBlockEvent ? "BLOCK" : "OTHER";
            if (!origins.contains(actual)) {
                return false;
            }
        }
        if (condition.getBoolean("direct-source", false)) {
            if (!(event instanceof EntityDamageByEntityEvent byEntity)) {
                return false;
            }
            Entity expected = condition.getContext().entity(
                    condition.getSection().getString("source"), EntitySelector.PLAYER);
            return expected != null && byEntity.getDamager().equals(expected);
        }
        return !origins.isEmpty();
    }
}
