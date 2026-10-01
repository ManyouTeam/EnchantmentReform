package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AttributeLayerAbility extends AbstractAbility {

    private static final Map<Key, ActiveLayers> ACTIVE = new ConcurrentHashMap<>();

    public AttributeLayerAbility(org.bukkit.configuration.ConfigurationSection section) {
        super("AttributeLayer", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof Player player) || AttributeManager.attributeManager == null) {
            return false;
        }
        String attributeId = section.getString("attribute", "absorption");
        ObjectCustomAttribute attribute = AttributeManager.attributeManager.resolveAttribute(
                attributeId);
        if (attribute == null) {
            return false;
        }
        Key key = new Key(player.getUniqueId(), attribute.getId());
        int capacity = Math.max(0, attribute.getValue(player));
        String operation = section.getString("operation", "ADD").toUpperCase();
        if (operation.equals("EXECUTE")) {
            return capacity > 0 && AbilityManager.abilityManager.execute(
                    section.getConfigurationSection("abilities"),
                    context.withLevel(capacity));
        }
        if (operation.equals("SYNC")) {
            sync(key, player, capacity);
            return false;
        }
        if (capacity == 0) {
            clear(key, ACTIVE.remove(key));
            return false;
        }

        int added = Math.max(0, getInt("amount", 1, context));
        double healthPerLayer = Math.max(0.0D,
                getDouble("health-per-layer", 1.0D, context));
        long duration = Math.max(2L, getInt("duration", 140, context));
        long decayInterval = Math.max(1L,
                getInt("decay-interval", 20, context));
        ActiveLayers previous = ACTIVE.get(key);
        int oldLayers = previous == null ? 0 : Math.min(previous.layers, capacity);
        int layers = Math.min(capacity, oldLayers + added);
        long generation = previous == null ? 1L : previous.generation + 1L;
        UUID modifierId = previous == null
                ? modifierId(key) : previous.modifierId;
        int increase = layers - oldLayers;
        ActiveLayers active = new ActiveLayers(generation, layers, healthPerLayer,
                decayInterval, modifierId, new WeakReference<>(player));
        ACTIVE.put(key, active);
        applyMaximum(player, active);

        if (increase > 0) {
            double current = player.getAbsorptionAmount();
            double maximum = CommonUtil.getMaxAbsorption(player);
            player.setAbsorptionAmount(Math.min(maximum,
                    current + increase * healthPerLayer));
            AbilityManager.abilityManager.execute(
                    section.getConfigurationSection("on-increase"),
                    withLayerContext(context, oldLayers, layers));
        } else {
            AbilityManager.abilityManager.execute(
                    section.getConfigurationSection("on-maximum"),
                    withLayerContext(context, oldLayers, layers));
        }

        SchedulerUtil.runTaskLater(player,
                () -> decayLayer(key, player, generation), duration);
        return false;
    }

    private static void decayLayer(Key key, Player player, long generation) {
        ActiveLayers current = ACTIVE.get(key);
        if (current == null || current.generation != generation) {
            return;
        }
        if (current.layers <= 1) {
            if (ACTIVE.remove(key, current)) {
                clear(key, current);
            }
            return;
        }
        ActiveLayers updated = new ActiveLayers(current.generation,
                current.layers - 1, current.healthPerLayer, current.decayInterval,
                current.modifierId,
                new WeakReference<>(player));
        if (!ACTIVE.replace(key, current, updated)) {
            return;
        }
        applyMaximum(player, updated);
        player.setAbsorptionAmount(Math.min(
                CommonUtil.getMaxAbsorption(player), player.getAbsorptionAmount()));
        SchedulerUtil.runTaskLater(player,
                () -> decayLayer(key, player, generation), current.decayInterval);
    }

    private static void sync(Key key, Player player, int capacity) {
        ActiveLayers current = ACTIVE.get(key);
        if (current == null) {
            return;
        }
        if (capacity <= 0) {
            clear(key, current);
            ACTIVE.remove(key, current);
            return;
        }
        int layers = Math.min(current.layers, capacity);
        ActiveLayers synced = layers == current.layers ? current
                : new ActiveLayers(current.generation, layers,
                current.healthPerLayer, current.decayInterval, current.modifierId,
                new WeakReference<>(player));
        if (synced != current) {
            ACTIVE.replace(key, current, synced);
        }
        applyMaximum(player, synced);
        player.setAbsorptionAmount(Math.min(
                CommonUtil.getMaxAbsorption(player), player.getAbsorptionAmount()));
    }

    private PowerContext withLayerContext(PowerContext context, int previous, int current) {
        TriggerData data = context.triggerData();
        if (data == null) {
            return context;
        }
        return context.withTriggerData(data.toBuilder()
                .extra(BuiltinContextKeys.STATE_PREVIOUS, (double) previous)
                .extra(BuiltinContextKeys.STATE_CURRENT, (double) current)
                .build());
    }

    private static UUID modifierId(Key key) {
        String identity = "enchantmentreform:attribute_layer/" + key.playerId
                + "/" + key.attributeId;
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }

    private static void applyMaximum(Player player, ActiveLayers active) {
        AttributeInstance instance = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (instance == null) {
            return;
        }
        instance.getModifiers().stream()
                .filter(modifier -> modifier.getUniqueId().equals(active.modifierId))
                .findFirst().ifPresent(instance::removeModifier);
        instance.addModifier(new AttributeModifier(active.modifierId,
                "enchantmentreform:attribute_layer",
                active.layers * active.healthPerLayer,
                AttributeModifier.Operation.ADD_NUMBER));
    }

    private static void clear(Key key, ActiveLayers active) {
        if (active == null) {
            return;
        }
        Player player = active.player.get();
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (instance != null) {
            instance.getModifiers().stream()
                    .filter(modifier -> modifier.getUniqueId().equals(active.modifierId))
                    .findFirst().ifPresent(instance::removeModifier);
        }
        player.setAbsorptionAmount(Math.min(
                CommonUtil.getMaxAbsorption(player), player.getAbsorptionAmount()));
    }

    @Override
    public void onEntityUnload(UUID entityId) {
        if (entityId == null) {
            return;
        }
        ACTIVE.entrySet().removeIf(entry -> {
            if (!entityId.equals(entry.getKey().playerId)) {
                return false;
            }
            clear(entry.getKey(), entry.getValue());
            return true;
        });
    }

    @Override
    public void onUnload() {
        ACTIVE.forEach(AttributeLayerAbility::clear);
        ACTIVE.clear();
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private record Key(UUID playerId, String attributeId) {
    }

    private record ActiveLayers(long generation, int layers, double healthPerLayer,
                                long decayInterval,
                                UUID modifierId, WeakReference<Player> player) {
    }
}
