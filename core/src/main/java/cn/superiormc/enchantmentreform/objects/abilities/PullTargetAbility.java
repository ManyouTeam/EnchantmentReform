package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class PullTargetAbility extends AbstractAbility {

    public PullTargetAbility(ConfigurationSection section) {
        super("PullTarget", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity attacker = getSourceEntity(context);
        if (attacker == null) {
            return false;
        }
        Entity target = getTargetEntity(context);
        if (target == null) {
            return false;
        }
        if (getBoolean("remove-powder-snow", false)) {
            removePowderSnow(target);
        }

        Vector direction = getDirection(attacker, target);
        if (direction.lengthSquared() < 0.0001) {
            return false;
        }

        double speed = getDouble("speed", getDouble("strength", 1.0, context), context);
        double vertical = getDouble("vertical", Double.NaN, context);
        Vector velocity = direction.normalize().multiply(speed);
        if (Double.isNaN(vertical)) {
            velocity.setY(Math.max(0.2, velocity.getY() + 0.2));
        } else {
            velocity.setY(vertical);
        }
        target.setVelocity(velocity);
        return false;
    }

    private Vector getDirection(Entity source, Entity target) {
        Vector away = target.getLocation().toVector().subtract(source.getLocation().toVector());
        if (away.lengthSquared() < 1.0E-4D) {
            away = source.getLocation().getDirection();
        }
        away.normalize();

        String direction = getString("direction", "TOWARD").toUpperCase(Locale.ROOT);
        if (direction.equals("AWAY") || direction.equals("AWAY_FROM_SOURCE")) {
            return away;
        }
        if (direction.equals("RANDOM") && ThreadLocalRandom.current().nextBoolean()) {
            return away;
        }
        return away.multiply(-1.0D);
    }

    private void removePowderSnow(Entity target) {
        Block block = target.getLocation().getBlock();
        if (block.getType() == Material.POWDER_SNOW) {
            block.setType(Material.AIR, false);
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
