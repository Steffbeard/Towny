// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PreDeleteTownEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private String townName;
    private Town town;
    private boolean isCancelled;
    private String cancelMessage;
    
    public HandlerList getHandlers() {
        return PreDeleteTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PreDeleteTownEvent.handlers;
    }
    
    public PreDeleteTownEvent(final Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.cancelMessage = "Sorry this event was cancelled";
        this.townName = town.getName();
        this.town = town;
    }
    
    public String getTownName() {
        return this.townName;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    public String getCancelMessage() {
        return this.cancelMessage;
    }
    
    public void setCancelMessage(final String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
    
    static {
        handlers = new HandlerList();
    }
}
