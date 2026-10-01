package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.LanguageManager;
import org.bukkit.entity.Player;

public final class SubHelp extends AbstractCommand {

    public SubHelp() {
        this.id = "help";
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{1};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        LanguageManager.languageManager.sendStringText(player, player.hasPermission("enchantmentreform.admin") ? "help.main-admin" : "help.main");
    }
    @Override
    public void executeCommandInConsole(String[] args) {
        LanguageManager.languageManager.sendStringText("help.main-console");
    }
}
