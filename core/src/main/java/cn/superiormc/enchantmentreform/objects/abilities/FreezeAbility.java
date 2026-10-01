package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FreezeAbility extends AbstractAbility {

    public FreezeAbility(ConfigurationSection section) {
        super("Freeze", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        int freezeTicks = Math.max(0, getInt("freeze-ticks", 60, context));
        if (getBoolean("accumulate", false)) {
            living.setFreezeTicks(freezeTicks);
        } else {
            living.setFreezeTicks(freezeTicks + living.getFreezeTicks());
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
