// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import java.util.List;

public interface TownBlockOwner extends Permissible
{
    void setTownblocks(final List<TownBlock> p0);
    
    List<TownBlock> getTownBlocks();
    
    boolean hasTownBlock(final TownBlock p0);
    
    void addTownBlock(final TownBlock p0) throws AlreadyRegisteredException;
    
    void removeTownBlock(final TownBlock p0) throws NotRegisteredException;
}
