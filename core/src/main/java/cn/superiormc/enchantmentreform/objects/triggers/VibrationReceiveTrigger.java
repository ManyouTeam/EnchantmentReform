package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockReceiveGameEvent;

/**
 * Fires when a Sculk sensor is about to receive a vibration (game event) caused by a player.
 * <p>
 * The {@code BlockReceiveGameEvent} is fired for any entity that triggers a Sculk sensor; this
 * trigger narrows it down to player-caused vibrations, which are the ones that can propagate to
 * nearby Sculk shriekers and ultimately raise the warden warning level.
 */
public final class VibrationReceiveTrigger extends EventTrigger<BlockReceiveGameEvent> {

    public VibrationReceiveTrigger() {
        super("vibration_receive", "on-vibration-receive", BlockReceiveGameEvent.class);
    }

    @Override
    protected void handle(BlockReceiveGameEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(player)
                .target(event.getEntity())
                .location(event.getBlock().getLocation()));
    }
}
