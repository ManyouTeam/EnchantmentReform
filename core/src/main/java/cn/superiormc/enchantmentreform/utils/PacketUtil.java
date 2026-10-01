package cn.superiormc.enchantmentreform.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketUtil {

    private static final Map<UUID, Integer> interactionSequences = new ConcurrentHashMap<>();

    public static void rightClick(Player player, InteractionHand hand) {
        Location location = player.getLocation();

        int sequence = interactionSequences.merge(
                player.getUniqueId(),
                1,
                (oldValue, value) -> oldValue == Integer.MAX_VALUE
                        ? 0
                        : oldValue + 1
        );

        WrapperPlayClientUseItem packet =
                new WrapperPlayClientUseItem(
                        hand,
                        sequence,
                        location.getYaw(),
                        location.getPitch()
                );

        PacketEvents.getAPI()
                .getPlayerManager()
                .receivePacket(player, packet);
    }
}
