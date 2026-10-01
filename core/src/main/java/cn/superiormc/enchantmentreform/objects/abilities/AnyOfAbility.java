package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class AnyOfAbility extends AbstractAbility {

    public AnyOfAbility(ConfigurationSection section) {
        super("AnyOf", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection abilities = section.getConfigurationSection("abilities");
        if (abilities == null) {
            return false;
        }

        List<WeightedAbility> remaining = new ArrayList<>();
        for (String key : abilities.getKeys(false)) {
            ConfigurationSection ability = abilities.getConfigurationSection(key);
            if (ability == null) {
                continue;
            }

            double rate = new EntryConfig(ability).getDouble("rate", 1.0D, context);
            if (!Double.isFinite(rate) || rate <= 0.0D) {
                continue;
            }
            remaining.add(new WeightedAbility(ability, rate));
        }

        int amount = Math.max(0, getInt("amount", 1, context));
        amount = Math.min(amount, remaining.size());

        boolean cancel = false;
        for (int i = 0; i < amount; i++) {
            WeightedAbility selected = remaining.remove(selectIndex(remaining));
            if (AbilityManager.abilityManager.executeSingle(selected.section(), context)) {
                cancel = true;
            }
        }
        return cancel;
    }

    private int selectIndex(List<WeightedAbility> abilities) {
        double maximum = 0.0D;
        for (WeightedAbility ability : abilities) {
            maximum = Math.max(maximum, ability.rate());
        }

        double total = 0.0D;
        for (WeightedAbility ability : abilities) {
            total += ability.rate() / maximum;
        }

        double selected = ThreadLocalRandom.current().nextDouble(total);
        double current = 0.0D;
        for (int i = 0; i < abilities.size(); i++) {
            current += abilities.get(i).rate() / maximum;
            if (selected < current) {
                return i;
            }
        }
        return abilities.size() - 1;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }

    private record WeightedAbility(ConfigurationSection section, double rate) {
    }

    private static final class EntryConfig extends AbstractConfiguredSection<PowerContext> {
        private EntryConfig(ConfigurationSection section) {
            super(section);
        }
    }
}
