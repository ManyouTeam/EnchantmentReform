package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public final class RicochetProjectileAbility extends AbstractAbility {

    private static final NamespacedKey RICOCHET_COUNT = new NamespacedKey(
            EnchantmentReform.instance, "ricochet_count");

    public RicochetProjectileAbility(ConfigurationSection section) {
        super("RicochetProjectile", section);
    }

    @Override
    public boolean shouldExecute(PowerContext context) {
        return context.event() instanceof ProjectileHitEvent event
                && event.getHitBlock() != null
                && context.skill() instanceof AbstractArrow
                && super.shouldExecute(context);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.event() instanceof ProjectileHitEvent event)
                || event.getHitBlock() == null
                || !(context.skill() instanceof AbstractArrow source)) {
            return false;
        }

        int count = source.getPersistentDataContainer().getOrDefault(
                RICOCHET_COUNT, PersistentDataType.INTEGER, 0);
        int maximum = Math.max(0, Math.min(12,
                getInt("maximum-ricochets", 1, context)));
        if (count >= maximum) {
            return false;
        }

        Vector incoming = incomingVelocity(source, context);
        double minimumSquared = Math.max(0.0D,
                getDouble("minimum-velocity-squared", 0.09D, context));
        if (incoming.lengthSquared() < minimumSquared) {
            return false;
        }

        BlockFace face = resolveHitFace(event, incoming);
        Vector reflected = reflect(incoming, face.getDirection());
        if (reflected == null) {
            return false;
        }

        double speedBonus = Math.max(0.0D,
                getDouble("speed-bonus-per-ricochet", 0.1D, context));
        double minimumSpeed = Math.max(0.01D,
                getDouble("minimum-post-ricochet-speed", 0.45D, context));
        double speed = Math.max(minimumSpeed, incoming.length()) * (1.0D + speedBonus);
        double maximumSpeed = Math.max(minimumSpeed,
                getDouble("maximum-post-ricochet-speed", 1.5D, context));
        speed = Math.min(speed, maximumSpeed);
        if (!Double.isFinite(speed) || speed <= 0.0D) {
            return false;
        }

        Location spawnLocation = spawnLocation(source, face, reflected, context);
        World world = spawnLocation.getWorld();
        if (world == null || !world.isChunkLoaded(
                spawnLocation.getBlockX() >> 4, spawnLocation.getBlockZ() >> 4)) {
            return false;
        }

        Entity spawned = world.spawnEntity(spawnLocation, source.getType());
        if (!(spawned instanceof AbstractArrow replacement)) {
            spawned.remove();
            return false;
        }

        copyArrow(source, replacement);
        replacement.getPersistentDataContainer().set(
                RICOCHET_COUNT, PersistentDataType.INTEGER, count + 1);
        double damageBonus = Math.max(0.0D,
                getDouble("damage-bonus-per-ricochet", 0.0D, context));
        AbilityDamageUtil.markDamageBonus(replacement,
                AbilityDamageUtil.getDamageBonus(source) + damageBonus);
        replacement.setVelocity(reflected.multiply(speed));

        double maximumDistance = Math.max(0.0D,
                getDouble("maximum-ricochet-distance", 24.0D, context));
        limitFlightDistance(replacement, maximumDistance);

        if (context.result() != null) {
            context.result().skillEntity(replacement);
        }
        inheritTracking(replacement, context);
        AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), context);
        source.remove();
        return true;
    }

    private Vector incomingVelocity(AbstractArrow source, PowerContext context) {
        Vector velocity = source.getVelocity().clone();
        double liveMinimum = Math.max(0.0D,
                getDouble("minimum-live-velocity-squared", 0.0004D, context));
        if (velocity.lengthSquared() >= liveMinimum) {
            return velocity;
        }
        Vector facing = source.getLocation().getDirection();
        if (facing.lengthSquared() <= 1.0E-7D) {
            return velocity;
        }
        double speed = Math.max(0.01D,
                getDouble("minimum-post-ricochet-speed", 0.45D, context));
        return facing.normalize().multiply(speed);
    }

    private Vector reflect(Vector incoming, Vector surfaceNormal) {
        if (incoming.lengthSquared() <= 1.0E-7D || surfaceNormal.lengthSquared() <= 1.0E-7D) {
            return null;
        }
        Vector direction = incoming.clone().normalize();
        Vector normal = surfaceNormal.clone().normalize();
        Vector reflected = direction.subtract(normal.multiply(2.0D * direction.dot(normal)));
        return reflected.lengthSquared() <= 1.0E-7D ? null : reflected.normalize();
    }

    private BlockFace resolveHitFace(ProjectileHitEvent event, Vector incoming) {
        if (event.getHitBlockFace() != null) {
            return event.getHitBlockFace();
        }
        double x = Math.abs(incoming.getX());
        double y = Math.abs(incoming.getY());
        double z = Math.abs(incoming.getZ());
        if (y >= x && y >= z) {
            return incoming.getY() > 0.0D ? BlockFace.DOWN : BlockFace.UP;
        }
        if (x >= z) {
            return incoming.getX() > 0.0D ? BlockFace.WEST : BlockFace.EAST;
        }
        return incoming.getZ() > 0.0D ? BlockFace.NORTH : BlockFace.SOUTH;
    }

    private Location spawnLocation(AbstractArrow source, BlockFace face,
                                   Vector reflected, PowerContext context) {
        double surfaceOffset = Math.max(0.0D,
                getDouble("surface-offset", 0.22D, context));
        double directionOffset = Math.max(0.0D,
                getDouble("direction-offset", 0.14D, context));
        return source.getLocation().clone()
                .add(face.getDirection().normalize().multiply(surfaceOffset))
                .add(reflected.clone().multiply(directionOffset));
    }

    private void copyArrow(AbstractArrow source, AbstractArrow target) {
        target.setShooter(source.getShooter());
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

    private void inheritTracking(AbstractArrow projectile, PowerContext context) {
        TriggerManager manager = TriggerManager.triggerManager;
        Player player = context.player();
        ItemStack item = context.triggerItem() == null ? context.item() : context.triggerItem();
        EquipmentSlot slot = context.contextSlot() == null
                ? EquipmentSlot.HAND : context.contextSlot();
        if (manager != null && player != null && item != null && !item.getType().isAir()) {
            manager.runtime().trackProjectile(player, projectile, item, slot);
        }
    }

    private void limitFlightDistance(AbstractArrow projectile, double maximumDistance) {
        if (maximumDistance <= 0.0D) {
            return;
        }
        Location[] previous = {projectile.getLocation().clone()};
        double[] traveled = {0.0D};
        SchedulerUtil[] task = new SchedulerUtil[1];
        task[0] = SchedulerUtil.runTaskTimer(projectile, () -> {
            if (!projectile.isValid() || projectile.isDead() || projectile.isOnGround()) {
                task[0].cancel();
                return;
            }
            Location current = projectile.getLocation();
            if (!current.getWorld().equals(previous[0].getWorld())) {
                removeLimitedProjectile(projectile, task[0]);
                return;
            }
            traveled[0] += previous[0].distance(current);
            previous[0] = current;
            if (traveled[0] >= maximumDistance) {
                removeLimitedProjectile(projectile, task[0]);
            }
        }, 1L, 1L);
    }

    private void removeLimitedProjectile(AbstractArrow projectile, SchedulerUtil task) {
        TriggerManager manager = TriggerManager.triggerManager;
        if (manager != null) {
            manager.runtime().stopTrackingProjectile(projectile);
        }
        projectile.remove();
        task.cancel();
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SKILL;
    }
}
