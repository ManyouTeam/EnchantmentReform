package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class ProjectileLaunchTrigger extends EventTrigger<ProjectileLaunchEvent> {

    public ProjectileLaunchTrigger() {
        super("projectile_launch", "on-projectile-launch", ProjectileLaunchEvent.class);
    }

    @Override
    protected void handle(ProjectileLaunchEvent event, TriggerRuntime runtime) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        runtime.fire(this, runtime.base(player, event)
                .source(player).skill(event.getEntity()).target(event.getEntity())
                .location(event.getEntity().getLocation())
                .triggerItem(player.getInventory().getItemInMainHand(), EquipmentSlot.HAND)
                .extra(BuiltinContextKeys.PROJECTILE, event.getEntity()));
    }
}

