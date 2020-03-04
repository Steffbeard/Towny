// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

import com.palmergames.bukkit.towny.object.Town;

public class EmptyTownException extends Exception
{
    private static final long serialVersionUID = 5058583908170407803L;
    private EmptyNationException emptyNationException;
    private Town town;
    
    public EmptyTownException(final Town town) {
        this.setTown(town);
    }
    
    public EmptyTownException(final Town town, final EmptyNationException emptyNationException) {
        this.setTown(town);
        this.setEmptyNationException(emptyNationException);
    }
    
    public boolean hasEmptyNationException() {
        return this.emptyNationException != null;
    }
    
    public EmptyNationException getEmptyNationException() {
        return this.emptyNationException;
    }
    
    public void setEmptyNationException(final EmptyNationException emptyNationException) {
        this.emptyNationException = emptyNationException;
    }
    
    public void setTown(final Town town) {
        this.town = town;
    }
    
    public Town getTown() {
        return this.town;
    }
}
