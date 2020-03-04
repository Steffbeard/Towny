// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlotClearEvent extends Event
{
    private static final HandlerList handlers;
    private TownBlock townBlock;
    
    public HandlerList getHandlers() {
        return PlotClearEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlotClearEvent.handlers;
    }
    
    public PlotClearEvent(final TownBlock _townBlock) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.townBlock = _townBlock;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    static {
        handlers = new HandlerList();
    }
}
