package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SubSetSkillMultiplier extends AbstractCommand {

    public SubSetSkillMultiplier() {
        this.id = "setskillmultiplier";
        this.requiredPermission = "enchantmentreform.setskillmultiplier";
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{4};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        execute(args, player);
    }

    @Override
    public void executeCommandInConsole(String[] args) {
        execute(args, Bukkit.getConsoleSender());
    }

    private void execute(String[] args, CommandSender sender) {
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.player-not-found", "player", args[1]);
            return;
        }
        SkillManager manager = SkillManager.skillManager;
        if (manager == null || !manager.isEnabled()) {
            LanguageManager.languageManager.sendStringText(sender, "error.skills-disabled");
            return;
        }
        boolean all = args[2].equalsIgnoreCase("all") || args[2].equals("*");
        SkillDefinition skill = all ? null : manager.getSkill(args[2]);
        if (!all && skill == null) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.skill-not-found", "skill", args[2]);
            return;
        }
        double multiplier;
        try {
            multiplier = Double.parseDouble(args[3]);
        } catch (NumberFormatException exception) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-decimal", "value", args[3]);
            return;
        }
        if (!Double.isFinite(multiplier) || multiplier < 0.0D) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-multiplier", "value", args[3]);
            return;
        }
        manager.setExperienceMultiplier(target, skill, multiplier);
        LanguageManager.languageManager.sendStringText(sender, "skill.multiplier-set",
                "player", target.getName(),
                "skill", all ? "all" : skill.name(target),
                "multiplier", String.valueOf(multiplier));
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(online -> result.add(online.getName()));
        } else if (args.length == 3) {
            result.add("all");
            if (SkillManager.skillManager != null) {
                SkillManager.skillManager.getSkills().forEach(skill -> result.add(skill.id()));
            }
        } else if (args.length == 4) {
            result.add("1.0");
        }
        return result;
    }
}
