package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public final class ReflectProjectileAbility extends AbstractAbility {

    private static final String REFLECTED_KEY = "reflected_projectile";

    public ReflectProjectileAbility(ConfigurationSection section) {
        super("ReflectProjectile", section);
    }

    @Override
    public boolean shouldExecute(PowerContext context) {
        Projectile projectile = getProjectile(context);
        return projectile != null && !isReflected(projectile) && super.shouldExecute(context);
    }

    @Override
    public boolean execute(PowerContext context) {
        Projectile projectile = getProjectile(context);
        Player defender = context.player();
        if (projectile == null || defender == null || isReflected(projectile)) {
            return false;
        }

        PowerStateStore.Key cooldownKey = PowerStateStore.key(
                defender,
                context.power() == null ? "none" : context.power().getId(),
                "reflect-projectile",
                section.getCurrentPath(),
                null);
        double cooldown = Math.max(0.0D,
                getDouble("reflection-cooldown", 0.0D, context));
        if (!PowerStateStore.tryAcquireCooldown(cooldownKey, cooldown)) {
            return false;
        }

        Vector velocity = reflectedVelocity(projectile, defender, context);
        Location destination = reflectionOrigin(defender, velocity, context);
        double damageMultiplier = Math.max(0.0D,
                getDouble("damage-multiplier", 1.0D, context));
        ProjectileSource shooter = resolveShooter(context, defender);

        if (projectile instanceof AbstractArrow arrow) {
            return replaceReflectedArrow(
                    arrow, destination, shooter, velocity, damageMultiplier, context, cooldownKey);
        }

        if (EnchantmentReform.isFolia) {
            markReflected(projectile);
            projectile.teleportAsync(destination).whenComplete((success, failure) -> {
                if (failure != null || !Boolean.TRUE.equals(success)) {
                    SchedulerUtil.runSync(defender, () -> {
                        unmarkReflected(projectile);
                        PowerStateStore.remove(cooldownKey);
                    });
                    return;
                }
                SchedulerUtil.runSync(projectile, () -> finishReflection(
                        projectile, shooter, velocity, damageMultiplier, context));
            });
            return true;
        }

        if (!projectile.teleport(destination)) {
            PowerStateStore.remove(cooldownKey);
            return false;
        }
        markReflected(projectile);
        finishReflection(projectile, shooter, velocity, damageMultiplier, context);
        return true;
    }

    private Projectile getProjectile(PowerContext context) {
        Entity target = getTargetEntity(context);
        return target instanceof Projectile projectile ? projectile : null;
    }

    private ProjectileSource resolveShooter(PowerContext context, Player fallback) {
        EntitySelector selector = EntitySelector.parse(
                section.getString("shooter"), EntitySelector.PLAYER);
        Entity selected = context.entity(selector);
        return selected instanceof ProjectileSource source ? source : fallback;
    }

    private Vector reflectedVelocity(Projectile projectile, Player defender, PowerContext context) {
        double multiplier = Math.max(0.01D,
                getDouble("velocity-multiplier", 1.0D, context));
        Vector reflected = projectile.getVelocity().clone().multiply(-multiplier);
        double minimumSquared = Math.max(0.0D,
                getDouble("minimum-velocity-squared", 0.08D, context));
        if (reflected.lengthSquared() >= minimumSquared) {
            return reflected;
        }
        double fallbackSpeed = Math.max(0.01D,
                getDouble("fallback-speed", 0.95D, context));
        Vector towardAttacker = directionToward(context.source(), defender);
        return towardAttacker.lengthSquared() <= 1.0E-6D
                ? new Vector(0.0D, 0.0D, fallbackSpeed)
                : towardAttacker.normalize().multiply(fallbackSpeed);
    }

    private Vector directionToward(Entity attacker, LivingEntity defender) {
        if (attacker == null || !attacker.getWorld().equals(defender.getWorld())) {
            return defender.getEyeLocation().getDirection();
        }
        Location target = attacker instanceof LivingEntity living
                ? living.getEyeLocation() : attacker.getLocation();
        return target.toVector().subtract(defender.getEyeLocation().toVector());
    }

    private Location reflectionOrigin(Player defender, Vector velocity, PowerContext context) {
        double distance = Math.max(0.0D, getDouble("origin-distance", 0.55D, context));
        Vector direction = velocity.clone();
        if (direction.lengthSquared() <= 1.0E-6D) {
            direction = defender.getEyeLocation().getDirection();
        }
        if (direction.lengthSquared() > 1.0E-6D) {
            direction.normalize().multiply(distance);
        }
        return defender.getEyeLocation().add(direction);
    }

    private void finishReflection(Projectile projectile,
                                  ProjectileSource shooter,
                                  Vector velocity,
                                  double damageMultiplier,
                                  PowerContext context) {
        if (!projectile.isValid()) {
            return;
        }
        TriggerManager.triggerManager.runtime().stopTrackingProjectile(projectile);
        projectile.setShooter(shooter);
        projectile.setVelocity(velocity);
        AbilityDamageUtil.markDamageMultiplier(projectile, damageMultiplier);
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), context);
    }

    private boolean replaceReflectedArrow(AbstractArrow source,
                                          Location destination,
                                          ProjectileSource shooter,
                                          Vector velocity,
                                          double damageMultiplier,
                                          PowerContext context,
                                          PowerStateStore.Key cooldownKey) {
        World world = destination.getWorld();
        if (world == null || !world.isChunkLoaded(
                destination.getBlockX() >> 4, destination.getBlockZ() >> 4)) {
            PowerStateStore.remove(cooldownKey);
            return false;
        }
        Entity spawned = world.spawnEntity(destination, source.getType());
        if (!(spawned instanceof AbstractArrow replacement)) {
            spawned.remove();
            PowerStateStore.remove(cooldownKey);
            return false;
        }
        copyArrow(source, replacement);
        replacement.setShooter(shooter);
        markReflected(replacement);
        AbilityDamageUtil.markDamageMultiplier(replacement, damageMultiplier);
        replacement.setVelocity(velocity);
        TriggerManager.triggerManager.runtime().stopTrackingProjectile(source);
        if (context.result() != null) {
            context.result().skillEntity(replacement);
        }
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), context);
        source.remove();
        return true;
    }

    private void copyArrow(AbstractArrow source, AbstractArrow target) {
        target.setGravity(source.hasGravity());
        target.setFireTicks(source.getFireTicks());
        target.setCritical(source.isCritical());
        target.setPierceLevel(source.getPierceLevel());
        target.setKnockbackStrength(source.getKnockbackStrength());
        target.setPickupStatus(source.getPickupStatus());
        target.setDamage(source.getDamage());
        target.setSilent(source.isSilent());
        target.setGlowing(source.isGlowing());
        target.setPersistent(source.isPersistent());
        source.getPersistentDataContainer().copyTo(target.getPersistentDataContainer(), true);
        for (String tag : source.getScoreboardTags()) {
            target.addScoreboardTag(tag);
        }
        if (source instanceof Arrow sourceArrow && target instanceof Arrow targetArrow) {
            targetArrow.setBasePotionType(sourceArrow.getBasePotionType());
            targetArrow.setColor(sourceArrow.getColor());
            for (PotionEffect effect : sourceArrow.getCustomEffects()) {
                targetArrow.addCustomEffect(effect, true);
            }
        } else if (source instanceof SpectralArrow sourceSpectral
                && target instanceof SpectralArrow targetSpectral) {
            targetSpectral.setGlowingTicks(sourceSpectral.getGlowingTicks());
        }
    }

    private boolean isReflected(Projectile projectile) {
        return projectile.getPersistentDataContainer().has(
                reflectedKey(), PersistentDataType.BYTE);
    }

    private void markReflected(Projectile projectile) {
        projectile.getPersistentDataContainer().set(
                reflectedKey(), PersistentDataType.BYTE, (byte) 1);
    }

    private void unmarkReflected(Projectile projectile) {
        PersistentDataContainer data = projectile.getPersistentDataContainer();
        data.remove(reflectedKey());
    }

    private org.bukkit.NamespacedKey reflectedKey() {
        return new org.bukkit.NamespacedKey(EnchantmentReform.instance, REFLECTED_KEY);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SKILL;
    }
}
