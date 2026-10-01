package cn.superiormc.enchantmentreform.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ObjectCondition<S, C> {

    @FunctionalInterface
    public interface ConditionFactory<S, C> {
        S create(ObjectCondition<S, C> condition, ConfigurationSection section);
    }

    @FunctionalInterface
    public interface ConditionChecker<S, C> {
        boolean check(S singleCondition, Player player, C context);
    }

    @FunctionalInterface
    public interface FailureHandler<C> {
        void onFailure(ObjectCondition<?, C> condition, Player player, C context);
    }

    @FunctionalInterface
    public interface PlayerGetter<C> {
        Player getPlayer(C context);
    }

    private ConfigurationSection section;

    private boolean empty = false;

    private final List<S> conditions = new ArrayList<>();

    private final ConditionChecker<S, C> checker;

    private final FailureHandler<C> failureHandler;

    private final PlayerGetter<C> playerGetter;

    public ObjectCondition(ConditionChecker<S, C> checker) {
        this.checker = checker;
        this.failureHandler = null;
        this.playerGetter = null;
        this.section = new MemoryConfiguration();
        this.empty = true;
    }

    public ObjectCondition(ConfigurationSection section,
                           ConditionFactory<S, C> factory,
                           ConditionChecker<S, C> checker) {
        this(section, factory, checker, null);
    }

    public ObjectCondition(ConfigurationSection section,
                           ConditionFactory<S, C> factory,
                           ConditionChecker<S, C> checker,
                           FailureHandler<C> failureHandler) {
        this(section, factory, checker, failureHandler, null);
    }

    public ObjectCondition(ConfigurationSection section,
                           ConditionFactory<S, C> factory,
                           ConditionChecker<S, C> checker,
                           FailureHandler<C> failureHandler,
                           PlayerGetter<C> playerGetter) {
        this.section = section;
        this.checker = checker;
        this.failureHandler = failureHandler;
        this.playerGetter = playerGetter;
        initCondition(factory);
    }

    private void initCondition(ConditionFactory<S, C> factory) {
        if (section == null) {
            this.empty = true;
            this.section = new MemoryConfiguration();
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection singleConditionSection = section.getConfigurationSection(key);
            if (singleConditionSection == null || !section.isConfigurationSection(key)) {
                continue;
            }
            conditions.add(factory.create(this, singleConditionSection));
        }
        this.empty = conditions.isEmpty();
    }

    public boolean getAllBoolean(Player player, C context) {
        if (player == null) {
            return false;
        }
        for (S singleCondition : conditions) {
            if (!checker.check(singleCondition, player, context)) {
                onFailure(player, context);
                return false;
            }
        }
        return true;
    }

    public boolean getAllBoolean(Player player) {
        return getAllBoolean(player, null);
    }

    public boolean getAllBoolean(Player player, ItemStack original, ItemStack item) {
        return getAllBoolean(player, (C) new ItemStack[] { original, item });
    }

    public boolean getAllBoolean(C context) {
        if (playerGetter != null) {
            return getAllBoolean(playerGetter.getPlayer(context), context);
        }
        return getAllBoolean(null, context);
    }

    public boolean getAnyBoolean(Player player, C context) {
        if (player == null) {
            return false;
        }
        for (S singleCondition : conditions) {
            if (checker.check(singleCondition, player, context)) {
                return true;
            }
        }
        onFailure(player, context);
        return false;
    }

    public boolean getAnyBoolean(Player player) {
        return getAnyBoolean(player, null);
    }

    public boolean getAnyBoolean(Player player, ItemStack original, ItemStack item) {
        return getAnyBoolean(player, (C) new ItemStack[] { original, item });
    }

    public boolean getAnyBoolean(C context) {
        if (playerGetter != null) {
            return getAnyBoolean(playerGetter.getPlayer(context), context);
        }
        return getAnyBoolean(null, context);
    }

    public boolean getAllBooleanAllowNullPlayer(C context) {
        for (S singleCondition : conditions) {
            if (!checker.check(singleCondition, null, context)) {
                onFailure(null, context);
                return false;
            }
        }
        return true;
    }

    public boolean getAnyBooleanAllowNullPlayer(C context) {
        for (S singleCondition : conditions) {
            if (checker.check(singleCondition, null, context)) {
                return true;
            }
        }
        onFailure(null, context);
        return false;
    }

    private void onFailure(Player player, C context) {
        if (failureHandler != null) {
            failureHandler.onFailure(this, player, context);
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public ConfigurationSection getSection() {
        return section;
    }
}
