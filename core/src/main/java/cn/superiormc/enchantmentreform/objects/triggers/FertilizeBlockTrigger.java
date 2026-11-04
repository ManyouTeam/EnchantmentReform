package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class FertilizeBlockTrigger extends EventTrigger<Event> {

    private final Map<UUID, PlayerInteractEvent> interactions = new ConcurrentHashMap<>();

    private final Map<UUID, ItemStack> items = new ConcurrentHashMap<>();

    public FertilizeBlockTrigger() {
        super("fertilize_block", "on-fertilize-block", Event.class, PlayerInteractEvent.class, BlockFertilizeEvent.class);
    }

    @Override
    protected void handle(Event event, TriggerRuntime runtime) {
        if (event instanceof PlayerInteractEvent interaction) {
            ItemStack item = interaction.getItem();
            if (interaction.getAction() != Action.RIGHT_CLICK_BLOCK || item == null || item.getType() != Material.BONE_MEAL
                    || interaction.useItemInHand() == Event.Result.DENY) {
                return;
            }
            UUID id = interaction.getPlayer().getUniqueId();
            interactions.put(id, interaction);
            items.put(id, item.clone());
            SchedulerUtil.runTaskLater(() -> {
                if (interactions.remove(id, interaction)) {
                    items.remove(id);
                }
            }, 1L);
            return;
        }
        if (!(event instanceof BlockFertilizeEvent fertilize) || fertilize.isCancelled() || fertilize.getPlayer() == null || fertilize.getBlocks().isEmpty()) {
            return;
        }
        Player player = fertilize.getPlayer();
        PlayerInteractEvent interaction = interactions.remove(player.getUniqueId());
        ItemStack consumed = items.remove(player.getUniqueId());
        TriggerData.Builder data = runtime.base(player, fertilize).block(fertilize.getBlock())
                .location(fertilize.getBlock().getLocation().add(0.5D, 0.5D, 0.5D))
                .placedBlocks(fertilize.getBlocks());
        if (interaction != null && consumed != null && fertilize.getBlock().equals(interaction.getClickedBlock())
                && interaction.useItemInHand() != Event.Result.DENY) {
            EquipmentSlot hand = interaction.getHand();
            data.triggerItem(consumed, hand).extra(BuiltinContextKeys.CONSUMABLE, consumed)
                    .extra(BuiltinContextKeys.HAND, hand);
        }
        runtime.fire(this, data);
    }
}
