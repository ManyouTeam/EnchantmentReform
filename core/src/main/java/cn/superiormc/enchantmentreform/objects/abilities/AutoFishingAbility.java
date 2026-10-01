package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.NmsCapability;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.PacketUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Locale;

public final class AutoFishingAbility extends AbstractAbility {

    private final NmsBridge nmsBridge;

    public AutoFishingAbility(ConfigurationSection section) {
        this(section, EnchantmentReform.instance == null
                ? new UnsupportedNmsBridge("Plugin is not enabled")
                : EnchantmentReform.instance.getNmsBridge());
    }

    public AutoFishingAbility(ConfigurationSection section, NmsBridge nmsBridge) {
        super("AutoFishing", section);
        this.nmsBridge = nmsBridge;
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.event() instanceof PlayerFishEvent event)
                || event.getState() != PlayerFishEvent.State.BITE) {
            return false;
        }

        Player player = event.getPlayer();
        FishHook hook = event.getHook();
        int retrieveDelay = Math.max(1, getInt("retrieve-delay-ticks", 5, context));
        int recastDelay = Math.max(1, getInt("recast-delay-ticks", 10, context));
        EquipmentSlot hand = context.contextSlot();
        if (hand == null) {
            return false;
        }

        if (method() == Method.NMS) {
            if (nmsBridge.supports(NmsCapability.FISHING_ROD_USE)) {
                SchedulerUtil.runTaskLater(player,
                        () -> retrieveAndRecastNms(player, hook, hand, recastDelay), retrieveDelay);
            }
        } else if (CommonUtil.checkPluginLoad("packetevents")) {
            SchedulerUtil.runTaskLater(player,
                    () -> retrieveAndRecastLegacy(player, hook, hand, recastDelay), retrieveDelay);
        }
        return false;
    }

    private void retrieveAndRecastLegacy(Player player, FishHook hook, EquipmentSlot hand, int recastDelay) {
        if (!player.isOnline() || hook == null || !hook.isValid() || !hasRod(player, hand)) {
            return;
        }

        InteractionHand packetHand = packetHand(hand);
        PacketUtil.rightClick(player, packetHand);
        if (recastDelay >= 0) {
            SchedulerUtil.runTaskLater(player, () -> {
                if (player.isOnline() && hasRod(player, hand)) {
                    PacketUtil.rightClick(player, packetHand);
                }
            }, recastDelay);
        }
    }

    private void retrieveAndRecastNms(Player player, FishHook hook, EquipmentSlot hand, int recastDelay) {
        if (!player.isOnline() || hook == null || !hook.isValid() || !hasRod(player, hand)) {
            return;
        }
        if (!nmsBridge.retrieveFishingRod(player, hook, hand).successful()) {
            return;
        }
        SchedulerUtil.runTaskLater(player, () -> {
            if (player.isOnline() && hasRod(player, hand)) {
                nmsBridge.castFishingRod(player, hand);
            }
        }, recastDelay);
    }

    private InteractionHand packetHand(EquipmentSlot hand) {
        return hand == EquipmentSlot.OFF_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private boolean hasRod(Player player, EquipmentSlot hand) {
        return switch (hand) {
            case HAND -> player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD;
            case OFF_HAND -> player.getInventory().getItemInOffHand().getType() == Material.FISHING_ROD;
            default -> false;
        };
    }

    private Method method() {
        try {
            return Method.valueOf(section.getString("method", "LEGACY").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Method.LEGACY;
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private enum Method {
        LEGACY,
        NMS
    }
}
