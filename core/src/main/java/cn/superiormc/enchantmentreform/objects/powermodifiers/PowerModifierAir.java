package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierAir extends AbstractPowerModifier {

    public PowerModifierAir(ConfigurationSection section) {
        super("air", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().airAmount(Math.max(0, (int) Math.round(modifyValue(context,
                context.result().airAmount(context.triggerData())))));
    }
}
