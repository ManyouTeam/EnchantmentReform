package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.methods.EnchantsUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.ItemUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public final class SubGiveBook extends AbstractCommand {

    public SubGiveBook() {
        id = "givebook";
        requiredPermission = "enchantmentreform.givebook";
        requiredArgLength = new Integer[]{2, 3, 4};
        requiredConsoleArgLength = new Integer[]{3, 4};
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
        Player namedTarget = args.length > 2 ? Bukkit.getPlayerExact(args[1]) : null;
        boolean self = sender instanceof Player && (args.length == 2 || (args.length == 3
                && (args[2].matches("[+-]?[0-9]+") || (namedTarget == null && resolve(args[1]) != null))));
        Player target = self ? (Player) sender : namedTarget;
        if (target == null) {
            LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", args[1]);
            return;
        }
        int enchantmentIndex = self ? 1 : 2;
        String enchantmentId = args[enchantmentIndex];
        Enchantment enchantment = resolve(enchantmentId);
        if (enchantment == null) {
            LanguageManager.languageManager.sendStringText(sender, "give-book.unknown", "value", enchantmentId);
            return;
        }
        int level = enchantment.getMaxLevel();
        if (args.length > enchantmentIndex + 1) {
            String value = args[enchantmentIndex + 1];
            Integer requestedLevel = Ints.tryParse(value.startsWith("+") ? value.substring(1) : value);
            if (requestedLevel == null || requestedLevel < 1 || requestedLevel > 255) {
                LanguageManager.languageManager.sendStringText(sender, "give-book.invalid-level");
                return;
            }
            level = requestedLevel;
        }
        int selectedLevel = level;
        SchedulerUtil.runSync(target, () -> {
            if (!target.isOnline()) {
                LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", target.getName());
                return;
            }
            CommonUtil.giveOrDrop(target, ItemUtil.generateEnchantedBook(enchantment, selectedLevel));
            Player viewer = sender instanceof Player player ? player : null;
            LanguageManager.languageManager.sendStringText(sender, "give-book.given", "player", target.getName(),
                    "enchantment", EnchantsUtil.getEnchantmentName(enchantment, viewer), "level", String.valueOf(selectedLevel));
        });
    }

    private Enchantment resolve(String raw) {
        String id = raw.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(id);
        if (key == null) {
            return null;
        }
        Enchantment enchantment = Registry.ENCHANTMENT.get(key);
        if (enchantment == null && !id.contains(":")) {
            return Registry.ENCHANTMENT.get(new NamespacedKey("enchantmentreform", id));
        }
        return enchantment;
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        List<String> suggestions = new ArrayList<>(getConsoleTabResult(args));
        if (args.length == 2) {
            suggestions.addAll(enchantmentIds());
        } else if (args.length == 3) {
            suggestions.addAll(levels(args[1]));
        }
        return suggestions;
    }

    @Override
    protected List<String> getConsoleTabResult(String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 3 && Bukkit.getPlayerExact(args[1]) != null) {
            return enchantmentIds();
        }
        if (args.length == 4) {
            return levels(args[2]);
        }
        return List.of();
    }

    private List<String> enchantmentIds() {
        return Registry.ENCHANTMENT.stream().map(enchantment -> enchantment.getKey().toString()).sorted().toList();
    }

    private List<String> levels(String id) {
        Enchantment enchantment = resolve(id);
        if (enchantment == null) {
            return List.of();
        }
        return IntStream.rangeClosed(1, Math.min(255, enchantment.getMaxLevel())).mapToObj(String::valueOf).toList();
    }
}
