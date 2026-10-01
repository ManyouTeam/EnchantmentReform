package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.objects.skills.SkillSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class SkillExperienceAbility extends AbstractAbility {

    public SkillExperienceAbility(ConfigurationSection section) {
        super("SkillExperience", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof Player player) || SkillManager.skillManager == null) {
            return false;
        }
        SkillDefinition skill = SkillManager.skillManager.getSkill(
                section.getString("skill", ""));
        if (skill == null) {
            return false;
        }
        String sourceId = section.getString("source", "").strip();
        if (!sourceId.isEmpty()) {
            SkillSource source = skill.sources().stream()
                    .filter(candidate -> candidate.id().equalsIgnoreCase(sourceId))
                    .findFirst().orElse(null);
            double sourceAmount = getDouble("amount", 1.0D, context);
            if (Double.isFinite(sourceAmount) && sourceAmount > 0.0D) {
                SkillManager.skillManager.grantManualSourceExperience(
                        player, skill, source, context, sourceAmount);
            }
            return false;
        }
        double amount = getDouble("amount", 0.0D, context);
        if (Double.isFinite(amount) && amount > 0.0D) {
            SkillManager.skillManager.addExperience(player, skill, amount);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
