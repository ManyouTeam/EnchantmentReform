package cn.superiormc.enchantmentreform.hooks.items;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ItemMMOItemsHook extends AbstractItemHook {
    public ItemMMOItemsHook() { super("MMOItems"); }
    @Override public ItemStack getHookItemByID(Player player, String hookItemID) {
        String[] split = hookItemID.split(";;", 2);
        if (split.length != 2) return returnNullItem(hookItemID);
        ItemStack item = MMOItems.plugin.getItem(split[0], split[1]);
        return item == null ? returnNullItem(hookItemID) : item;
    }
    @Override public String getIDByItemStack(ItemStack item) {
        String id = MMOItems.getID(item);
        String type = MMOItems.getTypeName(item);
        return id == null || id.isEmpty() || type == null || type.isEmpty() ? null : type + ";;" + id;
    }
}
