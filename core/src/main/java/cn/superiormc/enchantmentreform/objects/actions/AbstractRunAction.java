package cn.superiormc.enchantmentreform.objects.actions;

import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public abstract class AbstractRunAction<S extends AbstractConfiguredSection<C>, C> {

    private final String type;

    private String[] requiredArgs;

    public AbstractRunAction(String type) {
        this.type = type;
    }

    protected void setRequiredArgs(String... requiredArgs) {
        this.requiredArgs = requiredArgs;
    }

    public void runAction(S singleAction, Player player, C context) {
        if (requiredArgs != null) {
            for (String arg : requiredArgs) {
                if (!singleAction.getSection().contains(arg)) {
                    ErrorManager.errorManager.sendErrorMessage("§cError: Your action missing required arg: " + arg + ".");
                    return;
                }
            }
        }
        onDoAction(singleAction, player, context);
    }

    protected abstract void onDoAction(S singleAction, Player player, C context);

    public String getType() {
        return type;
    }
}
