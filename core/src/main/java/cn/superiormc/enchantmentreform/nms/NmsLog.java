package cn.superiormc.enchantmentreform.nms;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NmsLog {
    public static final String BRIDGE = "Bridge";
    public static final String AQUATIC_ARROW = "Aquatic arrow";
    public static final String LOOT_ENCHANTING = "Loot enchanting";
    public static final String FOOD_USE = "Food use";
    public static final String FISHING_ROD = "Fishing rod";
    public static final String ITEM_USE = "Item use";
    public static final String ENCHANTMENT_REGISTRY = "Enchantment registry";

    private NmsLog() {
    }

    public static void info(Logger logger, String feature, String status) {
        log(logger, Level.INFO, feature, status, null);
    }

    public static void debug(Logger logger, String feature, String status) {
        log(logger, Level.FINE, feature, status, null);
    }

    public static void warning(Logger logger, String feature, String status) {
        log(logger, Level.WARNING, feature, status, null);
    }

    public static void warning(Logger logger, String feature, String status, Throwable cause) {
        log(logger, Level.WARNING, feature, status, cause);
    }

    public static void error(Logger logger, String feature, String status, Throwable cause) {
        log(logger, Level.SEVERE, feature, status, cause);
    }

    private static void log(Logger logger, Level level, String feature, String status, Throwable cause) {
        while (cause instanceof InvocationTargetException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = "[NMS] " + feature + ": " + status;
        if (cause == null) {
            logger.log(level, message);
        } else {
            logger.log(level, message, cause);
        }
    }
}
