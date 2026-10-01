package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.managers.ItemManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SubSaveItem extends AbstractCommand {

    public SubSaveItem() {
        this.id = "saveitem";
        this.requiredPermission =  "enchantmentreform." + id;
        this.onlyInGame = true;
        this.requiredArgLength = new Integer[]{2, 3};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        boolean saved = false;
        if (args.length == 2 || args[args.length - 1].equalsIgnoreCase("bukkit")) {
            saved = ItemManager.itemManager.saveMainHandItem(player, args[1]);
        } else if (args[args.length - 1].equalsIgnoreCase("itemformat")) {
            saved = ItemManager.itemManager.saveMainHandItemFormat(player, args[1]);
        }
        if (saved) {
            LanguageManager.languageManager.sendStringText(player, "plugin.saved");
        }
    }

    @Override
    public List<String> getTabResult(String[] args, Player player) {
        List<String> tempVal1 = new ArrayList<>();
        switch (args.length) {
            case 2:
                tempVal1.add(LanguageManager.languageManager.getStringText(player, "command-tab.type-item-id"));
                break;
            case 3:
                tempVal1.add("bukkit");
                tempVal1.add("itemformat");
        }
        return tempVal1;
    }
}
