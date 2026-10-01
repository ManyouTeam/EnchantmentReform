package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ObjectCustomEnchantment extends PowerEnchantmentDefinition {

    public ObjectCustomEnchantment(String fileName,
                                   YamlConfiguration config,
                                   YamlConfiguration registryLanguage,
                                   ConfigurationSection raritySettings) {
        super(fileName, config, registryLanguage, raritySettings, false);
    }

    public record EnchantmentCost(int base, int perLevel) {}
}
