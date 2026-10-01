package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import io.papermc.paper.event.player.PlayerItemGroupCooldownEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ItemGroupCooldownTrigger extends EventTrigger<PlayerItemGroupCooldownEvent> {

    public ItemGroupCooldownTrigger() {
        super("item_group_cooldown", "on-item-group-cooldown", PlayerItemGroupCooldownEvent.class);
    }

    @Override
    protected void handle(PlayerItemGroupCooldownEvent event, TriggerRuntime runtime) {
        Player player = event.getPlayer();
        TriggerData.Builder data = runtime.base(player, event)
                .source(player)
                .skill(player)
                .target(player)
                .location(player.getLocation());

        if (event instanceof PlayerItemCooldownEvent itemCooldown) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (mainHand.getType() == itemCooldown.getType()) {
                data.triggerItem(mainHand, EquipmentSlot.HAND);
            } else if (offHand.getType() == itemCooldown.getType()) {
                data.triggerItem(offHand, EquipmentSlot.OFF_HAND);
            }
        }

        runtime.fire(this, data);
    }
}
