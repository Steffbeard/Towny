// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.List;

public interface ResidentModes
{
    List<String> getModes();
    
    boolean hasMode(final String p0);
    
    void toggleMode(final String[] p0, final boolean p1);
    
    void setModes(final String[] p0, final boolean p1);
    
    void clearModes();
}
