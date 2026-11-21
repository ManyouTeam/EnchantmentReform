package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantabilityOverrideListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        int slot = event.getSlot();
        Player player = event.getPlayer();
        ItemStack item = event.getNewItemStack();
        if (ConfigManager.configManager.getBoolean("item-enchantability-overrides.enabled") && ConfigManager.configManager.getBoolean("item-enchantability-overrides.ignore-equipment-when-open-other-invenotry", true)
                && !(player.getOpenInventory().getTopInventory() instanceof CraftingInventory)) {
            switch (slot) {
                case 5:
                    if (player.getInventory().getItem(EquipmentSlot.HEAD).hashCode() == item.hashCode()) {
                        return;
                    }
                    break;
                case 6:
                    if (player.getInventory().getItem(EquipmentSlot.CHEST).hashCode() == item.hashCode()) {
                        return;
                    }
                    break;
                case 7:
                    if (player.getInventory().getItem(EquipmentSlot.LEGS).hashCode() == item.hashCode()) {
                        return;
                    }
                    break;
                case 8:
                    if (player.getInventory().getItem(EquipmentSlot.FEET).hashCode() == item.hashCode()) {
                        return;
                    }
                    break;
                case 45:
                    if (player.getInventory().getItem(EquipmentSlot.OFF_HAND).hashCode() == item.hashCode()) {
                        return;
                    }
                    break;
            }
        }
        apply(player, slot, item);
    }

    private void apply(Player player, int slot, ItemStack item) {
        if (item == null || item.getType().isAir()
                || slot < 0 || slot >= player.getInventory().getSize()) {
            return;
        }
        Integer enchantability = ConfigManager.configManager.getEnchantabilityOverride(item.getType());
        if (enchantability == null) {
            return;
        }
        ItemMeta currentMeta = item.getItemMeta();
        if (currentMeta.hasEnchantable() && currentMeta.getEnchantable() == enchantability) {
            return;
        }
        ItemStack updated = item.clone();
        ItemMeta updatedMeta = updated.getItemMeta();
        updatedMeta.setEnchantable(enchantability);
        updated.setItemMeta(updatedMeta);
        player.getInventory().setItem(slot, updated);
    }
}
