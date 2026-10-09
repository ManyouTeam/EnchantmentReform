package cn.superiormc.enchantmentreform.nms;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;

public interface NmsBridge {

    Set<NmsCapability> capabilities();

    default boolean supports(NmsCapability capability) {
        return capabilities().contains(capability);
    }

    FoodUseResult finishUsingItem(Player player, ItemStack item);

    FishingUseResult retrieveFishingRod(Player player, FishHook expectedHook, EquipmentSlot hand);

    FishingUseResult castFishingRod(Player player, EquipmentSlot hand);

    UseOnResult useOn(Player player,
                      EquipmentSlot hand,
                      Block clickedBlock,
                      BlockFace clickedFace,
                      Vector hitPosition,
                      boolean inside);
}
