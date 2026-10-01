package cn.superiormc.enchantmentreform.utils;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class SchedulerUtil {

    private final Object task;

    public SchedulerUtil(BukkitTask task) {
        this.task = task;
    }

    public SchedulerUtil(Object task) {
        this.task = task;
    }

    public void cancel() {
        if (task == null) {
            return;
        }
        if (task instanceof BukkitTask bukkitTask) {
            bukkitTask.cancel();
            return;
        }
        try {
            task.getClass().getMethod("cancel").invoke(task);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void runSync(Runnable task) {
        if (EnchantmentReform.isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(EnchantmentReform.instance, task);
        } else {
            Bukkit.getScheduler().runTask(EnchantmentReform.instance, task);
        }
    }

    public static void runSync(Entity entity, Runnable task) {
        if (EnchantmentReform.isFolia) {
            entity.getScheduler().run(EnchantmentReform.instance, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(EnchantmentReform.instance, task);
        }
    }

    public static void runSync(Location location, Runnable task) {
        if (EnchantmentReform.isFolia) {
            Bukkit.getRegionScheduler().run(EnchantmentReform.instance, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(EnchantmentReform.instance, task);
        }
    }

    public static void runTaskAsynchronously(Runnable task) {
        if (EnchantmentReform.isFolia) {
            Bukkit.getAsyncScheduler().runNow(EnchantmentReform.instance, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(EnchantmentReform.instance, task);
        }
    }

    public static SchedulerUtil runTaskLater(Runnable task, long delayTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            return new SchedulerUtil(Bukkit.getGlobalRegionScheduler().runDelayed(
                    EnchantmentReform.instance, scheduledTask -> task.run(), delayTicks));
        }
        return new SchedulerUtil(Bukkit.getScheduler().runTaskLater(EnchantmentReform.instance, task, delayTicks));
    }

    public static SchedulerUtil runTaskLater(Entity entity, Runnable task, long delayTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            return new SchedulerUtil(entity.getScheduler().runDelayed(
                    EnchantmentReform.instance, scheduledTask -> task.run(), null, delayTicks));
        }
        return new SchedulerUtil(Bukkit.getScheduler().runTaskLater(EnchantmentReform.instance, task, delayTicks));
    }

    public static SchedulerUtil runTaskLater(Location location, Runnable task, long delayTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            return new SchedulerUtil(Bukkit.getRegionScheduler().runDelayed(
                    EnchantmentReform.instance, location, scheduledTask -> task.run(), delayTicks));
        }
        return new SchedulerUtil(Bukkit.getScheduler().runTaskLater(EnchantmentReform.instance, task, delayTicks));
    }

    public static SchedulerUtil runTaskLaterAsynchronously(Runnable task, long delayTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            return new SchedulerUtil(Bukkit.getAsyncScheduler().runDelayed(
                    EnchantmentReform.instance, scheduledTask -> task.run(), delayTicks * 50L, TimeUnit.MILLISECONDS));
        }
        return new SchedulerUtil(Bukkit.getScheduler().runTaskLaterAsynchronously(EnchantmentReform.instance, task, delayTicks));
    }

    public static SchedulerUtil runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            periodTicks = validTicks(periodTicks);
            return new SchedulerUtil(Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    EnchantmentReform.instance, scheduledTask -> task.run(), delayTicks, periodTicks));
        }
        return new SchedulerUtil(Bukkit.getScheduler().runTaskTimer(EnchantmentReform.instance, task, delayTicks, periodTicks));
    }

    public static SchedulerUtil runTaskTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            periodTicks = validTicks(periodTicks);
            return new SchedulerUtil(entity.getScheduler().runAtFixedRate(
                    EnchantmentReform.instance, scheduledTask -> task.run(), null, delayTicks, periodTicks));
        }
        return runTaskTimer(task, delayTicks, periodTicks);
    }

    public static SchedulerUtil runTaskTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            periodTicks = validTicks(periodTicks);
            return new SchedulerUtil(Bukkit.getRegionScheduler().runAtFixedRate(
                    EnchantmentReform.instance, location, scheduledTask -> task.run(), delayTicks, periodTicks));
        }
        return runTaskTimer(task, delayTicks, periodTicks);
    }

    public static SchedulerUtil runTaskTimerAsynchronously(Runnable task, long delayTicks, long periodTicks) {
        if (EnchantmentReform.isFolia) {
            delayTicks = validTicks(delayTicks);
            periodTicks = validTicks(periodTicks);
            return new SchedulerUtil(Bukkit.getAsyncScheduler().runAtFixedRate(
                    EnchantmentReform.instance,
                    scheduledTask -> task.run(),
                    delayTicks * 50L,
                    periodTicks * 50L,
                    TimeUnit.MILLISECONDS));
        }
        return new SchedulerUtil(Bukkit.getScheduler().runTaskTimerAsynchronously(
                EnchantmentReform.instance, task, delayTicks, periodTicks));
    }

    public static void teleport(Entity entity, Location location) {
        if (EnchantmentReform.isFolia) {
            entity.teleportAsync(location);
            return;
        }
        entity.teleport(location);
    }

    private static long validTicks(long ticks) {
        return Math.max(1L, ticks);
    }
}
