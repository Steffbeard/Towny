// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PreDeleteNationEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private String nationName;
    private boolean isCancelled;
    
    public PreDeleteNationEvent(final String nation) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.nationName = nation;
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return PreDeleteNationEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PreDeleteNationEvent.handlers;
    }
    
    public String getNationName() {
        return this.nationName;
    }
    
    static {
        handlers = new HandlerList();
    }
}
