// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlotChangeOwnerEvent extends Event
{
    public static final HandlerList handlers;
    private Resident oldowner;
    private Resident newowner;
    private TownBlock townBlock;
    
    public HandlerList getHandlers() {
        return PlotChangeOwnerEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlotChangeOwnerEvent.handlers;
    }
    
    public PlotChangeOwnerEvent(final Resident oldowner, final Resident newowner, final TownBlock townBlock) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.newowner = newowner;
        this.oldowner = oldowner;
        this.townBlock = townBlock;
    }
    
    public String getNewowner() {
        return this.newowner.getName();
    }
    
    public String getOldowner() {
        if (this.oldowner == null) {
            return "undefined";
        }
        return this.oldowner.getName();
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    static {
        handlers = new HandlerList();
    }
}
