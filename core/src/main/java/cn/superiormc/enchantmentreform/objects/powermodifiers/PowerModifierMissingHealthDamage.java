package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public final class PowerModifierMissingHealthDamage extends AbstractPowerModifier {

    public PowerModifierMissingHealthDamage(ConfigurationSection section) {
        super("missing_health_damage", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.source() instanceof LivingEntity source)) {
            return;
        }
        double maximum = CommonUtil.getMaxHealth(source);
        double missing = maximum <= 0 ? 0 : 100.0D - source.getHealth() / maximum * 100.0D;
        double steps = Math.floor(missing / Math.max(0.1D, getDouble("step-percent", 10.0D, context)));
        double bonus = Math.min(getDouble("maximum-percent", 100.0D, context), steps * getDouble("percent-per-step", 5.0D, context));
        double multiplier = 1.0D + bonus / 100.0D;
        context.result().damage(
                context.result().damage(context.triggerData()) * multiplier);
    }
}
