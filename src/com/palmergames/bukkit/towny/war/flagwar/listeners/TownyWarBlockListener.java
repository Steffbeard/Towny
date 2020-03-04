// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar.listeners;

import org.bukkit.event.block.BlockPistonRetractEvent;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Cancellable;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import org.bukkit.event.block.BlockBreakEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyWarBlockListener implements Listener
{
    public TownyWarBlockListener(final Towny plugin) {
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        TownyWar.checkBlock(event.getPlayer(), event.getBlock(), (Cancellable)event);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBurn(final BlockBurnEvent event) {
        TownyWar.checkBlock(null, event.getBlock(), (Cancellable)event);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        for (final Block block : event.getBlocks()) {
            TownyWar.checkBlock(null, block, (Cancellable)event);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
    }
}
