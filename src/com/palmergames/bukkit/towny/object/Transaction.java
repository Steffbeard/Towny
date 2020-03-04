// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import org.bukkit.entity.Player;

public class Transaction
{
    private TransactionType type;
    private Player player;
    private int amount;
    
    public Transaction(final TransactionType type, final Player player, final int amount) {
        this.type = type;
        this.player = player;
        this.amount = amount;
    }
    
    public TransactionType getType() {
        return this.type;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public int getAmount() {
        return this.amount;
    }
}
