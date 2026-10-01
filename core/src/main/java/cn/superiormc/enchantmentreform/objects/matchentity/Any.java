package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.managers.MatchEntityManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class Any extends AbstractMatchEntityRule {

    public Any() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection anySection = match.section.getConfigurationSection("any");
        if (anySection == null) {
            return true;
        }
        Set<String> anyKeys = anySection.getKeys(false);
        if (anyKeys.isEmpty()) {
            return true;
        }
        if (anyKeys.contains("1")) {
            big: for (String anyKey : anyKeys) {
                ObjectSingleMatchEntity checkMatch = match.withSection(anySection.getConfigurationSection(anyKey));
                for (AbstractMatchEntityRule rule : MatchEntityManager.matchEntityManager.getRules()) {
                    if (rule.configNotContains(checkMatch.section)) {
                        continue;
                    }
                    if (!rule.getMatch(checkMatch)) {
                        continue big;
                    }
                }
                return true;
            }
        } else {
            ObjectSingleMatchEntity anyMatch = match.withSection(anySection);
            for (AbstractMatchEntityRule rule : MatchEntityManager.matchEntityManager.getRules()) {
                if (rule.configNotContains(anySection)) {
                    continue;
                }
                if (rule.getMatch(anyMatch)) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("any");
    }
}
