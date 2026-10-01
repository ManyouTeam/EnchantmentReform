package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AddLoreLast extends AbstractChangesRule {

    public AddLoreLast() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        ItemMeta meta = singleChange.getItemMeta();
        List<String> itemLore = EnchantmentReform.methodUtil.getItemLore(meta);
        if (itemLore == null) {
            itemLore = new ArrayList<>();
        }
        itemLore.addAll(singleChange.getStringList("add-lore-last"));
        EnchantmentReform.methodUtil.setItemLore(meta, itemLore, singleChange.getPlayer());;
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("add-lore-last").isEmpty();
    }
}
