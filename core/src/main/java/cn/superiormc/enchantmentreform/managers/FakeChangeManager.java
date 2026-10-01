package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.hooks.enchantmentdescription.EnchantmentDescriptionItemDisplay;
import cn.superiormc.enchantmentreform.hooks.fakechange.FakeChange;
import cn.superiormc.enchantmentreform.hooks.fakechange.FakeChangeContext;
import cn.superiormc.enchantmentreform.protocol.packetevents.FakeChangePacketListener;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class FakeChangeManager extends AbstractManager implements AutoCloseable {

    public static FakeChangeManager fakeChangeManager;

    private final List<FakeChange> changes = List.of(
            new EnchantmentDescriptionItemDisplay());

    private PacketListenerCommon packetListener;

    public FakeChangeManager() {
        fakeChangeManager = this;
    }

    public static boolean enableThis() {
        return CommonUtil.checkPluginLoad("packetevents");
    }

    @Override
    public void onInit() {
        reload();
    }

    public void reload() {
        changes.forEach(FakeChange::reload);
        unregisterPacketListener();
        if (changes.stream().anyMatch(FakeChange::isEnabled)) {
            tryRegisterPacketListener();
        }
    }

    public ItemStack transform(ItemStack original, FakeChangeContext context) {
        if (original == null || original.getType().isAir()) {
            return original;
        }
        ItemStack display = original.clone();
        for (FakeChange change : changes) {
            if (change.isEnabled()) {
                display = change.apply(display, context);
            }
        }
        return display;
    }

    private void tryRegisterPacketListener() {
        if (packetListener != null || !enableThis()) {
            return;
        }
        try {
            PacketListenerPriority priority = PacketListenerPriority.valueOf(
                    ConfigManager.configManager.getString(
                            "enchantment-description.item-display.packet-listener-priority", "LOWEST")
                            .toUpperCase(Locale.ROOT));
            packetListener = PacketEvents.getAPI().getEventManager().registerListener(
                    new FakeChangePacketListener(this), priority);
            TextUtil.sendMessage(null, TextUtil.pluginPrefix()
                    + " §fEnabled client-side enchantment description display.");
        } catch (Throwable throwable) {
            EnchantmentReform.instance.getLogger().log(
                    Level.WARNING,
                    "Could not register the enchantment-description PacketEvents listener.",
                    throwable);
        }
    }

    private void unregisterPacketListener() {
        if (packetListener == null || !enableThis()) {
            packetListener = null;
            return;
        }
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        packetListener = null;
    }

    @Override
    public void close() {
        unregisterPacketListener();
    }

    @Override
    public void onPluginDisable() {
        close();
        fakeChangeManager = null;
    }
}
