package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import com.google.common.base.Enums;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SummonAbility extends AbstractAbility {

    public SummonAbility(ConfigurationSection section) {
        super("Summon", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location location = getLocation(context);
        if (location == null || location.getWorld() == null) {
            return false;
        }

        String typeString = selectEntityType();
        EntityType type = Enums.getIfPresent(EntityType.class, typeString.toUpperCase()).orNull();
        if (type == null || !type.isSpawnable()) {
            return false;
        }

        Entity spawned = location.getWorld().spawnEntity(location, type);
        if (!(spawned instanceof LivingEntity living)) {
            return false;
        }

        applyLivingStats(context, living);
        if (living instanceof Mob mob) {
            applyMobOptions(context, mob);
        }
        if (living instanceof Creeper creeper) {
            applyCreeperStats(context, creeper);
        }
        return false;
    }

    private String selectEntityType() {
        List<String> entities = section.getStringList("entities");
        return entities.isEmpty()
                ? section.getString("entity", section.getString("entity-type", ""))
                : entities.get(ThreadLocalRandom.current().nextInt(entities.size()));
    }

    private void applyLivingStats(PowerContext context, LivingEntity living) {
        double maxHealth = getDouble("max-health", -1, context);
        if (maxHealth > 0) {
            AttributeInstance maxHealthAttr = living.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(maxHealth);
                living.setHealth(Math.min(maxHealth, Math.max(0.1, maxHealth)));
            }
        }

        double health = getDouble("health", -1, context);
        if (health > 0) {
            double cap = maxHealth > 0 ? maxHealth : CommonUtil.getMaxHealth(living);
            living.setHealth(Math.min(cap, Math.max(0.1, health)));
        }

        double attack = getDouble("attack-damage", -1, context);
        if (attack >= 0) {
            AttributeInstance attackAttr = living.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackAttr != null) {
                attackAttr.setBaseValue(attack);
            }
        }
    }

    private void applyMobOptions(PowerContext context, Mob mob) {
        if (section.contains("set-target")) {
            Entity target = context.entity(EntitySelector.parse(
                    section.getString("set-target"), EntitySelector.TARGET));
            if (target instanceof LivingEntity livingTarget && target != mob) {
                mob.setTarget(livingTarget);
            }
        }

        if (!getBoolean("set-none-drops", false)) {
            return;
        }
        mob.setLootTable(null);
        mob.setCanPickupItems(false);
        EntityEquipment equipment = mob.getEquipment();
        if (equipment == null) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (mob.canUseEquipmentSlot(slot)) {
                equipment.setDropChance(slot, 0.0F);
            }
        }
    }

    private void applyCreeperStats(PowerContext context, Creeper creeper) {
        float explosionRadius = (float) getDouble("creeper.explosion-radius", -1, context);
        if (explosionRadius >= 0) {
            creeper.setExplosionRadius(Math.max(0, Math.round(explosionRadius)));
        }

        int maxFuseTicks = getInt("creeper.fuse-ticks", -1, context);
        if (maxFuseTicks >= 0) {
            creeper.setMaxFuseTicks(Math.max(1, maxFuseTicks));
        }

        if (section.contains("creeper.powered")) {
            creeper.setPowered(getBoolean("creeper.powered", false));
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
