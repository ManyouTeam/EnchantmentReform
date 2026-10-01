package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierItemDamage extends AbstractPowerModifier {

    public PowerModifierItemDamage(ConfigurationSection section) {
        super("item_damage", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        context.result().itemDamage(Math.max(0, (int) Math.round(modifyValue(context,
                context.result().itemDamage(context.triggerData())))));
    }
}
