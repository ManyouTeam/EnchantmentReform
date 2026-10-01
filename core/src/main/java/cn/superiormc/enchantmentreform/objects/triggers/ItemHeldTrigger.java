package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ItemHeldTrigger extends EventTrigger<PlayerItemHeldEvent> {

    public ItemHeldTrigger() {
        super("item_held", "on-item-held", PlayerItemHeldEvent.class);
    }

    @Override
    protected void handle(PlayerItemHeldEvent event, TriggerRuntime runtime) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .triggerItem(item, EquipmentSlot.HAND));
    }
}

