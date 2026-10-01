package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerToggleFlightEvent;

public final class ToggleFlightTrigger extends EventTrigger<PlayerToggleFlightEvent> {

    public ToggleFlightTrigger() {
        super("toggle_flight", "on-toggle-flight", PlayerToggleFlightEvent.class);
    }

    @Override
    protected void handle(PlayerToggleFlightEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event));
    }
}

