// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Transaction;
import org.bukkit.event.HandlerList;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class NationPreTransactionEvent extends Event implements Cancellable
{
    private Nation nation;
    private static final HandlerList handlers;
    private Transaction transaction;
    private String cancelMessage;
    private boolean isCancelled;
    
    public NationPreTransactionEvent(final Nation nation, final Transaction transaction) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.cancelMessage = "Sorry this event was cancelled.";
        this.isCancelled = false;
        this.nation = nation;
        this.transaction = transaction;
    }
    
    public HandlerList getHandlers() {
        return NationPreTransactionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationPreTransactionEvent.handlers;
    }
    
    public Nation getNation() {
        return this.nation;
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
                    return (int)(this.nation.getAccount().getHoldingBalance() + this.transaction.getAmount());
                }
                case WITHDRAW: {
                    return (int)(this.nation.getAccount().getHoldingBalance() - this.transaction.getAmount());
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
