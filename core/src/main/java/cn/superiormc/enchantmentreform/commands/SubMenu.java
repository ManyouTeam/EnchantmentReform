package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.gui.inv.ConfiguredMenuGUI;
import cn.superiormc.enchantmentreform.gui.inv.AttributeDetailGUI;
import cn.superiormc.enchantmentreform.gui.inv.EnchantmentInfoGUI;
import cn.superiormc.enchantmentreform.gui.inv.SkillDetailGUI;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SubMenu extends AbstractCommand {

    public SubMenu() {
        id = "menu";
        requiredPermission = "enchantmentreform.menu";
        onlyInGame = true;
        requiredArgLength = new Integer[]{2, 3};
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        String menuId = args[1].toLowerCase(Locale.ROOT);
        if (!ConfigManager.configManager.hasMenu(menuId)) {
            TextUtil.sendMessage(player, CommonUtil.parseLang(player,
                    "{lang:menu-not-found}"));
            return;
        }
        if (menuId.equals("enchantment-info")) {
            new EnchantmentInfoGUI(player).openGUI();
            return;
        }
        if (menuId.equals("skill-detail") && args.length == 3) {
            SkillDefinition skill = SkillManager.skillManager == null
                    ? null : SkillManager.skillManager.getSkill(args[2]);
            if (skill != null) {
                new SkillDetailGUI(player, skill, "skill-info").openGUI();
                return;
            }
        }
        if (menuId.equals("attribute-detail") && args.length == 3) {
            ObjectCustomAttribute attribute = AttributeManager.attributeManager == null
                    ? null : AttributeManager.attributeManager.resolveAttribute(args[2]);
            if (attribute != null) {
                new AttributeDetailGUI(player, attribute, "attribute-info").openGUI();
                return;
            }
        }
        if (menuId.equals("skill-detail") || menuId.equals("attribute-detail")) {
            TextUtil.sendMessage(player, CommonUtil.parseLang(player,
                    "{lang:menu-requires-context}"));
            return;
        }
        new ConfiguredMenuGUI(player, menuId).openGUI();
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        if (args.length == 2) {
            return new ArrayList<>(ConfigManager.configManager.getMenuIds());
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("skill-detail")
                && SkillManager.skillManager != null) {
            return SkillManager.skillManager.getSkills().stream()
                    .map(SkillDefinition::id).sorted().toList();
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("attribute-detail")
                && AttributeManager.attributeManager != null) {
            return AttributeManager.attributeManager.getAttributes().stream()
                    .map(ObjectCustomAttribute::getId).sorted().toList();
        }
        return new ArrayList<>();
    }
}
