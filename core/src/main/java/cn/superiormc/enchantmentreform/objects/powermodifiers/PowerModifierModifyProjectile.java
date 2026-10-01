package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class PowerModifierModifyProjectile extends AbstractPowerModifier {

    public PowerModifierModifyProjectile(ConfigurationSection section) {
        super("modify_projectile", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        Entity entity = context.skill();
        if (!(entity instanceof Projectile)) {
            entity = context.target();
        }
        if (!(entity instanceof Projectile projectile)) {
            return;
        }

        double speedMultiplier = getDouble("speed-multiplier", 1.0D, context);
        float force = context.triggerData().extra(BuiltinContextKeys.BOW_FORCE).orElse(0.0F);
        if (section.contains("draw-speed-multiplier") && force > 0.0F) {
            double effective = Math.min(1.0D,
                    force * getDouble("draw-speed-multiplier", 1.0D, context));
            speedMultiplier *= effective / force;
        }
        projectile.setVelocity(projectile.getVelocity().multiply(speedMultiplier));

        if (section.contains("spread-degrees")) {
            double spread = Math.toRadians(Math.max(0.0D,
                    getDouble("spread-degrees", 0.0D, context)));
            Vector velocity = projectile.getVelocity();
            velocity.rotateAroundY(ThreadLocalRandom.current().nextDouble(-spread, spread));
            velocity.rotateAroundX(ThreadLocalRandom.current().nextDouble(-spread, spread));
            projectile.setVelocity(velocity);
        }
        if (section.contains("accuracy") && context.source() instanceof LivingEntity shooter) {
            double accuracy = Math.max(0.0D, Math.min(1.0D,
                    getDouble("accuracy", 1.0D, context)));
            double speed = projectile.getVelocity().length();
            Vector desired = shooter.getEyeLocation().getDirection().normalize().multiply(speed);
            projectile.setVelocity(projectile.getVelocity().multiply(1.0D - accuracy)
                    .add(desired.multiply(accuracy)));
        }
        if (section.contains("gravity")) {
            projectile.setGravity(section.getBoolean("gravity"));
        }
        if (section.contains("fire-ticks")) {
            projectile.setFireTicks(Math.max(0,
                    getInt("fire-ticks", 0, context)));
        }
        if (projectile instanceof AbstractArrow arrow) {
            double damageMultiplier = getDouble("damage-multiplier", 1.0D, context);
            arrow.setDamage(Math.max(0.0D, arrow.getDamage() * damageMultiplier));
            arrow.setCritical(section.getBoolean("critical", arrow.isCritical()));
            int pierce = Math.max(0, Math.min(127,
                    getInt("pierce-level", arrow.getPierceLevel(), context)));
            arrow.setPierceLevel(pierce);
        }
    }
}
