package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.AttackCooldownUtil;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import io.papermc.paper.event.entity.*;
import io.papermc.paper.event.player.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.logging.Level;

public class PaperEnchantmentPowerListener extends EnchantmentPowerListener {

    public PaperEnchantmentPowerListener(TriggerManager triggerManager) {
        super(triggerManager);
        registerOptionalEvent(
                "io.papermc.paper.event.entity.EntityLungeEvent",
                "lunge",
                EventPriority.NORMAL,
                true);
        registerOptionalEvent(
                "io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent",
                "attempt_smash_attack",
                EventPriority.NORMAL,
                false);
    }

    @EventHandler
    public void onBlockBreakProgressUpdate(BlockBreakProgressUpdateEvent event) {
        triggerManager.dispatch(BlockBreakProgressUpdateEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJump(PlayerJumpEvent event) {
        dispatch(PlayerJumpEvent.class, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPreAttack(PrePlayerAttackEntityEvent event) {
        if (!event.willAttack()) {
            return;
        }
        // Capture the strength before the attack resets it and damage triggers run.
        AttackCooldownUtil.record(event.getPlayer(), event.getAttacked(),
                event.getPlayer().getAttackCooldown());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhantomPreSpawn(PhantomPreSpawnEvent event) {
        triggerManager.dispatch(PhantomPreSpawnEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEndermanAttackPlayer(EndermanAttackPlayerEvent event) {
        triggerManager.dispatch(EndermanAttackPlayerEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLoadCrossbow(EntityLoadCrossbowEvent event) {
        triggerManager.dispatch(EntityLoadCrossbowEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEffectTick(EntityEffectTickEvent event) {
        triggerManager.dispatch(EntityEffectTickEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        triggerManager.dispatch(EntityPotionEffectEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInsideBlock(EntityInsideBlockEvent event) {
        triggerManager.dispatch(EntityInsideBlockEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        triggerManager.dispatch(EntityKnockbackEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExhaustion(EntityExhaustionEvent event) {
        triggerManager.dispatch(EntityExhaustionEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onNameEntity(PlayerNameEntityEvent event) {
        triggerManager.dispatch(PlayerNameEntityEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        triggerManager.dispatch(PlayerElytraBoostEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupExperience(PlayerPickupExperienceEvent event) {
        triggerManager.dispatch(PlayerPickupExperienceEvent.class, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPurchase(PlayerPurchaseEvent event) {
        // PlayerTradeEvent shares the purchase event hierarchy. It is dispatched explicitly below
        // so on-purchase runs exactly once for villager and wandering-trader transactions.
        if (event instanceof PlayerTradeEvent) {
            return;
        }
        triggerManager.dispatch(PlayerPurchaseEvent.class, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTrade(PlayerTradeEvent event) {
        triggerManager.dispatch(PlayerPurchaseEvent.class, event);
        if (!event.isCancelled()) {
            triggerManager.dispatch(PlayerTradeEvent.class, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWardenAngerChange(WardenAngerChangeEvent event) {
        triggerManager.dispatch(WardenAngerChangeEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShieldDisable(PlayerShieldDisableEvent event) {
        dispatch(PlayerShieldDisableEvent.class, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemGroupCooldown(PlayerItemGroupCooldownEvent event) {
        dispatch(PlayerItemGroupCooldownEvent.class, event);
    }

    private <E extends Event> void dispatchOptionalEvent(Class<E> eventClass, Event event) {
        triggerManager.dispatch(eventClass, eventClass.cast(event));
    }

    private void registerOptionalEvent(String eventClassName,
                                       String triggerId,
                                       EventPriority priority,
                                       boolean ignoreCancelled) {
        if (triggerManager.get(triggerId).isEmpty()) {
            return;
        }

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Class<? extends Event> eventClass = Class
                    .forName(eventClassName, false, classLoader)
                    .asSubclass(Event.class);
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    this,
                    priority,
                    (listener, event) -> dispatchOptionalEvent(eventClass, event),
                    EnchantmentReform.instance,
                    ignoreCancelled);
        } catch (ClassNotFoundException | LinkageError | ClassCastException exception) {
            EnchantmentReform.instance.getLogger().log(
                    Level.WARNING,
                    "Could not register optional Paper event " + eventClassName,
                    exception);
        }
    }
}
