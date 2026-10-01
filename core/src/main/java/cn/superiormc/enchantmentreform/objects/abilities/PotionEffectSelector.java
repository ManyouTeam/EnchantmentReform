package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PotionEffectSelector {

    private final List<Rule> rules;

    private PotionEffectSelector(List<Rule> rules) {
        this.rules = rules;
    }

    public static PotionEffectSelector parse(List<String> configured) {
        List<Rule> rules = new ArrayList<>();
        for (String value : configured) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            switch (normalized) {
                case "ALL" -> rules.add(Rule.matchAll());
                case "BENEFICIAL" -> rules.add(Rule.category(PotionEffectTypeCategory.BENEFICIAL));
                case "HARMFUL" -> rules.add(Rule.category(PotionEffectTypeCategory.HARMFUL));
                case "NEUTRAL" -> rules.add(Rule.category(PotionEffectTypeCategory.NEUTRAL));
                default -> {
                    NamespacedKey key = CommonUtil.parseNamespacedKey(value);
                    PotionEffectType type = key == null ? null : Registry.EFFECT.get(key);
                    if (type != null) {
                        rules.add(Rule.exact(type));
                    }
                }
            }
        }
        return new PotionEffectSelector(List.copyOf(rules));
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    public boolean matches(PotionEffectType type) {
        if (type == null || rules.isEmpty()) {
            return false;
        }
        for (Rule rule : rules) {
            if (rule.matches(type)) {
                return true;
            }
        }
        return false;
    }

    public List<PotionEffectType> selectEffects(Collection<PotionEffect> effects, int limit) {
        return selectTypes(effects.stream().map(PotionEffect::getType).toList(), limit);
    }

    public List<PotionEffectType> selectTypes(Collection<PotionEffectType> available, int limit) {
        if (limit <= 0 || rules.isEmpty()) {
            return List.of();
        }
        List<PotionEffectType> sorted = available.stream()
                .distinct()
                .sorted(Comparator.comparing(PotionEffectSelector::key))
                .toList();
        Set<PotionEffectType> selected = new LinkedHashSet<>();
        for (Rule rule : rules) {
            for (PotionEffectType type : sorted) {
                if (rule.matches(type)) {
                    selected.add(type);
                    if (selected.size() >= limit) {
                        return List.copyOf(selected);
                    }
                }
            }
        }
        return List.copyOf(selected);
    }

    private static String key(PotionEffectType type) {
        NamespacedKey key = Registry.EFFECT.getKey(type);
        return key == null ? type.getName() : key.toString();
    }

    private record Rule(boolean all, PotionEffectType exact, PotionEffectTypeCategory category) {

        static Rule matchAll() {
            return new Rule(true, null, null);
        }

        static Rule exact(PotionEffectType type) {
            return new Rule(false, type, null);
        }

        static Rule category(PotionEffectTypeCategory category) {
            return new Rule(false, null, category);
        }

        boolean matches(PotionEffectType type) {
            return all || exact == type || category != null && category == type.getCategory();
        }
    }
}
