package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.managers.MatchEntityManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;
import org.bukkit.configuration.ConfigurationSection;

public class Not extends AbstractMatchEntityRule {

    public Not() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ObjectSingleMatchEntity notMatch = match.withSection(match.section.getConfigurationSection("not"));
        for (AbstractMatchEntityRule rule : MatchEntityManager.matchEntityManager.getRules()) {
            if (rule.configNotContains(notMatch.section)) {
                continue;
            }
            if (rule.getMatch(notMatch)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getConfigurationSection("not") == null;
    }
}
