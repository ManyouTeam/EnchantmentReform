package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class PowerModifierYield extends AbstractPowerModifier {

    public PowerModifierYield(ConfigurationSection section) {
        super("yield", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (context.event() instanceof EntityExplodeEvent) {
            context.result().explosionYield((float) Math.max(0.0D, modifyValue(context,
                    context.result().explosionYield(context.triggerData()))));
        }
    }
}
