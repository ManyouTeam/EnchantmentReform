package cn.superiormc.enchantmentreform.api.registry;

import cn.superiormc.enchantmentreform.objects.abilities.AbstractAbility;
import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface AbilityFactory {

    AbstractAbility create(ConfigurationSection section);
}
