// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.event.player.PlayerMoveEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlayerLeaveTownEvent extends Event
{
    private static final HandlerList handlers;
    private Town lefttown;
    private PlayerMoveEvent pme;
    private WorldCoord from;
    private Player player;
    private WorldCoord to;
    
    public HandlerList getHandlers() {
        return PlayerLeaveTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlayerLeaveTownEvent.handlers;
    }
    
    public PlayerLeaveTownEvent(final Player player, final WorldCoord to, final WorldCoord from, final Town lefttown, final PlayerMoveEvent pme) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.lefttown = lefttown;
        this.player = player;
        this.from = from;
        this.pme = pme;
        this.to = to;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public PlayerMoveEvent getPlayerMoveEvent() {
        return this.pme;
    }
    
    public Town getLefttown() {
        return this.lefttown;
    }
    
    public WorldCoord getFrom() {
        return this.from;
    }
    
    public WorldCoord getTo() {
        return this.to;
    }
    
    static {
        handlers = new HandlerList();
    }
}
