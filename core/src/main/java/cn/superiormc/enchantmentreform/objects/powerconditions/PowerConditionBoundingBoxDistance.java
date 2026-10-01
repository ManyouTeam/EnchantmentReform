package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public final class PowerConditionBoundingBoxDistance extends AbstractNumericPowerCondition {

    public PowerConditionBoundingBoxDistance() {
        super("bounding_box_distance");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Entity source = context.entity(EntitySelector.parse(
                condition.getSection().getString("source"), EntitySelector.SOURCE));
        Entity target = context.entity(EntitySelector.parse(
                condition.getSection().getString("target"), EntitySelector.TARGET));
        if (source == null || target == null || !source.getWorld().equals(target.getWorld())) {
            return null;
        }

        boolean sourceEye = condition.getSection().getBoolean("source-eye", false);
        Location sourceLocation = sourceEye && source instanceof LivingEntity living
                ? living.getEyeLocation()
                : source.getLocation();
        BoundingBox bounds = target.getBoundingBox();
        double dx = axisDistance(sourceLocation.getX(), bounds.getMinX(), bounds.getMaxX());
        double dy = axisDistance(sourceLocation.getY(), bounds.getMinY(), bounds.getMaxY());
        double dz = axisDistance(sourceLocation.getZ(), bounds.getMinZ(), bounds.getMaxZ());
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double axisDistance(double point, double minimum, double maximum) {
        if (point < minimum) {
            return minimum - point;
        }
        if (point > maximum) {
            return point - maximum;
        }
        return 0.0D;
    }
}
