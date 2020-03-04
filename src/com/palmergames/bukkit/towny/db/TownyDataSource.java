// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.db;

import java.util.Set;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import java.util.UUID;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.entity.Player;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;
import java.util.concurrent.locks.Lock;

public abstract class TownyDataSource
{
    final Lock lock;
    protected final Towny plugin;
    protected final TownyUniverse universe;
    
    TownyDataSource(final Towny plugin, final TownyUniverse universe) {
        this.lock = new ReentrantLock();
        this.plugin = plugin;
        this.universe = universe;
    }
    
    public abstract boolean backup() throws IOException;
    
    public abstract void cleanupBackups();
    
    public abstract void deleteUnusedResidents();
    
    public boolean loadAll() {
        return this.loadWorldList() && this.loadNationList() && this.loadTownList() && this.loadPlotGroupList() && this.loadSiegeZoneList() && this.loadResidentList() && this.loadTownBlockList() && this.loadWorlds() && this.loadNations() && this.loadTowns() && this.loadSiegeZones() && this.loadResidents() && this.loadTownBlocks() && this.loadPlotGroups() && this.loadRegenList() && this.loadSnapshotList();
    }
    
    public boolean saveAll() {
        return this.saveWorldList() && this.saveNationList() && this.saveTownList() && this.savePlotGroupList() && this.saveSiegeZoneList() && this.saveResidentList() && this.saveTownBlockList() && this.saveWorlds() && this.saveNations() && this.saveTowns() && this.saveResidents() && this.savePlotGroups() && this.saveSiegeZones() && this.saveAllTownBlocks() && this.saveRegenList() && this.saveSnapshotList();
    }
    
    public boolean saveAllWorlds() {
        return this.saveWorldList() && this.saveWorlds();
    }
    
    public boolean saveQueues() {
        return this.saveRegenList() && this.saveSnapshotList();
    }
    
    public abstract void cancelTask();
    
    public abstract boolean loadTownBlockList();
    
    public abstract boolean loadResidentList();
    
    public abstract boolean loadTownList();
    
    public abstract boolean loadNationList();
    
    public abstract boolean loadSiegeZoneList();
    
    public abstract boolean loadWorldList();
    
    public abstract boolean loadRegenList();
    
    public abstract boolean loadSnapshotList();
    
    public abstract boolean loadTownBlocks();
    
    public abstract boolean loadResident(final Resident p0);
    
    public abstract boolean loadTown(final Town p0);
    
    public abstract boolean loadNation(final Nation p0);
    
    public abstract boolean loadSiegeZone(final SiegeZone p0);
    
    public abstract boolean loadWorld(final TownyWorld p0);
    
    public abstract boolean loadPlotGroupList();
    
    public abstract boolean loadPlotGroups();
    
    public abstract boolean saveTownBlockList();
    
    public abstract boolean saveResidentList();
    
    public abstract boolean saveTownList();
    
    public abstract boolean savePlotGroupList();
    
    public abstract boolean saveNationList();
    
    public abstract boolean saveSiegeZoneList();
    
    public abstract boolean saveWorldList();
    
    public abstract boolean saveRegenList();
    
    public abstract boolean saveSnapshotList();
    
    public abstract boolean saveResident(final Resident p0);
    
    public abstract boolean saveTown(final Town p0);
    
    public abstract boolean savePlotGroup(final PlotObjectGroup p0);
    
    public abstract boolean saveNation(final Nation p0);
    
    public abstract boolean saveSiegeZone(final SiegeZone p0);
    
    public abstract boolean saveWorld(final TownyWorld p0);
    
    public abstract boolean saveAllTownBlocks();
    
    public abstract boolean saveTownBlock(final TownBlock p0);
    
    public abstract boolean savePlotData(final PlotBlockData p0);
    
    public abstract PlotBlockData loadPlotData(final String p0, final int p1, final int p2);
    
    public abstract PlotBlockData loadPlotData(final TownBlock p0);
    
    public abstract void deletePlotData(final PlotBlockData p0);
    
    public abstract void deleteResident(final Resident p0);
    
    public abstract void deleteTown(final Town p0);
    
    public abstract void deleteNation(final Nation p0);
    
    public abstract void deleteSiegeZone(final SiegeZone p0);
    
    public abstract void deleteWorld(final TownyWorld p0);
    
    public abstract void deleteTownBlock(final TownBlock p0);
    
    public abstract void deleteFile(final String p0);
    
    public abstract void deletePlotGroup(final PlotObjectGroup p0);
    
    public boolean cleanup() {
        return true;
    }
    
    public boolean loadResidents() {
        TownyMessaging.sendDebugMsg("Loading Residents");
        final List<Resident> toRemove = new ArrayList<Resident>();
        for (final Resident resident : new ArrayList<Resident>(this.getResidents())) {
            if (!this.loadResident(resident)) {
                System.out.println("[Towny] Loading Error: Could not read resident data '" + resident.getName() + "'.");
                toRemove.add(resident);
            }
        }
        for (final Resident resident : toRemove) {
            System.out.println("[Towny] Loading Error: Removing resident data for '" + resident.getName() + "'.");
            this.removeResidentList(resident);
        }
        return true;
    }
    
    public boolean loadTowns() {
        TownyMessaging.sendDebugMsg("Loading Towns");
        for (final Town town : this.getTowns()) {
            if (!this.loadTown(town)) {
                System.out.println("[Towny] Loading Error: Could not read town data '" + town.getName() + "'.");
                return false;
            }
        }
        return true;
    }
    
    public boolean loadNations() {
        TownyMessaging.sendDebugMsg("Loading Nations");
        for (final Nation nation : this.getNations()) {
            if (!this.loadNation(nation)) {
                System.out.println("[Towny] Loading Error: Could not read nation data '" + nation.getName() + "'.");
                return false;
            }
        }
        return true;
    }
    
    public boolean loadSiegeZones() {
        TownyMessaging.sendDebugMsg("Loading Siege Zones");
        for (final SiegeZone siegeZone : this.getSiegeZones()) {
            if (!this.loadSiegeZone(siegeZone)) {
                System.out.println("[Towny] Loading Error: Could not read siege zone data '" + siegeZone.getName() + "'.");
                return false;
            }
        }
        return true;
    }
    
    public boolean loadWorlds() {
        TownyMessaging.sendDebugMsg("Loading Worlds");
        for (final TownyWorld world : this.getWorlds()) {
            if (!this.loadWorld(world)) {
                System.out.println("[Towny] Loading Error: Could not read world data '" + world.getName() + "'.");
                return false;
            }
        }
        return true;
    }
    
    public boolean saveResidents() {
        TownyMessaging.sendDebugMsg("Saving Residents");
        for (final Resident resident : this.getResidents()) {
            this.saveResident(resident);
        }
        return true;
    }
    
    public boolean savePlotGroups() {
        TownyMessaging.sendDebugMsg("Saving PlotGroups");
        for (final PlotObjectGroup plotGroup : this.getAllPlotGroups()) {
            this.savePlotGroup(plotGroup);
        }
        return true;
    }
    
    public boolean saveTowns() {
        TownyMessaging.sendDebugMsg("Saving Towns");
        for (final Town town : this.getTowns()) {
            this.saveTown(town);
        }
        return true;
    }
    
    public boolean saveNations() {
        TownyMessaging.sendDebugMsg("Saving Nations");
        for (final Nation nation : this.getNations()) {
            this.saveNation(nation);
        }
        return true;
    }
    
    public boolean saveSiegeZones() {
        TownyMessaging.sendDebugMsg("Saving Siege Zones");
        for (final SiegeZone siegeZone : this.getSiegeZones()) {
            this.saveSiegeZone(siegeZone);
        }
        return true;
    }
    
    public boolean saveWorlds() {
        TownyMessaging.sendDebugMsg("Saving Worlds");
        for (final TownyWorld world : this.getWorlds()) {
            this.saveWorld(world);
        }
        return true;
    }
    
    public abstract List<Resident> getResidents(final Player p0, final String[] p1);
    
    public abstract List<Resident> getResidents();
    
    public abstract List<PlotObjectGroup> getAllPlotGroups();
    
    public abstract List<Resident> getResidents(final String[] p0);
    
    public abstract Resident getResident(final String p0) throws NotRegisteredException;
    
    public abstract void removeResidentList(final Resident p0);
    
    public abstract void removeNation(final Nation p0);
    
    public abstract boolean hasResident(final String p0);
    
    public abstract boolean hasTown(final String p0);
    
    public abstract boolean hasNation(final String p0);
    
    public abstract List<Town> getTowns(final String[] p0);
    
    public abstract List<Town> getTowns();
    
    public abstract Town getTown(final String p0) throws NotRegisteredException;
    
    public abstract Town getTown(final UUID p0) throws NotRegisteredException;
    
    public abstract SiegeZone getSiegeZone(final String p0) throws NotRegisteredException;
    
    public abstract List<Nation> getNations(final String[] p0);
    
    public abstract List<Nation> getNations();
    
    public abstract List<SiegeZone> getSiegeZones();
    
    public abstract Nation getNation(final String p0) throws NotRegisteredException;
    
    public abstract Nation getNation(final UUID p0) throws NotRegisteredException;
    
    public abstract TownyWorld getWorld(final String p0) throws NotRegisteredException;
    
    public abstract List<TownyWorld> getWorlds();
    
    public abstract TownyWorld getTownWorld(final String p0);
    
    public abstract void removeResident(final Resident p0);
    
    public abstract void removeTownBlock(final TownBlock p0);
    
    public abstract void removeTownBlocks(final Town p0);
    
    public abstract List<TownBlock> getAllTownBlocks();
    
    public abstract void newResident(final String p0) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void newTown(final String p0) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void newNation(final String p0) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void newSiegeZone(final String p0, final String p1) throws AlreadyRegisteredException;
    
    public abstract void newWorld(final String p0) throws AlreadyRegisteredException;
    
    public abstract void removeTown(final Town p0);
    
    public abstract void removeTown(final Town p0, final boolean p1);
    
    public abstract void removeRuinedTown(final Town p0);
    
    public abstract void removeSiege(final Siege p0);
    
    public abstract void removeSiegeZone(final SiegeZone p0);
    
    public abstract void removeWorld(final TownyWorld p0) throws UnsupportedOperationException;
    
    public abstract Set<String> getResidentKeys();
    
    public abstract Set<String> getTownsKeys();
    
    public abstract Set<String> getNationsKeys();
    
    public abstract Set<String> getSiegeZonesKeys();
    
    public abstract List<Town> getTownsWithoutNation();
    
    public abstract List<Resident> getResidentsWithoutTown();
    
    public abstract void renameTown(final Town p0, final String p1) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void renameNation(final Nation p0, final String p1) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void mergeNation(final Nation p0, final Nation p1) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void renamePlayer(final Resident p0, final String p1) throws AlreadyRegisteredException, NotRegisteredException;
    
    public abstract void renameGroup(final PlotObjectGroup p0, final String p1) throws AlreadyRegisteredException;
}
