package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.configuration.ConfigurationSection;

public final class PowerModifierElytraBoostNotConsume extends AbstractPowerModifier {

    public PowerModifierElytraBoostNotConsume(ConfigurationSection section) {
        super("elytra_boost_not_consume", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (context.event() instanceof PlayerElytraBoostEvent event) {
            event.setShouldConsume(false);
        }
    }

}
