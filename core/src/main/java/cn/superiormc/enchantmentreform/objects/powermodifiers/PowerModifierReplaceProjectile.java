package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import com.google.common.base.Enums;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Locale;

public final class PowerModifierReplaceProjectile extends AbstractPowerModifier {

    public PowerModifierReplaceProjectile(ConfigurationSection section) {
        super("replace_projectile", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof EntityShootBowEvent) || !(context.skill() instanceof Projectile oldProjectile)) {
            return;
        }
        String rawType = getString("entity-type",
                getString("projectile", getString("projectile-type", ""))).toUpperCase(Locale.ROOT);
        if (rawType.isBlank()) {
            return;
        }
        Location location = oldProjectile.getLocation();
        Entity shooter = context.source();
        if (rawType.equals("TNT") || rawType.equals("PRIMED_TNT") || rawType.equals("TNT_PRIMED")) {
            TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
            tnt.setFuseTicks(Math.max(1, getInt("fuse", 40, context)));
            tnt.setVelocity(oldProjectile.getVelocity().multiply(getDouble("speed-multiplier", 1.2D, context)));
            if (shooter instanceof LivingEntity living) {
                tnt.setSource(living);
            }
            replace(context, oldProjectile, tnt);
            return;
        }

        EntityType entityType = Enums.getIfPresent(EntityType.class, rawType).orNull();
        if (entityType == null || !entityType.isSpawnable()) {
            return;
        }
        Entity spawned = location.getWorld().spawnEntity(location, entityType);
        if (!(spawned instanceof Projectile projectile)) {
            spawned.remove();
            return;
        }
        projectile.setVelocity(oldProjectile.getVelocity());
        projectile.setGravity(oldProjectile.hasGravity());
        if (shooter instanceof LivingEntity living) {
            projectile.setShooter(living);
        }
        if (projectile instanceof Fireball fireball) {
            fireball.setYield((float) getDouble("fireball-yield", 1.0D, context));
            fireball.setIsIncendiary(getBoolean("fireball-incendiary", true));
        }
        if (projectile instanceof ThrownPotion potion) {
            applyPotion(potion, context);
        }
        if (projectile instanceof ShulkerBullet bullet) {
            bullet.setTarget(context.target());
        }
        replace(context, oldProjectile, projectile);
    }

    private void replace(PowerContext context, Projectile oldProjectile, Entity replacement) {
        if (context.event() instanceof EntityShootBowEvent event) {
            event.setProjectile(replacement);
        }
        oldProjectile.remove();
        if (context.result() != null) {
            context.result().skillEntity(replacement);
        }
    }

    private void applyPotion(ThrownPotion potion, PowerContext context) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        String potionName = getString("potion-type", "");
        if (!potionName.isBlank()) {
            PotionType potionType = Enums.getIfPresent(PotionType.class, potionName.toUpperCase(Locale.ROOT)).orNull();
            if (potionType != null) {
                meta.setBasePotionType(potionType);
            }
        }
        ConfigurationSection effects = getSection("potion-effects");
        if (effects != null) {
            for (String key : effects.getKeys(false)) {
                ConfigurationSection effect = effects.getConfigurationSection(key);
                if (effect == null) {
                    continue;
                }
                String effectName = effect.getString("potion", key);
                PotionEffectType effectType = Registry.EFFECT.get(CommonUtil.parseNamespacedKey(effectName));
                if (effectType == null) {
                    effectType = PotionEffectType.getByName(effectName.toUpperCase(Locale.ROOT));
                }
                if (effectType == null) {
                    continue;
                }
                String path = "potion-effects." + key;
                int duration = Math.max(1, getInt(path + ".duration",
                        getInt(path + ".potion-duration", 100, context), context));
                int amplifier = Math.max(0, getInt(path + ".amplifier",
                        getInt(path + ".potion.amplifier", 0, context), context));
                meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            }
        }
        item.setItemMeta(meta);
        potion.setItem(item);
    }
}
