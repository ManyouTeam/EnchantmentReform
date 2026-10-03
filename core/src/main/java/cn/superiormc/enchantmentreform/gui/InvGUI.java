package cn.superiormc.enchantmentreform.gui;

import cn.superiormc.enchantmentreform.methods.Dupe;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class InvGUI extends AbstractGUI implements InventoryHolder {

    protected Inventory inv;

    public String title;

    public InvGUI(Player player) {
        super(player);
    }

    public abstract boolean clickEventHandle(Inventory inventory, ClickType type, int slot);

    public void afterClickEventHandle(ItemStack item, ItemStack currentItem, int slot) {
        return;
    }

    public void closeEventHandle(Inventory inventory) {
        return;
    }

    @Override
    public void openGUI() {
        ConfigurationSection section = getSection();
        if (section != null) {
            if (!section.getBoolean("enabled", true)) {
                TextUtil.sendMessage(player, CommonUtil.parseLang(player, "{lang:menu-disabled}"));
                return;
            }
            String permission = section.getString("permission", "").strip();
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                TextUtil.sendMessage(player, CommonUtil.parseLang(player, "{lang:menu-no-permission}"));
                return;
            }
        }
        constructGUI();
        if (inv != null) {
            SchedulerUtil.runSync(player, () -> player.openInventory(inv));
        }
    }

    public @NotNull Inventory getInventory() {
        return inv;
    }

    public void setItem(int slot, ItemStack item) {
        inv.setItem(slot, Dupe.markGuiDisplayItem(item));
    }

    public ConfigurationSection getSection() {
        return null;
    }
}
