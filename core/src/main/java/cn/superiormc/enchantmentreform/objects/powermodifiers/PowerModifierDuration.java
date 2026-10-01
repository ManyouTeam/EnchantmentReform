package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierDuration extends AbstractPowerModifier {

    public PowerModifierDuration(ConfigurationSection section) {
        super("duration", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().combustDuration(Math.max(0, (int) Math.round(modifyValue(context,
                context.result().combustDuration(context.triggerData())))));
    }
}
