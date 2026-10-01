package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockReceiveGameEvent;

public final class PowerModifierVibrationReduce extends AbstractPowerModifier {

    public PowerModifierVibrationReduce(ConfigurationSection section) {
        super("vibration_reduce", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof BlockReceiveGameEvent event)) {
            return;
        }
        event.setCancelled(true);
    }
}
