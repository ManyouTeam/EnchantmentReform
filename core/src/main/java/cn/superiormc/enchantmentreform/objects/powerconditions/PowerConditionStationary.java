package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PowerConditionStationary extends AbstractPowerCondition {

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();
    private static final long STALE_MS = 30L * 60L * 1000L;
    private static final long CLEANUP_INTERVAL_MS = 30_000L;
    private static volatile long lastCleanupAt;

    public PowerConditionStationary(){
        super("stationary");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition){
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        if (entity == null) return false;
        Location now = entity.getLocation();
        long time = System.currentTimeMillis();
        if (time - lastCleanupAt >= CLEANUP_INTERVAL_MS) {
            lastCleanupAt = time;
            STATES.entrySet().removeIf(entry -> time - entry.getValue().lastAccess >= STALE_MS);
        }
        State old = STATES.get(entity.getUniqueId());
        UUID worldId = now.getWorld() == null ? null : now.getWorld().getUID();
        if(old == null || !java.util.Objects.equals(old.worldId, worldId) || old.distanceSquared(now) > .01D) {
            STATES.put(entity.getUniqueId(), new State(worldId, now.getX(), now.getY(), now.getZ(), time, time));
            return false;
        }
        STATES.put(entity.getUniqueId(), old.touch(time));
        return time-old.since >= condition.getDouble("seconds", 3, condition.getContext()) * 1000;
    }

    @Override
    public void onEntityUnload(UUID entityId) {
        if (entityId != null) {
            STATES.remove(entityId);
        }
    }

    @Override
    public void onUnload() {
        STATES.clear();
    }

    private record State(UUID worldId, double x, double y, double z, long since, long lastAccess) {
        private double distanceSquared(Location location) {
            double dx = x - location.getX();
            double dy = y - location.getY();
            double dz = z - location.getZ();
            return dx * dx + dy * dy + dz * dz;
        }

        private State touch(long now) {
            return new State(worldId, x, y, z, since, now);
        }
    }
}
