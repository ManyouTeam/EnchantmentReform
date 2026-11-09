package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.methods.EnchantsUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class SubRandomBook extends AbstractCommand {

    public SubRandomBook() {
        this.id = "randombook";
        this.requiredPermission = "enchantmentreform.randombook";
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
        boolean levels = args[2].equalsIgnoreCase("levels");
        if ((!levels && !args[2].equalsIgnoreCase("all"))
                || (levels && args.length < 4) || (!levels && args.length > 4)) {
            LanguageManager.languageManager.sendStringText(sender, "random-book.usage");
            return;
        }
        int power = 0;
        if (levels) {
            try {
                power = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-power", "value", args[3]);
                return;
            }
            if (power <= 0) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-power", "value", args[3]);
                return;
            }
        }
        int amount = 1;
        int amountIndex = levels ? 4 : 3;
        if (args.length > amountIndex) {
            try {
                amount = Integer.parseInt(args[amountIndex]);
            } catch (NumberFormatException exception) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-amount", "value", args[amountIndex]);
                return;
            }
            if (amount <= 0 || amount > 64) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-amount", "value", args[amountIndex]);
                return;
            }
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", args[1]);
            return;
        }
        int selectedPower = power;
        int selectedAmount = amount;
        SchedulerUtil.runSync(target, () -> giveBooks(sender, target, levels, selectedPower, selectedAmount));
    }

    private void giveBooks(CommandSender sender, Player target, boolean levels, int power, int amount) {
        if (!target.isOnline()) {
            LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", target.getName());
            return;
        }
        List<ItemStack> books = levels ? EnchantsUtil.enchantBooksWithLevels(power, amount)
                : EnchantsUtil.randomEnchantedBooks(amount);
        if (books == null) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Failed to generate random enchanted books.");
            LanguageManager.languageManager.sendStringText(sender, "random-book.failed");
            return;
        }
        if (books.isEmpty()) {
            LanguageManager.languageManager.sendStringText(sender, levels ? "random-book.no-result" : "random-book.no-enchantments",
                    "power", String.valueOf(power));
            return;
        }
        CommonUtil.giveOrDrop(target, books.toArray(ItemStack[]::new));
        LanguageManager.languageManager.sendStringText(sender, "random-book.given", "player", target.getName(), "amount", String.valueOf(amount));
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        return getConsoleTabResult(args);
    }

    @Override
    protected List<String> getConsoleTabResult(String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 3) {
            return List.of("levels", "all");
        }
        if (args.length == 4 && args[2].equalsIgnoreCase("levels")) {
            return List.of("1", "10", "20", "30", "50", "100");
        }
        if ((args.length == 4 && args[2].equalsIgnoreCase("all"))
                || (args.length == 5 && args[2].equalsIgnoreCase("levels"))) {
            return List.of("1", "8", "16", "32", "64");
        }
        return List.of();
    }
}
