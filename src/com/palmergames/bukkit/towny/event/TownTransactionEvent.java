// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Transaction;
import org.bukkit.event.HandlerList;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.Event;

public class TownTransactionEvent extends Event
{
    private Town town;
    private static final HandlerList handlers;
    private Transaction transaction;
    
    public TownTransactionEvent(final Town town, final Transaction transaction) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
        this.transaction = transaction;
    }
    
    public HandlerList getHandlers() {
        return TownTransactionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownTransactionEvent.handlers;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public Transaction getTransaction() {
        return this.transaction;
    }
    
    static {
        handlers = new HandlerList();
    }
}
