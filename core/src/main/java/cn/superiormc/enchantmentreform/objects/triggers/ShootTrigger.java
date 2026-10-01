package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class ShootTrigger extends EventTrigger<EntityShootBowEvent> {

    public ShootTrigger() {
        super("shoot", "on-shoot", EntityShootBowEvent.class);
    }

    @Override
    protected void handle(EntityShootBowEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        EquipmentSlot hand = event.getHand();
        TriggerData.Builder data = runtime.base(player, event)
                .source(player).skill(event.getProjectile()).target(event.getProjectile())
                .location(event.getProjectile().getLocation()).triggerItem(event.getBow(), hand)
                .extra(BuiltinContextKeys.BOW_FORCE, event.getForce())
                .extra(BuiltinContextKeys.BOW, event.getBow())
                .extra(BuiltinContextKeys.CONSUMABLE, event.getConsumable())
                .extra(BuiltinContextKeys.HAND, hand);
        if (event.getProjectile() instanceof Projectile projectile) {
            data.extra(BuiltinContextKeys.PROJECTILE, projectile);
            runtime.rememberProjectileSources(projectile, runtime.fire(this, data).executedSources());
            return;
        }
        runtime.fire(this, data);
    }
}
