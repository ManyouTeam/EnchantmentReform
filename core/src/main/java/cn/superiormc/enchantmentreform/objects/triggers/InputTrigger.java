package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerInputEvent;

public final class InputTrigger extends EventTrigger<PlayerInputEvent> {

    public InputTrigger() {
        super("input", "on-input", PlayerInputEvent.class);
    }

    @Override
    protected void handle(PlayerInputEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event));
    }
}
