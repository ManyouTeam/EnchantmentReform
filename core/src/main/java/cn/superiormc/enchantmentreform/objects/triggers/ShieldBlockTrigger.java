package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class ShieldBlockTrigger extends EventTrigger<EntityDamageByEntityEvent> {

    public ShieldBlockTrigger() {
        super("shield_block", "on-shield-block", EntityDamageByEntityEvent.class, Set.of());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void handle(EntityDamageByEntityEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Entity direct = event.getDamager();
        Entity attacker = runtime.getDamager(direct, event);
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
