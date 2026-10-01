package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class SwapHandTrigger extends EventTrigger<PlayerSwapHandItemsEvent> {

    public SwapHandTrigger() {
        super("swap_hand", "on-swap-hand", PlayerSwapHandItemsEvent.class);
    }

    @Override
    protected void handle(PlayerSwapHandItemsEvent event, TriggerRuntime runtime) {
        runtime.fire(this, runtime.base(event.getPlayer(), event)
                .triggerItem(event.getMainHandItem(), EquipmentSlot.HAND));
    }
}

