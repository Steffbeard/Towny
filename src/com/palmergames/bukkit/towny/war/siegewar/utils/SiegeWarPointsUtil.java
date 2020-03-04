// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.utils;

import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.entity.Player;
import java.util.Iterator;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.Map;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;

public class SiegeWarPointsUtil
{
    public static TownyObject calculateSiegeWinner(final Siege siege) {
        TownyObject winner = siege.getDefendingTown();
        int winningPoints = 0;
        for (final Map.Entry<Nation, SiegeZone> entry : siege.getSiegeZones().entrySet()) {
            if (entry.getValue().getSiegePoints() > winningPoints) {
                winner = entry.getKey();
                winningPoints = entry.getValue().getSiegePoints();
            }
        }
        return winner;
    }
    
    public static boolean isPlayerInSiegePointZone(final Player player, final SiegeZone siegeZone) {
        return player.getLocation().getWorld() == siegeZone.getFlagLocation().getWorld() && !TownyAPI.getInstance().hasTownBlock(player.getLocation()) && player.getLocation().distance(siegeZone.getFlagLocation()) < TownySettings.getTownBlockSize();
    }
    
    public static void evaluateSiegePenaltyPoints(final Resident resident, final String unformattedErrorMessage) {
        try {
            final Player player = TownyAPI.getInstance().getPlayer(resident);
            if (player == null) {
                return;
            }
            if (!resident.hasTown()) {
                return;
            }
            Town town;
            try {
                town = resident.getTown();
            }
            catch (NotRegisteredException e2) {
                return;
            }
            if (town.isOccupied()) {
                return;
            }
            final TownyUniverse universe = TownyUniverse.getInstance();
            if (town.hasSiege() && town.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode())) {
                for (final SiegeZone siegeZone : town.getSiege().getSiegeZones().values()) {
                    if (player.getLocation().distance(siegeZone.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                        awardSiegePenaltyPoints(false, siegeZone.getAttackingNation(), resident, siegeZone, unformattedErrorMessage);
                    }
                }
            }
            if (town.hasNation() && universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
                final Nation nation = town.getNation();
                for (final SiegeZone siegeZone2 : universe.getDataSource().getSiegeZones()) {
                    if (siegeZone2.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && (nation == siegeZone2.getAttackingNation() || nation.hasMutualAlly(siegeZone2.getAttackingNation())) && player.getLocation().distance(siegeZone2.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                        awardSiegePenaltyPoints(true, siegeZone2.getDefendingTown(), resident, siegeZone2, unformattedErrorMessage);
                    }
                }
                for (final SiegeZone siegeZone2 : universe.getDataSource().getSiegeZones()) {
                    if (siegeZone2.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && siegeZone2.getDefendingTown().hasNation() && (nation == siegeZone2.getDefendingTown().getNation() || nation.hasMutualAlly(siegeZone2.getDefendingTown().getNation())) && player.getLocation().distance(siegeZone2.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                        awardSiegePenaltyPoints(false, siegeZone2.getAttackingNation(), resident, siegeZone2, unformattedErrorMessage);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error evaluating siege point penalty");
        }
    }
    
    public static void awardSiegePenaltyPoints(final boolean attackerDeath, final TownyObject pointsRecipient, final Resident deadResident, final SiegeZone siegeZone, final String unformattedErrorMessage) throws NotRegisteredException {
        int siegePoints;
        if (attackerDeath) {
            siegePoints = TownySettings.getWarSiegePointsForAttackerDeath();
            siegeZone.adjustSiegePoints(-siegePoints);
        }
        else {
            siegePoints = TownySettings.getWarSiegePointsForDefenderDeath();
            siegeZone.adjustSiegePoints(siegePoints);
        }
        TownyUniverse.getInstance().getDataSource().saveSiegeZone(siegeZone);
        final String message = String.format(unformattedErrorMessage, TownyFormatter.getFormattedName(deadResident), TownyFormatter.getFormattedName(siegeZone.getDefendingTown()), siegePoints, TownyFormatter.getFormattedName(pointsRecipient));
        TownyMessaging.sendPrefixedNationMessage(siegeZone.getAttackingNation(), message);
        final Set<Nation> alliesToInform = new HashSet<Nation>();
        alliesToInform.addAll(siegeZone.getAttackingNation().getMutualAllies());
        if (siegeZone.getDefendingTown().hasNation()) {
            TownyMessaging.sendPrefixedNationMessage(siegeZone.getDefendingTown().getNation(), message);
            alliesToInform.addAll(siegeZone.getDefendingTown().getNation().getMutualAllies());
        }
        else {
            TownyMessaging.sendPrefixedTownMessage(siegeZone.getDefendingTown(), message);
        }
        for (final Nation alliedNation : alliesToInform) {
            TownyMessaging.sendPrefixedNationMessage(alliedNation, message);
        }
    }
}
