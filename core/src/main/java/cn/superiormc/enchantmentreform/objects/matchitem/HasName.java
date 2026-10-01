package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasName extends AbstractMatchItemRule {

    public HasName() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        if (section.getBoolean("has-name", false)) {
            return meta.hasDisplayName();
        } else {
            return !meta.hasDisplayName();
        }
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.get("has-name") == null;
    }
}
