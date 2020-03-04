// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.playeractions;

import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarSiegeCompletionUtil;
import java.util.Collection;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.util.TimeMgmt;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.event.block.BlockPlaceEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class SurrenderTown
{
    public static void processTownSurrenderRequest(final Player player, final Town townWhereBlockWasPlaced, final BlockPlaceEvent event) {
        try {
            final TownyUniverse universe = TownyUniverse.getInstance();
            final Resident resident = universe.getDataSource().getResident(player.getName());
            if (!resident.hasTown()) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_action_not_a_town_member"));
            }
            final Town townOfAttackingResident = resident.getTown();
            if (townOfAttackingResident != townWhereBlockWasPlaced) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_surrender_not_your_town"));
            }
            if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_TOWN_SIEGE_SURRENDER.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            final Siege siege = townWhereBlockWasPlaced.getSiege();
            if (siege.getStatus() != SiegeStatus.IN_PROGRESS) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_surrender_siege_finished"));
            }
            if (siege.getSiegeZones().size() > 1) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_surrender_multiple_attackers"));
            }
            final long timeUntilSurrenderIsAllowedMillis = siege.getTimeUntilSurrenderIsAllowedMillis();
            if (timeUntilSurrenderIsAllowedMillis > 0L) {
                final String message = String.format(TownySettings.getLangString("msg_err_siege_war_cannot_surrender_yet"), TimeMgmt.getFormattedTimeValue((double)timeUntilSurrenderIsAllowedMillis));
                throw new TownyException(message);
            }
            defenderSurrender(siege);
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            event.setBuild(false);
            event.setCancelled(true);
        }
    }
    
    private static void defenderSurrender(final Siege siege) {
        final Nation winnerNation = new ArrayList<Nation>(siege.getSiegeZones().keySet()).get(0);
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_SURRENDER, winnerNation);
        TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_town_surrender"), TownyFormatter.getFormattedTownName(siege.getDefendingTown()), TownyFormatter.getFormattedNationName(siege.getAttackerWinner())));
        SiegeWarMoneyUtil.giveWarChestsToWinnerNation(siege, siege.getAttackerWinner());
    }
}
