package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

public final class HoneyExtractTrigger extends EventTrigger<PlayerInteractEvent> {

    public HoneyExtractTrigger() {
        super("honey_extract", "on-honey-extract", PlayerInteractEvent.class);
    }

    @Override
    protected void handle(PlayerInteractEvent event, TriggerRuntime runtime) {
        runtime.harvestHoney(this, event, Material.GLASS_BOTTLE);
    }
}
