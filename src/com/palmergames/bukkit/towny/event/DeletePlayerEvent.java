// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class DeletePlayerEvent extends Event
{
    private static final HandlerList handlers;
    private String playerName;
    
    public HandlerList getHandlers() {
        return DeletePlayerEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return DeletePlayerEvent.handlers;
    }
    
    public DeletePlayerEvent(final String player) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.playerName = player;
    }
    
    public String getPlayerName() {
        return this.playerName;
    }
    
    static {
        handlers = new HandlerList();
    }
}
