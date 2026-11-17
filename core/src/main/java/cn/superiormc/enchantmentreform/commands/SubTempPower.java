package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.managers.TempPowerManager;
import cn.superiormc.enchantmentreform.objects.ObjectTempPower;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SubTempPower extends AbstractCommand {

    public SubTempPower() {
        this.id = "temppower";
        this.requiredPermission = "enchantmentreform.temppower";
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{3, 4, 5};
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
        String operation = args[1].toLowerCase(Locale.ROOT);
        boolean valid = switch (operation) {
            case "set" -> args.length == 4 || args.length == 5;
            case "remove" -> args.length == 4;
            case "clear", "list" -> args.length == 3;
            default -> false;
        };
        if (!valid) {
            LanguageManager.languageManager.sendStringText(sender, "temp-power.usage");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", args[2]);
            return;
        }
        if (EnchantmentReform.isFolia) {
            SchedulerUtil.runSync(target, () -> executeForPlayer(args, operation, sender, target));
        } else {
            executeForPlayer(args, operation, sender, target);
        }
    }

    private void executeForPlayer(String[] args, String operation, CommandSender sender, Player target) {
        if (!target.isOnline()) {
            LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", args[2]);
            return;
        }
        if (operation.equals("list")) {
            List<ActivePowerSource> activePowers = TempPowerManager.tempPowerManager.getActiveTempPowers(target);
            LanguageManager.languageManager.sendStringText(sender, "temp-power.list-header",
                    "player", target.getName(), "amount", String.valueOf(activePowers.size()));
            for (ActivePowerSource active : activePowers) {
                ObjectTempPower definition = (ObjectTempPower) active.source();
                LanguageManager.languageManager.sendStringText(sender, "temp-power.list-entry",
                        "power", definition.getId(), "level", String.valueOf(active.level()),
                        "remember", String.valueOf(definition.isRemembered()));
            }
            return;
        }
        if (operation.equals("clear")) {
            int cleared = TempPowerManager.tempPowerManager.clearTempPowers(target);
            LanguageManager.languageManager.sendStringText(sender, "temp-power.cleared",
                    "player", target.getName(), "amount", String.valueOf(cleared));
            return;
        }
        ObjectTempPower definition = TempPowerManager.tempPowerManager.getTempPower(args[3]);
        if (definition == null) {
            LanguageManager.languageManager.sendStringText(sender, "temp-power.not-found", "power", args[3]);
            return;
        }
        if (operation.equals("remove")) {
            TempPowerManager.tempPowerManager.removeTempPower(target, definition);
            LanguageManager.languageManager.sendStringText(sender, "temp-power.removed",
                    "player", target.getName(), "power", definition.getId());
            return;
        }
        int level = definition.getDefaultLevel();
        if (args.length == 5) {
            try {
                level = Integer.parseInt(args[4]);
            } catch (NumberFormatException exception) {
                LanguageManager.languageManager.sendStringText(sender, "temp-power.invalid-level", "value", args[4]);
                return;
            }
            if (level <= 0) {
                LanguageManager.languageManager.sendStringText(sender, "temp-power.invalid-level", "value", args[4]);
                return;
            }
        }
        TempPowerManager.tempPowerManager.setTempPowerLevel(target, definition, level);
        LanguageManager.languageManager.sendStringText(sender, "temp-power.set",
                "player", target.getName(), "power", definition.getId(), "level", String.valueOf(level));
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        return suggestions(args);
    }

    @Override
    protected List<String> getConsoleTabResult(String[] args) {
        return suggestions(args);
    }

    private List<String> suggestions(String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            result.addAll(List.of("set", "remove", "clear", "list"));
        } else if (args.length == 3) {
            Bukkit.getOnlinePlayers().forEach(player -> result.add(player.getName()));
        } else if (args.length == 4 && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("remove"))) {
            TempPowerManager.tempPowerManager.getTempPowers().forEach(power -> result.add(power.getId()));
        } else if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
            ObjectTempPower definition = TempPowerManager.tempPowerManager.getTempPower(args[3]);
            if (definition != null) {
                result.add(String.valueOf(definition.getDefaultLevel()));
            }
        }
        return result;
    }
}
