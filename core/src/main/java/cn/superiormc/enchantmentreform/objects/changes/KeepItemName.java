package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KeepItemName extends AbstractChangesRule {

    public KeepItemName() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (singleChange.getBoolean("keep-item-name")) {
            ItemMeta meta = singleChange.getItemMeta();
            ItemMeta originalMeta = singleChange.getOriginalMeta();
            if (originalMeta.hasItemName()) {
                meta.setItemName(originalMeta.getItemName());
            }
            return singleChange.setItemMeta(meta);
        }
        return singleChange.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("keep-item-name") && CommonUtil.getMinorVersion(20, 5);
    }
}
