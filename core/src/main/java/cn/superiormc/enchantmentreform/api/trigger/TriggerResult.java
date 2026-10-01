package cn.superiormc.enchantmentreform.api.trigger;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.power.TrackedPowerSource;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class TriggerResult {

    private int executedPowers;

    private final List<TrackedPowerSource> executedSources = new ArrayList<>();

    private boolean cancelled;

    private boolean clearDrops;

    private boolean collectDrops;

    private Double damage;

    private Double regainAmount;

    private Integer combustDuration;

    private Float explosionYield;

    private Float explosionRadius;

    private Integer airAmount;

    private Integer foodLevel;

    private Integer experienceAmount;

    private Integer itemDamage;

    private int lastChangedBlocks;

    private int changedBlocks;

    private Double reviveHealth;

    private Entity skillEntity;

    public int executedPowers() {
        return executedPowers;
    }

    public List<TrackedPowerSource> executedSources() {
        return List.copyOf(executedSources);
    }

    public boolean cancelled() {
        return cancelled;
    }

    public boolean clearDrops() {
        return clearDrops;
    }

    public Double reviveHealth() {
        return reviveHealth;
    }

    public Entity skillEntity() {
        return skillEntity;
    }

    public int lastChangedBlocks() {
        return lastChangedBlocks;
    }

    public int changedBlocks() {
        return changedBlocks;
    }

    public void recordChangedBlocks(int amount) {
        lastChangedBlocks = Math.max(0, amount);
        changedBlocks += lastChangedBlocks;
    }

    public void markExecuted() {
        executedPowers++;
    }

    public void markExecuted(ActivePowerSource active) {
        markExecuted();
        if (active != null) {
            executedSources.add(TrackedPowerSource.from(active));
        }
    }

    public void cancel() {
        cancelled = true;
    }

    public void clearDrops(boolean value) {
        clearDrops = value;
    }

    public void collectDrops(boolean value) {
        collectDrops = value;
    }

    public void reviveHealth(double value) {
        reviveHealth = value;
        cancelled = true;
    }

    public void skillEntity(Entity value) {
        skillEntity = value;
    }

    public double damage(TriggerData data) {
        return damage == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_DAMAGE).orElse(0.0D) : damage;
    }

    public void damage(double value) {
        damage = value;
    }

    public double regainAmount(TriggerData data) {
        return regainAmount == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_AMOUNT).orElse(0.0D) : regainAmount;
    }

    public void regainAmount(double value) {
        regainAmount = value;
    }

    public float combustDuration(TriggerData data) {
        return combustDuration == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_DURATION).orElse(0.0F)
                : combustDuration.floatValue();
    }

    public void combustDuration(float value) {
        combustDuration = Math.round(value);
    }

    public float explosionYield(TriggerData data) {
        return explosionYield == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_YIELD).orElse(0.0F) : explosionYield;
    }

    public void explosionYield(float value) {
        explosionYield = value;
    }

    public float explosionRadius(TriggerData data) {
        return explosionRadius == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_RADIUS).orElse(0.0F) : explosionRadius;
    }

    public void explosionRadius(float value) {
        explosionRadius = value;
    }

    public int airAmount(TriggerData data) {
        return airAmount == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_AIR).orElse(0) : airAmount;
    }

    public void airAmount(int value) {
        airAmount = value;
    }

    public int foodLevel(TriggerData data) {
        return foodLevel == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_FOOD_LEVEL).orElse(0) : foodLevel;
    }

    public void foodLevel(int value) {
        foodLevel = value;
    }

    public int experienceAmount(TriggerData data) {
        return experienceAmount == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_EXPERIENCE).orElse(0)
                : experienceAmount;
    }

    public void experienceAmount(int value) {
        experienceAmount = value;
    }

    public int itemDamage(TriggerData data) {
        return itemDamage == null
                ? data.extra(BuiltinContextKeys.ORIGINAL_ITEM_DAMAGE).orElse(0) : itemDamage;
    }

    public void itemDamage(int value) {
        itemDamage = value;
    }

    public void apply(TriggerData data) {
        if (reviveHealth != null && data.event() instanceof EntityDeathEvent death
                && EnchantmentReform.methodUtil.methodID().equals("paper")) {
            double maximumHealth = CommonUtil.getMaxHealth(death.getEntity());
            death.setReviveHealth(Math.min(maximumHealth, Math.max(1.0D, reviveHealth)));
        }
        if (cancelled && data.event() instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
        if (!cancelled && damage != null && data.event() instanceof EntityDamageEvent event) {
            event.setDamage(Math.max(0.0D, damage));
        }
        if (!cancelled && regainAmount != null
                && data.event() instanceof EntityRegainHealthEvent event) {
            event.setAmount(Math.max(0.0D, regainAmount));
        }
        if (!cancelled && combustDuration != null
                && data.event() instanceof EntityCombustEvent event) {
            event.setDuration(Math.max(0, combustDuration));
        }
        if (!cancelled && explosionYield != null
                && data.event() instanceof EntityExplodeEvent event) {
            event.setYield(Math.max(0.0F, explosionYield));
        }
        if (!cancelled && explosionRadius != null
                && data.event() instanceof ExplosionPrimeEvent event) {
            event.setRadius(Math.max(0.0F, explosionRadius));
        }
        if (!cancelled && airAmount != null
                && data.event() instanceof EntityAirChangeEvent event) {
            event.setAmount(Math.max(0, airAmount));
        }
        if (!cancelled && foodLevel != null
                && data.event() instanceof FoodLevelChangeEvent event) {
            event.setFoodLevel(Math.min(20, Math.max(0, foodLevel)));
        }
        if (!cancelled && experienceAmount != null) {
            applyExperience(data, Math.max(0, experienceAmount));
        }
        if (!cancelled && itemDamage != null
                && data.event() instanceof PlayerItemDamageEvent event) {
            event.setDamage(Math.max(0, itemDamage));
        }
        if (clearDrops && data.event() instanceof EntityDeathEvent death) {
            death.getDrops().clear();
        }
        if (!cancelled && !clearDrops && collectDrops) {
            applyDropPickup(data);
        }
    }

    private void applyDropPickup(TriggerData data) {
        List<ItemStack> drops = new ArrayList<>();
        if (data.event() instanceof BlockDropItemEvent event) {
            for (Item item : event.getItems()) {
                drops.add(item.getItemStack().clone());
                item.remove();
            }
            event.getItems().clear();
        } else if (data.event() instanceof EntityDeathEvent event) {
            event.getDrops().forEach(drop -> drops.add(drop.clone()));
            event.getDrops().clear();
        } else if (data.event() instanceof PlayerFishEvent event) {
            collectFishingCatch(event, drops);
        }
        if (drops.isEmpty()) {
            return;
        }
        var location = data.location();
        if (location == null) {
            location = data.player().getLocation();
        }
        for (ItemStack drop : drops) {
            var leftovers = data.player().getInventory().addItem(drop);
            for (ItemStack leftover : leftovers.values()) {
                location.getWorld().dropItemNaturally(location, leftover);
            }
        }
    }

    private void collectFishingCatch(PlayerFishEvent event, List<ItemStack> drops) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                || !(event.getCaught() instanceof Item caught)) {
            return;
        }
        drops.add(caught.getItemStack().clone());
        caught.remove();
    }

    private void applyExperience(TriggerData data, int value) {
        if (data.event() instanceof PlayerExpChangeEvent event) {
            event.setAmount(value);
        } else if (data.event() instanceof BlockBreakEvent event) {
            event.setExpToDrop(value);
        } else if (data.event() instanceof EntityDeathEvent event) {
            event.setDroppedExp(value);
        } else if (data.event() instanceof PlayerFishEvent event) {
            event.setExpToDrop(value);
        } else if (data.event() instanceof EnchantItemEvent event) {
            event.setExpLevelCost(value);
        }
    }
}
