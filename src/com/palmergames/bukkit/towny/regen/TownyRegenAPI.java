// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.regen;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.TownyMessaging;
import org.bukkit.World;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.Material;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.block.Block;
import java.util.Set;
import com.palmergames.bukkit.towny.tasks.ProtectionRegenTask;
import com.palmergames.bukkit.towny.regen.block.BlockLocation;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.List;
import java.util.Hashtable;

public class TownyRegenAPI
{
    private static Hashtable<String, PlotBlockData> PlotChunks;
    private static List<WorldCoord> deleteTownBlockIdQueue;
    private static List<WorldCoord> worldCoords;
    private static Hashtable<BlockLocation, ProtectionRegenTask> protectionRegenTasks;
    private static Set<Block> protectionPlaceholders;
    
    public static void initialize(final Towny plugin) {
    }
    
    public static void addWorldCoord(final WorldCoord worldCoord) {
        if (!TownyRegenAPI.worldCoords.contains(worldCoord)) {
            TownyRegenAPI.worldCoords.add(worldCoord);
        }
    }
    
    public static boolean hasWorldCoords() {
        return TownyRegenAPI.worldCoords.size() != 0;
    }
    
    public static boolean hasWorldCoord(final WorldCoord worldCoord) {
        return TownyRegenAPI.worldCoords.contains(worldCoord);
    }
    
    public static WorldCoord getWorldCoord() {
        if (!TownyRegenAPI.worldCoords.isEmpty()) {
            final WorldCoord wc = TownyRegenAPI.worldCoords.get(0);
            TownyRegenAPI.worldCoords.remove(0);
            return wc;
        }
        return null;
    }
    
    public static Hashtable<String, PlotBlockData> getPlotChunks() {
        return TownyRegenAPI.PlotChunks;
    }
    
    public static boolean hasPlotChunks() {
        return !TownyRegenAPI.PlotChunks.isEmpty();
    }
    
    public static void setPlotChunks(final Hashtable<String, PlotBlockData> plotChunks) {
        TownyRegenAPI.PlotChunks = plotChunks;
    }
    
    public static void deletePlotChunk(final PlotBlockData plotChunk) {
        if (TownyRegenAPI.PlotChunks.containsKey(getPlotKey(plotChunk))) {
            TownyRegenAPI.PlotChunks.remove(getPlotKey(plotChunk));
            TownyUniverse.getInstance().getDataSource().saveRegenList();
        }
    }
    
    public static void addPlotChunk(final PlotBlockData plotChunk, final boolean save) {
        if (!TownyRegenAPI.PlotChunks.containsKey(getPlotKey(plotChunk))) {
            TownyRegenAPI.PlotChunks.put(getPlotKey(plotChunk), plotChunk);
            if (save) {
                TownyUniverse.getInstance().getDataSource().saveRegenList();
            }
        }
    }
    
    public static void addPlotChunkSnapshot(final PlotBlockData plotChunk) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (townyUniverse.getDataSource().loadPlotData(plotChunk.getWorldName(), plotChunk.getX(), plotChunk.getZ()) == null) {
            townyUniverse.getDataSource().savePlotData(plotChunk);
        }
    }
    
    public static void deletePlotChunkSnapshot(final PlotBlockData plotChunk) {
        TownyUniverse.getInstance().getDataSource().deletePlotData(plotChunk);
    }
    
    public static PlotBlockData getPlotChunkSnapshot(final TownBlock townBlock) {
        return TownyUniverse.getInstance().getDataSource().loadPlotData(townBlock);
    }
    
    public static PlotBlockData getPlotChunk(final TownBlock townBlock) {
        if (TownyRegenAPI.PlotChunks.containsKey(getPlotKey(townBlock))) {
            return TownyRegenAPI.PlotChunks.get(getPlotKey(townBlock));
        }
        return null;
    }
    
    private static String getPlotKey(final PlotBlockData plotChunk) {
        return "[" + plotChunk.getWorldName() + "|" + plotChunk.getX() + "|" + plotChunk.getZ() + "]";
    }
    
    public static String getPlotKey(final TownBlock townBlock) {
        return "[" + townBlock.getWorld().getName() + "|" + townBlock.getX() + "|" + townBlock.getZ() + "]";
    }
    
    public static boolean hasDeleteTownBlockIdQueue() {
        return !TownyRegenAPI.deleteTownBlockIdQueue.isEmpty();
    }
    
    public static boolean isDeleteTownBlockIdQueue(final WorldCoord plot) {
        return TownyRegenAPI.deleteTownBlockIdQueue.contains(plot);
    }
    
    public static void addDeleteTownBlockIdQueue(final WorldCoord plot) {
        if (!TownyRegenAPI.deleteTownBlockIdQueue.contains(plot)) {
            TownyRegenAPI.deleteTownBlockIdQueue.add(plot);
        }
    }
    
    public static WorldCoord getDeleteTownBlockIdQueue() {
        if (!TownyRegenAPI.deleteTownBlockIdQueue.isEmpty()) {
            final WorldCoord wc = TownyRegenAPI.deleteTownBlockIdQueue.get(0);
            TownyRegenAPI.deleteTownBlockIdQueue.remove(0);
            return wc;
        }
        return null;
    }
    
    public static void doDeleteTownBlockIds(final WorldCoord worldCoord) {
        World world = null;
        final int plotSize = TownySettings.getTownBlockSize();
        world = worldCoord.getBukkitWorld();
        if (world != null) {
            final int height = world.getMaxHeight() - 1;
            final int worldx = worldCoord.getX() * plotSize;
            final int worldz = worldCoord.getZ() * plotSize;
            for (int z = 0; z < plotSize; ++z) {
                for (int x = 0; x < plotSize; ++x) {
                    for (int y = height; y > 0; --y) {
                        Block block = world.getBlockAt(worldx + x, y, worldz + z);
                        try {
                            if (worldCoord.getTownyWorld().isPlotManagementDeleteIds(block.getType().name())) {
                                block.setType(Material.AIR);
                            }
                        }
                        catch (NotRegisteredException ex) {}
                        block = null;
                    }
                }
            }
        }
    }
    
    public static void deleteTownBlockMaterial(final TownBlock townBlock, final Material material) {
        final int plotSize = TownySettings.getTownBlockSize();
        TownyMessaging.sendDebugMsg("Processing deleteTownBlockMaterial");
        final World world = BukkitTools.getServer().getWorld(townBlock.getWorld().getName());
        if (world != null) {
            final int height = world.getMaxHeight() - 1;
            final int worldx = townBlock.getX() * plotSize;
            final int worldz = townBlock.getZ() * plotSize;
            for (int z = 0; z < plotSize; ++z) {
                for (int x = 0; x < plotSize; ++x) {
                    for (int y = height; y > 0; --y) {
                        Block block = world.getBlockAt(worldx + x, y, worldz + z);
                        if (block.getType() == material) {
                            block.setType(Material.AIR);
                        }
                        block = null;
                    }
                }
            }
        }
    }
    
    public static boolean hasProtectionRegenTask(final BlockLocation blockLocation) {
        return TownyRegenAPI.protectionRegenTasks.containsKey(blockLocation);
    }
    
    public static ProtectionRegenTask GetProtectionRegenTask(final BlockLocation blockLocation) {
        if (TownyRegenAPI.protectionRegenTasks.containsKey(blockLocation)) {
            return TownyRegenAPI.protectionRegenTasks.get(blockLocation);
        }
        return null;
    }
    
    public static void addProtectionRegenTask(final ProtectionRegenTask task) {
        TownyRegenAPI.protectionRegenTasks.put(task.getBlockLocation(), task);
    }
    
    public static void removeProtectionRegenTask(final ProtectionRegenTask task) {
        TownyRegenAPI.protectionRegenTasks.remove(task.getBlockLocation());
        if (TownyRegenAPI.protectionRegenTasks.isEmpty()) {
            TownyRegenAPI.protectionPlaceholders.clear();
        }
    }
    
    public static void cancelProtectionRegenTasks() {
        for (final ProtectionRegenTask task : TownyRegenAPI.protectionRegenTasks.values()) {
            BukkitTools.getServer().getScheduler().cancelTask(task.getTaskId());
            task.replaceProtections();
        }
        TownyRegenAPI.protectionRegenTasks.clear();
        TownyRegenAPI.protectionPlaceholders.clear();
    }
    
    public static boolean isPlaceholder(final Block block) {
        return TownyRegenAPI.protectionPlaceholders.contains(block);
    }
    
    public static void addPlaceholder(final Block block) {
        TownyRegenAPI.protectionPlaceholders.add(block);
    }
    
    public static void removePlaceholder(final Block block) {
        TownyRegenAPI.protectionPlaceholders.remove(block);
    }
    
    static {
        TownyRegenAPI.PlotChunks = new Hashtable<String, PlotBlockData>();
        TownyRegenAPI.deleteTownBlockIdQueue = new ArrayList<WorldCoord>();
        TownyRegenAPI.worldCoords = new ArrayList<WorldCoord>();
        TownyRegenAPI.protectionRegenTasks = new Hashtable<BlockLocation, ProtectionRegenTask>();
        TownyRegenAPI.protectionPlaceholders = new HashSet<Block>();
    }
}
