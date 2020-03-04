// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar.events;

import com.palmergames.bukkit.towny.war.flagwar.CellUnderAttack;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class CellAttackCanceledEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    private CellUnderAttack cell;
    
    public HandlerList getHandlers() {
        return CellAttackCanceledEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return CellAttackCanceledEvent.handlers;
    }
    
    public CellAttackCanceledEvent(final CellUnderAttack cell) {
        this.cancelled = false;
        this.cell = cell;
    }
    
    public CellUnderAttack getCell() {
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
