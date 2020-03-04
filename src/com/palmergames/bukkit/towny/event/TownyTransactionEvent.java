// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import com.palmergames.bukkit.towny.object.Transaction;
import org.bukkit.event.Event;

public class TownyTransactionEvent extends Event
{
    private Transaction transaction;
    private static final HandlerList handlers;
    
    public TownyTransactionEvent(final Transaction transaction) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.transaction = transaction;
    }
    
    public HandlerList getHandlers() {
        return TownyTransactionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownyTransactionEvent.handlers;
    }
    
    public Transaction getTransaction() {
        return this.transaction;
    }
    
    static {
        handlers = new HandlerList();
    }
}
