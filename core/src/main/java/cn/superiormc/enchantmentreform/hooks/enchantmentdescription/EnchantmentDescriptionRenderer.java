package cn.superiormc.enchantmentreform.hooks.enchantmentdescription;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.EnchantmentOrderUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class EnchantmentDescriptionRenderer {

    public static final List<String> DEFAULT_FORMAT = List.of(
            "{name} {level-roman}",
            "&7{description}");

    private static final NamespacedKey GENERATED_LORE_KEY =
            new NamespacedKey(EnchantmentReform.instance, "mythicchanger_enchantment_description");

    private EnchantmentDescriptionRenderer() {
    }

    public static ItemStack apply(ItemStack item,
                                  Player player,
                                  Settings settings,
                                  boolean realChange,
                                  UnaryOperator<String> placeholderParser) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> currentLore = EnchantmentReform.methodUtil.getItemLore(meta);
        List<String> lore = currentLore == null
                ? new ArrayList<>()
                : new ArrayList<>(currentLore);
        if (realChange) {
            removePreviouslyGeneratedLore(meta, lore);
        }
        if (!settings.enabled()) {
            writeLore(item, meta, lore, List.of(), false, realChange, lore.size(), player);
            return item;
        }

        List<String> generated = buildDescriptionLore(
                item, player, settings, placeholderParser == null ? UnaryOperator.identity() : placeholderParser);
        int retainedLoreSize = lore.size();
        if (settings.first()) {
            lore.addAll(0, generated);
        } else {
            lore.addAll(generated);
        }
        writeLore(item, meta, lore, generated, settings.first(), realChange, retainedLoreSize, player);
        return item;
    }

    public static ItemStack applyItemDisplay(ItemStack item,
                                             Player player,
                                             DisplaySettings settings,
                                             UnaryOperator<String> placeholderParser) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> originalLore = EnchantmentReform.methodUtil.getItemLore(meta);
        List<String> lore = originalLore == null
                ? new ArrayList<>()
                : new ArrayList<>(originalLore);
        if (settings.removeLoreFirst() && !settings.lorePrefix().isEmpty()) {
            lore.removeIf(line -> line.contains(settings.lorePrefix()));
        }

        UnaryOperator<String> parser = placeholderParser == null
                ? UnaryOperator.identity()
                : placeholderParser;
        List<DescriptionEntry> entries = resolveEntries(item, true);
        List<String> enchantLore = buildItemDisplayEnchantLore(
                entries, player, settings, parser);
        boolean expandedEnchants = false;

        if (settings.autoParse() && !enchantLore.isEmpty()) {
            List<String> parsedLore = new ArrayList<>();
            for (String line : lore) {
                if (line.contains("{enchants}")) {
                    parsedLore.addAll(enchantLore);
                    expandedEnchants = true;
                } else {
                    parsedLore.add(parser.apply(CommonUtil.modifyString(
                            player,
                            line,
                            "enchant_amount", Integer.toString(entries.size()))));
                }
            }
            lore = parsedLore;
        }

        List<String> displayBlock = new ArrayList<>();
        for (String line : settings.displayValue()) {
            if (line.equals("{enchants}")) {
                for (String enchantLine : enchantLore) {
                    displayBlock.add(settings.lorePrefix() + enchantLine);
                }
                expandedEnchants |= !enchantLore.isEmpty();
                continue;
            }
            String rendered = CommonUtil.modifyString(
                    player,
                    line,
                    "enchant_amount", Integer.toString(entries.size()));
            displayBlock.add(settings.lorePrefix() + parser.apply(rendered));
        }
        if (settings.first()) {
            lore.addAll(0, displayBlock);
        } else {
            lore.addAll(displayBlock);
        }

        if (expandedEnchants) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            try {
                meta.addItemFlags(ItemFlag.valueOf("HIDE_STORED_ENCHANTS"));
            } catch (IllegalArgumentException ignored) {
                // Older Bukkit versions use HIDE_ENCHANTS for both kinds.
            }
        }
        EnchantmentReform.methodUtil.setItemLore(meta, lore.isEmpty() ? null : lore, player);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> buildDescriptionLore(ItemStack item,
                                                     Player player,
                                                     Settings settings,
                                                     UnaryOperator<String> placeholderParser) {
        List<DescriptionEntry> entries = resolveEntries(item, false);

        List<String> result = new ArrayList<>();
        for (int entryIndex = 0; entryIndex < entries.size(); entryIndex++) {
            if (entryIndex > 0) {
                result.addAll(settings.separator());
            }
            DescriptionEntry entry = entries.get(entryIndex);
            List<String> descriptions = TextUtil.wrapKeepColor(
                    entry.definition().getLocalizedDescription(player, entry.level()),
                    settings.wrapLength());
            for (String template : settings.format()) {
                if (template.contains("{description}")) {
                    for (String description : descriptions) {
                        result.add(renderLine(template, entry, description, player, placeholderParser));
                    }
                } else {
                    result.add(renderLine(template, entry, "", player, placeholderParser));
                }
            }
        }
        return result;
    }

    private static List<String> buildItemDisplayEnchantLore(
            List<DescriptionEntry> entries,
            Player player,
            DisplaySettings settings,
            UnaryOperator<String> placeholderParser) {
        List<String> result = new ArrayList<>();
        for (DescriptionEntry entry : entries) {
            List<String> descriptions = TextUtil.wrapKeepColor(
                    entry.definition().getLocalizedDescription(player, entry.level()),
                    defaultWrapLength());
            String level = settings.levelHideOne()
                    && entry.level() == 1
                    && entry.definition().getMaxLevel() == 1
                    ? ""
                    : Integer.toString(entry.level());
            String romanLevel = level.isEmpty() ? "" : toRoman(entry.level());
            if (settings.autoAddSpace()) {
                level = addLeadingSpace(level);
                romanLevel = addLeadingSpace(romanLevel);
            }
            String firstDescription = descriptions.isEmpty() ? "" : descriptions.get(0);
            result.add(renderItemDisplayLine(
                    settings.enchantFormat(), entry, level, romanLevel,
                    firstDescription, player, placeholderParser));
            for (String description : descriptions) {
                if (!settings.descriptionFormat().isEmpty()) {
                    result.add(renderItemDisplayLine(
                            settings.descriptionFormat(), entry, level, romanLevel,
                            description, player, placeholderParser));
                }
            }
        }
        return result;
    }

    private static String renderItemDisplayLine(String template,
                                                DescriptionEntry entry,
                                                String level,
                                                String romanLevel,
                                                String description,
                                                Player player,
                                                UnaryOperator<String> placeholderParser) {
        String replaced = template
                .replace("{enchant_name}", entry.definition().getLocalizedName(player))
                .replace("{enchant_raw_name}", entry.definition().getName())
                .replace("{enchant_level}", level)
                .replace("{enchant_level_roman}", romanLevel)
                .replace("{enchant_description}", description);
        return placeholderParser.apply(CommonUtil.parseLang(player, replaced));
    }

    private static String addLeadingSpace(String value) {
        return value.isEmpty() ? value : " " + value;
    }

    private static List<DescriptionEntry> resolveEntries(ItemStack item, boolean alwaysSort) {
        Map<Enchantment, Integer> enchantments = getEnchantments(item.getItemMeta());
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        if (manager == null || enchantments.isEmpty()) {
            return List.of();
        }
        List<DescriptionEntry> entries = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            PowerEnchantmentDefinition definition = manager.getDefinition(
                    entry.getKey().getKey().toString());
            if (definition == null || !definition.isEnabled()
                    || definition.getDescription() == null
                    || definition.getDescription().isBlank()) {
                continue;
            }
            entries.add(new DescriptionEntry(definition, entry.getValue()));
        }
        sort(entries, alwaysSort);
        return entries;
    }

    private static String renderLine(String template,
                                     DescriptionEntry entry,
                                     String description,
                                     Player player,
                                     UnaryOperator<String> placeholderParser) {
        String replaced = template
                .replace("{key}", entry.definition().getKey().asString())
                .replace("{name}", entry.definition().getLocalizedName(player))
                .replace("{level}", Integer.toString(entry.level()))
                .replace("{level-roman}", toRoman(entry.level()))
                .replace("{description}", description);
        return placeholderParser.apply(replaced);
    }

    private static void sort(List<DescriptionEntry> entries, boolean alwaysSort) {
        ConfigManager config = ConfigManager.configManager;
        ConfigurationSection tooltip = config == null ? null : config.getSection("tooltip-order");
        if (!alwaysSort && !EnchantmentOrderUtil.isEnabled(tooltip)) {
            return;
        }
        ConfigurationSection rarities = config == null ? null : config.getRarities();
        Comparator<PowerEnchantmentDefinition> comparator =
                EnchantmentOrderUtil.comparator(rarities, tooltip);
        entries.sort(Comparator.comparing(DescriptionEntry::definition, comparator));
    }

    private static Map<Enchantment, Integer> getEnchantments(ItemMeta meta) {
        if (meta instanceof EnchantmentStorageMeta storageMeta
                && !storageMeta.getStoredEnchants().isEmpty()) {
            return new LinkedHashMap<>(storageMeta.getStoredEnchants());
        }
        return new LinkedHashMap<>(meta.getEnchants());
    }

    private static void writeLore(ItemStack item,
                                  ItemMeta meta,
                                  List<String> lore,
                                  List<String> generated,
                                  boolean first,
                                  boolean realChange,
                                  int retainedLoreSize,
                                  Player player) {
        EnchantmentReform.methodUtil.setItemLore(meta, lore.isEmpty() ? null : lore, player);
        if (!realChange) {
            item.setItemMeta(meta);
            return;
        }
        if (generated.isEmpty()) {
            meta.getPersistentDataContainer().remove(GENERATED_LORE_KEY);
        } else {
            List<String> normalizedLore = EnchantmentReform.methodUtil.getItemLore(meta);
            int normalizedGeneratedSize = normalizedLore.size() - retainedLoreSize;
            int start = first ? 0 : retainedLoreSize;
            meta.getPersistentDataContainer().set(
                    GENERATED_LORE_KEY,
                    PersistentDataType.STRING,
                    encode(normalizedLore.subList(start, start + normalizedGeneratedSize)));
        }
        item.setItemMeta(meta);
    }

    private static void removePreviouslyGeneratedLore(ItemMeta meta, List<String> lore) {
        String encoded = meta.getPersistentDataContainer().get(
                GENERATED_LORE_KEY, PersistentDataType.STRING);
        if (encoded == null) {
            return;
        }
        List<String> previous = decode(encoded);
        int index = findSequence(lore, previous);
        if (index >= 0) {
            lore.subList(index, index + previous.size()).clear();
        }
        meta.getPersistentDataContainer().remove(GENERATED_LORE_KEY);
    }

    private static int findSequence(List<String> lore, List<String> expected) {
        if (expected.isEmpty() || expected.size() > lore.size()) {
            return -1;
        }
        for (int index = 0; index <= lore.size() - expected.size(); index++) {
            if (lore.subList(index, index + expected.size()).equals(expected)) {
                return index;
            }
        }
        return -1;
    }

    private static String encode(List<String> lines) {
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line.length()).append(':').append(line);
        }
        return result.toString();
    }

    private static List<String> decode(String encoded) {
        List<String> result = new ArrayList<>();
        int cursor = 0;
        try {
            while (cursor < encoded.length()) {
                int separator = encoded.indexOf(':', cursor);
                int length = Integer.parseInt(encoded.substring(cursor, separator));
                int start = separator + 1;
                result.add(encoded.substring(start, start + length));
                cursor = start + length;
            }
        } catch (RuntimeException ignored) {
            return List.of();
        }
        return result;
    }

    private static int defaultWrapLength() {
        return ConfigManager.configManager == null
                ? 30
                : ConfigManager.configManager.getInt("enchantment-description.wrap-length", 30);
    }

    private static String toRoman(int value) {
        if (value <= 0 || value > 3999) {
            return Integer.toString(value);
        }
        int remaining = value;
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            while (remaining >= values[index]) {
                result.append(numerals[index]);
                remaining -= values[index];
            }
        }
        return result.toString();
    }

    public record Settings(boolean enabled,
                           boolean first,
                           List<String> format,
                           List<String> separator,
                           int wrapLength) {

        public Settings {
            format = List.copyOf(format);
            separator = List.copyOf(separator);
        }

        public static Settings defaults(boolean enabled) {
            return new Settings(enabled, false, DEFAULT_FORMAT, List.of(""), defaultWrapLength());
        }

        public static Settings from(ConfigurationSection section, boolean defaultEnabled) {
            if (section == null) {
                return defaults(defaultEnabled);
            }
            boolean enabled = section.getBoolean("enabled", defaultEnabled);
            boolean first = "FIRST".equals(section.getString("position", "LAST")
                    .toUpperCase(Locale.ROOT));
            List<String> format = section.contains("format")
                    ? section.getStringList("format")
                    : DEFAULT_FORMAT;
            List<String> separator = section.contains("separator")
                    ? section.getStringList("separator")
                    : List.of("");
            int wrapLength = section.getInt("wrap-length", defaultWrapLength());
            return new Settings(enabled, first, format, separator, wrapLength);
        }
    }

    public record DisplaySettings(boolean enabled,
                                  boolean forceEnabled,
                                  String lorePrefix,
                                  boolean removeLoreFirst,
                                  boolean blackCreative,
                                  boolean first,
                                  List<String> displayValue,
                                  boolean autoParse,
                                  String enchantFormat,
                                  boolean autoAddSpace,
                                  boolean levelHideOne,
                                  String descriptionFormat) {

        public DisplaySettings {
            displayValue = List.copyOf(displayValue);
        }

        public static DisplaySettings defaults() {
            return new DisplaySettings(
                    true, false, "§y", true, true, false, List.of("{enchants}"), true,
                    "&a{enchant_name}{enchant_level_roman}", true, true,
                    "&7  {enchant_description}");
        }

        public static DisplaySettings from(ConfigurationSection section) {
            DisplaySettings defaults = defaults();
            if (section == null) {
                return defaults;
            }
            ConfigurationSection placeholder = section.getConfigurationSection("placeholder");
            ConfigurationSection enchants = placeholder == null
                    ? null
                    : placeholder.getConfigurationSection("enchants");
            ConfigurationSection description = enchants == null
                    ? null
                    : enchants.getConfigurationSection("description");
            List<String> displayValue = section.contains("display-value")
                    ? section.getStringList("display-value")
                    : defaults.displayValue();
            return new DisplaySettings(
                    section.getBoolean("enabled", defaults.enabled()),
                    section.getBoolean("force-enabled", defaults.forceEnabled()),
                    section.getString("lore-prefix", defaults.lorePrefix()),
                    section.getBoolean("remove-lore-first", defaults.removeLoreFirst()),
                    section.getBoolean("black-creative", defaults.blackCreative()),
                    section.getBoolean("at-first-or-last", defaults.first()),
                    displayValue,
                    placeholder == null
                            ? defaults.autoParse()
                            : placeholder.getBoolean("auto-parse", defaults.autoParse()),
                    enchants == null
                            ? defaults.enchantFormat()
                            : enchants.getString("format", defaults.enchantFormat()),
                    enchants == null
                            ? defaults.autoAddSpace()
                            : enchants.getBoolean("auto-add-space", defaults.autoAddSpace()),
                    enchants == null
                            ? defaults.levelHideOne()
                            : enchants.getBoolean("level-hide-one", defaults.levelHideOne()),
                    description == null
                            ? defaults.descriptionFormat()
                            : description.getString("format", defaults.descriptionFormat()));
        }
    }

    private record DescriptionEntry(PowerEnchantmentDefinition definition, int level) {
    }
}
