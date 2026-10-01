package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class FoodLevelChangeTrigger extends EventTrigger<FoodLevelChangeEvent> {

    public FoodLevelChangeTrigger() {
        super("food_level_change", "on-food-level-change", FoodLevelChangeEvent.class);
    }

    @Override
    protected void handle(FoodLevelChangeEvent event, TriggerRuntime runtime) {
        if (event.getEntity() instanceof Player player) {
            runtime.fire(this, runtime.base(player, event)
                    .extra(BuiltinContextKeys.ORIGINAL_FOOD_LEVEL, event.getFoodLevel()));
        }
    }
}
