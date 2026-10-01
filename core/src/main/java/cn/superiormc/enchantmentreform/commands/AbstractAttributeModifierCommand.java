package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttributeModifier;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class AbstractAttributeModifierCommand extends AbstractCommand {

    protected AbstractAttributeModifierCommand(String id) {
        this.id = id;
        this.requiredPermission = "enchantmentreform." + id;
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{6};
    }

    @Override
    public final void executeCommandInGame(String[] args, Player player) {
        execute(args, player);
    }

    @Override
    public final void executeCommandInConsole(String[] args) {
        execute(args, Bukkit.getConsoleSender());
    }

    protected abstract boolean replacesExistingModifier();

    protected abstract String successMessage();

    private void execute(String[] args, CommandSender sender) {
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

        double amount;
        try {
            amount = Double.parseDouble(args[4]);
            if (!Double.isFinite(amount)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.invalid-decimal", "value", args[4]);
            return;
        }

        ObjectCustomAttributeModifier modifier;
        try {
            modifier = new ObjectCustomAttributeModifier(
                    args[3], amount,
                    ObjectCustomAttributeModifier.Operation.parse(args[5]));
        } catch (IllegalArgumentException exception) {
            LanguageManager.languageManager.sendStringText(
                    sender, "error.attribute-modifier-invalid", "value", exception.getMessage());
            return;
        }

        if (replacesExistingModifier()) {
            attribute.setModifier(target, modifier);
        } else if (!attribute.addModifier(target, modifier)) {
            LanguageManager.languageManager.sendStringText(sender,
                    "error.attribute-modifier-exists", "modifier", modifier.id());
            return;
        }

        LanguageManager.languageManager.sendStringText(sender, successMessage(),
                "player", target.getName(),
                "attribute", attribute.getLocalizedName(target),
                "modifier", modifier.id(),
                "amount", String.valueOf(modifier.amount()),
                "operation", modifier.operation().name(),
                "value", String.valueOf(attribute.getValue(target)));
    }

    @Override
    protected final List<String> getTabResult(String[] args, Player player) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(online -> result.add(online.getName()));
        } else if (args.length == 3 && AttributeManager.attributeManager != null) {
            result.addAll(AttributeManager.attributeManager.getAttributeMap().keySet());
        } else if (args.length == 4) {
            result.add("[modifier-id]");
        } else if (args.length == 5) {
            result.add("[amount]");
        } else if (args.length == 6) {
            result.addAll(Arrays.stream(ObjectCustomAttributeModifier.Operation.values())
                    .map(Enum::name).toList());
        }
        return result;
    }
}
