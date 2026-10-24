package cn.superiormc.enchantmentreform.objects.menus;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ObjectSkillMenu extends ObjectMenu {

    private final List<Integer> sourceSlots = new ArrayList<>();

    private final List<Integer> attributeSlots = new ArrayList<>();

    private final List<Integer> rewardSlots = new ArrayList<>();

    private final Map<Integer, String> sourceStaticSlots = new LinkedHashMap<>();

    private final Map<Integer, String> rewardStaticSlots = new LinkedHashMap<>();

    private final ConfigurationSection config;

    public ObjectSkillMenu(String id, ConfigurationSection config) {
        super(id, config, "buttons", false);
        this.config = config;
        initMenu();
    }

    @Override
    public void initMenu() {
        super.initMenu();
        sourceSlots.clear();
        attributeSlots.clear();
        rewardSlots.clear();
        sourceStaticSlots.clear();
        rewardStaticSlots.clear();
        parseLayouts();
    }

    public List<Integer> getSourceSlots() {
        return Collections.unmodifiableList(sourceSlots);
    }

    public List<Integer> getAttributeSlots() {
        return Collections.unmodifiableList(attributeSlots);
    }

    public List<Integer> getRewardSlots() {
        return Collections.unmodifiableList(rewardSlots);
    }

    public Map<Integer, String> getSourceStaticSlots() {
        return Collections.unmodifiableMap(sourceStaticSlots);
    }

    public Map<Integer, String> getRewardStaticSlots() {
        return Collections.unmodifiableMap(rewardStaticSlots);
    }

    private void parseLayouts() {
        parseLayout(config.getStringList("layout"), config.getString("source-symbol", "S"),
                sourceSlots, sourceStaticSlots, false);
        parseAttributeSlots(config.getStringList("layout"),
                config.getString("attribute-symbol", "A"));
        List<String> rewardLayout = config.getStringList("reward-layout");
        if (rewardLayout.isEmpty()) {
            rewardLayout = config.getStringList("layout");
        }
        parseLayout(rewardLayout, config.getString("reward-symbol", "R"),
                rewardSlots, rewardStaticSlots, true);
    }

    private void parseAttributeSlots(List<String> layout, String symbol) {
        for (int rowIndex = 0; rowIndex < layout.size(); rowIndex++) {
            String row = layout.get(rowIndex);
            for (int column = 0; column < 9; column++) {
                if (column < row.length()
                        && String.valueOf(row.charAt(column)).equals(symbol)) {
                    int slot = rowIndex * 9 + column;
                    attributeSlots.add(slot);
                    sourceStaticSlots.remove(slot);
                }
            }
        }
    }

    private void parseLayout(List<String> layout, String symbol, List<Integer> entrySlots,
                             Map<Integer, String> staticSlots, boolean snake) {
        boolean vertical = snake && config.getBoolean("reward-vertical", false);
        int rewardBand = 0;
        for (int band = 0; band < (vertical ? 9 : layout.size()); band++) {
            List<Integer> rowEntries = new ArrayList<>();
            for (int offset = 0; offset < (vertical ? layout.size() : 9); offset++) {
                int rowIndex = vertical ? offset : band;
                int column = vertical ? band : offset;
                String row = layout.get(rowIndex);
                String value = column < row.length() ? String.valueOf(row.charAt(column)) : " ";
                int slot = rowIndex * 9 + column;
                if (value.equals(symbol)) {
                    rowEntries.add(slot);
                } else if (!value.isBlank() && !value.equals(".")) {
                    staticSlots.put(slot, value);
                }
            }
            if (snake && rowEntries.size() > 1 && rewardBand++ % 2 == 1) {
                Collections.reverse(rowEntries);
            }
            entrySlots.addAll(rowEntries);
        }
    }

}
