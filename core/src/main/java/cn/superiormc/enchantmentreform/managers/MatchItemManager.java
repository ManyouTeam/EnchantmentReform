package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.matchitem.*;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashSet;

public class MatchItemManager extends AbstractManager {

    public static MatchItemManager matchItemManager;

    private final Collection<AbstractMatchItemRule> rules = new HashSet<>();

    public MatchItemManager() {
        matchItemManager = this;
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        registerNewRule(new ContainsLore());
        registerNewRule(new ContainsName());
        registerNewRule(new HasEnchants());
        registerNewRule(new HasLore());
        registerNewRule(new HasName());
        registerNewRule(new ItemFormat());
        registerNewRule(new Items());
        registerNewRule(new Not());
        registerNewRule(new Material());
        registerNewRule(new None());
        if (CommonUtil.checkPluginLoad("NBTAPI")) {
            registerNewRule(new ContainsNBT());
            registerNewRule(new NBTString());
            registerNewRule(new NBTByte());
            registerNewRule(new NBTInt());
            registerNewRule(new NBTDouble());
        }
        if (CommonUtil.getMinorVersion(20, 5)) {
            registerNewRule(new Rarity());
        }
        registerNewRule(new ContainsEnchants());
        registerNewRule(new ContainsEnchantsAmount());
        registerNewRule(new Any());
        registerNewRule(new MaterialTag());
        registerNewRule(new Enchantable());
        registerNewRule(new HasStoredEnchants());
        registerNewRule(new PotionTypeRule());
    }

    public void registerNewRule(AbstractMatchItemRule rule) {
        rules.add(rule);
    }

    public boolean getMatch(ConfigurationSection section, ItemStack item) {
        return getMatch(section, null, item, null);
    }

    public boolean getMatch(ConfigurationSection section, Player player, ItemStack item) {
        return getMatch(section, player, item, null);
    }

    public boolean getMatch(ConfigurationSection section, Player player, ItemStack item, PowerContext context) {
        if (section == null) {
            return true;
        }
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return getMatch(new ObjectSingleMatchItem(section, item, meta, player, context));
    }

    public boolean getMatch(ObjectSingleMatchItem match) {
        for (AbstractMatchItemRule rule : rules) {
            if (rule.configNotContains(match.section)) {
                continue;
            }
            if (ConfigManager.configManager.getBoolean("debug")) {
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fChecking match item rule: " + rule.getClass().getSimpleName());
            }
            if (!rule.getMatch(match)) {
                return false;
            }
        }
        return true;
    }

    public Collection<AbstractMatchItemRule> getRules() {
        return rules;
    }
}
