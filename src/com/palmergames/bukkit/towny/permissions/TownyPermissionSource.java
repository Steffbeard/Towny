// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.permissions;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Material;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.util.Iterator;
import org.bukkit.permissions.PermissionAttachmentInfo;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.Resident;
import org.anjocaido.groupmanager.GroupManager;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownySettings;

public abstract class TownyPermissionSource
{
    protected TownySettings settings;
    protected Towny plugin;
    protected GroupManager groupManager;
    
    public TownyPermissionSource() {
        this.groupManager = null;
    }
    
    public abstract String getPrefixSuffix(final Resident p0, final String p1);
    
    public abstract int getGroupPermissionIntNode(final String p0, final String p1);
    
    public abstract int getPlayerPermissionIntNode(final String p0, final String p1);
    
    public abstract String getPlayerGroup(final Player p0);
    
    public abstract String getPlayerPermissionStringNode(final String p0, final String p1);
    
    protected int getEffectivePermIntNode(final String playerName, final String node) {
        final Player player = BukkitTools.getPlayer(playerName);
        for (final PermissionAttachmentInfo test : player.getEffectivePermissions()) {
            if (test.getPermission().startsWith(node + ".")) {
                final String[] split = test.getPermission().split("\\.");
                try {
                    return Integer.parseInt(split[split.length - 1]);
                }
                catch (NumberFormatException ex) {}
            }
        }
        return -1;
    }
    
    public boolean hasWildOverride(final TownyWorld world, final Player player, final Material material, final TownyPermission.ActionType action) {
        final String blockPerm = PermissionNodes.TOWNY_WILD_ALL.getNode(action.toString().toLowerCase() + "." + material);
        final boolean hasBlock = this.has(player, blockPerm);
        if (hasBlock) {
            return true;
        }
        switch (action) {
            case BUILD: {
                return world.getUnclaimedZoneBuild();
            }
            case DESTROY: {
                return world.getUnclaimedZoneDestroy();
            }
            case SWITCH: {
                return world.getUnclaimedZoneSwitch();
            }
            case ITEM_USE: {
                return world.getUnclaimedZoneItemUse();
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean unclaimedZoneAction(final TownyWorld world, final Material material, final TownyPermission.ActionType action) {
        switch (action) {
            case BUILD: {
                return world.getUnclaimedZoneBuild() || world.isUnclaimedZoneIgnoreMaterial(material);
            }
            case DESTROY: {
                return world.getUnclaimedZoneDestroy() || world.isUnclaimedZoneIgnoreMaterial(material);
            }
            case SWITCH: {
                return world.getUnclaimedZoneSwitch() || world.isUnclaimedZoneIgnoreMaterial(material);
            }
            case ITEM_USE: {
                return world.getUnclaimedZoneItemUse() || world.isUnclaimedZoneIgnoreMaterial(material);
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean hasOwnTownOverride(final Player player, final Material material, final TownyPermission.ActionType action) {
        final String blockPerm = PermissionNodes.TOWNY_CLAIMED_ALL.getNode("owntown." + action.toString().toLowerCase() + "." + material);
        final boolean hasBlock = this.has(player, blockPerm);
        TownyMessaging.sendDebugMsg(player.getName() + " - owntown (Block: " + material);
        return hasBlock || this.hasAllTownOverride(player, material, action);
    }
    
    public boolean hasTownOwnedOverride(final Player player, final Material material, final TownyPermission.ActionType action) {
        final String blockPerm = PermissionNodes.TOWNY_CLAIMED_ALL.getNode("townowned." + action.toString().toLowerCase() + "." + material);
        final boolean hasBlock = this.has(player, blockPerm);
        TownyMessaging.sendDebugMsg(player.getName() + " - townowned (Block: " + hasBlock);
        return hasBlock || this.hasOwnTownOverride(player, material, action) || this.hasAllTownOverride(player, material, action);
    }
    
    public boolean hasAllTownOverride(final Player player, final Material material, final TownyPermission.ActionType action) {
        final String blockPerm = PermissionNodes.TOWNY_CLAIMED_ALL.getNode("alltown." + action.toString().toLowerCase() + "." + material);
        final boolean hasBlock = this.has(player, blockPerm);
        TownyMessaging.sendDebugMsg(player.getName() + " - alltown (Block: " + hasBlock);
        return hasBlock;
    }
    
    public boolean isTownyAdmin(final Player player) {
        return player == null || player.isOp() || this.has(player, PermissionNodes.TOWNY_ADMIN.getNode());
    }
    
    public boolean testPermission(final Player player, final String perm) {
        return TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(player) || this.has(player, perm);
    }
    
    public boolean has(final Player player, final String node) {
        if (player.isOp()) {
            return true;
        }
        if (player.isPermissionSet(node)) {
            return player.hasPermission(node);
        }
        final String[] parts = node.split("\\.");
        final StringBuilder builder = new StringBuilder(node.length());
        for (final String part : parts) {
            builder.append('*');
            if (player.hasPermission("-" + builder.toString())) {
                return false;
            }
            if (player.hasPermission(builder.toString())) {
                return true;
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(part).append('.');
        }
        return false;
    }
}
