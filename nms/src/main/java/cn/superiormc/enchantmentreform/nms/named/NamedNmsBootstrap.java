package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NamedNmsBootstrap {

    private NamedNmsBootstrap() {
    }

    public static NmsBridge create(Logger logger) {
        // Spigot 26.2 no longer exposes Bukkit#getMinecraftVersion(). Keep the
        // bootstrap linked only against the long-standing Bukkit API instead.
        String version = Bukkit.getBukkitVersion().split("-", 2)[0];
        NamedNmsResolver resolver = new NamedNmsResolver();
        NamedNmsHandles.Common common;
        try {
            common = resolver.resolveCommon();
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "Named NMS common ABI probe failed on Minecraft " + version
                    + "; NMS abilities are unavailable.", throwable);
            return new UnsupportedNmsBridge("Named common ABI probe failed: " + throwable.getClass().getSimpleName());
        }

        FoodNmsOperation food = null;
        FishingNmsOperation fishing = null;
        UseOnNmsOperation useOn = null;
        try {
            food = new FoodNmsOperation(common, resolver.resolveFood(), logger);
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "Named NMS finishUsingItem ABI is unavailable on Minecraft " + version + '.', throwable);
        }
        try {
            fishing = new FishingNmsOperation(common, resolver.resolveFishing(), logger);
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "Named NMS fishing ABI is unavailable on Minecraft " + version + '.', throwable);
        }
        try {
            useOn = new UseOnNmsOperation(common, resolver.resolveUseOn(), logger);
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "Named NMS ItemStack#useOn ABI is unavailable on Minecraft " + version + '.', throwable);
        }

        NamedNmsBridge bridge = new NamedNmsBridge(food, fishing, useOn);
        logger.info("Named NMS bridge for Minecraft " + version + " enabled capabilities: "
                + bridge.capabilities());
        return bridge;
    }
}
