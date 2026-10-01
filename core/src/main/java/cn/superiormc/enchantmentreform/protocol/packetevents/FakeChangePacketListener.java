package cn.superiormc.enchantmentreform.protocol.packetevents;

import cn.superiormc.enchantmentreform.hooks.fakechange.FakeChangeContext;
import cn.superiormc.enchantmentreform.managers.FakeChangeManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.recipe.data.MerchantOffer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class FakeChangePacketListener implements PacketListener {

    private final FakeChangeManager manager;

    public FakeChangePacketListener(FakeChangeManager manager) {
        this.manager = manager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (event.getPacketType().equals(PacketType.Play.Server.WINDOW_ITEMS)) {
            transformWindowItems(event, player);
        } else if (event.getPacketType().equals(PacketType.Play.Server.SET_SLOT)) {
            transformSetSlot(event, player);
        } else if (event.getPacketType().equals(PacketType.Play.Server.SET_CURSOR_ITEM)) {
            transformCursor(event, player);
        } else if (event.getPacketType().equals(PacketType.Play.Server.MERCHANT_OFFERS)) {
            transformMerchantOffers(event, player);
        }
    }

    private void transformWindowItems(PacketSendEvent event, Player player) {
        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
        int windowId = packet.getWindowId();
        packet.getCarriedItem().ifPresent(item -> packet.setCarriedItem(transform(
                item, player, FakeChangeContext.Surface.CURSOR, windowId, -1, true)));
        List<ItemStack> transformed = new ArrayList<>(packet.getItems().size());
        int playerInventoryStart = Math.max(0, packet.getItems().size() - 36);
        for (int index = 0; index < packet.getItems().size(); index++) {
            transformed.add(transform(
                    packet.getItems().get(index),
                    player,
                    FakeChangeContext.Surface.WINDOW_ITEM,
                    windowId,
                    index,
                    windowId == 0 || index >= playerInventoryStart));
        }
        packet.setItems(transformed);
    }

    private void transformSetSlot(PacketSendEvent event, Player player) {
        WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
        int windowId = packet.getWindowId();
        int slot = packet.getSlot();
        packet.setItem(transform(
                packet.getItem(),
                player,
                FakeChangeContext.Surface.SET_SLOT,
                windowId,
                slot,
                isPlayerInventory(player, slot, windowId)));
    }

    private void transformCursor(PacketSendEvent event, Player player) {
        WrapperPlayServerSetCursorItem packet = new WrapperPlayServerSetCursorItem(event);
        packet.setStack(transform(
                packet.getStack(), player, FakeChangeContext.Surface.CURSOR, -1, -1, true));
    }

    private void transformMerchantOffers(PacketSendEvent event, Player player) {
        WrapperPlayServerMerchantOffers packet = new WrapperPlayServerMerchantOffers(event);
        for (MerchantOffer offer : packet.getMerchantOffers()) {
            offer.setOutputItem(transform(offer.getOutputItem(), player,
                    FakeChangeContext.Surface.MERCHANT, -1, -1, false));
            offer.setFirstInputItem(transform(offer.getFirstInputItem(), player,
                    FakeChangeContext.Surface.MERCHANT, -1, -1, false));
            offer.setSecondInputItem(transform(offer.getSecondInputItem(), player,
                    FakeChangeContext.Surface.MERCHANT, -1, -1, false));
        }
    }

    private ItemStack transform(ItemStack item,
                                Player player,
                                FakeChangeContext.Surface surface,
                                int windowId,
                                int slot,
                                boolean playerInventory) {
        if (item == null || item.isEmpty()) {
            return item;
        }
        org.bukkit.inventory.ItemStack bukkit = SpigotConversionUtil.toBukkitItemStack(item);
        FakeChangeContext context = new FakeChangeContext(
                player, surface, windowId, slot, playerInventory);
        return SpigotConversionUtil.fromBukkitItemStack(manager.transform(bukkit, context));
    }

    private boolean isPlayerInventory(Player player, int slot, int windowId) {
        if (windowId == 0) {
            return slot >= 5 && slot <= 44;
        }
        int topSize = player.getOpenInventory().getTopInventory().getSize();
        return slot >= topSize;
    }
}
