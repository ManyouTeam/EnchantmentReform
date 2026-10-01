package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ContainsLore extends AbstractMatchItemRule {

    public ContainsLore() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }
        for (String hasLore : EnchantmentReform.methodUtil.getItemLore(meta)) {
            for (String requiredLore : section.getStringList("contains-lore")) {
                if (TextUtil.clear(hasLore).contains(TextUtil.clear(requiredLore))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("contains-lore").isEmpty();
    }
}
