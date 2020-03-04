// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.playeractions;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.event.block.BlockPlaceEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;

public class InvadeTown
{
    public static void processInvadeTownRequest(final Towny plugin, final Player player, final Town townToBeInvaded, final BlockPlaceEvent event) {
        try {
            final TownyUniverse universe = TownyUniverse.getInstance();
            final Resident resident = universe.getDataSource().getResident(player.getName());
            final Town townOfInvadingResident = resident.getTown();
            if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_INVADE.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (townOfInvadingResident == townToBeInvaded) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_invade_own_town"));
            }
            final Siege siege = townToBeInvaded.getSiege();
            if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_invade_without_victory"));
            }
            final Nation nationOfInvadingResident = townOfInvadingResident.getNation();
            final Nation attackerWinner = siege.getAttackerWinner();
            if (nationOfInvadingResident != attackerWinner) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_invade_without_victory"));
            }
            if (siege.isTownInvaded()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_siege_war_town_already_invaded"), townToBeInvaded.getName()));
            }
            if (townToBeInvaded.hasNation() && townToBeInvaded.getNation() == attackerWinner) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_siege_war_town_already_belongs_to_your_nation"), townToBeInvaded.getName()));
            }
            if (TownySettings.getNationRequiresProximity() > 0.0) {
                final Coord capitalCoord = attackerWinner.getCapital().getHomeBlock().getCoord();
                final Coord townCoord = townToBeInvaded.getHomeBlock().getCoord();
                if (!attackerWinner.getCapital().getHomeBlock().getWorld().getName().equals(townToBeInvaded.getHomeBlock().getWorld().getName())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_nation_homeblock_in_another_world"));
                }
                final double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2.0) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2.0));
                if (distance > TownySettings.getNationRequiresProximity()) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_town_not_close_enough_to_nation"), townToBeInvaded.getName()));
                }
            }
            if (TownySettings.getMaxTownsPerNation() > 0 && attackerWinner.getTowns().size() >= TownySettings.getMaxTownsPerNation()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
            }
            captureTown(plugin, siege, attackerWinner, townToBeInvaded);
        }
        catch (TownyException x) {
            event.setBuild(false);
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    private static void captureTown(final Towny plugin, final Siege siege, final Nation attackingNation, final Town defendingTown) {
        siege.setTownInvaded(true);
        SiegeWarTimeUtil.activateRevoltImmunityTimer(defendingTown);
        if (defendingTown.hasNation()) {
            Nation nationOfDefendingTown = null;
            try {
                nationOfDefendingTown = defendingTown.getNation();
            }
            catch (NotRegisteredException ex) {}
            removeTownFromNation(plugin, defendingTown, nationOfDefendingTown);
            addTownToNation(plugin, defendingTown, attackingNation);
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_nation_town_captured"), TownyFormatter.getFormattedTownName(defendingTown), TownyFormatter.getFormattedNationName(nationOfDefendingTown), TownyFormatter.getFormattedNationName(attackingNation)));
            if (nationOfDefendingTown.getTowns().size() == 0) {
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_nation_defeated"), TownyFormatter.getFormattedNationName(nationOfDefendingTown)));
            }
        }
        else {
            addTownToNation(plugin, defendingTown, attackingNation);
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_neutral_town_captured"), TownyFormatter.getFormattedTownName(defendingTown), TownyFormatter.getFormattedNationName(attackingNation)));
        }
        defendingTown.setOccupied(true);
        TownyUniverse.getInstance().getDataSource().saveTown(defendingTown);
    }
    
    private static void removeTownFromNation(final Towny plugin, final Town town, final Nation nation) {
        boolean removeNation = false;
        final Resident king = nation.getKing();
        try {
            nation.removeTown(town);
        }
        catch (NotRegisteredException x) {
            return;
        }
        catch (EmptyNationException x2) {
            removeNation = true;
        }
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (removeNation) {
            universe.getDataSource().removeNation(nation);
            universe.getDataSource().saveNationList();
        }
        else {
            universe.getDataSource().saveNation(nation);
            universe.getDataSource().saveNationList();
            plugin.resetCache();
        }
        universe.getDataSource().saveTown(town);
    }
    
    private static void addTownToNation(final Towny plugin, final Town town, final Nation nation) {
        try {
            final TownyUniverse universe = TownyUniverse.getInstance();
            nation.addTown(town);
            universe.getDataSource().saveTown(town);
            plugin.resetCache();
            universe.getDataSource().saveNation(nation);
        }
        catch (AlreadyRegisteredException x) {}
    }
}
