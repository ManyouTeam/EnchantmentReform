package cn.superiormc.enchantmentreform.paper.listeners;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.TempBlockManager;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public final class PaperTempBlockListener extends TempBlockManager implements Listener {

    public PaperTempBlockListener() {
        Bukkit.getPluginManager().registerEvents(this, EnchantmentReform.instance);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        if (!isTempBlock(location)) return;
        event.setDropItems(false);
        event.setExpToDrop(0);
        removeTempBlock(location);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        restoreExplodedBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        restoreExplodedBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakBlock(BlockBreakBlockEvent event) {
        Block block = event.getBlock();
        if (!isTempBlock(block.getLocation())) return;
        event.getDrops().clear();
        SchedulerUtil.runTaskLater(block.getLocation(), () -> removeTempBlock(block.getLocation()), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent event) {
        Block block = event.getBlock();
        if (!isTempBlock(block.getLocation())) return;
        event.setWillDrop(false);
        removeTempBlock(block.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (containsTempBlock(event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (containsTempBlock(event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Location location = event.getBlock().getLocation();
        if (!isTempBlock(location)) return;
        event.setCancelled(true);
        removeTempBlock(location);
    }

    private void restoreExplodedBlocks(Iterable<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!isTempBlock(block.getLocation())) continue;
            iterator.remove();
            removeTempBlock(block.getLocation());
        }
    }

    private boolean containsTempBlock(Iterable<Block> blocks, BlockFace direction) {
        for (Block block : blocks) {
            if (isTempBlock(block.getLocation())
                    || isTempBlock(block.getRelative(direction).getLocation())) {
                return true;
            }
        }
        return false;
    }
}
