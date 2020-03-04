// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

public class EconomyException extends Exception
{
    private static final long serialVersionUID = 5273714478509976170L;
    public String error;
    
    public EconomyException() {
        this.error = "unknown";
    }
    
    public EconomyException(final String error) {
        super(error);
        this.error = error;
    }
    
    public String getError() {
        return this.error;
    }
}
