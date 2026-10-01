package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Deapply extends AbstractChangesRule {
    private static final NamespacedKey KEY = new NamespacedKey(EnchantmentReform.instance, "apply_rule");
    @Override public ItemStack setChange(ObjectSingleChange change) {
        if (!change.getBoolean("deapply", false)) return change.getItem();
        ItemMeta meta = change.getItemMeta();
        meta.getPersistentDataContainer().remove(KEY);
        return change.setItemMeta(meta);
    }
    @Override public boolean configNotContains(ConfigurationSection section) { return !section.contains("deapply"); }
}
