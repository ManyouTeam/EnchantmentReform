package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FireAbility extends AbstractAbility {

    public FireAbility(ConfigurationSection section) {
        super("Fire", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        int fireTicks = Math.max(0, getInt("fire-ticks", 60, context));
        if (getBoolean("accumulate", false)) {
            living.setFireTicks(fireTicks);
        } else {
            living.setFireTicks(fireTicks + living.getFireTicks());
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
