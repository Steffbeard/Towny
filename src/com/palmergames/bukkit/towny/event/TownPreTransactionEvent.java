// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Transaction;
import org.bukkit.event.HandlerList;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TownPreTransactionEvent extends Event implements Cancellable
{
    private Town town;
    private static final HandlerList handlers;
    private Transaction transaction;
    private String cancelMessage;
    private boolean isCancelled;
    
    public TownPreTransactionEvent(final Town town, final Transaction transaction) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.cancelMessage = "Sorry this event was cancelled.";
        this.isCancelled = false;
        this.town = town;
        this.transaction = transaction;
    }
    
    public HandlerList getHandlers() {
        return TownPreTransactionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownPreTransactionEvent.handlers;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public Transaction getTransaction() {
        return this.transaction;
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
    
    public void setCancelMessage(final String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
    
    public int getNewBalance() {
        try {
            switch (this.transaction.getType()) {
                case DEPOSIT: {
                    return (int)(this.town.getAccount().getHoldingBalance() + this.transaction.getAmount());
                }
                case WITHDRAW: {
                    return (int)(this.town.getAccount().getHoldingBalance() - this.transaction.getAmount());
                }
            }
        }
        catch (EconomyException e) {
            BukkitTools.getServer().getLogger().warning(e.getMessage());
        }
        return 0;
    }
    
    static {
        handlers = new HandlerList();
    }
}
