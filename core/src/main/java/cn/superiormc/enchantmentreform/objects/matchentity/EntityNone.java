package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class EntityNone extends AbstractMatchEntityRule {

    public EntityNone() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        return !section.getBoolean("none");
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("none");
    }
}
