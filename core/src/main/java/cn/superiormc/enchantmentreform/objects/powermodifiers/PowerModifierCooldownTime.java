package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import io.papermc.paper.event.player.PlayerItemGroupCooldownEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityExhaustionEvent;

public final class PowerModifierCooldownTime extends AbstractPowerModifier {

    public PowerModifierCooldownTime(ConfigurationSection section) {
        super("cooldown_time", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof PlayerItemGroupCooldownEvent event)) {
            return;
        }
        int modified = (int) Math.round(modifyValue(context, event.getCooldown()));
        event.setCooldown(Math.min(40, Math.max(0, modified)));
    }
}
