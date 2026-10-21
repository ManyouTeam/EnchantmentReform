package cn.superiormc.enchantmentreform.gui;

import cn.superiormc.enchantmentreform.methods.Dupe;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
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

import java.util.ArrayList;
import java.util.List;

public abstract class InvGUI extends AbstractGUI implements InventoryHolder {

    protected Inventory inv;

    protected boolean bypassPermission;

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
            if (!bypassPermission && !permission.isEmpty() && !player.hasPermission(permission)) {
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

    public void openGUI(boolean bypass) {
        bypassPermission = bypass;
        openGUI();
    }

    public void setItem(int slot, ItemStack item) {
        inv.setItem(slot, Dupe.markGuiDisplayItem(item));
    }

    protected final List<String> wrapLore(List<String> configured) {
        List<String> result = new ArrayList<>();
        int wrapLength = ConfigManager.configManager.getInt(
                "enchantment-description.wrap-length", 30);
        for (String value : configured) {
            if (value.isBlank()) {
                result.add("");
                continue;
            }
            for (String line : value.split("\\\\n", -1)) {
                result.addAll(TextUtil.wrapKeepColor(line, wrapLength));
            }
        }
        return result;
    }

    protected final String[] merge(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public ConfigurationSection getSection() {
        return null;
    }
}
