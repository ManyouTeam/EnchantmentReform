package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleUnaryOperator;

public final class PowerStateStore {

    private static final Map<Key, Entry> VALUES = new ConcurrentHashMap<>();

    private static final long STALE_ENTRY_MS = 30L * 60L * 1000L;

    private static final long SWEEP_PERIOD_TICKS = 20L * 60L;

    private static final int MAX_ENTRIES = 16_384;

    private static final AtomicLong LAST_SWEEP_AT = new AtomicLong();

    private static volatile SchedulerUtil sweepTask;

    private PowerStateStore() {
    }

    public static Key key(PowerContext context, String name, boolean perTarget) {
        return key(context, name, perTarget, EntitySelector.SOURCE);
    }

    public static Key key(PowerContext context, String name, boolean perTarget,
                          EntitySelector ownerSelector) {
        return key(context, name, perTarget, ownerSelector, EntitySelector.TARGET);
    }

    public static Key key(PowerContext context, String name, boolean perTarget,
                          EntitySelector ownerSelector, EntitySelector targetSelector) {
        Entity owner = context == null ? null : context.entity(ownerSelector);
        Entity target = context == null ? null : context.entity(targetSelector);
        String powerId = context == null || context.power() == null ? "none" : context.power().getId();
        return key(owner, powerId, "configured", name, perTarget ? target : null);
    }

    public static Key key(PowerContext context, String namespace, String name, boolean perTarget) {
        return key(context, namespace, name, perTarget, EntitySelector.SOURCE);
    }

    public static Key key(PowerContext context, String namespace, String name, boolean perTarget,
                          EntitySelector ownerSelector) {
        return key(context, namespace, name, perTarget, ownerSelector, EntitySelector.TARGET);
    }

    public static Key key(PowerContext context, String namespace, String name, boolean perTarget,
                          EntitySelector ownerSelector, EntitySelector targetSelector) {
        Entity source = context == null ? null : context.entity(ownerSelector);
        Entity target = context == null ? null : context.entity(targetSelector);
        String powerId = context == null || context.power() == null ? "none" : context.power().getId();
        return key(source, powerId, namespace, name, perTarget ? target : null);
    }

    public static Key key(Entity owner, String powerId, String namespace, String name, Entity target) {
        return new Key(owner == null ? null : owner.getUniqueId(), powerId, namespace, name,
                target == null ? null : target.getUniqueId());
    }

    public static double get(Key key) {
        if (key == null) {
            return 0.0D;
        }
        long now = System.currentTimeMillis();
        AtomicReference<Double> result = new AtomicReference<>(0.0D);
        VALUES.computeIfPresent(key, (ignored, entry) -> {
            if (entry.expired(now)) {
                return null;
            }
            result.set(entry.value);
            return entry.touch(now);
        });
        return result.get();
    }

    /** Returns the remaining lifetime of an active entry in seconds, or zero when absent/expired. */
    public static double getRemainingSeconds(Key key) {
        if (key == null) {
            return 0.0D;
        }
        long now = System.currentTimeMillis();
        AtomicReference<Double> result = new AtomicReference<>(0.0D);
        VALUES.computeIfPresent(key, (ignored, entry) -> {
            if (entry.expired(now)) {
                return null;
            }
            double remaining = entry.expiresAt <= 0L
                    ? Double.POSITIVE_INFINITY
                    : Math.max(0.0D, (entry.expiresAt - now) / 1000.0D);
            result.set(remaining);
            return entry.touch(now);
        });
        return result.get();
    }

    public static void set(Key key, double value, double maximum, double durationSeconds) {
        update(key, ignored -> value, maximum, durationSeconds);
    }

    /** Atomically updates a number and returns both its previous and resulting values. */
    public static Update update(Key key, DoubleUnaryOperator operation, double maximum,
                                double durationSeconds) {
        if (key == null || operation == null) {
            return new Update(0.0D, 0.0D);
        }
        long now = System.currentTimeMillis();
        long expiresAt = expiresAt(now, durationSeconds);
        AtomicReference<Update> result = new AtomicReference<>();
        VALUES.compute(key, (ignored, entry) -> {
            double previous = entry == null || entry.expired(now) ? 0.0D : entry.value;
            double limit = Double.isNaN(maximum) ? Double.MAX_VALUE : Math.max(0.0D, maximum);
            double requested = operation.applyAsDouble(previous);
            double current = Double.isFinite(requested)
                    ? Math.min(limit, Math.max(0.0D, requested)) : 0.0D;
            result.set(new Update(previous, current));
            return current <= 0.0D ? null : new Entry(current, expiresAt, now);
        });
        maybeSweep(now);
        return result.get();
    }

    /** Atomically acquires a cooldown lease, returning false while an existing lease is active. */
    public static boolean tryAcquireCooldown(Key key, double durationSeconds) {
        if (key == null || durationSeconds <= 0.0D) {
            return true;
        }
        long now = System.currentTimeMillis();
        long expiresAt = expiresAt(now, durationSeconds);
        AtomicBoolean acquired = new AtomicBoolean();
        VALUES.compute(key, (ignored, entry) -> {
            if (entry != null && !entry.expired(now)) {
                return entry.touch(now);
            }
            acquired.set(true);
            return new Entry(1.0D, expiresAt, now);
        });
        maybeSweep(now);
        return acquired.get();
    }

    public static double consume(Key key) {
        Entry entry = key == null ? null : VALUES.remove(key);
        long now = System.currentTimeMillis();
        return entry == null || entry.expired(now) ? 0.0D : entry.value;
    }

    public static void remove(Key key) {
        if (key != null) {
            VALUES.remove(key);
        }
    }

    public static void clearEntity(UUID entityId) {
        if (entityId != null) {
            VALUES.keySet().removeIf(key -> entityId.equals(key.owner) || entityId.equals(key.target));
        }
    }

    public static void clearPower(String powerId) {
        if (powerId != null) {
            VALUES.keySet().removeIf(key -> powerId.equals(key.powerId));
        }
    }

    public static void clearNamespace(String namespace) {
        if (namespace != null) {
            VALUES.keySet().removeIf(key -> namespace.equals(key.namespace));
        }
    }

    public static void clearAll() {
        VALUES.clear();
        LAST_SWEEP_AT.set(System.currentTimeMillis());
    }

    public static synchronized void start() {
        if (sweepTask == null) {
            sweepTask = SchedulerUtil.runTaskTimer(PowerStateStore::sweep, SWEEP_PERIOD_TICKS,
                    SWEEP_PERIOD_TICKS);
        }
    }

    public static synchronized void shutdown() {
        if (sweepTask != null) {
            sweepTask.cancel();
            sweepTask = null;
        }
        clearAll();
    }

    static int size() {
        return VALUES.size();
    }

    private static void sweep() {
        long now = System.currentTimeMillis();
        LAST_SWEEP_AT.set(now);
        enforceBound(now);
    }

    private static void maybeSweep(long now) {
        long previous = LAST_SWEEP_AT.get();
        if (VALUES.size() <= MAX_ENTRIES && now - previous < 60_000L) {
            return;
        }
        if (!LAST_SWEEP_AT.compareAndSet(previous, now)) {
            return;
        }
        enforceBound(now);
    }

    private static void enforceBound(long now) {
        VALUES.entrySet().removeIf(entry -> entry.getValue().expired(now)
                || now - entry.getValue().lastAccessAt >= STALE_ENTRY_MS);
        int overflow = VALUES.size() - MAX_ENTRIES;
        if (overflow <= 0) {
            return;
        }
        ArrayList<Map.Entry<Key, Entry>> oldest = new ArrayList<>(VALUES.entrySet());
        oldest.sort(Comparator.comparingLong(entry -> entry.getValue().lastAccessAt));
        for (int index = 0; index < overflow && index < oldest.size(); index++) {
            Map.Entry<Key, Entry> entry = oldest.get(index);
            VALUES.remove(entry.getKey(), entry.getValue());
        }
    }

    private static long expiresAt(long now, double durationSeconds) {
        if (durationSeconds <= 0.0D || Double.isNaN(durationSeconds)) {
            return 0L;
        }
        double millis = durationSeconds * 1000.0D;
        if (!Double.isFinite(millis) || millis >= Long.MAX_VALUE - now) {
            return Long.MAX_VALUE;
        }
        return now + Math.max(1L, (long) millis);
    }

    public record Key(UUID owner, String powerId, String namespace, String name, UUID target) {
        public Key {
            powerId = normalize(powerId, "none");
            namespace = normalize(namespace, "default");
            name = normalize(name, "default");
        }

        private static String normalize(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value.trim();
        }
    }

    public record Update(double previous, double current) {
    }

    private record Entry(double value, long expiresAt, long lastAccessAt) {
        private boolean expired(long now) {
            return expiresAt > 0L && now >= expiresAt;
        }

        private Entry touch(long now) {
            return new Entry(value, expiresAt, now);
        }
    }
}
