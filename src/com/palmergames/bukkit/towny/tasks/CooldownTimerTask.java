// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.TownySettings;
import java.util.Iterator;
import com.palmergames.bukkit.towny.Towny;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownTimerTask extends TownyTimerTask
{
    private static ConcurrentHashMap<AbstractMap.SimpleEntry<String, CooldownType>, Long> cooldowns;
    
    public CooldownTimerTask(final Towny plugin) {
        super(plugin);
        CooldownTimerTask.cooldowns = new ConcurrentHashMap<AbstractMap.SimpleEntry<String, CooldownType>, Long>();
    }
    
    @Override
    public void run() {
        final long currentTime = System.currentTimeMillis();
        if (!CooldownTimerTask.cooldowns.isEmpty()) {
            for (final AbstractMap.SimpleEntry<String, CooldownType> map : CooldownTimerTask.cooldowns.keySet()) {
                final long time = CooldownTimerTask.cooldowns.get(map);
                if (time < currentTime) {
                    CooldownTimerTask.cooldowns.remove(map);
                }
            }
        }
    }
    
    public static void addCooldownTimer(final String object, final CooldownType type) {
        final AbstractMap.SimpleEntry<String, CooldownType> map = new AbstractMap.SimpleEntry<String, CooldownType>(object, type);
        CooldownTimerTask.cooldowns.put(map, System.currentTimeMillis() + type.getSeconds() * 1000);
    }
    
    public static boolean hasCooldown(final String object, final CooldownType type) {
        final AbstractMap.SimpleEntry<String, CooldownType> map = new AbstractMap.SimpleEntry<String, CooldownType>(object, type);
        return CooldownTimerTask.cooldowns.containsKey(map);
    }
    
    public static int getCooldownRemaining(final String object, final CooldownType type) {
        final AbstractMap.SimpleEntry<String, CooldownType> map = new AbstractMap.SimpleEntry<String, CooldownType>(object, type);
        if (CooldownTimerTask.cooldowns.containsKey(map)) {
            return (int)((CooldownTimerTask.cooldowns.get(map) - System.currentTimeMillis()) / 1000L);
        }
        return 0;
    }
    
    public enum CooldownType
    {
        PVP(TownySettings.getPVPCoolDownTime()), 
        TELEPORT(TownySettings.getSpawnCooldownTime());
        
        private int seconds;
        
        private int getSeconds() {
            return this.seconds;
        }
        
        private CooldownType(final int seconds) {
            this.seconds = seconds;
        }
    }
}
