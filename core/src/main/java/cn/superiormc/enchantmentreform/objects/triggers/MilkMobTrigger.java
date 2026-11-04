package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.utils.SchedulerUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class MilkMobTrigger extends EventTrigger<Event> {

    private final Map<UUID, PlayerInteractEntityEvent> interactions = new ConcurrentHashMap<>();

    public MilkMobTrigger() {
        super("milk_mob", "on-milk-mob", Event.class, PlayerInteractEntityEvent.class, PlayerBucketFillEvent.class);
    }

    @Override
    protected void handle(Event event, TriggerRuntime runtime) {
        if (event instanceof PlayerInteractEntityEvent interaction) {
            Player player = interaction.getPlayer();
            if (interaction.isCancelled() || player.getInventory().getItem(interaction.getHand()).getType() != Material.BUCKET
                    || !(interaction.getRightClicked() instanceof Cow || interaction.getRightClicked() instanceof Goat)) {
                return;
            }
            UUID id = player.getUniqueId();
            interactions.put(id, interaction);
            SchedulerUtil.runTaskLater(() -> interactions.remove(id, interaction), 1L);
        } else if (event instanceof PlayerBucketFillEvent fill) {
            ItemStack milk = fill.getItemStack();
            if (fill.isCancelled() || milk == null || milk.getType() != Material.MILK_BUCKET) {
                return;
            }
            PlayerInteractEntityEvent interaction = interactions.remove(fill.getPlayer().getUniqueId());
            if (interaction == null || interaction.isCancelled() || interaction.getHand() != fill.getHand()) {
                return;
            }
            ItemStack output = milk.clone();
            output.setAmount(1);
            runtime.fire(this, runtime.base(fill.getPlayer(), fill)
                    .target(interaction.getRightClicked()).location(interaction.getRightClicked().getLocation())
                    .outputItems(List.of(output)).triggerItem(output, fill.getHand()));
        }
    }
}
