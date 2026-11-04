package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerItemBreakEvent;

public final class ItemBrokenTrigger extends EventTrigger<PlayerItemBreakEvent> {

    public ItemBrokenTrigger() {
        super("item_broken", "on-item-broken", PlayerItemBreakEvent.class);
    }

    @Override
    protected void handle(PlayerItemBreakEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .triggerItem(event.getBrokenItem().clone(), runtime.findSlot(event.getPlayer(), event.getBrokenItem())));
    }
}
