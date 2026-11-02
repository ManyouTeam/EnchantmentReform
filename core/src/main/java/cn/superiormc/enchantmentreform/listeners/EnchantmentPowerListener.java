package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.power.AttackCooldownUtil;
import cn.superiormc.enchantmentreform.power.BlockBreakProtectionUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

public class EnchantmentPowerListener implements Listener, AutoCloseable {

    protected final TriggerManager triggerManager;

    protected final SchedulerUtil tickTask;

    public EnchantmentPowerListener(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
        registerTriggerEvents(false);
        tickTask = SchedulerUtil.runTaskTimer(this::schedulePlayerTicks, 1L, 1L);
        Bukkit.getPluginManager().registerEvents(this, EnchantmentReform.instance);
    }

    private void schedulePlayerTicks() {
        BlockBreakProtectionUtil.advanceTick();
        long tick = triggerManager.runtime().nextTick();
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtil.runSync(player, () -> triggerManager.dispatch(
                    TriggerRuntime.PlayerTick.class, new TriggerRuntime.PlayerTick(player, tick)));
        }
    }

    protected void registerTriggerEvents(boolean paper) {
        for (Class<?> type : triggerManager.eventClasses()) {
            if (Event.class.isAssignableFrom(type) && paper != type.getName().startsWith("org.bukkit.")) {
                registerEvent(type.asSubclass(Event.class));
            }
        }
    }

    private <E extends Event> void registerEvent(Class<E> eventClass) {
        Bukkit.getPluginManager().registerEvent(eventClass, this, eventPriority(eventClass),
                (listener, event) -> route(eventClass, event), EnchantmentReform.instance, ignoreCancelled(eventClass));
    }

    private <E extends Event> void route(Class<E> eventClass, Event event) {
        if (!eventClass.isInstance(event) || !accepted(eventClass, event)) {
            return;
        }
        if (eventClass == EntityDamageByEntityEvent.class && event instanceof EntityDamageByEntityEvent damage) {
            AbilityDamageUtil.applyMarkedDamage(damage);
        }
        if (event instanceof EntityDamageEvent && AbilityDamageUtil.isApplyingDirectDamage()) {
            return;
        }
        triggerManager.dispatch(eventClass, eventClass.cast(event));
    }

    protected EventPriority eventPriority(Class<? extends Event> eventClass) {
        if (eventClass == ProjectileHitEvent.class) {
            return EventPriority.LOWEST;
        }
        if (eventClass.getSimpleName().equals("PlayerPurchaseEvent")
                || eventClass.getSimpleName().equals("PlayerTradeEvent")) {
            return EventPriority.HIGHEST;
        }
        return EventPriority.NORMAL;
    }

    protected boolean ignoreCancelled(Class<? extends Event> eventClass) {
        return eventClass != PlayerInteractEvent.class
                && !eventClass.getSimpleName().equals("BlockBreakProgressUpdateEvent")
                && !eventClass.getSimpleName().equals("EntityAttemptSmashAttackEvent");
    }

    protected boolean accepted(Class<? extends Event> eventClass, Event event) {
        if (ignoreCancelled(eventClass) && event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return false;
        }
        if (event instanceof PlayerInteractEvent interaction && interaction.isCancelled()
                && interaction.getAction() != Action.LEFT_CLICK_AIR) {
            return false;
        }
        return true;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTargetMonitor(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }
        SchedulerUtil.runSync(monster, () -> triggerManager.runtime().refreshTarget(monster));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        triggerManager.runtime().clearPlayerTargets(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRemove(EntityRemoveEvent event) {
        triggerManager.runtime().clearEntity(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnload(EntitiesUnloadEvent event) {
        for (Entity entity : event.getEntities()) {
            triggerManager.runtime().clearEntity(entity.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        triggerManager.runtime().playerQuit(event.getPlayer());
        AttackCooldownUtil.clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        triggerManager.unregisterAll(event.getPlugin());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProtectedBlockBreak(BlockBreakEvent event) {
        if (BlockBreakProtectionUtil.shouldCancel(event.getPlayer().getUniqueId(), event.getBlock())) {
            event.setCancelled(true);
        }
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
        triggerManager.runtime().rememberBarter(piglin.getUniqueId(), item.getThrower());
    }

    @Override
    public void close() {
        tickTask.cancel();
        BlockBreakProtectionUtil.clearProtection();
    }

}
