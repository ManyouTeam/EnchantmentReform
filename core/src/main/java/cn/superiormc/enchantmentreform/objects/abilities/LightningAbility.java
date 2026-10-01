package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class LightningAbility extends AbstractAbility {

    public LightningAbility(ConfigurationSection section) {
        super("Lightning", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        Entity damageSource = getSourceEntity(context);
        int level = context.level();
        int count = Math.max(1, getInt("count", 2, level));
        double radius = getDouble("radius", 3.0, level);

        for (int i = 0; i < count; i++) {
            Location strike = entity.getLocation().clone().add(
                    (Math.random() * 2 - 1) * radius,
                    0,
                    (Math.random() * 2 - 1) * radius
            );
            entity.getWorld().strikeLightningEffect(strike);
            for (Entity nearby : entity.getNearbyEntities(radius, 3, radius)) {
                if (nearby instanceof LivingEntity livingEntity) {
                    AbilityDamageUtil.runDirectDamage(() -> livingEntity.damage(getDouble("damage", 4.0, level), damageSource));
                }
            }
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
