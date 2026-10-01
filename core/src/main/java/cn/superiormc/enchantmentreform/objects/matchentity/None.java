package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class None extends AbstractMatchEntityRule {

    public None() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        return false;
    }
    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("none");
    }
}
