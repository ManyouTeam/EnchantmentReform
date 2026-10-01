package cn.superiormc.enchantmentreform.api.trigger;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TriggerData {

    private final Player player;

    private final Entity sourceEntity;

    private final Entity skillEntity;

    private final Entity targetEntity;

    private final Block block;

    private final Location location;

    private final Location from;

    private final Location to;

    private final Event event;

    private final ItemStack triggerItem;

    private final EquipmentSlot triggerItemSlot;

    private final long tick;

    private final Map<ContextKey<?>, Object> extras;

    private TriggerData(Builder builder) {
        player = Objects.requireNonNull(builder.player, "player");
        sourceEntity = builder.sourceEntity;
        skillEntity = builder.skillEntity;
        targetEntity = builder.targetEntity;
        block = builder.block;
        location = cloneLocation(builder.location);
        from = cloneLocation(builder.from);
        to = cloneLocation(builder.to);
        event = builder.event;
        triggerItem = builder.triggerItem;
        triggerItemSlot = builder.triggerItemSlot;
        tick = builder.tick;
        extras = Map.copyOf(builder.extras);
    }

    public static Builder builder(Player player) {
        return new Builder(player);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public TriggerData withTarget(Entity target) {
        return toBuilder().target(target).build();
    }

    public TriggerData withBlock(Block value) {
        Builder builder = toBuilder().block(value);
        if (value != null) {
            builder.location(value.getLocation().add(0.5D, 0.5D, 0.5D));
        }
        return builder.build();
    }

    public Player player() {
        return player;
    }

    public Entity sourceEntity() {
        return sourceEntity;
    }

    public Entity skillEntity() {
        return skillEntity;
    }

    public Entity targetEntity() {
        return targetEntity;
    }

    public Block block() {
        return block;
    }

    public Location location() {
        return cloneLocation(location);
    }

    public Location from() {
        return cloneLocation(from);
    }

    public Location to() {
        return cloneLocation(to);
    }

    public Event event() {
        return event;
    }

    public ItemStack triggerItem() {
        return triggerItem;
    }

    public EquipmentSlot triggerItemSlot() {
        return triggerItemSlot;
    }

    public long tick() {
        return tick;
    }

    public <T> Optional<T> extra(ContextKey<T> key) {
        Object value = extras.get(key);
        return value == null ? Optional.empty() : Optional.of(key.type().cast(value));
    }

    public Entity resolveEntity(EntitySelector selector) {
        return switch (selector) {
            case PLAYER -> player;
            case SOURCE -> sourceEntity;
            case SKILL -> skillEntity;
            case TARGET -> targetEntity;
        };
    }

    private static Location cloneLocation(Location location) {
        return location == null ? null : location.clone();
    }

    public static final class Builder {

        private final Player player;
        private Entity sourceEntity;
        private Entity skillEntity;
        private Entity targetEntity;
        private Block block;
        private Location location;
        private Location from;
        private Location to;
        private Event event;
        private ItemStack triggerItem;
        private EquipmentSlot triggerItemSlot;
        private long tick;
        private final Map<ContextKey<?>, Object> extras = new LinkedHashMap<>();

        private Builder(Player player) {
            this.player = Objects.requireNonNull(player, "player");
        }

        private Builder(TriggerData data) {
            player = data.player;
            sourceEntity = data.sourceEntity;
            skillEntity = data.skillEntity;
            targetEntity = data.targetEntity;
            block = data.block;
            location = data.location;
            from = data.from;
            to = data.to;
            event = data.event;
            triggerItem = data.triggerItem;
            triggerItemSlot = data.triggerItemSlot;
            tick = data.tick;
            extras.putAll(data.extras);
        }

        public Builder source(Entity entity) {
            sourceEntity = entity;
            return this;
        }

        public Builder skill(Entity entity) {
            skillEntity = entity;
            return this;
        }

        public Builder target(Entity entity) {
            targetEntity = entity;
            return this;
        }

        public Builder entities(Entity source, Entity skill, Entity target) {
            sourceEntity = source;
            skillEntity = skill;
            targetEntity = target;
            return this;
        }

        public Builder block(Block value) {
            block = value;
            return this;
        }

        public Builder location(Location value) {
            location = value;
            return this;
        }

        public Builder movement(Location original, Location destination) {
            from = original;
            to = destination;
            return this;
        }

        public Builder event(Event value) {
            event = value;
            return this;
        }

        public Builder triggerItem() {
            EquipmentSlot hand = player.getActiveItemHand();
            triggerItem = hand == EquipmentSlot.OFF_HAND
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItemInMainHand();
            triggerItemSlot = hand;
            return this;
        }

        public Builder triggerItem(ItemStack value) {
            triggerItem = value;
            EquipmentSlot hand = player.getActiveItemHand();
            if (ConfigManager.configManager.getBoolean("strict-active-slot-check") &&
                    !player.getInventory().getItem(hand).isSimilar(triggerItem)) {
                if (hand == EquipmentSlot.OFF_HAND) {
                    triggerItemSlot = EquipmentSlot.HAND;
                } else {
                    triggerItemSlot = EquipmentSlot.OFF_HAND;
                }
            } else {
                triggerItemSlot = hand;
            }
            return this;
        }

        public Builder triggerItem(ItemStack value, EquipmentSlot slot) {
            triggerItem = value;
            triggerItemSlot = slot;
            return this;
        }

        public Builder tick(long value) {
            tick = value;
            return this;
        }

        public <T> Builder extra(ContextKey<T> key, T value) {
            Objects.requireNonNull(key, "key").validate(value);
            if (value == null) {
                extras.remove(key);
            } else {
                extras.put(key, value);
            }
            return this;
        }

        public TriggerData build() {
            return new TriggerData(this);
        }
    }
}
