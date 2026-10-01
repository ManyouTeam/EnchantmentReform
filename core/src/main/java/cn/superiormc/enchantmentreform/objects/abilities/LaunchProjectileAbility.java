package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.power.AbilityDamageUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import com.google.common.base.Enums;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.Locale;

public class LaunchProjectileAbility extends AbstractAbility {

    public LaunchProjectileAbility(ConfigurationSection section) {
        super("LaunchProjectile", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity source = section.contains("source") ? getSourceEntity(context) : getTargetEntity(context);
        if (!(source instanceof LivingEntity livingEntity)) {
            return false;
        }

        EntityType projectileType = Enums.getIfPresent(EntityType.class, getString("entity-type", "ARROW")).orNull();
        if (projectileType == null || !projectileType.isSpawnable()) {
            return false;
        }

        Location eye = livingEntity.getEyeLocation();
        Vector baseDirection = getLaunchDirection(eye, context);
        baseDirection.setY(baseDirection.getY() + getDouble("extra-y", 0, context));
        int count = Math.max(1, getInt("count", 1, context));
        double spread = Math.toRadians(Math.max(
                0.0D, getDouble("spread-degrees", 0.0D, context)));

        for (int i = 0; i < count; i++) {
            double offset = count == 1
                    ? 0.0D
                    : -spread + 2.0D * spread * i / (count - 1);
            Vector direction = baseDirection.clone().rotateAroundY(offset);
            if (!spawnProjectile(livingEntity, projectileType, eye, direction, context)) {
                return false;
            }
        }
        return false;
    }

    private boolean spawnProjectile(LivingEntity shooter,
                                    EntityType projectileType,
                                    Location eye,
                                    Vector direction,
                                    PowerContext context) {
        Location spawnLocation = getSpawnLocation(eye, direction, context);
        Entity spawned = eye.getWorld().spawnEntity(spawnLocation, projectileType);
        if (!(spawned instanceof Projectile projectile)) {
            spawned.remove();
            return false;
        }

        projectile.setShooter(shooter);
        projectile.setVelocity(direction);

        if (projectile instanceof Explosive fireball) {
            fireball.setYield((float) getDouble("fireball-yield", 1.0, context));
            fireball.setIsIncendiary(getBoolean("fireball-incendiary", true));
        }

        if (projectile instanceof ShulkerBullet bullet) {
            Entity target = context.target();
            if (target != null && target.isValid()) {
                bullet.setTarget(target);
            }
        }

        if (projectile instanceof ThrownPotion potion) {
            applyPotionItem(potion, context);
        }

        if (section.contains("damage")) {
            AbilityDamageUtil.markDamage(projectile, getDouble("damage", 0.0D, context));
        }
        inheritPowers(projectile, context);
        return true;
    }

    private void inheritPowers(Projectile projectile, PowerContext context) {
        if (!getBoolean("inherit-powers", false)) {
            return;
        }
        TriggerManager manager = TriggerManager.triggerManager;
        Player player = context.player();
        ItemStack item = context.triggerItem() == null ? context.item() : context.triggerItem();
        EquipmentSlot slot = context.contextSlot() == null ? EquipmentSlot.HAND : context.contextSlot();
        if (manager == null || player == null || item == null || item.getType().isAir()) {
            return;
        }
        manager.runtime().trackProjectile(player, projectile, item, slot);
    }

    private void applyPotionItem(ThrownPotion potion, PowerContext context) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) {
            return;
        }

        String basePotion = getString("potion-type", "");
        PotionType potionType = Enums.getIfPresent(PotionType.class, basePotion.toUpperCase()).orNull();
        if (potionType != null) {
            meta.setBasePotionType(potionType);
        }

        ConfigurationSection effects = section.getConfigurationSection("potion-effects");
        if (effects != null) {
            for (String key : effects.getKeys(false)) {
                ConfigurationSection effectSection = effects.getConfigurationSection(key);
                if (effectSection == null) {
                    continue;
                }
                String potionKey = effectSection.getString("potion", key);
                PotionEffectType effectType = getPotionEffectType(potionKey);
                if (effectType == null) {
                    continue;
                }
                int duration = Math.max(1, getInt(effectSection.getCurrentPath() + ".duration", 100, context));
                int amplifier = Math.max(0, getInt(effectSection.getCurrentPath() + ".amplifier", 0, context));
                meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            }
        } else {
            String potionKey = getString("potion", "");
            PotionEffectType effectType = getPotionEffectType(potionKey);
            if (effectType != null) {
                int duration = Math.max(1, getInt("duration", 100, context));
                int amplifier = Math.max(0, getInt("amplifier", 0, context));
                meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            }
        }

        item.setItemMeta(meta);
        potion.setItem(item);
    }

    private PotionEffectType getPotionEffectType(String potionKey) {
        PotionEffectType effectType = Registry.EFFECT.get(CommonUtil.parseNamespacedKey(potionKey));
        if (effectType != null || potionKey == null) {
            return effectType;
        }
        return PotionEffectType.getByName(potionKey.toUpperCase(Locale.ROOT));
    }

    private Location getSpawnLocation(Location eye, Vector direction, PowerContext context) {
        double offset = getDouble("spawn-offset", 0.0D, context);
        if (offset <= 0.0D || direction.lengthSquared() <= 1.0E-4D) {
            return eye;
        }
        return eye.clone().add(direction.clone().normalize().multiply(offset));
    }

    private Vector getLaunchDirection(Location eye, PowerContext context) {
        double speed = getDouble("speed", 1.5D, context);
        Entity aimTarget = context.target();
        if (aimTarget != null && aimTarget.getWorld().equals(eye.getWorld()) && aimTarget instanceof LivingEntity livingEntity) {
            Vector targetDirection = livingEntity.getEyeLocation().toVector().subtract(eye.toVector());
            if (targetDirection.lengthSquared() > 1.0E-4D) {
                return targetDirection.normalize().multiply(speed);
            }
        }
        return eye.getDirection().normalize().multiply(speed);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
