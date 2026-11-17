package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.listeners.AnvilListener;
import cn.superiormc.enchantmentreform.listeners.DupeListener;
import cn.superiormc.enchantmentreform.listeners.EnchantabilityOverrideListener;
import cn.superiormc.enchantmentreform.listeners.EquipmentListener;
import cn.superiormc.enchantmentreform.listeners.GUIListener;
import cn.superiormc.enchantmentreform.listeners.LocationChangedEffectTempBlockListener;
import cn.superiormc.enchantmentreform.listeners.AntiAbuseListener;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class ListenerManager extends AbstractManager {

    public static ListenerManager listenerManager;

    private EnchantabilityOverrideListener enchantabilityOverrideListener;

    private AntiAbuseListener antiAbuseListener;

    public ListenerManager(){
        listenerManager = this;
        registerListeners();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(), EnchantmentReform.instance);
        reloadAntiAbuseListener();
        Bukkit.getPluginManager().registerEvents(new LocationChangedEffectTempBlockListener(), EnchantmentReform.instance);
        if (EnchantmentReform.methodUtil.methodID().equals("paper")) {
            if (CommonUtil.getMajorVersion(19)) {
                Bukkit.getPluginManager().registerEvents(new DupeListener(), EnchantmentReform.instance);
            }
            enchantabilityOverrideListener = new EnchantabilityOverrideListener(
                    ConfigManager.configManager.getSection("item-enchantability-overrides"));
            Bukkit.getPluginManager().registerEvents(enchantabilityOverrideListener, EnchantmentReform.instance);
            Bukkit.getPluginManager().registerEvents(new EquipmentListener(TriggerManager.triggerManager), EnchantmentReform.instance);
            Bukkit.getPluginManager().registerEvents(new AnvilListener(), EnchantmentReform.instance);
        }
    }

    private void reloadEnchantabilityOverrides() {
        if (enchantabilityOverrideListener != null) {
            enchantabilityOverrideListener.reload(
                    ConfigManager.configManager.getSection("item-enchantability-overrides"));
        }
    }

    private void reloadAntiAbuseListener() {
        if (AntiAbuseManager.antiAbuseManager.isEnabled()) {
            if (antiAbuseListener == null) {
                antiAbuseListener = new AntiAbuseListener();
                Bukkit.getPluginManager().registerEvents(antiAbuseListener, EnchantmentReform.instance);
            }
        } else if (antiAbuseListener != null) {
            HandlerList.unregisterAll(antiAbuseListener);
            antiAbuseListener = null;
        }
    }

    public void unregisterAllListener() {
        HandlerList.unregisterAll(EnchantmentReform.instance);
    }

    @Override
    public void onPluginReload() {
        reloadEnchantabilityOverrides();
        reloadAntiAbuseListener();
    }

    @Override
    public void onPluginDisable() {
        unregisterAllListener();
        listenerManager = null;
    }
}
