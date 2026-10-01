package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public final class PullLocationAbility extends AbstractAbility {

    public PullLocationAbility(ConfigurationSection section) {
        super("PullLocation", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        Location destination = getLocation(context);
        if (target == null || destination == null) {
            return false;
        }
        double speed = getDouble("speed", getDouble("strength", 1.0D, context), context);
        double vertical = getDouble("vertical", Double.NaN, context);
        if (destination.getWorld() == null || !destination.getWorld().equals(target.getWorld()) || speed <= 0.0D) {
            return false;
        }
        Vector direction = destination.toVector().subtract(target.getLocation().toVector());
        if (direction.lengthSquared() < 1.0E-4D) {
            return false;
        }
        Vector velocity = direction.normalize().multiply(speed);
        if (!Double.isNaN(vertical)) {
            velocity.setY(vertical);
        }
        if (section.getBoolean("preserve-momentum", false)) {
            Vector current = target.getVelocity();
            velocity.setX(velocity.getX() + current.getX());
            velocity.setZ(velocity.getZ() + current.getZ());
        }
        target.setVelocity(velocity);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
