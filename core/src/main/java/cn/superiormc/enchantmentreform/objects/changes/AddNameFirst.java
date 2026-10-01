package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AddNameFirst extends AbstractChangesRule {

    public AddNameFirst() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (ConfigManager.configManager.getBoolean("keep-name-in-anvil") && !singleChange.isPlayerInventory() &&
                singleChange.getPlayer().getOpenInventory().getType().equals(InventoryType.ANVIL)) {
            return singleChange.getItem();
        }
        ItemMeta meta = singleChange.getItemMeta();
        String tempVal1;
        if (!meta.hasDisplayName()) {
            tempVal1 = "§f" + singleChange.getString("add-name-first") + EnchantmentReform.methodUtil.getItemName(meta);
        } else {
            tempVal1 = singleChange.getString("add-name-first") + EnchantmentReform.methodUtil.getItemName(meta);
        }
        EnchantmentReform.methodUtil.setItemName(meta, tempVal1, singleChange.getPlayer());
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getString("add-name-first") == null;
    }
}
