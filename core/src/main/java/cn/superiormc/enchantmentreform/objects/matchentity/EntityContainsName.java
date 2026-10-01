package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class EntityContainsName extends AbstractMatchEntityRule {

    public EntityContainsName() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        for (String mobID : section.getStringList("entity-contains-name")) {
            if (TextUtil.clear(EnchantmentReform.methodUtil.getEntityName(entity)).contains(TextUtil.clear(mobID))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("entity-contains-name");
    }
}
