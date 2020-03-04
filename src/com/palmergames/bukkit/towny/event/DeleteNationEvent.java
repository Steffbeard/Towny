// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class DeleteNationEvent extends Event
{
    private static final HandlerList handlers;
    private String nationName;
    
    public HandlerList getHandlers() {
        return DeleteNationEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return DeleteNationEvent.handlers;
    }
    
    public DeleteNationEvent(final String nation) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.nationName = nation;
    }
    
    public String getNationName() {
        return this.nationName;
    }
    
    static {
        handlers = new HandlerList();
    }
}
