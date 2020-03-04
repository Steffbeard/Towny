// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

public class TownyException extends Exception
{
    private static final long serialVersionUID = -6821768221748544277L;
    
    @Deprecated
    public String getError() {
        return this.getMessage();
    }
    
    public TownyException() {
        super("unknown");
    }
    
    public TownyException(final String message) {
        super(message);
    }
}
