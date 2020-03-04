// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.flagwar.events.CellAttackEvent;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.utils.AreaSelectionUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Cancellable;
import org.bukkit.block.Block;
import com.palmergames.bukkit.towny.war.flagwar.events.CellAttackCanceledEvent;
import com.palmergames.bukkit.towny.war.flagwar.events.CellDefendedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.war.flagwar.events.CellWonEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import java.util.Map;

public class TownyWar
{
    private static Map<Cell, CellUnderAttack> cellsUnderAttack;
    private static Map<String, List<CellUnderAttack>> cellsUnderAttackByPlayer;
    private static Map<Town, Long> lastFlag;
    
    public static void onEnable() {
        TownyWar.cellsUnderAttack = new HashMap<Cell, CellUnderAttack>();
        TownyWar.cellsUnderAttackByPlayer = new HashMap<String, List<CellUnderAttack>>();
        TownyWar.lastFlag = new HashMap<Town, Long>();
    }
    
    public static void onDisable() {
        try {
            for (final CellUnderAttack cell : new ArrayList<CellUnderAttack>(TownyWar.cellsUnderAttack.values())) {
                attackCanceled(cell);
            }
        }
        catch (NullPointerException ex) {}
    }
    
    public static void registerAttack(final CellUnderAttack cell) throws Exception {
        final CellUnderAttack currentData = TownyWar.cellsUnderAttack.get(cell);
        if (currentData != null) {
            throw new Exception(String.format(TownySettings.getLangString("msg_err_enemy_war_cell_already_under_attack"), currentData.getNameOfFlagOwner()));
        }
        final String playerName = cell.getNameOfFlagOwner();
        final int futureActiveFlagCount = getNumActiveFlags(playerName) + 1;
        if (futureActiveFlagCount > TownyWarConfig.getMaxActiveFlagsPerPerson()) {
            throw new Exception(String.format(TownySettings.getLangString("msg_err_enemy_war_reached_max_active_flags"), TownyWarConfig.getMaxActiveFlagsPerPerson()));
        }
        addFlagToPlayerCount(playerName, cell);
        TownyWar.cellsUnderAttack.put(cell, cell);
        cell.begin();
    }
    
    public static int getNumActiveFlags(final String playerName) {
        final List<CellUnderAttack> activeFlags = TownyWar.cellsUnderAttackByPlayer.get(playerName);
        return (activeFlags == null) ? 0 : activeFlags.size();
    }
    
    public static List<CellUnderAttack> getCellsUnderAttack() {
        return new ArrayList<CellUnderAttack>(TownyWar.cellsUnderAttack.values());
    }
    
    public static List<CellUnderAttack> getCellsUnderAttack(final Town town) {
        final List<CellUnderAttack> cells = new ArrayList<CellUnderAttack>();
        for (final CellUnderAttack cua : TownyWar.cellsUnderAttack.values()) {
            try {
                final Town townUnderAttack = TownyAPI.getInstance().getTownBlock(cua.getFlagBaseBlock().getLocation()).getTown();
                if (townUnderAttack == null) {
                    continue;
                }
                if (townUnderAttack != town) {
                    continue;
                }
                cells.add(cua);
            }
            catch (NotRegisteredException ex) {}
        }
        return cells;
    }
    
    public static boolean isUnderAttack(final Town town) {
        for (final CellUnderAttack cua : TownyWar.cellsUnderAttack.values()) {
            try {
                final Town townUnderAttack = TownyAPI.getInstance().getTownBlock(cua.getFlagBaseBlock().getLocation()).getTown();
                if (townUnderAttack == null) {
                    continue;
                }
                if (townUnderAttack == town) {
                    return true;
                }
                continue;
            }
            catch (NotRegisteredException ex) {}
        }
        return false;
    }
    
    public static boolean isUnderAttack(final Cell cell) {
        return TownyWar.cellsUnderAttack.containsKey(cell);
    }
    
    public static CellUnderAttack getAttackData(final Cell cell) {
        return TownyWar.cellsUnderAttack.get(cell);
    }
    
    public static void removeCellUnderAttack(final CellUnderAttack cell) {
        removeFlagFromPlayerCount(cell.getNameOfFlagOwner(), cell);
        TownyWar.cellsUnderAttack.remove(cell);
    }
    
    public static void attackWon(final CellUnderAttack cell) {
        final CellWonEvent cellWonEvent = new CellWonEvent(cell);
        Bukkit.getServer().getPluginManager().callEvent((Event)cellWonEvent);
        cell.cancel();
        removeCellUnderAttack(cell);
    }
    
    public static void attackDefended(final Player player, final CellUnderAttack cell) {
        final CellDefendedEvent cellDefendedEvent = new CellDefendedEvent(player, cell);
        Bukkit.getServer().getPluginManager().callEvent((Event)cellDefendedEvent);
        cell.cancel();
        removeCellUnderAttack(cell);
    }
    
    public static void attackCanceled(final CellUnderAttack cell) {
        final CellAttackCanceledEvent cellAttackCanceledEvent = new CellAttackCanceledEvent(cell);
        Bukkit.getServer().getPluginManager().callEvent((Event)cellAttackCanceledEvent);
        cell.cancel();
        removeCellUnderAttack(cell);
    }
    
    public static void removeAttackerFlags(final String playerName) {
        final List<CellUnderAttack> cells = TownyWar.cellsUnderAttackByPlayer.get(playerName);
        if (cells != null) {
            for (final CellUnderAttack cell : cells) {
                attackCanceled(cell);
            }
        }
    }
    
    public static List<CellUnderAttack> getCellsUnderAttackByPlayer(final String playerName) {
        final List<CellUnderAttack> cells = TownyWar.cellsUnderAttackByPlayer.get(playerName);
        if (cells == null) {
            return null;
        }
        return new ArrayList<CellUnderAttack>(cells);
    }
    
    private static void addFlagToPlayerCount(final String playerName, final CellUnderAttack cell) {
        List<CellUnderAttack> activeFlags = getCellsUnderAttackByPlayer(playerName);
        if (activeFlags == null) {
            activeFlags = new ArrayList<CellUnderAttack>();
        }
        activeFlags.add(cell);
        TownyWar.cellsUnderAttackByPlayer.put(playerName, activeFlags);
    }
    
    private static void removeFlagFromPlayerCount(final String playerName, final Cell cell) {
        final List<CellUnderAttack> activeFlags = TownyWar.cellsUnderAttackByPlayer.get(playerName);
        if (activeFlags != null) {
            if (activeFlags.size() <= 1) {
                TownyWar.cellsUnderAttackByPlayer.remove(playerName);
            }
            else {
                activeFlags.remove(cell);
                TownyWar.cellsUnderAttackByPlayer.put(playerName, activeFlags);
            }
        }
    }
    
    public static void checkBlock(final Player player, final Block block, final Cancellable event) {
        if (TownyWarConfig.isAffectedMaterial(block.getType())) {
            final Cell cell = Cell.parse(block.getLocation());
            if (cell.isUnderAttack()) {
                final CellUnderAttack cellAttackData = cell.getAttackData();
                if (cellAttackData.isFlag(block)) {
                    attackDefended(player, cellAttackData);
                    event.setCancelled(true);
                }
                else if (cellAttackData.isUneditableBlock(block)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    public static boolean callAttackCellEvent(final Towny plugin, final Player player, final Block block, final WorldCoord worldCoord) throws TownyException {
        final int topY = block.getWorld().getHighestBlockYAt(block.getX(), block.getZ()) - 1;
        if (block.getY() < topY) {
            throw new TownyException(TownySettings.getLangString("msg_err_enemy_war_must_be_placed_above_ground"));
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Resident attackingResident;
        Town attackingTown;
        Nation attackingNation;
        try {
            attackingResident = townyUniverse.getDataSource().getResident(player.getName());
            attackingTown = attackingResident.getTown();
            attackingNation = attackingTown.getNation();
        }
        catch (NotRegisteredException e3) {
            throw new TownyException(TownySettings.getLangString("msg_err_dont_belong_nation"));
        }
        if (attackingTown.getTownBlocks().size() < 1) {
            throw new TownyException(TownySettings.getLangString("msg_err_enemy_war_your_town_has_no_claims"));
        }
        Town landOwnerTown;
        TownBlock townBlock;
        Nation landOwnerNation;
        try {
            landOwnerTown = worldCoord.getTownBlock().getTown();
            townBlock = worldCoord.getTownBlock();
            landOwnerNation = landOwnerTown.getNation();
        }
        catch (NotRegisteredException e3) {
            throw new TownyException(TownySettings.getLangString("msg_err_enemy_war_not_part_of_nation"));
        }
        if (landOwnerNation.isNeutral()) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_enemy_war_is_peaceful"), landOwnerNation.getFormattedName()));
        }
        if (!townyUniverse.getPermissionSource().isTownyAdmin(player) && attackingNation.isNeutral()) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_enemy_war_is_peaceful"), attackingNation.getFormattedName()));
        }
        checkIfTownHasMinOnlineForWar(landOwnerTown);
        checkIfNationHasMinOnlineForWar(landOwnerNation);
        checkIfTownHasMinOnlineForWar(attackingTown);
        checkIfNationHasMinOnlineForWar(attackingNation);
        if (TownyWarConfig.isAttackingBordersOnly() && !AreaSelectionUtil.isOnEdgeOfOwnership(landOwnerTown, worldCoord)) {
            throw new TownyException(TownySettings.getLangString("msg_err_enemy_war_not_on_edge_of_town"));
        }
        final double costToPlaceWarFlag = TownyWarConfig.getCostToPlaceWarFlag();
        if (TownySettings.isUsingEconomy()) {
            try {
                double requiredAmount = costToPlaceWarFlag;
                final double balance = attackingResident.getAccount().getHoldingBalance();
                if (balance < costToPlaceWarFlag) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_insuficient_funds_warflag"), TownyEconomyHandler.getFormattedBalance(costToPlaceWarFlag)));
                }
                final int activeFlagCount = getNumActiveFlags(attackingResident.getName());
                final double defendedAttackCost = TownyWarConfig.getDefendedAttackReward() * (activeFlagCount + 1);
                double attackWinCost = 0.0;
                double amount = TownyWarConfig.getWonHomeblockReward();
                final double homeBlockFine = (amount < 0.0) ? (-amount) : 0.0;
                amount = TownyWarConfig.getWonTownblockReward();
                final double townBlockFine = (amount < 0.0) ? (-amount) : 0.0;
                if (townBlock.isHomeBlock()) {
                    attackWinCost = homeBlockFine + activeFlagCount * townBlockFine;
                }
                else {
                    attackWinCost = (activeFlagCount + 1) * townBlockFine;
                }
                if (defendedAttackCost > 0.0 && attackWinCost > 0.0) {
                    double cost;
                    String reason;
                    if (defendedAttackCost > attackWinCost) {
                        requiredAmount += defendedAttackCost;
                        cost = defendedAttackCost;
                        reason = TownySettings.getLangString("name_defended_attack");
                    }
                    else {
                        requiredAmount += attackWinCost;
                        cost = attackWinCost;
                        reason = TownySettings.getLangString("name_rebuilding");
                    }
                    if (balance < requiredAmount) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_insuficient_funds_future"), TownyEconomyHandler.getFormattedBalance(cost), String.format("%d %s", activeFlagCount + 1, reason + "(s)")));
                    }
                }
            }
            catch (EconomyException e) {
                throw new TownyException(e.getError());
            }
        }
        final CellAttackEvent cellAttackEvent = new CellAttackEvent(plugin, player, block);
        plugin.getServer().getPluginManager().callEvent((Event)cellAttackEvent);
        if (!cellAttackEvent.isCancelled()) {
            if (TownySettings.isUsingEconomy() && costToPlaceWarFlag > 0.0) {
                try {
                    attackingResident.getAccount().pay(costToPlaceWarFlag, "War - WarFlag Cost");
                    TownyMessaging.sendResidentMessage(attackingResident, String.format(TownySettings.getLangString("msg_enemy_war_purchased_warflag"), TownyEconomyHandler.getFormattedBalance(costToPlaceWarFlag)));
                }
                catch (EconomyException e2) {
                    e2.printStackTrace();
                }
            }
            if (!landOwnerNation.hasEnemy(attackingNation)) {
                landOwnerNation.addEnemy(attackingNation);
                townyUniverse.getDataSource().saveNation(landOwnerNation);
            }
            townyUniverse.addWarZone(worldCoord);
            plugin.updateCache(worldCoord);
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_enemy_war_area_under_attack"), landOwnerTown.getFormattedName(), worldCoord.toString(), attackingResident.getFormattedName()));
            return true;
        }
        if (cellAttackEvent.hasReason()) {
            throw new TownyException(cellAttackEvent.getReason());
        }
        return false;
    }
    
    public static void checkIfTownHasMinOnlineForWar(final Town town) throws TownyException {
        final int requiredOnline = TownyWarConfig.getMinPlayersOnlineInTownForWar();
        final int onlinePlayerCount = TownyAPI.getInstance().getOnlinePlayers(town).size();
        if (onlinePlayerCount < requiredOnline) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_enemy_war_require_online"), requiredOnline, town.getFormattedName()));
        }
    }
    
    public static void checkIfNationHasMinOnlineForWar(final Nation nation) throws TownyException {
        final int requiredOnline = TownyWarConfig.getMinPlayersOnlineInNationForWar();
        final int onlinePlayerCount = TownyAPI.getInstance().getOnlinePlayers(nation).size();
        if (onlinePlayerCount < requiredOnline) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_enemy_war_require_online"), requiredOnline, nation.getFormattedName()));
        }
    }
    
    public static WorldCoord cellToWorldCoord(final Cell cell) throws NotRegisteredException {
        return new WorldCoord(cell.getWorldName(), cell.getX(), cell.getZ());
    }
    
    public static long lastFlagged(final Town town) {
        if (TownyWar.lastFlag.containsKey(town)) {
            return TownyWar.lastFlag.get(town);
        }
        return 0L;
    }
    
    public static void townFlagged(final Town town) {
        if (TownyWar.lastFlag.containsKey(town)) {
            TownyWar.lastFlag.replace(town, System.currentTimeMillis());
        }
        else {
            TownyWar.lastFlag.put(town, System.currentTimeMillis());
        }
    }
}
