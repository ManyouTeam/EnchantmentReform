package cn.superiormc.enchantmentreform.objects.triggers;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.inventory.ItemStack;

public final class ForgeItemTrigger extends EventTrigger<SmithItemEvent> {

    public ForgeItemTrigger() {
        super("forge_item", "on-forge-item", SmithItemEvent.class);
    }

    @Override
    protected void handle(SmithItemEvent event, TriggerRuntime runtime) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        ItemStack output = runtime.inventoryOutput(event, event.getCurrentItem(), List.of(), false);
        if (output == null) {
            return;
        }
        runtime.finishInventoryOutput(this, event, output);
    }
}
