package cn.superiormc.enchantmentreform.paper.registry;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.List;

public final class PaperDoubleTradePriceTagRegistrar {

    private static final String OPTION = "double-trade-multiplier";

    private PaperDoubleTradePriceTagRegistrar() {
    }

    public static void bind(BootstrapContext context,
                            Collection<ObjectCustomEnchantment> definitions,
                            ConfigurationSection raritySettings) {
        List<ObjectCustomEnchantment> snapshot = List.copyOf(definitions);
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
                    for (ObjectCustomEnchantment definition : snapshot) {
                        if (!definition.isEnabled() || !usesDoubleTradePrice(definition, raritySettings)) {
                            continue;
                        }
                        event.registrar().addToTag(
                                EnchantmentTagKeys.DOUBLE_TRADE_PRICE,
                                List.of(EnchantmentKeys.create(
                                        Key.key(definition.getKey().asString()))));
                    }
                }));
    }

    private static boolean usesDoubleTradePrice(ObjectCustomEnchantment definition,
                                                ConfigurationSection raritySettings) {
        ConfigurationSection enchantment = definition.getConfig();
        if (enchantment.contains(OPTION)) {
            return enchantment.getBoolean(OPTION, false);
        }

        String rarity = enchantment.getString("rarity", "COMMON");
        ConfigurationSection raritySection = findRaritySection(raritySettings, rarity);
        if (raritySection != null && raritySection.contains(OPTION)) {
            return raritySection.getBoolean(OPTION, false);
        }

        return "MYTHIC".equalsIgnoreCase(rarity);
    }

    private static ConfigurationSection findRaritySection(ConfigurationSection settings,
                                                            String rarity) {
        if (settings == null || rarity == null) {
            return null;
        }
        ConfigurationSection direct = settings.getConfigurationSection(rarity);
        if (direct != null) {
            return direct;
        }
        for (String key : settings.getKeys(false)) {
            if (key.equalsIgnoreCase(rarity)) {
                return settings.getConfigurationSection(key);
            }
        }
        return null;
    }
}
