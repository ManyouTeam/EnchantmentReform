package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Set;

public final class PowerConditionSpawnReason extends AbstractPowerCondition {

    public PowerConditionSpawnReason() {
        super("spawn_reason");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }

        Set<String> reasons = CommonUtil.values(
                condition.getSection(), "reasons", "reason");
        if (reasons.isEmpty()) {
            return false;
        }

        Entity entity = context.entity(EntitySelector.parse(
                condition.getSection().getString("target"), EntitySelector.TARGET));
        if (entity == null) {
            return false;
        }

        CreatureSpawnEvent.SpawnReason spawnReason = null;
        if (context.event() instanceof CreatureSpawnEvent event
                && event.getEntity().getUniqueId().equals(entity.getUniqueId())) {
            spawnReason = event.getSpawnReason();
        }
        if (spawnReason == null) {
            spawnReason = entity.getEntitySpawnReason();
        }
        return spawnReason != null && reasons.contains(spawnReason.name());
    }
}
