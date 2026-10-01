package cn.superiormc.enchantmentreform.api.nms;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

/**
 * Optional NMS bridge extension for invoking the held item's native useOn flow.
 */
public interface UseOnNmsBridge {

    boolean useOn(Player player,
                  EquipmentSlot hand,
                  Block clickedBlock,
                  BlockFace clickedFace,
                  Vector hitPosition,
                  boolean inside);
}
