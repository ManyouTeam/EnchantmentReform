package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.gui.InvGUI;
import cn.superiormc.enchantmentreform.gui.inv.EnchantmentInfoGUI;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUIListener implements Listener {

    private static final String ADMIN_PERMISSION = "enchantmentreform.admin";

    private static final Pattern ENCHANTMENT_KEY_PATTERN =
            Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)", Pattern.CASE_INSENSITIVE);

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            try {
                Inventory topInventory = e.getView().getTopInventory();
                InvGUI gui = topInventory.getHolder() instanceof InvGUI ? (InvGUI) topInventory.getHolder() : null;
                if (gui == null) {
                    return;
                }
                if (!topInventory.equals(gui.getInventory())) {
                    player.closeInventory();
                    ErrorManager.errorManager.sendErrorMessage("§cError: Found unregistered GUI Listener, now force close the inventory and then delete the excess GUI Listener. If this always heppens, please report to the plugin author.");
                    return;
                }
                if (!Objects.equals(e.getClickedInventory(), gui.getInventory())) {
                    if (e.getClick().isShiftClick() || e.getClick() == ClickType.DOUBLE_CLICK || ConfigManager.configManager.getBoolean("choose-prefix-gui.forbid-click-outside")) {
                        e.setCancelled(true);
                    }
                    return;
                }
                if (e.getClick() == ClickType.DOUBLE_CLICK) {
                    e.setCancelled(true);
                    return;
                }
                if (gui.clickEventHandle(e.getClickedInventory(), e.getClick(), e.getSlot())) {
                    e.setCancelled(true);
                }
                gui.afterClickEventHandle(e.getCursor(), e.getCurrentItem(), e.getSlot());
                if (gui instanceof EnchantmentInfoGUI) {
                    giveHighestLevelEnchantmentBook(player, e.getCurrentItem());
                }
                if (e.getClick().toString().equals("SWAP_OFFHAND") && e.isCancelled()) {
                    player.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
                }
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Your menu configs has wrong, error message: " +
                        throwable.getMessage());
                throwable.printStackTrace();
                e.setCancelled(true);
            }
        }
    }

    private static void giveHighestLevelEnchantmentBook(Player player, ItemStack clickedItem) {
        if (!player.hasPermission(ADMIN_PERMISSION)
                || clickedItem == null
                || clickedItem.getType() != Material.ENCHANTED_BOOK) {
            return;
        }
        ItemMeta clickedMeta = clickedItem.getItemMeta();
        List<String> lore = clickedMeta == null ? null : clickedMeta.getLore();
        if (lore == null) {
            return;
        }
        for (int index = lore.size() - 1; index >= 0; index--) {
            String plain = ChatColor.stripColor(lore.get(index));
            if (plain == null) {
                continue;
            }
            Matcher matcher = ENCHANTMENT_KEY_PATTERN.matcher(plain);
            while (matcher.find()) {
                NamespacedKey key = NamespacedKey.fromString(matcher.group(1).toLowerCase(Locale.ROOT));
                Enchantment enchantment = key == null ? null : Registry.ENCHANTMENT.get(key);
                if (enchantment == null) {
                    continue;
                }
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                if (!(book.getItemMeta() instanceof EnchantmentStorageMeta bookMeta)) {
                    return;
                }
                bookMeta.addStoredEnchant(enchantment, enchantment.getMaxLevel(), true);
                book.setItemMeta(bookMeta);
                player.getInventory().addItem(book).values().forEach(overflow ->
                        player.getWorld().dropItemNaturally(player.getLocation(), overflow));
                return;
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            Inventory topInventory = e.getView().getTopInventory();
            InvGUI gui = topInventory.getHolder() instanceof InvGUI ? (InvGUI) topInventory.getHolder() : null;
            if (gui == null) {
                return;
            }
            if (!topInventory.equals(gui.getInventory())) {
                player.closeInventory();
                ErrorManager.errorManager.sendErrorMessage("§cError: Found unregistered GUI Listener, now force close the inventory and then delete the excess GUI Listener. If this always heppens, please report to the plugin author.");
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            Inventory topInventory = e.getView().getTopInventory();
            InvGUI gui = topInventory.getHolder() instanceof InvGUI ? (InvGUI) topInventory.getHolder() : null;
            if (gui == null) {
                return;
            }
            if (!Objects.equals(e.getInventory(), gui.getInventory())) {
                return;
            }
            gui.closeEventHandle(e.getInventory());
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e){
        Inventory topInventory = e.getPlayer().getOpenInventory().getTopInventory();
        if (topInventory.getHolder() instanceof InvGUI) {
            e.setCancelled(true);
        }
    }
}
