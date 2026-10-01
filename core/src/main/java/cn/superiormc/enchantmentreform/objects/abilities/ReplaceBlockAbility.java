package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.hooks.BlockPriceUtil;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ReplaceBlockAbility extends AbstractAbility {

    private final Map<String, String> replacements;

    public ReplaceBlockAbility(ConfigurationSection section) {
        super("ReplaceBlock", section);
        replacements = loadReplacements(section.getConfigurationSection("replacements"));
    }

    @Override
    public boolean execute(PowerContext context) {
        Block block = context.block();
        if (block == null || replacements.isEmpty()) {
            return false;
        }

        String source = BlockPriceUtil.findMatch(block, replacements.keySet());
        if (source != null) {
            BlockPriceUtil.place(block.getLocation(), replacements.get(source));
        }
        return false;
    }

    private Map<String, String> loadReplacements(ConfigurationSection replacementsSection) {
        if (replacementsSection == null) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : replacementsSection.getValues(false).entrySet()) {
            if (!(entry.getValue() instanceof String target)) {
                continue;
            }
            String source = entry.getKey().trim();
            String destination = target.trim();
            if (!source.isEmpty() && !destination.isEmpty()) {
                result.put(source, destination);
            }
        }
        return Map.copyOf(result);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
