package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class RepairDamage extends AbstractChangesRule {

    @Override
    public ItemStack setChange(ObjectSingleChange change) {
        if (!(change.getItemMeta() instanceof Damageable damageable)) {
            return change.getItem();
        }
        damageable.setDamage(Math.max(0, damageable.getDamage() - change.getInt("repair-damage", 0)));
        return change.setItemMeta(damageable);
    }
    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("repair-damage");
    }
}
