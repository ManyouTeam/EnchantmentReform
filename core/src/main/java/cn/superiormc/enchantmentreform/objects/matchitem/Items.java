package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Items extends AbstractMatchItemRule {

    public Items() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection section = match.getSection();
        ItemStack item = match.getItem();
        ItemMeta meta = match.getItemMeta();
        if (ConfigManager.configManager.getBoolean("debug")) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §cItem ID: " +
                    HookManager.hookManager.parseItemID(item, section.getBoolean("use-tier-identify", false)));
        }
        return section.getStringList("items").contains(
                HookManager.hookManager.parseItemID(item, section.getBoolean("use-tier-identify", false)));
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("items").isEmpty();
    }
}
