package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.objects.ObjectPower;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import org.bukkit.entity.Entity;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PowerManager extends AbstractManager {

    public static PowerManager powerManager;

    private final Map<String, ObjectPower> powers = new ConcurrentHashMap<>();

    public PowerManager() {
        powerManager = this;
    }

    public void markUsedPower(Entity entity) {
        if (entity != null) {
            PowerStateStore.Key key = new PowerStateStore.Key(
                    entity.getUniqueId(), "global", "used-power", "mark", null);
            PowerStateStore.set(key, 1.0D, 1.0D, 10.0D);
        }
    }

    public boolean isUsedPower(Entity entity) {
        PowerStateStore.Key key = new PowerStateStore.Key(entity.getUniqueId(), "global", "used-power", "mark", null);
        return PowerStateStore.consume(key) > 0.0D;
    }

    public void markInternalBlockBreak(Block block) {
        if (block != null) {
            PowerStateStore.set(internalBlockKey(block), 1.0D, 1.0D, 10.0D);
        }
    }

    public boolean consumeInternalBlockBreak(Block block) {
        if (block == null) {
            return false;
        }
        return PowerStateStore.consume(internalBlockKey(block)) > 0.0D;
    }

    public void clearInternalBlockBreak(Block block) {
        if (block != null) {
            PowerStateStore.remove(internalBlockKey(block));
        }
    }

    private PowerStateStore.Key internalBlockKey(Block block) {
        return new PowerStateStore.Key(null, "global", "internal-block-break", blockKey(block), null);
    }

    private String blockKey(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    public void registerPower(ObjectPower power) {
        if (power != null) {
            powers.put(power.getId(), power);
        }
    }

    public void clearPowers() {
        powers.clear();
    }

    public void onEntityUnload(UUID entityId) {
        if (entityId == null) {
            return;
        }
        AbilityManager.abilityManager.onEntityUnload(entityId);
        PowerModifiersManager.powerModifiers.onEntityUnload(entityId);
        PowerConditionsManager.powerConditions.onEntityUnload(entityId);
        PowerStateStore.clearEntity(entityId);
    }

    public void onReload() {
        unloadExtensionState();
        PowerStateStore.clearAll();
        clearPowers();
    }

    public void onUnload() {
        unloadExtensionState();
        PowerStateStore.shutdown();
        clearPowers();
    }

    @Override
    public void onPluginReload() {
        onReload();
    }

    @Override
    public void onPluginDisable() {
        onUnload();
        powerManager = null;
    }

    private void unloadExtensionState() {
        AbilityManager.abilityManager.onUnload();
        PowerModifiersManager.powerModifiers.onUnload();
        PowerConditionsManager.powerConditions.onUnload();
    }

    public Map<String, ObjectPower> getPowers() {
        return powers;
    }
}
