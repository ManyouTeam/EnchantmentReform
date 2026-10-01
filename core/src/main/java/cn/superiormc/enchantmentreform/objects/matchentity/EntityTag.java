package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;

public class EntityTag extends AbstractMatchEntityRule {

    public EntityTag() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        for (String singleEntity : section.getStringList("entity-tag")) {
            if (singleEntity.equals("monster")) {
                return entity instanceof Monster;
            }
            Tag<EntityType> tempVal1 = Bukkit.getTag(Tag.REGISTRY_ENTITY_TYPES, CommonUtil.parseNamespacedKey(singleEntity), EntityType.class);
            if (tempVal1 != null && tempVal1.isTagged(entity.getType())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return section.getStringList("entity-tag").isEmpty();
    }
}
