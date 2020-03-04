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

public class PlayerEnterTownEvent extends Event
{
    private static final HandlerList handlers;
    private Town enteredtown;
    private PlayerMoveEvent pme;
    private WorldCoord from;
    private WorldCoord to;
    private Player player;
    
    public HandlerList getHandlers() {
        return PlayerEnterTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlayerEnterTownEvent.handlers;
    }
    
    public PlayerEnterTownEvent(final Player player, final WorldCoord to, final WorldCoord from, final Town enteredtown, final PlayerMoveEvent pme) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.enteredtown = enteredtown;
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
    
    public Town getEnteredtown() {
        return this.enteredtown;
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
