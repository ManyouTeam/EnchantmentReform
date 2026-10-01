package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MythicSkillAbility extends AbstractAbility {

    public MythicSkillAbility(ConfigurationSection section) {
        super("MythicSkill", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!CommonUtil.checkPluginLoad("MythicMobs")) {
            return false;
        }

        Entity caster = context.source();
        if (caster == null) {
            return false;
        }

        Entity target = getTargetEntity(context);
        if (target == null) {
            return false;
        }

        float power = (float) getDouble("power", 1.0, context);

        List<String> skills = getSkills();
        for (String skill : skills) {
            if (skill == null || skill.isBlank()) {
                continue;
            }
            Collection<Entity> tempVal1 = new HashSet<>();
            tempVal1.add(target);
            Collection<Location> tempVal2 = new HashSet<>();
            tempVal2.add(getLocation(context));
            //MythicBukkit.inst().getAPIHelper().castSkill(caster, skill, getLocation(context), power);
            MythicBukkit.inst().getAPIHelper().castSkill(caster, skill, target, getLocation(context), tempVal1, tempVal2, power);
        }
        return false;
    }

    private List<String> getSkills() {
        List<String> skills = new ArrayList<>();

        List<String> list = getStringList("skills");
        if (list != null && !list.isEmpty()) {
            for (String skill : list) {
                if (skill != null && !skill.isBlank()) {
                    skills.add(skill.trim());
                }
            }
        }

        if (!skills.isEmpty()) {
            return skills;
        }

        String single = getString("skill", "");
        if (single != null && !single.isBlank()) {
            skills.add(single.trim());
        }

        return skills;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
