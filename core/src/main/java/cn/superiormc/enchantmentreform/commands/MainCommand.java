package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.CommandManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        return execute(sender, args);
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            LanguageManager.languageManager.sendStringText(sender, "error.args");
            return true;
        }
        AbstractCommand object = CommandManager.commandManager.getSubCommandsMap()
                .get(args[0].toLowerCase(Locale.ROOT));
        if (object == null) {
            LanguageManager.languageManager.sendStringText(sender, "error.args");
            return true;
        }
        if (object.getOnlyInGame() && !(sender instanceof Player)) {
            LanguageManager.languageManager.sendStringText(sender, "error.in-game");
            return true;
        }
        if (object.getRequiredPermission() != null && !object.getRequiredPermission().isEmpty()
                && !sender.hasPermission(object.getRequiredPermission())) {
            LanguageManager.languageManager.sendStringText(sender, "error.miss-permission");
            return true;
        }
        if (!object.getLengthCorrect(args.length, sender)) {
            LanguageManager.languageManager.sendStringText(sender, "error.args");
            return true;
        }
        if (sender instanceof Player player) {
            object.executeCommandInGame(args, player);
        }
        else {
            object.executeCommandInConsole(args);
        }
        return true;
    }
}
