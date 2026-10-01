package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class MythicMobs extends AbstractMatchEntityRule {

    private final BukkitAPIHelper mythicHelper = new BukkitAPIHelper();

    public MythicMobs() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        for (String mobID : section.getStringList("mythicmobs")) {
            ActiveMob activeMob = mythicHelper.getMythicMobInstance(entity);
            if (activeMob == null) {
                continue;
            }
            MythicMob mythicMob = activeMob.getType();
            if (mythicMob == null) {
                continue;
            }
            if (mythicMob.getInternalName().equalsIgnoreCase(mobID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("mythicmobs").isEmpty();
    }
}
