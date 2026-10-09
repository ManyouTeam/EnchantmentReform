package cn.superiormc.enchantmentreform.nms;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;

public final class UnsupportedNmsBridge implements NmsBridge {

    private final String reason;

    public UnsupportedNmsBridge(String reason) {
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }

    @Override
    public Set<NmsCapability> capabilities() {
        return Set.of();
    }

    @Override
    public FoodUseResult finishUsingItem(Player player, ItemStack item) {
        return new FoodUseResult(NmsStatus.UNSUPPORTED, null);
    }

    @Override
    public FishingUseResult retrieveFishingRod(Player player, FishHook expectedHook, EquipmentSlot hand) {
        return new FishingUseResult(NmsStatus.UNSUPPORTED);
    }

    @Override
    public FishingUseResult castFishingRod(Player player, EquipmentSlot hand) {
        return new FishingUseResult(NmsStatus.UNSUPPORTED);
    }

    @Override
    public UseOnResult useOn(Player player,
                             EquipmentSlot hand,
                             Block clickedBlock,
                             BlockFace clickedFace,
                             Vector hitPosition,
                             boolean inside) {
        return new UseOnResult(NmsStatus.UNSUPPORTED);
    }
}
