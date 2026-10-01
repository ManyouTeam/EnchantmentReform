package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ExecuteCommandAbility extends AbstractAbility {

    public ExecuteCommandAbility(ConfigurationSection section) {
        super("ExecuteCommand", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity source = context.source();
        Player hateTarget = resolveHateTargetPlayer(context);
        List<Player> players = collectPlayers(source, hateTarget, context);
        List<String> commands = getStringList("commands");
        if (commands.isEmpty()) {
            String single = getString("command", "");
            if (!single.isEmpty()) {
                commands = List.of(single);
            }
        }

        boolean asConsole = getBoolean("as-console", true);
        for (Player player : players) {
            for (String command : commands) {
                String parsed = CommonUtil.modifyString(player, command,
                        "player", player.getName(),
                        "target", hateTarget == null ? "" : hateTarget.getName(),
                        "level", String.valueOf(context.level()));
                parsed = parsed.startsWith("/") ? parsed.substring(1) : parsed;
                if (asConsole) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                } else {
                    player.performCommand(parsed);
                }
            }
        }
        return false;
    }

    private Player resolveHateTargetPlayer(PowerContext context) {
        Entity source = context.source();
        if (!(source instanceof Mob mob)) {
            return null;
        }
        if (mob.getTarget() instanceof Player player && player.isOnline() && !player.isDead()) {
            return player;
        }
        return null;
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
