package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public final class PowerModifierArmorPierce extends AbstractPowerModifier {

    public PowerModifierArmorPierce(ConfigurationSection section) {
        super("armor_pierce", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.target() instanceof LivingEntity target)) {
            return;
        }
        AttributeInstance armorAttribute = target.getAttribute(Attribute.ARMOR);
        AttributeInstance toughnessAttribute = target.getAttribute(Attribute.ARMOR_TOUGHNESS);
        double armor = armorAttribute == null ? 0.0D : armorAttribute.getValue();
        double toughness = toughnessAttribute == null ? 0.0D : toughnessAttribute.getValue();
        double penetration = Math.max(0.0D, Math.min(100.0D, getDouble("percent", 0.0D, context))) / 100.0D;
        if (armor <= 0.0D || penetration <= 0.0D) {
            return;
        }
        double damage = context.result().damage(context.triggerData());
        context.result().damage(compensate(damage, armor, toughness, penetration));
    }

    private double compensate(double damage, double armor, double toughness, double penetration) {
        double normal = reduction(damage, armor, toughness);
        double pierced = reduction(damage, armor * (1.0D - penetration), toughness);
        return normal >= 0.999D ? damage : damage * (1.0D - pierced) / (1.0D - normal);
    }

    private double reduction(double damage, double armor, double toughness) {
        double effective = Math.min(20.0D, Math.max(armor / 5.0D, armor - damage / (2.0D + toughness / 4.0D)));
        return effective / 25.0D;
    }
}
