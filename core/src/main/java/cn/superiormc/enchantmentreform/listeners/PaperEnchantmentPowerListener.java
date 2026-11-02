package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.AttackCooldownUtil;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public final class PaperEnchantmentPowerListener extends EnchantmentPowerListener {

    public PaperEnchantmentPowerListener(TriggerManager triggerManager) {
        super(triggerManager);
        registerTriggerEvents(true);
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
}
