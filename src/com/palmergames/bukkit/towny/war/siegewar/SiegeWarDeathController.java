// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar;

import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarPointsUtil;
import java.util.Iterator;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;

public class SiegeWarDeathController
{
    public static void evaluateSiegePvPDeath(final Player deadPlayer, final Player killerPlayer, final Resident deadResident, final Resident killerResident) {
        System.out.println("Now evaluating pvp death");
        try {
            if (!deadResident.hasTown()) {
                return;
            }
            if (!killerResident.hasTown()) {
                return;
            }
            final Town deadResidentTown = deadResident.getTown();
            final Town killerResidentTown = killerResident.getTown();
            if (deadResidentTown.isOccupied() || killerResidentTown.isOccupied()) {
                return;
            }
            SiegeZone siegeZone;
            if ((siegeZone = getSiegeZoneForAttackingSoldierKilledDefendingGuard(deadPlayer, killerPlayer, deadResidentTown, killerResidentTown)) != null) {
                awardSiegePvpPenaltyPoints(false, siegeZone.getAttackingNation(), deadResident, siegeZone);
            }
            else if ((siegeZone = getSiegeZoneForAttackingSoldierKilledDefendingSoldier(deadPlayer, killerPlayer, deadResidentTown, killerResidentTown)) != null) {
                awardSiegePvpPenaltyPoints(false, siegeZone.getAttackingNation(), deadResident, siegeZone);
            }
            else if ((siegeZone = getSiegeZoneForDefendingGuardKilledAttackingSoldier(deadPlayer, killerPlayer, deadResidentTown, killerResidentTown)) != null) {
                awardSiegePvpPenaltyPoints(true, siegeZone.getDefendingTown(), deadResident, siegeZone);
            }
            else if ((siegeZone = getSiegeZoneForDefendingSoldierKilledAttackingSoldier(deadPlayer, killerPlayer, deadResidentTown, killerResidentTown)) != null) {
                awardSiegePvpPenaltyPoints(true, siegeZone.getDefendingTown(), deadResident, siegeZone);
            }
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
            System.out.println("Error evaluating siege pvp death");
        }
    }
    
    private static SiegeZone getSiegeZoneForAttackingSoldierKilledDefendingGuard(final Player deadPlayer, final Player killerPlayer, final Town deadResidentTown, final Town killerResidentTown) throws NotRegisteredException {
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (killerResidentTown.hasNation() && deadResidentTown.hasSiege() && deadResidentTown.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && universe.getPermissionSource().testPermission(deadPlayer, PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode()) && universe.getPermissionSource().testPermission(killerPlayer, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
            for (final SiegeZone siegeZone : deadResidentTown.getSiege().getSiegeZones().values()) {
                if ((killerResidentTown.getNation() == siegeZone.getAttackingNation() || killerResidentTown.getNation().hasMutualAlly(siegeZone.getAttackingNation())) && deadPlayer.getLocation().distance(siegeZone.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                    return siegeZone;
                }
            }
        }
        return null;
    }
    
    private static SiegeZone getSiegeZoneForAttackingSoldierKilledDefendingSoldier(final Player deadPlayer, final Player killerPlayer, final Town deadResidentTown, final Town killerResidentTown) throws NotRegisteredException {
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (killerResidentTown.hasNation() && deadResidentTown.hasNation() && universe.getPermissionSource().testPermission(deadPlayer, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode()) && universe.getPermissionSource().testPermission(killerPlayer, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
            for (final SiegeZone siegeZone : universe.getDataSource().getSiegeZones()) {
                if (siegeZone.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && (killerResidentTown.getNation() == siegeZone.getAttackingNation() || killerResidentTown.getNation().hasMutualAlly(siegeZone.getAttackingNation())) && (deadResidentTown.getNation() == siegeZone.getDefendingTown().getNation() || deadResidentTown.getNation().hasMutualAlly(siegeZone.getDefendingTown().getNation())) && deadPlayer.getLocation().distance(siegeZone.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                    return siegeZone;
                }
            }
        }
        return null;
    }
    
    private static SiegeZone getSiegeZoneForDefendingGuardKilledAttackingSoldier(final Player deadPlayer, final Player killerPlayer, final Town deadResidentTown, final Town killerResidentTown) throws NotRegisteredException {
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (killerResidentTown.hasSiege() && killerResidentTown.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && deadResidentTown.hasNation() && universe.getPermissionSource().testPermission(deadPlayer, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode()) && universe.getPermissionSource().testPermission(killerPlayer, PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode())) {
            for (final SiegeZone siegeZone : killerResidentTown.getSiege().getSiegeZones().values()) {
                if ((siegeZone.getAttackingNation() == deadResidentTown.getNation() || siegeZone.getAttackingNation().hasMutualAlly(deadResidentTown.getNation())) && deadPlayer.getLocation().distance(siegeZone.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                    return siegeZone;
                }
            }
        }
        return null;
    }
    
    private static SiegeZone getSiegeZoneForDefendingSoldierKilledAttackingSoldier(final Player deadPlayer, final Player killerPlayer, final Town deadResidentTown, final Town killerResidentTown) throws NotRegisteredException {
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (killerResidentTown.hasNation() && deadResidentTown.hasNation() && universe.getPermissionSource().testPermission(deadPlayer, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode()) && universe.getPermissionSource().testPermission(killerPlayer, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
            for (final SiegeZone siegeZone : universe.getDataSource().getSiegeZones()) {
                if (siegeZone.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && (killerResidentTown.getNation() == siegeZone.getDefendingTown().getNation() || killerResidentTown.getNation().hasMutualAlly(siegeZone.getDefendingTown().getNation())) && (deadResidentTown.getNation() == siegeZone.getAttackingNation() || deadResidentTown.getNation().hasMutualAlly(siegeZone.getAttackingNation())) && deadPlayer.getLocation().distance(siegeZone.getFlagLocation()) < TownySettings.getWarSiegeZoneDeathRadiusBlocks()) {
                    return siegeZone;
                }
            }
        }
        return null;
    }
    
    private static void awardSiegePvpPenaltyPoints(final boolean attackerDeath, final TownyObject pointsRecipient, final Resident deadResident, final SiegeZone siegeZone) throws NotRegisteredException {
        if (attackerDeath) {
            SiegeWarPointsUtil.awardSiegePenaltyPoints(attackerDeath, pointsRecipient, deadResident, siegeZone, TownySettings.getLangString("msg_siege_war_participant_death"));
        }
        else {
            for (final SiegeZone siegeZoneInCollection : siegeZone.getSiege().getSiegeZones().values()) {
                SiegeWarPointsUtil.awardSiegePenaltyPoints(attackerDeath, siegeZoneInCollection.getAttackingNation(), deadResident, siegeZoneInCollection, TownySettings.getLangString("msg_siege_war_participant_death"));
            }
        }
    }
}
