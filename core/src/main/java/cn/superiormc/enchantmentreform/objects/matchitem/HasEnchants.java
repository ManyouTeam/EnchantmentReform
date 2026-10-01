package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasEnchants extends AbstractMatchItemRule {

    public HasEnchants() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemMeta meta = match.getItemMeta();
        for (String ench : section.getStringList("has-enchants")) {
            if (ench.equals("*")) {
                return !meta.getEnchants().isEmpty();
            }
            Enchantment vanillaEnchant = Registry.ENCHANTMENT.get(CommonUtil.parseNamespacedKey(ench.toLowerCase()));
            if (vanillaEnchant != null && meta.getEnchants().containsKey(vanillaEnchant)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("has-enchants").isEmpty();
    }
}
