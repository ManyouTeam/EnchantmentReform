package cn.superiormc.enchantmentreform.objects.menus;

import cn.superiormc.enchantmentreform.objects.buttons.ObjectButton;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectMenu {

    public static Map<String, ObjectMenu> commonMenus = new LinkedHashMap<>();

    private final String id;

    private final ConfigurationSection config;

    private final String buttonPath;

    private final boolean validateLayout;

    private final Map<String, ObjectButton> buttonItems = new LinkedHashMap<>();

    private final Map<String, ObjectMenuContent> contents = new LinkedHashMap<>();

    private final Map<String, Map<String, List<Integer>>> layouts = new LinkedHashMap<>();

    public ObjectMenu(String id, ConfigurationSection config) {
        this(id, config, "items", true);
        initMenu();
    }

    protected ObjectMenu(String id, ConfigurationSection config, String buttonPath, boolean validateLayout) {
        this.id = id;
        this.config = config;
        this.buttonPath = buttonPath;
        this.validateLayout = validateLayout;
    }

    public void initMenu() {
        layouts.clear();
        initLayouts();
        initButtons();
        initContents();
    }

    public void initButtons() {
        buttonItems.clear();
        ConfigurationSection section = config.getConfigurationSection(buttonPath);
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection button = section.getConfigurationSection(key);
            if (button != null) {
                buttonItems.put(key, new ObjectButton(key, button));
            }
        }
    }

    public void initContents() {
        contents.clear();
        ConfigurationSection section = config.getConfigurationSection("contents");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection content = section.getConfigurationSection(key);
            if (content != null) {
                contents.put(key, new ObjectMenuContent(key, content));
            }
        }
    }

    public Map<String, List<Integer>> layoutSlots(List<String> layout) {
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        for (int row = 0; row < layout.size(); row++) {
            for (int column = 0; column < 9; column++) {
                String symbol = String.valueOf(layout.get(row).charAt(column));
                result.computeIfAbsent(symbol, ignored -> new ArrayList<>()).add(row * 9 + column);
            }
        }
        return result;
    }

    public List<Integer> symbolSlots(String id, Map<String, List<Integer>> layoutSlots) {
        if (id.length() != 1) {
            throw new IllegalArgumentException("Menu '" + this.id + "' layout entry '" + id + "' must be one character");
        }
        return layoutSlots.getOrDefault(id, List.of());
    }

    public String getId() {
        return id;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public Map<String, ObjectButton> getButtons() {
        return Collections.unmodifiableMap(buttonItems);
    }

    public Map<String, ObjectMenuContent> getContents() {
        return Collections.unmodifiableMap(contents);
    }

    public List<String> getLayout() {
        return config.getStringList("layout");
    }

    public ObjectButton getButton(String id) {
        return buttonItems.get(id);
    }

    public ObjectButton getContentButton(ConfigurationSection section) {
        for (ObjectMenuContent content : contents.values()) {
            if (content.getConfig() == section) {
                return content.getButton();
            }
        }
        return null;
    }

    private void initLayouts() {
        if (!validateLayout) {
            return;
        }
        List<String> rows = getLayout();
        if (rows.isEmpty() || rows.size() > 6 || rows.stream().anyMatch(row -> row.length() != 9)) {
            throw new IllegalArgumentException("Menu '" + id + "' requires a layout containing 1-6 rows of exactly 9 characters");
        }
        layouts.put("layout", layoutSlots(rows));
    }

}
