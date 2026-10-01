package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.power.AttackCooldownTracker;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class EnchantmentPowerListener implements Listener {

    protected final TriggerManager triggerManager;

    protected final SchedulerUtil tickTask;

    public EnchantmentPowerListener(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
        tickTask = SchedulerUtil.runTaskTimer(this::schedulePlayerTicks, 1L, 1L);
        Bukkit.getPluginManager().registerEvents(this, EnchantmentReform.instance);
    }

    private void schedulePlayerTicks() {
        long tick = triggerManager.runtime().nextTick();
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtil.runSync(player, () -> triggerManager.dispatch(
                    TriggerRuntime.PlayerTick.class, new TriggerRuntime.PlayerTick(player, tick)));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        AbilityDamageUtil.applyMarkedDamage(event);
        if (AbilityDamageUtil.isApplyingDirectDamage()) {
            return;
        }
        dispatch(EntityDamageByEntityEvent.class, event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent event) {
        if (AbilityDamageUtil.isApplyingDirectDamage()) {
            return;
        }
        dispatch(EntityDamageEvent.class, event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInvulnerableDamage(EntityDamageEvent event) {
        if (AbilityDamageUtil.isInvulnerable(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (AbilityDamageUtil.isApplyingDirectDamage()
                || !(event.getEntity() instanceof Player player)
                || !player.isBlocking()
                || !event.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING)
                || event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) >= 0.0D) {
            return;
        }
        triggerManager.dispatch("shield_block", event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        dispatch(EntityDeathEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        dispatch(EntityShootBowEvent.class, event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        dispatch(ProjectileHitEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        dispatch(ProjectileLaunchEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombust(EntityCombustEvent event) {
        dispatch(EntityCombustEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        dispatch(EntityRegainHealthEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        dispatch(EntityTargetEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrime(ExplosionPrimeEvent event) {
        dispatch(ExplosionPrimeEvent.class, event);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        dispatch(PlayerJoinEvent.class, event);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        dispatch(PlayerRespawnEvent.class, event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        triggerManager.runtime().playerQuit(event.getPlayer());
        AttackCooldownTracker.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        triggerManager.unregisterAll(event.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        dispatch(BlockBreakEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        dispatch(BlockDamageEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        dispatch(BlockDropItemEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        dispatch(BlockPlaceEvent.class, event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Bukkit/Paper may pre-cancel LEFT_CLICK_AIR because vanilla predicts no action.
        // It is still a real input used by powers such as Air Dash. Keep respecting
        // cancellations for interactions that can affect a block or an item.
        if (event.isCancelled() && event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        dispatch(PlayerInteractEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        dispatch(PlayerItemConsumeEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        dispatch(PlayerFishEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        dispatch(PlayerItemDamageEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        dispatch(FoodLevelChangeEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAirChange(EntityAirChangeEvent event) {
        dispatch(EntityAirChangeEvent.class, event);
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        dispatch(PlayerExpChangeEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        dispatch(PlayerToggleFlightEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        dispatch(PlayerToggleSneakEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        dispatch(PlayerSwapHandItemsEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRiptide(PlayerRiptideEvent event) {
        dispatch(PlayerRiptideEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        dispatch(PlayerItemHeldEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        dispatch(EnchantItemEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPiglinPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Piglin piglin)) {
            return;
        }
        Item item = event.getItem();
        if (item == null || item.getItemStack().getType() != Material.GOLD_INGOT) {
            return;
        }
        if (item.getThrower() == null) {
            return;
        }
        triggerManager.runtime().rememberTarget(piglin.getUniqueId(), item.getThrower());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        triggerManager.dispatch(PiglinBarterEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        triggerManager.dispatch(BlockReceiveGameEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        dispatch(PlayerMoveEvent.class, event);
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        dispatch(PlayerInputEvent.class, event);
    }

    protected <E> void dispatch(Class<E> eventClass, E event) {
        triggerManager.dispatch(eventClass, event);
    }

    public void close() {
        tickTask.cancel();
    }

    protected void registerOptionalListener(String eventClassName, String listenerClassName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Class.forName(eventClassName, false, classLoader);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return;
        }

        try {
            Class<?> listenerClass = Class.forName(listenerClassName, true, classLoader);
            Listener listener = (Listener) listenerClass
                    .getDeclaredConstructor(TriggerManager.class)
                    .newInstance(triggerManager);
            Bukkit.getPluginManager().registerEvents(listener, EnchantmentReform.instance);
        } catch (Throwable ignored) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Failed to register version-special listener: " + listenerClassName);
        }
    }
}
