package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public record EnchantmentRarity(String id,
                                String colorPrefix,
                                String colorSuffix,
                                String displayName,
                                ConfigurationSection settings) {

    public static EnchantmentRarity resolve(String configuredRarity,
                                            ConfigurationSection raritySettings) {
        String id = configuredRarity == null || configuredRarity.isBlank()
                ? "COMMON"
                : configuredRarity.strip().toUpperCase(Locale.ROOT).replace('-', '_');
        ConfigurationSection section = findSection(raritySettings, id);
        String prefix = section == null
                ? ""
                : section.getString("color-prefix", section.getString("color", ""));
        String suffix = section == null ? "" : section.getString("color-suffix", "");
        return new EnchantmentRarity(
                id,
                prefix == null ? "" : prefix,
                suffix == null ? "" : suffix,
                section == null ? null : section.getString("display-name"),
                section);
    }

    public int inheritedInt(String path, int defaultValue) {
        return settings == null ? defaultValue : settings.getInt(path, defaultValue);
    }

    private static ConfigurationSection findSection(ConfigurationSection settings, String rarity) {
        if (settings == null) {
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
