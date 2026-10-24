package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.menus.ObjectAttributeMenu;
import cn.superiormc.enchantmentreform.objects.menus.ObjectMenu;
import cn.superiormc.enchantmentreform.objects.menus.ObjectSkillMenu;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigManager extends AbstractManager {

    public static ConfigManager configManager;

    public FileConfiguration config;

    public Map<String, ObjectCustomAttribute> attributeConfigs = new LinkedHashMap<>();

    public Map<String, SkillDefinition> skillConfigs = new LinkedHashMap<>();

    public Map<String, ObjectMenu> menuConfigs = new LinkedHashMap<>();

    private YamlConfiguration menuSettings = new YamlConfiguration();

    public ConfigManager() {
        configManager = this;
        EnchantmentReform.instance.reloadConfig();
        config = EnchantmentReform.instance.getConfig();
        EnchantmentReform.instance.saveConfig();
        initAttributeConfigs();
        initSkillConfigs();
        initMenuConfigs();
    }

    public void reloadAttributeConfigs() {
        initAttributeConfigs();
    }

    public void reloadSkillConfigs() {
        initSkillConfigs();
    }

    public void reloadMenuConfigs() {
        initMenuConfigs();
    }

    private void initAttributeConfigs() {
        attributeConfigs.clear();
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "attributes");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not create directory: " + directory);
            return;
        }
        for (File file : CommonUtil.getYamlFiles(directory)) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
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
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not create directory: " + directory);
            return;
        }
        for (File file : CommonUtil.getYamlFiles(directory)) {
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
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

    private void initMenuConfigs() {
        menuConfigs.clear();
        menuSettings = new YamlConfiguration();
        File directory = new File(EnchantmentReform.instance.getDataFolder(), "menus");
        if (!directory.exists() && !directory.mkdirs()) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Could not create menu configuration directory: " + directory);
            return;
        }
        File settingsFile = new File(directory, "settings.yml");
        try {
            menuSettings = YamlConfiguration.loadConfiguration(settingsFile);
        } catch (Exception exception) {
            ErrorManager.errorManager.sendErrorMessage(
                    "§cError: Could not load menu configuration menus/settings.yml: " + exception.getMessage());
        }
        for (File file : CommonUtil.getYamlFiles(directory)) {
            if (file.equals(settingsFile)) {
                continue;
            }
            String id = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            String resource = "menus/" + directory.toPath().relativize(file.toPath())
                    .toString().replace(File.separatorChar, '/');
            try {
                YamlConfiguration section = YamlConfiguration.loadConfiguration(file);
                ObjectMenu menu = switch (id) {
                    case "skill-detail" -> new ObjectSkillMenu(id, section);
                    case "attribute-detail" -> new ObjectAttributeMenu(id, section);
                    case "enchantment-info" -> {
                        ObjectMenu enchantmentMenu = new ObjectMenu(id, section, "buttons", false);
                        enchantmentMenu.initMenu();
                        yield enchantmentMenu;
                    }
                    default -> new ObjectMenu(id, section);
                };
                if (menuConfigs.putIfAbsent(id, menu) != null) {
                    throw new IllegalArgumentException("Duplicate menu id: " + id);
                }
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fLoaded menu: " + id + ".yml!");
            } catch (Throwable throwable) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Failed to load menu " + resource + ": " + throwable.getMessage());
            }
        }
    }

    public ConfigurationSection getEnchantmentMenu() {
        return getMenu("enchantment-info");
    }

    public ConfigurationSection getMenuSettings() {
        return menuSettings;
    }

    public ConfigurationSection getSupportedItems() {
        return getSection("supported-items");
    }

    public ConfigurationSection getRarities() {
        return getSection("rarity");
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public String getString(String path, String... args) {
        String s = config.getString(path);
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
        return config.getInt(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    public ConfigurationSection getSection(String path) {
        if (config.getConfigurationSection(path) == null) {
            return new MemoryConfiguration();
        }
        return config.getConfigurationSection(path);
    }

    public ConfigurationSection getMenu(String id) {
        if (id == null) {
            return new MemoryConfiguration();
        }
        ObjectMenu menu = menuConfigs.get(id.toLowerCase(Locale.ROOT));
        return menu == null ? new MemoryConfiguration() : menu.getConfig();
    }

    public ObjectCustomAttribute getAttribute(String id) {
        return id == null ? null : attributeConfigs.get(id.toLowerCase(Locale.ROOT));
    }

    public SkillDefinition getSkill(String id) {
        return id == null ? null : skillConfigs.get(id.toLowerCase(Locale.ROOT));
    }

    public ObjectMenu getMenuObject(String id) {
        ObjectMenu menu = id == null ? null : menuConfigs.get(id.toLowerCase(Locale.ROOT));
        if (menu == null) {
            throw new IllegalArgumentException("Unknown menu: " + id);
        }
        return menu;
    }

    public boolean hasMenu(String id) {
        return id != null && menuConfigs.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public Collection<String> getMenuIds() {
        return List.copyOf(menuConfigs.keySet());
    }
}
