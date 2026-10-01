package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class DelayAbility extends AbstractAbility {

    public DelayAbility(ConfigurationSection section) {
        super("Delay", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        int delay = getInt("ticks", section.getInt("delay", 1), context);
        if (delay <= 0) {
            delay = 1;
        }
        Entity source = context.source();
        if (source != null) {
            SchedulerUtil.runTaskLater(source, () ->
                    AbilityManager.abilityManager.execute(section.getConfigurationSection("abilities"), context), delay);
        } else {
            SchedulerUtil.runTaskLater(() ->
                    AbilityManager.abilityManager.execute(section.getConfigurationSection("abilities"), context), delay);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }
}
