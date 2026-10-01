package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public final class SwapLocationsAbility extends AbstractAbility {

    public SwapLocationsAbility(ConfigurationSection section) {
        super("SwapLocations", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity source = getSourceEntity(context);
        Entity target = getTargetEntity(context);
        if (source == null || target == null || source.equals(target) || !source.getWorld().equals(target.getWorld())) {
            return false;
        }
        Location sourceLocation = source.getLocation();
        Location targetLocation = target.getLocation();
        SchedulerUtil.teleport(source, targetLocation);
        SchedulerUtil.teleport(target, sourceLocation);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
