package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class DamageEntityAbility extends AbstractAbility {

    public DamageEntityAbility(ConfigurationSection section) {
        super("DamageEntity", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof LivingEntity living) || living.getHealth() <= 0) {
            return false;
        }
        double original = context.originalDamage();
        double current = context.result().damage(context.triggerData());
        double amount = Math.max(0.0D, getDouble("amount", 1.0D, context,
                "damage", String.valueOf(current),
                "original", String.valueOf(original)));
        AbilityDamageUtil.runDirectDamage(() -> living.damage(amount, getSourceEntity(context)));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
