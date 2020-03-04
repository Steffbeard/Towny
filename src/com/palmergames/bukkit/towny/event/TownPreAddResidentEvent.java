// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TownPreAddResidentEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private String townName;
    private Town town;
    private Resident resident;
    private boolean isCancelled;
    private String cancelMessage;
    
    public TownPreAddResidentEvent(final Town town, final Resident resident) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.cancelMessage = "Sorry this event was cancelled";
        this.town = town;
        this.townName = town.getName();
        this.resident = resident;
    }
    
    public static HandlerList getHandlerList() {
        return TownPreAddResidentEvent.handlers;
    }
    
    public HandlerList getHandlers() {
        return TownPreAddResidentEvent.handlers;
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
    
    public Resident getResident() {
        return this.resident;
    }
    
    static {
        handlers = new HandlerList();
    }
}
