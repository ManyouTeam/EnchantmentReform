package cn.superiormc.enchantmentreform.gui.action;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.gui.inv.AttributeDetailGUI;
import cn.superiormc.enchantmentreform.gui.inv.ConfiguredMenuGUI;
import cn.superiormc.enchantmentreform.gui.inv.EnchantmentInfoGUI;
import cn.superiormc.enchantmentreform.gui.inv.SkillDetailGUI;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.actions.AbstractRunAction;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.entity.Player;

public final class ActionGUIPage extends AbstractRunAction<ObjectSingleGUIAction, GUIActionContext> {

    private final int pageOffset;

    public ActionGUIPage(String type, int pageOffset) {
        super(type);
        this.pageOffset = pageOffset;
        if ("command".equals(type)) {
            setRequiredArgs("command");
        }
    }

    @Override
    protected void onDoAction(ObjectSingleGUIAction singleAction,
                              Player player,
                              GUIActionContext context) {
        if (context == null) {
            return;
        }
        switch (getType()) {
            case "previous_page", "next_page", "refresh" -> {
                context.gui().openPage(context.gui().getCurrentPage() + pageOffset);
            }
            case "open_menu" -> {
                openMenu(singleAction, player, context);
            }
            case "open_skill" -> {
                if (context.skill() != null) {
                    new SkillDetailGUI(player, context.skill(), context.returnMenuId())
                            .openGUI(context.bypassPermission());
                }
            }
            case "open_attribute" -> {
                if (context.attribute() != null) {
                    new AttributeDetailGUI(player, context.attribute(), context.skill(), context.returnMenuId())
                            .openGUI(context.bypassPermission());
                }
            }
            case "allocate_attribute" -> {
                allocateAttribute(singleAction, player, context);
            }
            case "command" -> {
                String command = singleAction.getString("command", player, context);
                if (!command.isBlank()) {
                    if (singleAction.getBoolean("as-console", true)) {
                        EnchantmentReform.methodUtil.dispatchCommand(command);
                    } else {
                        EnchantmentReform.methodUtil.dispatchCommand(player, command);
                    }
                }
            }
            default -> {
            }
        }
    }

    private void openMenu(ObjectSingleGUIAction singleAction, Player player, GUIActionContext context) {
        String targetId = singleAction.contains("menu")
                ? singleAction.getString("menu", player, context) : "main";
        if (!ConfigManager.configManager.hasMenu(targetId)) {
            TextUtil.sendMessage(player, CommonUtil.parseLang(player, "{lang:menu-not-found}"));
            return;
        }
        if ("enchantment-info".equalsIgnoreCase(targetId)) {
            new EnchantmentInfoGUI(player).openGUI(context.bypassPermission());
        } else {
            new ConfiguredMenuGUI(player, targetId).openGUI(context.bypassPermission());
        }
    }

    private void allocateAttribute(ObjectSingleGUIAction singleAction, Player player, GUIActionContext context) {
        ObjectCustomAttribute attribute = context.attribute();
        if (attribute == null || SkillManager.skillManager == null) {
            return;
        }
        int add = Math.max(1, singleAction.getInt("add", 1));
        int price = attribute.getAllocationPrice(attribute.getBaseValue(player), add);
        SkillManager.AllocationResult result = SkillManager.skillManager.allocate(player, attribute.getId(), add);
        String path = switch (result) {
            case SUCCESS -> "skills.allocation-messages.success";
            case NOT_ENOUGH_POINTS -> "skills.allocation-messages.not-enough-points";
            case MAXIMUM_REACHED -> "skills.allocation-messages.maximum-reached";
            case CONDITION_NOT_MET -> "skills.allocation-messages.condition-not-met";
            default -> "skills.allocation-messages.invalid";
        };
        TextUtil.sendMessage(player, CommonUtil.modifyString(player,
                ConfigManager.configManager.getString(path, "&cCould not allocate points."),
                "attribute", attribute.getLocalizedName(player), "add", String.valueOf(add),
                "price", String.valueOf(price),
                "points", String.valueOf(SkillManager.skillManager.getAttributePoints(player))));
        context.gui().openPage(context.gui().getCurrentPage());
    }
}
