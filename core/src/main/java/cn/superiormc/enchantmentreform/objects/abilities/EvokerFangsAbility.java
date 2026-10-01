package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;

public class EvokerFangsAbility extends AbstractAbility {

    public EvokerFangsAbility(ConfigurationSection section) {
        super("EvokerFangs", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity sourceEntity = context.source();
        Entity targetEntity = getTargetEntity(context);
        if (!(sourceEntity instanceof LivingEntity owner) || !(targetEntity instanceof LivingEntity target)) {
            return false;
        }

        if (!owner.isValid() || !target.isValid() || owner.isDead() || target.isDead()) {
            return false;
        }

        Location from = owner.getLocation();
        Location to = target.getLocation();
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) {
            return false;
        }

        double minY = Math.min(to.getY(), from.getY());
        double maxY = Math.max(to.getY(), from.getY()) + 1.0D;
        float angleToTarget = (float) Math.atan2(to.getZ() - from.getZ(), to.getX() - from.getX());
        double damage = getDouble("damage", 6.0D, context);
        LivingEntity damageSource = getSourceEntity(context) instanceof LivingEntity livingSource ? livingSource : owner;

        double closeRange = getDouble("close-range", 3.0D, context);
        if (from.distanceSquared(to) < closeRange * closeRange) {
            spawnCloseFangs(world, from, minY, maxY, angleToTarget, damageSource, damage);
        } else {
            spawnLineFangs(world, from, minY, maxY, angleToTarget, damageSource, damage);
        }
        return false;
    }

    private void spawnCloseFangs(World world, Location from, double minY, double maxY, float angleToTarget, LivingEntity owner, double damage) {
        for (int i = 0; i < 5; i++) {
            float angle = angleToTarget + i * (float) Math.PI * 0.4F;
            spawnFang(
                    world,
                    from.getX() + Math.cos(angle) * 1.5D,
                    from.getZ() + Math.sin(angle) * 1.5D,
                    minY,
                    maxY,
                    angle,
                    i * 2,
                    owner,
                    damage
            );
        }

        for (int i = 0; i < 8; i++) {
            float angle = angleToTarget + i * (float) Math.PI * 2.0F / 8.0F + 1.2566371F;
            spawnFang(
                    world,
                    from.getX() + Math.cos(angle) * 2.5D,
                    from.getZ() + Math.sin(angle) * 2.5D,
                    minY,
                    maxY,
                    angle,
                    3 + i * 2,
                    owner,
                    damage
            );
        }
    }

    private void spawnLineFangs(World world, Location from, double minY, double maxY, float angleToTarget, LivingEntity owner, double damage) {
        for (int i = 0; i < 16; i++) {
            double reach = 1.25D * (i + 1);
            spawnFang(
                    world,
                    from.getX() + Math.cos(angleToTarget) * reach,
                    from.getZ() + Math.sin(angleToTarget) * reach,
                    minY,
                    maxY,
                    angleToTarget,
                    i,
                    owner,
                    damage
            );
        }
    }

    private void spawnFang(
            World world,
            double x,
            double z,
            double minY,
            double maxY,
            float angle,
            int delay,
            LivingEntity owner,
            double damage
    ) {
        Location location = new Location(world, x, maxY, z);
        Location ground = findGround(location, minY);
        if (ground == null) {
            return;
        }

        ground.setYaw((float) Math.toDegrees(angle));
        world.spawn(ground, EvokerFangs.class, fangs -> {
            fangs.setOwner(owner);
            fangs.setAttackDelay(delay);
            AbilityDamageUtil.markDamage(fangs, damage);
        });
    }

    private Location findGround(Location start, double minY) {
        World world = start.getWorld();
        if (world == null) {
            return null;
        }

        int x = start.getBlockX();
        int z = start.getBlockZ();
        for (int y = start.getBlockY(); y >= Math.floor(minY) - 1; y--) {
            Location feet = new Location(world, x + 0.5D, y, z + 0.5D);
            Location below = feet.clone().subtract(0, 1, 0);
            if (below.getBlock().getType().isSolid()) {
                return feet;
            }
        }
        return null;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
