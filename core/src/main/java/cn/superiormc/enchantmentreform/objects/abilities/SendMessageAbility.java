package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.MathUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class SendMessageAbility extends AbstractAbility {

    public SendMessageAbility(ConfigurationSection section) {
        super("SendMessage", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity source = context.source();
        Player hateTarget = context.player();
        List<Player> players = collectPlayers(source, hateTarget, context);
        String[] placeholders = resolveConfiguredPlaceholders(context);
        List<String> messages = resolveMessages(context, placeholders);
        if (messages.isEmpty()) {
            String single = resolveText(section.getString("message", ""), context.player(),
                    context, context.level(), placeholders);
            if (!single.isEmpty()) {
                messages = List.of(single);
            }
        }

        for (Player player : players) {
            for (String message : messages) {
                TextUtil.sendMessage(player, CommonUtil.modifyString(player, message,
                        "player", player.getName(),
                        "target", hateTarget == null ? "" : hateTarget.getName(),
                        "level", String.valueOf(context.level())));
            }
        }
        return false;
    }

    private List<String> resolveMessages(PowerContext context, String[] placeholders) {
        List<String> resolved = new ArrayList<>();
        for (String message : section.getStringList("messages")) {
            resolved.add(resolveText(message, context.player(), context, context.level(), placeholders));
        }
        return resolved;
    }

    private String[] resolveConfiguredPlaceholders(PowerContext context) {
        ConfigurationSection configured = section.getConfigurationSection("placeholders");
        if (configured == null) {
            return new String[0];
        }
        List<String> replacements = new ArrayList<>();
        for (Map.Entry<String, Object> entry : configured.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                continue;
            }
            String value = resolveText(String.valueOf(entry.getValue()), context.player(),
                    context, context.level());
            OptionalDouble calculated = MathUtil.tryCalculate(value);
            replacements.add(entry.getKey());
            replacements.add(calculated.isPresent()
                    ? formatNumber(calculated.getAsDouble())
                    : value);
        }
        return replacements.toArray(String[]::new);
    }

    private String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private List<Player> collectPlayers(Entity source, Player hateTarget, PowerContext context) {
        String mode = getString("mode", "target-player").toLowerCase();
        List<Player> players = new ArrayList<>();
        if (mode.equals("nearby")) {
            if (source == null || source.getWorld() == null) {
                return players;
            }
            double radius = getDouble("radius", 16, context);
            Location location = source.getLocation();
            for (Entity nearby : source.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                if (nearby instanceof Player player && player.isOnline() && !player.isDead()) {
                    players.add(player);
                }
            }
            return players;
        }
        if (hateTarget != null) {
            players.add(hateTarget);
        }
        return players;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
