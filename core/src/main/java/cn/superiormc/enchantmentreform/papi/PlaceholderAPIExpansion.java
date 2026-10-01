package cn.superiormc.enchantmentreform.papi;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private static final String ATTRIBUTE_PREFIX = "attribute_";

    private final EnchantmentReform plugin;

    public PlaceholderAPIExpansion(EnchantmentReform plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "PQguanfang";
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "enchantmentreform";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null) {
            return null;
        }
        Player player = offlinePlayer.getPlayer();
        if (player == null || params == null) {
            return null;
        }
        if (params.equals("attribute_points")) {
            return SkillManager.skillManager == null ? null
                    : String.valueOf(SkillManager.skillManager.getAttributePoints(player));
        }
        String skillValue = skillPlaceholder(player, params);
        if (skillValue != null) {
            return skillValue;
        }
        if (!params.startsWith(ATTRIBUTE_PREFIX)) {
            return null;
        }
        AttributeManager manager = AttributeManager.attributeManager;
        if (manager == null) {
            return null;
        }
        ObjectCustomAttribute attribute = manager.getAttribute(
                params.substring(ATTRIBUTE_PREFIX.length()));
        return attribute == null ? null : String.valueOf(attribute.getValue(player));
    }

    private String skillPlaceholder(Player player, String params) {
        SkillManager manager = SkillManager.skillManager;
        if (manager == null || !params.startsWith("skill_")) {
            return null;
        }
        String remainder = params.substring("skill_".length());
        String field;
        if (remainder.startsWith("level_")) field = "level";
        else if (remainder.startsWith("xp_")) field = "xp";
        else if (remainder.startsWith("required_xp_")) field = "required_xp";
        else return null;
        String id = remainder.substring(field.length() + 1);
        SkillDefinition skill = manager.getSkill(id);
        if (skill == null) return null;
        return switch (field) {
            case "level" -> String.valueOf(manager.getLevel(player, skill));
            case "xp" -> String.valueOf(manager.getExperience(player, skill));
            default -> String.valueOf(manager.getRequiredExperience(player, skill));
        };
    }
}
