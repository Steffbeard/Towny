// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class DeleteTownEvent extends Event
{
    private static final HandlerList handlers;
    private String townName;
    
    public HandlerList getHandlers() {
        return DeleteTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return DeleteTownEvent.handlers;
    }
    
    public DeleteTownEvent(final String town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.townName = town;
    }
    
    public String getTownName() {
        return this.townName;
    }
    
    static {
        handlers = new HandlerList();
    }
}
