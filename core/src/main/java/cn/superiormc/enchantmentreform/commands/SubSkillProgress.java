package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class SubSkillProgress extends AbstractCommand {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.##");

    private final Operation operation;

    public SubSkillProgress(Operation operation) {
        this.operation = operation;
        this.id = operation.id;
        this.requiredPermission = "enchantmentreform." + id;
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
        SkillDefinition skill = manager.getSkill(args[2]);
        if (skill == null) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.skill-not-found", "skill", args[2]);
            return;
        }
        if (operation.level) {
            updateLevel(args[3], sender, target, skill, manager);
        } else {
            updateExperience(args[3], sender, target, skill, manager);
        }
    }

    private void updateExperience(String raw, CommandSender sender, Player target,
                                  SkillDefinition skill, SkillManager manager) {
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException exception) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-decimal", "value", raw);
            return;
        }
        if (!Double.isFinite(amount) || amount < 0.0D) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-skill-experience", "value", raw);
            return;
        }
        if (operation.add) {
            manager.addExperienceDirect(target, skill, amount);
        } else {
            manager.setExperience(target, skill, amount);
        }
        sendResult(sender, target, skill, manager);
    }

    private void updateLevel(String raw, CommandSender sender, Player target,
                             SkillDefinition skill, SkillManager manager) {
        int amount;
        try {
            amount = Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-number", "value", raw);
            return;
        }
        if (!operation.add && (amount < 0 || amount > skill.maximumLevel())) {
            LanguageManager.languageManager.sendStringText(sender, "error.invalid-skill-level",
                    "value", raw, "maximum", String.valueOf(skill.maximumLevel()));
            return;
        }
        if (operation.add) {
            manager.addLevel(target, skill, amount);
        } else {
            manager.setLevel(target, skill, amount);
        }
        sendResult(sender, target, skill, manager);
    }

    private void sendResult(CommandSender sender, Player target, SkillDefinition skill,
                            SkillManager manager) {
        LanguageManager.languageManager.sendStringText(sender, "skill.progress-updated",
                "player", target.getName(),
                "skill", skill.name(target),
                "level", String.valueOf(manager.getLevel(target, skill)),
                "maximum_level", String.valueOf(skill.maximumLevel()),
                "experience", NUMBER.format(manager.getExperience(target, skill)),
                "required_experience", NUMBER.format(manager.getRequiredExperience(target, skill)));
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(online -> result.add(online.getName()));
        } else if (args.length == 3 && SkillManager.skillManager != null) {
            SkillManager.skillManager.getSkills().forEach(skill -> result.add(skill.id()));
        } else if (args.length == 4) {
            result.add(operation.level ? (operation.add ? "1" : "[level]") : "[experience]");
        }
        return result;
    }

    public enum Operation {
        SET_EXPERIENCE("setskillxp", false, false),
        ADD_EXPERIENCE("addskillxp", true, false),
        SET_LEVEL("setskilllevel", false, true),
        ADD_LEVEL("addskilllevel", true, true);

        private final String id;
        private final boolean add;
        private final boolean level;

        Operation(String id, boolean add, boolean level) {
            this.id = id;
            this.add = add;
            this.level = level;
        }
    }
}
