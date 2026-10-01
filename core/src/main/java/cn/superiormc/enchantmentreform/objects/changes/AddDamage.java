package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class AddDamage extends AbstractChangesRule {

    @Override
    public ItemStack setChange(ObjectSingleChange change) {
        if (!(change.getItemMeta() instanceof Damageable damageable)) {
            return change.getItem();
        }
        damageable.setDamage(Math.max(0, Math.min(change.getItem().getType().getMaxDurability(),
                damageable.getDamage() + change.getInt("add-damage", 0))));
        return change.setItemMeta(damageable);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("add-damage");
    }
}
