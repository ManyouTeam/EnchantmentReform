package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.objects.ObjectTempPower;
import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TempPowerManager extends AbstractManager {

    public static TempPowerManager tempPowerManager;

    private final Map<String, ObjectTempPower> tempPowers = new LinkedHashMap<>();

    private final Map<UUID, Set<String>> activatedPowers = new ConcurrentHashMap<>();

    private final String serverSession;

    private volatile boolean closed;

    public TempPowerManager() {
        tempPowerManager = this;
        this.serverSession = ManagementFactory.getRuntimeMXBean().getStartTime()
                + ":" + System.identityHashCode(Bukkit.getServer());
        for (ObjectTempPower definition : ConfigManager.configManager.tempPowerConfigs.values()) {
            tempPowers.put(definition.getId(), definition);
            definition.initializePower();
            PowerManager.powerManager.registerPower(definition.getPower());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            SchedulerUtil.runSync(player, () -> {
                if (tempPowerManager == this && !closed && player.isOnline()) {
                    restoreTempPowers(player);
                }
            });
        }
    }

    public ObjectTempPower getTempPower(String id) {
        return id == null ? null : tempPowers.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<ObjectTempPower> getTempPowers() {
        return List.copyOf(tempPowers.values());
    }

    public List<ActivePowerSource> getActiveTempPowers(Player player) {
        if (player == null || closed) {
            return List.of();
        }
        List<ActivePowerSource> active = new ArrayList<>();
        String session = playerSession(player);
        for (ObjectTempPower definition : tempPowers.values()) {
            if (definition.isOnce()) {
                continue;
            }
            int level = definition.getLevel(player, session);
            if (level > 0) {
                active.add(new ActivePowerSource(definition, definition.getPower(), level, null, null));
            }
        }
        return List.copyOf(active);
    }

    public boolean isActive(Player player, ObjectTempPower definition) {
        return !closed && tempPowers.get(definition.getId()) == definition
                && definition.getLevel(player, playerSession(player)) > 0;
    }

    public void setTempPowerLevel(Player player, ObjectTempPower definition, int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("Temporary power level must be positive");
        }
        ActivePowerSource active = new ActivePowerSource(definition, definition.getPower(), level, null, null);
        if (definition.isOnce()) {
            dispatchActivation(player, active);
            return;
        }
        int previous = definition.getLevel(player, playerSession(player));
        if (previous == level) {
            return;
        }
        definition.setLevel(player, level, playerSession(player));
        if (previous > 0) {
            deactivateTempPower(player, new ActivePowerSource(
                    definition, definition.getPower(), previous, null, null));
        }
        activateTempPower(player, active);
    }

    public void removeTempPower(Player player, ObjectTempPower definition) {
        int level = definition.getLevel(player, playerSession(player));
        definition.remove(player);
        if (level > 0) {
            deactivateTempPower(player, new ActivePowerSource(definition, definition.getPower(), level, null, null));
        }
    }

    public int clearTempPowers(Player player) {
        List<ActivePowerSource> active = getActiveTempPowers(player);
        player.getPersistentDataContainer().remove(ObjectTempPower.DATA_KEY);
        for (ActivePowerSource source : active) {
            deactivateTempPower(player, source);
        }
        return active.size();
    }

    public void restoreTempPowers(Player player) {
        pruneTempPowers(player);
        for (ActivePowerSource active : getActiveTempPowers(player)) {
            activateTempPower(player, active);
        }
    }

    public void quitTempPowers(Player player) {
        for (ActivePowerSource active : getActiveTempPowers(player)) {
            ObjectTempPower definition = (ObjectTempPower) active.source();
            if (!definition.isRemembered()) {
                definition.remove(player);
            }
            deactivateTempPower(player, active);
        }
    }

    private String playerSession(Player player) {
        // Server and entity identities survive plugin replacement, but change on server restart/login.
        return serverSession + ":" + player.getEntityId();
    }

    private void pruneTempPowers(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        PersistentDataContainer stored = data.get(ObjectTempPower.DATA_KEY, PersistentDataType.TAG_CONTAINER);
        if (stored == null) {
            return;
        }
        for (NamespacedKey key : stored.getKeys()) {
            ObjectTempPower definition = tempPowers.get(key.getKey());
            if (!key.getNamespace().equals("enchantmentreform") || definition == null || definition.isOnce()) {
                stored.remove(key);
            }
        }
        if (stored.isEmpty()) {
            data.remove(ObjectTempPower.DATA_KEY);
        } else {
            data.set(ObjectTempPower.DATA_KEY, PersistentDataType.TAG_CONTAINER, stored);
        }
    }

    private void activateTempPower(Player player, ActivePowerSource active) {
        if (!activatedPowers.computeIfAbsent(player.getUniqueId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(active.source().powerSourceId())) {
            return;
        }
        dispatchActivation(player, active);
    }

    private void dispatchActivation(Player player, ActivePowerSource active) {
        if (TriggerManager.triggerManager != null) {
            TriggerManager.triggerManager.dispatch(TriggerRuntime.Activation.class,
                    new TriggerRuntime.Activation(player, null, active));
        }
    }

    private void deactivateTempPower(Player player, ActivePowerSource active) {
        Set<String> activated = activatedPowers.get(player.getUniqueId());
        if (activated != null) {
            activated.remove(active.source().powerSourceId());
            if (activated.isEmpty()) {
                activatedPowers.remove(player.getUniqueId(), activated);
            }
        }
        if (TriggerManager.triggerManager != null) {
            TriggerManager.triggerManager.dispatch(TriggerRuntime.Deactivation.class,
                    new TriggerRuntime.Deactivation(player, null, active));
        }
        if (AbilityManager.abilityManager != null) {
            AbilityManager.abilityManager.onPowerSourceDeactivate(player, active);
        }
        active.power().clearCounting(player.getUniqueId());
    }

    @Override
    public void onPluginReload() {
        closed = true;
        activatedPowers.clear();
    }

    @Override
    public void onPluginDisable() {
        closed = true;
        activatedPowers.clear();
        tempPowerManager = null;
    }
}
