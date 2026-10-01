package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import io.lumine.mythic.bukkit.utils.lib.math3.analysis.function.Pow;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class SonicBoomAbility extends AbstractAbility {

    public SonicBoomAbility(ConfigurationSection section) {
        super("SonicBoom", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity sourceEntity = getSourceEntity(context);
        Entity targetEntity = getTargetEntity(context);
        if (!(sourceEntity instanceof LivingEntity source) || !(targetEntity instanceof LivingEntity target)) {
            return false;
        }

        if (!isInRange(source, target, context)) {
            return false;
        }

        World world = source.getWorld();
        float chargeVolume = (float) getDouble("charge-volume", 3.0, context);
        float chargePitch = (float) getDouble("charge-pitch", 1.0, context);
        world.playSound(source.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, chargeVolume, chargePitch);

        int chargeTicks = Math.max(1, getInt("charge-ticks", 34, context));
        SchedulerUtil.runTaskLater(source, () -> boom(source, target, context), chargeTicks);
        return false;
    }

    private void boom(LivingEntity source, LivingEntity target, PowerContext context) {
        if (!source.isValid() || !target.isValid() || source.isDead() || target.isDead()) {
            return;
        }

        if (!isInRange(source, target, context)) {
            return;
        }

        World world = source.getWorld();
        Vector sourcePosition = source.getLocation()
                .add(0.0D, source.getBoundingBox().getHeight() * 0.55D, 0.0D)
                .toVector();
        Vector targetPosition = target.getEyeLocation().toVector();
        Vector delta = targetPosition.clone().subtract(sourcePosition);
        if (delta.lengthSquared() < 0.0001D) {
            return;
        }

        Vector direction = delta.clone().normalize();
        int extraSteps = Math.max(0, getInt("particle-extra-steps", 7, context));
        int steps = (int) Math.floor(delta.length()) + extraSteps;
        for (int i = 1; i < steps; i++) {
            Vector particlePosition = sourcePosition.clone().add(direction.clone().multiply(i));
            world.spawnParticle(
                    Particle.SONIC_BOOM,
                    particlePosition.getX(),
                    particlePosition.getY(),
                    particlePosition.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        float boomVolume = (float) getDouble("boom-volume", 3.0, context);
        float boomPitch = (float) getDouble("boom-pitch", 1.0, context);
        world.playSound(source.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, boomVolume, boomPitch);

        double damage = Math.max(0.0D, getDouble("damage", 10.0D, context));
        if (damage > 0.0D) {
            AbilityDamageUtil.runDirectDamage(() -> target.damage(damage, getSourceEntity(context)));
        }

        double horizontal = getDouble("knockback-horizontal", 2.5D, context);
        double vertical = getDouble("knockback-vertical", 0.5D, context);
        Vector knockback = new Vector(
                direction.getX() * horizontal,
                direction.getY() * vertical,
                direction.getZ() * horizontal
        );
        target.setVelocity(target.getVelocity().add(knockback));
    }

    private boolean isInRange(LivingEntity source, LivingEntity target, PowerContext context) {
        if (!source.getWorld().equals(target.getWorld())) {
            return false;
        }

        double rangeXZ = getDouble("range-xz", 15.0D, context);
        double rangeY = getDouble("range-y", 20.0D, context);
        double dx = source.getLocation().getX() - target.getLocation().getX();
        double dz = source.getLocation().getZ() - target.getLocation().getZ();
        double dy = Math.abs(source.getLocation().getY() - target.getLocation().getY());

        return dx * dx + dz * dz <= rangeXZ * rangeXZ && dy <= rangeY;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
