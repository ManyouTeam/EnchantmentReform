package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.hooks.enchantmentdescription.EnchantmentDescriptionItemDisplay;
import cn.superiormc.enchantmentreform.listeners.AntiAbuseListener;
import cn.superiormc.enchantmentreform.listeners.AnvilListener;
import cn.superiormc.enchantmentreform.listeners.DupeListener;
import cn.superiormc.enchantmentreform.listeners.EnchantabilityOverrideListener;
import cn.superiormc.enchantmentreform.listeners.EnchantmentPowerListener;
import cn.superiormc.enchantmentreform.listeners.EquipmentListener;
import cn.superiormc.enchantmentreform.listeners.GUIListener;
import cn.superiormc.enchantmentreform.listeners.LocationChangedEffectTempBlockListener;
import cn.superiormc.enchantmentreform.protocol.packetevents.FakeChangePacketListener;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Locale;

public final class ListenerManager extends AbstractManager {

    public static ListenerManager listenerManager;

    private final EnchantmentDescriptionItemDisplay itemDisplay = new EnchantmentDescriptionItemDisplay();

    private final EnchantmentPowerListener powerListener;

    private AntiAbuseListener antiAbuseListener;

    private boolean packetEventsRegistered;

    public ListenerManager() {
        listenerManager = this;
        registerListener(new GUIListener());
        updateAntiAbuseListener();
        registerListener(new LocationChangedEffectTempBlockListener());
        if (EnchantmentReform.isPaper) {
            registerPaperListeners();
        }
        registerPacketListeners();
        EnchantmentReform.methodUtil.tempBlockManager();
        powerListener = EnchantmentReform.methodUtil.enchantmentPowerListener();
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, EnchantmentReform.instance);
    }

    private void registerPaperListeners() {
        if (CommonUtil.getMajorVersion(19)) {
            registerListener(new DupeListener());
        }
        registerListener(new EnchantabilityOverrideListener());
        registerListener(new EquipmentListener(TriggerManager.triggerManager));
        registerListener(new AnvilListener());
    }

    private void updateAntiAbuseListener() {
        boolean enabled = AntiAbuseManager.antiAbuseManager.isEnabled();
        if (enabled && antiAbuseListener == null) {
            antiAbuseListener = new AntiAbuseListener();
            registerListener(antiAbuseListener);
        } else if (!enabled && antiAbuseListener != null) {
            HandlerList.unregisterAll(antiAbuseListener);
            antiAbuseListener = null;
        }
    }

    public void registerPacketListeners() {
        if (packetEventsRegistered || !CommonUtil.checkPluginLoad("packetevents")) {
            return;
        }
        itemDisplay.reload();
        if (!itemDisplay.isEnabled()) {
            return;
        }

        try {
            PacketEventsListener.registerPacketEventsListeners(itemDisplay);
            packetEventsRegistered = true;
        } catch (Throwable throwable) {
            return;
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix()
                + " §fEnabled client-side enchantment description display.");
    }

    private void unregisterPacketListener() {
        if (packetEventsRegistered) {
            PacketEventsListener.unregisterPacketEventsListeners();
            packetEventsRegistered = false;
        }
    }

    @Override
    public void onPluginReload() {
        updateAntiAbuseListener();
        unregisterPacketListener();
        registerPacketListeners();
    }

    @Override
    public void onPluginDisable() {
        if (powerListener != null) {
            powerListener.close();
        }
        unregisterPacketListener();
    }
}

final class PacketEventsListener {

    private static PacketListenerCommon packetListener;

    public static void registerPacketEventsListeners(EnchantmentDescriptionItemDisplay itemDisplay) {
        PacketListenerPriority priority = PacketListenerPriority.valueOf(ConfigManager.configManager.getString(
                "enchantment-description.item-display.packet-listener-priority", "LOWEST").toUpperCase(Locale.ROOT));
        packetListener = PacketEvents.getAPI().getEventManager().registerListener(
                new FakeChangePacketListener(itemDisplay), priority);
    }

    public static void unregisterPacketEventsListeners() {
        if (packetListener != null && Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        }
        packetListener = null;
    }
}
