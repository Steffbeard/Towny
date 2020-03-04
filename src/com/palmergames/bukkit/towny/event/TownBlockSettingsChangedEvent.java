// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownBlockSettingsChangedEvent extends Event
{
    private static final HandlerList handlers;
    private TownyWorld w;
    private Town t;
    private TownBlock tb;
    
    public HandlerList getHandlers() {
        return TownBlockSettingsChangedEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownBlockSettingsChangedEvent.handlers;
    }
    
    private TownBlockSettingsChangedEvent() {
        super(!Bukkit.getServer().isPrimaryThread());
    }
    
    public TownBlockSettingsChangedEvent(final TownyWorld w) {
        this();
        this.w = w;
    }
    
    public TownBlockSettingsChangedEvent(final Town t) {
        this();
        this.t = t;
    }
    
    public TownBlockSettingsChangedEvent(final TownBlock tb) {
        this();
        this.tb = tb;
    }
    
    public TownyWorld getTownyWorld() {
        return this.w;
    }
    
    public Town getTown() {
        return this.t;
    }
    
    public TownBlock getTownBlock() {
        return this.tb;
    }
    
    static {
        handlers = new HandlerList();
    }
}
