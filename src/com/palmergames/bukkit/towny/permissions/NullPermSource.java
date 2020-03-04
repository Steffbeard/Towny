// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.permissions;

import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.Towny;

public class NullPermSource extends TownyPermissionSource
{
    public NullPermSource(final Towny towny) {
        this.plugin = towny;
    }
    
    @Override
    public String getPrefixSuffix(final Resident resident, final String node) {
        return "";
    }
    
    @Override
    public int getGroupPermissionIntNode(final String playerName, final String node) {
        return -1;
    }
    
    @Override
    public int getPlayerPermissionIntNode(final String playerName, final String node) {
        return -1;
    }
    
    @Override
    public String getPlayerPermissionStringNode(final String playerName, final String node) {
        return "";
    }
    
    @Override
    public String getPlayerGroup(final Player player) {
        return "";
    }
}
