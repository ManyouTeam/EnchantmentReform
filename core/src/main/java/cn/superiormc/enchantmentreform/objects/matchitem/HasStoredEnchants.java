package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class HasStoredEnchants extends AbstractMatchItemRule {

    public HasStoredEnchants() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        if (!(meta instanceof EnchantmentStorageMeta)) {
            return false; // 不是附魔书直接返回 false
        }

        EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;

        for (String ench : section.getStringList("has-stored-enchants")) {
            if (ench.equals("*")) {
                return !storageMeta.getStoredEnchants().isEmpty();
            }
            Enchantment vanillaEnchant = Registry.ENCHANTMENT.get(CommonUtil.parseNamespacedKey(ench.toLowerCase()));
            if (vanillaEnchant != null && storageMeta.getStoredEnchants().containsKey(vanillaEnchant)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("has-stored-enchants").isEmpty();
    }
}