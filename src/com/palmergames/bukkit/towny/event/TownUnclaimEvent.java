// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownUnclaimEvent extends Event
{
    private static final HandlerList handlers;
    private Town town;
    private WorldCoord worldCoord;
    
    public HandlerList getHandlers() {
        return TownUnclaimEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownUnclaimEvent.handlers;
    }
    
    public TownUnclaimEvent(final Town _town, final WorldCoord _worldcoord) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = _town;
        this.worldCoord = _worldcoord;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public WorldCoord getWorldCoord() {
        return this.worldCoord;
    }
    
    static {
        handlers = new HandlerList();
    }
}
