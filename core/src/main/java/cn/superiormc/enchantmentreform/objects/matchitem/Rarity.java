package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Rarity extends AbstractMatchItemRule {

    public Rarity() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        String result;
        if (meta.hasRarity()) {
            ItemRarity rarity = meta.getRarity();
            result = rarity.name();
        } else {
            result = "NONE";
        }
        return result.equalsIgnoreCase(section.getString("rarity"));
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getString("rarity") == null;
    }
}
