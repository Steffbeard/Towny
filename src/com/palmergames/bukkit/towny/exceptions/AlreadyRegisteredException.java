// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

public class AlreadyRegisteredException extends TownyException
{
    private static final long serialVersionUID = 4191685552690886161L;
    
    public AlreadyRegisteredException() {
        super("Already registered.");
    }
    
    public AlreadyRegisteredException(final String message) {
        super(message);
    }
}
