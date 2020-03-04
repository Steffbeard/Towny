// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import org.bukkit.Location;
import java.util.UUID;
import com.google.common.base.Charsets;
import org.bukkit.OfflinePlayer;
import com.palmergames.bukkit.towny.TownySettings;
import de.themoep.idconverter.IdMappings;
import org.bukkit.Material;
import java.util.HashMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.PluginManager;
import org.bukkit.World;
import java.util.Iterator;
import java.util.Locale;
import org.bukkit.entity.Entity;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import java.util.Collection;
import org.bukkit.Server;
import com.palmergames.bukkit.towny.Towny;

public class BukkitTools
{
    private static Towny plugin;
    private static Server server;
    
    public static void initialize(final Towny plugin) {
        BukkitTools.plugin = plugin;
        BukkitTools.server = plugin.getServer();
    }
    
    public static Collection<? extends Player> getOnlinePlayers() {
        return (Collection<? extends Player>)getServer().getOnlinePlayers();
    }
    
    public static List<Player> matchPlayer(final String name) {
        final List<Player> matchedPlayers = new ArrayList<Player>();
        for (final Player iterPlayer : Bukkit.getOnlinePlayers()) {
            final String iterPlayerName = iterPlayer.getName();
            if (BukkitTools.plugin.isCitizens2() && CitizensAPI.getNPCRegistry().isNPC((Entity)iterPlayer)) {
                continue;
            }
            if (name.equalsIgnoreCase(iterPlayerName)) {
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (!iterPlayerName.toLowerCase(Locale.ENGLISH).contains(name.toLowerCase(Locale.ENGLISH))) {
                continue;
            }
            matchedPlayers.add(iterPlayer);
        }
        return matchedPlayers;
    }
    
    public static Player getPlayerExact(final String name) {
        return getServer().getPlayerExact(name);
    }
    
    public static Player getPlayer(final String playerId) {
        return getServer().getPlayer(playerId);
    }
    
    public static boolean isOnline(final String playerId) {
        for (final Player players : getOnlinePlayers()) {
            if (players.getName().equals(playerId)) {
                return true;
            }
        }
        return false;
    }
    
    public static List<World> getWorlds() {
        return (List<World>)getServer().getWorlds();
    }
    
    public static World getWorld(final String name) {
        return getServer().getWorld(name);
    }
    
    public static Server getServer() {
        synchronized (BukkitTools.server) {
            return BukkitTools.server;
        }
    }
    
    public static PluginManager getPluginManager() {
        return getServer().getPluginManager();
    }
    
    public static BukkitScheduler getScheduler() {
        return getServer().getScheduler();
    }
    
    public static int scheduleSyncDelayedTask(final Runnable task, final long delay) {
        return getScheduler().scheduleSyncDelayedTask((Plugin)BukkitTools.plugin, task, delay);
    }
    
    public static int scheduleAsyncDelayedTask(final Runnable task, final long delay) {
        return getScheduler().runTaskLaterAsynchronously((Plugin)BukkitTools.plugin, task, delay).getTaskId();
    }
    
    public static int scheduleSyncRepeatingTask(final Runnable task, final long delay, final long repeat) {
        return getScheduler().scheduleSyncRepeatingTask((Plugin)BukkitTools.plugin, task, delay, repeat);
    }
    
    public static int scheduleAsyncRepeatingTask(final Runnable task, final long delay, final long repeat) {
        return getScheduler().runTaskTimerAsynchronously((Plugin)BukkitTools.plugin, task, delay, repeat).getTaskId();
    }
    
    public static HashMap<String, Integer> getPlayersPerWorld() {
        final HashMap<String, Integer> m = new HashMap<String, Integer>();
        for (final World world : getServer().getWorlds()) {
            m.put(world.getName(), 0);
        }
        for (final Player player : getServer().getOnlinePlayers()) {
            m.put(player.getWorld().getName(), m.get(player.getWorld().getName()) + 1);
        }
        return m;
    }
    
    @Deprecated
    public static Material getMaterial(final int id) {
        return Material.getMaterial(IdMappings.getById(String.valueOf(id)).getFlatteningType());
    }
    
    public static int calcChunk(final int value) {
        return value * TownySettings.getTownBlockSize() / 16;
    }
    
    public static OfflinePlayer getOfflinePlayer(final String name) {
        return Bukkit.getOfflinePlayer(getPlayerExact(name).getUniqueId());
    }
    
    public static OfflinePlayer getOfflinePlayerForVault(final String name) {
        return Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)));
    }
    
    public static String convertCoordtoXYZ(final Location loc) {
        final String string = loc.getWorld().getName() + " " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
        return string;
    }
    
    public static boolean isSpigot() {
        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            return true;
        }
        catch (Throwable tr) {
            return false;
        }
    }
    
    static {
        BukkitTools.plugin = null;
        BukkitTools.server = null;
    }
}
