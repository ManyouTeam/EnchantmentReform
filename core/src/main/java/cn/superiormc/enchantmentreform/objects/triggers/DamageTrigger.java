package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class DamageTrigger extends EventTrigger<EntityDamageEvent> {

    public DamageTrigger() {
        super("damage", "on-damage", EntityDamageEvent.class);
    }

    @Override
    protected void handle(EntityDamageEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Entity direct = event instanceof EntityDamageByEntityEvent byEntityEvent
                ? byEntityEvent.getDamager() : null;
        Entity attacker = direct == null ? null : EnchantmentReform.methodUtil.getDamager(direct);
        if (attacker == null) {
            attacker = direct;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(attacker).skill(direct == null ? player : direct)
                .target(player).location(player.getLocation())
                .extra(BuiltinContextKeys.ORIGINAL_DAMAGE, event.getDamage())
                .extra(BuiltinContextKeys.DAMAGE_CAUSE, event.getCause())
                .extra(BuiltinContextKeys.DAMAGE_BY_ENTITY,
                        event instanceof EntityDamageByEntityEvent)
                .extra(BuiltinContextKeys.DAMAGE_BY_BLOCK,
                        event instanceof EntityDamageByBlockEvent));
    }
}
