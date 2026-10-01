package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.methods.DebuildItem;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SubGenerateItemFormat extends AbstractCommand {

    public SubGenerateItemFormat() {
        this.id = "generateitemformat";
        this.requiredPermission =  "enchantmentreform." + id;
        this.onlyInGame = true;
        this.requiredArgLength = new Integer[]{1};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        YamlConfiguration itemConfig = new YamlConfiguration();
        DebuildItem.debuildItem(player.getInventory().getItemInMainHand(), itemConfig);
        String yaml = itemConfig.saveToString();
        SchedulerUtil.runTaskAsynchronously(() -> {
            Path path = new File(EnchantmentReform.instance.getDataFolder(), "generated-item-format.yml").toPath();
            try {
                Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        LanguageManager.languageManager.sendStringText(player, "plugin.generated");
        if (ConfigManager.configManager.getString("debuild-item-method", "LEGACY").equalsIgnoreCase("COMPONENT")) {
            TextUtil.sendMessage(player,  TextUtil.pluginPrefix() + " §fYou are using the modern component item debuilder, this feature is still in early alpha test, " +
                    "if you found any issue, please report it to author. You can still use LEGACY item debuilder by setting debuild-item-method option in config.yml file.");
        }
    }
}
