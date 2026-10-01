package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class DamageByEntityTrigger extends EventTrigger<EntityDamageByEntityEvent> {

    public DamageByEntityTrigger() {
        super("damage_by_entity", "on-damage-by-entity", EntityDamageByEntityEvent.class);
    }

    @Override
    protected void handle(EntityDamageByEntityEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Entity direct = event.getDamager();
        Entity attacker = EnchantmentReform.methodUtil.getDamager(direct);
        if (attacker == null) {
            attacker = direct;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(attacker).skill(direct).target(player).location(player.getLocation())
                .extra(BuiltinContextKeys.ORIGINAL_DAMAGE, event.getDamage())
                .extra(BuiltinContextKeys.DAMAGE_CAUSE, event.getCause())
                .extra(BuiltinContextKeys.DAMAGE_BY_ENTITY, true)
                .extra(BuiltinContextKeys.DAMAGE_BY_BLOCK, false));
    }
}
