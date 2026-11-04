package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public final class AnvilRenameTrigger extends EventTrigger<InventoryClickEvent> {

    public AnvilRenameTrigger() {
        super("anvil_rename", "on-anvil-rename", InventoryClickEvent.class);
    }

    @Override
    protected void handle(InventoryClickEvent event, TriggerRuntime runtime) {
        if (!(event.getInventory() instanceof AnvilInventory) || event.getRawSlot() != 2) {
            return;
        }
        ItemStack output = runtime.anvilOutput(event, "rename");
        if (output != null) {
            runtime.finishInventoryOutput(this, event, output);
        }
    }
}
