package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ConfigManager extends AbstractManager {

    public static ConfigManager configManager;

    public FileConfiguration config;

    private final YamlConfiguration menuConfig = new YamlConfiguration();

    private static final List<String> MENU_FILES = List.of(
            "enchantment-info", "attribute-info", "skill-info", "skill-detail",
            "attribute-allocation", "attribute-detail", "main");

    private final ConfigurationSection enchantmentMenu;

    public ConfigManager() {
        configManager = this;
        EnchantmentReform.instance.reloadConfig();
        config = EnchantmentReform.instance.getConfig();
        config.addDefault("anvil.bypass-enchantment-level-limit", false);
        config.options().copyDefaults(true);
        EnchantmentReform.instance.saveConfig();
        loadMenus();
        enchantmentMenu = getSection("menu.enchantment-info");
    }

    public ConfigurationSection getEnchantmentMenu() {
        return enchantmentMenu;
    }

    public ConfigurationSection getSupportedItems() {
        return getSection("supported-items");
    }

    public ConfigurationSection getRarities() {
        return getSection("rarity");
    }

    public boolean getBoolean(String path) {
        return configuration(path).getBoolean(normalizePath(path), false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return configuration(path).getBoolean(normalizePath(path), defaultValue);
    }

    public String getString(String path, String... args) {
        String s = configuration(path).getString(normalizePath(path));
        if (s == null) {
            if (args.length == 0) {
                return null;
            }
            s = args[0];
        }
        for (int i = 1 ; i < args.length ; i += 2) {
            String var = "{" + args[i] + "}";
            if (args[i + 1] == null) {
                s = s.replace(var, "");
            }
            else {
                s = s.replace(var, args[i + 1]);
            }
        }
        return s.replace("{plugin_folder}", String.valueOf(EnchantmentReform.instance.getDataFolder()));
    }

    public int getInt(String path, int defaultValue) {
        return configuration(path).getInt(normalizePath(path), defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return configuration(path).getDouble(normalizePath(path), defaultValue);
    }

    public ConfigurationSection getSection(String path) {
        FileConfiguration selected = configuration(path);
        String normalized = normalizePath(path);
        if (selected.getConfigurationSection(normalized) == null) {
            return new MemoryConfiguration();
        }
        return selected.getConfigurationSection(normalized);
    }

    public ConfigurationSection getMenu(String id) {
        if (id == null) return new MemoryConfiguration();
        ConfigurationSection section = menuConfig.getConfigurationSection(
                id.toLowerCase(Locale.ROOT));
        return section == null ? new MemoryConfiguration() : section;
    }

    public boolean hasMenu(String id) {
        return id != null && menuConfig.isConfigurationSection(id.toLowerCase(Locale.ROOT));
    }

    public Collection<String> getMenuIds() {
        return menuConfig.getKeys(false).stream()
                .filter(menuConfig::isConfigurationSection)
                .toList();
    }

    private void loadMenus() {
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "menus");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Could not create menu configuration directory: " + directory);
            return;
        }
        loadMenuFile(directory, "settings", null);
        for (String menu : MENU_FILES) {
            loadMenuFile(directory, menu, menu);
        }
        File[] customMenus = directory.listFiles((ignored, name) ->
                name.toLowerCase(Locale.ROOT).endsWith(".yml")
                        && !name.equalsIgnoreCase("settings.yml"));
        if (customMenus != null) {
            Set<String> loaded = new LinkedHashSet<>(MENU_FILES);
            for (File file : customMenus) {
                String id = file.getName().substring(0, file.getName().length() - 4)
                        .toLowerCase(Locale.ROOT);
                if (loaded.add(id)) {
                    loadMenuFile(directory, id, id);
                }
            }
        }
    }

    private void loadMenuFile(File directory, String fileName, String sectionName) {
        String resource = "menus/" + fileName + ".yml";
        File file = new File(directory, fileName + ".yml");
        YamlConfiguration loaded;
        try {
            loaded = InitManager.loadConfiguration(file, resource);
        } catch (Exception exception) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Could not load menu configuration " + resource + ": " + exception.getMessage());
            return;
        }
        ConfigurationSection target = sectionName == null
                ? menuConfig : menuConfig.createSection(sectionName);
        copySection(loaded, target);
    }

    private static void copySection(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            ConfigurationSection child = source.getConfigurationSection(key);
            if (child == null) {
                target.set(key, source.get(key));
            } else {
                copySection(child, target.createSection(key));
            }
        }
    }

    private FileConfiguration configuration(String path) {
        return path != null && path.startsWith("menu.") ? menuConfig : config;
    }

    private static String normalizePath(String path) {
        return path != null && path.startsWith("menu.") ? path.substring("menu.".length()) : path;
    }
}
