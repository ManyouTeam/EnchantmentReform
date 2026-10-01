package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class Any extends AbstractMatchItemRule {

    public Any() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchItem match) {
        ConfigurationSection anySection = match.section.getConfigurationSection("any");
        Set<String> anyKeys = anySection.getKeys(false);
        if (anyKeys.isEmpty()) {
            return true;
        }
        if (anyKeys.contains("1")) {
            big: for (String anyKey : anyKeys) {
                ObjectSingleMatchItem checkMatch = match.withSection(anySection.getConfigurationSection(anyKey));
                for (AbstractMatchItemRule rule : MatchItemManager.matchItemManager.getRules()) {
                    if (rule.configNotContains(checkMatch.section)) {
                        continue;
                    }
                    if (!rule.getMatch(checkMatch)) {
                        continue big;
                    }
                }
                return true;
            }
            return false;
        } else {
            ObjectSingleMatchItem anyMatch = match.withSection(anySection);
            for (AbstractMatchItemRule rule : MatchItemManager.matchItemManager.getRules()) {
                if (rule.configNotContains(anySection)) {
                    continue;
                }
                if (rule.getMatch(anyMatch)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getConfigurationSection("any") == null;
    }
}
