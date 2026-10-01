package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.managers.AntiAbuseManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class AntiAbuseListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        manager().placedBlocks().markPlayerPlaced(event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        SchedulerUtil.runTaskLater(location,
                () -> manager().placedBlocks().remove(location.getBlock()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event) {
        for (BlockState state : event.getBlocks()) {
            manager().placedBlocks().markPlayerPlaced(state.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        if (manager().placedBlocks().isPlayerPlaced(event.getSource())) {
            manager().placedBlocks().markPlayerPlaced(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason().name().contains("SPAWNER")) {
            event.getEntity().getPersistentDataContainer().set(
                    manager().spawnerMobKey(), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        move(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        move(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach(manager().placedBlocks()::remove);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().forEach(manager().placedBlocks()::remove);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        manager().placedBlocks().remove(event.getLocation().getBlock());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager().playerQuit(event.getPlayer().getUniqueId());
        if (SkillManager.skillManager != null) {
            SkillManager.skillManager.playerQuit(event.getPlayer().getUniqueId());
        }
    }

    private void move(List<Block> blocks, org.bukkit.block.BlockFace direction) {
        List<Block> tracked = new ArrayList<>();
        for (Block block : blocks) {
            if (manager().placedBlocks().isPlayerPlaced(block)) tracked.add(block);
        }
        tracked.forEach(manager().placedBlocks()::remove);
        tracked.forEach(block -> manager().placedBlocks().markPlayerPlaced(block.getRelative(direction)));
    }

    private AntiAbuseManager manager() {
        return AntiAbuseManager.antiAbuseManager;
    }
}
