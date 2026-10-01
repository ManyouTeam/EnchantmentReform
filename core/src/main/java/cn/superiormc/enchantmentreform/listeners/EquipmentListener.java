package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.power.ActiveEnchantmentManager;
import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

public final class EquipmentListener implements Listener {

    private final TriggerManager triggerManager;

    public EquipmentListener(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEquipmentChanged(EntityEquipmentChangedEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Map<EquipmentSlot, ActiveEnchantmentManager.EquipmentChange> changes =
                new EnumMap<>(EquipmentSlot.class);
        event.getEquipmentChanges().forEach((slot, change) -> changes.put(slot,
                new ActiveEnchantmentManager.EquipmentChange(change.oldItem(), change.newItem())));

        ActiveEnchantmentManager.EquipmentTransition transition =
                triggerManager.activeEnchantments().updateEquipment(player, changes);
        for (ActivePowerSource active : transition.deactivated()) {
            triggerManager.dispatch(TriggerRuntime.Deactivation.class,
                    new TriggerRuntime.Deactivation(player, event, active));
            if (AbilityManager.abilityManager != null) {
                AbilityManager.abilityManager.onPowerSourceDeactivate(player, active);
            }
        }
        for (ActivePowerSource active : transition.activated()) {
            triggerManager.dispatch(TriggerRuntime.Activation.class,
                    new TriggerRuntime.Activation(player, event, active));
        }
    }
}
