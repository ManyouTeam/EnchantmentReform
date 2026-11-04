package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public final class AnvilRepairTrigger extends EventTrigger<InventoryClickEvent> {

    public AnvilRepairTrigger() {
        super("anvil_repair", "on-anvil-repair", InventoryClickEvent.class);
    }

    @Override
    protected void handle(InventoryClickEvent event, TriggerRuntime runtime) {
        if (!(event.getInventory() instanceof AnvilInventory) || event.getRawSlot() != 2) {
            return;
        }
        ItemStack output = runtime.anvilOutput(event, "repair");
        if (output != null) {
            runtime.finishInventoryOutput(this, event, output);
        }
    }
}
