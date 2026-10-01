package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class HomingProjectileAbility extends AbstractAbility {

    private static final NamespacedKey HOMING_TICKS = new NamespacedKey(EnchantmentReform.instance, "homing_ticks");
    private static final NamespacedKey HOMING_TARGET = new NamespacedKey(EnchantmentReform.instance, "homing_target");

    public HomingProjectileAbility(ConfigurationSection section) {
        super("HomingProjectile", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Projectile projectile = context.skill() instanceof Projectile current ? current : null;
        if (projectile == null || !projectile.isValid()) {
            return false;
        }

        if (isExpired(projectile, context)) {
            if (getBoolean("remove-on-expire", true)) {
                projectile.remove();
            }
            return true;
        }

        // Cancel gravity/drag drift so long-range homing keeps its aim; opt-in to preserve vanilla arcs.
        if (getBoolean("disable-gravity", false) && projectile.hasGravity()) {
            projectile.setGravity(false);
        }

        double radius = getDouble("radius", 16, context);
        double strength = Math.min(1.0, Math.max(0.01, getDouble("strength", 0.2, context)));
        boolean requireLineOfSight = getBoolean("require-line-of-sight", false);
        // Cone half-angle (degrees) around the projectile's heading; targets outside it are ignored.
        double maxAngle = getDouble("max-angle", 360.0, context);
        double minDot = maxAngle >= 360.0 ? -1.0 : Math.cos(Math.toRadians(Math.min(180.0, maxAngle)));
        double lead = getDouble("lead", 0.0, context);

        Entity shooter = context.source();
        Location projectileLocation = projectile.getLocation();
        Vector velocity = projectile.getVelocity();
        Vector heading = velocity.lengthSquared() < 1.0E-6 ? null : velocity.clone().normalize();

        // Keep last tick's target locked while it stays valid and in range, so we only rescan on loss.
        LivingEntity target = resolveLockedTarget(projectile, shooter, projectileLocation, radius,
                heading, minDot, requireLineOfSight);
        if (target == null && shooter instanceof Mob mob
                && isValidTarget(mob.getTarget(), shooter, projectileLocation, radius,
                heading, minDot, requireLineOfSight)) {
            target = mob.getTarget();
        }
        if (target == null) {
            target = findBestTarget(projectileLocation, shooter, radius, heading, minDot, requireLineOfSight);
        }
        if (target == null) {
            projectile.getPersistentDataContainer().remove(HOMING_TARGET);
            return false;
        }
        rememberTarget(projectile, target);

        // Aim at a predicted intercept point when lead > 0, otherwise the target's current eye position.
        Vector aim = target.getEyeLocation().toVector();
        if (lead > 0.0) {
            double speedNow = Math.max(0.4, velocity.length());
            double distance = projectileLocation.toVector().distance(aim);
            aim.add(target.getVelocity().multiply(lead * distance / speedNow));
        }

        Vector desired = aim.subtract(projectileLocation.toVector());
        if (desired.lengthSquared() < 1.0E-4) {
            return false;
        }

        // Steer direction only, then restore the original speed so turning never bleeds off velocity.
        double speed = Math.max(0.4, velocity.length());
        Vector desiredDir = desired.normalize();
        Vector newDir = heading == null
                ? desiredDir
                : heading.multiply(1 - strength).add(desiredDir.multiply(strength));
        if (newDir.lengthSquared() < 1.0E-6) {
            newDir = desiredDir;
        }
        projectile.setVelocity(newDir.normalize().multiply(speed));
        return false;
    }

    private boolean isExpired(Projectile projectile, PowerContext context) {
        int maxTicks = getInt("max-ticks", 100, context);
        if (maxTicks <= 0) {
            return false;
        }

        PersistentDataContainer pdc = projectile.getPersistentDataContainer();
        int ticks = pdc.getOrDefault(HOMING_TICKS, PersistentDataType.INTEGER, 0) + 1;
        pdc.set(HOMING_TICKS, PersistentDataType.INTEGER, ticks);
        return ticks > maxTicks;
    }

    private LivingEntity findBestTarget(Location center, Entity shooter, double radius,
                                        Vector heading, double minDot, boolean requireLineOfSight) {
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        double radiusSquared = radius * radius;
        for (Entity candidate : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(candidate instanceof LivingEntity living)
                    || !isValidTarget(living, shooter, center, radius, heading, minDot, requireLineOfSight)) {
                continue;
            }
            double distanceSquared = living.getLocation().distanceSquared(center);
            if (distanceSquared > radiusSquared) {
                continue;
            }
            // Prefer targets that are both close and near the heading; falls back to pure distance when no heading.
            double score = distanceSquared;
            if (heading != null) {
                double dot = directionTo(center, living, heading);
                score = distanceSquared / (dot + 1.05);
            }
            if (score < bestScore) {
                bestScore = score;
                best = living;
            }
        }
        return best;
    }

    private LivingEntity resolveLockedTarget(Projectile projectile, Entity shooter, Location center,
                                             double radius, Vector heading, double minDot,
                                             boolean requireLineOfSight) {
        String raw = projectile.getPersistentDataContainer()
                .getOrDefault(HOMING_TARGET, PersistentDataType.STRING, "");
        if (raw.isEmpty()) {
            return null;
        }
        Entity locked;
        try {
            locked = projectile.getWorld().getEntity(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            return null;
        }
        return isValidTarget(locked, shooter, center, radius, heading, minDot, requireLineOfSight)
                ? (LivingEntity) locked : null;
    }

    private void rememberTarget(Projectile projectile, LivingEntity target) {
        projectile.getPersistentDataContainer()
                .set(HOMING_TARGET, PersistentDataType.STRING, target.getUniqueId().toString());
    }

    private boolean isValidTarget(Entity candidate, Entity shooter, Location center, double radius,
                                  Vector heading, double minDot, boolean requireLineOfSight) {
        if (!(candidate instanceof LivingEntity living) || living.isDead() || candidate == shooter) {
            return false;
        }
        if (candidate instanceof Player player && !canTarget(player)) {
            return false;
        }
        if (!candidate.getWorld().equals(center.getWorld())
                || living.getLocation().distanceSquared(center) > radius * radius) {
            return false;
        }
        if (heading != null && minDot > -1.0 && directionTo(center, living, heading) < minDot) {
            return false;
        }
        return !requireLineOfSight || living.hasLineOfSight(center) || living.hasLineOfSight(shooter);
    }

    private double directionTo(Location center, LivingEntity target, Vector heading) {
        Vector toTarget = target.getEyeLocation().toVector().subtract(center.toVector());
        return toTarget.lengthSquared() < 1.0E-6 ? 1.0 : toTarget.normalize().dot(heading);
    }

    private boolean canTarget(Player player) {
        if (!player.isOnline() || player.isDead()) {
            return false;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }
        return player.getGameMode() != GameMode.SPECTATOR;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SKILL;
    }
}
