package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class SetAirAbility extends AbstractAbility {

    public SetAirAbility(ConfigurationSection section) {
        super("SetAir", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        int maxAir = living.getMaximumAir();
        int air = living.getRemainingAir();
        int amount = (int) getDouble("amount", air, context,
                "air", String.valueOf(air),
                "max-air", String.valueOf(maxAir));
        living.setRemainingAir(Math.max(-20, Math.min(maxAir, amount)));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
