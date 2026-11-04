package cn.superiormc.enchantmentreform.objects.triggers;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public final class CraftItemTrigger extends EventTrigger<CraftItemEvent> {

    public CraftItemTrigger() {
        super("craft_item", "on-craft-item", CraftItemEvent.class);
    }

    @Override
    protected void handle(CraftItemEvent event, TriggerRuntime runtime) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        ItemStack output = runtime.inventoryOutput(event, event.getCurrentItem(),
                Arrays.asList(event.getInventory().getMatrix()), true);
        if (output == null) {
            return;
        }
        runtime.finishInventoryOutput(this, event, output);
    }
}
