package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainCommandTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return suggest(sender, args);
    }

    public List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length <= 1) {
            for (AbstractCommand object : CommandManager.commandManager.getSubCommandsMap().values()) {
                if (object.getRequiredPermission() == null || object.getRequiredPermission().isEmpty()
                        || sender.hasPermission(object.getRequiredPermission())) result.add(object.getId());
            }
            return result;
        }
        AbstractCommand object = CommandManager.commandManager.getSubCommandsMap()
                .get(args[0].toLowerCase(Locale.ROOT));
        if (object != null && (object.getRequiredPermission() == null
                || object.getRequiredPermission().isEmpty()
                || sender.hasPermission(object.getRequiredPermission()))) {
            if (sender instanceof Player player) {
                return object.filterTabResult(args, player);
            }
            if (!object.getOnlyInGame()) {
                return object.filterConsoleTabResult(args);
            }
        }
        return result;
    }
}
