package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import java.util.LinkedHashMap;
import java.util.Objects;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public final class TriggerRuntime {

    private static final Set<InventoryAction> OUTPUT_ACTIONS = Set.of(
            InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_SOME, InventoryAction.MOVE_TO_OTHER_INVENTORY,
            InventoryAction.HOTBAR_SWAP, InventoryAction.HOTBAR_MOVE_AND_READD,
            InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_SLOT);

    private static final NamespacedKey WORKSTATION_PLAYER_KEY = new NamespacedKey("enchantmentreform", "workstation_player");

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

    public ItemStack anvilOutput(InventoryClickEvent event, String operation) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack result = event.getCurrentItem();
        if (first == null || result == null || first.getType() != result.getType()) {
            return null;
        }
        ItemMeta before = first.getItemMeta();
        ItemMeta after = result.getItemMeta();
        boolean matches;
        if (operation.equals("repair")) {
            matches = before instanceof Damageable oldDamage && after instanceof Damageable newDamage
                    && newDamage.getDamage() < oldDamage.getDamage();
        } else if (operation.equals("enchant")) {
            Map<Enchantment, Integer> oldEnchants = enchantmentLevels(first);
            matches = enchantmentLevels(result).entrySet().stream()
                    .anyMatch(entry -> entry.getValue() > oldEnchants.getOrDefault(entry.getKey(), 0));
        } else {
            String oldName = before != null && before.hasDisplayName() ? before.getDisplayName() : "";
            String newName = after != null && after.hasDisplayName() ? after.getDisplayName() : "";
            matches = !Objects.equals(oldName, newName);
        }
        return matches ? inventoryOutput(event, result, List.of(), false) : null;
    }

    private Map<Enchantment, Integer> enchantmentLevels(ItemStack item) {
        Map<Enchantment, Integer> enchants = new LinkedHashMap<>(item.getEnchantments());
        if (item.getItemMeta() instanceof EnchantmentStorageMeta book) {
            book.getStoredEnchants().forEach((enchantment, level) -> enchants.merge(enchantment, level, Math::max));
        }
        return enchants;
    }

    public void finishInventoryOutput(AbstractTrigger<?> trigger, InventoryClickEvent event, ItemStack output) {
        if (!(event.getWhoClicked() instanceof Player player) || event.isCancelled()) {
            return;
        }
        Inventory inventory = event.getInventory();
        ItemStack[] inputs = inventory.getContents();
        for (int slot = 0; slot < inputs.length; slot++) {
            inputs[slot] = inputs[slot] == null ? null : inputs[slot].clone();
        }
        int resultSlot = event.getSlot();
        SchedulerUtil.runTaskLater(player, () -> {
            if (closed || event.isCancelled() || !player.isOnline()) {
                return;
            }
            boolean changed = false;
            for (int slot = 0; slot < inputs.length; slot++) {
                if (slot == resultSlot) {
                    continue;
                }
                ItemStack before = inputs[slot];
                ItemStack after = inventory.getItem(slot);
                boolean oldEmpty = before == null || before.getType().isAir() || before.getAmount() <= 0;
                boolean newEmpty = after == null || after.getType().isAir() || after.getAmount() <= 0;
                if (!oldEmpty && (newEmpty || !before.isSimilar(after) || after.getAmount() < before.getAmount())) {
                    changed = true;
                    break;
                }
            }
            if (changed) {
                fire(trigger, base(player, event).outputItems(List.of(output)).triggerItem(output, null));
            }
        }, 1L);
    }

    public void harvestHoney(AbstractTrigger<?> trigger, PlayerInteractEvent event, Material tool) {
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (closed || event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null || item == null
                || item.getType() != tool || event.useItemInHand() == Event.Result.DENY
                || !(block.getBlockData() instanceof Beehive hive) || hive.getHoneyLevel() < hive.getMaximumHoneyLevel()) {
            return;
        }
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack bottle = item.clone();
        int before = item.getAmount();
        SchedulerUtil.runTaskLater(player, () -> {
            if (closed || event.isCancelled() || !player.isOnline()
                    || !(block.getBlockData() instanceof Beehive current) || current.getHoneyLevel() >= hive.getHoneyLevel()) {
                return;
            }
            ItemStack held = player.getInventory().getItem(hand);
            if (held.getType() != Material.HONEY_BOTTLE
                    && !(held.getType() == Material.GLASS_BOTTLE && held.getAmount() < before)) {
                return;
            }
            ItemStack output = new ItemStack(Material.HONEY_BOTTLE);
            fire(trigger, base(player, event).block(block).location(block.getLocation().add(0.5D, 0.5D, 0.5D))
                    .outputItems(List.of(output)).triggerItem(bottle, hand));
        }, 1L);
    }

    public ItemStack inventoryOutput(InventoryClickEvent event, ItemStack result,
                                     List<ItemStack> ingredients, boolean repeat) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)
                || event.getSlotType() != InventoryType.SlotType.RESULT
                || result == null || result.getType().isAir() || result.getAmount() <= 0) {
            return null;
        }
        InventoryAction action = event.getAction();
        if (!OUTPUT_ACTIONS.contains(action)) {
            return null;
        }
        int operations = 1;
        if (repeat && event.isShiftClick()) {
            operations = ingredients.stream().filter(item -> item != null && !item.getType().isAir())
                    .mapToInt(ItemStack::getAmount).min().orElse(0);
        }
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            long capacity = 0L;
            for (ItemStack stored : player.getInventory().getStorageContents()) {
                if (stored == null || stored.getType().isAir()) {
                    capacity += result.getMaxStackSize();
                } else if (stored.isSimilar(result)) {
                    capacity += Math.max(0, Math.min(stored.getMaxStackSize(), result.getMaxStackSize()) - stored.getAmount());
                }
            }
            operations = (int) Math.min(operations, capacity / result.getAmount());
        } else if (action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_ONE
                || action == InventoryAction.PICKUP_HALF || action == InventoryAction.PICKUP_SOME) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir()
                    && (!cursor.isSimilar(result) || cursor.getAmount() + result.getAmount() > result.getMaxStackSize())) {
                return null;
            }
        }
        if (operations <= 0) {
            return null;
        }
        ItemStack output = result.clone();
        output.setAmount((int) Math.min(Integer.MAX_VALUE, (long) operations * result.getAmount()));
        return output;
    }

    public void trackWorkstationInput(Event event, Class<? extends Inventory> inventoryType, int... slots) {
        if (closed || event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return;
        }
        if (event instanceof InventoryMoveItemEvent move) {
            if (inventoryType.isInstance(move.getDestination())
                    && move.getDestination().getHolder() instanceof BlockInventoryHolder holder) {
                setWorkstationPlayer(holder.getBlock(), null);
            }
            return;
        }
        Inventory inventory;
        Player player;
        if (event instanceof InventoryClickEvent click && click.getWhoClicked() instanceof Player actor) {
            inventory = click.getInventory();
            player = actor;
            boolean inputClick = click.getClickedInventory() == inventory
                    && Arrays.stream(slots).anyMatch(slot -> slot == click.getSlot());
            boolean shiftInput = click.isShiftClick() && click.getClickedInventory() != null
                    && click.getClickedInventory() != inventory;
            if (!inputClick && !shiftInput) {
                return;
            }
        } else if (event instanceof InventoryDragEvent drag && drag.getWhoClicked() instanceof Player actor) {
            inventory = drag.getInventory();
            player = actor;
            if (Arrays.stream(slots).noneMatch(slot -> drag.getRawSlots().contains(slot))) {
                return;
            }
        } else {
            return;
        }
        if (!inventoryType.isInstance(inventory) || !(inventory.getHolder() instanceof BlockInventoryHolder holder)) {
            return;
        }
        Block block = holder.getBlock();
        Material blockType = block.getType();
        UUID playerId = player.getUniqueId();
        ItemStack[] previous = new ItemStack[slots.length];
        for (int index = 0; index < slots.length; index++) {
            ItemStack item = inventory.getItem(slots[index]);
            previous[index] = item == null ? null : item.clone();
        }
        SchedulerUtil.runTaskLater(block.getLocation(), () -> {
            if (closed || block.getType() != blockType || !(block.getState() instanceof BlockInventoryHolder currentHolder)) {
                return;
            }
            Inventory current = currentHolder.getInventory();
            boolean empty = true;
            boolean added = false;
            for (int index = 0; index < slots.length; index++) {
                ItemStack item = current.getItem(slots[index]);
                if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                    continue;
                }
                empty = false;
                ItemStack before = previous[index];
                if (before == null || !item.isSimilar(before) || item.getAmount() > before.getAmount()) {
                    added = true;
                }
            }
            if (added) {
                setWorkstationPlayer(block, playerId);
            } else if (empty) {
                setWorkstationPlayer(block, null);
            }
        }, 1L);
    }

    public Player workstationPlayer(Block block) {
        if (!(block.getState() instanceof TileState state)) {
            return null;
        }
        long[] owner = state.getPersistentDataContainer().get(WORKSTATION_PLAYER_KEY, PersistentDataType.LONG_ARRAY);
        if (owner == null || owner.length != 2) {
            return null;
        }
        Player player = Bukkit.getPlayer(new UUID(owner[0], owner[1]));
        return player != null && player.isOnline() ? player : null;
    }

    private void setWorkstationPlayer(Block block, UUID playerId) {
        if (!(block.getState() instanceof TileState state)) {
            return;
        }
        if (playerId == null) {
            state.getPersistentDataContainer().remove(WORKSTATION_PLAYER_KEY);
        } else {
            state.getPersistentDataContainer().set(WORKSTATION_PLAYER_KEY, PersistentDataType.LONG_ARRAY,
                    new long[] {playerId.getMostSignificantBits(), playerId.getLeastSignificantBits()});
        }
        state.update();
    }

    public TriggerData.Builder movement(Player player, Event event, Location from, Location to) {
        Location destination = to == null ? from : to;
        return base(player, event).movement(from, destination).location(destination).block(destination.getBlock());
    }

    public TriggerResult fire(AbstractTrigger<?> trigger, TriggerData.Builder data) {
        return fire(trigger, data.build());
    }

    public TriggerResult fire(AbstractTrigger<?> trigger, TriggerData data) {
        if (EnchantmentReform.isFolia
                && (data.event() instanceof BrewEvent || data.event() instanceof FurnaceSmeltEvent)
                && !Bukkit.isOwnedByCurrentRegion(data.player())) {
            SchedulerUtil.runSync(data.player(), () -> manager.fire(trigger, data));
            return new TriggerResult();
        }
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
