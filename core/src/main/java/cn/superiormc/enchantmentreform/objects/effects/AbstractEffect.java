package cn.superiormc.enchantmentreform.objects.effects;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class AbstractEffect {

    protected final String id;

    protected final Player player;

    protected final ConfigurationSection section;

    private final double value;

    private SchedulerUtil retryTask;

    private int attempts;

    private boolean applied;

    private boolean closed;

    protected AbstractEffect(String id, Player player, ConfigurationSection section, double value) {
        this.id = id;
        this.player = player;
        this.section = section;
        this.value = value;
    }

    public final double getValue() {
        return value;
    }

    public final synchronized void activate() {
        if (closed || applied) {
            return;
        }
        attempts++;
        try {
            applied = addPlayerStat();
        } catch (RuntimeException e) {
            attempts = 3;
            EnchantmentReform.instance.getLogger().warning(section.getCurrentPath()
                    + ": Failed to apply stat for " + player.getName() + ": " + e);
            return;
        }
        if (!applied && attempts < 3 && player.isOnline()) {
            retryTask = SchedulerUtil.runTaskLater(player, this::activate, 20L);
        } else if (!applied) {
            EnchantmentReform.instance.getLogger().warning(section.getCurrentPath()
                    + ": Stat profile unavailable for " + player.getName());
        }
    }

    public final synchronized boolean canRetry() {
        return !applied && attempts >= 3;
    }

    public final synchronized void deactivate() {
        closed = true;
        if (retryTask != null) {
            retryTask.cancel();
        }
        if (applied) {
            removePlayerStat();
            applied = false;
        }
    }

    /** Returns false while the player's profile is loading. */
    protected abstract boolean addPlayerStat();

    protected abstract void removePlayerStat();
}
