package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasLore extends AbstractMatchItemRule {

    public HasLore() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        if (section.getBoolean("has-lore", false)) {
            return meta.hasLore();
        } else {
            return !meta.hasLore();
        }
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.get("has-lore") == null;
    }
}
