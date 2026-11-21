package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.NmsLog;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public final class NamedNmsBootstrap {

    private NamedNmsBootstrap() {
    }

    public static NmsBridge create(Logger logger) {
        // Spigot 26.2 no longer exposes Bukkit#getMinecraftVersion(). Keep the
        // bootstrap linked only against the long-standing Bukkit API instead.
        String version = Bukkit.getBukkitVersion().split("-", 2)[0];
        installLootEnchantFunctionPatch(logger);
        ArrowWaterResistancePatch.install(logger);
        NamedNmsResolver resolver = new NamedNmsResolver();
        NamedNmsHandles.Common common;
        try {
            common = resolver.resolveCommon();
        } catch (Throwable throwable) {
            NmsLog.warning(logger, NmsLog.BRIDGE, "unavailable (Minecraft " + version
                    + "); compatibility check failed.", throwable);
            return new UnsupportedNmsBridge("Named common ABI probe failed: " + throwable.getClass().getSimpleName());
        }

        FoodNmsOperation food = null;
        FishingNmsOperation fishing = null;
        UseOnNmsOperation useOn = null;
        try {
            food = new FoodNmsOperation(common, resolver.resolveFood(), logger);
        } catch (Throwable throwable) {
            NmsLog.warning(logger, NmsLog.FOOD_USE, "unavailable; compatibility check failed.", throwable);
        }
        try {
            fishing = new FishingNmsOperation(common, resolver.resolveFishing(), logger);
        } catch (Throwable throwable) {
            NmsLog.warning(logger, NmsLog.FISHING_ROD, "unavailable; compatibility check failed.", throwable);
        }
        try {
            useOn = new UseOnNmsOperation(common, resolver.resolveUseOn(), logger);
        } catch (Throwable throwable) {
            NmsLog.warning(logger, NmsLog.ITEM_USE, "unavailable; compatibility check failed.", throwable);
        }

        NamedNmsBridge bridge = new NamedNmsBridge(food, fishing, useOn);
        NmsLog.info(logger, NmsLog.BRIDGE, "enabled (Minecraft " + version + "; capabilities: "
                + bridge.capabilities().stream().map(Enum::name).sorted().toList() + ").");
        return bridge;
    }

    private static void installLootEnchantFunctionPatch(Logger logger) {
        ConfigManager config = ConfigManager.configManager;
        if (!config.getBoolean("enchant-randomly-overrides.enabled", false)) {
            return;
        }

        int minimumCost = config.getInt("enchant-randomly-overrides.minimum-cost", 10);
        int maximumCost = config.getInt("enchant-randomly-overrides.maximum-cost", 30);
        try {
            LootEnchantFunctionPatch.install(logger, minimumCost, maximumCost);
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§c[NMS] Loot enchanting: patch initialization failed. " + throwable);
        }
    }

    public static void uninstallPatches(Logger logger) {
        try {
            ArrowWaterResistancePatch.uninstall(logger);
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§c[NMS] Aquatic arrow: restoration failed. " + throwable);
        }

        try {
            LootEnchantFunctionPatch.uninstall(logger);
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§c[NMS] Loot enchanting: restoration failed. " + throwable);
        }
    }
}
