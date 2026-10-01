package cn.superiormc.enchantmentreform.listeners;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.managers.TempBlockManager;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.VanillaEnchantmentOverride;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class LocationChangedEffectTempBlockListener implements Listener {

    private static final String ENABLED_PATH =
            "powers.location-changed-effect.auto-remove.enabled";

    private static final String DURATION_PATH =
            "powers.location-changed-effect.auto-remove.duration";

    private static final int DEFAULT_DURATION = 100;

    private final Map<String, PowerEnchantmentDefinition> trackedEnchantments;

    public LocationChangedEffectTempBlockListener() {
        trackedEnchantments = findLocationChangedEnchantments();
        if (!trackedEnchantments.isEmpty()) {
            EnchantmentReform.instance.getLogger().info(
                    "Location-changed effect auto-remove is tracking "
                            + trackedEnchantments.size() + " enchantment(s).");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        ConfigManager config = ConfigManager.configManager;
        TempBlockManager manager = TempBlockManager.tempBlockManager;
        if (config == null || manager == null || trackedEnchantments.isEmpty()
                || !config.getBoolean(ENABLED_PATH, true)
                || !(event.getEntity() instanceof LivingEntity living)
                || !hasTrackedEnchantment(living)) {
            return;
        }

        BlockData originalData = event.getBlock().getBlockData().clone();
        BlockData temporaryData = event.getNewState().getBlockData().clone();
        int duration = randomDuration(config.getString(
                DURATION_PATH, String.valueOf(DEFAULT_DURATION)));
        manager.trackFormedBlock(
                event.getBlock().getLocation(), originalData, temporaryData, duration);
    }

    private static int randomDuration(String configured) {
        if (configured == null || configured.isBlank()) {
            return DEFAULT_DURATION;
        }
        String value = configured.trim();
        int separator = value.indexOf('~');
        try {
            if (separator > 0 && separator < value.length() - 1) {
                int first = Math.max(1, Integer.parseInt(
                        value.substring(0, separator).trim()));
                int second = Math.max(1, Integer.parseInt(
                        value.substring(separator + 1).trim()));
                int minimum = Math.min(first, second);
                int maximum = Math.max(first, second);
                return (int) ThreadLocalRandom.current().nextLong(
                        minimum, (long) maximum + 1L);
            }
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return DEFAULT_DURATION;
        }
    }

    private Map<String, PowerEnchantmentDefinition> findLocationChangedEnchantments() {
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        if (manager == null) {
            return Map.of();
        }
        Map<String, PowerEnchantmentDefinition> result = new LinkedHashMap<>();
        manager.getEnchantments().stream()
                .filter(enchantment -> enchantment.hasNativeEffect("minecraft:location_changed"))
                .forEach(enchantment -> result.put(normalizeKey(
                        enchantment.getKey().asString()), enchantment));
        manager.getVanillaEnchantments().stream()
                .filter(VanillaEnchantmentOverride::isEnabled)
                .filter(enchantment -> enchantment.hasNativeEffect("minecraft:location_changed"))
                .forEach(enchantment -> result.put(normalizeKey(
                        enchantment.getKey().asString()), enchantment));
        return Map.copyOf(result);
    }

    private boolean hasTrackedEnchantment(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return false;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item;
            try {
                item = equipment.getItem(slot);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (item == null || item.getType().isAir()) {
                continue;
            }
            for (org.bukkit.enchantments.Enchantment enchantment
                    : item.getEnchantments().keySet()) {
                PowerEnchantmentDefinition definition = trackedEnchantments.get(
                        normalizeKey(enchantment.getKey().toString()));
                if (definition != null && definition.isActiveOn(slot)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeKey(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
