// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PreNewTownEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private Player player;
    private String townName;
    private boolean isCancelled;
    private String cancelMessage;
    
    public PreNewTownEvent(final Player player, final String townName) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.cancelMessage = "Sorry this event was cancelled";
        this.player = player;
        this.townName = townName;
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return PreNewTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PreNewTownEvent.handlers;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getCancelMessage() {
        return this.cancelMessage;
    }
    
    public void setCancelMessage(final String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
    
    public String getTownName() {
        return this.townName;
    }
    
    static {
        handlers = new HandlerList();
    }
}
