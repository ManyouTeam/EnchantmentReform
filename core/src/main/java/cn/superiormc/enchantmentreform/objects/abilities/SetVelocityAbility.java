package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class SetVelocityAbility extends AbstractAbility {

    public SetVelocityAbility(ConfigurationSection section) {
        super("SetVelocity", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (target == null) {
            return false;
        }
        String operation = section.getString("operation", "SET").toUpperCase(Locale.ROOT);
        Vector velocity = switch (operation) {
            case "SCALE" -> {
                double multiplier = getDouble(
                        "multiplier",
                        1.0D,
                        context
                );

                yield target.getVelocity().multiply(multiplier);
            }

            case "ADD" -> {
                Vector configured = createVector(context, target);
                yield target.getVelocity().add(configured);
            }

            case "MULTIPLY" -> {
                Vector configured = createVector(context, target);
                yield target.getVelocity().multiply(configured);
            }

            default -> {
                Vector configured = createVector(context, target);
                yield configured;
            }
        };
        target.setVelocity(clamp(velocity, context));
        if (section.getBoolean("reset-fall-distance", false)) {
            target.setFallDistance(0.0F);
        }
        return false;
    }

    private Vector createVector(PowerContext context, Entity target) {
        String direction = section.getString("direction", "VECTOR").toUpperCase(Locale.ROOT);
        double strength = getDouble("strength", 1.0D, context);
        Vector vector = switch (direction) {
            case "LOOK" -> target.getLocation().getDirection().normalize().multiply(strength);
            case "LOOK_HORIZONTAL" -> horizontalLook(target).multiply(strength);
            case "RANDOM_HORIZONTAL_SIDE", "RANDOM_SIDE" -> horizontalSide(target)
                    .multiply(ThreadLocalRandom.current().nextBoolean()
                            ? strength : -strength);
            case "SOURCE_TO_TARGET" -> direction(context.source(), target).multiply(strength);
            case "TARGET_TO_SOURCE" -> direction(target, context.source()).multiply(strength);
            case "UP" -> new Vector(0.0D, strength, 0.0D);
            default -> new Vector(
                    getDouble("x", 0.0D, context),
                    getDouble("y", 0.0D, context),
                    getDouble("z", 0.0D, context));
        };
        double vertical = getDouble("vertical", 0.0D, context);
        if (section.getBoolean("preserve-vertical", false)) {
            double minimumVertical = getDouble("minimum-vertical", -Double.MAX_VALUE, context);
            vector.setY(Math.max(target.getVelocity().getY(), minimumVertical) + vertical);
        } else {
            vector.setY(vector.getY() + vertical);
        }
        return vector;
    }

    private Vector clamp(Vector vector, PowerContext context) {
        if (section.contains("minimum-x")) {
            vector.setX(Math.max(vector.getX(), getDouble("minimum-x", vector.getX(), context)));
        }
        if (section.contains("minimum-y")) {
            vector.setY(Math.max(vector.getY(), getDouble("minimum-y", vector.getY(), context)));
        }
        if (section.contains("minimum-z")) {
            vector.setZ(Math.max(vector.getZ(), getDouble("minimum-z", vector.getZ(), context)));
        }
        if (section.contains("maximum-x")) {
            vector.setX(Math.min(vector.getX(), getDouble("maximum-x", vector.getX(), context)));
        }
        if (section.contains("maximum-y")) {
            vector.setY(Math.min(vector.getY(), getDouble("maximum-y", vector.getY(), context)));
        }
        if (section.contains("maximum-z")) {
            vector.setZ(Math.min(vector.getZ(), getDouble("maximum-z", vector.getZ(), context)));
        }
        return vector;
    }

    private Vector horizontalLook(Entity entity) {
        Vector direction = entity.getLocation().getDirection().setY(0.0D);
        return direction.lengthSquared() <= 1.0E-4D ? new Vector() : direction.normalize();
    }

    private Vector horizontalSide(Entity entity) {
        Vector look = horizontalLook(entity);
        return new Vector(-look.getZ(), 0.0D, look.getX());
    }

    private Vector direction(Entity from, Entity to) {
        if (from == null || to == null || !from.getWorld().equals(to.getWorld())) {
            return new Vector();
        }
        Vector vector = to.getLocation().toVector().subtract(from.getLocation().toVector());
        if (vector.lengthSquared() == 0.0D) {
            return new Vector();
        }
        return vector.normalize();
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
