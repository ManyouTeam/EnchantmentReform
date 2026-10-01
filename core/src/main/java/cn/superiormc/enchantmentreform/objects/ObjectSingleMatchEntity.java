package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ObjectSingleMatchEntity {

    public final ConfigurationSection section;

    private final LivingEntity entity;

    private final Player player;

    private final PowerContext context;

    public ObjectSingleMatchEntity(ConfigurationSection section, LivingEntity entity,
                                   Player player, PowerContext context) {
        this.section = section;
        this.entity = entity;
        this.player = player;
        this.context = context;
    }

    public ObjectSingleMatchEntity withSection(ConfigurationSection section) {
        return new ObjectSingleMatchEntity(section, entity, player, context);
    }

    public String parsePlaceholder(String value) {
        if (value == null) {
            return null;
        }
        int level = context == null ? 1 : context.level();
        return CommonUtil.modifyString(player, value, "level", String.valueOf(level));
    }

    public List<String> parsePlaceholder(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(parsePlaceholder(value));
        }
        return result;
    }

    public ConfigurationSection getSection() {
        return section;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }

    public PowerContext getContext() {
        return context;
    }
}
