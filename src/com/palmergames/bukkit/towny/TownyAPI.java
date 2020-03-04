// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.tasks.TeleportWarmupTimerTask;
import com.palmergames.bukkit.towny.war.eventwar.War;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.permissions.TownyPermissionSource;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.World;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.block.Block;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import com.palmergames.bukkit.towny.object.ResidentList;
import java.util.UUID;
import java.util.Iterator;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownyAPI
{
    private static TownyAPI instance;
    private final Towny towny;
    private final TownyUniverse townyUniverse;
    
    private TownyAPI() {
        this.towny = Towny.getPlugin();
        this.townyUniverse = TownyUniverse.getInstance();
    }
    
    public Location getTownSpawnLocation(final Player player) {
        try {
            final Resident resident = this.townyUniverse.getDataSource().getResident(player.getName());
            final Town town = resident.getTown();
            return town.getSpawn();
        }
        catch (TownyException x) {
            return null;
        }
    }
    
    public Location getNationSpawnLocation(final Player player) {
        try {
            final Resident resident = this.townyUniverse.getDataSource().getResident(player.getName());
            final Nation nation = resident.getTown().getNation();
            return nation.getNationSpawn();
        }
        catch (TownyException x) {
            return null;
        }
    }
    
    public Player getPlayer(final Resident resident) {
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null && player.getName().equals(resident.getName())) {
                return player;
            }
        }
        return null;
    }
    
    public UUID getPlayerUUID(final Resident resident) {
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null && player.getName().equals(resident.getName())) {
                return player.getUniqueId();
            }
        }
        return null;
    }
    
    public List<Player> getOnlinePlayers(final ResidentList residentList) {
        final ArrayList<Player> players = new ArrayList<Player>();
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null && residentList.hasResident(player.getName())) {
                players.add(player);
            }
        }
        return players;
    }
    
    public List<Player> getOnlinePlayers(final Town town) {
        final ArrayList<Player> players = new ArrayList<Player>();
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null && town.hasResident(player.getName())) {
                players.add(player);
            }
        }
        return players;
    }
    
    public List<Player> getOnlinePlayers(final Nation nation) {
        final ArrayList<Player> players = new ArrayList<Player>();
        for (final Town town : nation.getTowns()) {
            players.addAll(this.getOnlinePlayers(town));
        }
        return players;
    }
    
    public List<Player> getOnlinePlayersAlliance(final Nation nation) {
        final ArrayList<Player> players = new ArrayList<Player>();
        players.addAll(this.getOnlinePlayers(nation));
        if (!nation.getAllies().isEmpty()) {
            for (final Nation nations : nation.getAllies()) {
                players.addAll(this.getOnlinePlayers(nations));
            }
        }
        return players;
    }
    
    @Deprecated
    public boolean isWilderness(final Block block) {
        WorldCoord worldCoord;
        try {
            worldCoord = new WorldCoord(this.townyUniverse.getDataSource().getWorld(block.getWorld().getName()).getName(), Coord.parseCoord(block));
        }
        catch (NotRegisteredException e) {
            return true;
        }
        try {
            return worldCoord.getTownBlock().getTown() == null;
        }
        catch (NotRegisteredException e) {
            return true;
        }
    }
    
    public boolean isWilderness(final Location location) {
        WorldCoord worldCoord;
        try {
            worldCoord = new WorldCoord(this.townyUniverse.getDataSource().getWorld(location.getWorld().getName()).getName(), Coord.parseCoord(location));
        }
        catch (NotRegisteredException e) {
            return true;
        }
        try {
            return worldCoord.getTownBlock().getTown() == null;
        }
        catch (NotRegisteredException e) {
            return true;
        }
    }
    
    public boolean isTownyWorld(final World world) {
        try {
            return this.townyUniverse.getDataSource().getWorld(world.getName()).isUsingTowny();
        }
        catch (NotRegisteredException e) {
            return false;
        }
    }
    
    public String getTownName(final Location location) {
        try {
            final WorldCoord worldCoord = new WorldCoord(this.townyUniverse.getDataSource().getWorld(location.getWorld().getName()).getName(), Coord.parseCoord(location));
            return worldCoord.getTownBlock().getTown().getName();
        }
        catch (NotRegisteredException e) {
            return null;
        }
    }
    
    public UUID getTownUUID(final Location location) {
        try {
            final WorldCoord worldCoord = new WorldCoord(this.townyUniverse.getDataSource().getWorld(location.getWorld().getName()).getName(), Coord.parseCoord(location));
            return worldCoord.getTownBlock().getTown().getUuid();
        }
        catch (NotRegisteredException e) {
            return null;
        }
    }
    
    public TownBlock getTownBlock(final Location location) {
        try {
            final WorldCoord worldCoord = new WorldCoord(this.townyUniverse.getDataSource().getWorld(location.getWorld().getName()).getName(), Coord.parseCoord(location));
            return worldCoord.getTownBlock();
        }
        catch (NotRegisteredException e) {
            return null;
        }
    }
    
    public boolean hasTownBlock(final Location location) {
        try {
            return this.townyUniverse.getDataSource().getWorld(location.getWorld().getName()).hasTownBlock(Coord.parseCoord(location));
        }
        catch (NotRegisteredException e) {
            return false;
        }
    }
    
    public List<Resident> getActiveResidents() {
        final List<Resident> activeResidents = new ArrayList<Resident>();
        for (final Resident resident : this.townyUniverse.getDataSource().getResidents()) {
            if (this.isActiveResident(resident)) {
                activeResidents.add(resident);
            }
        }
        return activeResidents;
    }
    
    public boolean isActiveResident(final Resident resident) {
        return System.currentTimeMillis() - resident.getLastOnline() < 20L * TownySettings.getInactiveAfter() || BukkitTools.isOnline(resident.getName());
    }
    
    public TownyDataSource getDataSource() {
        return this.townyUniverse.getDataSource();
    }
    
    @Deprecated
    public TownyPermissionSource getPermissionSource() {
        return this.townyUniverse.getPermissionSource();
    }
    
    public boolean isWarTime() {
        return this.townyUniverse.getWarEvent() != null && this.townyUniverse.getWarEvent().isWarTime();
    }
    
    public List<Resident> getOnlineResidents(final ResidentList residentList) {
        final List<Resident> onlineResidents = new ArrayList<Resident>();
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                for (final Resident resident : residentList.getResidents()) {
                    if (resident.getName().equalsIgnoreCase(player.getName())) {
                        onlineResidents.add(resident);
                    }
                }
            }
        }
        return onlineResidents;
    }
    
    public void jailTeleport(final Player player, final Location location) {
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.towny, () -> player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN), (long)(TownySettings.getTeleportWarmupTime() * 20));
    }
    
    @Deprecated
    public War getWarEvent() {
        return TownyUniverse.getInstance().getWarEvent();
    }
    
    public void clearWarEvent() {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.getWarEvent().cancelTasks(BukkitTools.getScheduler());
        townyUniverse.setWarEvent(null);
    }
    
    public void requestTeleport(final Player player, final Location spawnLoc) {
        try {
            TeleportWarmupTimerTask.requestTeleport(this.getDataSource().getResident(player.getName().toLowerCase()), spawnLoc);
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    public void abortTeleportRequest(final Resident resident) {
        TeleportWarmupTimerTask.abortTeleportRequest(resident);
    }
    
    public void registerCustomDataField(final CustomDataField field) throws KeyAlreadyRegisteredException {
        this.townyUniverse.addCustomCustomDataField(field);
    }
    
    public static TownyAPI getInstance() {
        if (TownyAPI.instance == null) {
            TownyAPI.instance = new TownyAPI();
        }
        return TownyAPI.instance;
    }
}
