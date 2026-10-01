package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DeleteEnchants extends AbstractChangesRule {

    public DeleteEnchants() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        ConfigurationSection deleteEnchantsSection = singleChange.getConfigurationSection("delete-enchants");
        ItemMeta meta = singleChange.getItemMeta();
        for (String ench : deleteEnchantsSection.getKeys(false)) {
            Enchantment vanillaEnchant = Registry.ENCHANTMENT.get(CommonUtil.parseNamespacedKey(ench.toLowerCase()));
            if (vanillaEnchant == null || singleChange.getItem().getEnchantments().get(vanillaEnchant) == null) {
                continue;
            }
            if (deleteEnchantsSection.isList(ench)) {
                if (deleteEnchantsSection.getIntegerList(ench).contains(singleChange.getItem().getEnchantments().get(vanillaEnchant))) {
                    meta.removeEnchant(vanillaEnchant);
                }
            } else {
                if (singleChange.getItem().getEnchantments().get(vanillaEnchant) > deleteEnchantsSection.getInt(ench)) {
                    meta.removeEnchant(vanillaEnchant);
                }
            }
        }
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getConfigurationSection("delete-enchants") == null;
    }
}
