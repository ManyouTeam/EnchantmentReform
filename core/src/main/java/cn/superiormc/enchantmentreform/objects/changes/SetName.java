package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetName extends AbstractChangesRule {

    public SetName() {
        super();
    }

    @Override
    public ItemStack setChange(ObjectSingleChange singleChange) {
        if (singleChange.isFakeOrReal() && !ConfigManager.configManager.getBoolean("ignore-warning")) {
            ErrorManager.errorManager.sendErrorMessage("§6Warning: set-name rule has incompatibility issues with" +
                    " other packet based item plugins, it is recommend that remove it in fake changes from all your rule configs!" +
                    " If you want to ignore this warning, please disable warning display in config.yml file.");
        }
        ItemMeta meta = singleChange.getItemMeta();
        EnchantmentReform.methodUtil.setItemName(meta, singleChange.getString("set-name"), singleChange.getPlayer());
        return singleChange.setItemMeta(meta);
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getString("set-name") == null;
    }
}
