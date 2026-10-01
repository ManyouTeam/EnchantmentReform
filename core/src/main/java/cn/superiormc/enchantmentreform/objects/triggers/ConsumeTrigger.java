package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerItemConsumeEvent;

public final class ConsumeTrigger extends EventTrigger<PlayerItemConsumeEvent> {

    public ConsumeTrigger() {
        super("consume", "on-consume", PlayerItemConsumeEvent.class);
    }

    @Override
    protected void handle(PlayerItemConsumeEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .triggerItem(event.getItem(), event.getHand()));
    }
}

