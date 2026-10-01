package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class SetHealthAbility extends AbstractAbility {

    public SetHealthAbility(ConfigurationSection section) {
        super("SetHealth", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        if (living.getHealth() <= 0) {
            return false;
        }
        double max = CommonUtil.getMaxHealth(living);
        double damage = context.originalDamage();
        double heal = getDouble("amount", 10.0, context,
                "health", String.valueOf(living.getHealth()),
                "max-health", String.valueOf(max),
                "damage", String.valueOf(damage),
                "original", String.valueOf(damage));
        if (heal >= 2048) {
            heal = 2048;
        }
        living.setHealth(Math.min(max, heal));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
