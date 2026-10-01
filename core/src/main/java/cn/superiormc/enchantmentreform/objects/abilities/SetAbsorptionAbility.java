package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class SetAbsorptionAbility extends AbstractAbility {

    public SetAbsorptionAbility(ConfigurationSection section) {
        super("SetAbsorption", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof Damageable living)) {
            return false;
        }
        if (living.getHealth() <= 0) {
            return false;
        }
        double max = CommonUtil.getMaxAbsorption(living);
        double heal = getDouble("amount", 10.0, context, "health", String.valueOf(living.getAbsorptionAmount()), "max-health", String.valueOf(max));
        if (heal >= 2048) {
            heal = 2048;
        }
        living.setAbsorptionAmount(Math.min(max, heal));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
