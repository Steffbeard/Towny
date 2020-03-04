// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import org.apache.logging.log4j.LogManager;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import java.util.Collection;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.Towny;
import org.apache.logging.log4j.Logger;

public class RepeatingTimerTask extends TownyTimerTask
{
    private static final Logger LOGGER;
    private Long timerCounter;
    
    public RepeatingTimerTask(final Towny plugin) {
        super(plugin);
        this.timerCounter = 0L;
    }
    
    @Override
    public void run() {
        if (TownyRegenAPI.hasPlotChunks()) {
            final long max = Math.max(1L, TownySettings.getPlotManagementSpeed());
            final Long value = this.timerCounter + 1L;
            this.timerCounter = value;
            if (max <= value) {
                for (final PlotBlockData plotChunk : new ArrayList<PlotBlockData>(TownyRegenAPI.getPlotChunks().values())) {
                    if (!plotChunk.restoreNextBlock()) {
                        TownyRegenAPI.deletePlotChunk(plotChunk);
                        TownyRegenAPI.deletePlotChunkSnapshot(plotChunk);
                    }
                }
                this.timerCounter = 0L;
            }
        }
        if (TownyRegenAPI.hasWorldCoords()) {
            try {
                final TownBlock townBlock = TownyRegenAPI.getWorldCoord().getTownBlock();
                final PlotBlockData plotChunk = new PlotBlockData(townBlock);
                plotChunk.initialize();
                if (!plotChunk.getBlockList().isEmpty() && plotChunk.getBlockList() != null) {
                    TownyRegenAPI.addPlotChunkSnapshot(plotChunk);
                }
                townBlock.setLocked(false);
                TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
                this.plugin.updateCache(townBlock.getWorldCoord());
                if (!TownyRegenAPI.hasWorldCoords()) {
                    RepeatingTimerTask.LOGGER.info("Plot snapshots completed.");
                }
            }
            catch (NotRegisteredException ex) {}
        }
        if (TownyRegenAPI.hasDeleteTownBlockIdQueue()) {
            TownyRegenAPI.doDeleteTownBlockIds(TownyRegenAPI.getDeleteTownBlockIdQueue());
        }
    }
    
    static {
        LOGGER = LogManager.getLogger((Class)Towny.class);
    }
}
