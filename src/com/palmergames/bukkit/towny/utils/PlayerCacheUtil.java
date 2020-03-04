// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;

public class PlayerCacheUtil
{
    static Towny plugin;
    
    public static void initialize(final Towny plugin) {
        PlayerCacheUtil.plugin = plugin;
    }
    
    public static boolean getCachePermission(final Player player, final Location location, final Material material, final TownyPermission.ActionType action) {
        try {
            WorldCoord worldCoord;
            if (location.getWorld().equals(player.getWorld())) {
                worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord(location));
            }
            else {
                worldCoord = new WorldCoord(location.getWorld().getName(), Coord.parseCoord(location));
            }
            final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
            cache.updateCoord(worldCoord);
            TownyMessaging.sendDebugMsg("Cache permissions for " + action.toString() + " : " + cache.getCachePermission(material, action));
            return cache.getCachePermission(material, action);
        }
        catch (NullPointerException e) {
            WorldCoord worldCoord;
            if (location.getWorld().equals(player.getWorld())) {
                worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord(location));
            }
            else {
                worldCoord = new WorldCoord(location.getWorld().getName(), Coord.parseCoord(location));
            }
            final PlayerCache.TownBlockStatus status = cacheStatus(player, worldCoord, getTownBlockStatus(player, worldCoord));
            triggerCacheCreate(player, location, worldCoord, status, material, action);
            final PlayerCache cache2 = PlayerCacheUtil.plugin.getCache(player);
            cache2.updateCoord(worldCoord);
            TownyMessaging.sendDebugMsg("New Cache Created and updated!");
            TownyMessaging.sendDebugMsg("New Cache permissions for " + material + ":" + action.toString() + ":" + status.name() + " = " + cache2.getCachePermission(material, action));
            return cache2.getCachePermission(material, action);
        }
    }
    
    private static void triggerCacheCreate(final Player player, final Location location, final WorldCoord worldCoord, final PlayerCache.TownBlockStatus status, final Material material, final TownyPermission.ActionType action) {
        switch (action) {
            case BUILD: {
                cacheBuild(player, worldCoord, material, getPermission(player, status, worldCoord, material, action));
            }
            case DESTROY: {
                cacheDestroy(player, worldCoord, material, getPermission(player, status, worldCoord, material, action));
            }
            case SWITCH: {
                cacheSwitch(player, worldCoord, material, getPermission(player, status, worldCoord, material, action));
            }
            case ITEM_USE: {
                cacheItemUse(player, worldCoord, material, getPermission(player, status, worldCoord, material, action));
            }
            default: {}
        }
    }
    
    public static PlayerCache.TownBlockStatus cacheStatus(final Player player, final WorldCoord worldCoord, final PlayerCache.TownBlockStatus townBlockStatus) {
        final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
        cache.updateCoord(worldCoord);
        cache.setStatus(townBlockStatus);
        TownyMessaging.sendDebugMsg(player.getName() + " (" + worldCoord.toString() + ") Cached Status: " + townBlockStatus);
        return townBlockStatus;
    }
    
    private static void cacheBuild(final Player player, final WorldCoord worldCoord, final Material material, final Boolean buildRight) {
        final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
        cache.updateCoord(worldCoord);
        cache.setBuildPermission(material, buildRight);
        TownyMessaging.sendDebugMsg(player.getName() + " (" + worldCoord.toString() + ") Cached Build: " + buildRight);
    }
    
    private static void cacheDestroy(final Player player, final WorldCoord worldCoord, final Material material, final Boolean destroyRight) {
        final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
        cache.updateCoord(worldCoord);
        cache.setDestroyPermission(material, destroyRight);
        TownyMessaging.sendDebugMsg(player.getName() + " (" + worldCoord.toString() + ") Cached Destroy: " + destroyRight);
    }
    
    private static void cacheSwitch(final Player player, final WorldCoord worldCoord, final Material material, final Boolean switchRight) {
        final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
        cache.updateCoord(worldCoord);
        cache.setSwitchPermission(material, switchRight);
        TownyMessaging.sendDebugMsg(player.getName() + " (" + worldCoord.toString() + ") Cached Switch: " + switchRight);
    }
    
    private static void cacheItemUse(final Player player, final WorldCoord worldCoord, final Material material, final Boolean itemUseRight) {
        final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
        cache.updateCoord(worldCoord);
        cache.setItemUsePermission(material, itemUseRight);
        TownyMessaging.sendDebugMsg(player.getName() + " (" + worldCoord.toString() + ") Cached Item Use: " + itemUseRight);
    }
    
    public static void cacheBlockErrMsg(final Player player, final String msg) {
        final PlayerCache cache = PlayerCacheUtil.plugin.getCache(player);
        cache.setBlockErrMsg(msg);
    }
    
    public static PlayerCache.TownBlockStatus getTownBlockStatus(final Player player, final WorldCoord worldCoord) {
        try {
            if (!worldCoord.getTownyWorld().isUsingTowny()) {
                return PlayerCache.TownBlockStatus.OFF_WORLD;
            }
        }
        catch (NotRegisteredException ex) {
            return PlayerCache.TownBlockStatus.NOT_REGISTERED;
        }
        TownBlock townBlock;
        Town town;
        try {
            townBlock = worldCoord.getTownBlock();
            town = townBlock.getTown();
            if (townBlock.isLocked()) {
                if (town.getWorld().isUsingPlotManagementRevert() && TownySettings.getPlotManagementSpeed() > 0L) {
                    TownyRegenAPI.addWorldCoord(townBlock.getWorldCoord());
                    return PlayerCache.TownBlockStatus.LOCKED;
                }
                townBlock.setLocked(false);
            }
        }
        catch (NotRegisteredException e) {
            if (TownySettings.getNationZonesEnabled() && (!TownySettings.getNationZonesWarDisables() || !TownyAPI.getInstance().isWarTime())) {
                Town nearestTown = null;
                int distance;
                try {
                    nearestTown = worldCoord.getTownyWorld().getClosestTownFromCoord(worldCoord.getCoord(), nearestTown);
                    if (nearestTown == null) {
                        return PlayerCache.TownBlockStatus.UNCLAIMED_ZONE;
                    }
                    if (TownySettings.getWarSiegeEnabled() && TownySettings.getNationZonesWarDisables() && nearestTown.hasSiege() && nearestTown.getSiege().getStatus() == SiegeStatus.IN_PROGRESS) {
                        return PlayerCache.TownBlockStatus.UNCLAIMED_ZONE;
                    }
                    if (!nearestTown.hasNation()) {
                        return PlayerCache.TownBlockStatus.UNCLAIMED_ZONE;
                    }
                    distance = worldCoord.getTownyWorld().getMinDistanceFromOtherTownsPlots(worldCoord.getCoord());
                }
                catch (NotRegisteredException e2) {
                    return PlayerCache.TownBlockStatus.UNCLAIMED_ZONE;
                }
                if (!nearestTown.isCapital() && TownySettings.getNationZonesCapitalsOnly()) {
                    return PlayerCache.TownBlockStatus.UNCLAIMED_ZONE;
                }
                try {
                    int nationZoneRadius;
                    if (nearestTown.isCapital()) {
                        nationZoneRadius = Integer.parseInt(TownySettings.getNationLevel(nearestTown.getNation()).get(TownySettings.NationLevel.NATIONZONES_SIZE).toString()) + TownySettings.getNationZonesCapitalBonusSize();
                    }
                    else {
                        nationZoneRadius = Integer.parseInt(TownySettings.getNationLevel(nearestTown.getNation()).get(TownySettings.NationLevel.NATIONZONES_SIZE).toString());
                    }
                    if (distance <= nationZoneRadius) {
                        return PlayerCache.TownBlockStatus.NATION_ZONE;
                    }
                }
                catch (NumberFormatException ex2) {}
                catch (NotRegisteredException ex3) {}
            }
            return PlayerCache.TownBlockStatus.UNCLAIMED_ZONE;
        }
        Resident resident;
        try {
            resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
        }
        catch (TownyException e3) {
            System.out.print("Failed to fetch resident: " + player.getName());
            return PlayerCache.TownBlockStatus.NOT_REGISTERED;
        }
        try {
            if (TownyAPI.getInstance().isWarTime()) {
                if (TownySettings.isAllowWarBlockGriefing()) {
                    try {
                        if (!resident.getTown().getNation().isNeutral() && !town.getNation().isNeutral() && worldCoord.getTownyWorld().isWarAllowed()) {
                            return PlayerCache.TownBlockStatus.WARZONE;
                        }
                    }
                    catch (NotRegisteredException ex4) {}
                }
                if (!TownySettings.isWarTimeTownsNeutral() && !town.hasNation() && worldCoord.getTownyWorld().isWarAllowed()) {
                    return PlayerCache.TownBlockStatus.WARZONE;
                }
            }
            try {
                if (townBlock.getTown().isMayor(resident)) {
                    return PlayerCache.TownBlockStatus.TOWN_OWNER;
                }
            }
            catch (NotRegisteredException ex5) {}
            try {
                final Resident owner = townBlock.getResident();
                if (resident == owner) {
                    return PlayerCache.TownBlockStatus.PLOT_OWNER;
                }
                if (owner.hasFriend(resident)) {
                    return PlayerCache.TownBlockStatus.PLOT_FRIEND;
                }
                if (resident.hasTown() && CombatUtil.isSameTown(owner.getTown(), resident.getTown())) {
                    return PlayerCache.TownBlockStatus.PLOT_TOWN;
                }
                if (resident.hasTown() && CombatUtil.isAlly(owner.getTown(), resident.getTown())) {
                    return PlayerCache.TownBlockStatus.PLOT_ALLY;
                }
                throw new TownyException();
            }
            catch (TownyException ex6) {
                if (!resident.hasTown()) {
                    if (TownyAPI.getInstance().isWarTime() && townBlock.isWarZone() && !TownySettings.isWarTimeTownsNeutral()) {
                        return PlayerCache.TownBlockStatus.WARZONE;
                    }
                    return PlayerCache.TownBlockStatus.OUTSIDER;
                }
                else if (resident.getTown() != town) {
                    if (CombatUtil.isSameNation(town, resident.getTown())) {
                        return PlayerCache.TownBlockStatus.TOWN_NATION;
                    }
                    if (CombatUtil.isAlly(town, resident.getTown())) {
                        return PlayerCache.TownBlockStatus.TOWN_ALLY;
                    }
                    if (!CombatUtil.isEnemy(resident.getTown(), town)) {
                        return PlayerCache.TownBlockStatus.OUTSIDER;
                    }
                    if ((TownyAPI.getInstance().isWarTime() && townBlock.isWarZone()) || War.isWarZone(townBlock.getWorldCoord())) {
                        return PlayerCache.TownBlockStatus.WARZONE;
                    }
                    return PlayerCache.TownBlockStatus.ENEMY;
                }
                else {
                    if (resident.isMayor()) {
                        return PlayerCache.TownBlockStatus.TOWN_OWNER;
                    }
                    return PlayerCache.TownBlockStatus.TOWN_RESIDENT;
                }
            }
        }
        catch (TownyException e3) {
            return PlayerCache.TownBlockStatus.OUTSIDER;
        }
    }
    
    private static boolean getPermission(final Player player, final PlayerCache.TownBlockStatus status, final WorldCoord pos, final Material material, final TownyPermission.ActionType action) {
        if (status == PlayerCache.TownBlockStatus.OFF_WORLD || status == PlayerCache.TownBlockStatus.PLOT_OWNER || status == PlayerCache.TownBlockStatus.TOWN_OWNER) {
            return true;
        }
        if (status == PlayerCache.TownBlockStatus.WARZONE && TownySettings.isAllowWarBlockGriefing()) {
            return true;
        }
        if (status == PlayerCache.TownBlockStatus.NOT_REGISTERED) {
            cacheBlockErrMsg(player, TownySettings.getLangString("msg_cache_block_error"));
            return false;
        }
        if (status == PlayerCache.TownBlockStatus.LOCKED) {
            cacheBlockErrMsg(player, TownySettings.getLangString("msg_cache_block_error_locked"));
            return false;
        }
        TownBlock townBlock = null;
        Town playersTown = null;
        Town targetTown = null;
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            playersTown = townyUniverse.getDataSource().getResident(player.getName()).getTown();
        }
        catch (NotRegisteredException ex) {}
        Label_0511: {
            try {
                townBlock = pos.getTownBlock();
                targetTown = townBlock.getTown();
            }
            catch (NotRegisteredException e) {
                try {
                    if (status == PlayerCache.TownBlockStatus.UNCLAIMED_ZONE) {
                        if (townyUniverse.getPermissionSource().hasWildOverride(pos.getTownyWorld(), player, material, action)) {
                            return true;
                        }
                        cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_wild"), TownySettings.getLangString(action.toString())));
                        return false;
                    }
                    else {
                        if (!TownySettings.getNationZonesEnabled() || status != PlayerCache.TownBlockStatus.NATION_ZONE) {
                            break Label_0511;
                        }
                        if (townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN_NATION_ZONE.getNode()) && townyUniverse.getPermissionSource().hasWildOverride(pos.getTownyWorld(), player, material, action)) {
                            return true;
                        }
                        Town nearestTown = null;
                        nearestTown = pos.getTownyWorld().getClosestTownWithNationFromCoord(pos.getCoord(), nearestTown);
                        final Nation nearestNation = nearestTown.getNation();
                        if (TownySettings.getWarSiegeEnabled() && nearestTown.hasSiege() && nearestTown.getSiege().getStatus() == SiegeStatus.IN_PROGRESS) {
                            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_err_siege_war_nation_zone_this_area_protected_but_besieged"), pos.getTownyWorld().getUnclaimedZoneName(), nearestNation.getName()));
                            return false;
                        }
                        Nation playersNation;
                        try {
                            playersNation = playersTown.getNation();
                        }
                        catch (Exception e2) {
                            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("nation_zone_this_area_under_protection_of"), pos.getTownyWorld().getUnclaimedZoneName(), nearestNation.getName()));
                            return false;
                        }
                        if (!playersNation.equals(nearestNation)) {
                            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("nation_zone_this_area_under_protection_of"), pos.getTownyWorld().getUnclaimedZoneName(), nearestNation.getName()));
                            return false;
                        }
                        if (townyUniverse.getPermissionSource().hasWildOverride(pos.getTownyWorld(), player, material, action)) {
                            return true;
                        }
                        cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_wild"), TownySettings.getLangString(action.toString())));
                        return false;
                    }
                }
                catch (NotRegisteredException e3) {
                    TownyMessaging.sendErrorMsg(player, "Error updating " + action.toString() + " permission.");
                    return false;
                }
            }
        }
        if (townyUniverse.getPermissionSource().isTownyAdmin(player)) {
            return true;
        }
        if (townBlock.hasResident()) {
            if (targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasOwnTownOverride(player, material, action)) {
                return true;
            }
            if (!targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasAllTownOverride(player, material, action)) {
                return true;
            }
            if (status == PlayerCache.TownBlockStatus.PLOT_FRIEND) {
                if (townBlock.getPermissions().getResidentPerm(action)) {
                    if (townBlock.getType() == TownBlockType.WILDS) {
                        try {
                            if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                                return true;
                            }
                        }
                        catch (NotRegisteredException ex2) {}
                    }
                    else {
                        if (townBlock.getType() != TownBlockType.FARM || (!action.equals(TownyPermission.ActionType.BUILD) && !action.equals(TownyPermission.ActionType.DESTROY))) {
                            return true;
                        }
                        if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                            return true;
                        }
                    }
                }
                cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_plot"), "friends", TownySettings.getLangString(action.toString())));
                return false;
            }
            if (status == PlayerCache.TownBlockStatus.PLOT_TOWN) {
                if (townBlock.getPermissions().getNationPerm(action)) {
                    if (townBlock.getType() == TownBlockType.WILDS) {
                        try {
                            if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                                return true;
                            }
                        }
                        catch (NotRegisteredException ex3) {}
                    }
                    else {
                        if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                            return true;
                        }
                        if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                            return true;
                        }
                    }
                }
                cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_plot"), "town members", TownySettings.getLangString(action.toString())));
                return false;
            }
            if (status == PlayerCache.TownBlockStatus.PLOT_ALLY) {
                if (townBlock.getPermissions().getAllyPerm(action)) {
                    if (townBlock.getType() == TownBlockType.WILDS) {
                        try {
                            if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                                return true;
                            }
                        }
                        catch (NotRegisteredException ex4) {}
                    }
                    else {
                        if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                            return true;
                        }
                        if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                            return true;
                        }
                    }
                }
                cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_plot"), "allies", TownySettings.getLangString(action.toString())));
                return false;
            }
            if (townBlock.getPermissions().getOutsiderPerm(action)) {
                if (townBlock.getType() == TownBlockType.WILDS) {
                    try {
                        if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                            return true;
                        }
                    }
                    catch (NotRegisteredException ex5) {}
                }
                else {
                    if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                        return true;
                    }
                    if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                        return true;
                    }
                }
            }
            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_plot"), "outsiders", TownySettings.getLangString(action.toString())));
            return false;
        }
        else if (status == PlayerCache.TownBlockStatus.TOWN_RESIDENT) {
            if (targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasTownOwnedOverride(player, material, action)) {
                return true;
            }
            if (!targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasAllTownOverride(player, material, action)) {
                return true;
            }
            if (townBlock.getPermissions().getResidentPerm(action)) {
                if (townBlock.getType() == TownBlockType.WILDS) {
                    try {
                        if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                            return true;
                        }
                    }
                    catch (NotRegisteredException ex6) {}
                }
                else {
                    if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                        return true;
                    }
                    if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                        return true;
                    }
                }
            }
            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_town_resident"), TownySettings.getLangString(action.toString())));
            return false;
        }
        else if (status == PlayerCache.TownBlockStatus.TOWN_NATION) {
            if (targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasOwnTownOverride(player, material, action)) {
                return true;
            }
            if (!targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasAllTownOverride(player, material, action)) {
                return true;
            }
            if (townBlock.getPermissions().getNationPerm(action)) {
                if (townBlock.getType() == TownBlockType.WILDS) {
                    try {
                        if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                            return true;
                        }
                    }
                    catch (NotRegisteredException ex7) {}
                }
                else {
                    if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                        return true;
                    }
                    if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                        return true;
                    }
                }
            }
            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_town_nation"), TownySettings.getLangString(action.toString())));
            return false;
        }
        else if (status == PlayerCache.TownBlockStatus.TOWN_ALLY) {
            if (targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasOwnTownOverride(player, material, action)) {
                return true;
            }
            if (!targetTown.equals(playersTown) && townyUniverse.getPermissionSource().hasAllTownOverride(player, material, action)) {
                return true;
            }
            if (townBlock.getPermissions().getAllyPerm(action)) {
                if (townBlock.getType() == TownBlockType.WILDS) {
                    try {
                        if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                            return true;
                        }
                    }
                    catch (NotRegisteredException ex8) {}
                }
                else {
                    if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                        return true;
                    }
                    if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                        return true;
                    }
                }
            }
            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_town_allies"), TownySettings.getLangString(action.toString())));
            return false;
        }
        else {
            if (status != PlayerCache.TownBlockStatus.OUTSIDER && status != PlayerCache.TownBlockStatus.ENEMY) {
                TownyMessaging.sendErrorMsg(player, "Error updating " + action.toString() + " permission.");
                return false;
            }
            if (townyUniverse.getPermissionSource().hasAllTownOverride(player, material, action)) {
                return true;
            }
            if (townBlock.getPermissions().getOutsiderPerm(action)) {
                if (townBlock.getType() == TownBlockType.WILDS) {
                    try {
                        if (townyUniverse.getPermissionSource().unclaimedZoneAction(pos.getTownyWorld(), material, action)) {
                            return true;
                        }
                    }
                    catch (NotRegisteredException ex9) {}
                }
                else {
                    if (townBlock.getType() != TownBlockType.FARM || (action != TownyPermission.ActionType.BUILD && action != TownyPermission.ActionType.DESTROY)) {
                        return true;
                    }
                    if (TownySettings.getFarmPlotBlocks().contains(material.toString())) {
                        return true;
                    }
                }
            }
            cacheBlockErrMsg(player, String.format(TownySettings.getLangString("msg_cache_block_error_town_outsider"), TownySettings.getLangString(action.toString())));
            return false;
        }
    }
    
    static {
        PlayerCacheUtil.plugin = null;
    }
}
