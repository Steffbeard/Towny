// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Transaction;
import org.bukkit.event.HandlerList;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.Event;

public class NationTransactionEvent extends Event
{
    private Nation nation;
    private static final HandlerList handlers;
    private Transaction transaction;
    
    public NationTransactionEvent(final Nation nation, final Transaction transaction) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.nation = nation;
        this.transaction = transaction;
    }
    
    public HandlerList getHandlers() {
        return NationTransactionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationTransactionEvent.handlers;
    }
    
    public Nation getNation() {
        return this.nation;
    }
    
    public Transaction getTransaction() {
        return this.transaction;
    }
    
    static {
        handlers = new HandlerList();
    }
}
