// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.Iterator;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import java.util.Map;
import java.util.Hashtable;
import com.palmergames.bukkit.towny.permissions.TownyPermissionSource;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import org.bukkit.block.Block;
import java.util.List;
import java.util.UUID;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;

@Deprecated
public class TownyUniverse
{
    public TownyUniverse(final Towny plugin) {
    }
    
    public Location getTownSpawnLocation(final Player player) {
        return TownyAPI.getInstance().getTownSpawnLocation(player);
    }
    
    public Location getNationSpawnLocation(final Player player) {
        return TownyAPI.getInstance().getNationSpawnLocation(player);
    }
    
    public static Player getPlayer(final Resident resident) {
        return TownyAPI.getInstance().getPlayer(resident);
    }
    
    public static UUID getPlayerUUID(final Resident resident) {
        return TownyAPI.getInstance().getPlayerUUID(resident);
    }
    
    public static List<Player> getOnlinePlayers(final ResidentList residentList) {
        return TownyAPI.getInstance().getOnlinePlayers(residentList);
    }
    
    public static List<Player> getOnlinePlayers(final Town town) {
        return TownyAPI.getInstance().getOnlinePlayers(town);
    }
    
    public static List<Player> getOnlinePlayers(final Nation nation) {
        return TownyAPI.getInstance().getOnlinePlayers(nation);
    }
    
    public static boolean isWilderness(final Block block) {
        return TownyAPI.getInstance().isWilderness(block.getLocation());
    }
    
    public static String getTownName(final Location location) {
        return TownyAPI.getInstance().getTownName(location);
    }
    
    public static UUID getTownUUID(final Location location) {
        return TownyAPI.getInstance().getTownUUID(location);
    }
    
    public static TownBlock getTownBlock(final Location location) {
        return TownyAPI.getInstance().getTownBlock(location);
    }
    
    public List<Resident> getActiveResidents() {
        return TownyAPI.getInstance().getActiveResidents();
    }
    
    public boolean isActiveResident(final Resident resident) {
        return TownyAPI.getInstance().isActiveResident(resident);
    }
    
    public String getRootFolder() {
        return com.palmergames.bukkit.towny.TownyUniverse.getInstance().getRootFolder();
    }
    
    public static TownyDataSource getDataSource() {
        return com.palmergames.bukkit.towny.TownyUniverse.getInstance().getDataSource();
    }
    
    public void setPermissionSource(final TownyPermissionSource permissionSource) {
        com.palmergames.bukkit.towny.TownyUniverse.getInstance().setPermissionSource(permissionSource);
    }
    
    public static TownyPermissionSource getPermissionSource() {
        return com.palmergames.bukkit.towny.TownyUniverse.getInstance().getPermissionSource();
    }
    
    public Hashtable<String, Resident> getResidentMap() {
        return new Hashtable<String, Resident>(com.palmergames.bukkit.towny.TownyUniverse.getInstance().getResidentMap());
    }
    
    public Hashtable<String, Town> getTownsMap() {
        return new Hashtable<String, Town>(com.palmergames.bukkit.towny.TownyUniverse.getInstance().getTownsMap());
    }
    
    public Hashtable<String, Nation> getNationsMap() {
        return new Hashtable<String, Nation>(com.palmergames.bukkit.towny.TownyUniverse.getInstance().getNationsMap());
    }
    
    public Hashtable<String, SiegeZone> getSiegeZonesMap() {
        return new Hashtable<String, SiegeZone>(com.palmergames.bukkit.towny.TownyUniverse.getInstance().getSiegeZonesMap());
    }
    
    public Hashtable<String, TownyWorld> getWorldMap() {
        return new Hashtable<String, TownyWorld>(com.palmergames.bukkit.towny.TownyUniverse.getInstance().getWorldMap());
    }
    
    public static boolean isWarTime() {
        return TownyAPI.getInstance().isWarTime();
    }
    
    public void startWarEvent() {
        com.palmergames.bukkit.towny.TownyUniverse.getInstance().startWarEvent();
    }
    
    public void endWarEvent() {
        com.palmergames.bukkit.towny.TownyUniverse.getInstance().endWarEvent();
    }
    
    public void clearWarEvent() {
        this.getWarEvent().cancelTasks(BukkitTools.getScheduler());
        this.setWarEvent(null);
    }
    
    public War getWarEvent() {
        return com.palmergames.bukkit.towny.TownyUniverse.getInstance().getWarEvent();
    }
    
    public void setWarEvent(final War event) {
        com.palmergames.bukkit.towny.TownyUniverse.getInstance().setWarEvent(event);
    }
    
    public List<String> getTreeString(final int depth) {
        return com.palmergames.bukkit.towny.TownyUniverse.getInstance().getTreeString(depth);
    }
    
    public void sendUniverseTree(final CommandSender commandSender) {
        for (final String line : this.getTreeString(0)) {
            commandSender.sendMessage(line);
        }
    }
    
    public static List<Resident> getOnlineResidents(final ResidentList residentList) {
        return TownyAPI.getInstance().getOnlineResidents(residentList);
    }
    
    public void requestTeleport(final Player player, final Location spawnLoc) {
        TownyAPI.getInstance().requestTeleport(player, spawnLoc);
    }
    
    public void abortTeleportRequest(final Resident resident) {
        TownyAPI.getInstance().abortTeleportRequest(resident);
    }
    
    public static void jailTeleport(final Player player, final Location location) {
        TownyAPI.getInstance().jailTeleport(player, location);
    }
    
    @Deprecated
    public void addWarZone(final WorldCoord worldCoord) {
        com.palmergames.bukkit.towny.TownyUniverse.getInstance().addWarZone(worldCoord);
    }
    
    @Deprecated
    public void removeWarZone(final WorldCoord worldCoord) {
        com.palmergames.bukkit.towny.TownyUniverse.getInstance().removeWarZone(worldCoord);
    }
}
