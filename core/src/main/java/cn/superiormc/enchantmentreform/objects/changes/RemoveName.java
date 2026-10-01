package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RemoveName extends AbstractChangesRule {

    public RemoveName() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (!singleChange.getBoolean("remove-name")) {
            return singleChange.getItem();
        }
        ItemMeta meta = singleChange.getItemMeta();
        if (meta.hasDisplayName()) {
            EnchantmentReform.methodUtil.setItemName(meta, null, singleChange.getPlayer());
        }
        singleChange.setItemMeta(meta);
        return singleChange.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("remove-name");
    }
}
