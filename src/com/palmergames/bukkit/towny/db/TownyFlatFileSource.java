// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.db;

import java.io.EOFException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.util.StringMgmt;
import javax.naming.InvalidNameException;
import com.palmergames.bukkit.util.NameValidation;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.HashMap;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import org.bukkit.World;
import java.io.FileNotFoundException;
import java.util.UUID;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.Set;
import java.io.IOException;
import java.text.SimpleDateFormat;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.util.FileMgmt;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.scheduler.BukkitTask;
import java.util.Queue;

public final class TownyFlatFileSource extends TownyDatabaseHandler
{
	private final Queue<FlatFile_Task> queryQueue = new ConcurrentLinkedQueue<>();
	private final BukkitTask task;

	private final String newLine = System.getProperty("line.separator");
    
	public TownyFlatFileSource(Towny plugin, TownyUniverse universe) {
		super(plugin, universe);
		// Create files and folders if non-existent
		if (!FileMgmt.checkOrCreateFolders(
			rootFolderPath,
			dataFolderPath,
			dataFolderPath + File.separator + "residents",
			dataFolderPath + File.separator + "towns",
			dataFolderPath + File.separator + "towns" + File.separator + "deleted",
			dataFolderPath + File.separator + "nations",
			dataFolderPath + File.separator + "nations" + File.separator + "deleted",
			dataFolderPath + File.separator + "worlds",
			dataFolderPath + File.separator + "worlds" + File.separator + "deleted",
			dataFolderPath + File.separator + "plot-block-data",
			dataFolderPath + File.separator + "townblocks",
			dataFolderPath + File.separator + "plotgroups"
		) || !FileMgmt.checkOrCreateFiles(
			dataFolderPath + File.separator + "townblocks.txt",
			dataFolderPath + File.separator + "residents.txt",
			dataFolderPath + File.separator + "towns.txt",
			dataFolderPath + File.separator + "nations.txt",
			dataFolderPath + File.separator + "worlds.txt",
			dataFolderPath + File.separator + "regen.txt",
			dataFolderPath + File.separator + "snapshot_queue.txt",
			dataFolderPath + File.separator + "plotgroups.txt"
		)) {
			TownyMessaging.sendErrorMsg("Could not create flatfile default files and folders.");
		}
		/*
		 * Start our Async queue for pushing data to the database.
		 */
		task = BukkitTools.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			
			while (!TownyFlatFileSource.this.queryQueue.isEmpty()) {
				
				FlatFile_Task query = TownyFlatFileSource.this.queryQueue.poll();
				
				try {
					
					FileMgmt.listToFile(query.list, query.path);
					
				} catch (NullPointerException ex) {
					
					if (query != null)
						TownyMessaging.sendErrorMsg("Null Error saving to file - " + query.path);
					
				}
				
			}
			
		}, 5L, 5L);
	}
    
    @Override
    public void cancelTask() {
        this.task.cancel();
    }
    
    @Override
    public synchronized boolean backup() throws IOException {
        final String backupType = TownySettings.getFlatFileBackupType();
        final long t = System.currentTimeMillis();
        final String newBackupFolder = this.backupFolderPath + File.separator + new SimpleDateFormat("yyyy-MM-dd HH-mm").format(t) + " - " + t;
        FileMgmt.checkOrCreateFolders(this.rootFolderPath, this.rootFolderPath + File.separator + "backup");
        final String lowerCase = backupType.toLowerCase();
        switch (lowerCase) {
            case "folder": {
                FileMgmt.checkOrCreateFolder(newBackupFolder);
                FileMgmt.copyDirectory(new File(this.dataFolderPath), new File(newBackupFolder));
                FileMgmt.copyDirectory(new File(this.logFolderPath), new File(newBackupFolder));
                FileMgmt.copyDirectory(new File(this.settingsFolderPath), new File(newBackupFolder));
                return true;
            }
            case "zip": {
                FileMgmt.zipDirectories(new File(newBackupFolder + ".zip"), new File(this.dataFolderPath), new File(this.logFolderPath), new File(this.settingsFolderPath));
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public void cleanupBackups() {
        final long deleteAfter = TownySettings.getBackupLifeLength();
        if (deleteAfter >= 0L) {
            FileMgmt.deleteOldBackups(new File(this.universe.getRootFolder() + File.separator + "backup"), deleteAfter);
        }
    }
    
    @Override
    public synchronized void deleteUnusedResidents() {
        String path = this.dataFolderPath + File.separator + "residents";
        Set<String> names = this.getResidentKeys();
        FileMgmt.deleteUnusedFiles(new File(path), names);
        path = this.dataFolderPath + File.separator + "towns";
        names = this.getTownsKeys();
        FileMgmt.deleteUnusedFiles(new File(path), names);
        path = this.dataFolderPath + File.separator + "nations";
        names = this.getNationsKeys();
        FileMgmt.deleteUnusedFiles(new File(path), names);
        path = this.dataFolderPath + File.separator + "siegezones";
        names = this.getSiegeZonesKeys();
        FileMgmt.deleteUnusedFiles(new File(path), names);
    }
    
    public String getResidentFilename(final Resident resident) {
        return this.dataFolderPath + File.separator + "residents" + File.separator + resident.getName() + ".txt";
    }
    
    public String getTownFilename(final Town town) {
        return this.dataFolderPath + File.separator + "towns" + File.separator + town.getName() + ".txt";
    }
    
    public String getNationFilename(final Nation nation) {
        return this.dataFolderPath + File.separator + "nations" + File.separator + nation.getName() + ".txt";
    }
    
    public String getSiegeZoneFilename(final SiegeZone siegeZone) {
        return this.dataFolderPath + File.separator + "siegezones" + File.separator + siegeZone.getName() + ".txt";
    }
    
    public String getWorldFilename(final TownyWorld world) {
        return this.dataFolderPath + File.separator + "worlds" + File.separator + world.getName() + ".txt";
    }
    
    public String getPlotFilename(final PlotBlockData plotChunk) {
        return this.dataFolderPath + File.separator + "plot-block-data" + File.separator + plotChunk.getWorldName() + File.separator + plotChunk.getX() + "_" + plotChunk.getZ() + "_" + plotChunk.getSize() + ".data";
    }
    
    public String getPlotFilename(final TownBlock townBlock) {
        return this.dataFolderPath + File.separator + "plot-block-data" + File.separator + townBlock.getWorld().getName() + File.separator + townBlock.getX() + "_" + townBlock.getZ() + "_" + TownySettings.getTownBlockSize() + ".data";
    }
    
    public String getTownBlockFilename(final TownBlock townBlock) {
        return this.dataFolderPath + File.separator + "townblocks" + File.separator + townBlock.getWorld().getName() + File.separator + townBlock.getX() + "_" + townBlock.getZ() + "_" + TownySettings.getTownBlockSize() + ".data";
    }
    
    public String getPlotGroupFilename(final PlotObjectGroup group) {
        return this.dataFolderPath + File.separator + "plotgroups" + File.separator + group.getID() + ".data";
    }
    
    @Override
    public boolean loadTownBlockList() {
        TownyMessaging.sendDebugMsg("Loading TownBlock List");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "townblocks.txt"), StandardCharsets.UTF_8));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        final String[] tokens = line.split(",");
                        if (tokens.length < 3) {
                            continue;
                        }
                        TownyWorld world;
                        try {
                            world = this.getWorld(tokens[0]);
                        }
                        catch (NotRegisteredException ex) {
                            this.newWorld(tokens[0]);
                            world = this.getWorld(tokens[0]);
                        }
                        final int x = Integer.parseInt(tokens[1]);
                        final int z = Integer.parseInt(tokens[2]);
                        try {
                            world.newTownBlock(x, z);
                        }
                        catch (AlreadyRegisteredException ex2) {}
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading Townblock List at " + line + ", in towny\\data\\townblocks.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadPlotGroupList() {
        TownyMessaging.sendDebugMsg("Loading Group List");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "plotgroups.txt"), StandardCharsets.UTF_8));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        final String[] tokens = line.split(",");
                        String townName = null;
                        UUID groupID;
                        String groupName;
                        if (tokens.length == 4) {
                            townName = tokens[1];
                            groupID = UUID.fromString(tokens[2]);
                            groupName = tokens[3];
                        }
                        else {
                            townName = tokens[0];
                            groupID = UUID.fromString(tokens[1]);
                            groupName = tokens[2];
                        }
                        Town town = null;
                        try {
                            town = this.getTown(townName);
                        }
                        catch (NotRegisteredException e2) {
                            continue;
                        }
                        if (town == null) {
                            continue;
                        }
                        this.universe.newGroup(town, groupName, groupID);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading Group List at " + line + ", in towny\\data\\groups.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadResidentList() {
        TownyMessaging.sendDebugMsg("Loading Resident List");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "residents.txt"), StandardCharsets.UTF_8));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        this.newResident(line);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (AlreadyRegisteredException e) {
            TownyMessaging.sendErrorMsg("Error Loading Resident List at " + line + ", resident is possibly listed twice.");
            e.printStackTrace();
            return false;
        }
        catch (Exception e2) {
            TownyMessaging.sendErrorMsg("Error Loading Resident List at " + line + ", in towny\\data\\residents.txt");
            e2.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadTownList() {
        TownyMessaging.sendDebugMsg("Loading Town List");
        String line = null;
        BufferedReader fin;
        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "towns.txt"), StandardCharsets.UTF_8));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            while ((line = fin.readLine()) != null) {
                if (!line.equals("")) {
                    this.newTown(line);
                }
            }
            return true;
        }
        catch (AlreadyRegisteredException e2) {
            e2.printStackTrace();
            return false;
        }
        catch (Exception e3) {
            TownyMessaging.sendErrorMsg("Error Loading Town List at " + line + ", in towny\\data\\towns.txt");
            e3.printStackTrace();
            return false;
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                }
                catch (IOException ex) {}
            }
        }
    }
    
    @Override
    public boolean loadNationList() {
        TownyMessaging.sendDebugMsg("Loading Nation List");
        String line = null;
        BufferedReader fin;
        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "nations.txt"), StandardCharsets.UTF_8));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            while ((line = fin.readLine()) != null) {
                if (!line.equals("")) {
                    this.newNation(line);
                }
            }
            return true;
        }
        catch (AlreadyRegisteredException e2) {
            e2.printStackTrace();
            return false;
        }
        catch (Exception e3) {
            TownyMessaging.sendErrorMsg("Error Loading Nation List at " + line + ", in towny\\data\\nations.txt");
            e3.printStackTrace();
            return false;
        }
        finally {
            try {
                fin.close();
            }
            catch (IOException ex) {}
        }
    }
    
    @Override
    public boolean loadSiegeZoneList() {
        TownyMessaging.sendDebugMsg("Loading Siege Zone List");
        String siegeZoneName = null;
        BufferedReader fin;
        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "siegezones.txt"), StandardCharsets.UTF_8));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            while ((siegeZoneName = fin.readLine()) != null) {
                if (!siegeZoneName.equals("")) {
                    this.newSiegeZone(siegeZoneName);
                }
            }
            return true;
        }
        catch (AlreadyRegisteredException e2) {
            e2.printStackTrace();
            return false;
        }
        catch (Exception e3) {
            TownyMessaging.sendErrorMsg("Error Loading Siege zone list at " + siegeZoneName + ", in towny\\data\\sieges.txt");
            e3.printStackTrace();
            return false;
        }
        finally {
            try {
                fin.close();
            }
            catch (IOException ex) {}
        }
    }
    
    @Override
    public boolean loadWorldList() {
        if (this.plugin != null) {
            TownyMessaging.sendDebugMsg("Loading Server World List");
            for (final World world : this.plugin.getServer().getWorlds()) {
                try {
                    this.newWorld(world.getName());
                }
                catch (AlreadyRegisteredException ex) {}
            }
        }
        TownyMessaging.sendDebugMsg("Loading World List");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "worlds.txt"), StandardCharsets.UTF_8));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        this.newWorld(line);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (AlreadyRegisteredException e2) {
            return true;
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading World List at " + line + ", in towny\\data\\worlds.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadRegenList() {
        TownyMessaging.sendDebugMsg("Loading Regen List");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "regen.txt"), StandardCharsets.UTF_8));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        final String[] split = line.split(",");
                        final PlotBlockData plotData = this.loadPlotData(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        if (plotData == null) {
                            continue;
                        }
                        TownyRegenAPI.addPlotChunk(plotData, false);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading Regen List at " + line + ", in towny\\data\\regen.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadSnapshotList() {
        TownyMessaging.sendDebugMsg("Loading Snapshot Queue");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(this.dataFolderPath + File.separator + "snapshot_queue.txt"), StandardCharsets.UTF_8));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        final String[] split = line.split(",");
                        final WorldCoord worldCoord = new WorldCoord(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        TownyRegenAPI.addWorldCoord(worldCoord);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading Snapshot Queue List at " + line + ", in towny\\data\\snapshot_queue.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadResident(final Resident resident) {
        String line = null;
        final String path = this.getResidentFilename(resident);
        final File fileResident = new File(path);
        if (fileResident.exists() && fileResident.isFile()) {
            TownyMessaging.sendDebugMsg("Loading Resident: " + resident.getName());
            try {
                final HashMap<String, String> keys = new HashMap<String, String>();
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(fileResident), StandardCharsets.UTF_8));
                for (final String key : properties.stringPropertyNames()) {
                    final String value = properties.getProperty(key);
                    keys.put(key, String.valueOf(value));
                }
                resident.setLastOnline(Long.parseLong(keys.get("lastOnline")));
                line = keys.get("registered");
                if (line != null) {
                    resident.setRegistered(Long.parseLong(line));
                }
                else {
                    resident.setRegistered(resident.getLastOnline());
                }
                line = keys.get("isNPC");
                if (line != null) {
                    resident.setNPC(Boolean.parseBoolean(line));
                }
                line = keys.get("isJailed");
                if (line != null) {
                    resident.setJailed(Boolean.parseBoolean(line));
                }
                line = keys.get("JailSpawn");
                if (line != null) {
                    resident.setJailSpawn(Integer.valueOf(line));
                }
                line = keys.get("JailDays");
                if (line != null) {
                    resident.setJailDays(Integer.valueOf(line));
                }
                line = keys.get("JailTown");
                if (line != null) {
                    resident.setJailTown(line);
                }
                line = keys.get("title");
                if (line != null) {
                    resident.setTitle(line);
                }
                line = keys.get("surname");
                if (line != null) {
                    resident.setSurname(line);
                }
                line = keys.get("town");
                if (line != null) {
                    resident.setTown(this.getTown(line));
                }
                line = keys.get("town-ranks");
                if (line != null) {
                    resident.setTownRanks(new ArrayList<String>(Arrays.asList(line.split(","))));
                }
                line = keys.get("nation-ranks");
                if (line != null) {
                    resident.setNationRanks(new ArrayList<String>(Arrays.asList(line.split(","))));
                }
                line = keys.get("friends");
                if (line != null) {
                    final String[] split;
                    split = line.split(",");
                    for (final String token : split) {
                        if (!token.isEmpty()) {
                            final Resident friend = this.getResident(token);
                            if (friend != null) {
                                resident.addFriend(friend);
                            }
                        }
                    }
                }
                line = keys.get("protectionStatus");
                if (line != null) {
                    resident.setPermissions(line);
                }
                line = keys.get("townBlocks");
                if (line != null) {
                    this.utilLoadTownBlocks(line, null, resident);
                }
                line = keys.get("metadata");
                if (line != null && !line.isEmpty()) {
                    resident.setMetadata(line.trim());
                }
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading resident file " + resident.getName() + " at line: " + line + ", in towny\\data\\residents\\" + resident.getName() + ".txt");
                return false;
            }
            finally {
                this.saveResident(resident);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean loadTown(final Town town) {
        String line = null;
        final String path = this.getTownFilename(town);
        final File fileTown = new File(path);
        if (fileTown.exists() && fileTown.isFile()) {
            TownyMessaging.sendDebugMsg("Loading Town: " + town.getName());
            try {
                final HashMap<String, String> keys = new HashMap<String, String>();
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(fileTown), StandardCharsets.UTF_8));
                for (final String key : properties.stringPropertyNames()) {
                    final String value = properties.getProperty(key);
                    keys.put(key, String.valueOf(value));
                }
                line = keys.get("residents");
                if (line != null) {
                    final String[] split;
                    split = line.split(",");
                    for (final String token : split) {
                        if (!token.isEmpty()) {
                            TownyMessaging.sendDebugMsg("Town (" + town.getName() + ") Fetching Resident: " + token);
                            try {
                                final Resident resident = this.getResident(token);
                                if (resident != null) {
                                    try {
                                        town.addResident(resident);
                                    }
                                    catch (AlreadyRegisteredException e) {
                                        TownyMessaging.sendErrorMsg("Loading Error: " + resident.getName() + " is already a member of a town (" + resident.getTown().getName() + ").");
                                    }
                                }
                            }
                            catch (NotRegisteredException e2) {
                                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading a resident in the town file of " + town.getName() + ".txt. The resident " + token + " does not exist, removing them from town... (Will require manual editing of the town file if they are the mayor)");
                            }
                        }
                    }
                }
                line = keys.get("outlaws");
                if (line != null) {
                    final String[] split2;
                    split2 = line.split(",");
                    for (final String token : split2) {
                        if (!token.isEmpty()) {
                            TownyMessaging.sendDebugMsg("Town Fetching Outlaw: " + token);
                            try {
                                final Resident outlaw = this.getResident(token);
                                if (outlaw != null) {
                                    town.addOutlaw(outlaw);
                                }
                            }
                            catch (NotRegisteredException e2) {
                                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading an outlaw of town file " + town.getName() + ".txt. The outlaw " + token + " does not exist, removing from list...");
                            }
                        }
                    }
                }
                line = keys.get("mayor");
                if (line != null) {
                    town.setMayor(this.getResident(line));
                }
                town.setTownBoard(keys.get("townBoard"));
                line = keys.get("tag");
                if (line != null) {
                    try {
                        town.setTag(line);
                    }
                    catch (TownyException e3) {
                        town.setTag("");
                    }
                }
                line = keys.get("protectionStatus");
                if (line != null) {
                    town.setPermissions(line);
                }
                line = keys.get("bonusBlocks");
                if (line != null) {
                    try {
                        town.setBonusBlocks(Integer.parseInt(line));
                    }
                    catch (Exception e4) {
                        town.setBonusBlocks(0);
                    }
                }
                line = keys.get("purchasedBlocks");
                if (line != null) {
                    try {
                        town.setPurchasedBlocks(Integer.parseInt(line));
                    }
                    catch (Exception e4) {
                        town.setPurchasedBlocks(0);
                    }
                }
                line = keys.get("plotPrice");
                if (line != null) {
                    try {
                        town.setPlotPrice(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setPlotPrice(0.0);
                    }
                }
                line = keys.get("hasUpkeep");
                if (line != null) {
                    try {
                        town.setHasUpkeep(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex) {}
                }
                line = keys.get("taxpercent");
                if (line != null) {
                    try {
                        town.setTaxPercentage(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex2) {}
                }
                line = keys.get("taxes");
                if (line != null) {
                    try {
                        town.setTaxes(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setTaxes(0.0);
                    }
                }
                line = keys.get("plotTax");
                if (line != null) {
                    try {
                        town.setPlotTax(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setPlotTax(0.0);
                    }
                }
                line = keys.get("commercialPlotPrice");
                if (line != null) {
                    try {
                        town.setCommercialPlotPrice(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setCommercialPlotPrice(0.0);
                    }
                }
                line = keys.get("commercialPlotTax");
                if (line != null) {
                    try {
                        town.setCommercialPlotTax(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setCommercialPlotTax(0.0);
                    }
                }
                line = keys.get("embassyPlotPrice");
                if (line != null) {
                    try {
                        town.setEmbassyPlotPrice(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setEmbassyPlotPrice(0.0);
                    }
                }
                line = keys.get("embassyPlotTax");
                if (line != null) {
                    try {
                        town.setEmbassyPlotTax(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setEmbassyPlotTax(0.0);
                    }
                }
                line = keys.get("spawnCost");
                if (line != null) {
                    try {
                        town.setSpawnCost(Double.parseDouble(line));
                    }
                    catch (Exception e4) {
                        town.setSpawnCost(TownySettings.getSpawnTravelCost());
                    }
                }
                line = keys.get("adminDisabledPvP");
                if (line != null) {
                    try {
                        town.setAdminDisabledPVP(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex3) {}
                }
                line = keys.get("adminEnabledPvP");
                if (line != null) {
                    try {
                        town.setAdminEnabledPVP(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex4) {}
                }
                line = keys.get("open");
                if (line != null) {
                    try {
                        town.setOpen(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex5) {}
                }
                line = keys.get("public");
                if (line != null) {
                    try {
                        town.setPublic(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex6) {}
                }
                line = keys.get("conquered");
                if (line != null) {
                    try {
                        town.setConquered(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex7) {}
                }
                line = keys.get("conqueredDays");
                if (line != null) {
                    town.setConqueredDays(Integer.valueOf(line));
                }
                line = keys.get("townBlocks");
                if (line != null) {
                    this.utilLoadTownBlocks(line, town, null);
                }
                line = keys.get("homeBlock");
                if (line != null) {
                    final String[] tokens = line.split(",");
                    if (tokens.length == 3) {
                        try {
                            final TownyWorld world = this.getWorld(tokens[0]);
                            try {
                                final int x = Integer.parseInt(tokens[1]);
                                final int z = Integer.parseInt(tokens[2]);
                                final TownBlock homeBlock = world.getTownBlock(x, z);
                                town.forceSetHomeBlock(homeBlock);
                            }
                            catch (NumberFormatException e5) {
                                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " homeBlock tried to load invalid location.");
                            }
                            catch (NotRegisteredException e6) {
                                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " homeBlock tried to load invalid TownBlock.");
                            }
                            catch (TownyException e7) {
                                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " does not have a home block.");
                            }
                        }
                        catch (NotRegisteredException e8) {
                            TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " homeBlock tried to load invalid world.");
                        }
                    }
                }
                line = keys.get("spawn");
                if (line != null) {
                    final String[] tokens = line.split(",");
                    if (tokens.length >= 4) {
                        try {
                            final World world2 = this.plugin.getServerWorld(tokens[0]);
                            final double x2 = Double.parseDouble(tokens[1]);
                            final double y = Double.parseDouble(tokens[2]);
                            final double z2 = Double.parseDouble(tokens[3]);
                            final Location loc = new Location(world2, x2, y, z2);
                            if (tokens.length == 6) {
                                loc.setPitch(Float.parseFloat(tokens[4]));
                                loc.setYaw(Float.parseFloat(tokens[5]));
                            }
                            town.forceSetSpawn(loc);
                        }
                        catch (NumberFormatException ex8) {}
                        catch (NullPointerException ex9) {}
                        catch (NotRegisteredException ex10) {}
                    }
                }
                line = keys.get("outpostspawns");
                if (line != null) {
                    final String[] split3;
                    split3 = line.split(";");
                    for (final String spawn : split3) {
                        final String[] tokens = spawn.split(",");
                        if (tokens.length >= 4) {
                            try {
                                final World world3 = this.plugin.getServerWorld(tokens[0]);
                                final double x3 = Double.parseDouble(tokens[1]);
                                final double y2 = Double.parseDouble(tokens[2]);
                                final double z3 = Double.parseDouble(tokens[3]);
                                final Location loc2 = new Location(world3, x3, y2, z3);
                                if (tokens.length == 6) {
                                    loc2.setPitch(Float.parseFloat(tokens[4]));
                                    loc2.setYaw(Float.parseFloat(tokens[5]));
                                }
                                town.forceAddOutpostSpawn(loc2);
                            }
                            catch (NumberFormatException ex11) {}
                            catch (NullPointerException ex12) {}
                            catch (NotRegisteredException ex13) {}
                        }
                    }
                }
                line = keys.get("jailspawns");
                if (line != null) {
                    final String[] split4;
                    split4 = line.split(";");
                    for (final String spawn : split4) {
                        final String[] tokens = spawn.split(",");
                        if (tokens.length >= 4) {
                            try {
                                final World world3 = this.plugin.getServerWorld(tokens[0]);
                                final double x3 = Double.parseDouble(tokens[1]);
                                final double y2 = Double.parseDouble(tokens[2]);
                                final double z3 = Double.parseDouble(tokens[3]);
                                final Location loc2 = new Location(world3, x3, y2, z3);
                                if (tokens.length == 6) {
                                    loc2.setPitch(Float.parseFloat(tokens[4]));
                                    loc2.setYaw(Float.parseFloat(tokens[5]));
                                }
                                town.forceAddJailSpawn(loc2);
                            }
                            catch (NumberFormatException ex14) {}
                            catch (NullPointerException ex15) {}
                            catch (NotRegisteredException ex16) {}
                        }
                    }
                }
                line = keys.get("uuid");
                if (line != null) {
                    try {
                        town.setUuid(UUID.fromString(line));
                    }
                    catch (IllegalArgumentException ee) {
                        town.setUuid(UUID.randomUUID());
                    }
                }
                line = keys.get("registered");
                if (line != null) {
                    try {
                        town.setRegistered(Long.valueOf(line));
                    }
                    catch (Exception ee2) {
                        town.setRegistered(0L);
                    }
                }
                line = keys.get("metadata");
                if (line != null && !line.isEmpty()) {
                    town.setMetadata(line.trim());
                }
                line = keys.get("recentlyRuinedEndTime");
                if (line != null) {
                    try {
                        town.setRecentlyRuinedEndTime(Long.parseLong(line));
                    }
                    catch (Exception e4) {
                        town.setRecentlyRuinedEndTime(0L);
                    }
                }
                else {
                    town.setRecentlyRuinedEndTime(0L);
                }
                line = keys.get("revoltCooldownEndTime");
                if (line != null) {
                    try {
                        town.setRevoltImmunityEndTime(Long.parseLong(line));
                    }
                    catch (Exception e4) {
                        town.setRevoltImmunityEndTime(0L);
                    }
                }
                else {
                    town.setRevoltImmunityEndTime(0L);
                }
                line = keys.get("siegeCooldownEndTime");
                if (line != null) {
                    try {
                        town.setSiegeImmunityEndTime(Long.parseLong(line));
                    }
                    catch (Exception e4) {
                        town.setSiegeImmunityEndTime(0L);
                    }
                }
                else {
                    town.setSiegeImmunityEndTime(0L);
                }
                line = keys.get("siegeStatus");
                if (line != null) {
                    try {
                        final Siege siege = new Siege(town);
                        town.setSiege(siege);
                        siege.setStatus(SiegeStatus.parseString(line));
                    }
                    catch (Exception e4) {
                        town.setSiege(null);
                    }
                }
                else {
                    town.setSiege(null);
                }
                if (town.hasSiege()) {
                    final Siege siege = town.getSiege();
                    try {
                        line = keys.get("siegeTownPlundered");
                        siege.setTownPlundered(Boolean.parseBoolean(line));
                    }
                    catch (Exception e9) {
                        siege.setTownPlundered(false);
                    }
                    try {
                        line = keys.get("siegeTownInvaded");
                        siege.setTownInvaded(Boolean.parseBoolean(line));
                    }
                    catch (Exception e9) {
                        siege.setTownInvaded(false);
                    }
                    try {
                        line = keys.get("siegeAttackerWinner");
                        if (line != null) {
                            siege.setAttackerWinner(this.getNation(line));
                        }
                        else {
                            siege.setAttackerWinner(null);
                        }
                    }
                    catch (Exception e9) {
                        siege.setAttackerWinner(null);
                    }
                    try {
                        line = keys.get("siegeActualStartTime");
                        siege.setStartTime(Long.parseLong(line));
                    }
                    catch (Exception e9) {
                        siege.setStartTime(0L);
                    }
                    try {
                        line = keys.get("siegeScheduledEndTime");
                        siege.setScheduledEndTime(Long.parseLong(line));
                    }
                    catch (Exception e9) {
                        siege.setScheduledEndTime(0L);
                    }
                    try {
                        line = keys.get("siegeActualEndTime");
                        siege.setActualEndTime(Long.parseLong(line));
                    }
                    catch (Exception e9) {
                        siege.setActualEndTime(0L);
                    }
                    try {
                        line = keys.get("siegeNextUpkeepTime");
                        siege.setActualEndTime(Long.parseLong(line));
                    }
                    catch (Exception e9) {
                        siege.setActualEndTime(0L);
                    }
                    try {
                        line = keys.get("siegeZones");
                        final String[] split5;
                        split5 = line.split(",");
                        for (final String siegeZoneName : split5) {
                            final SiegeZone siegeZone = this.universe.getDataSource().getSiegeZone(siegeZoneName);
                            town.getSiege().getSiegeZones().put(siegeZone.getAttackingNation(), siegeZone);
                        }
                    }
                    catch (Exception ex17) {}
                }
                line = keys.get("occupied");
                if (line != null) {
                    try {
                        town.setOccupied(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex18) {}
                }
                line = keys.get("neutral");
                if (line != null) {
                    try {
                        town.setNeutral(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex19) {}
                }
                line = keys.get("desiredNeutralityValue");
                if (line != null) {
                    try {
                        town.setDesiredNeutralityValue(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex20) {}
                }
                line = keys.get("neutralityChangeConfirmationCounterDays");
                if (line != null) {
                    try {
                        town.setNeutralityChangeConfirmationCounterDays(Integer.parseInt(line));
                    }
                    catch (Exception ex21) {}
                }
            }
            catch (Exception e10) {
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading town file " + town.getName() + " at line: " + line + ", in towny\\data\\towns\\" + town.getName() + ".txt");
                return false;
            }
            finally {
                this.saveTown(town);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean loadNation(final Nation nation) {
        String line = "";
        final String path = this.getNationFilename(nation);
        final File fileNation = new File(path);
        if (fileNation.exists() && fileNation.isFile()) {
            TownyMessaging.sendDebugMsg("Loading Nation: " + nation.getName());
            try {
                final HashMap<String, String> keys = new HashMap<String, String>();
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(fileNation), StandardCharsets.UTF_8));
                for (final String key : properties.stringPropertyNames()) {
                    final String value = properties.getProperty(key);
                    keys.put(key, String.valueOf(value));
                }
                line = keys.get("towns");
                if (line != null) {
                    final String[] split;
                    split = line.split(",");
                    for (final String token : split) {
                        if (!token.isEmpty()) {
                            try {
                                TownyMessaging.sendDebugMsg("Nation Fetching Town: " + token);
                                final Town town = this.getTown(token);
                                if (town != null) {
                                    nation.addTown(town);
                                }
                            }
                            catch (NotRegisteredException e2) {
                                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading a town in the nation file of " + nation.getName() + ".txt. The town " + token + " does not exist, removing it from nation... (Will require editing of the nation file if it is the capital)");
                            }
                        }
                    }
                }
                line = keys.get("capital");
                if (line != null) {
                    nation.setCapital(this.getTown(line));
                }
                line = keys.get("nationBoard");
                if (line != null) {
                    try {
                        nation.setNationBoard(line);
                    }
                    catch (Exception e3) {
                        nation.setNationBoard("");
                    }
                }
                line = keys.get("tag");
                if (line != null) {
                    try {
                        nation.setTag(line);
                    }
                    catch (TownyException e4) {
                        nation.setTag("");
                    }
                }
                line = keys.get("allies");
                if (line != null) {
                    final String[] split2;
                    split2 = line.split(",");
                    for (final String token : split2) {
                        if (!token.isEmpty()) {
                            final Nation friend = this.getNation(token);
                            if (friend != null) {
                                nation.addAlly(friend);
                            }
                        }
                    }
                }
                line = keys.get("enemies");
                if (line != null) {
                    final String[] split3;
                    split3 = line.split(",");
                    for (final String token : split3) {
                        if (!token.isEmpty()) {
                            final Nation enemy = this.getNation(token);
                            if (enemy != null) {
                                nation.addEnemy(enemy);
                            }
                        }
                    }
                }
                line = keys.get("siegeZones");
                if (line != null) {
                    final String[] split4;
                    split4 = line.split(",");
                    for (final String token : split4) {
                        if (!token.isEmpty()) {
                            final SiegeZone siegeZone = this.getSiegeZone(token);
                            if (siegeZone != null) {
                                nation.addSiegeZone(siegeZone);
                            }
                        }
                    }
                }
                line = keys.get("taxes");
                if (line != null) {
                    try {
                        nation.setTaxes(Double.parseDouble(line));
                    }
                    catch (Exception e3) {
                        nation.setTaxes(0.0);
                    }
                }
                line = keys.get("spawnCost");
                if (line != null) {
                    try {
                        nation.setSpawnCost(Double.parseDouble(line));
                    }
                    catch (Exception e3) {
                        nation.setSpawnCost(TownySettings.getSpawnTravelCost());
                    }
                }
                line = keys.get("neutral");
                if (line != null) {
                    try {
                        nation.setNeutral(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex) {}
                }
                line = keys.get("uuid");
                if (line != null) {
                    try {
                        nation.setUuid(UUID.fromString(line));
                    }
                    catch (IllegalArgumentException ee) {
                        nation.setUuid(UUID.randomUUID());
                    }
                }
                line = keys.get("registered");
                if (line != null) {
                    try {
                        nation.setRegistered(Long.valueOf(line));
                    }
                    catch (Exception ee2) {
                        nation.setRegistered(0L);
                    }
                }
                line = keys.get("nationSpawn");
                if (line != null) {
                    final String[] tokens = line.split(",");
                    if (tokens.length >= 4) {
                        try {
                            final World world = this.plugin.getServerWorld(tokens[0]);
                            final double x = Double.parseDouble(tokens[1]);
                            final double y = Double.parseDouble(tokens[2]);
                            final double z = Double.parseDouble(tokens[3]);
                            final Location loc = new Location(world, x, y, z);
                            if (tokens.length == 6) {
                                loc.setPitch(Float.parseFloat(tokens[4]));
                                loc.setYaw(Float.parseFloat(tokens[5]));
                            }
                            nation.forceSetNationSpawn(loc);
                        }
                        catch (NumberFormatException ex2) {}
                        catch (NullPointerException ex3) {}
                        catch (NotRegisteredException ex4) {}
                    }
                }
                line = keys.get("isPublic");
                if (line != null) {
                    try {
                        nation.setPublic(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex5) {}
                }
                line = keys.get("isOpen");
                if (line != null) {
                    try {
                        nation.setOpen(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex6) {}
                }
                line = keys.get("metadata");
                if (line != null && !line.isEmpty()) {
                    nation.setMetadata(line.trim());
                }
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading nation file " + nation.getName() + " at line: " + line + ", in towny\\data\\nations\\" + nation.getName() + ".txt");
                e.printStackTrace();
                return false;
            }
            finally {
                this.saveNation(nation);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean loadSiegeZone(final SiegeZone siegeZone) {
        String line = "";
        final String path = this.getSiegeZoneFilename(siegeZone);
        final File fileSiegeZone = new File(path);
        if (fileSiegeZone.exists() && fileSiegeZone.isFile()) {
            try {
                final HashMap<String, String> keys = new HashMap<String, String>();
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(fileSiegeZone), StandardCharsets.UTF_8));
                for (final String key : properties.stringPropertyNames()) {
                    final String value = properties.getProperty(key);
                    keys.put(key, String.valueOf(value));
                }
                line = keys.get("flagLocation");
                final String[] locationValues = line.split(",");
                final World flagLocationWorld = BukkitTools.getWorld(locationValues[0]);
                final double flagLocationX = Double.parseDouble(locationValues[1]);
                final double flagLocationY = Double.parseDouble(locationValues[2]);
                final double flagLocationZ = Double.parseDouble(locationValues[3]);
                final Location flagLocation = new Location(flagLocationWorld, flagLocationX, flagLocationY, flagLocationZ);
                siegeZone.setFlagLocation(flagLocation);
                line = keys.get("attackingNation");
                siegeZone.setAttackingNation(this.getNation(line));
                line = keys.get("defendingTown");
                siegeZone.setDefendingTown(this.getTown(line));
                line = keys.get("siegePoints");
                siegeZone.setSiegePoints(Integer.parseInt(line));
                line = keys.get("warChestAmount");
                if (line != null && !line.isEmpty()) {
                    siegeZone.setWarChestAmount(Double.parseDouble(line));
                }
            }
            catch (Exception e) {
                final String filename = this.getSiegeZoneFilename(siegeZone);
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading siege zone file at line: " + line + ", in file: " + filename);
                return false;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean loadWorld(final TownyWorld world) {
        String line = "";
        final String path = this.getWorldFilename(world);
        if (!FileMgmt.checkOrCreateFile(path)) {
            TownyMessaging.sendErrorMsg("Loading Error: Exception while reading file " + path);
        }
        final File fileWorld = new File(path);
        if (fileWorld.exists() && fileWorld.isFile()) {
            TownyMessaging.sendDebugMsg("Loading World: " + world.getName());
            try {
                final HashMap<String, String> keys = new HashMap<String, String>();
                final Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(fileWorld), StandardCharsets.UTF_8));
                for (final String key : properties.stringPropertyNames()) {
                    final String value = properties.getProperty(key);
                    keys.put(key, String.valueOf(value));
                }
                line = keys.get("towns");
                if (line != null) {
                    final String[] split;
                    split = line.split(",");
                    for (final String token : split) {
                        if (!token.isEmpty()) {
                            TownyMessaging.sendDebugMsg("World Fetching Town: " + token);
                            final Town town = this.getTown(token);
                            if (town != null) {
                                town.setWorld(world);
                            }
                        }
                    }
                }
                line = keys.get("claimable");
                if (line != null) {
                    try {
                        world.setClaimable(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex) {}
                }
                line = keys.get("pvp");
                if (line != null) {
                    try {
                        world.setPVP(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex2) {}
                }
                line = keys.get("forcepvp");
                if (line != null) {
                    try {
                        world.setForcePVP(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex3) {}
                }
                line = keys.get("forcetownmobs");
                if (line != null) {
                    try {
                        world.setForceTownMobs(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex4) {}
                }
                line = keys.get("worldmobs");
                if (line != null) {
                    try {
                        world.setWorldMobs(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex5) {}
                }
                line = keys.get("firespread");
                if (line != null) {
                    try {
                        world.setFire(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex6) {}
                }
                line = keys.get("forcefirespread");
                if (line != null) {
                    try {
                        world.setForceFire(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex7) {}
                }
                line = keys.get("explosions");
                if (line != null) {
                    try {
                        world.setExpl(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex8) {}
                }
                line = keys.get("forceexplosions");
                if (line != null) {
                    try {
                        world.setForceExpl(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex9) {}
                }
                line = keys.get("endermanprotect");
                if (line != null) {
                    try {
                        world.setEndermanProtect(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex10) {}
                }
                line = keys.get("disableplayertrample");
                if (line != null) {
                    try {
                        world.setDisablePlayerTrample(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex11) {}
                }
                line = keys.get("disablecreaturetrample");
                if (line != null) {
                    try {
                        world.setDisableCreatureTrample(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex12) {}
                }
                line = keys.get("unclaimedZoneBuild");
                if (line != null) {
                    try {
                        world.setUnclaimedZoneBuild(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex13) {}
                }
                line = keys.get("unclaimedZoneDestroy");
                if (line != null) {
                    try {
                        world.setUnclaimedZoneDestroy(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex14) {}
                }
                line = keys.get("unclaimedZoneSwitch");
                if (line != null) {
                    try {
                        world.setUnclaimedZoneSwitch(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex15) {}
                }
                line = keys.get("unclaimedZoneItemUse");
                if (line != null) {
                    try {
                        world.setUnclaimedZoneItemUse(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex16) {}
                }
                line = keys.get("unclaimedZoneName");
                if (line != null) {
                    try {
                        world.setUnclaimedZoneName(line);
                    }
                    catch (Exception ex17) {}
                }
                line = keys.get("unclaimedZoneIgnoreIds");
                if (line != null) {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        for (final String s : line.split(",")) {
                            if (!s.isEmpty()) {
                                mats.add(s);
                            }
                        }
                        world.setUnclaimedZoneIgnore(mats);
                    }
                    catch (Exception ex18) {}
                }
                line = keys.get("usingPlotManagementDelete");
                if (line != null) {
                    try {
                        world.setUsingPlotManagementDelete(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex19) {}
                }
                line = keys.get("plotManagementDeleteIds");
                if (line != null) {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        for (final String s : line.split(",")) {
                            if (!s.isEmpty()) {
                                mats.add(s);
                            }
                        }
                        world.setPlotManagementDeleteIds(mats);
                    }
                    catch (Exception ex20) {}
                }
                line = keys.get("usingPlotManagementMayorDelete");
                if (line != null) {
                    try {
                        world.setUsingPlotManagementMayorDelete(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex21) {}
                }
                line = keys.get("plotManagementMayorDelete");
                if (line != null) {
                    try {
                        final List<String> materials = new ArrayList<String>();
                        for (final String s : line.split(",")) {
                            if (!s.isEmpty()) {
                                try {
                                    materials.add(s.toUpperCase().trim());
                                }
                                catch (NumberFormatException ex22) {}
                            }
                        }
                        world.setPlotManagementMayorDelete(materials);
                    }
                    catch (Exception ex23) {}
                }
                line = keys.get("usingPlotManagementRevert");
                if (line != null) {
                    try {
                        world.setUsingPlotManagementRevert(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex24) {}
                }
                line = keys.get("plotManagementIgnoreIds");
                if (line != null) {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        for (final String s : line.split(",")) {
                            if (!s.isEmpty()) {
                                mats.add(s);
                            }
                        }
                        world.setPlotManagementIgnoreIds(mats);
                    }
                    catch (Exception ex25) {}
                }
                line = keys.get("usingPlotManagementWildRegen");
                if (line != null) {
                    try {
                        world.setUsingPlotManagementWildRevert(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex26) {}
                }
                line = keys.get("PlotManagementWildRegenEntities");
                if (line != null) {
                    try {
                        final List<String> entities = new ArrayList<String>();
                        for (final String s : line.split(",")) {
                            if (!s.isEmpty()) {
                                try {
                                    entities.add(s.trim());
                                }
                                catch (NumberFormatException ex27) {}
                            }
                        }
                        world.setPlotManagementWildRevertEntities(entities);
                    }
                    catch (Exception ex28) {}
                }
                line = keys.get("usingPlotManagementWildRegenDelay");
                if (line != null) {
                    try {
                        world.setPlotManagementWildRevertDelay(Long.parseLong(line));
                    }
                    catch (Exception ex29) {}
                }
                line = keys.get("usingTowny");
                if (line != null) {
                    try {
                        world.setUsingTowny(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex30) {}
                }
                line = keys.get("warAllowed");
                if (line != null) {
                    try {
                        world.setWarAllowed(Boolean.parseBoolean(line));
                    }
                    catch (Exception ex31) {}
                }
                line = keys.get("metadata");
                if (line != null && !line.isEmpty()) {
                    world.setMetadata(line.trim());
                }
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading world file " + path + " at line: " + line + ", in towny\\data\\worlds\\" + world.getName() + ".txt");
                return false;
            }
            finally {
                this.saveWorld(world);
            }
            return true;
        }
        TownyMessaging.sendErrorMsg("Loading Error: File error while reading " + world.getName() + " at line: " + line + ", in towny\\data\\worlds\\" + world.getName() + ".txt");
        return false;
    }
    
    @Override
    public boolean loadPlotGroups() {
        String line = "";
        for (final PlotObjectGroup group : this.getAllPlotGroups()) {
            final String path = this.getPlotGroupFilename(group);
            final File groupFile = new File(path);
            if (groupFile.exists() && groupFile.isFile()) {
                String test = null;
                try {
                    final HashMap<String, String> keys = new HashMap<String, String>();
                    final Properties properties = new Properties();
                    properties.load(new InputStreamReader(new FileInputStream(groupFile), StandardCharsets.UTF_8));
                    for (final String key : properties.stringPropertyNames()) {
                        final String value = properties.getProperty(key);
                        keys.put(key, String.valueOf(value));
                    }
                    line = keys.get("groupName");
                    if (line != null) {
                        group.setGroupName(line.trim());
                    }
                    line = keys.get("groupID");
                    if (line != null) {
                        group.setID(UUID.fromString(line.trim()));
                    }
                    test = "town";
                    line = keys.get("town");
                    if (line != null && !line.isEmpty()) {
                        final Town town = this.getTown(line.trim());
                        group.setTown(town);
                    }
                    else {
                        TownyMessaging.sendErrorMsg("Could not add to town!");
                        this.deletePlotGroup(group);
                    }
                    line = keys.get("groupPrice");
                    if (line == null || line.isEmpty()) {
                        continue;
                    }
                    group.setPrice(Double.parseDouble(line.trim()));
                }
                catch (Exception e) {
                    if (!test.equals("town")) {
                        TownyMessaging.sendErrorMsg("Loading Error: Exception while reading Group file " + path + " at line: " + line);
                        return false;
                    }
                    TownyMessaging.sendDebugMsg("Group file missing Town, deleting " + path);
                    this.deletePlotGroup(group);
                    TownyMessaging.sendDebugMsg("Missing file: " + path + " deleting entry in group.txt");
                }
            }
            else {
                TownyMessaging.sendDebugMsg("Missing file: " + path + " deleting entry in groups.txt");
            }
        }
        this.savePlotGroupList();
        return true;
    }
    
    @Override
    public boolean loadTownBlocks() {
        String line = "";
        for (final TownBlock townBlock : this.getAllTownBlocks()) {
            final String path = this.getTownBlockFilename(townBlock);
            final File fileTownBlock = new File(path);
            if (fileTownBlock.exists() && fileTownBlock.isFile()) {
                String test = null;
                try {
                    final HashMap<String, String> keys = new HashMap<String, String>();
                    final Properties properties = new Properties();
                    properties.load(new InputStreamReader(new FileInputStream(fileTownBlock), StandardCharsets.UTF_8));
                    for (final String key : properties.stringPropertyNames()) {
                        final String value = properties.getProperty(key);
                        keys.put(key, String.valueOf(value));
                    }
                    test = "town";
                    line = keys.get("town");
                    if (line != null && line.isEmpty()) {
                        TownyMessaging.sendDebugMsg("TownBlock file missing Town, deleting " + path);
                        TownyMessaging.sendDebugMsg("Missing file: " + path + " deleting entry in townblocks.txt");
                        final TownyWorld world = townBlock.getWorld();
                        world.removeTownBlock(townBlock);
                        this.deleteTownBlock(townBlock);
                    }
                    else {
                        try {
                            final Town town = this.getTown(line.trim());
                            townBlock.setTown(town);
                        }
                        catch (Exception ex) {}
                        line = keys.get("name");
                        if (line != null) {
                            try {
                                townBlock.setName(line.trim());
                            }
                            catch (Exception ex2) {}
                        }
                        line = keys.get("price");
                        if (line != null) {
                            try {
                                townBlock.setPlotPrice(Double.parseDouble(line.trim()));
                            }
                            catch (Exception ex3) {}
                        }
                        line = keys.get("resident");
                        if (line != null && !line.isEmpty()) {
                            try {
                                final Resident res = this.getResident(line.trim());
                                townBlock.setResident(res);
                            }
                            catch (Exception ex4) {}
                        }
                        line = keys.get("type");
                        if (line != null) {
                            try {
                                townBlock.setType(Integer.parseInt(line));
                            }
                            catch (Exception ex5) {}
                        }
                        line = keys.get("outpost");
                        if (line != null) {
                            try {
                                townBlock.setOutpost(Boolean.parseBoolean(line));
                            }
                            catch (Exception ex6) {}
                        }
                        line = keys.get("permissions");
                        if (line != null && !line.isEmpty()) {
                            try {
                                townBlock.setPermissions(line.trim());
                            }
                            catch (Exception ex7) {}
                        }
                        line = keys.get("changed");
                        if (line != null) {
                            try {
                                townBlock.setChanged(Boolean.parseBoolean(line.trim()));
                            }
                            catch (Exception ex8) {}
                        }
                        line = keys.get("locked");
                        if (line != null) {
                            try {
                                townBlock.setLocked(Boolean.parseBoolean(line.trim()));
                            }
                            catch (Exception ex9) {}
                        }
                        test = "metadata";
                        line = keys.get("metadata");
                        if (line != null && !line.isEmpty()) {
                            townBlock.setMetadata(line.trim());
                        }
                        test = "groupID";
                        line = keys.get("groupID");
                        UUID groupID = null;
                        if (line != null && !line.isEmpty()) {
                            groupID = UUID.fromString(line.trim());
                        }
                        if (groupID == null) {
                            continue;
                        }
                        final PlotObjectGroup group = this.getPlotObjectGroup(townBlock.getTown().toString(), groupID);
                        townBlock.setPlotObjectGroup(group);
                    }
                }
                catch (Exception e) {
                    if (test != "town") {
                        TownyMessaging.sendErrorMsg("Loading Error: Exception while reading TownBlock file " + path + " at line: " + line);
                        return false;
                    }
                    TownyMessaging.sendDebugMsg("TownBlock file missing Town, deleting " + path);
                    TownyMessaging.sendDebugMsg("Missing file: " + path + " deleting entry in townblocks.txt");
                    final TownyWorld world2 = townBlock.getWorld();
                    world2.removeTownBlock(townBlock);
                    this.deleteTownBlock(townBlock);
                }
            }
            else {
                TownyMessaging.sendDebugMsg("Missing file: " + path + " deleting entry in townblocks.txt");
                final TownyWorld world3 = townBlock.getWorld();
                world3.removeTownBlock(townBlock);
                this.deleteTownBlock(townBlock);
            }
        }
        this.saveTownBlockList();
        return true;
    }
    
    @Override
    public boolean saveTownBlockList() {
        final List<String> list = new ArrayList<String>();
        for (final TownBlock townBlock : this.getAllTownBlocks()) {
            list.add(townBlock.getWorld().getName() + "," + townBlock.getX() + "," + townBlock.getZ());
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "townblocks.txt"));
        return true;
    }
    
    @Override
    public boolean savePlotGroupList() {
        final List<String> list = new ArrayList<String>();
        for (final PlotObjectGroup group : this.getAllPlotGroups()) {
            list.add(group.getTown().getName() + "," + group.getID() + "," + group.getGroupName());
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "plotgroups.txt"));
        return true;
    }
    
    @Override
    public boolean saveResidentList() {
        final List<String> list = new ArrayList<String>();
        for (final Resident resident : this.getResidents()) {
            try {
                list.add(NameValidation.checkAndFilterPlayerName(resident.getName()));
            }
            catch (InvalidNameException e) {
                TownyMessaging.sendErrorMsg("Saving Error: Exception while saving town list file:" + resident.getName());
            }
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "residents.txt"));
        return true;
    }
    
    @Override
    public boolean saveTownList() {
        final List<String> list = new ArrayList<String>();
        for (final Town town : this.getTowns()) {
            list.add(town.getName());
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "towns.txt"));
        return true;
    }
    
    @Override
    public boolean saveNationList() {
        final List<String> list = new ArrayList<String>();
        for (final Nation nation : this.getNations()) {
            list.add(nation.getName());
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "nations.txt"));
        return true;
    }
    
    @Override
    public boolean saveSiegeZoneList() {
        final List<String> list = new ArrayList<String>();
        for (final SiegeZone siegeZone : this.getSiegeZones()) {
            list.add(siegeZone.getName());
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "siegezones.txt"));
        return true;
    }
    
    @Override
    public boolean saveWorldList() {
        final List<String> list = new ArrayList<String>();
        for (final TownyWorld world : this.getWorlds()) {
            list.add(world.getName());
        }
        this.queryQueue.add(new FlatFile_Task(list, this.dataFolderPath + File.separator + "worlds.txt"));
        return true;
    }
    
    @Override
    public boolean saveResident(final Resident resident) {
        final List<String> list = new ArrayList<String>();
        list.add("lastOnline=" + resident.getLastOnline());
        list.add("registered=" + resident.getRegistered());
        list.add("isNPC=" + resident.isNPC());
        list.add("isJailed=" + resident.isJailed());
        list.add("JailSpawn=" + resident.getJailSpawn());
        list.add("JailDays=" + resident.getJailDays());
        list.add("JailTown=" + resident.getJailTown());
        list.add("title=" + resident.getTitle());
        list.add("surname=" + resident.getSurname());
        if (resident.hasTown()) {
            try {
                list.add("town=" + resident.getTown().getName());
            }
            catch (NotRegisteredException ex) {}
            list.add("town-ranks=" + StringMgmt.join(resident.getTownRanks(), ","));
            list.add("nation-ranks=" + StringMgmt.join(resident.getNationRanks(), ","));
        }
        list.add("friends=" + StringMgmt.join(resident.getFriends(), ","));
        list.add("");
        list.add("protectionStatus=" + resident.getPermissions().toString());
        final StringBuilder md = new StringBuilder();
        if (resident.hasMeta()) {
            final HashSet<CustomDataField> tdata = resident.getMetadata();
            for (final CustomDataField cdf : tdata) {
                md.append(cdf.toString()).append(";");
            }
        }
        list.add("metadata=" + md.toString());
        this.queryQueue.add(new FlatFile_Task(list, this.getResidentFilename(resident)));
        return true;
    }
    
    @Override
    public boolean saveTown(final Town town) {
        final List<String> list = new ArrayList<String>();
        list.add("name=" + town.getName());
        list.add("residents=" + StringMgmt.join(town.getResidents(), ","));
        if (town.hasMayor()) {
            list.add("mayor=" + town.getMayor().getName());
        }
        if (town.hasNation()) {
            try {
                list.add("nation=" + town.getNation().getName());
            }
            catch (NotRegisteredException ex) {}
        }
        list.add("assistants=" + StringMgmt.join(town.getAssistants(), ","));
        list.add(this.newLine);
        list.add("townBoard=" + town.getTownBoard());
        list.add("tag=" + town.getTag());
        list.add("protectionStatus=" + town.getPermissions().toString());
        list.add("bonusBlocks=" + town.getBonusBlocks());
        list.add("purchasedBlocks=" + town.getPurchasedBlocks());
        list.add("taxpercent=" + town.isTaxPercentage());
        list.add("taxes=" + town.getTaxes());
        list.add("plotPrice=" + town.getPlotPrice());
        list.add("plotTax=" + town.getPlotTax());
        list.add("commercialPlotPrice=" + town.getCommercialPlotPrice());
        list.add("commercialPlotTax=" + town.getCommercialPlotTax());
        list.add("embassyPlotPrice=" + town.getEmbassyPlotPrice());
        list.add("embassyPlotTax=" + town.getEmbassyPlotTax());
        list.add("spawnCost=" + town.getSpawnCost());
        list.add("hasUpkeep=" + town.hasUpkeep());
        list.add("open=" + town.isOpen());
        list.add("adminDisabledPvP=" + town.isAdminDisabledPVP());
        list.add("adminEnabledPvP=" + town.isAdminEnabledPVP());
        list.add("public=" + town.isPublic());
        list.add("conquered=" + town.isConquered());
        list.add("conqueredDays " + town.getConqueredDays());
        if (town.hasValidUUID()) {
            list.add("uuid=" + town.getUuid());
        }
        else {
            list.add("uuid=" + UUID.randomUUID());
        }
        list.add("registered=" + town.getRegistered());
        if (town.hasHomeBlock()) {
            try {
                list.add("homeBlock=" + town.getHomeBlock().getWorld().getName() + "," + town.getHomeBlock().getX() + "," + town.getHomeBlock().getZ());
            }
            catch (TownyException ex2) {}
        }
        if (town.hasSpawn()) {
            try {
                list.add("spawn=" + town.getSpawn().getWorld().getName() + "," + town.getSpawn().getX() + "," + town.getSpawn().getY() + "," + town.getSpawn().getZ() + "," + town.getSpawn().getPitch() + "," + town.getSpawn().getYaw());
            }
            catch (TownyException ex3) {}
        }
        final StringBuilder outpostArray = new StringBuilder("outpostspawns=");
        if (town.hasOutpostSpawn()) {
            for (final Location spawn : new ArrayList<Location>(town.getAllOutpostSpawns())) {
                outpostArray.append(spawn.getWorld().getName()).append(",").append(spawn.getX()).append(",").append(spawn.getY()).append(",").append(spawn.getZ()).append(",").append(spawn.getPitch()).append(",").append(spawn.getYaw()).append(";");
            }
        }
        list.add(outpostArray.toString());
        final StringBuilder jailArray = new StringBuilder("jailspawns=");
        if (town.hasJailSpawn()) {
            for (final Location spawn2 : new ArrayList<Location>(town.getAllJailSpawns())) {
                jailArray.append(spawn2.getWorld().getName()).append(",").append(spawn2.getX()).append(",").append(spawn2.getY()).append(",").append(spawn2.getZ()).append(",").append(spawn2.getPitch()).append(",").append(spawn2.getYaw()).append(";");
            }
        }
        list.add(jailArray.toString());
        list.add("outlaws=" + StringMgmt.join(town.getOutlaws(), ","));
        final StringBuilder md = new StringBuilder();
        if (town.hasMeta()) {
            final HashSet<CustomDataField> tdata = town.getMetadata();
            for (final CustomDataField cdf : tdata) {
                md.append(cdf.toString()).append(";");
            }
        }
        list.add("metadata=" + md.toString());
        list.add("recentlyRuinedEndTime=" + town.getRecentlyRuinedEndTime());
        list.add("revoltCooldownEndTime=" + town.getRevoltImmunityEndTime());
        list.add("siegeCooldownEndTime=" + town.getSiegeImmunityEndTime());
        if (town.hasSiege()) {
            final Siege siege = town.getSiege();
            list.add("siegeStatus=" + siege.getStatus().toString());
            list.add("siegeTownPlundered=" + siege.getTownPlundered());
            list.add("siegeTownInvaded=" + siege.getTownInvaded());
            if (siege.getAttackerWinner() != null) {
                list.add("siegeAttackerWinner=" + siege.getAttackerWinner().getName());
            }
            list.add("siegeActualStartTime=" + siege.getStartTime());
            list.add("siegeScheduledEndTime=" + siege.getScheduledEndTime());
            list.add("siegeActualEndTime=" + siege.getActualEndTime());
            list.add("siegeZones=" + StringMgmt.join(town.getSiege().getSiegeZoneNames(), ","));
        }
        list.add("occupied=" + town.isOccupied());
        list.add("neutral=" + town.isNeutral());
        list.add("desiredNeutralityValue=" + town.getDesiredNeutralityValue());
        list.add("neutralityChangeConfirmationCounterDays=" + town.getNeutralityChangeConfirmationCounterDays());
        this.queryQueue.add(new FlatFile_Task(list, this.getTownFilename(town)));
        return true;
    }
    
    @Override
    public boolean savePlotGroup(final PlotObjectGroup group) {
        final List<String> list = new ArrayList<String>();
        list.add("groupID=" + group.getID().toString());
        list.add("groupName=" + group.getGroupName());
        list.add("groupPrice=" + group.getPrice());
        list.add("town=" + group.getTown().toString());
        this.queryQueue.add(new FlatFile_Task(list, this.getPlotGroupFilename(group)));
        return true;
    }
    
    @Override
    public boolean saveNation(final Nation nation) {
        final List<String> list = new ArrayList<String>();
        list.add("towns=" + StringMgmt.join(nation.getTowns(), ","));
        if (nation.hasCapital()) {
            list.add("capital=" + nation.getCapital().getName());
        }
        list.add("nationBoard=" + nation.getNationBoard());
        if (nation.hasTag()) {
            list.add("tag=" + nation.getTag());
        }
        list.add("assistants=" + StringMgmt.join(nation.getAssistants(), ","));
        list.add("allies=" + StringMgmt.join(nation.getAllies(), ","));
        list.add("enemies=" + StringMgmt.join(nation.getEnemies(), ","));
        list.add("siegeZones=" + StringMgmt.join(nation.getSiegeZoneNames(), ","));
        list.add("taxes=" + nation.getTaxes());
        list.add("spawnCost=" + nation.getSpawnCost());
        list.add("neutral=" + nation.isNeutral());
        if (nation.hasValidUUID()) {
            list.add("uuid=" + nation.getUuid());
        }
        else {
            list.add("uuid=" + UUID.randomUUID());
        }
        list.add("registered=" + nation.getRegistered());
        if (nation.hasNationSpawn()) {
            try {
                list.add("nationSpawn=" + nation.getNationSpawn().getWorld().getName() + "," + nation.getNationSpawn().getX() + "," + nation.getNationSpawn().getY() + "," + nation.getNationSpawn().getZ() + "," + nation.getNationSpawn().getPitch() + "," + nation.getNationSpawn().getYaw());
            }
            catch (TownyException ex) {}
        }
        list.add("isPublic=" + nation.isPublic());
        list.add("isOpen=" + nation.isOpen());
        final StringBuilder md = new StringBuilder();
        if (nation.hasMeta()) {
            final HashSet<CustomDataField> tdata = nation.getMetadata();
            for (final CustomDataField cdf : tdata) {
                md.append(cdf.toString()).append(";");
            }
        }
        list.add("metadata=" + md.toString());
        this.queryQueue.add(new FlatFile_Task(list, this.getNationFilename(nation)));
        return true;
    }
    
    @Override
    public boolean saveSiegeZone(final SiegeZone siegeZone) {
        final List<String> list = new ArrayList<String>();
        list.add("flagLocation=" + siegeZone.getFlagLocation().getWorld().getName() + "," + siegeZone.getFlagLocation().getX() + "," + siegeZone.getFlagLocation().getY() + "," + siegeZone.getFlagLocation().getZ());
        list.add("attackingNation=" + siegeZone.getAttackingNation().getName());
        list.add("defendingTown=" + siegeZone.getDefendingTown().getName());
        list.add("siegePoints=" + siegeZone.getSiegePoints());
        list.add("warChestAmount=" + siegeZone.getWarChestAmount());
        this.queryQueue.add(new FlatFile_Task(list, this.getSiegeZoneFilename(siegeZone)));
        return true;
    }
    
    @Override
    public boolean saveWorld(final TownyWorld world) {
        final List<String> list = new ArrayList<String>();
        list.add("towns=" + StringMgmt.join(world.getTowns(), ","));
        list.add("");
        list.add("pvp=" + world.isPVP());
        list.add("forcepvp=" + world.isForcePVP());
        list.add("# Can players found towns and claim plots in this world?");
        list.add("claimable=" + world.isClaimable());
        list.add("worldmobs=" + world.hasWorldMobs());
        list.add("forcetownmobs=" + world.isForceTownMobs());
        list.add("firespread=" + world.isFire());
        list.add("forcefirespread=" + world.isForceFire());
        list.add("explosions=" + world.isExpl());
        list.add("forceexplosions=" + world.isForceExpl());
        list.add("endermanprotect=" + world.isEndermanProtect());
        list.add("disableplayertrample=" + world.isDisablePlayerTrample());
        list.add("disablecreaturetrample=" + world.isDisableCreatureTrample());
        list.add(this.newLine);
        list.add("# Unclaimed Zone settings.");
        if (world.getUnclaimedZoneBuild() != null) {
            list.add("unclaimedZoneBuild=" + world.getUnclaimedZoneBuild());
        }
        if (world.getUnclaimedZoneDestroy() != null) {
            list.add("unclaimedZoneDestroy=" + world.getUnclaimedZoneDestroy());
        }
        if (world.getUnclaimedZoneSwitch() != null) {
            list.add("unclaimedZoneSwitch=" + world.getUnclaimedZoneSwitch());
        }
        if (world.getUnclaimedZoneItemUse() != null) {
            list.add("unclaimedZoneItemUse=" + world.getUnclaimedZoneItemUse());
        }
        if (world.getUnclaimedZoneName() != null) {
            list.add("unclaimedZoneName=" + world.getUnclaimedZoneName());
        }
        list.add("");
        list.add("# The following settings are only used if you are not using any permissions provider plugin");
        if (world.getUnclaimedZoneIgnoreMaterials() != null) {
            list.add("unclaimedZoneIgnoreIds=" + StringMgmt.join(world.getUnclaimedZoneIgnoreMaterials(), ","));
        }
        list.add(this.newLine);
        list.add("# The following settings control what blocks are deleted upon a townblock being unclaimed");
        list.add("usingPlotManagementDelete=" + world.isUsingPlotManagementDelete());
        if (world.getPlotManagementDeleteIds() != null) {
            list.add("plotManagementDeleteIds=" + StringMgmt.join(world.getPlotManagementDeleteIds(), ","));
        }
        list.add(this.newLine);
        list.add("# The following settings control what blocks are deleted upon a mayor issuing a '/plot clear' command");
        list.add("usingPlotManagementMayorDelete=" + world.isUsingPlotManagementMayorDelete());
        if (world.getPlotManagementMayorDelete() != null) {
            list.add("plotManagementMayorDelete=" + StringMgmt.join(world.getPlotManagementMayorDelete(), ","));
        }
        list.add(this.newLine + "# If enabled when a town claims a townblock a snapshot will be taken at the time it is claimed.");
        list.add("# When the townblock is unclaimded its blocks will begin to revert to the original snapshot.");
        list.add("usingPlotManagementRevert=" + world.isUsingPlotManagementRevert());
        list.add("# Any block Id's listed here will not be respawned. Instead it will revert to air.");
        if (world.getPlotManagementIgnoreIds() != null) {
            list.add("plotManagementIgnoreIds=" + StringMgmt.join(world.getPlotManagementIgnoreIds(), ","));
        }
        list.add("");
        list.add("# If enabled any damage caused by explosions will repair itself.");
        list.add("usingPlotManagementWildRegen=" + world.isUsingPlotManagementWildRevert());
        if (world.getPlotManagementWildRevertEntities() != null) {
            list.add("PlotManagementWildRegenEntities=" + StringMgmt.join(world.getPlotManagementWildRevertEntities(), ","));
        }
        list.add("usingPlotManagementWildRegenDelay=" + world.getPlotManagementWildRevertDelay());
        list.add("");
        list.add("# This setting is used to enable or disable Towny in this world.");
        list.add("usingTowny=" + world.isUsingTowny());
        list.add("");
        list.add("# This setting is used to enable or disable Event war in this world.");
        list.add("warAllowed=" + world.isWarAllowed());
        final StringBuilder md = new StringBuilder();
        if (world.hasMeta()) {
            final HashSet<CustomDataField> tdata = world.getMetadata();
            for (final CustomDataField cdf : tdata) {
                md.append(cdf.toString()).append(";");
            }
        }
        list.add("metadata=" + md.toString());
        this.queryQueue.add(new FlatFile_Task(list, this.getWorldFilename(world)));
        return true;
    }
    
    @Override
    public boolean saveAllTownBlocks() {
        for (final TownyWorld world : this.getWorlds()) {
            for (final TownBlock townBlock : world.getTownBlocks()) {
                this.saveTownBlock(townBlock);
            }
        }
        return true;
    }
    
    @Override
    public boolean saveTownBlock(final TownBlock townBlock) {
        FileMgmt.checkOrCreateFolder(this.dataFolderPath + File.separator + "townblocks" + File.separator + townBlock.getWorld().getName());
        final List<String> list = new ArrayList<String>();
        list.add("name=" + townBlock.getName());
        list.add("price=" + townBlock.getPlotPrice());
        try {
            list.add("town=" + townBlock.getTown().getName());
        }
        catch (NotRegisteredException ex) {}
        if (townBlock.hasResident()) {
            try {
                list.add("resident=" + townBlock.getResident().getName());
            }
            catch (NotRegisteredException ex2) {}
        }
        list.add("type=" + townBlock.getType().getId());
        list.add("outpost=" + townBlock.isOutpost());
        if (townBlock.isChanged()) {
            list.add("permissions=" + townBlock.getPermissions().toString());
        }
        list.add("changed=" + townBlock.isChanged());
        list.add("locked=" + townBlock.isLocked());
        final StringBuilder md = new StringBuilder();
        if (townBlock.hasMeta()) {
            final HashSet<CustomDataField> tdata = townBlock.getMetadata();
            for (final CustomDataField cdf : tdata) {
                md.append(cdf.toString()).append(";");
            }
        }
        list.add("metadata=" + md.toString());
        final StringBuilder groupID = new StringBuilder();
        final StringBuilder groupName = new StringBuilder();
        if (townBlock.hasPlotObjectGroup()) {
            groupID.append(townBlock.getPlotObjectGroup().getID());
            groupName.append(townBlock.getPlotObjectGroup().getGroupName());
        }
        list.add("groupID=" + groupID.toString());
        this.queryQueue.add(new FlatFile_Task(list, this.getTownBlockFilename(townBlock)));
        return true;
    }
    
    @Override
    public boolean saveRegenList() {
        try {
            final BufferedWriter fout = new BufferedWriter(new FileWriter(this.dataFolderPath + File.separator + "regen.txt"));
            try {
                for (final PlotBlockData plot : new ArrayList<PlotBlockData>(TownyRegenAPI.getPlotChunks().values())) {
                    fout.write(plot.getWorldName() + "," + plot.getX() + "," + plot.getZ() + this.newLine);
                }
                fout.close();
            }
            catch (Throwable t) {
                try {
                    fout.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Saving Error: Exception while saving regen file");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Override
    public boolean saveSnapshotList() {
        try {
            final BufferedWriter fout = new BufferedWriter(new FileWriter(this.dataFolderPath + File.separator + "snapshot_queue.txt"));
            try {
                while (TownyRegenAPI.hasWorldCoords()) {
                    final WorldCoord worldCoord = TownyRegenAPI.getWorldCoord();
                    fout.write(worldCoord.getWorldName() + "," + worldCoord.getX() + "," + worldCoord.getZ() + this.newLine);
                }
                fout.close();
            }
            catch (Throwable t) {
                try {
                    fout.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Saving Error: Exception while saving snapshot_queue file");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Deprecated
    public void utilLoadTownBlocks(final String line, final Town town, final Resident resident) {
        final String[] split2;
        split2 = line.split("\\|");
        for (final String w : split2) {
            final String[] split = w.split(":");
            if (split.length != 2) {
                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " BlockList does not have a World or data.");
            }
            else {
                try {
                    final TownyWorld world = this.getWorld(split[0]);
                    for (String s : split[1].split(";")) {
                        String blockTypeData = null;
                        final int indexOfType = s.indexOf("[");
                        if (indexOfType != -1) {
                            final int endIndexOfType = s.indexOf("]");
                            if (endIndexOfType != -1) {
                                blockTypeData = s.substring(indexOfType + 1, endIndexOfType);
                            }
                            s = s.substring(endIndexOfType + 1);
                        }
                        final String[] tokens = s.split(",");
                        if (tokens.length >= 2) {
                            try {
                                final int x = Integer.parseInt(tokens[0]);
                                final int z = Integer.parseInt(tokens[1]);
                                try {
                                    world.newTownBlock(x, z);
                                }
                                catch (AlreadyRegisteredException ex) {}
                                final TownBlock townblock = world.getTownBlock(x, z);
                                if (town != null) {
                                    townblock.setTown(town);
                                }
                                if (resident != null && townblock.hasTown()) {
                                    townblock.setResident(resident);
                                }
                                if (blockTypeData != null) {
                                    this.utilLoadTownBlockTypeData(townblock, blockTypeData);
                                }
                                if (tokens.length >= 3) {
                                    if (tokens[2].equals("true")) {
                                        townblock.setPlotPrice(town.getPlotPrice());
                                    }
                                    else {
                                        townblock.setPlotPrice(Double.parseDouble(tokens[2]));
                                    }
                                }
                            }
                            catch (NumberFormatException ex2) {}
                            catch (NotRegisteredException ex3) {}
                        }
                    }
                }
                catch (NotRegisteredException ex4) {}
            }
        }
    }
    
    @Deprecated
    public void utilLoadTownBlockTypeData(final TownBlock townBlock, final String data) {
        final String[] tokens = data.split(",");
        if (tokens.length >= 1) {
            townBlock.setType(Integer.valueOf(tokens[0]));
        }
        if (tokens.length >= 2) {
            townBlock.setOutpost(tokens[1].equalsIgnoreCase("1"));
        }
    }
    
    @Deprecated
    public String utilSaveTownBlocks(final List<TownBlock> townBlocks) {
        final HashMap<TownyWorld, ArrayList<TownBlock>> worlds = new HashMap<TownyWorld, ArrayList<TownBlock>>();
        final StringBuilder out = new StringBuilder();
        for (final TownBlock townBlock : townBlocks) {
            final TownyWorld world = townBlock.getWorld();
            if (!worlds.containsKey(world)) {
                worlds.put(world, new ArrayList<TownBlock>());
            }
            worlds.get(world).add(townBlock);
        }
        for (final TownyWorld world2 : worlds.keySet()) {
            out.append(world2.getName()).append(":");
            for (final TownBlock townBlock2 : worlds.get(world2)) {
                out.append("[").append(townBlock2.getType().getId());
                out.append(",").append(townBlock2.isOutpost() ? "1" : "0");
                out.append("]").append(townBlock2.getX()).append(",").append(townBlock2.getZ()).append(",").append(townBlock2.getPlotPrice()).append(";");
            }
            out.append("|");
        }
        return out.toString();
    }
    
    @Override
    public PlotBlockData loadPlotData(final String worldName, final int x, final int z) {
        try {
            final TownyWorld world = this.getWorld(worldName);
            final TownBlock townBlock = new TownBlock(x, z, world);
            return this.loadPlotData(townBlock);
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public PlotBlockData loadPlotData(final TownBlock townBlock) {
        final String fileName = this.getPlotFilename(townBlock);
        if (this.isFile(fileName)) {
            final PlotBlockData plotBlockData = new PlotBlockData(townBlock);
            final List<String> blockArr = new ArrayList<String>();
            int version = 0;
            try {
                final DataInputStream fin = new DataInputStream(new FileInputStream(fileName));
                try {
                    fin.mark(3);
                    final byte[] key = new byte[3];
                    fin.read(key, 0, 3);
                    final String test = new String(key);
                    if (elements.fromString(test) == elements.VER) {
                        version = fin.read();
                        plotBlockData.setVersion(version);
                        plotBlockData.setHeight(fin.readInt());
                    }
                    else {
                        plotBlockData.setVersion(version);
                        fin.reset();
                        plotBlockData.setHeight(fin.readInt());
                        blockArr.add(fin.readUTF());
                        blockArr.add(fin.readUTF());
                    }
                    switch (version) {
                        default: {
                            String value;
                            while ((value = fin.readUTF()) != null) {
                                blockArr.add(value);
                            }
                            break;
                        }
                        case 2: {
                            int temp = 0;
                            while ((temp = fin.readInt()) >= 0) {
                                blockArr.add(temp + "");
                            }
                            break;
                        }
                    }
                    fin.close();
                }
                catch (Throwable t) {
                    try {
                        fin.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                    throw t;
                }
            }
            catch (EOFException ex) {}
            catch (IOException e) {
                e.printStackTrace();
            }
            plotBlockData.setBlockList(blockArr);
            plotBlockData.resetBlockListRestored();
            return plotBlockData;
        }
        return null;
    }
    
    @Override
    public void deletePlotData(final PlotBlockData plotChunk) {
        final File file = new File(this.getPlotFilename(plotChunk));
        if (file.exists()) {
            file.delete();
        }
    }
    
    private boolean isFile(final String fileName) {
        final File file = new File(fileName);
        return file.exists() && file.isFile();
    }
    
    @Override
    public void deleteFile(final String fileName) {
        final File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Override
    public void deleteResident(final Resident resident) {
        final File file = new File(this.getResidentFilename(resident));
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Override
    public void deleteTown(final Town town) {
        final File file = new File(this.getTownFilename(town));
        if (file.exists()) {
            FileMgmt.moveFile(file, "deleted");
        }
    }
    
    @Override
    public void deleteNation(final Nation nation) {
        final File file = new File(this.getNationFilename(nation));
        if (file.exists()) {
            FileMgmt.moveFile(file, "deleted");
        }
    }
    
    @Override
    public void deleteSiegeZone(final SiegeZone siegeZone) {
        final File file = new File(this.getSiegeZoneFilename(siegeZone));
        if (file.exists()) {
            FileMgmt.moveFile(file, "deleted");
        }
    }
    
    @Override
    public void deleteWorld(final TownyWorld world) {
        final File file = new File(this.getWorldFilename(world));
        if (file.exists()) {
            FileMgmt.moveFile(file, "deleted");
        }
    }
    
    @Override
    public void deleteTownBlock(final TownBlock townBlock) {
        final File file = new File(this.getTownBlockFilename(townBlock));
        if (file.exists()) {
            file.deleteOnExit();
        }
    }
    
    @Override
    public void deletePlotGroup(final PlotObjectGroup group) {
        final File file = new File(this.getPlotGroupFilename(group));
        if (file.exists()) {
            file.deleteOnExit();
        }
        else {
            TownyMessaging.sendErrorMsg("That file doesn't exist!");
        }
    }
    
    public enum elements
    {
        VER, 
        novalue;
        
        public static elements fromString(final String Str) {
            try {
                return valueOf(Str);
            }
            catch (Exception ex) {
                return elements.novalue;
            }
        }
    }
}
