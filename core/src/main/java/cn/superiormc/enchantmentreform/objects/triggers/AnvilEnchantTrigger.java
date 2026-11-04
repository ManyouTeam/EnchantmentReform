package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public final class AnvilEnchantTrigger extends EventTrigger<InventoryClickEvent> {

    public AnvilEnchantTrigger() {
        super("anvil_enchant", "on-anvil-enchant", InventoryClickEvent.class);
    }

    @Override
    protected void handle(InventoryClickEvent event, TriggerRuntime runtime) {
        if (!(event.getInventory() instanceof AnvilInventory) || event.getRawSlot() != 2) {
            return;
        }
        ItemStack output = runtime.anvilOutput(event, "enchant");
        if (output != null) {
            runtime.finishInventoryOutput(this, event, output);
        }
    }
}
