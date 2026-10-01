package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ShootBowTrigger extends EventTrigger<EntityShootBowEvent> {

    public ShootBowTrigger() {
        super("shoot_bow", "on-shoot-bow", EntityShootBowEvent.class);
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
        }
        TriggerData triggerData = data.build();
        TriggerResult result = runtime.fire(this, triggerData);
        Entity effectiveProjectile = result.skillEntity() == null
                ? event.getProjectile() : result.skillEntity();
        if (effectiveProjectile instanceof Projectile projectile) {
            Set<cn.superiormc.enchantmentreform.power.TrackedPowerSource> sources = new LinkedHashSet<>(
                    event.getProjectile() instanceof Projectile original
                            ? runtime.takeProjectileSources(original) : Set.of());
            sources.addAll(runtime.captureProjectileSources(triggerData));
            sources.addAll(result.executedSources());
            runtime.trackProjectile(player, projectile, event.getBow(), hand, sources);
        }
    }
}
