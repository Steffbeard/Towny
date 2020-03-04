// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.invites.exceptions;

public class TooManyInvitesException extends Exception
{
    @Deprecated
    public String getError() {
        return this.getMessage();
    }
    
    public TooManyInvitesException() {
        super("unknown");
    }
    
    public TooManyInvitesException(final String message) {
        super(message);
    }
}
