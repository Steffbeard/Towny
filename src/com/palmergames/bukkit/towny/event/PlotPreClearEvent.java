// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PlotPreClearEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean isCancelled;
    private String cancelMessage;
    private TownBlock townBlock;
    
    public HandlerList getHandlers() {
        return PlotPreClearEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlotPreClearEvent.handlers;
    }
    
    public PlotPreClearEvent(final TownBlock _townBlock) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.cancelMessage = "Sorry this event was cancelled";
        this.townBlock = _townBlock;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
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
    
    static {
        handlers = new HandlerList();
    }
}
