package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Method;
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
        ConfigurationSection section = CommonUtil.findSectionIgnoreCase(raritySettings, id);
        if (section == null) {
            id = "COMMON";
            section = CommonUtil.findSectionIgnoreCase(raritySettings, id);
        }
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

    public static String idOf(PowerEnchantmentDefinition definition, Enchantment enchantment) {
        if (definition != null) {
            return definition.getRarity();
        }
        if (enchantment.isCursed()) {
            return "CURSE";
        }
        try {
            Method method = enchantment.getClass().getMethod("getRarity");
            method.setAccessible(true);
            Object paperRarity = method.invoke(enchantment);
            if (paperRarity != null) {
                String value = paperRarity.toString().toUpperCase(Locale.ROOT);
                return value.equals("VERY_RARE") ? "LEGENDARY" : value;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return enchantment.isTreasure() ? "RARE" : "COMMON";
    }

    public int inheritedInt(String path, int defaultValue) {
        return settings == null ? defaultValue : settings.getInt(path, defaultValue);
    }


}
