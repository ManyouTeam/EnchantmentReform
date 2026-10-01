package cn.superiormc.enchantmentreform.objects.conditions;

import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.entity.Player;

public abstract class AbstractCheckCondition<S extends AbstractConfiguredSection<C>, C> {

    private final String type;

    private String[] requiredArgs;

    public AbstractCheckCondition(String type) {
        this.type = type;
    }

    protected void setRequiredArgs(String... requiredArgs) {
        this.requiredArgs = requiredArgs;
    }

    public boolean checkCondition(S singleCondition, Player player, C context) {
        if (requiredArgs != null) {
            for (String arg : requiredArgs) {
                if (!singleCondition.getSection().contains(arg)) {
                    ErrorManager.errorManager.sendErrorMessage("§cError: Your condition missing required arg: " + arg + ".");
                    sendResultMessage(singleCondition, player, context, true);
                    return true;
                }
            }
        }
        boolean matched = onCheckCondition(singleCondition, player, context);
        sendResultMessage(singleCondition, player, context, matched);
        return matched;
    }

    private void sendResultMessage(S singleCondition, Player player, C context, boolean matched) {
        String path = matched ? "meet-message" : "not-meet-message";
        TextUtil.sendMessage(player, singleCondition.getString(path, player, context));
    }

    protected abstract boolean onCheckCondition(S singleCondition, Player player, C context);

    public String getType() {
        return type;
    }
}
