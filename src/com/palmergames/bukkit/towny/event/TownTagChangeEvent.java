// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownTagChangeEvent extends Event
{
    private static final HandlerList handlers;
    private Town town;
    private String newTag;
    
    public HandlerList getHandlers() {
        return TownTagChangeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownTagChangeEvent.handlers;
    }
    
    public TownTagChangeEvent(final String newTag, final Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.newTag = newTag;
        this.town = town;
    }
    
    public String getNewTag() {
        return this.newTag;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    static {
        handlers = new HandlerList();
    }
}
