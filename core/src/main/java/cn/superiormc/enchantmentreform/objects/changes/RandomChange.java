package cn.superiormc.enchantmentreform.objects.changes;

import cn.superiormc.enchantmentreform.managers.ChangesManager;
import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleChange;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomChange extends AbstractChangesRule {

    @Override
    public ItemStack setChange(ObjectSingleChange change) {
        ConfigurationSection choices = change.getConfigurationSection("random-change");
        if (choices == null) {
            return change.getItem();
        }
        Map<ConfigurationSection, Double> weights = new LinkedHashMap<>();
        double total = 0;
        for (String key : choices.getKeys(false)) {
            ConfigurationSection choice = choices.getConfigurationSection(key);
            if (choice == null) {
                continue;
            }
            // Match the unchanged input item so reroll exclusions still see the old
            // affix even when an earlier change has already removed its lore.
            if (!MatchItemManager.matchItemManager.getMatch(
                    choice.getConfigurationSection("match-item"), change.getPlayer(),
                    change.getOriginal(), change.getContext())
                    || !PowerConditionsManager.powerConditions.matches(
                    choice.getConfigurationSection("conditions"), change.getContext(),
                    change.getPlayer())) {
                continue;
            }
            double weight = Math.max(0, choice.getDouble("rate", 1));
            weights.put(choice, weight);
            total += weight;
        }
        if (total <= 0) {
            return change.getItem();
        }
        double picked = ThreadLocalRandom.current().nextDouble(total);
        for (Map.Entry<ConfigurationSection, Double> entry : weights.entrySet()) {
            picked -= entry.getValue();
            if (picked <= 0) {
                ConfigurationSection changes = entry.getKey().getConfigurationSection("changes");
                return ChangesManager.changesManager.setChange(new ObjectSingleChange(
                        changes != null ? changes : entry.getKey(), change));
            }
        }
        return change.getItem();
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("random-change");
    }
}
