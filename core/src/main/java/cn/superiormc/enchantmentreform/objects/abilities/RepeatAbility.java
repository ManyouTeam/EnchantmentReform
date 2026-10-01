package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.atomic.AtomicInteger;

public class RepeatAbility extends AbstractAbility {

    public RepeatAbility(ConfigurationSection section) {
        super("Repeat", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection abilities = section.getConfigurationSection("abilities");
        if (abilities == null) {
            return false;
        }

        int durationTicks = Math.max(0, getInt("duration", getInt("duration-ticks", 0, context), context));
        int intervalTicks = Math.max(1, getInt("interval", getInt("period", 20, context), context));

        AbilityManager.abilityManager.execute(abilities, context);
        if (durationTicks <= 0) {
            return false;
        }

        AtomicInteger elapsedTicks = new AtomicInteger(intervalTicks);
        SchedulerUtil[] task = new SchedulerUtil[1];
        Runnable runnable = () -> {
            if (elapsedTicks.getAndAdd(intervalTicks) > durationTicks) {
                if (task[0] != null) {
                    task[0].cancel();
                }
                return;
            }
            AbilityManager.abilityManager.execute(abilities, context);
        };

        Entity source = context.source();
        Location location = context.location();
        if (source != null && source.isValid() && (!(source instanceof LivingEntity living) || !living.isDead())) {
            task[0] = SchedulerUtil.runTaskTimer(source, runnable, intervalTicks, intervalTicks);
        } else if (location != null) {
            task[0] = SchedulerUtil.runTaskTimer(location, runnable, intervalTicks, intervalTicks);
        } else {
            task[0] = SchedulerUtil.runTaskTimer(runnable, intervalTicks, intervalTicks);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }
}
