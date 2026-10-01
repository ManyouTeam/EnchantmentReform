package cn.superiormc.enchantmentreform.objects.matchitem;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchItem;
import org.bukkit.configuration.ConfigurationSection;

public abstract class AbstractMatchItemRule {

    public AbstractMatchItemRule() {
        // Empty...
    }

    public abstract boolean getMatch(ObjectSingleMatchItem match);

    public abstract boolean configNotContains(ConfigurationSection section);

    @Override
    public String toString() {
        return getClass().getName();
    }
}
