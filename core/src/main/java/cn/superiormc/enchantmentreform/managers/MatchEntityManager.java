package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.matchentity.*;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

public class MatchEntityManager extends AbstractManager {

    public static MatchEntityManager matchEntityManager;

    private final Collection<AbstractMatchEntityRule> rules = new HashSet<>();

    public MatchEntityManager() {
        matchEntityManager = this;
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        registerNewRule(new EntityType());
        registerNewRule(new None());
        registerNewRule(new EntityContainsName());
        registerNewRule(new EntityNone());
        registerNewRule(new EntityHealth());
        registerNewRule(new EntityTag());
        registerNewRule(new EntityPDC());
        registerNewRule(new Any());
        registerNewRule(new Not());
        registerNewRule(new Equip());
        registerNewRule(new Ranged());
        registerNewRule(new Monster());
        registerNewRule(new Animal());
        if (CommonUtil.checkPluginLoad("MythicMobs")) {
            registerNewRule(new MythicMobs());
        }
        if (CommonUtil.checkPluginLoad("LevelledMobs")) {
            registerNewRule(new LevelledMobs());
        }
    }

    public void registerNewRule(AbstractMatchEntityRule rule) {
        rules.add(rule);
    }

    public boolean getMatch(ConfigurationSection section, LivingEntity entity) {
        return getMatch(section, entity, null, null);
    }

    public boolean getMatch(ConfigurationSection section, LivingEntity entity, Player player, PowerContext context) {
        if (section == null) {
            return true;
        }
        if (entity == null) {
            return false;
        }
        return getMatch(new ObjectSingleMatchEntity(section, entity, player, context));
    }

    public boolean getMatch(ObjectSingleMatchEntity match) {
        for (AbstractMatchEntityRule rule : rules) {
            if (ConfigManager.configManager.getBoolean("debug")) {
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fChecking rule: " + rule.getClass().getSimpleName() + "!");
            }
            if (rule.configNotContains(match.section)) {
                continue;
            }
            if (!rule.getMatch(match)) {
                return false;
            }
        }
        return true;
    }

    public Collection<AbstractMatchEntityRule> getRules() {
        return rules;
    }
}
