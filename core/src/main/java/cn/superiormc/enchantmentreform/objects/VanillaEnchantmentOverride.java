package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class VanillaEnchantmentOverride extends PowerEnchantmentDefinition {

    public VanillaEnchantmentOverride(String fileName,
                                       YamlConfiguration config,
                                       YamlConfiguration registryLanguage,
                                       ConfigurationSection raritySettings) {
        super(fileName, config, registryLanguage, raritySettings, true);
    }
}
