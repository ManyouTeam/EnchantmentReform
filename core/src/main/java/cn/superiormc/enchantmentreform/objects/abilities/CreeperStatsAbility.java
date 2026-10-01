package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;

public class CreeperStatsAbility extends AbstractAbility {

    public CreeperStatsAbility(ConfigurationSection section) {
        super("CreeperStats", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof Creeper creeper)) {
            return false;
        }

        if (section.contains("explosion-radius")) {
            int explosionRadius = getInt("explosion-radius", creeper.getExplosionRadius(), context);
            creeper.setExplosionRadius(Math.max(0, explosionRadius));
        }

        if (section.contains("fuse-ticks")) {
            int fuseTicks = getInt("fuse-ticks", creeper.getMaxFuseTicks(), context);
            creeper.setMaxFuseTicks(Math.max(1, fuseTicks));
        }

        if (section.contains("powered")) {
            creeper.setPowered(getBoolean("powered", creeper.isPowered()));
        }

        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}