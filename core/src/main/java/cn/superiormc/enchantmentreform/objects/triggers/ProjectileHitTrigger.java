package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

public final class ProjectileHitTrigger extends EventTrigger<ProjectileHitEvent> {

    public ProjectileHitTrigger() {
        super("projectile_hit", "on-projectile-hit", ProjectileHitEvent.class);
    }

    @Override
    protected void handle(ProjectileHitEvent event, TriggerRuntime runtime) {
        TriggerRuntime.TrackedProjectile tracked = runtime.stopTrackingProjectile(event.getEntity());
        Player player = tracked == null ? null : Bukkit.getPlayer(tracked.ownerId());
        if (player == null && tracked == null && event.getEntity().getShooter() instanceof Player shooter) {
            player = shooter;
        }
        if (player == null) {
            return;
        }
        Entity shooter = tracked == null ? null : Bukkit.getEntity(tracked.shooterId());
        if (shooter == null && event.getEntity().getShooter() instanceof Entity entity) {
            shooter = entity;
        }
        if (shooter == null) {
            shooter = player;
        }
        Location location = event.getHitBlock() != null
                ? event.getHitBlock().getLocation().add(0.5D, 0.5D, 0.5D)
                : event.getHitEntity() != null ? event.getHitEntity().getLocation() : event.getEntity().getLocation();
        TriggerData.Builder data = runtime.base(player, event)
                .source(shooter).skill(event.getEntity()).target(event.getHitEntity())
                .block(event.getHitBlock()).location(location)
                .extra(BuiltinContextKeys.PROJECTILE, event.getEntity());
        if (tracked != null) data.triggerItem(tracked.item(), tracked.slot());
        if (tracked == null) {
            runtime.fire(this, data);
        } else {
            runtime.fireTracked(this, data.build(), tracked.sources());
        }
    }
}
