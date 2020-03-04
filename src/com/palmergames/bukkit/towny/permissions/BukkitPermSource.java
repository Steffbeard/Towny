// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.permissions;

import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.Towny;

public class BukkitPermSource extends TownyPermissionSource
{
    public BukkitPermSource(final Towny towny) {
        this.plugin = towny;
    }
    
    @Override
    public String getPrefixSuffix(final Resident resident, final String node) {
        final Player player = BukkitTools.getPlayer(resident.getName());
        for (final PermissionAttachmentInfo test : player.getEffectivePermissions()) {
            if (test.getPermission().startsWith(node + ".")) {
                final String[] split = test.getPermission().split("\\.");
                return split[split.length - 1];
            }
        }
        return "";
    }
    
    @Override
    public int getGroupPermissionIntNode(final String playerName, final String node) {
        return this.getEffectivePermIntNode(playerName, node);
    }
    
    @Override
    public int getPlayerPermissionIntNode(final String playerName, final String node) {
        return this.getEffectivePermIntNode(playerName, node);
    }
    
    @Override
    public String getPlayerPermissionStringNode(final String playerName, final String node) {
        final Player player = BukkitTools.getPlayer(playerName);
        for (final PermissionAttachmentInfo test : player.getEffectivePermissions()) {
            if (test.getPermission().startsWith(node + ".")) {
                final String[] split = test.getPermission().split("\\.");
                return split[split.length - 1];
            }
        }
        return "";
    }
    
    @Override
    public String getPlayerGroup(final Player player) {
        return "";
    }
}
