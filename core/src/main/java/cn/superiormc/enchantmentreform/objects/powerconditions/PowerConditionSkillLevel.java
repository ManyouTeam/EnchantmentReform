package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import org.bukkit.entity.Player;

/** Compares a player's current level in a configured skill. */
public final class PowerConditionSkillLevel extends AbstractNumericPowerCondition {

    public PowerConditionSkillLevel() {
        super("skill_level");
    }

    @Override
    protected boolean alwaysMatches(ObjectSingleCondition condition) {
        return ConfigManager.configManager != null
                && !ConfigManager.configManager.getBoolean("modules.skills", true);
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Player player = condition.player();
        if (player == null || SkillManager.skillManager == null) {
            return null;
        }
        String id = condition.getString("skill", condition.getString("id", ""));
        SkillDefinition skill = SkillManager.skillManager.getSkill(id);
        return skill == null ? null : (double) SkillManager.skillManager.getLevel(player, skill);
    }
}
