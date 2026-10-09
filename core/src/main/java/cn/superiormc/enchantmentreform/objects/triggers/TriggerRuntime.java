package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.AbstractTrigger;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.power.TrackedPowerSource;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class TriggerRuntime {

    private static final NamespacedKey FIRING_SLOT_KEY =
            new NamespacedKey(EnchantmentReform.instance, "firing_slot");

    private final TriggerManager manager;

    private final Map<UUID, TargetRecord> mobTargets = new ConcurrentHashMap<>();

    private final Map<UUID, TrackedProjectile> projectiles = new ConcurrentHashMap<>();

    private final Map<UUID, List<TrackedPowerSource>> pendingProjectileSources = new ConcurrentHashMap<>();

    private volatile long currentTick;

    public TriggerRuntime(TriggerManager manager) {
        this.manager = manager;
    }

    public TriggerData.Builder base(Player player, Event event) {
        return TriggerData.builder(player)
                .entities(player, player, player)
                .location(player.getLocation())
                .event(event);
    }

    public TriggerData.Builder movement(Player player, Event event, Location from, Location to) {
        return base(player, event).movement(from, to).location(to).block(to.getBlock());
    }

    public TriggerResult fire(AbstractTrigger<?> trigger, TriggerData.Builder data) {
        return manager.fire(trigger, data.build());
    }

    public TriggerResult fire(AbstractTrigger<?> trigger, TriggerData data) {
        return manager.fire(trigger, data);
    }

    public TriggerResult fireActive(AbstractTrigger<?> trigger, TriggerData data, ActivePowerSource active) {
        return manager.fireActive(trigger, data, active);
    }

    public TriggerResult fireTracked(AbstractTrigger<?> trigger, TriggerData data,
                                     Collection<TrackedPowerSource> trackedSources) {
        return manager.fireTracked(trigger, data, trackedSources);
    }

    public List<TrackedPowerSource> captureProjectileSources(TriggerData data) {
        return manager.captureProjectileSources(data);
    }

    public void rememberProjectileSources(Projectile projectile,
                                          Collection<TrackedPowerSource> sources) {
        if (projectile != null) {
            pendingProjectileSources.put(projectile.getUniqueId(), copySources(sources));
        }
    }

    public List<TrackedPowerSource> takeProjectileSources(Projectile projectile) {
        if (projectile == null) {
            return List.of();
        }
        List<TrackedPowerSource> sources = pendingProjectileSources.remove(projectile.getUniqueId());
        return sources == null ? List.of() : sources;
    }

    public long nextTick() {
        return ++currentTick;
    }

    public long currentTick() {
        return currentTick;
    }

    public void rememberTarget(UUID mob, UUID player) {
        rememberTarget(mob, player, false);
    }

    public void rememberTarget(UUID mob, UUID player, boolean monster) {
        mobTargets.put(mob, new TargetRecord(player, monster));
    }

    public Player removeTarget(UUID mob) {
        TargetRecord target = mobTargets.remove(mob);
        return target == null ? null : Bukkit.getPlayer(target.playerId());
    }

    public int countTargets(UUID player) {
        if (player == null) {
            return 0;
        }
        return (int) mobTargets.values().stream()
                .filter(TargetRecord::monster)
                .filter(target -> player.equals(target.playerId()))
                .count();
    }

    public void trackProjectile(Player player, Projectile projectile, ItemStack item, EquipmentSlot slot,
                                Collection<TrackedPowerSource> sources) {
        writeFiringSlot(projectile, slot);
        stopTrackingProjectile(projectile);
        Projectile[] active = {projectile};
        SchedulerUtil[] task = new SchedulerUtil[1];
        task[0] = SchedulerUtil.runTaskTimer(projectile, () -> {
            Projectile current = active[0];
            if (current == null || !current.isValid() || current.isDead() || current.isOnGround()) {
                if (current != null) {
                    stopTrackingProjectile(current);
                } else {
                    task[0].cancel();
                }
                return;
            }
            Entity shooter = current.getShooter() instanceof Entity entity ? entity : player;
            manager.dispatch(TriggerRuntime.ProjectileTick.class,
                    new ProjectileTick(player, shooter, current, item, slot, copySources(sources), currentTick,
                            result -> {
                        if (result.cancelled()) {
                            stopTrackingProjectile(current);
                        } else if (result.skillEntity() instanceof Projectile replacement) {
                            transferTrackedProjectile(current, replacement);
                            active[0] = replacement;
                        } else if (result.skillEntity() != null) {
                            stopTrackingProjectile(current);
                        }
                    }));
        }, 1L, 1L);
        UUID shooterId = projectile.getShooter() instanceof Entity shooter
                ? shooter.getUniqueId() : player.getUniqueId();
        projectiles.put(projectile.getUniqueId(),
                new TrackedProjectile(player.getUniqueId(), shooterId, item, slot,
                        copySources(sources), task[0]));
    }

    /** Compatibility path for abilities that launch a projectile outside an EntityShootBowEvent. */
    public void trackProjectile(Player player, Projectile projectile, ItemStack item, EquipmentSlot slot) {
        TriggerData data = base(player, null).triggerItem(item, slot).build();
        trackProjectile(player, projectile, item, slot, manager.captureProjectileSources(data));
    }

    public TrackedProjectile stopTrackingProjectile(Projectile projectile) {
        TrackedProjectile tracked = projectiles.remove(projectile.getUniqueId());
        if (tracked != null) {
            tracked.task().cancel();
        }
        return tracked;
    }

    private void transferTrackedProjectile(Projectile previous, Projectile replacement) {
        if (previous.getUniqueId().equals(replacement.getUniqueId())) {
            return;
        }
        TrackedProjectile tracked = projectiles.remove(previous.getUniqueId());
        if (tracked == null) {
            return;
        }
        TrackedProjectile replaced = projectiles.remove(replacement.getUniqueId());
        if (replaced != null && replaced.task() != tracked.task()) {
            replaced.task().cancel();
        }
        UUID shooterId = replacement.getShooter() instanceof Entity shooter
                ? shooter.getUniqueId() : tracked.shooterId();
        projectiles.put(replacement.getUniqueId(), new TrackedProjectile(
                tracked.ownerId(), shooterId, tracked.item(), tracked.slot(), tracked.sources(), tracked.task()));
    }

    /**
     * Persists the hand that launched a projectile onto the projectile itself, so later events
     * (e.g. the damage dealt on impact) can recover the firing slot even after the in-memory
     * tracking record has been consumed by {@link #stopTrackingProjectile(Projectile)}.
     */
    public static void writeFiringSlot(Projectile projectile, EquipmentSlot slot) {
        if (projectile == null || slot == null) {
            return;
        }
        projectile.getPersistentDataContainer().set(
                FIRING_SLOT_KEY, PersistentDataType.STRING, slot.name());
    }

    /**
     * Reads back the firing slot previously written by {@link #writeFiringSlot}, or {@code null}
     * when the projectile carries no such marker (e.g. it was not launched from a tracked weapon).
     */
    public static EquipmentSlot readFiringSlot(Projectile projectile) {
        if (projectile == null) {
            return null;
        }
        PersistentDataContainer pdc = projectile.getPersistentDataContainer();
        String stored = pdc.get(FIRING_SLOT_KEY, PersistentDataType.STRING);
        if (stored == null) {
            return null;
        }
        try {
            return EquipmentSlot.valueOf(stored);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public EquipmentSlot findSlot(Player player, ItemStack item) {
        if (player.getInventory().getItemInMainHand() == item) return EquipmentSlot.HAND;
        if (player.getInventory().getItemInOffHand() == item) return EquipmentSlot.OFF_HAND;
        if (player.getInventory().getHelmet() == item) return EquipmentSlot.HEAD;
        if (player.getInventory().getChestplate() == item) return EquipmentSlot.CHEST;
        if (player.getInventory().getLeggings() == item) return EquipmentSlot.LEGS;
        if (player.getInventory().getBoots() == item) return EquipmentSlot.FEET;
        return EquipmentSlot.HAND;
    }

    public void clearEntity(UUID entityId) {
        mobTargets.remove(entityId);
        PowerManager.powerManager.onEntityUnload(entityId);
    }

    public void playerQuit(Player player) {
        if (EnchantmentReform.methodUtil != null) {
            EnchantmentReform.methodUtil.clearBossBars(player);
        }
        UUID playerId = player.getUniqueId();
        mobTargets.values().removeIf(target -> playerId.equals(target.playerId()));
        projectiles.entrySet().removeIf(entry -> {
            if (!entry.getValue().ownerId().equals(playerId)) return false;
            entry.getValue().task().cancel();
            return true;
        });
        clearEntity(playerId);
        manager.activeEnchantments().clear(player);
    }

    public void close() {
        // Avoid linking a lambda to TrackedProjectile for the first time during shutdown.
        // An empty runtime should not need to load that class to clear its state.
        for (TrackedProjectile projectile : projectiles.values()) {
            projectile.task().cancel();
        }
        projectiles.clear();
        pendingProjectileSources.clear();
        mobTargets.clear();
    }

    private static List<TrackedPowerSource> copySources(Collection<TrackedPowerSource> sources) {
        return sources == null ? List.of() : List.copyOf(new LinkedHashSet<>(sources));
    }

    private record TargetRecord(UUID playerId, boolean monster) {
    }

    public record PlayerTick(Player player, long tick) {
    }

    public record ProjectileTick(
            Player player,
            Entity shooter,
            Projectile projectile,
            ItemStack item,
            EquipmentSlot slot,
            List<TrackedPowerSource> sources,
            long tick,
            Consumer<TriggerResult> completion
    ) {
        public void complete(TriggerResult result) {
            completion.accept(result);
        }
    }

    public record Activation(Player player, Event event, ActivePowerSource active) {
    }

    public record Deactivation(Player player, Event event, ActivePowerSource active) {
    }

    public record TrackedProjectile(
            UUID ownerId,
            UUID shooterId,
            ItemStack item,
            EquipmentSlot slot,
            List<TrackedPowerSource> sources,
            SchedulerUtil task
    ) {
    }
}
