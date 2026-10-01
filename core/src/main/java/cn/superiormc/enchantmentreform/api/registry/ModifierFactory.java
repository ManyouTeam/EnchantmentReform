package cn.superiormc.enchantmentreform.api.registry;

import cn.superiormc.enchantmentreform.objects.powermodifiers.AbstractPowerModifier;
import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface ModifierFactory {

    AbstractPowerModifier create(ConfigurationSection section);
}
