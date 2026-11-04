package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;

import org.bukkit.event.player.PlayerLevelChangeEvent;

public final class GainLevelTrigger extends EventTrigger<PlayerLevelChangeEvent> {

    public GainLevelTrigger() {
        super("gain_level", "on-gain-level", PlayerLevelChangeEvent.class);
    }

    @Override
    protected void handle(PlayerLevelChangeEvent event, TriggerRuntime runtime) {
        int gained = event.getNewLevel() - event.getOldLevel();
        if (gained <= 0) {
            return;
        }
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .extra(BuiltinContextKeys.LEVELS_GAINED, gained));
    }
}
