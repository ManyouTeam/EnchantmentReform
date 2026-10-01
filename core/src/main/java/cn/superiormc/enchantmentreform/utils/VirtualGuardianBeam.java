package cn.superiormc.enchantmentreform.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VirtualGuardianBeam {

    private static final double VIEW_DISTANCE_SQUARED = 96.0 * 96.0;

    private static PacketBridge packetBridge;

    private static boolean packetBridgeLoadFailed;

    private VirtualGuardianBeam() {
    }

    public static boolean start(LivingEntity caster,
                                LivingEntity victim,
                                int chargeTicks,
                                double range,
                                Runnable onComplete) {
        PacketBridge bridge = getPacketBridge();
        if (bridge == null) {
            return false;
        }

        BeamTask task = new BeamTask(bridge, caster, victim, Math.max(1, chargeTicks), range, onComplete);
        task.start();
        return true;
    }

    private static PacketBridge getPacketBridge() {
        if (packetBridge != null) {
            return packetBridge;
        }
        if (packetBridgeLoadFailed) {
            return null;
        }

        try {
            packetBridge = new PacketBridge();
            return packetBridge;
        } catch (RuntimeException | LinkageError error) {
            packetBridgeLoadFailed = true;
            error.printStackTrace();
            return null;
        }
    }

    private static final class BeamTask implements Runnable {

        private final PacketBridge bridge;
        private final LivingEntity caster;
        private final LivingEntity victim;
        private final int chargeTicks;
        private final double rangeSquared;
        private final Runnable onComplete;
        private final int entityId;
        private final UUID uuid;
        private final Set<Player> viewers = new HashSet<>();

        private SchedulerUtil task;
        private Location lastLocation;
        private int ticks;

        private BeamTask(PacketBridge bridge,
                         LivingEntity caster,
                         LivingEntity victim,
                         int chargeTicks,
                         double range,
                         Runnable onComplete) {
            this.bridge = bridge;
            this.caster = caster;
            this.victim = victim;
            this.chargeTicks = chargeTicks;
            this.rangeSquared = range * range;
            this.onComplete = onComplete;
            this.entityId = ThreadLocalRandom.current().nextInt(2000000000, Integer.MAX_VALUE);
            this.uuid = UUID.randomUUID();
        }

        private void start() {
            task = SchedulerUtil.runTaskTimer(caster, this, 1L, 1L);
        }

        @Override
        public void run() {
            if (!isStillValid()) {
                stopBeam();
                return;
            }

            Location currentLocation = caster.getEyeLocation().subtract(0.0, 0.5, 0.0);
            updateViewers(currentLocation);
            moveBeam(currentLocation);
            lastLocation = currentLocation.clone();

            ticks++;
            if (ticks >= chargeTicks) {
                stopBeam();
                if (onComplete != null && isStillValid()) {
                    onComplete.run();
                }
            }
        }

        private boolean isStillValid() {
            return caster.isValid()
                    && victim.isValid()
                    && !caster.isDead()
                    && !victim.isDead()
                    && caster.getWorld().equals(victim.getWorld())
                    && caster.getLocation().distanceSquared(victim.getLocation()) <= rangeSquared;
        }

        private void updateViewers(Location currentLocation) {
            Set<Player> currentViewers = new HashSet<>();
            World world = currentLocation.getWorld();
            if (world == null) {
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(world)) {
                    continue;
                }
                if (player.getLocation().distanceSquared(currentLocation) > VIEW_DISTANCE_SQUARED
                        && player.getLocation().distanceSquared(victim.getLocation()) > VIEW_DISTANCE_SQUARED) {
                    continue;
                }

                currentViewers.add(player);
                if (!viewers.contains(player)) {
                    bridge.spawnGuardian(player, entityId, uuid, currentLocation, victim.getEntityId());
                }
            }

            for (Player viewer : new HashSet<>(viewers)) {
                if (!currentViewers.contains(viewer)) {
                    bridge.destroyEntity(viewer, entityId);
                }
            }

            viewers.clear();
            viewers.addAll(currentViewers);
        }

        private void moveBeam(Location currentLocation) {
            if (lastLocation == null) {
                return;
            }

            double deltaX = currentLocation.getX() - lastLocation.getX();
            double deltaY = currentLocation.getY() - lastLocation.getY();
            double deltaZ = currentLocation.getZ() - lastLocation.getZ();
            if (Math.abs(deltaX) > 7.5 || Math.abs(deltaY) > 7.5 || Math.abs(deltaZ) > 7.5) {
                for (Player viewer : viewers) {
                    bridge.destroyEntity(viewer, entityId);
                    bridge.spawnGuardian(viewer, entityId, uuid, currentLocation, victim.getEntityId());
                }
                return;
            }

            for (Player viewer : viewers) {
                bridge.moveEntity(viewer, entityId, deltaX, deltaY, deltaZ);
            }
        }

        private void stopBeam() {
            for (Player viewer : viewers) {
                bridge.destroyEntity(viewer, entityId);
            }
            viewers.clear();
            if (task != null) {
                task.cancel();
            }
        }
    }

    private static final class PacketBridge {

        private final PacketEventsAPI<?> packetEvents;

        private PacketBridge() {
            packetEvents = PacketEvents.getAPI();
        }

        private void spawnGuardian(Player player, int entityId, UUID uuid, Location location, int targetEntityId) {
            GuardianMetadataIndexes indexes = GuardianMetadataIndexes.from(packetEvents.getPlayerManager().getClientVersion(player));
            if (indexes == null) {
                return;
            }

            sendPacket(player, new WrapperPlayServerSpawnEntity(
                    entityId,
                    Optional.of(uuid),
                    EntityTypes.GUARDIAN,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    location.getPitch(),
                    location.getYaw(),
                    location.getYaw(),
                    0,
                    Optional.of(Vector3d.zero())
            ));
            sendPacket(player, createMetadataPacket(entityId, targetEntityId, indexes));
        }

        private void destroyEntity(Player player, int entityId) {
            sendPacket(player, new WrapperPlayServerDestroyEntities(entityId));
        }

        private void moveEntity(Player player, int entityId, double deltaX, double deltaY, double deltaZ) {
            sendPacket(player, new WrapperPlayServerEntityRelativeMove(entityId, deltaX, deltaY, deltaZ, false));
        }

        private WrapperPlayServerEntityMetadata createMetadataPacket(int entityId,
                                                                     int targetEntityId,
                                                                     GuardianMetadataIndexes indexes) {
            List<EntityData<?>> metadata = new ArrayList<>();
            metadata.add(new EntityData<>(indexes.sharedFlagsIndex(), EntityDataTypes.BYTE, (byte) 0x20));
            metadata.add(new EntityData<>(indexes.attackTargetIndex(), EntityDataTypes.INT, targetEntityId));
            if (indexes.movingType() == GuardianMovingMetadataType.BOOLEAN) {
                metadata.add(new EntityData<>(indexes.movingIndex(), EntityDataTypes.BOOLEAN, false));
            } else {
                metadata.add(new EntityData<>(indexes.movingIndex(), EntityDataTypes.INT, 0));
            }
            return new WrapperPlayServerEntityMetadata(entityId, metadata);
        }

        private void sendPacket(Player player, PacketWrapper<?> packet) {
            packetEvents.getPlayerManager().sendPacket(player, packet);
        }
    }

    private enum GuardianMovingMetadataType {
        BOOLEAN,
        INT
    }

    private record GuardianMetadataIndexes(int sharedFlagsIndex,
                                           int movingIndex,
                                           int attackTargetIndex,
                                           GuardianMovingMetadataType movingType) {

        private static GuardianMetadataIndexes from(ClientVersion version) {
            if (version == null
                    || version == ClientVersion.UNKNOWN
                    || version == ClientVersion.LOWER_THAN_SUPPORTED_VERSIONS
                    || version.isOlderThan(ClientVersion.V_1_8)) {
                return null;
            }

            if (version.isOlderThan(ClientVersion.V_1_9)) {
                return new GuardianMetadataIndexes(0, 16, 17, GuardianMovingMetadataType.INT);
            }
            if (version.isOlderThan(ClientVersion.V_1_14)) {
                return new GuardianMetadataIndexes(0, 12, 13, GuardianMovingMetadataType.BOOLEAN);
            }
            if (version.isOlderThan(ClientVersion.V_1_15)) {
                return new GuardianMetadataIndexes(0, 14, 15, GuardianMovingMetadataType.BOOLEAN);
            }
            if (version.isOlderThan(ClientVersion.V_1_17)) {
                return new GuardianMetadataIndexes(0, 15, 16, GuardianMovingMetadataType.BOOLEAN);
            }
            return new GuardianMetadataIndexes(0, 16, 17, GuardianMovingMetadataType.BOOLEAN);
        }
    }
}
