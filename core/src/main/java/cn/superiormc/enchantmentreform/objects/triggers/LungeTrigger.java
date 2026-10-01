package cn.superiormc.enchantmentreform.objects.triggers;

import io.papermc.paper.event.entity.EntityLungeEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class LungeTrigger extends EventTrigger<EntityLungeEvent> {

    public LungeTrigger() {
        super("lunge", "on-lunge", EntityLungeEvent.class);
    }

    @Override
    protected void handle(EntityLungeEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        EquipmentSlot hand = player.getActiveItemHand();
        ItemStack item = hand == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
        if (player.hasActiveItem()) {
            ItemStack activeItem = player.getActiveItem();
            if (!activeItem.getType().isAir()) {
                hand = player.getActiveItemHand();
                item = activeItem;
            }
        }

        runtime.fire(this, runtime.base(player, event)
                .triggerItem(item, hand));
    }
}
