package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectAction<S, C> {

    @FunctionalInterface
    public interface ActionFactory<S, C> {
        S create(ObjectAction<S, C> action, ConfigurationSection section);
    }

    @FunctionalInterface
    public interface ActionRunner<S, C> {
        void run(S singleAction, Player player, C context);
    }

    private final List<S> actions = new ArrayList<>();

    private final ActionRunner<S, C> runner;

    public ObjectAction(ConfigurationSection section, ActionFactory<S, C> factory, ActionRunner<S, C> runner) {
        this.runner = runner;
        if (section == null) {
            return;
        }
        if (section.isString("type")) {
            actions.add(factory.create(this, section));
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection singleAction = section.getConfigurationSection(key);
            if (singleAction != null) {
                actions.add(factory.create(this, singleAction));
            }
        }
    }

    public void runAllActions(Player player, C context) {
        for (S action : actions) {
            runner.run(action, player, context);
        }
    }

    public void runRandomEveryActions(Player player, C context, int amount) {
        List<S> shuffled = new ArrayList<>(actions);
        Collections.shuffle(shuffled);
        for (int index = 0; index < Math.min(amount, shuffled.size()); index++) {
            runner.run(shuffled.get(index), player, context);
        }
    }
}
