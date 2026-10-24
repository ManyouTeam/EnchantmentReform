package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.menus.ObjectAttributeMenu;
import cn.superiormc.enchantmentreform.objects.menus.ObjectMenu;
import cn.superiormc.enchantmentreform.objects.menus.ObjectSkillMenu;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.TextUtil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConfigManager extends AbstractManager {

    public static ConfigManager configManager;

    private static final List<String> MENU_FILES = List.of(
            "enchantment-info", "attribute-info", "skill-info", "skill-detail",
            "attribute-allocation", "attribute-detail", "main");

    public FileConfiguration config;

    public Map<String, ObjectCustomAttribute> attributeConfigs = new LinkedHashMap<>();

    public Map<String, SkillDefinition> skillConfigs = new LinkedHashMap<>();

    private final YamlConfiguration menuConfig = new YamlConfiguration();

    private final ConfigurationSection enchantmentMenu;

    public ConfigManager() {
        configManager = this;
        EnchantmentReform.instance.reloadConfig();
        config = EnchantmentReform.instance.getConfig();
        config.addDefault("anvil.bypass-enchantment-level-limit", false);
        config.options().copyDefaults(true);
        EnchantmentReform.instance.saveConfig();
        initAttributeConfigs();
        initSkillConfigs();
        loadMenus();
        initMenuConfigs();
        enchantmentMenu = getSection("menu.enchantment-info");
    }

    public void reloadAttributeConfigs() {
        initAttributeConfigs();
    }

    public void reloadSkillConfigs() {
        initSkillConfigs();
    }

    private void initAttributeConfigs() {
        attributeConfigs.clear();
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "attributes");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not create custom attribute directory: " + directory);
            return;
        }
        for (File file : findAttributeFiles(directory)) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = InitManager.loadConfiguration(file, "attributes/" + file.getName().toLowerCase(Locale.ROOT));
                ObjectCustomAttribute attribute = new ObjectCustomAttribute(id, config);
                if (!attribute.isEnabled()) {
                    continue;
                }
                if (attributeConfigs.putIfAbsent(id, attribute) != null) {
                    throw new IllegalArgumentException("Duplicate custom attribute id: " + id);
                }
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded custom attribute: " + id + ".yml!");
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Failed to load custom attribute " + file.getName() + ": " + throwable.getMessage());
            }
        }
    }

    private void initSkillConfigs() {
        skillConfigs.clear();
        if (!getBoolean("modules.skills", true)) {
            return;
        }
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "skills");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not create skill directory: " + directory);
            return;
        }
        File[] files = directory.listFiles((ignored, name) ->
                name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = InitManager.loadConfiguration(file, "skills/" + file.getName());
                if (!config.getBoolean("enabled", true)) {
                    continue;
                }
                SkillDefinition skill = new SkillDefinition(id, config);
                if (skillConfigs.putIfAbsent(id, skill) != null) {
                    throw new IllegalArgumentException("Duplicate skill id: " + id);
                }
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded skill: " + id + ".yml!");
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Failed to load skill " + file.getName() + ": " + throwable.getMessage());
            }
        }
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

    private void initMenuConfigs() {
        ObjectMenu.commonMenus.clear();
        for (String id : getMenuIds()) {
            ConfigurationSection section = getMenu(id);
            if (id.equals("enchantment-info")) {
                continue;
            }
            try {
                ObjectMenu menu = switch (id) {
                    case "skill-detail" -> new ObjectSkillMenu(id, section);
                    case "attribute-detail" -> new ObjectAttributeMenu(id, section);
                    default -> new ObjectMenu(id, section);
                };
                ObjectMenu.commonMenus.put(id, menu);
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Failed to load menu " + id + ": " + throwable.getMessage());
            }
        }
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
        if (id == null) {
            return new MemoryConfiguration();
        }
        ConfigurationSection section = menuConfig.getConfigurationSection(
                id.toLowerCase(Locale.ROOT));
        return section == null ? new MemoryConfiguration() : section;
    }

    public ObjectCustomAttribute getAttribute(String id) {
        return id == null ? null : attributeConfigs.get(id.toLowerCase(Locale.ROOT));
    }

    public SkillDefinition getSkill(String id) {
        return id == null ? null : skillConfigs.get(id.toLowerCase(Locale.ROOT));
    }

    public ObjectMenu getMenuObject(String id) {
        ObjectMenu menu = ObjectMenu.commonMenus.get(id.toLowerCase(Locale.ROOT));
        if (menu == null) {
            throw new IllegalArgumentException("Unknown menu: " + id);
        }
        return menu;
    }

    public boolean hasMenu(String id) {
        return id != null && menuConfig.isConfigurationSection(id.toLowerCase(Locale.ROOT));
    }

    public Collection<String> getMenuIds() {
        return menuConfig.getKeys(false).stream()
                .filter(menuConfig::isConfigurationSection)
                .toList();
    }

    private List<File> findAttributeFiles(File directory) {
        List<File> result = new ArrayList<>();
        File[] entries = directory.listFiles();
        if (entries == null) {
            return result;
        }
        for (File entry : entries) {
            if (entry.isFile()
                    && entry.getName().toLowerCase(Locale.ROOT).endsWith(".yml")) {
                result.add(entry);
            }
        }
        result.sort(Comparator.comparing(File::getName));
        return result;
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
