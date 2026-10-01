package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SubReload extends AbstractCommand {

    public SubReload() {
        this.id = "reload";
        this.requiredPermission =  "enchantmentreform." + id;
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{1};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        reload(player);
    }

    @Override
    public void executeCommandInConsole(String[] args) {
        reload(Bukkit.getConsoleSender());
    }

    private void reload(CommandSender sender) {
        LanguageManager.languageManager.sendStringText(sender, "plugin.reloading");
        EnchantmentReform.instance.reloadConfig();
        AbstractManager.reloadManagers();
        new ConfigManager();
        if (ListenerManager.listenerManager != null) {
            ListenerManager.listenerManager.reloadEnchantabilityOverrides();
        }
        new LanguageManager();
        new ItemManager();
        new AttributeManager();
        new SkillManager();
        new CommandManager();
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        if (manager != null) {
            manager.reloadDefinitions();
            manager.initializePowers();
        }
        if (TriggerManager.triggerManager != null) {
            TriggerManager.triggerManager.activeEnchantments().reloadMode();
        }
        if (FakeChangeManager.fakeChangeManager != null) {
            FakeChangeManager.fakeChangeManager.reload();
        }
        LanguageManager.languageManager.sendStringText(sender, "plugin.reloaded");
        LanguageManager.languageManager.sendStringText(sender, "plugin.registry-restart-required");
    }
}
