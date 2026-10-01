package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageManager extends AbstractManager {

    public static LanguageManager languageManager;

    private static final String[] BUNDLED_LANGUAGES = {"en_US", "zh_CN"};

    private String serverLanguage = null;

    private final Map<String, YamlConfiguration> languageFiles = new HashMap<>();

    private YamlConfiguration tempMessageFile;

    public LanguageManager() {
        languageManager = this;
        initLanguages();
    }

    public String getPlayerLanguage(Player player) {
        if (player == null) {
            return normalizeLanguage(serverLanguage);
        }
        try {
            if (ConfigManager.configManager.getBoolean("config-files.per-player-language")) {
                return resolvePlayerLanguage(player.getLocale());
            } else {
                return normalizeLanguage(serverLanguage);
            }
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            return normalizeLanguage(serverLanguage);
        }
    }

    private void initLanguages() {
        File langFolder = new File(EnchantmentReform.instance.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        serverLanguage = normalizeLanguage(ConfigManager.configManager.getString("config-files.language", "en_US"));
        tempMessageFile = loadInternalLanguage(serverLanguage);
        if (tempMessageFile == null) {
            tempMessageFile = new YamlConfiguration();
        }
        YamlConfiguration fullLanguageFile = loadInternalLanguage("en_US");
        if (fullLanguageFile != null) {
            mergeMissingKeys(tempMessageFile, fullLanguageFile);
        }

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String lang = file.getName().replace(".yml", "");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                YamlConfiguration bundledDefaults = loadInternalLanguage(lang);
                if (bundledDefaults != null) {
                    // Keep administrator overrides while exposing newly bundled keys after upgrades.
                    config.setDefaults(bundledDefaults);
                }
                languageFiles.put(normalizeLanguage(lang), config);
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded language: " + lang + ".yml!");
            }
        }

        if (!languageFiles.containsKey("en_us")) {
            languageFiles.put("en_us", loadInternalLanguage("en_US"));
        }
        if (!languageFiles.containsKey(normalizeLanguage(serverLanguage))) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not found language file: " + serverLanguage + ".yml at languages folder!");
            serverLanguage = "en_US";
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fDefault language being set to: " + serverLanguage + ".yml!");
    }

    private String getMessage(Player player, String key, String... args) {
        String lang = getPlayerLanguage(player);

        YamlConfiguration config = languageFiles.getOrDefault(lang,
                languageFiles.getOrDefault(normalizeLanguage(serverLanguage), tempMessageFile));
        String text = config.getString(key);

        if (text == null) {
            if (tempMessageFile.getString(key) != null) {
                text = tempMessageFile.getString(key);
                config.set(key, text);
                saveLanguageFile(lang, config);
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §cAdded new language key: " + key + " for " + lang);
            } else {
                if (args.length == 0) {
                    text = "§cLanguage key not found: " + key;
                } else {
                    text = args[0];
                }
            }
            if (text == null) {
                text = "§cLanguage key not found: " + key;
            }
        }

        for (int i = 1 ; i + 1 < args.length ; i += 2) {
            String var = "{" + args[i] + "}";
            if (args[i + 1] == null) {
                text = text.replace(var, "");
            } else {
                text = text.replace(var, args[i + 1]);
            }
        }
        text = text.replace("{plugin_folder}", String.valueOf(EnchantmentReform.instance.getDataFolder()));
        return text;
    }

    private void saveLanguageFile(String lang, YamlConfiguration config) {
        File file = new File(EnchantmentReform.instance.getDataFolder(), "languages/" + lang + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendStringText(CommandSender sender, String... args) {
        if (sender instanceof Player player) {
            sendStringText(player, args);
        } else {
            sendStringText(null, args);
        }
    }

    public void sendStringText(String... args) {
        sendStringText(null, args);
    }

    public void sendStringText(Player player, String... args) {
        if (args.length == 0) {
            return;
        }
        String text = getMessage(player, args[0]);

        // 替换变量 {key}
        for (int i = 1; i < args.length; i += 2) {
            String var = "{" + args[i] + "}";
            text = text.replace(var, i + 1 < args.length ? (args[i + 1] == null ? "" : args[i + 1]) : "");
        }

        if (!text.isEmpty()) {
            TextUtil.sendMessage(player, text);
        }
    }

    public String getStringText(Player player, String path, String... args) {
        return getMessage(player, path, args);
    }

    public List<String> getStringListText(Player player, String path) {
        String lang = getPlayerLanguage(player);
        YamlConfiguration config = languageFiles.getOrDefault(lang, tempMessageFile);

        List<String> list = config.getStringList(path);
        if (list.isEmpty()) {
            List<String> temp = tempMessageFile.getStringList(path);
            if (!temp.isEmpty()) {
                config.set(path, temp);
                saveLanguageFile(lang, config);
                return temp;
            } else {
                temp.add("§cLanguage key not found: " + path);
                return temp;
            }
        }
        return list;
    }

    private YamlConfiguration loadInternalLanguage(String language) {
        if (language == null) {
            return null;
        }

        String resourceName = language.replace('-', '_');
        String normalizedLanguage = normalizeLanguage(language);
        for (String bundledLanguage : BUNDLED_LANGUAGES) {
            if (normalizeLanguage(bundledLanguage).equals(normalizedLanguage)) {
                resourceName = bundledLanguage;
                break;
            }
        }
        return InitManager.loadBundledConfiguration("languages/" + resourceName + ".yml");
    }

    private void mergeMissingKeys(YamlConfiguration target, YamlConfiguration source) {
        for (String key : source.getKeys(true)) {
            if (source.isConfigurationSection(key)) {
                continue;
            }
            if (target.get(key) == null) {
                target.set(key, source.get(key));
            }
        }
    }

    private String normalizeLanguage(String language) {
        return language == null ? "en_us" : language.replace('-', '_').toLowerCase(Locale.ROOT);
    }

    private String resolvePlayerLanguage(String locale) {
        String normalized = normalizeLanguage(locale);
        if (languageFiles.containsKey(normalized)) {
            return normalized;
        }
        int separator = normalized.indexOf('_');
        String family = separator < 0 ? normalized : normalized.substring(0, separator);
        for (String language : languageFiles.keySet()) {
            if (language.equals(family) || language.startsWith(family + "_")) {
                return language;
            }
        }
        return normalizeLanguage(serverLanguage);
    }
}
