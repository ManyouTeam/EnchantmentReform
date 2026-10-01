package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class EnchantabilityOverrideListener implements Listener {

    private volatile Map<Material, Integer> overrides = Map.of();

    public EnchantabilityOverrideListener(ConfigurationSection section) {
        reload(section);
    }

    public void reload(ConfigurationSection section) {
        overrides = loadOverrides(section);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        int slot = event.getSlot();
        Player player = event.getPlayer();
        ItemStack item = event.getNewItemStack();
        if (ConfigManager.configManager.getBoolean("item-enchantability-overrides.ignore-equipment-when-open-other-invenotry", true) &&
                !(player.getOpenInventory().getTopInventory() instanceof CraftingInventory)) {
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
        Integer enchantability = overrides.get(item.getType());
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

    private static Map<Material, Integer> loadOverrides(ConfigurationSection section) {
        if (section == null || !section.getBoolean("enabled", true)) {
            return Map.of();
        }
        ConfigurationSection materials = section.getConfigurationSection("materials");
        if (materials == null) {
            return Map.of();
        }
        Map<Material, Integer> result = new EnumMap<>(Material.class);
        for (String key : materials.getKeys(false)) {
            Material material = parseMaterial(key);
            if (material == null || material.isAir() || !material.isItem()) {
                continue;
            }
            Object configured = materials.get(key);
            if (!(configured instanceof Number number) || number.intValue() <= 0) {
                continue;
            }
            result.put(material, number.intValue());
        }
        return Map.copyOf(result);
    }

    private static Material parseMaterial(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.strip();
        Material material = Material.matchMaterial(normalized);
        if (material != null) {
            return material;
        }
        int separator = normalized.indexOf(':');
        if (separator >= 0 && separator + 1 < normalized.length()) {
            normalized = normalized.substring(separator + 1);
        }
        return Material.matchMaterial(normalized.toUpperCase(Locale.ROOT));
    }
}
