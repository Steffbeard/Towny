// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

public enum TransactionType
{
    DEPOSIT("Deposit"), 
    WITHDRAW("Withdraw"), 
    ADD("Add"), 
    SUBTRACT("Subtract");
    
    private String name;
    
    private TransactionType(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
