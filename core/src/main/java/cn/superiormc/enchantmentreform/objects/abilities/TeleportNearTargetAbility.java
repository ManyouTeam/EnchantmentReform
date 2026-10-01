package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

public class TeleportNearTargetAbility extends AbstractAbility {

    public TeleportNearTargetAbility(ConfigurationSection section) {
        super("TeleportNearTarget", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof LivingEntity living)) {
            return false;
        }

        if (EnchantmentReform.isFolia) {
            SchedulerUtil.runSync(living, () -> teleportNear(living, context));
            return false;
        }

        teleportNear(living, context);
        return false;
    }

    private void teleportNear(LivingEntity living, PowerContext context) {
        double minRadius = Math.max(1.0, getDouble("min-radius", 3.0, context));
        double maxRadius = Math.max(minRadius, getDouble("max-radius", 8.0, context));
        int maxTry = Math.max(1, getInt("max-tries", 12, context));

        for (int i = 0; i < maxTry; i++) {
            Location candidate = randomAround(living.getLocation(), minRadius, maxRadius);
            Location safe = findSafeLocation(candidate);
            if (safe == null) {
                continue;
            }
            safe.setYaw(living.getLocation().getYaw());
            safe.setPitch(living.getLocation().getPitch());
            SchedulerUtil.teleport(living, safe);
            return;
        }
    }

    private Location randomAround(Location center, double minRadius, double maxRadius) {
        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        double radius = ThreadLocalRandom.current().nextDouble(minRadius, maxRadius);
        return center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    private Location findSafeLocation(Location base) {
        Location probe = base.clone();
        int minY = probe.getWorld().getMinHeight();
        int maxY = probe.getWorld().getMaxHeight() - 2;
        int y = Math.max(minY + 1, Math.min(maxY, probe.getBlockY()));

        for (int off = 0; off <= 6; off++) {
            Location up = probe.clone();
            up.setY(Math.min(maxY, y + off));
            if (isSafe(up)) {
                return up.add(0.5, 0, 0.5);
            }

            Location down = probe.clone();
            down.setY(Math.max(minY + 1, y - off));
            if (isSafe(down)) {
                return down.add(0.5, 0, 0.5);
            }
        }
        return null;
    }

    private boolean isSafe(Location feet) {
        Block blockFeet = feet.getBlock();
        Block blockHead = feet.clone().add(0, 1, 0).getBlock();
        Block blockBelow = feet.clone().add(0, -1, 0).getBlock();
        return blockFeet.isPassable() && blockHead.isPassable() && blockBelow.getType().isSolid();
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
