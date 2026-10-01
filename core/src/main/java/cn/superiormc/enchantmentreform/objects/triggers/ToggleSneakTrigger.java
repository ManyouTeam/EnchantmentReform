package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerToggleSneakEvent;

public final class ToggleSneakTrigger extends EventTrigger<PlayerToggleSneakEvent> {

    public ToggleSneakTrigger() {
        super("toggle_sneak", "on-toggle-sneak", PlayerToggleSneakEvent.class);
    }

    @Override
    protected void handle(PlayerToggleSneakEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event));
    }
}

