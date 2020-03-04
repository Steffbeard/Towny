// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

import com.palmergames.bukkit.towny.object.Nation;

public class EmptyNationException extends Exception
{
    private static final long serialVersionUID = 6093696939107516795L;
    private Nation nation;
    
    public EmptyNationException(final Nation nation) {
        this.setNation(nation);
    }
    
    public void setNation(final Nation nation) {
        this.nation = nation;
    }
    
    public Nation getNation() {
        return this.nation;
    }
}
