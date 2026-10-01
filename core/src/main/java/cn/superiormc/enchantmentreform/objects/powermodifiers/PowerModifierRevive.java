package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

public final class PowerModifierRevive extends AbstractPowerModifier {

    public PowerModifierRevive(ConfigurationSection section) {
        super("revive", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        double maximum = context.source() instanceof LivingEntity living
                ? CommonUtil.getMaxHealth(living) : 20.0D;
        double health = getDouble("health", 20.0D, context,
                "original", String.valueOf(maximum));
        context.result().reviveHealth(Math.min(maximum, health));
        if (section.getBoolean("no-drops", false)) {
            context.result().clearDrops(true);
        }
    }
}
