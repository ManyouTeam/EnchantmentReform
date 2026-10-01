package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierDamage extends AbstractPowerModifier {

    public PowerModifierDamage(ConfigurationSection section) {
        super("damage", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().damage(Math.max(0.0D, modifyValue(context,
                context.result().damage(context.triggerData()))));
    }
}
