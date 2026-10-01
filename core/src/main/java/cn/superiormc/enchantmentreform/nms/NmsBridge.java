package cn.superiormc.enchantmentreform.nms;

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface NmsBridge {

    Set<NmsCapability> capabilities();

    default boolean supports(NmsCapability capability) {
        return capabilities().contains(capability);
    }

    FoodUseResult finishUsingItem(Player player, ItemStack item);

    FishingUseResult retrieveFishingRod(Player player, FishHook expectedHook, EquipmentSlot hand);

    FishingUseResult castFishingRod(Player player, EquipmentSlot hand);
}
