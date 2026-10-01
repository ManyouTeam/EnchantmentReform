package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import org.bukkit.event.player.PlayerInteractEvent;

public final class InteractTrigger extends EventTrigger<PlayerInteractEvent> {

    public InteractTrigger() {
        super("interact", "on-interact", PlayerInteractEvent.class);
    }

    @Override
    protected void handle(PlayerInteractEvent event, TriggerRuntime runtime) {
        TriggerData.Builder data = runtime.base(event.getPlayer(), event)
                .block(event.getClickedBlock()).triggerItem(event.getItem(), event.getHand());
        if (event.getClickedBlock() != null) {
            data.location(event.getClickedBlock().getLocation().add(0.5D, 0.5D, 0.5D));
        }
        runtime.fire(this, data);
    }
}

