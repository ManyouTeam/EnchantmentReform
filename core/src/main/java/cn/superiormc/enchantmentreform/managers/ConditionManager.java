package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.conditions.*;
import org.bukkit.entity.Player;

import java.util.List;

public class ConditionManager<S extends AbstractConfiguredSection<C>, C> {

    private final KeyedRegistry<String, AbstractCheckCondition<S, C>> conditions = KeyedRegistry.stringTypes();

    public ConditionManager() {
        registerBuiltInConditions();
    }

    public void registerBuiltInConditions() {
        registerNewCondition("biome", new ConditionBiome<>());
        registerNewCondition("permission", new ConditionPermission<>());
        registerNewCondition("placeholder", new ConditionPlaceholder<>());
        registerNewCondition("world", new ConditionWorld<>());
        registerNewCondition("any", new ConditionAny<>(
                (section, parent, player, context) -> matchesAny(section, player, context)));
        registerNewCondition("not", new ConditionNot<>(
                (section, parent, player, context) -> matches(section, player, context)));
    }

    public void registerNewCondition(String conditionID, AbstractCheckCondition<S, C> condition) {
        conditions.register(normalize(conditionID), condition);
    }

    public boolean checkBoolean(S condition, Player player, C context) {
        if (condition == null || player == null) {
            return false;
        }
        AbstractCheckCondition<S, C> checkCondition = conditions.get(normalize(condition.getString("type")));
        return checkCondition != null && checkCondition.checkCondition(condition, player, context);
    }

    public boolean checkBoolean(S condition, Player player) {
        return checkBoolean(condition, player, null);
    }

    /** Evaluates all named normal conditions in a section. */
    public boolean matches(org.bukkit.configuration.ConfigurationSection section, Player player, C context) {
        if (section == null) {
            return true;
        }
        for (String key : section.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection single = section.getConfigurationSection(key);
            if (single == null || !single.contains("type")) {
                return false;
            }
            @SuppressWarnings("unchecked")
            S condition = (S) new cn.superiormc.enchantmentreform.objects.ObjectSingleCondition(
                    single, powerContext(context, player), player);
            if (!checkBoolean(condition, player, context)) {
                return false;
            }
        }
        return true;
    }

    /** Evaluates whether any named normal condition in a section matches. */
    public boolean matchesAny(org.bukkit.configuration.ConfigurationSection section, Player player, C context) {
        if (section == null) {
            return true;
        }
        for (String key : section.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection single = section.getConfigurationSection(key);
            if (single == null || !single.contains("type")) {
                continue;
            }
            @SuppressWarnings("unchecked")
            S condition = (S) new cn.superiormc.enchantmentreform.objects.ObjectSingleCondition(
                    single, powerContext(context, player), player);
            if (checkBoolean(condition, player, context)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getConditionTypes() {
        return conditions.keys();
    }

    public boolean hasConditionType(String type) {
        return conditions.get(normalize(type)) != null;
    }

    private String normalize(String type) {
        return type == null ? "" : type.toLowerCase().replace('-', '_');
    }

    private PowerContext powerContext(C context, Player player) {
        return context instanceof PowerContext powerContext ? powerContext.withPlayer(player) : null;
    }

}
