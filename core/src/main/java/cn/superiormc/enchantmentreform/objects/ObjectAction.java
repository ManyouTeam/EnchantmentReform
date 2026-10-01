package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    @FunctionalInterface
    public interface OnceChecker<S> {
        boolean isOnce(S singleAction);
    }

    @FunctionalInterface
    public interface MultiGetter<C> {
        int getMulti(C context);
    }

    private ConfigurationSection section;

    private final List<S> everyActions = new ArrayList<>();

    private final List<S> onceActions = new ArrayList<>();

    private final ActionRunner<S, C> runner;

    private final OnceChecker<S> onceChecker;

    private final MultiGetter<C> multiGetter;

    private boolean empty = false;

    private Object lastTradeStatus = null;

    public ObjectAction(ActionRunner<S, C> runner) {
        this.runner = runner;
        this.onceChecker = null;
        this.multiGetter = null;
        this.section = new MemoryConfiguration();
        this.empty = true;
    }

    public ObjectAction(ConfigurationSection section, ActionFactory<S, C> factory, ActionRunner<S, C> runner) {
        this(section, factory, runner, null);
    }

    public ObjectAction(ConfigurationSection section,
                        ActionFactory<S, C> factory,
                        ActionRunner<S, C> runner,
                        OnceChecker<S> onceChecker) {
        this(section, factory, runner, onceChecker, null);
    }

    public ObjectAction(ConfigurationSection section,
                        ActionFactory<S, C> factory,
                        ActionRunner<S, C> runner,
                        OnceChecker<S> onceChecker,
                        MultiGetter<C> multiGetter) {
        this.runner = runner;
        this.onceChecker = onceChecker;
        this.multiGetter = multiGetter;
        this.section = section;
        initAction(factory);
    }

    private void initAction(ActionFactory<S, C> factory) {
        if (section == null) {
            this.empty = true;
            this.section = new MemoryConfiguration();
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection singleActionSection = section.getConfigurationSection(key);
            if (singleActionSection == null || !section.isConfigurationSection(key)) {
                continue;
            }
            S singleAction = factory.create(this, singleActionSection);
            if (onceChecker != null && onceChecker.isOnce(singleAction)) {
                onceActions.add(singleAction);
            } else {
                everyActions.add(singleAction);
            }
        }
        this.empty = onceActions.isEmpty() && everyActions.isEmpty();
    }

    public void runAllActions(Player player, C context) {
        for (S singleAction : everyActions) {
            runner.run(singleAction, player, context);
        }
    }

    public void runAllActions(Player player) {
        runAllActions(player, (C) null);
    }

    public void runAllActions(C context) {
        if (multiGetter != null) {
            runAllActionsByMulti(null, context, multiGetter.getMulti(context));
            return;
        }
        runAllActions(null, context);
    }

    public void runAllActions(Player player, String... args) {
        runAllActions(player, (C) args);
    }

    public void runAllActions(Player player, ItemStack original, ItemStack item) {
        runAllActions(player, (C) new ItemStack[] { original, item });
    }

    public void runAllEveryActions(Player player, C context) {
        runAllActions(player, context);
    }

    public void runAllEveryActions(C context) {
        runAllEveryActions(null, context);
    }

    public void runAllOnceActions(Player player, C context) {
        for (S singleAction : onceActions) {
            runner.run(singleAction, player, context);
        }
    }

    public void runAllOnceActions(C context) {
        runAllOnceActions(null, context);
    }

    public void runAllActionsByMulti(Player player, C context, int multi) {
        runAllOnceActions(player, context);
        for (int i = 0; i < multi; i++) {
            runAllEveryActions(player, context);
        }
    }

    public void runRandomEveryActions(Player player, C context, int amount) {
        Collections.shuffle(everyActions);
        for (int i = 0; i < Math.min(amount, everyActions.size()); i++) {
            runner.run(everyActions.get(i), player, context);
        }
    }

    public void runRandomEveryActions(Player player, int amount) {
        runRandomEveryActions(player, (C) null, amount);
    }

    public void runRandomEveryActions(Player player, int amount, String... args) {
        runRandomEveryActions(player, (C) args, amount);
    }

    public void runRandomEveryActions(Player player, ItemStack original, ItemStack item, int amount) {
        runRandomEveryActions(player, (C) new ItemStack[] { original, item }, amount);
    }

    public void runRandomEveryActions(C context, int amount) {
        runRandomEveryActions(null, context, amount);
    }

    public void runRandomOnceActions(Player player, C context, int amount) {
        Collections.shuffle(onceActions);
        for (int i = 0; i < Math.min(amount, onceActions.size()); i++) {
            runner.run(onceActions.get(i), player, context);
        }
    }

    public void runRandomOnceActions(C context, int amount) {
        runRandomOnceActions(null, context, amount);
    }

    public void runAnyActions(Player player, C context, int amount, int multi) {
        runRandomOnceActions(player, context, amount);
        for (int i = 0; i < multi; i++) {
            runRandomEveryActions(player, context, amount);
        }
    }

    public void runAnyActions(C context, int amount) {
        runAnyActions(null, context, amount, multiGetter == null ? 1 : multiGetter.getMulti(context));
    }

    public boolean isEmpty() {
        return empty;
    }

    public ConfigurationSection getSection() {
        return section;
    }

    public int getAmount() {
        return section.getInt("amount");
    }

    public void setLastTradeStatus(Object status) {
        this.lastTradeStatus = status;
    }

    public Object getLastTradeStatus() {
        return lastTradeStatus;
    }
}
