package cn.superiormc.enchantmentreform.objects.triggers;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class ShearTrigger extends EventTrigger<Event> {

    private final boolean leash;

    public ShearTrigger() {
        this(false);
    }

    private ShearTrigger(boolean leash) {
        super(leash ? "cut_leash" : "shear", leash ? "on-cut-leash" : "on-shear", Event.class,
                leash ? PlayerUnleashEntityEvent.class : PlayerShearEntityEvent.class);
        this.leash = leash;
    }

    public static ShearTrigger cutLeash() {
        return new ShearTrigger(true);
    }

    @Override
    protected void handle(Event event, TriggerRuntime runtime) {
        if (!leash && event instanceof PlayerShearEntityEvent shear) {
            runtime.fire(this, runtime.base(shear.getPlayer(), shear)
                    .target(shear.getEntity()).location(shear.getEntity().getLocation())
                    .triggerItem(shear.getItem(), shear.getHand()));
        } else if (leash && event instanceof PlayerUnleashEntityEvent unleash) {
            ItemStack item = unleash.getPlayer().getInventory().getItem(unleash.getHand());
            if (item.getType() != Material.SHEARS) {
                return;
            }
            runtime.fire(this, runtime.base(unleash.getPlayer(), unleash)
                    .target(unleash.getEntity()).location(unleash.getEntity().getLocation())
                    .triggerItem(item, unleash.getHand()));
        }
    }
}
