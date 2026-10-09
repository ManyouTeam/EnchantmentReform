package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.nms.NmsStatus;
import cn.superiormc.enchantmentreform.nms.UseOnResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

final class UseOnNmsOperation {

    private final NamedNmsHandles.Common common;
    private final NamedNmsHandles.UseOn useOn;
    private final Logger logger;
    private final AtomicBoolean failureLogged = new AtomicBoolean();

    UseOnNmsOperation(NamedNmsHandles.Common common,
                      NamedNmsHandles.UseOn useOn,
                      Logger logger) {
        this.common = common;
        this.useOn = useOn;
        this.logger = logger;
    }

    UseOnResult useOn(Player player,
                      EquipmentSlot hand,
                      Block clickedBlock,
                      BlockFace clickedFace,
                      Vector hitPosition,
                      boolean inside) {
        if (player == null || clickedBlock == null || clickedFace == null || hitPosition == null
                || hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return new UseOnResult(NmsStatus.INVALID_ARGUMENT);
        }

        ItemStack bukkitItem = hand == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
        if (bukkitItem.getType() == Material.AIR || bukkitItem.getAmount() <= 0) {
            return new UseOnResult(NmsStatus.INVALID_STATE);
        }

        try {
            Object serverPlayer = common.craftPlayerGetHandle().invoke(player);
            Object serverLevel = common.serverPlayerLevel().invoke(serverPlayer);
            Object nmsHand = hand == EquipmentSlot.HAND ? useOn.mainHand() : useOn.offHand();
            Object direction = direction(clickedFace);
            if (direction == null) {
                return new UseOnResult(NmsStatus.INVALID_ARGUMENT);
            }

            Object nmsItem = useOn.getItemInHand().invoke(serverPlayer, nmsHand);
            Object blockPos = useOn.blockPosConstructor().invoke(
                    clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
            Object hitVector = useOn.vec3Constructor().invoke(
                    hitPosition.getX(), hitPosition.getY(), hitPosition.getZ());
            Object hitResult = useOn.blockHitResultConstructor().invoke(
                    hitVector, direction, blockPos, inside);
            Object context = useOn.extendedContextConstructor()
                    ? useOn.useOnContextConstructor().invoke(
                            serverLevel, serverPlayer, nmsHand, nmsItem, hitResult)
                    : useOn.useOnContextConstructor().invoke(
                            serverPlayer, nmsHand, hitResult);
            Object result = useOn.useOnItem().invoke(nmsItem, context);
            return new UseOnResult(successful(result) ? NmsStatus.SUCCESS : NmsStatus.FAILED);
        } catch (Throwable throwable) {
            logOnce("Native ItemStack#useOn invocation failed; UseOnAbility is disabled for this call.", throwable);
            return new UseOnResult(NmsStatus.FAILED);
        }
    }

    private Object direction(BlockFace face) {
        return switch (face) {
            case DOWN -> useOn.down();
            case UP -> useOn.up();
            case NORTH -> useOn.north();
            case SOUTH -> useOn.south();
            case WEST -> useOn.west();
            case EAST -> useOn.east();
            default -> null;
        };
    }

    private boolean successful(Object result) {
        if (result == null) {
            return false;
        }
        Boolean consumesAction = invokeBoolean(result, "consumesAction");
        if (consumesAction != null) {
            return consumesAction;
        }
        Boolean success = invokeBoolean(result, "isSuccess");
        if (success != null) {
            return success;
        }

        String description = (result.getClass().getSimpleName() + ':' + result)
                .toUpperCase(Locale.ROOT);
        if (description.contains("PASS") || description.contains("FAIL")
                || description.contains("TRY_EMPTY_HAND")) {
            return false;
        }
        return description.contains("SUCCESS") || description.contains("CONSUME");
    }

    private Boolean invokeBoolean(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Boolean result ? result : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private void logOnce(String message, Throwable throwable) {
        if (failureLogged.compareAndSet(false, true)) {
            logger.log(Level.WARNING, message, throwable);
        }
    }
}
