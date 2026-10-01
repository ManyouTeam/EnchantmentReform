package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;

public final class PowerModifierFishHookTiming extends AbstractPowerModifier {

    public PowerModifierFishHookTiming(ConfigurationSection section) {
        super("fishing_hook_timing", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof PlayerFishEvent event)) {
            return;
        }
        double multiplier = Math.max(0.05D, getDouble("multiplier", 1.0D, context));
        if (multiplier == 1.0D) {
            return;
        }
        FishHook hook = event.getHook();
        hook.setMinWaitTime(Math.max(1, (int) Math.round(hook.getMinWaitTime() * multiplier)));
        hook.setMaxWaitTime(Math.max(hook.getMinWaitTime(),
                (int) Math.round(hook.getMaxWaitTime() * multiplier)));
    }
}
