// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import com.palmergames.bukkit.towny.object.Transaction;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TownyPreTransactionEvent extends Event implements Cancellable
{
    private Transaction transaction;
    private static final HandlerList handlers;
    private boolean isCancelled;
    private String cancelMessage;
    
    public TownyPreTransactionEvent(final Transaction transaction) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.cancelMessage = "Sorry this event was cancelled.";
        this.transaction = transaction;
    }
    
    public HandlerList getHandlers() {
        return TownyPreTransactionEvent.handlers;
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    public String getCancelMessage() {
        return this.cancelMessage;
    }
    
    public static HandlerList getHandlerList() {
        return TownyPreTransactionEvent.handlers;
    }
    
    public Transaction getTransaction() {
        return this.transaction;
    }
    
    public int getNewBalance() {
        switch (this.transaction.getType()) {
            case ADD: {
                return (int)(TownyEconomyHandler.getBalance(this.transaction.getPlayer().getName(), this.transaction.getPlayer().getWorld()) + this.transaction.getAmount());
            }
            case SUBTRACT: {
                return (int)(TownyEconomyHandler.getBalance(this.transaction.getPlayer().getName(), this.transaction.getPlayer().getWorld()) - this.transaction.getAmount());
            }
            default: {
                return 0;
            }
        }
    }
    
    static {
        handlers = new HandlerList();
    }
}
