// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlotChangeTypeEvent extends Event
{
    public static final HandlerList handlers;
    private TownBlockType oldType;
    private TownBlockType newType;
    private TownBlock townBlock;
    
    public HandlerList getHandlers() {
        return PlotChangeTypeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlotChangeTypeEvent.handlers;
    }
    
    public PlotChangeTypeEvent(final TownBlockType oldType, final TownBlockType newType, final TownBlock townBlock) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.newType = newType;
        this.oldType = oldType;
        this.townBlock = townBlock;
    }
    
    public TownBlockType getNewType() {
        return this.newType;
    }
    
    public TownBlockType getOldType() {
        if (this.oldType == null) {
            return TownBlockType.WILDS;
        }
        return this.oldType;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    static {
        handlers = new HandlerList();
    }
}
