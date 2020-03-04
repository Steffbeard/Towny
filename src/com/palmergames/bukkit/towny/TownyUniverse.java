// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import java.util.UUID;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import java.util.Collection;
import java.util.Arrays;
import org.bukkit.World;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.tasks.OnPlayerLogin;
import org.bukkit.entity.Player;
import java.util.Iterator;
import com.palmergames.util.FileMgmt;
import com.palmergames.bukkit.towny.db.TownySQLSource;
import com.palmergames.bukkit.towny.db.TownyFlatFileSource;
import com.palmergames.bukkit.towny.object.Coord;
import java.io.IOException;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import java.io.File;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.towny.permissions.TownyPermissionSource;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import java.util.List;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import java.util.HashMap;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.concurrent.ConcurrentHashMap;

public class TownyUniverse
{
    private static TownyUniverse instance;
    private final Towny towny;
    private final ConcurrentHashMap<String, Resident> residents;
    private final ConcurrentHashMap<String, Town> towns;
    private final ConcurrentHashMap<String, Nation> nations;
    private final ConcurrentHashMap<String, SiegeZone> siegeZones;
    private final ConcurrentHashMap<String, TownyWorld> worlds;
    private final HashMap<String, CustomDataField> registeredMetadata;
    private final List<Resident> jailedResidents;
    private final String rootFolder;
    private TownyDataSource dataSource;
    private TownyPermissionSource permissionSource;
    private War warEvent;
    
    private TownyUniverse() {
        this.residents = new ConcurrentHashMap<String, Resident>();
        this.towns = new ConcurrentHashMap<String, Town>();
        this.nations = new ConcurrentHashMap<String, Nation>();
        this.siegeZones = new ConcurrentHashMap<String, SiegeZone>();
        this.worlds = new ConcurrentHashMap<String, TownyWorld>();
        this.registeredMetadata = new HashMap<String, CustomDataField>();
        this.jailedResidents = new ArrayList<Resident>();
        this.towny = Towny.getPlugin();
        this.rootFolder = this.towny.getDataFolder().getPath();
    }
    
    boolean loadSettings() {
        try {
            TownySettings.loadConfig(this.rootFolder + File.separator + "settings" + File.separator + "config.yml", this.towny.getVersion());
            TownySettings.loadLanguage(this.rootFolder + File.separator + "settings", "english.yml");
            TownyPerms.loadPerms(this.rootFolder + File.separator + "settings", "townyperms.yml");
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        TownyLogger.getInstance();
        final String saveDbType = TownySettings.getSaveDatabase();
        final String loadDbType = TownySettings.getLoadDatabase();
        Coord.setCellSize(TownySettings.getTownBlockSize());
        System.out.println("[Towny] Database: [Load] " + loadDbType + " [Save] " + saveDbType);
        this.clearAll();
        if (!this.loadDatabase(loadDbType)) {
            System.out.println("[Towny] Error: Failed to load!");
            return false;
        }
        try {
            this.dataSource.cleanupBackups();
            final String lowerCase = saveDbType.toLowerCase();
            switch (lowerCase) {
                case "ff":
                case "flatfile": {
                    this.dataSource = new TownyFlatFileSource(this.towny, this);
                    break;
                }
                case "h2":
                case "sqlite":
                case "mysql": {
                    this.dataSource = new TownySQLSource(this.towny, this, saveDbType.toLowerCase());
                    break;
                }
            }
            FileMgmt.checkOrCreateFolder(this.rootFolder + File.separator + "logs");
            try {
                this.dataSource.backup();
                if (loadDbType.equalsIgnoreCase("flatfile") || saveDbType.equalsIgnoreCase("flatfile")) {
                    this.dataSource.deleteUnusedResidents();
                }
            }
            catch (IOException e2) {
                System.out.println("[Towny] Error: Could not create backup.");
                e2.printStackTrace();
                return false;
            }
            if (loadDbType.equalsIgnoreCase(saveDbType)) {
                this.dataSource.saveAllWorlds();
            }
            else {
                this.dataSource.saveAll();
            }
        }
        catch (UnsupportedOperationException e3) {
            System.out.println("[Towny] Error: Unsupported save format!");
            return false;
        }
        final File f = new File(this.rootFolder, "outpostschecked.txt");
        if (!f.exists()) {
            for (final Town town : this.dataSource.getTowns()) {
                TownySQLSource.validateTownOutposts(town);
            }
            this.towny.saveResource("outpostschecked.txt", false);
        }
        return true;
    }
    
    private boolean loadDatabase(final String loadDbType) {
        final String lowerCase = loadDbType.toLowerCase();
        switch (lowerCase) {
            case "ff":
            case "flatfile": {
                this.dataSource = new TownyFlatFileSource(this.towny, this);
                break;
            }
            case "h2":
            case "sqlite":
            case "mysql": {
                this.dataSource = new TownySQLSource(this.towny, this, loadDbType.toLowerCase());
                break;
            }
            default: {
                return false;
            }
        }
        return this.dataSource.loadAll();
    }
    
    public void onLogin(final Player player) {
        if (!player.isOnline()) {
            return;
        }
        player.getName();
        if (player.getName().contains(" ")) {
            player.kickPlayer("Invalid name!");
            return;
        }
        if (BukkitTools.scheduleSyncDelayedTask(new OnPlayerLogin(this.towny, player), 0L) == -1) {
            TownyMessaging.sendErrorMsg("Could not schedule OnLogin.");
        }
    }
    
    public void onLogout(final Player player) {
        try {
            final Resident resident = this.dataSource.getResident(player.getName());
            resident.setLastOnline(System.currentTimeMillis());
            this.dataSource.saveResident(resident);
        }
        catch (NotRegisteredException ex) {}
    }
    
    public void startWarEvent() {
        this.warEvent = new War(this.towny, TownySettings.getWarTimeWarningDelay());
    }
    
    public void endWarEvent() {
        if (this.warEvent != null && this.warEvent.isWarTime()) {
            this.warEvent.toggleEnd();
        }
    }
    
    public void addWarZone(final WorldCoord worldCoord) {
        try {
            if (worldCoord.getTownyWorld().isWarAllowed()) {
                worldCoord.getTownyWorld().addWarZone(worldCoord);
            }
        }
        catch (NotRegisteredException ex) {}
        this.towny.updateCache(worldCoord);
    }
    
    public void removeWarZone(final WorldCoord worldCoord) {
        try {
            worldCoord.getTownyWorld().removeWarZone(worldCoord);
        }
        catch (NotRegisteredException ex) {}
        this.towny.updateCache(worldCoord);
    }
    
    public TownyPermissionSource getPermissionSource() {
        return this.permissionSource;
    }
    
    public void setPermissionSource(final TownyPermissionSource permissionSource) {
        this.permissionSource = permissionSource;
    }
    
    public War getWarEvent() {
        return this.warEvent;
    }
    
    public void setWarEvent(final War warEvent) {
        this.warEvent = warEvent;
    }
    
    public String getRootFolder() {
        return this.rootFolder;
    }
    
    public ConcurrentHashMap<String, Nation> getNationsMap() {
        return this.nations;
    }
    
    public ConcurrentHashMap<String, SiegeZone> getSiegeZonesMap() {
        return this.siegeZones;
    }
    
    public ConcurrentHashMap<String, Resident> getResidentMap() {
        return this.residents;
    }
    
    public List<Resident> getJailedResidentMap() {
        return this.jailedResidents;
    }
    
    public ConcurrentHashMap<String, Town> getTownsMap() {
        return this.towns;
    }
    
    public ConcurrentHashMap<String, TownyWorld> getWorldMap() {
        return this.worlds;
    }
    
    public TownyDataSource getDataSource() {
        return this.dataSource;
    }
    
    public List<String> getTreeString(final int depth) {
        final List<String> out = new ArrayList<String>();
        out.add(this.getTreeDepth(depth) + "Universe (1)");
        if (this.towny != null) {
            out.add(this.getTreeDepth(depth + 1) + "Server (" + BukkitTools.getServer().getName() + ")");
            out.add(this.getTreeDepth(depth + 2) + "Version: " + BukkitTools.getServer().getVersion());
            out.add(this.getTreeDepth(depth + 2) + "Worlds (" + BukkitTools.getWorlds().size() + "): " + Arrays.toString(BukkitTools.getWorlds().toArray(new World[0])));
        }
        out.add(this.getTreeDepth(depth + 1) + "Worlds (" + this.worlds.size() + "):");
        for (final TownyWorld world : this.worlds.values()) {
            out.addAll(world.getTreeString(depth + 2));
        }
        out.add(this.getTreeDepth(depth + 1) + "Nations (" + this.nations.size() + "):");
        for (final Nation nation : this.nations.values()) {
            out.addAll(nation.getTreeString(depth + 2));
        }
        final Collection<Town> townsWithoutNation = this.dataSource.getTownsWithoutNation();
        out.add(this.getTreeDepth(depth + 1) + "Towns (" + townsWithoutNation.size() + "):");
        for (final Town town : townsWithoutNation) {
            out.addAll(town.getTreeString(depth + 2));
        }
        final Collection<Resident> residentsWithoutTown = this.dataSource.getResidentsWithoutTown();
        out.add(this.getTreeDepth(depth + 1) + "Residents (" + residentsWithoutTown.size() + "):");
        for (final Resident resident : residentsWithoutTown) {
            out.addAll(resident.getTreeString(depth + 2));
        }
        return out;
    }
    
    private String getTreeDepth(final int depth) {
        final char[] fill = new char[depth * 4];
        Arrays.fill(fill, ' ');
        if (depth > 0) {
            fill[0] = '|';
            final int offset = (depth - 1) * 4;
            fill[offset] = '+';
            fill[offset + 2] = (fill[offset + 1] = '-');
        }
        return new String(fill);
    }
    
    public boolean isTownBlockLocContainedInTownOutposts(final List<Location> minecraftcoordinates, final TownBlock tb) {
        if (minecraftcoordinates != null && tb != null) {
            for (final Location minecraftcoordinate : minecraftcoordinates) {
                if (Coord.parseCoord(minecraftcoordinate).equals(tb.getCoord())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void addCustomCustomDataField(final CustomDataField cdf) throws KeyAlreadyRegisteredException {
        if (this.getRegisteredMetadataMap().containsKey(cdf.getKey())) {
            throw new KeyAlreadyRegisteredException();
        }
        this.getRegisteredMetadataMap().put(cdf.getKey(), cdf);
    }
    
    public static TownyUniverse getInstance() {
        if (TownyUniverse.instance == null) {
            TownyUniverse.instance = new TownyUniverse();
        }
        return TownyUniverse.instance;
    }
    
    public void clearAll() {
        this.worlds.clear();
        this.nations.clear();
        this.towns.clear();
        this.residents.clear();
    }
    
    public boolean hasGroup(final String townName, final UUID groupID) {
        final Town t = this.towns.get(townName);
        return t != null && t.getObjectGroupFromID(groupID) != null;
    }
    
    public boolean hasGroup(final String townName, final String groupName) {
        final Town t = this.towns.get(townName);
        return t != null && t.hasObjectGroupName(groupName);
    }
    
    public Collection<PlotObjectGroup> getGroups() {
        final List<PlotObjectGroup> groups = new ArrayList<PlotObjectGroup>();
        for (final Town town : this.towns.values()) {
            if (town.hasObjectGroups()) {
                groups.addAll(town.getPlotObjectGroups());
            }
        }
        return groups;
    }
    
    public PlotObjectGroup getGroup(final String townName, final UUID groupID) {
        Town t = null;
        try {
            t = getInstance().getDataSource().getTown(townName);
        }
        catch (NotRegisteredException e) {
            return null;
        }
        if (t != null) {
            return t.getObjectGroupFromID(groupID);
        }
        return null;
    }
    
    public PlotObjectGroup getGroup(final String townName, final String groupName) {
        final Town t = this.towns.get(townName);
        if (t != null) {
            return t.getPlotObjectGroupFromName(groupName);
        }
        return null;
    }
    
    public HashMap<String, CustomDataField> getRegisteredMetadataMap() {
        return this.getRegisteredMetadata();
    }
    
    public PlotObjectGroup newGroup(final Town town, final String name, final UUID id) throws AlreadyRegisteredException {
        final PlotObjectGroup newGroup = new PlotObjectGroup(id, name, town);
        if (town.hasObjectGroupName(newGroup.getGroupName())) {
            TownyMessaging.sendErrorMsg("group " + town.getName() + ":" + id + " already exists");
            throw new AlreadyRegisteredException();
        }
        town.addPlotGroup(newGroup);
        return newGroup;
    }
    
    public UUID generatePlotGroupID() {
        return UUID.randomUUID();
    }
    
    public void removeGroup(final PlotObjectGroup group) {
        group.getTown().removePlotGroup(group);
    }
    
    public HashMap<String, CustomDataField> getRegisteredMetadata() {
        return this.registeredMetadata;
    }
}
