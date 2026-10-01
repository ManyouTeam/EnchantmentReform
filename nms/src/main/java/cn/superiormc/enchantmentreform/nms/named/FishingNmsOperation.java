package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.nms.FishingUseResult;
import cn.superiormc.enchantmentreform.nms.NmsStatus;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FishingNmsOperation {

    private final NamedNmsHandles.Common common;
    private final NamedNmsHandles.Fishing fishing;
    private final Logger logger;
    private final AtomicBoolean failureLogged = new AtomicBoolean();

    FishingNmsOperation(NamedNmsHandles.Common common, NamedNmsHandles.Fishing fishing, Logger logger) {
        this.common = common;
        this.fishing = fishing;
        this.logger = logger;
    }

    FishingUseResult retrieve(Player player, FishHook expectedHook, EquipmentSlot hand) {
        if (player == null || expectedHook == null) {
            return new FishingUseResult(NmsStatus.INVALID_ARGUMENT);
        }
        try {
            Object serverPlayer = common.craftPlayerGetHandle().invoke(player);
            Object currentHook = fishing.fishingHook().get(serverPlayer);
            Object expectedNmsHook = fishing.craftEntityGetHandle().invoke(expectedHook);
            if (currentHook == null || currentHook != expectedNmsHook) {
                return new FishingUseResult(NmsStatus.INVALID_STATE);
            }
            useRod(serverPlayer, hand);
            return new FishingUseResult(fishing.fishingHook().get(serverPlayer) == null
                    ? NmsStatus.SUCCESS : NmsStatus.INVALID_STATE);
        } catch (IllegalArgumentException exception) {
            return new FishingUseResult(NmsStatus.INVALID_ARGUMENT);
        } catch (Throwable throwable) {
            logOnce("Native fishing rod retrieval failed; AutoFishing NMS mode is disabled for this call.", throwable);
            return new FishingUseResult(NmsStatus.FAILED);
        }
    }

    FishingUseResult cast(Player player, EquipmentSlot hand) {
        if (player == null) {
            return new FishingUseResult(NmsStatus.INVALID_ARGUMENT);
        }
        try {
            Object serverPlayer = common.craftPlayerGetHandle().invoke(player);
            if (fishing.fishingHook().get(serverPlayer) != null) {
                return new FishingUseResult(NmsStatus.INVALID_STATE);
            }
            useRod(serverPlayer, hand);
            return new FishingUseResult(fishing.fishingHook().get(serverPlayer) != null
                    ? NmsStatus.SUCCESS : NmsStatus.INVALID_STATE);
        } catch (IllegalArgumentException exception) {
            return new FishingUseResult(NmsStatus.INVALID_ARGUMENT);
        } catch (Throwable throwable) {
            logOnce("Native fishing rod cast failed; AutoFishing NMS mode is disabled for this call.", throwable);
            return new FishingUseResult(NmsStatus.FAILED);
        }
    }

    private void useRod(Object serverPlayer, EquipmentSlot hand) throws Throwable {
        Object nmsHand = switch (hand) {
            case HAND -> fishing.mainHand();
            case OFF_HAND -> fishing.offHand();
            default -> throw new IllegalArgumentException("Fishing rods can only be used from a hand");
        };
        Object serverLevel = common.serverPlayerLevel().invoke(serverPlayer);
        Object rod = fishing.getItemInHand().invoke(serverPlayer, nmsHand);
        fishing.useItem().invoke(rod, serverLevel, serverPlayer, nmsHand);
    }

    private void logOnce(String message, Throwable throwable) {
        if (failureLogged.compareAndSet(false, true)) {
            logger.log(Level.WARNING, message, throwable);
        }
    }
}
