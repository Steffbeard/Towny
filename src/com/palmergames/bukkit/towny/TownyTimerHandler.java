// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import java.util.TimeZone;
import java.util.Calendar;
import com.palmergames.bukkit.towny.tasks.DrawSmokeTask;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import com.palmergames.bukkit.towny.tasks.TeleportWarmupTimerTask;
import com.palmergames.bukkit.towny.tasks.HealthRegenTimerTask;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarTimerTask;
import com.palmergames.util.TimeMgmt;
import com.palmergames.bukkit.towny.tasks.MobRemovalTimerTask;
import com.palmergames.util.TimeTools;
import com.palmergames.bukkit.towny.tasks.RepeatingTimerTask;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.tasks.DailyTimerTask;

public class TownyTimerHandler
{
    private static Towny plugin;
    private static int townyRepeatingTask;
    private static int dailyTask;
    private static int siegeWarTask;
    private static int mobRemoveTask;
    private static int healthRegenTask;
    private static int teleportWarmupTask;
    private static int cooldownTimerTask;
    private static int drawSmokeTask;
    
    public static void initialize(final Towny plugin) {
        TownyTimerHandler.plugin = plugin;
    }
    
    public static void newDay() {
        if (!isDailyTimerRunning()) {
            toggleDailyTimer(true);
        }
        if (TownySettings.isEconomyAsync()) {
            if (BukkitTools.scheduleAsyncDelayedTask(new DailyTimerTask(TownyTimerHandler.plugin), 0L) == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule newDay.");
            }
        }
        else if (BukkitTools.scheduleSyncDelayedTask(new DailyTimerTask(TownyTimerHandler.plugin), 0L) == -1) {
            TownyMessaging.sendErrorMsg("Could not schedule newDay.");
        }
    }
    
    public static void toggleTownyRepeatingTimer(final boolean on) {
        if (on && !isTownyRepeatingTaskRunning()) {
            TownyTimerHandler.townyRepeatingTask = BukkitTools.scheduleSyncRepeatingTask(new RepeatingTimerTask(TownyTimerHandler.plugin), 0L, TimeTools.convertToTicks(1L));
            if (TownyTimerHandler.townyRepeatingTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule Towny Timer Task.");
            }
        }
        else if (!on && isTownyRepeatingTaskRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.townyRepeatingTask);
            TownyTimerHandler.townyRepeatingTask = -1;
        }
    }
    
    public static void toggleMobRemoval(final boolean on) {
        if (on && !isMobRemovalRunning()) {
            TownyTimerHandler.mobRemoveTask = BukkitTools.scheduleSyncRepeatingTask(new MobRemovalTimerTask(TownyTimerHandler.plugin, BukkitTools.getServer()), 0L, TimeTools.convertToTicks(TownySettings.getMobRemovalSpeed()));
            if (TownyTimerHandler.mobRemoveTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule mob removal loop.");
            }
        }
        else if (!on && isMobRemovalRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.mobRemoveTask);
            TownyTimerHandler.mobRemoveTask = -1;
        }
    }
    
    public static void toggleDailyTimer(final boolean on) {
        if (on && !isDailyTimerRunning()) {
            final long timeTillNextDay = townyTime();
            System.out.println("[Towny] Time until a New Day: " + TimeMgmt.formatCountdownTime(timeTillNextDay));
            if (TownySettings.isEconomyAsync()) {
                TownyTimerHandler.dailyTask = BukkitTools.scheduleAsyncRepeatingTask(new DailyTimerTask(TownyTimerHandler.plugin), TimeTools.convertToTicks(timeTillNextDay), TimeTools.convertToTicks(TownySettings.getDayInterval()));
            }
            else {
                TownyTimerHandler.dailyTask = BukkitTools.scheduleSyncRepeatingTask(new DailyTimerTask(TownyTimerHandler.plugin), TimeTools.convertToTicks(timeTillNextDay), TimeTools.convertToTicks(TownySettings.getDayInterval()));
            }
            if (TownyTimerHandler.dailyTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule new day loop.");
            }
        }
        else if (!on && isDailyTimerRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.dailyTask);
            TownyTimerHandler.dailyTask = -1;
        }
    }
    
    public static void toggleSiegeWarTimer(final boolean on) {
        if (!TownySettings.getWarSiegeEnabled()) {
            return;
        }
        if (on && !isSiegeWarTimerRunning()) {
            final long delayTicks = TimeTools.convertToTicks(60L);
            TownyTimerHandler.siegeWarTask = BukkitTools.scheduleAsyncRepeatingTask(new SiegeWarTimerTask(TownyTimerHandler.plugin), delayTicks, TimeTools.convertToTicks(TownySettings.getWarSiegeTimerIntervalSeconds()));
            if (TownyTimerHandler.siegeWarTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule siege war timer.");
            }
        }
        else if (!on && isDailyTimerRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.siegeWarTask);
            TownyTimerHandler.siegeWarTask = -1;
        }
    }
    
    public static void toggleHealthRegen(final boolean on) {
        if (on && !isHealthRegenRunning()) {
            TownyTimerHandler.healthRegenTask = BukkitTools.scheduleSyncRepeatingTask(new HealthRegenTimerTask(TownyTimerHandler.plugin, BukkitTools.getServer()), 0L, TimeTools.convertToTicks(TownySettings.getHealthRegenSpeed()));
            if (TownyTimerHandler.healthRegenTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule health regen loop.");
            }
        }
        else if (!on && isHealthRegenRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.healthRegenTask);
            TownyTimerHandler.healthRegenTask = -1;
        }
    }
    
    public static void toggleTeleportWarmup(final boolean on) {
        if (on && !isTeleportWarmupRunning()) {
            TownyTimerHandler.teleportWarmupTask = BukkitTools.scheduleSyncRepeatingTask(new TeleportWarmupTimerTask(TownyTimerHandler.plugin), 0L, 20L);
            if (TownyTimerHandler.teleportWarmupTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule teleport warmup loop.");
            }
        }
        else if (!on && isTeleportWarmupRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.teleportWarmupTask);
            TownyTimerHandler.teleportWarmupTask = -1;
        }
    }
    
    public static void toggleCooldownTimer(final boolean on) {
        if (on && !isCooldownTimerRunning()) {
            TownyTimerHandler.cooldownTimerTask = BukkitTools.scheduleAsyncRepeatingTask(new CooldownTimerTask(TownyTimerHandler.plugin), 0L, 20L);
            if (TownyTimerHandler.cooldownTimerTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule cooldown timer loop.");
            }
        }
        else if (!on && isCooldownTimerRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.cooldownTimerTask);
            TownyTimerHandler.cooldownTimerTask = -1;
        }
    }
    
    public static void toggleDrawSmokeTask(final boolean on) {
        if (on && !isDrawSmokeTaskRunning()) {
            TownyTimerHandler.drawSmokeTask = BukkitTools.scheduleAsyncRepeatingTask(new DrawSmokeTask(TownyTimerHandler.plugin), 0L, 100L);
            if (TownyTimerHandler.drawSmokeTask == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule draw smoke loop");
            }
        }
        else if (!on && isDrawSmokeTaskRunning()) {
            BukkitTools.getScheduler().cancelTask(TownyTimerHandler.drawSmokeTask);
            TownyTimerHandler.drawSmokeTask = -1;
        }
    }
    
    public static boolean isTownyRepeatingTaskRunning() {
        return TownyTimerHandler.townyRepeatingTask != -1;
    }
    
    public static boolean isMobRemovalRunning() {
        return TownyTimerHandler.mobRemoveTask != -1;
    }
    
    public static boolean isDailyTimerRunning() {
        return TownyTimerHandler.dailyTask != -1;
    }
    
    public static boolean isSiegeWarTimerRunning() {
        return TownyTimerHandler.siegeWarTask != -1;
    }
    
    public static boolean isHealthRegenRunning() {
        return TownyTimerHandler.healthRegenTask != -1;
    }
    
    public static boolean isTeleportWarmupRunning() {
        return TownyTimerHandler.teleportWarmupTask != -1;
    }
    
    public static boolean isCooldownTimerRunning() {
        return TownyTimerHandler.cooldownTimerTask != -1;
    }
    
    public static boolean isDrawSmokeTaskRunning() {
        return TownyTimerHandler.drawSmokeTask != -1;
    }
    
    public static Long townyTime() {
        final long secondsInDay = TownySettings.getDayInterval();
        final Calendar now = Calendar.getInstance();
        final TimeZone timeZone = now.getTimeZone();
        final long timeMilli = System.currentTimeMillis();
        final int timeOffset = timeZone.getOffset(timeMilli) / 1000;
        return (secondsInDay + (TownySettings.getNewDayTime() - timeMilli / 1000L % secondsInDay - timeOffset)) % secondsInDay;
    }
    
    static {
        TownyTimerHandler.townyRepeatingTask = -1;
        TownyTimerHandler.dailyTask = -1;
        TownyTimerHandler.siegeWarTask = -1;
        TownyTimerHandler.mobRemoveTask = -1;
        TownyTimerHandler.healthRegenTask = -1;
        TownyTimerHandler.teleportWarmupTask = -1;
        TownyTimerHandler.cooldownTimerTask = -1;
        TownyTimerHandler.drawSmokeTask = -1;
    }
}
