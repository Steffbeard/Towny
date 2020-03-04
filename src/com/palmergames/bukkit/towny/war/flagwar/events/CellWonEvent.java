// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar.events;

import com.palmergames.bukkit.towny.war.flagwar.CellUnderAttack;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class CellWonEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    public boolean cancelled;
    private CellUnderAttack cellAttackData;
    
    public HandlerList getHandlers() {
        return CellWonEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return CellWonEvent.handlers;
    }
    
    public CellWonEvent(final CellUnderAttack cellAttackData) {
        this.cancelled = false;
        this.cellAttackData = cellAttackData;
    }
    
    public CellUnderAttack getCellAttackData() {
        return this.cellAttackData;
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
