package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierRadius extends AbstractPowerModifier {

    public PowerModifierRadius(ConfigurationSection section) {
        super("radius", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().explosionRadius((float) Math.max(0.0D, modifyValue(context,
                context.result().explosionRadius(context.triggerData()))));
    }
}
