package com.palmergames.bukkit.towny.war.flagwar.listeners;

import com.palmergames.bukkit.towny.war.flagwar.events.CellAttackCanceledEvent;
import java.util.List;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.tasks.TownClaim;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.command.TownCommand;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.war.flagwar.events.CellWonEvent;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.object.EconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.war.flagwar.Cell;
import com.palmergames.bukkit.towny.war.flagwar.events.CellDefendedEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.war.flagwar.CellUnderAttack;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.events.CellAttackEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyWarCustomListener implements Listener
{
    private final Towny plugin;
    
    public TownyWarCustomListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCellAttackEvent(final CellAttackEvent event) {
        if (event.isCancelled()) {
            return;
        }
        try {
            final CellUnderAttack cell = event.getData();
            TownyWar.registerAttack(cell);
        }
        catch (Exception e) {
            event.setCancelled(true);
            event.setReason(e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCellDefendedEvent(final CellDefendedEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final CellUnderAttack cell = event.getCell().getAttackData();
        try {
            TownyWar.townFlagged(TownyWar.cellToWorldCoord(cell).getTownBlock().getTown());
        }
        catch (NotRegisteredException ex2) {}
        final TownyUniverse universe = TownyUniverse.getInstance();
        final WorldCoord worldCoord = new WorldCoord(cell.getWorldName(), cell.getX(), cell.getZ());
        universe.removeWarZone(worldCoord);
        this.plugin.updateCache(worldCoord);
        String playerName;
        if (player == null) {
            playerName = "Greater Forces";
        }
        else {
            playerName = player.getName();
            try {
                playerName = universe.getDataSource().getResident(player.getName()).getFormattedName();
            }
            catch (TownyException ex3) {}
        }
        this.plugin.getServer().broadcastMessage(String.format(TownySettings.getLangString("msg_enemy_war_area_defended"), playerName, cell.getCellString()));
        if (TownySettings.isUsingEconomy()) {
            try {
                Resident defendingPlayer = null;
                final Resident attackingPlayer = universe.getDataSource().getResident(cell.getNameOfFlagOwner());
                if (player != null) {
                    try {
                        defendingPlayer = universe.getDataSource().getResident(player.getName());
                    }
                    catch (NotRegisteredException ex4) {}
                }
                final String formattedMoney = TownyEconomyHandler.getFormattedBalance(TownyWarConfig.getDefendedAttackReward());
                if (defendingPlayer == null) {
                    if (attackingPlayer.getAccount().pay(TownyWarConfig.getDefendedAttackReward(), "War - Attack Was Defended (Greater Forces)")) {
                        try {
                            TownyMessaging.sendResidentMessage(attackingPlayer, String.format(TownySettings.getLangString("msg_enemy_war_area_defended_greater_forces"), formattedMoney));
                        }
                        catch (TownyException ex5) {}
                    }
                }
                else if (attackingPlayer.getAccount().payTo(TownyWarConfig.getDefendedAttackReward(), defendingPlayer, "War - Attack Was Defended")) {
                    try {
                        TownyMessaging.sendResidentMessage(attackingPlayer, String.format(TownySettings.getLangString("msg_enemy_war_area_defended_attacker"), defendingPlayer.getFormattedName(), formattedMoney));
                    }
                    catch (TownyException ex6) {}
                    try {
                        TownyMessaging.sendResidentMessage(defendingPlayer, String.format(TownySettings.getLangString("msg_enemy_war_area_defended_defender"), attackingPlayer.getFormattedName(), formattedMoney));
                    }
                    catch (TownyException ex7) {}
                }
            }
            catch (EconomyException | NotRegisteredException e) {
                e.printStackTrace();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCellWonEvent(final CellWonEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final CellUnderAttack cell = event.getCellAttackData();
        final TownyUniverse universe = TownyUniverse.getInstance();
        try {
            final Resident attackingResident = universe.getDataSource().getResident(cell.getNameOfFlagOwner());
            final Town attackingTown = attackingResident.getTown();
            final Nation attackingNation = attackingTown.getNation();
            final WorldCoord worldCoord = TownyWar.cellToWorldCoord(cell);
            universe.removeWarZone(worldCoord);
            final TownBlock townBlock = worldCoord.getTownBlock();
            final Town defendingTown = townBlock.getTown();
            TownyWar.townFlagged(defendingTown);
            double amount = 0.0;
            String moneyTranserMsg = null;
            if (TownySettings.isUsingEconomy()) {
                try {
                    String reasonType;
                    if (townBlock.isHomeBlock()) {
                        amount = TownyWarConfig.getWonHomeblockReward();
                        reasonType = "Homeblock";
                    }
                    else {
                        amount = TownyWarConfig.getWonTownblockReward();
                        reasonType = "Townblock";
                    }
                    if (amount > 0.0) {
                        final String reason = String.format("War - Won Enemy %s (Pillage)", reasonType);
                        amount = Math.min(amount, defendingTown.getAccount().getHoldingBalance());
                        defendingTown.getAccount().payTo(amount, attackingResident, reason);
                        moneyTranserMsg = String.format(TownySettings.getLangString("msg_enemy_war_area_won_pillage"), attackingResident.getFormattedName(), TownyEconomyHandler.getFormattedBalance(amount), defendingTown.getFormattedName());
                    }
                    else if (amount < 0.0) {
                        amount = -amount;
                        final String reason = String.format("War - Won Enemy %s (Rebuild Cost)", reasonType);
                        if (!attackingResident.getAccount().payTo(amount, defendingTown, reason)) {
                            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_enemy_war_area_won"), attackingResident.getFormattedName(), attackingNation.hasTag() ? attackingNation.getTag() : attackingNation.getFormattedName(), cell.getCellString()));
                        }
                        moneyTranserMsg = String.format(TownySettings.getLangString("msg_enemy_war_area_won_rebuilding"), attackingResident.getFormattedName(), TownyEconomyHandler.getFormattedBalance(amount), defendingTown.getFormattedName());
                    }
                }
                catch (EconomyException x) {
                    x.printStackTrace();
                }
            }
            if (TownyWarConfig.isFlaggedTownblockTransfered()) {
                universe.getDataSource().removeTownBlock(townBlock);
                try {
                    final List<WorldCoord> selection = new ArrayList<WorldCoord>();
                    selection.add(worldCoord);
                    TownCommand.checkIfSelectionIsValid(attackingTown, selection, false, 0.0, false);
                    new TownClaim(this.plugin, null, attackingTown, selection, false, true, false).start();
                }
                catch (TownyException ex) {}
            }
            else {
                TownyMessaging.sendPrefixedTownMessage(attackingTown, String.format(TownySettings.getLangString("msg_war_defender_keeps_claims"), new Object[0]));
                TownyMessaging.sendPrefixedTownMessage(defendingTown, String.format(TownySettings.getLangString("msg_war_defender_keeps_claims"), new Object[0]));
            }
            this.plugin.updateCache(worldCoord);
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_enemy_war_area_won"), attackingResident.getFormattedName(), attackingNation.hasTag() ? attackingNation.getTag() : attackingNation.getFormattedName(), cell.getCellString()));
            if (TownySettings.isUsingEconomy() && amount != 0.0 && moneyTranserMsg != null) {
                try {
                    TownyMessaging.sendResidentMessage(attackingResident, moneyTranserMsg);
                }
                catch (TownyException ex2) {}
                TownyMessaging.sendPrefixedTownMessage(defendingTown, moneyTranserMsg);
            }
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCellAttackCanceledEvent(final CellAttackCanceledEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final CellUnderAttack cell = event.getCell();
        try {
            TownyWar.townFlagged(TownyWar.cellToWorldCoord(cell).getTownBlock().getTown());
        }
        catch (NotRegisteredException ex) {}
        final TownyUniverse universe = TownyUniverse.getInstance();
        final WorldCoord worldCoord = new WorldCoord(cell.getWorldName(), cell.getX(), cell.getZ());
        universe.removeWarZone(worldCoord);
        this.plugin.updateCache(worldCoord);
        System.out.println(cell.getCellString());
    }
}
