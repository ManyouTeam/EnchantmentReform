package cn.superiormc.enchantmentreform.objects.buttons.subobjects;

import cn.superiormc.enchantmentreform.methods.BuildItem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ObjectDisplayItem {

    private final ConfigurationSection config;

    public ObjectDisplayItem(ConfigurationSection config) {
        this.config = config;
    }

    public ItemStack build(Player player) {
        return BuildItem.buildItemStack(player, config);
    }

    public ConfigurationSection getConfig() {
        return config;
    }

}
