package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionInSunlight extends AbstractPowerCondition {

    public PowerConditionInSunlight() {
        super("in_sunlight");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        if (entity == null) {
            return false;
        }
        Location location = entity.getLocation();
        boolean sunlight = location.getWorld() != null && location.getWorld().getTime() < 12300
                && location.getBlock().getLightFromSky() >= 15;
        return sunlight == condition.getSection().getBoolean("value", true);
    }
}
