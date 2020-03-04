// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

public class KeyAlreadyRegisteredException extends TownyException
{
    private static final long serialVersionUID = 1435945343723569023L;
    
    public KeyAlreadyRegisteredException() {
        super("Meta Data can't be added because key with same name already exists.");
    }
    
    public KeyAlreadyRegisteredException(final String message) {
        super(message);
    }
}
