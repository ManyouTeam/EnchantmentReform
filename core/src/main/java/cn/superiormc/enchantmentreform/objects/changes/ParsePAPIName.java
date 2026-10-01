package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ParsePAPIName extends AbstractChangesRule {

    public ParsePAPIName() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (!singleChange.getBoolean("parse-papi-name")) {
            return singleChange.getItem();
        }
        ItemMeta meta = singleChange.getItemMeta();
        if (!meta.hasDisplayName()) {
            return singleChange.getItem();
        }
        EnchantmentReform.methodUtil.setItemName(meta, singleChange.parsePlaceholder(EnchantmentReform.methodUtil.getItemName(meta)), singleChange.getPlayer());
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("parse-papi-name");
    }
}
