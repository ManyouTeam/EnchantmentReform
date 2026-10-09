package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.nms.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

final class NamedNmsBridge implements NmsBridge {

    private final FoodNmsOperation food;
    private final FishingNmsOperation fishing;
    private final UseOnNmsOperation useOn;
    private final Set<NmsCapability> capabilities;

    NamedNmsBridge(FoodNmsOperation food,
                   FishingNmsOperation fishing,
                   UseOnNmsOperation useOn) {
        this.food = food;
        this.fishing = fishing;
        this.useOn = useOn;
        EnumSet<NmsCapability> available = EnumSet.noneOf(NmsCapability.class);
        if (food != null) {
            available.add(NmsCapability.FINISH_USING_ITEM);
        }
        if (fishing != null) {
            available.add(NmsCapability.FISHING_ROD_USE);
        }
        if (useOn != null) {
            available.add(NmsCapability.USE_ON);
        }
        this.capabilities = Set.copyOf(available);
    }

    @Override
    public Set<NmsCapability> capabilities() {
        return capabilities;
    }

    @Override
    public FoodUseResult finishUsingItem(Player player, ItemStack item) {
        return food == null
                ? new FoodUseResult(NmsStatus.UNSUPPORTED, null)
                : food.finishUsingItem(player, item);
    }

    @Override
    public FishingUseResult retrieveFishingRod(Player player, FishHook expectedHook, EquipmentSlot hand) {
        return fishing == null
                ? new FishingUseResult(NmsStatus.UNSUPPORTED)
                : fishing.retrieve(player, expectedHook, hand);
    }

    @Override
    public FishingUseResult castFishingRod(Player player, EquipmentSlot hand) {
        return fishing == null
                ? new FishingUseResult(NmsStatus.UNSUPPORTED)
                : fishing.cast(player, hand);
    }

    @Override
    public UseOnResult useOn(Player player,
                             EquipmentSlot hand,
                             Block clickedBlock,
                             BlockFace clickedFace,
                             Vector hitPosition,
                             boolean inside) {
        return useOn == null
                ? new UseOnResult(NmsStatus.UNSUPPORTED)
                : useOn.useOn(player, hand, clickedBlock, clickedFace, hitPosition, inside);
    }
}
