package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KeepLore extends AbstractChangesRule {

    public KeepLore() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (singleChange.getBoolean("keep-lore")) {
            ItemMeta meta = singleChange.getItemMeta();
            ItemMeta originalMeta = singleChange.getOriginalMeta();
            if (originalMeta.hasLore()) {
                EnchantmentReform.methodUtil.setItemLore(meta,
                        EnchantmentReform.methodUtil.getItemLore(originalMeta),
                        singleChange.getPlayer());
            }
            return singleChange.setItemMeta(meta);
        }
        return singleChange.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("keep-lore");
    }
}
