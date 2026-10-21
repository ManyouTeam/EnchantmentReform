package cn.superiormc.enchantmentreform.commands;

import cn.superiormc.enchantmentreform.gui.inv.ConfiguredMenuGUI;
import cn.superiormc.enchantmentreform.gui.inv.AttributeDetailGUI;
import cn.superiormc.enchantmentreform.gui.inv.EnchantmentInfoGUI;
import cn.superiormc.enchantmentreform.gui.inv.SkillDetailGUI;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SubMenu extends AbstractCommand {

    public SubMenu() {
        this.id = "menu";
        this.requiredPermission = "enchantmentreform.menu";
        this.onlyInGame = false;
        this.requiredArgLength = new Integer[]{1, 2, 3};
        this.requiredConsoleArgLength = new Integer[]{2, 3, 4, 5};
    }

    @Override
    public void executeCommandInConsole(String[] args) {
        boolean bypass = args[args.length - 1].equalsIgnoreCase("-b");
        int length = args.length - (bypass ? 1 : 0);
        if (length < 2 || length > 4) {
            LanguageManager.languageManager.sendStringText("error.menu-console-usage");
            return;
        }
        String menuId = length == 2 ? "main" : args[1].toLowerCase(Locale.ROOT);
        String playerName = length == 2 ? args[1] : args[2];
        String context = length == 4 ? args[3] : null;
        if (context != null && !menuId.equals("skill-detail") && !menuId.equals("attribute-detail")) {
            LanguageManager.languageManager.sendStringText("error.menu-console-usage");
            return;
        }
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            LanguageManager.languageManager.sendStringText("error.player-not-found", "player", playerName);
            return;
        }
        SchedulerUtil.runSync(target, () -> {
            if (!target.isOnline()) {
                LanguageManager.languageManager.sendStringText("error.player-not-found", "player", playerName);
                return;
            }
            open(target, menuId, context, bypass);
        });
    }

    @Override
    public void executeCommandInGame(String[] args, Player player) {
        open(player, args.length == 1 ? "main" : args[1].toLowerCase(Locale.ROOT), args.length == 3 ? args[2] : null, false);
    }

    private void open(Player player, String menuId, String context, boolean bypass) {
        if (!ConfigManager.configManager.hasMenu(menuId)) {
            TextUtil.sendMessage(player, CommonUtil.parseLang(player,
                    "{lang:menu-not-found}"));
            return;
        }
        if (menuId.equals("enchantment-info")) {
            new EnchantmentInfoGUI(player).openGUI(bypass);
            return;
        }
        if (menuId.equals("skill-detail") && context != null) {
            SkillDefinition skill = SkillManager.skillManager == null
                    ? null : SkillManager.skillManager.getSkill(context);
            if (skill != null) {
                new SkillDetailGUI(player, skill, "skill-info").openGUI(bypass);
                return;
            }
        }
        if (menuId.equals("attribute-detail") && context != null) {
            ObjectCustomAttribute attribute = AttributeManager.attributeManager == null
                    ? null : AttributeManager.attributeManager.resolveAttribute(context);
            if (attribute != null) {
                new AttributeDetailGUI(player, attribute, "attribute-info").openGUI(bypass);
                return;
            }
        }
        if (menuId.equals("skill-detail") || menuId.equals("attribute-detail")) {
            TextUtil.sendMessage(player, CommonUtil.parseLang(player,
                    "{lang:menu-requires-context}"));
            return;
        }
        new ConfiguredMenuGUI(player, menuId).openGUI(bypass);
    }

    @Override
    protected List<String> getTabResult(String[] args, Player player) {
        if (args.length == 2) {
            List<String> menus = new ArrayList<>(ConfigManager.configManager.getMenuIds());
            menus.removeIf(menu -> !ConfigManager.configManager.getMenu(menu).getBoolean("enabled", true));
            return menus;
        }
        if (args.length >= 3
                && !ConfigManager.configManager.getMenu(args[1]).getBoolean("enabled", true)) {
            return new ArrayList<>();
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

    @Override
    protected List<String> getConsoleTabResult(String[] args) {
        if (args.length == 2) {
            List<String> result = new ArrayList<>(getTabResult(args, null));
            Bukkit.getOnlinePlayers().forEach(player -> result.add(player.getName()));
            return result;
        }
        if (args.length == 3) {
            if (ConfigManager.configManager.hasMenu(args[1])) {
                return ConfigManager.configManager.getMenu(args[1]).getBoolean("enabled", true)
                        ? Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().toList() : List.of();
            }
            return Bukkit.getPlayerExact(args[1]) != null ? List.of("-b") : List.of();
        }
        if (args.length < 4 || !ConfigManager.configManager.hasMenu(args[1])
                || !ConfigManager.configManager.getMenu(args[1]).getBoolean("enabled", true)) {
            return List.of();
        }
        if (args.length == 4) {
            List<String> result = new ArrayList<>(getTabResult(new String[]{args[0], args[1], args[3]}, null));
            result.add("-b");
            return result;
        }
        if (args.length == 5 && (args[1].equalsIgnoreCase("skill-detail") || args[1].equalsIgnoreCase("attribute-detail"))) {
            return List.of("-b");
        }
        return new ArrayList<>();
    }
}
