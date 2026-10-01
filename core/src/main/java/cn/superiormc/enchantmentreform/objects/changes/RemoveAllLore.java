package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RemoveAllLore extends AbstractChangesRule {

    public RemoveAllLore() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (!singleChange.getBoolean("remove-all-lore")) {
            return singleChange.getItem();
        }
        ItemMeta meta = singleChange.getItemMeta();
        if (meta.hasLore()) {
            EnchantmentReform.methodUtil.setItemLore(meta, null, singleChange.getPlayer());
        }
        singleChange.setItemMeta(meta);
        return singleChange.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("remove-all-lore");
    }
}
