package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class NameEntityTrigger extends EventTrigger<PlayerNameEntityEvent> {

    public NameEntityTrigger() {
        super("name_entity", "on-name-entity", PlayerNameEntityEvent.class);
    }

    @Override
    protected void handle(PlayerNameEntityEvent event, TriggerRuntime runtime) {
        Player player = event.getPlayer();
        TriggerData.Builder data = runtime.base(player, event)
                .source(player)
                .skill(player)
                .target(event.getEntity())
                .location(event.getEntity().getLocation());

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (mainHand.getType() == Material.NAME_TAG) {
            data.triggerItem(mainHand, EquipmentSlot.HAND);
        } else if (offHand.getType() == Material.NAME_TAG) {
            data.triggerItem(offHand, EquipmentSlot.OFF_HAND);
        }

        runtime.fire(this, data);
    }
}
