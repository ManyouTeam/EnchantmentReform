package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.PowerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class MarkAbility extends AbstractAbility {

    public MarkAbility(ConfigurationSection section) {
        super("Mark", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (entity != null) {
            PowerManager.powerManager.markUsedPower(entity);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SKILL;
    }
}
