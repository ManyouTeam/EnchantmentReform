package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockReceiveGameEvent;

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
