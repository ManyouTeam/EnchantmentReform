package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;
import org.bukkit.configuration.ConfigurationSection;

public abstract class AbstractMatchEntityRule {

    public AbstractMatchEntityRule() {
        // Empty...
    }

    public abstract boolean getMatch(ObjectSingleMatchEntity match);

    public abstract boolean configNotContains(ConfigurationSection section);

    @Override
    public String toString() {
        return getClass().getName();
    }
}
