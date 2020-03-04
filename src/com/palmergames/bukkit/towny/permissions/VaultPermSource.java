// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.permissions;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.Towny;
import net.milkbowl.vault.chat.Chat;

public class VaultPermSource extends TownyPermissionSource
{
    private final Chat chat;
    
    public VaultPermSource(final Towny plugin, final Chat chat) {
        this.plugin = plugin;
        this.chat = chat;
    }
    
    @Override
    public String getPrefixSuffix(final Resident resident, final String node) {
        final Player player = BukkitTools.getPlayerExact(resident.getName());
        if (player != null) {
            final String primaryGroup = this.getPlayerGroup(player);
            String groupPrefixSuffix = "";
            String playerPrefixSuffix = "";
            if ("prefix".equalsIgnoreCase(node)) {
                if (!primaryGroup.isEmpty()) {
                    groupPrefixSuffix = this.chat.getGroupPrefix(player.getWorld(), primaryGroup);
                }
                playerPrefixSuffix = this.chat.getPlayerPrefix(player);
            }
            else if ("suffix".equalsIgnoreCase(node)) {
                if (!primaryGroup.isEmpty()) {
                    groupPrefixSuffix = this.chat.getGroupSuffix(player.getWorld(), primaryGroup);
                }
                playerPrefixSuffix = this.chat.getPlayerSuffix(player);
            }
            else if (node == "userprefix") {
                playerPrefixSuffix = this.chat.getPlayerPrefix(player);
            }
            else if (node == "usersuffix") {
                playerPrefixSuffix = this.chat.getPlayerSuffix(player);
            }
            else if (node == "groupprefix") {
                if (!primaryGroup.isEmpty()) {
                    groupPrefixSuffix = this.chat.getGroupPrefix(player.getWorld(), primaryGroup);
                }
                else {
                    groupPrefixSuffix = "";
                }
            }
            else if (node == "groupsuffix") {
                if (!primaryGroup.isEmpty()) {
                    groupPrefixSuffix = this.chat.getGroupSuffix(player.getWorld(), primaryGroup);
                }
                else {
                    groupPrefixSuffix = "";
                }
            }
            if (groupPrefixSuffix == null) {
                groupPrefixSuffix = "";
            }
            if (playerPrefixSuffix == null) {
                playerPrefixSuffix = "";
            }
            String prefixSuffix = playerPrefixSuffix;
            if (!playerPrefixSuffix.equals(groupPrefixSuffix)) {
                prefixSuffix = groupPrefixSuffix + playerPrefixSuffix;
            }
            return TownySettings.parseSingleLineString(prefixSuffix);
        }
        return "";
    }
    
    @Override
    public int getGroupPermissionIntNode(final String playerName, final String node) {
        int iReturn = -1;
        final Player player = BukkitTools.getPlayerExact(playerName);
        if (player != null) {
            final String primaryGroup = this.getPlayerGroup(player);
            if (!primaryGroup.isEmpty()) {
                iReturn = this.chat.getGroupInfoInteger(player.getWorld(), primaryGroup, node, -1);
            }
        }
        if (iReturn == -1) {
            iReturn = this.getEffectivePermIntNode(playerName, node);
        }
        return iReturn;
    }
    
    @Override
    public int getPlayerPermissionIntNode(final String playerName, final String node) {
        int iReturn = -1;
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(BukkitTools.getPlayerExact(playerName).getUniqueId());
        final Player player = BukkitTools.getPlayer(playerName);
        if (player != null) {
            iReturn = this.chat.getPlayerInfoInteger(player.getWorld().getName(), offlinePlayer, node, -1);
        }
        if (iReturn == -1) {
            iReturn = this.getEffectivePermIntNode(playerName, node);
        }
        return iReturn;
    }
    
    @Override
    public String getPlayerGroup(final Player player) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        final String result = this.chat.getPrimaryGroup(player.getWorld().getName(), offlinePlayer);
        return (result != null) ? result : "";
    }
    
    @Override
    public String getPlayerPermissionStringNode(final String playerName, final String node) {
        final Player player = BukkitTools.getPlayerExact(playerName);
        if (player != null) {
            return this.chat.getPlayerInfoString(player, node, "");
        }
        return "";
    }
}
