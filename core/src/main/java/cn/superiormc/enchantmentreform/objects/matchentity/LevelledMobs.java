package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class LevelledMobs extends AbstractMatchEntityRule {

    public LevelledMobs() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        String rankSetting = section.getString("levelled-mobs");
        int rank = getMobLevel(entity);
        if (rankSetting == null) {
            return true;
        }
        if (rankSetting.contains("~")) {
            String[] parts = rankSetting.split("~");
            int min = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            return rank >= min && rank <= max;
        } else if (rankSetting.startsWith("<=")) {
            int max = Integer.parseInt(rankSetting.substring(2));
            return rank <= max;
        } else if (rankSetting.startsWith(">=")) {
            int min = Integer.parseInt(rankSetting.substring(2));
            return rank >= min;
        } else {
            return rank == Integer.parseInt(rankSetting);
        }
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("levelled-mobs").isEmpty();
    }

    private int getMobLevel(LivingEntity livingEntity){
        Plugin levelledMobsPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        if (levelledMobsPlugin == null) return 0;
        NamespacedKey levelKey = new NamespacedKey(levelledMobsPlugin, "level");
        return Objects.requireNonNullElse(
                livingEntity.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER),
                0
        );
    }
}
