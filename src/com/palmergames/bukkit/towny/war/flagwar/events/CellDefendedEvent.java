// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar.events;

import com.palmergames.bukkit.towny.war.flagwar.Cell;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class CellDefendedEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    private Player player;
    private Cell cell;
    
    public HandlerList getHandlers() {
        return CellDefendedEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return CellDefendedEvent.handlers;
    }
    
    public CellDefendedEvent(final Player player, final Cell cell) {
        this.cancelled = false;
        this.player = player;
        this.cell = cell;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Cell getCell() {
        return this.cell;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }
    
    static {
        handlers = new HandlerList();
    }
}
