// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.playeractions;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.object.EconomyHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.event.block.BlockPlaceEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class PlunderTown
{
    public static void processPlunderTownRequest(final Player player, final Town townToBePlundered, final BlockPlaceEvent event) {
        try {
            if (!TownySettings.isUsingEconomy()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_plunder_without_economy"));
            }
            if (TownySettings.getWarSiegeTownNeutralityEnabled() && townToBePlundered.isNeutral()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_neutral_town_cannot_plunder"));
            }
            final TownyUniverse universe = TownyUniverse.getInstance();
            final Resident resident = universe.getDataSource().getResident(player.getName());
            if (!resident.hasTown()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_action_not_a_town_member"));
            }
            final Town townOfPlunderingResident = resident.getTown();
            if (!townOfPlunderingResident.hasNation()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_action_not_a_nation_member"));
            }
            if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_PLUNDER.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (townOfPlunderingResident == townToBePlundered) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_plunder_own_town"));
            }
            final Siege siege = townToBePlundered.getSiege();
            if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_plunder_without_victory"));
            }
            if (townOfPlunderingResident.getNation() != siege.getAttackerWinner()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_plunder_without_victory"));
            }
            if (siege.isTownPlundered()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_siege_war_town_already_plundered"), townToBePlundered.getName()));
            }
            plunderTown(siege, townToBePlundered, siege.getAttackerWinner(), event);
        }
        catch (TownyException x) {
            event.setBuild(false);
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    private static void plunderTown(final Siege siege, final Town defendingTown, final Nation winnerNation, final BlockPlaceEvent event) {
        siege.setTownPlundered(true);
        final double fullPlunderAmount = TownySettings.getWarSiegeAttackerPlunderAmountPerPlot() * defendingTown.getTownBlocks().size();
        try {
            final TownyUniverse universe = TownyUniverse.getInstance();
            if (defendingTown.getAccount().canPayFromHoldings(fullPlunderAmount)) {
                defendingTown.getAccount().payTo(fullPlunderAmount, winnerNation, "Town was plundered by attacker");
                sendPlunderSuccessMessage(defendingTown, winnerNation, fullPlunderAmount);
                universe.getDataSource().saveTown(defendingTown);
            }
            else {
                final double actualPlunderAmount = defendingTown.getAccount().getHoldingBalance();
                defendingTown.getAccount().payTo(actualPlunderAmount, winnerNation, "Town was plundered by attacker");
                sendPlunderSuccessMessage(defendingTown, winnerNation, actualPlunderAmount);
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_town_ruined_from_plunder"), TownyFormatter.getFormattedTownName(defendingTown), TownyFormatter.getFormattedNationName(winnerNation)));
                universe.getDataSource().removeTown(defendingTown);
            }
        }
        catch (EconomyException x) {
            event.setBuild(false);
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(x.getMessage());
        }
    }
    
    private static void sendPlunderSuccessMessage(final Town defendingTown, final Nation winnerNation, final double plunderAmount) {
        if (defendingTown.hasNation()) {
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_nation_town_plundered"), TownyFormatter.getFormattedTownName(defendingTown), TownyEconomyHandler.getFormattedBalance(plunderAmount), TownyFormatter.getFormattedNationName(winnerNation)));
        }
        else {
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_neutral_town_plundered"), TownyFormatter.getFormattedTownName(defendingTown), TownyEconomyHandler.getFormattedBalance(plunderAmount), TownyFormatter.getFormattedNationName(winnerNation)));
        }
    }
}
