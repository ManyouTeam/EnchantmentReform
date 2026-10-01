package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ShieldBlockTrigger extends EventTrigger<EntityDamageByEntityEvent> {

    public ShieldBlockTrigger() {
        super("shield_block", "on-shield-block", EntityDamageByEntityEvent.class, java.util.Set.of());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void handle(EntityDamageByEntityEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Entity direct = event.getDamager();
        Entity attacker = EnchantmentReform.methodUtil.getDamager(direct);
        if (attacker == null) {
            attacker = direct;
        }
        double blocked = event.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING)
                ? Math.max(0.0D, -event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING))
                : 0.0D;
        runtime.fire(this, runtime.base(player, event)
                .source(attacker).skill(direct).target(player)
                .location(player.getLocation())
                .triggerItem()
                .extra(BuiltinContextKeys.ORIGINAL_DAMAGE, blocked)
                .extra(BuiltinContextKeys.DAMAGE_CAUSE, event.getCause())
                .extra(BuiltinContextKeys.DAMAGE_BY_ENTITY, true));
    }
}
