package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

public final class PotionEffectTrigger extends EventTrigger<EntityPotionEffectEvent> {

    public PotionEffectTrigger() {
        super("potion_effect", "on-potion-effect", EntityPotionEffectEvent.class);
    }

    @Override
    protected void handle(EntityPotionEffectEvent event, TriggerRuntime runtime) {
        if (!CommonUtil.getYearVersion(26, 2, 0)) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PotionEffect newEffect = event.getNewEffect();
        float duration = newEffect == null ? 0.0F
                : newEffect.getDuration() == PotionEffect.INFINITE_DURATION
                ? 999999.0F : newEffect.getDuration();
        if (EnchantmentReform.methodUtil.methodID().equals("paper")) {
            Entity source = event.getSource() == null ? player : event.getSource();
            runtime.fire(this, runtime.base(player, event)
                    .source(source)
                    .skill(source)
                    .target(player)
                    .extra(BuiltinContextKeys.ORIGINAL_DURATION, duration));
        } else {
            runtime.fire(this, runtime.base(player, event)
                    .target(player)
                    .extra(BuiltinContextKeys.ORIGINAL_DURATION, duration));
        }
    }
}
