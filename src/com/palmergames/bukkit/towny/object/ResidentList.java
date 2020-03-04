// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.List;

public interface ResidentList
{
    List<Resident> getResidents();
    
    boolean hasResident(final String p0);
    
    List<Resident> getOutlaws();
}
