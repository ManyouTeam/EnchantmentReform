package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class AttackTrigger extends EventTrigger<EntityDamageByEntityEvent> {

    public AttackTrigger() {
        super("attack", "on-attack", EntityDamageByEntityEvent.class);
    }

    @Override
    protected void handle(EntityDamageByEntityEvent event, TriggerRuntime runtime) {
        Entity direct = event.getDamager();
        Entity attacker = EnchantmentReform.methodUtil.getDamager(direct);
        if (attacker == null) {
            attacker = direct;
        }
        if (!(attacker instanceof Player player)) {
            return;
        }
        ItemStack triggerItem;
        EquipmentSlot triggerSlot;
        if (direct instanceof Trident trident) {
            triggerItem = trident.getWeapon();
            triggerSlot = firingSlot(trident, EquipmentSlot.HAND);
        } else if (direct instanceof Projectile projectile) {
            triggerSlot = firingSlot(projectile, EquipmentSlot.HAND);
            triggerItem = itemAt(player.getInventory(), triggerSlot);
        } else {
            triggerItem = player.getInventory().getItemInMainHand();
            triggerSlot = EquipmentSlot.HAND;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(player).skill(direct).target(event.getEntity())
                .location(event.getEntity().getLocation())
                .triggerItem(triggerItem, triggerSlot)
                .extra(BuiltinContextKeys.ORIGINAL_DAMAGE, event.getDamage())
                .extra(BuiltinContextKeys.DAMAGE_CAUSE, event.getCause()));
    }

    private static EquipmentSlot firingSlot(Projectile projectile, EquipmentSlot fallback) {
        EquipmentSlot stored = TriggerRuntime.readFiringSlot(projectile);
        return stored != null ? stored : fallback;
    }

    private static ItemStack itemAt(PlayerInventory inventory, EquipmentSlot slot) {
        return slot == EquipmentSlot.OFF_HAND
                ? inventory.getItemInOffHand()
                : inventory.getItemInMainHand();
    }
}
