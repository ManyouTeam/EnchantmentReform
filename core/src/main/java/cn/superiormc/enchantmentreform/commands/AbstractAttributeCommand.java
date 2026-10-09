package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractAttributeCommand extends AbstractCommand {

    private final boolean supportsIgnoreLimits;

    protected AbstractAttributeCommand(String id, boolean supportsIgnoreLimits) {
        this.id = id;
        this.supportsIgnoreLimits = supportsIgnoreLimits;
        this.requiredPermission = "enchantmentreform." + id;
        this.onlyInGame = false;
        this.requiredArgLength = supportsIgnoreLimits ? new Integer[]{4, 5} : new Integer[]{4};
    }

    @Override
    public final void executeCommandInGame(String[] args, Player player) {
        execute(args, player);
    }

    @Override
    public final void executeCommandInConsole(String[] args) {
        execute(args, Bukkit.getConsoleSender());
    }

    protected abstract int update(ObjectCustomAttribute attribute, Player player, int value,
                                  boolean ignoreLimits);

    protected abstract String successMessage();

    private void execute(String[] args, CommandSender sender) {
        boolean ignoreLimits = args.length == 5 && "-ignore".equalsIgnoreCase(args[4]);
        if (args.length == 5 && (!supportsIgnoreLimits || !ignoreLimits)) {
            LanguageManager.languageManager.sendStringText(sender, "error.args");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.player-not-found", "player", args[1]);
            return;
        }
        AttributeManager manager = AttributeManager.attributeManager;
        ObjectCustomAttribute attribute = manager == null
                ? null : manager.resolveAttribute(args[2]);
        if (attribute == null) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.attribute-not-found", "attribute", args[2]);
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-number", "value", args[3]);
            return;
        }
        int baseValue = update(attribute, target, value, ignoreLimits);
        LanguageManager.languageManager.sendStringText(sender, successMessage(),
                "player", target.getName(),
                "attribute", attribute.getLocalizedName(target),
                "value", String.valueOf(baseValue),
                "current", String.valueOf(attribute.getValue(target)));
    }

    @Override
    protected final List<String> getTabResult(String[] args, Player player) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(online -> result.add(online.getName()));
        } else if (args.length == 3 && AttributeManager.attributeManager != null) {
            result.addAll(AttributeManager.attributeManager.getAttributeMap().keySet());
        } else if (args.length == 4) {
            result.add("[value]");
        } else if (args.length == 5 && supportsIgnoreLimits) {
            result.add("-ignore");
        }
        return result;
    }
}
