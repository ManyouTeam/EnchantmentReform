package cn.superiormc.enchantmentreform.objects.triggers;

import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.objects.ObjectTempPower;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import org.bukkit.configuration.ConfigurationSection;

public final class ActivateTrigger extends EventTrigger<TriggerRuntime.Activation> {

    public ActivateTrigger() {
        super("activate", "on-activate", TriggerRuntime.Activation.class);
    }

    @Override
    protected void handle(TriggerRuntime.Activation event, TriggerRuntime runtime) {
        ActivePowerSource active = event.active();
        TriggerData data = runtime.base(event.player(), event.event())
                .block(event.player().getLocation().getBlock())
                .triggerItem(active.item(), active.slot()).build();
        if (active.power() != null && active.power().isEnabled()
                && !(active.source() instanceof ObjectTempPower temp && temp.isOnce())) {
            ConfigurationSection abilities = active.power().getSection("activation-abilities");
            if (abilities != null) {
                PowerContext context = new PowerContext(active.power(), active.level(), this,
                        data, active.item(), active.slot(), new TriggerResult());
                AbilityManager.abilityManager.executeActivationStats(abilities, context);
            }
        }
        runtime.fireActive(this, data, active);
    }
}
