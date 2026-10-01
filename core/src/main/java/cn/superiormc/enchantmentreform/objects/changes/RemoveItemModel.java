package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RemoveItemModel extends AbstractChangesRule {

    public RemoveItemModel() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (!singleChange.getBoolean("remove-item-model")) {
            return singleChange.getItem();
        }
        ItemMeta meta = singleChange.getItemMeta();
        meta.setItemModel(null);
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("remove-item-model");
    }
}
