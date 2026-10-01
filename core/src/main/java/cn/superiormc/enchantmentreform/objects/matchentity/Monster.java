package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class Monster extends AbstractMatchEntityRule {

    public Monster() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        boolean result = entity instanceof org.bukkit.entity.Monster;
        if (section.getBoolean("monster")) {
            return result;
        }
        return !result;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("monster");
    }
}
