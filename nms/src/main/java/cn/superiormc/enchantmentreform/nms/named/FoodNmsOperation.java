package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.nms.FoodUseResult;
import cn.superiormc.enchantmentreform.nms.NmsStatus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FoodNmsOperation {

    private final NamedNmsHandles.Common common;
    private final NamedNmsHandles.Food food;
    private final Logger logger;
    private final AtomicBoolean failureLogged = new AtomicBoolean();

    FoodNmsOperation(NamedNmsHandles.Common common, NamedNmsHandles.Food food, Logger logger) {
        this.common = common;
        this.food = food;
        this.logger = logger;
    }

    FoodUseResult finishUsingItem(Player player, ItemStack item) {
        if (player == null || item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            return new FoodUseResult(NmsStatus.INVALID_ARGUMENT, null);
        }

        try {
            ItemStack single = item.clone();
            single.setAmount(1);
            Object serverPlayer = common.craftPlayerGetHandle().invoke(player);
            Object serverLevel = common.serverPlayerLevel().invoke(serverPlayer);
            Object nmsItem = food.asNmsCopy().invoke(single);
            Object nmsRemainder = food.finishUsingItem().invoke(nmsItem, serverLevel, serverPlayer);
            ItemStack remainder = (ItemStack) food.asBukkitCopy().invoke(nmsRemainder);
            if (remainder == null || remainder.getType() == Material.AIR || remainder.getAmount() <= 0) {
                remainder = null;
            }
            return new FoodUseResult(NmsStatus.SUCCESS, remainder);
        } catch (Throwable throwable) {
            logOnce("Native finishUsingItem invocation failed; AutoFeed NMS mode is disabled for this call.", throwable);
            return new FoodUseResult(NmsStatus.FAILED, null);
        }
    }

    private void logOnce(String message, Throwable throwable) {
        if (failureLogged.compareAndSet(false, true)) {
            logger.log(Level.WARNING, message, throwable);
        }
    }
}
