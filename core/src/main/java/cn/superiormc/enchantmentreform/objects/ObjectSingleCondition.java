package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ObjectSingleCondition extends AbstractConfiguredSection<PowerContext> {

    private final PowerContext context;

    private final Player player;

    public ObjectSingleCondition(ConfigurationSection section, PowerContext context) {
        this(section, context, context == null ? null : context.player());
    }

    public ObjectSingleCondition(ConfigurationSection section, PowerContext context, Player player) {
        super(section);
        this.context = context;
        this.player = player;
    }

    public ObjectSingleCondition withSection(ConfigurationSection section) {
        return new ObjectSingleCondition(section, context, player);
    }

    public String parsePlaceholder(String value) {
        return replacePlaceholder(value, player(), context);
    }

    public List<String> parsePlaceholder(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(parsePlaceholder(value));
        }
        return result;
    }

    public Player sourcePlayer() {
        return context == null ? null : context.player();
    }

    public Player player() {
        return context == null ? player : context.player();
    }

    public PowerContext getContext() {
        return context;
    }

    @Override
    protected String replacePlaceholder(String content, Player player, PowerContext context) {
        if (content == null) {
            return "";
        }
        int level = context == null ? 1 : context.level();
        return TextUtil.withPAPI(
                CommonUtil.modifyString(player, content, "level", String.valueOf(level)), player);
    }

}
