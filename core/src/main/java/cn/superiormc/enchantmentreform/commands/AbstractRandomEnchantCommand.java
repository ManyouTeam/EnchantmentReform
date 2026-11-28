package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.utils.ItemUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class AbstractRandomEnchantCommand extends AbstractCommand {

    protected AbstractRandomEnchantCommand(String id) {
        this.id = id;
        this.requiredPermission = "enchantmentreform." + id;
        this.requiredArgLength = new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10};
        this.requiredConsoleArgLength = new Integer[]{3, 4, 5, 6, 7, 8, 9, 10};
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
        Player player = sender instanceof Player inGame ? inGame : null;
        String firstArg = args[1];
        boolean self = player != null && (firstArg.equalsIgnoreCase("all")
                || firstArg.equalsIgnoreCase("level") || firstArg.equalsIgnoreCase("levels"));
        int modeIndex = self ? 1 : 2;
        String mode = args.length > modeIndex ? args[modeIndex] : "";
        boolean levels = mode.equalsIgnoreCase("level") || mode.equalsIgnoreCase("levels");
        if (!levels && !mode.equalsIgnoreCase("all")) {
            LanguageManager.languageManager.sendStringText(sender, "error.args");
            return;
        }
        int next = modeIndex + 1;
        int power = 0;
        if (levels) {
            if (args.length == next || args[next].startsWith("--")) {
                LanguageManager.languageManager.sendStringText(sender, "error.args");
                return;
            }
            String value = args[next];
            Integer parsedPower = Ints.tryParse(value.startsWith("+") ? value.substring(1) : value);
            if (parsedPower == null || parsedPower <= 0) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-power", "value", args[next]);
                return;
            }
            power = parsedPower;
            next++;
        }
        int amount = 1;
        if (next < args.length && !args[next].startsWith("--")) {
            String value = args[next];
            Integer parsedAmount = Ints.tryParse(value.startsWith("+") ? value.substring(1) : value);
            if (parsedAmount == null || parsedAmount <= 0 || parsedAmount > 64) {
                LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-amount", "value", args[next]);
                return;
            }
            amount = parsedAmount;
            next++;
        }
        int minimumLevel = 0;
        int maximumLevel = Integer.MAX_VALUE;
        String rarity = null;
        Set<Material> targetItems = Set.of();
        for (; next < args.length; next += 2) {
            if (next + 1 >= args.length || args[next + 1].startsWith("--")) {
                LanguageManager.languageManager.sendStringText(sender, "error.args");
                return;
            }
            String value = args[next + 1];
            switch (args[next].toLowerCase(Locale.ROOT)) {
                case "--level" -> {
                    if (levels || minimumLevel > 0) {
                        LanguageManager.languageManager.sendStringText(sender, "error.args");
                        return;
                    }
                    String[] bounds = value.split("~", 2);
                    String lower = bounds[0];
                    Integer minimum = Ints.tryParse(lower.startsWith("+") ? lower.substring(1) : lower);
                    String upper = bounds[bounds.length - 1];
                    Integer maximum = Ints.tryParse(upper.startsWith("+") ? upper.substring(1) : upper);
                    if (minimum == null || maximum == null || minimum <= 0 || maximum < minimum) {
                        LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-level", "value", value);
                        return;
                    }
                    minimumLevel = minimum;
                    maximumLevel = maximum;
                }
                case "--target" -> {
                    if (!id.equals("randombook") || !targetItems.isEmpty()) {
                        LanguageManager.languageManager.sendStringText(sender, "error.args");
                        return;
                    }
                    Map<String, ConfigurationSection> definitions = ConfigManager.configManager.getSupportedItemTags();
                    String tag = ItemUtil.normalizeItemTag(value);
                    if (!value.startsWith("#") && definitions.containsKey(tag)) {
                        targetItems = ItemUtil.resolveItemTag(tag, definitions, new LinkedHashSet<>());
                    }
                    if (targetItems.isEmpty()) {
                        LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-target", "value", value);
                        return;
                    }
                }
                case "--rarity" -> {
                    if (next + 2 != args.length) {
                        LanguageManager.languageManager.sendStringText(sender, "error.args");
                        return;
                    }
                    rarity = value.toLowerCase(Locale.ROOT);
                    if (!ConfigManager.configManager.getRarityNames().contains(rarity)) {
                        LanguageManager.languageManager.sendStringText(sender, "random-book.invalid-rarity", "value", value);
                        return;
                    }
                }
                default -> {
                    LanguageManager.languageManager.sendStringText(sender, "error.args");
                    return;
                }
            }
        }
        Player target = modeIndex == 1 ? player : Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", args[1]);
            return;
        }
        int selectedPower = power;
        int selectedAmount = amount;
        int selectedMinimum = minimumLevel;
        int selectedMaximum = maximumLevel;
        String selectedRarity = rarity;
        Set<Material> selectedItems = targetItems;
        SchedulerUtil.runSync(target, () -> {
            if (!target.isOnline()) {
                LanguageManager.languageManager.sendStringText(sender, "error.player-not-found", "player", target.getName());
                return;
            }
            applyRolls(sender, target, selectedPower, selectedAmount, selectedRarity, selectedMinimum, selectedMaximum, selectedItems);
        });
    }

    protected abstract void applyRolls(CommandSender sender, Player target, int power, int amount,
                                       String rarity, int minimumLevel, int maximumLevel, Set<Material> targetItems);

    @Override
    public List<String> filterTabResult(String[] args, Player player) {
        return suggest(args, player);
    }

    @Override
    public List<String> filterConsoleTabResult(String[] args) {
        return suggest(args, null);
    }

    private List<String> suggest(String[] args, Player player) {
        int current = args.length - 1;
        String input = args[current].toLowerCase(Locale.ROOT);
        List<String> modes = List.of("all", "levels");
        List<String> suggestions = new ArrayList<>();
        collectSuggestions: {
            if (args.length == 2) {
                Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                if (player != null) {
                    suggestions.addAll(modes);
                }
                break collectSuggestions;
            }

            String firstArg = args[1];
            boolean self = player != null && (firstArg.equalsIgnoreCase("all")
                    || firstArg.equalsIgnoreCase("level") || firstArg.equalsIgnoreCase("levels"));
            int modeIndex = self ? 1 : 2;
            if (current == modeIndex) {
                suggestions.addAll(modes);
                break collectSuggestions;
            }

            String mode = args[modeIndex];
            boolean levels = mode.equalsIgnoreCase("level") || mode.equalsIgnoreCase("levels");
            if (!levels && !mode.equalsIgnoreCase("all")) {
                return List.of();
            }
            if (levels && current == modeIndex + 1) {
                suggestions.addAll(List.of("1", "10", "20", "30", "50", "100"));
                break collectSuggestions;
            }

            int next = modeIndex + (levels ? 2 : 1);
            if (id.equals("randombook")) {
                suggestions.add("--target");
            }
            suggestions.add("--rarity");
            if (!levels) {
                suggestions.add("--level");
            }
            if (current == next) {
                suggestions.addAll(List.of("1", "8", "16", "32", "64"));
                break collectSuggestions;
            }
            if (!args[next].startsWith("--")) {
                next++;
            }

            for (; next < current; next += 2) {
                String option = args[next].toLowerCase(Locale.ROOT);
                if (!suggestions.remove(option)) {
                    return List.of();
                }
                if (next + 1 != current) {
                    if (option.equals("--rarity")) {
                        return List.of();
                    }
                    continue;
                }
                suggestions.clear();
                switch (option) {
                    case "--level" -> {
                        suggestions.addAll(List.of("1", "2", "3", "4", "5", "5~10"));
                    }
                    case "--rarity" -> {
                        suggestions.addAll(ConfigManager.configManager.getRarityNames());
                    }
                    case "--target" -> {
                        if (input.startsWith("#")) {
                            return List.of();
                        }
                        String tagPrefix = ItemUtil.normalizeItemTag(input);
                        return ConfigManager.configManager.getSupportedItemTags().keySet().stream()
                                .filter(tag -> tag.startsWith(input) || tag.startsWith(tagPrefix))
                                .toList();
                    }
                }
                break;
            }
        }
        suggestions.removeIf(value -> !value.toLowerCase(Locale.ROOT).startsWith(input));
        return suggestions;
    }
}
