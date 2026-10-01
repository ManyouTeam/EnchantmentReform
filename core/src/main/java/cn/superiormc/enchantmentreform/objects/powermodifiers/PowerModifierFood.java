package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierFood extends AbstractPowerModifier {

    public PowerModifierFood(ConfigurationSection section) {
        super("food", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().foodLevel(Math.min(20, Math.max(0, (int) Math.round(
                modifyValue(context, context.result().foodLevel(context.triggerData()))))));
    }
}
