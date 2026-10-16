package cn.superiormc.enchantmentreform.paper.methods;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.paper.adapts.PaperComponentAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BuildItemPaper {

    public static ItemStack buildItemStack(Player player,
                                           ConfigurationSection section,
                                           int amount,
                                           String... args) {
        ItemStack item = BuildItem.buildItemStack(player, section, args);
        int itemAmount = amount > 0 ? amount : section.getInt("amount", 1);
        if (itemAmount > 0) {
            item.setAmount(itemAmount);
        }
        return item;
    }

    public static ItemStack editItemStack(ItemStack item,
                                          Player player,
                                          ConfigurationSection section,
                                          int amount,
                                          String... args) {
        if (amount > 0) {
            item.setAmount(amount);
        }
        return PaperComponentAdapter.editItemStack(item, player, section, args);
    }

}
