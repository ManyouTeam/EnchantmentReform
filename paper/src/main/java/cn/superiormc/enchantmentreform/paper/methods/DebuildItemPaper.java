package cn.superiormc.enchantmentreform.paper.methods;

import cn.superiormc.enchantmentreform.paper.adapts.PaperComponentAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

public class DebuildItemPaper {

    public static ConfigurationSection serializeItemStack(ItemStack item) {
        return PaperComponentAdapter.serializeItemStack(item, new MemoryConfiguration());
    }

}
