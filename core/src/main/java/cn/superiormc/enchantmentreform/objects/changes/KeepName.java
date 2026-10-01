package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KeepName extends AbstractChangesRule {

    public KeepName() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (singleChange.getBoolean("keep-name")) {
            ItemMeta meta = singleChange.getItemMeta();
            ItemMeta originalMeta = singleChange.getOriginalMeta();
            if (originalMeta.hasDisplayName()) {
                EnchantmentReform.methodUtil.setItemName(meta, EnchantmentReform.methodUtil.getItemName(originalMeta), singleChange.getPlayer());
            }
            return singleChange.setItemMeta(meta);
        }
        return singleChange.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("keep-name");
    }
}
