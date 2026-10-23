package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class MoveTrigger extends EventTrigger<PlayerMoveEvent> {

    public MoveTrigger() {
        super("move", "on-move", PlayerMoveEvent.class);
    }

    @Override
    protected void handle(PlayerMoveEvent event, TriggerRuntime runtime) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || (Objects.equals(from.getWorld(), to.getWorld())
                && from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())) {
            return;
        }
        double distance = event instanceof PlayerTeleportEvent || !Objects.equals(from.getWorld(), to.getWorld())
                ? 0D : from.distance(to);
        runtime.fire(this, runtime.movement(event.getPlayer(), event, from, to)
                .extra(BuiltinContextKeys.MOVEMENT_DISTANCE, distance));
    }
}
