package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;

public final class PowerModifierExperience extends AbstractPowerModifier {

    public PowerModifierExperience(ConfigurationSection section) {
        super("experience", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (context.event() instanceof PlayerExpChangeEvent
                || context.event() instanceof BlockBreakEvent
                || context.event() instanceof EntityDeathEvent
                || context.event() instanceof PlayerFishEvent
                || context.event() instanceof EnchantItemEvent) {
            context.result().experienceAmount(value(context,
                    context.result().experienceAmount(context.triggerData())));
        }
    }

    private int value(PowerContext context, int original) {
        return Math.max(0, (int) Math.round(modifyValue(context, original)));
    }
}
