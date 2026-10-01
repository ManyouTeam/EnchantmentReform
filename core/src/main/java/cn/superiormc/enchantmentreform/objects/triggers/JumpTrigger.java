package cn.superiormc.enchantmentreform.objects.triggers;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;

public final class JumpTrigger extends EventTrigger<PlayerJumpEvent> {

    public JumpTrigger() {
        super("jump", "on-jump", PlayerJumpEvent.class);
    }

    @Override
    protected void handle(PlayerJumpEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.movement(
                event.getPlayer(), event, event.getFrom(), event.getTo()));
    }
}

