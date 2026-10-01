package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ContainsName extends AbstractMatchItemRule {
    public ContainsName() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        for (String requiredName : section.getStringList("contains-name")) {
            String itemName = EnchantmentReform.methodUtil.getItemName(meta);
            if (TextUtil.clear(itemName).contains(TextUtil.clear(requiredName))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("contains-name").isEmpty();
    }
}
