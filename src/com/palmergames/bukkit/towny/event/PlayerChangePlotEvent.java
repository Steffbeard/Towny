// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerMoveEvent;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlayerChangePlotEvent extends Event
{
    private static final HandlerList handlers;
    private Player player;
    private WorldCoord from;
    private WorldCoord to;
    private PlayerMoveEvent moveEvent;
    
    public HandlerList getHandlers() {
        return PlayerChangePlotEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlayerChangePlotEvent.handlers;
    }
    
    public PlayerChangePlotEvent(final Player player, final WorldCoord from, final WorldCoord to, final PlayerMoveEvent moveEvent) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.player = player;
        this.from = from;
        this.to = to;
        this.moveEvent = moveEvent;
    }
    
    public WorldCoord getFrom() {
        return this.from;
    }
    
    public PlayerMoveEvent getMoveEvent() {
        return this.moveEvent;
    }
    
    public WorldCoord getTo() {
        return this.to;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    static {
        handlers = new HandlerList();
    }
}
