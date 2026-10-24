package cn.superiormc.enchantmentreform.objects.menus;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ObjectAttributeMenu extends ObjectMenu {

    private static final List<String> LEGACY_DEFAULT_LAYOUT = List.of(
            "FFFFFFFFF", "FFFFIFFFF", "FFFFFFFFF",
            "FMMMMMMMF", "FMMMMMMMF", "BFFPUNFFC");

    private final List<Integer> modifierSlots = new ArrayList<>();

    private final Map<Integer, String> staticSlots = new LinkedHashMap<>();

    private final ConfigurationSection config;

    public ObjectAttributeMenu(String id, ConfigurationSection config) {
        super(id, config, "buttons", false);
        this.config = config;
        initMenu();
    }

    @Override
    public void initMenu() {
        super.initMenu();
        modifierSlots.clear();
        staticSlots.clear();
        parseLayout();
    }

    public List<Integer> getModifierSlots() {
        return Collections.unmodifiableList(modifierSlots);
    }

    public Map<Integer, String> getStaticSlots() {
        return Collections.unmodifiableMap(staticSlots);
    }

    private void parseLayout() {
        int slot = 0;
        String modifierSymbol = config.getString("modifier-symbol", "M");
        List<String> layout = config.getStringList("layout");
        for (String row : layout) {
            for (int column = 0; column < 9; column++, slot++) {
                String symbol = column < row.length()
                        ? String.valueOf(row.charAt(column)) : " ";
                if (symbol.equals(modifierSymbol)) {
                    modifierSlots.add(slot);
                } else if (!symbol.isBlank() && !symbol.equals(".")) {
                    staticSlots.put(slot, symbol);
                }
            }
        }
        if (layout.equals(LEGACY_DEFAULT_LAYOUT)) {
            staticSlots.put(46, config.getString("enable-symbol", "E"));
            staticSlots.put(52, config.getString("effective-level-symbol", "L"));
        }
    }

}
