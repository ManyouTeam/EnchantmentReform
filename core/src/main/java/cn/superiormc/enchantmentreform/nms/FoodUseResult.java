package cn.superiormc.enchantmentreform.nms;

import org.bukkit.inventory.ItemStack;

public record FoodUseResult(NmsStatus status, ItemStack remainder) {

    public boolean successful() {
        return status == NmsStatus.SUCCESS;
    }
}
