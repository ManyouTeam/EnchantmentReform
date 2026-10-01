package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CommandManager extends AbstractManager {

    public static CommandManager commandManager;

    private final Map<String, AbstractCommand> registeredCommands = new LinkedHashMap<>();

    public CommandManager() {
        commandManager = this;
        registerObjectCommand();
        registerBukkitCommand();
    }

    private void registerBukkitCommand() {
        PluginCommand command = Bukkit.getPluginCommand("enchantmentreform");
        if (command == null) {
            return;
        }
        command.setExecutor(new MainCommand());
        command.setTabCompleter(new MainCommandTab());
    }

    private void registerObjectCommand() {
        registeredCommands.put("menu", new SubMenu());
        registeredCommands.put("reload", new SubReload());
        registeredCommands.put("help", new SubHelp());
        registeredCommands.put("saveitem", new SubSaveItem());
        registeredCommands.put("givesaveitem", new SubGiveSaveItem());
        registeredCommands.put("generateitemformat", new SubGenerateItemFormat());
        registeredCommands.put("setattribute", new SubSetAttribute());
        registeredCommands.put("addattribute", new SubAddAttribute());
        registeredCommands.put("addattributemodifier", new SubAddAttributeModifier());
        registeredCommands.put("setattributemodifier", new SubSetAttributeModifier());
        if (ConfigManager.configManager.getBoolean("modules.skills", true)) {
            registeredCommands.put("setskillmultiplier", new SubSetSkillMultiplier());
            registeredCommands.put("setskillxp", new SubSkillProgress(
                    SubSkillProgress.Operation.SET_EXPERIENCE));
            registeredCommands.put("addskillxp", new SubSkillProgress(
                    SubSkillProgress.Operation.ADD_EXPERIENCE));
            registeredCommands.put("setskilllevel", new SubSkillProgress(
                    SubSkillProgress.Operation.SET_LEVEL));
            registeredCommands.put("addskilllevel", new SubSkillProgress(
                    SubSkillProgress.Operation.ADD_LEVEL));
        }
    }

    public Map<String, AbstractCommand> getSubCommandsMap() {
        return registeredCommands;
    }
}
