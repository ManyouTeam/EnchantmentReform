package cn.superiormc.enchantmentreform.objects.triggers;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;

public final class GrindstoneItemTrigger extends EventTrigger<InventoryClickEvent> {

    public GrindstoneItemTrigger() {
        super("grindstone_item", "on-grindstone-item", InventoryClickEvent.class);
    }

    @Override
    protected void handle(InventoryClickEvent event, TriggerRuntime runtime) {
        if (!(event.getWhoClicked() instanceof Player)
                || !(event.getInventory() instanceof GrindstoneInventory) || event.getRawSlot() != 2) {
            return;
        }
        ItemStack output = runtime.inventoryOutput(event, event.getCurrentItem(), List.of(), false);
        if (output == null) {
            return;
        }
        runtime.finishInventoryOutput(this, event, output);
    }
}
