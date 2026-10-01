package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class AbstractPowerCondition {

    private final String type;

    protected AbstractPowerCondition(String type) {
        this.type = type;
    }

    public final boolean matches(ObjectSingleCondition condition) {
        boolean matched = alwaysMatches(condition)
                || condition.getSection().getBoolean("not", false) != onMatch(condition);
        sendResultMessage(condition, matched);
        return matched;
    }

    /** Allows a condition type to be globally bypassed, including its {@code not} option. */
    protected boolean alwaysMatches(ObjectSingleCondition condition) {
        return false;
    }

    private void sendResultMessage(ObjectSingleCondition condition, boolean matched) {
        Player player = condition.player();
        if (player == null) {
            return;
        }
        String path = matched ? "meet-message" : "not-meet-message";
        TextUtil.sendMessage(player, condition.getString(path, player, condition.getContext()));
    }

    protected abstract boolean onMatch(ObjectSingleCondition condition);

    /** Cleans type-wide runtime state. */
    public void onUnload() {
    }

    /** Cleans type-wide runtime state associated with one entity. */
    public void onEntityUnload(UUID entityId) {
    }

    public final String getType() {
        return type;
    }
}
