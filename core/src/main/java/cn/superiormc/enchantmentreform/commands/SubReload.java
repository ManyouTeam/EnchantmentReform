package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.AbstractManager;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.CommandManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.managers.FakeChangeManager;
import cn.superiormc.enchantmentreform.managers.ItemManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
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
        new ConfigManager();
        new LanguageManager();
        AbstractManager.reloadManagers();
        new ItemManager();
        new AttributeManager();
        new SkillManager();
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();
        if (manager != null) {
            manager.reloadDefinitions();
            manager.initializePowers();
        }
        new CommandManager();
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
