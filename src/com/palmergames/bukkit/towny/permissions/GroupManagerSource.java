// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.permissions;

import org.anjocaido.groupmanager.events.GMSystemEvent;
import java.util.Iterator;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.events.GMGroupEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.event.Listener;
import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.Towny;

public class GroupManagerSource extends TownyPermissionSource
{
    public GroupManagerSource(final Towny towny, final Plugin test) {
        this.groupManager = (GroupManager)test;
        this.plugin = towny;
        try {
            this.plugin.getServer().getPluginManager().registerEvents((Listener)new GMCustomEventListener(), (Plugin)this.plugin);
        }
        catch (IllegalPluginAccessException e) {
            System.out.print("Your Version of GroupManager is out of date. Please update.");
        }
    }
    
    @Override
    public String getPrefixSuffix(final Resident resident, final String node) {
        String group = "";
        String user = "";
        final Player player = BukkitTools.getPlayer(resident.getName());
        final AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldData(player).getPermissionsHandler();
        if (node == "prefix") {
            group = handler.getGroupPrefix(handler.getPrimaryGroup(player.getName()));
            user = handler.getUserPrefix(player.getName());
        }
        else if (node == "suffix") {
            group = handler.getGroupSuffix(handler.getPrimaryGroup(player.getName()));
            user = handler.getUserSuffix(player.getName());
        }
        else if (node == "userprefix") {
            group = "";
            user = handler.getUserSuffix(player.getName());
        }
        else if (node == "usersuffix") {
            group = "";
            user = handler.getUserSuffix(player.getName());
        }
        else if (node == "groupprefix") {
            group = handler.getGroupPrefix(handler.getPrimaryGroup(player.getName()));
            user = "";
        }
        else if (node == "groupsuffix") {
            group = handler.getGroupSuffix(handler.getPrimaryGroup(player.getName()));
            user = "";
        }
        if (group == null) {
            group = "";
        }
        if (user == null) {
            user = "";
        }
        if (!group.equals(user)) {
            user = group + user;
        }
        user = TownySettings.parseSingleLineString(user);
        return user;
    }
    
    @Override
    public int getGroupPermissionIntNode(final String playerName, final String node) {
        int iReturn = -1;
        final Player player = BukkitTools.getPlayer(playerName);
        final AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldData(player).getPermissionsHandler();
        iReturn = handler.getPermissionInteger(playerName, node);
        if (iReturn == -1) {
            iReturn = this.getEffectivePermIntNode(playerName, node);
        }
        return iReturn;
    }
    
    @Override
    public int getPlayerPermissionIntNode(final String playerName, final String node) {
        int iReturn = -1;
        final Player player = BukkitTools.getPlayer(playerName);
        final AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldData(player).getPermissionsHandler();
        iReturn = handler.getPermissionInteger(playerName, node);
        if (iReturn == -1) {
            iReturn = this.getEffectivePermIntNode(playerName, node);
        }
        return iReturn;
    }
    
    @Override
    public String getPlayerPermissionStringNode(final String playerName, final String node) {
        final Player player = BukkitTools.getPlayer(playerName);
        final AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldData(player).getPermissionsHandler();
        return handler.getPermissionString(playerName, node);
    }
    
    @Override
    public String getPlayerGroup(final Player player) {
        final AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldData(player).getPermissionsHandler();
        return handler.getGroup(player.getName());
    }
    
    protected class GMCustomEventListener implements Listener
    {
        public GMCustomEventListener() {
        }
        
        @EventHandler(priority = EventPriority.HIGH)
        public void onGMUserEvent(final GMUserEvent event) {
            Resident resident = null;
            Player player = null;
            try {
                if (PermissionEventEnums.GMUser_Action.valueOf(event.getAction().name()) != null) {
                    try {
                        resident = TownyUniverse.getInstance().getDataSource().getResident(event.getUserName());
                        player = BukkitTools.getPlayerExact(resident.getName());
                        if (player != null) {
                            final String[] modes = GroupManagerSource.this.getPlayerPermissionStringNode(player.getName(), PermissionNodes.TOWNY_DEFAULT_MODES.getNode()).split(",");
                            GroupManagerSource.this.plugin.setPlayerMode(player, modes, false);
                            GroupManagerSource.this.plugin.resetCache(player);
                        }
                    }
                    catch (NotRegisteredException ex) {}
                }
            }
            catch (IllegalArgumentException ex2) {}
        }
        
        @EventHandler(priority = EventPriority.HIGH)
        public void onGMGroupEvent(final GMGroupEvent event) {
            try {
                if (PermissionEventEnums.GMGroup_Action.valueOf(event.getAction().name()) != null) {
                    final Group group = event.getGroup();
                    for (final Player toUpdate : BukkitTools.getOnlinePlayers()) {
                        if (toUpdate != null && group.equals((Object)GroupManagerSource.this.getPlayerGroup(toUpdate))) {
                            final String[] modes = GroupManagerSource.this.getPlayerPermissionStringNode(toUpdate.getName(), PermissionNodes.TOWNY_DEFAULT_MODES.getNode()).split(",");
                            GroupManagerSource.this.plugin.setPlayerMode(toUpdate, modes, false);
                            GroupManagerSource.this.plugin.resetCache(toUpdate);
                        }
                    }
                }
            }
            catch (IllegalArgumentException ex) {}
        }
        
        @EventHandler(priority = EventPriority.HIGH)
        public void onGMSystemEvent(final GMSystemEvent event) {
            try {
                if (PermissionEventEnums.GMSystem_Action.valueOf(event.getAction().name()) != null) {
                    for (final Player toUpdate : BukkitTools.getOnlinePlayers()) {
                        if (toUpdate != null) {
                            final String[] modes = GroupManagerSource.this.getPlayerPermissionStringNode(toUpdate.getName(), PermissionNodes.TOWNY_DEFAULT_MODES.getNode()).split(",");
                            GroupManagerSource.this.plugin.setPlayerMode(toUpdate, modes, false);
                            GroupManagerSource.this.plugin.resetCache(toUpdate);
                        }
                    }
                }
            }
            catch (IllegalArgumentException ex) {}
        }
    }
}
