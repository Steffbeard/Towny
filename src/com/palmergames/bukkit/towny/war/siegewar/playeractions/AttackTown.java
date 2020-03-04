// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.playeractions;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarBlockUtil;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.event.block.BlockPlaceEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class AttackTown
{
    public static void processAttackTownRequest(final Player player, final Block block, final TownBlock townBlock, final Town defendingTown, final BlockPlaceEvent event) {
        try {
            final TownyUniverse universe = TownyUniverse.getInstance();
            final Resident attackingResident = universe.getDataSource().getResident(player.getName());
            final Town townOfAttackingPlayer = attackingResident.getTown();
            if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_ATTACK.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (townOfAttackingPlayer == defendingTown) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_attack_own_town"));
            }
            final Nation nationOfAttackingPlayer = townOfAttackingPlayer.getNation();
            if (defendingTown.hasNation()) {
                final Nation nationOfDefendingTown = defendingTown.getNation();
                if (nationOfAttackingPlayer == nationOfDefendingTown) {
                    throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_attack_town_in_own_nation"));
                }
                if (!nationOfAttackingPlayer.hasEnemy(nationOfDefendingTown)) {
                    throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_attack_non_enemy_nation"));
                }
            }
            if (nationOfAttackingPlayer.isNationAttackingTown(defendingTown)) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_nation_already_attacking_town"));
            }
            if (defendingTown.isSiegeImmunityActive()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_attack_siege_immunity"));
            }
            if (defendingTown.hasSiege() && defendingTown.getSiege().getStatus() == SiegeStatus.IN_PROGRESS) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_join_siege"));
            }
            if (TownySettings.isUsingEconomy() && !nationOfAttackingPlayer.getAccount().canPayFromHoldings(defendingTown.getSiegeCost())) {
                throw new TownyException(TownySettings.getLangString("msg_err_no_money"));
            }
            if (SiegeWarBlockUtil.doesBlockHaveANonAirBlockAboveIt(block)) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_banner_must_be_placed_above_ground"));
            }
            if (defendingTown.isRuined()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_attack_ruined_town"));
            }
            if (!SiegeWarDistanceUtil.isBannerToTownElevationDifferenceOk(block, townBlock)) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_place_banner_far_above_town"));
            }
            attackTown(block, nationOfAttackingPlayer, defendingTown);
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            event.setBuild(false);
            event.setCancelled(true);
        }
        catch (EconomyException x2) {
            event.setBuild(false);
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(player, x2.getMessage());
        }
    }
    
    private static void attackTown(final Block block, final Nation attackingNation, final Town defendingTown) throws TownyException {
        boolean newSiege;
        Siege siege;
        if (!defendingTown.hasSiege()) {
            newSiege = true;
            siege = new Siege(defendingTown);
            siege.setStatus(SiegeStatus.IN_PROGRESS);
            siege.setTownPlundered(false);
            siege.setTownInvaded(false);
            siege.setAttackerWinner(null);
            siege.setStartTime(System.currentTimeMillis());
            siege.setScheduledEndTime(System.currentTimeMillis() + (long)(TownySettings.getWarSiegeMaxHoldoutTimeHours() * 3600000.0));
            siege.setActualEndTime(0L);
            defendingTown.setSiege(siege);
        }
        else {
            newSiege = false;
            siege = defendingTown.getSiege();
        }
        final TownyUniverse universe = TownyUniverse.getInstance();
        universe.getDataSource().newSiegeZone(attackingNation.getName(), defendingTown.getName());
        final SiegeZone siegeZone = universe.getDataSource().getSiegeZone(SiegeZone.generateName(attackingNation.getName(), defendingTown.getName()));
        siegeZone.setFlagLocation(block.getLocation());
        siegeZone.setWarChestAmount(defendingTown.getSiegeCost());
        siege.getSiegeZones().put(attackingNation, siegeZone);
        attackingNation.addSiegeZone(siegeZone);
        universe.getDataSource().saveSiegeZone(siegeZone);
        universe.getDataSource().saveNation(attackingNation);
        universe.getDataSource().saveTown(defendingTown);
        universe.getDataSource().saveSiegeZoneList();
        if (newSiege) {
            if (siege.getDefendingTown().hasNation()) {
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_siege_started_nation_town"), TownyFormatter.getFormattedNationName(attackingNation), TownyFormatter.getFormattedNationName(defendingTown.getNation()), TownyFormatter.getFormattedTownName(defendingTown)));
            }
            else {
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_siege_started_neutral_town"), TownyFormatter.getFormattedNationName(attackingNation), TownyFormatter.getFormattedTownName(defendingTown)));
            }
        }
        else {
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_siege_joined"), TownyFormatter.getFormattedNationName(attackingNation), TownyFormatter.getFormattedTownName(defendingTown)));
        }
        if (TownySettings.isUsingEconomy()) {
            try {
                attackingNation.getAccount().pay(siegeZone.getWarChestAmount(), "Cost of starting a siege.");
                final String moneyMessage = String.format(TownySettings.getLangString("msg_siege_war_attack_pay_war_chest"), TownyFormatter.getFormattedNationName(attackingNation), TownyEconomyHandler.getFormattedBalance(siegeZone.getWarChestAmount()));
                TownyMessaging.sendPrefixedNationMessage(attackingNation, moneyMessage);
                TownyMessaging.sendPrefixedTownMessage(defendingTown, moneyMessage);
            }
            catch (EconomyException e) {
                System.out.println("Problem paying into war chest");
                e.printStackTrace();
            }
        }
    }
}
