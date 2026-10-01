package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierHeal extends AbstractPowerModifier {

    public PowerModifierHeal(ConfigurationSection section) {
        super("heal", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().regainAmount(Math.max(0.0D, modifyValue(context,
                context.result().regainAmount(context.triggerData()))));
    }
}
