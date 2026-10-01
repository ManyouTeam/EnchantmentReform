package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class EntityHealth extends AbstractMatchEntityRule {

    public EntityHealth() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        return entity.getHealth() >= section.getDouble("entity-health");
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("entity-health");
    }
}
