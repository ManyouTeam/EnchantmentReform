package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ContainsEnchantsAmount extends AbstractMatchItemRule {

    public ContainsEnchantsAmount() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        int size;
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
            size = enchantmentStorageMeta.getStoredEnchants().size();
        } else {
            size = meta.getEnchants().size();
        }
        if (section.getString("contains-enchants-amount", "").startsWith("[")) {
            return section.getIntegerList("contains-enchants-amount").contains(size);
        }
        return size >= section.getInt("contains-enchants-amount");
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("contains-enchants-amount");
    }
}
