package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.AbstractTrigger;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.power.TrackedPowerSource;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public final class TriggerRuntime {

    private static final NamespacedKey FIRING_SLOT_KEY =
            new NamespacedKey("enchantmentreform", "firing_slot");

    private final TriggerManager manager;

    private final Map<UUID, TargetRecord> mobTargets = new ConcurrentHashMap<>();

    private final Map<UUID, UUID> barterPlayers = new ConcurrentHashMap<>();

    private final Map<UUID, TrackedProjectile> projectiles = new ConcurrentHashMap<>();

    private final Map<UUID, List<TrackedPowerSource>> pendingProjectileSources = new ConcurrentHashMap<>();

    private volatile long currentTick;

    private volatile boolean closed;

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
        Location destination = to == null ? from : to;
        return base(player, event).movement(from, destination).location(destination).block(destination.getBlock());
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
            UUID projectileId = projectile.getUniqueId();
            List<TrackedPowerSource> captured = copySources(sources);
            pendingProjectileSources.put(projectileId, captured);
            SchedulerUtil.runTaskLater(() -> pendingProjectileSources.remove(projectileId, captured), 1L);
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
        return countTargets(player, null);
    }

    public int countTargets(UUID player, Entity pendingTarget) {
        if (player == null) {
            return 0;
        }
        Player targetPlayer = Bukkit.getPlayer(player);
        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead()) {
            clearPlayerTargets(player);
            return 0;
        }
        UUID pendingId = pendingTarget == null ? null : pendingTarget.getUniqueId();
        int count = pendingTarget instanceof Monster ? 1 : 0;
        for (Map.Entry<UUID, TargetRecord> entry : mobTargets.entrySet()) {
            UUID mobId = entry.getKey();
            TargetRecord record = entry.getValue();
            if (!record.monster() || !player.equals(record.playerId()) || mobId.equals(pendingId)) {
                continue;
            }
            Entity entity = Bukkit.getEntity(mobId);
            if (!(entity instanceof Monster monster) || !monster.isValid() || monster.isDead()
                    || !monster.getWorld().equals(targetPlayer.getWorld())) {
                mobTargets.remove(mobId, record);
                continue;
            }
            if (EnchantmentReform.isFolia && !Bukkit.isOwnedByCurrentRegion(monster)) {
                SchedulerUtil.runSync(monster, () -> {
                    if (!isCurrentTarget(monster, targetPlayer)) {
                        mobTargets.remove(mobId, record);
                    }
                });
            } else if (!isCurrentTarget(monster, targetPlayer)) {
                mobTargets.remove(mobId, record);
                continue;
            }
            count++;
        }
        return count;
    }

    private boolean isCurrentTarget(Monster monster, Player player) {
        if (!monster.isValid() || monster.isDead() || !player.isOnline() || player.isDead()
                || !monster.getWorld().equals(player.getWorld())) {
            return false;
        }
        LivingEntity target = monster.getTarget();
        return target != null && player.getUniqueId().equals(target.getUniqueId());
    }

    public void refreshTarget(Entity entity) {
        if (closed || !(entity instanceof Monster monster)) {
            return;
        }
        UUID mobId = monster.getUniqueId();
        TargetRecord record = mobTargets.get(mobId);
        if (record != null && !record.monster()) {
            return;
        }
        if (monster.isValid() && !monster.isDead()
                && monster.getTarget() instanceof Player player && isCurrentTarget(monster, player)) {
            rememberTarget(mobId, player.getUniqueId(), true);
        } else if (record != null) {
            mobTargets.remove(mobId, record);
        }
    }

    public void clearPlayerTargets(UUID playerId) {
        mobTargets.values().removeIf(target -> playerId.equals(target.playerId()));
    }

    public void rememberBarter(UUID piglin, UUID player) {
        barterPlayers.put(piglin, player);
    }

    public Player takeBarterPlayer(UUID piglin) {
        UUID player = barterPlayers.remove(piglin);
        return player == null ? null : Bukkit.getPlayer(player);
    }

    public void trackProjectile(Player player, Projectile projectile, ItemStack item, EquipmentSlot slot,
                                Collection<TrackedPowerSource> sources) {
        UUID shooterId = projectile.getShooter() instanceof Entity shooter
                ? shooter.getUniqueId() : player.getUniqueId();
        trackProjectile(player, projectile, item, slot, sources, shooterId);
    }

    private void trackProjectile(Player player, Projectile projectile, ItemStack item, EquipmentSlot slot,
                                 Collection<TrackedPowerSource> sources, UUID shooterId) {
        if (closed) {
            return;
        }
        writeFiringSlot(projectile, slot);
        stopTrackingProjectile(projectile);
        UUID projectileId = projectile.getUniqueId();
        AtomicBoolean retired = new AtomicBoolean();
        SchedulerUtil[] task = new SchedulerUtil[1];
        task[0] = SchedulerUtil.runTaskTimer(projectile, () -> {
            TrackedProjectile tracked = projectiles.get(projectileId);
            if (tracked == null || tracked.task() != task[0]) {
                task[0].cancel();
                return;
            }
            if (closed || !projectile.isValid() || projectile.isDead() || projectile.isOnGround()) {
                stopTrackingProjectile(projectile);
                return;
            }
            Entity shooter = projectile.getShooter() instanceof Entity entity ? entity : player;
            manager.dispatch(TriggerRuntime.ProjectileTick.class,
                    new ProjectileTick(player, shooter, projectile, item, slot, copySources(sources), currentTick,
                            result -> {
                        if (result.cancelled()) {
                            stopTrackingProjectile(projectile);
                        } else if (result.skillEntity() instanceof Projectile replacement) {
                            transferTrackedProjectile(projectile, replacement, tracked);
                        } else if (result.skillEntity() != null) {
                            stopTrackingProjectile(projectile);
                        }
                    }));
        }, () -> {
            retired.set(true);
            TrackedProjectile tracked = projectiles.get(projectileId);
            if (tracked != null && tracked.task() == task[0]) {
                projectiles.remove(projectileId, tracked);
            }
            pendingProjectileSources.remove(projectileId);
        }, 1L, 1L);
        if (task[0] == null) {
            return;
        }
        TrackedProjectile tracked = new TrackedProjectile(player.getUniqueId(), shooterId, item, slot,
                copySources(sources), task[0]);
        projectiles.put(projectileId, tracked);
        if ((closed || retired.get()) && projectiles.remove(projectileId, tracked)) {
            task[0].cancel();
        }
    }

    /** Compatibility path for abilities that launch a projectile outside an EntityShootBowEvent. */
    public void trackProjectile(Player player, Projectile projectile, ItemStack item, EquipmentSlot slot) {
        TriggerData data = base(player, null).triggerItem(item, slot).build();
        trackProjectile(player, projectile, item, slot, manager.captureProjectileSources(data));
    }

    public TrackedProjectile stopTrackingProjectile(Projectile projectile) {
        return stopTrackingProjectile(projectile.getUniqueId());
    }

    private TrackedProjectile stopTrackingProjectile(UUID projectileId) {
        pendingProjectileSources.remove(projectileId);
        TrackedProjectile tracked = projectiles.remove(projectileId);
        if (tracked != null) {
            tracked.task().cancel();
        }
        return tracked;
    }

    private void transferTrackedProjectile(Projectile previous, Projectile replacement, TrackedProjectile tracked) {
        if (previous.getUniqueId().equals(replacement.getUniqueId())) {
            return;
        }
        stopTrackingProjectile(previous);
        Player player = Bukkit.getPlayer(tracked.ownerId());
        if (player == null || !player.isOnline()) {
            stopTrackingProjectile(replacement);
            return;
        }
        UUID shooterId = replacement.getShooter() instanceof Entity shooter
                ? shooter.getUniqueId() : tracked.shooterId();
        trackProjectile(player, replacement, tracked.item(), tracked.slot(), tracked.sources(), shooterId);
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
        String stored = projectile.getPersistentDataContainer().get(FIRING_SLOT_KEY, PersistentDataType.STRING);
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
        PlayerInventory inventory = player.getInventory();
        for (EquipmentSlot slot : List.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (inventory.getItem(slot) == item) {
                return slot;
            }
        }
        for (EquipmentSlot slot : List.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (item != null && item.equals(inventory.getItem(slot))) {
                return slot;
            }
        }
        return EquipmentSlot.HAND;
    }

    public Entity getDamager(Entity direct, EntityDamageEvent event) {
        return direct == null ? null : EnchantmentReform.methodUtil.getDamager(direct);
    }

    public void clearEntity(UUID entityId) {
        mobTargets.remove(entityId);
        barterPlayers.remove(entityId);
        clearPlayerTargets(entityId);
        stopTrackingProjectile(entityId);
        PowerManager.powerManager.onEntityUnload(entityId);
    }

    public void playerQuit(Player player) {
        if (EnchantmentReform.methodUtil != null) {
            EnchantmentReform.methodUtil.clearBossBars(player);
        }
        UUID playerId = player.getUniqueId();
        barterPlayers.values().removeIf(playerId::equals);
        clearPlayerTargets(playerId);
        projectiles.entrySet().removeIf(entry -> {
            if (!entry.getValue().ownerId().equals(playerId)) {
                return false;
            }
            entry.getValue().task().cancel();
            return true;
        });
        clearEntity(playerId);
        manager.activeEnchantments().clear(player);
    }

    public void close() {
        closed = true;
        // Avoid linking a lambda to TrackedProjectile for the first time during shutdown.
        // An empty runtime should not need to load that class to clear its state.
        for (TrackedProjectile projectile : projectiles.values()) {
            projectile.task().cancel();
        }
        projectiles.clear();
        pendingProjectileSources.clear();
        mobTargets.clear();
        barterPlayers.clear();
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
