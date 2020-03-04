// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import org.bukkit.Chunk;
import com.earth2me.essentials.Teleport;
import com.earth2me.essentials.User;
import java.util.List;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Iterator;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyTimerHandler;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.EconomyAccount;
import com.earth2me.essentials.Trade;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.object.NationSpawnLevel;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownSpawnLevel;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.SpawnType;
import com.palmergames.bukkit.towny.object.TownyObject;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;

public class SpawnUtil
{
    private static Towny plugin;
    
    public static void initialize(final Towny plugin) {
        SpawnUtil.plugin = plugin;
    }
    
    public static void sendToTownySpawn(final Player player, final String[] split, final TownyObject townyObject, final String notAffordMSG, final boolean outpost, final SpawnType spawnType) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
        if (TownySettings.getSpawnCooldownTime() > 0 && CooldownTimerTask.hasCooldown(resident.getName(), CooldownTimerTask.CooldownType.TELEPORT)) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_spawn_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(resident.getName(), CooldownTimerTask.CooldownType.TELEPORT)));
        }
        if (resident.isJailed()) {
            throw new TownyException(TownySettings.getLangString("msg_cannot_spawn_while_jailed"));
        }
        Town town = null;
        Nation nation = null;
        Location spawnLoc = null;
        TownSpawnLevel townSpawnPermission = null;
        NationSpawnLevel nationSpawnPermission = null;
        final boolean isTownyAdmin = townyUniverse.getPermissionSource().has(player, spawnType.getNode());
        switch (spawnType) {
            case RESIDENT: {
                if (resident.hasTown()) {
                    town = resident.getTown();
                }
                if (TownySettings.getBedUse() && player.getBedSpawnLocation() != null) {
                    spawnLoc = player.getBedSpawnLocation();
                }
                else if (town != null) {
                    spawnLoc = town.getSpawn();
                }
                else {
                    spawnLoc = SpawnUtil.plugin.getCache(player).getLastLocation().getWorld().getSpawnLocation();
                }
                if (isTownyAdmin) {
                    townSpawnPermission = TownSpawnLevel.ADMIN;
                    break;
                }
                townSpawnPermission = TownSpawnLevel.TOWN_RESIDENT;
                break;
            }
            case TOWN: {
                town = (Town)townyObject;
                if (outpost) {
                    if (!town.hasOutpostSpawn()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_outpost_spawn"));
                    }
                    Integer index = null;
                    try {
                        if (!split[split.length - 1].contains("name:")) {
                            index = Integer.parseInt(split[split.length - 1]);
                        }
                        else {
                            split[split.length - 1] = split[split.length - 1].replace("name:", "").replace("_", " ");
                            for (final Location loc : town.getAllOutpostSpawns()) {
                                final TownBlock tboutpost = TownyAPI.getInstance().getTownBlock(loc);
                                if (tboutpost != null) {
                                    final String name = tboutpost.getName();
                                    if (!name.startsWith(split[split.length - 1])) {
                                        continue;
                                    }
                                    index = 1 + town.getAllOutpostSpawns().indexOf(loc);
                                }
                            }
                            if (index == null) {
                                index = 1;
                            }
                        }
                    }
                    catch (NumberFormatException e2) {
                        index = 1;
                        split[split.length - 1] = split[split.length - 1].replace("_", " ");
                        for (final Location loc2 : town.getAllOutpostSpawns()) {
                            final TownBlock tboutpost2 = TownyAPI.getInstance().getTownBlock(loc2);
                            if (tboutpost2 != null) {
                                final String name2 = tboutpost2.getName();
                                if (!name2.startsWith(split[split.length - 1])) {
                                    continue;
                                }
                                index = 1 + town.getAllOutpostSpawns().indexOf(loc2);
                            }
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException i) {
                        index = 1;
                    }
                    if (TownySettings.isOutpostLimitStoppingTeleports() && TownySettings.isOutpostsLimitedByLevels() && town.isOverOutpostLimit() && Math.max(1, index) > town.getOutpostLimit()) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_over_outposts_limit"), town.getMaxOutpostSpawn(), town.getOutpostLimit()));
                    }
                    spawnLoc = town.getOutpostSpawn(Math.max(1, index));
                }
                else {
                    spawnLoc = town.getSpawn();
                }
                if (isTownyAdmin) {
                    townSpawnPermission = TownSpawnLevel.ADMIN;
                }
                else if (split.length == 0 && !outpost) {
                    townSpawnPermission = TownSpawnLevel.TOWN_RESIDENT;
                }
                else if (!resident.hasTown()) {
                    townSpawnPermission = TownSpawnLevel.UNAFFILIATED;
                }
                else if (resident.getTown() == town) {
                    townSpawnPermission = (outpost ? TownSpawnLevel.TOWN_RESIDENT_OUTPOST : TownSpawnLevel.TOWN_RESIDENT);
                }
                else if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeAttackerSpawnIntoBesiegedTownDisabled() && resident.hasNation() && town.hasSiege() && town.getSiege().getStatus() == SiegeStatus.IN_PROGRESS) {
                    for (final SiegeZone siegeZone : town.getSiege().getSiegeZones().values()) {
                        if (resident.getTown().getNation() == siegeZone.getAttackingNation() || resident.getTown().getNation().hasMutualAlly(siegeZone.getAttackingNation())) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_siege_war_cannot_spawn_into_besieged_town"), town.getName()));
                        }
                    }
                }
                else if (resident.hasNation() && town.hasNation()) {
                    final Nation playerNation = resident.getTown().getNation();
                    final Nation targetNation = town.getNation();
                    if (playerNation == targetNation) {
                        if (!town.isPublic() && TownySettings.isAllySpawningRequiringPublicStatus()) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_ally_isnt_public"), town));
                        }
                        townSpawnPermission = TownSpawnLevel.PART_OF_NATION;
                    }
                    else {
                        if (targetNation.hasEnemy(playerNation)) {
                            throw new TownyException(TownySettings.getLangString("msg_err_public_spawn_enemy"));
                        }
                        if (targetNation.hasAlly(playerNation)) {
                            if (!town.isPublic() && TownySettings.isAllySpawningRequiringPublicStatus()) {
                                throw new TownyException(String.format(TownySettings.getLangString("msg_err_ally_isnt_public"), town));
                            }
                            townSpawnPermission = TownSpawnLevel.NATION_ALLY;
                        }
                        else {
                            townSpawnPermission = TownSpawnLevel.UNAFFILIATED;
                        }
                    }
                }
                else {
                    townSpawnPermission = TownSpawnLevel.UNAFFILIATED;
                }
                TownyMessaging.sendDebugMsg(townSpawnPermission.toString() + " " + townSpawnPermission.isAllowed(town));
                townSpawnPermission.checkIfAllowed(SpawnUtil.plugin, player, town);
                Label_1204: {
                    if (!isTownyAdmin) {
                        if (townSpawnPermission == TownSpawnLevel.UNAFFILIATED) {
                            if (town.isPublic()) {
                                break Label_1204;
                            }
                        }
                        else if (townSpawnPermission.hasPermissionNode(SpawnUtil.plugin, player, town)) {
                            break Label_1204;
                        }
                        throw new TownyException(TownySettings.getLangString("msg_err_not_public"));
                    }
                }
                if (!isTownyAdmin && town.hasOutlaw(resident)) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_error_cannot_town_spawn_youre_an_outlaw_in_town"), town));
                }
                break;
            }
            case NATION: {
                nation = (Nation)townyObject;
                spawnLoc = nation.getNationSpawn();
                if (isTownyAdmin) {
                    nationSpawnPermission = NationSpawnLevel.ADMIN;
                }
                else if (split.length == 0) {
                    nationSpawnPermission = NationSpawnLevel.PART_OF_NATION;
                }
                else if (!resident.hasTown()) {
                    nationSpawnPermission = NationSpawnLevel.UNAFFILIATED;
                }
                else if (resident.hasNation()) {
                    final Nation playerNation = resident.getTown().getNation();
                    if (playerNation == nation) {
                        nationSpawnPermission = NationSpawnLevel.PART_OF_NATION;
                    }
                    else {
                        if (nation.hasEnemy(playerNation)) {
                            throw new TownyException(TownySettings.getLangString("msg_err_public_spawn_enemy"));
                        }
                        if (nation.hasAlly(playerNation)) {
                            nationSpawnPermission = NationSpawnLevel.NATION_ALLY;
                        }
                        else {
                            nationSpawnPermission = NationSpawnLevel.UNAFFILIATED;
                        }
                    }
                }
                else {
                    nationSpawnPermission = NationSpawnLevel.UNAFFILIATED;
                }
                if (!isTownyAdmin) {
                    if (nationSpawnPermission == NationSpawnLevel.UNAFFILIATED) {
                        if (nation.isPublic()) {
                            break;
                        }
                    }
                    else if (nationSpawnPermission.hasPermissionNode(SpawnUtil.plugin, player, nation)) {
                        break;
                    }
                    throw new TownyException(TownySettings.getLangString("msg_err_nation_not_public"));
                }
                break;
            }
        }
        if (!isTownyAdmin) {
            final List<String> disallowedZones = TownySettings.getDisallowedTownSpawnZones();
            if (!disallowedZones.isEmpty()) {
                String inTown;
                try {
                    final Location loc = SpawnUtil.plugin.getCache(player).getLastLocation();
                    inTown = TownyAPI.getInstance().getTownName(loc);
                }
                catch (NullPointerException e3) {
                    inTown = TownyAPI.getInstance().getTownName(player.getLocation());
                }
                if (inTown == null && disallowedZones.contains("unclaimed")) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_x_spawn_disallowed_from_x"), spawnType.getTypeName(), TownySettings.getLangString("msg_the_wilderness")));
                }
                if (inTown != null && resident.hasNation() && townyUniverse.getDataSource().getTown(inTown).hasNation()) {
                    final Nation inNation = townyUniverse.getDataSource().getTown(inTown).getNation();
                    final Nation playerNation2 = resident.getTown().getNation();
                    if (inNation.hasEnemy(playerNation2) && disallowedZones.contains("enemy")) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_x_spawn_disallowed_from_x"), spawnType.getTypeName(), TownySettings.getLangString("msg_enemy_areas")));
                    }
                    if (!inNation.hasAlly(playerNation2) && !inNation.hasEnemy(playerNation2) && disallowedZones.contains("neutral")) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_x_spawn_disallowed_from_x"), spawnType.getTypeName(), TownySettings.getLangString("msg_neutral_towns")));
                    }
                }
            }
        }
        double travelCost = 0.0;
        String spawnPermission = null;
        EconomyAccount payee = null;
        switch (spawnType) {
            case RESIDENT:
            case TOWN: {
                travelCost = Math.min(townSpawnPermission.getCost(town), townSpawnPermission.getCost());
                spawnPermission = String.format(spawnType.getTypeName() + " (%s)", townSpawnPermission);
                payee = town.getAccount();
                break;
            }
            case NATION: {
                travelCost = Math.min(nationSpawnPermission.getCost(nation), nationSpawnPermission.getCost());
                spawnPermission = String.format(spawnType.getTypeName() + " (%s)", nationSpawnPermission);
                payee = nation.getAccount();
                break;
            }
        }
        try {
            if (!townyUniverse.getPermissionSource().has(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_SPAWN_FREECHARGE.getNode()) && travelCost > 0.0 && TownySettings.isUsingEconomy() && resident.getAccount().getHoldingBalance() < travelCost) {
                throw new TownyException(notAffordMSG);
            }
        }
        catch (EconomyException ex) {}
        final boolean usingESS = SpawnUtil.plugin.isEssentials();
        if (usingESS && !isTownyAdmin) {
            try {
                final User essentialsUser = SpawnUtil.plugin.getEssentials().getUser(player);
                if (!essentialsUser.isJailed()) {
                    final Teleport teleport = essentialsUser.getTeleport();
                    teleport.cooldown(true);
                    teleport.teleport(spawnLoc, (Trade)null, PlayerTeleportEvent.TeleportCause.COMMAND);
                }
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg(player, "Error: " + e.getMessage());
                return;
            }
        }
        if (!townyUniverse.getPermissionSource().has(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_SPAWN_FREECHARGE.getNode())) {
            if (!TownySettings.isTownSpawnPaidToTown()) {
                payee = EconomyAccount.SERVER_ACCOUNT;
            }
            try {
                if (travelCost > 0.0 && TownySettings.isUsingEconomy() && resident.getAccount().payTo(travelCost, payee, spawnPermission)) {
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_cost_spawn"), TownyEconomyHandler.getFormattedBalance(travelCost)));
                }
            }
            catch (EconomyException ex2) {}
        }
        final Chunk chunk = spawnLoc.getChunk();
        if (isTownyAdmin) {
            if (player.getVehicle() != null) {
                player.getVehicle().eject();
            }
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            player.teleport(spawnLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
            return;
        }
        if (!usingESS) {
            if (TownyTimerHandler.isTeleportWarmupRunning()) {
                player.sendMessage(String.format(TownySettings.getLangString("msg_town_spawn_warmup"), TownySettings.getTeleportWarmupTime()));
                TownyAPI.getInstance().requestTeleport(player, spawnLoc);
            }
            else {
                if (player.getVehicle() != null) {
                    player.getVehicle().eject();
                }
                if (!chunk.isLoaded()) {
                    chunk.load();
                }
                player.teleport(spawnLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
                if (TownySettings.getSpawnCooldownTime() > 0) {
                    CooldownTimerTask.addCooldownTimer(resident.getName(), CooldownTimerTask.CooldownType.TELEPORT);
                }
            }
        }
    }
}
