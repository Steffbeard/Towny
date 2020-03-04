// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar;

import java.util.ArrayList;
import java.util.List;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarBlockUtil;
import org.bukkit.potion.PotionEffectType;
import java.util.Map;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.war.siegewar.timeractions.AttackerWin;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.timeractions.DefenderWin;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarPointsUtil;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import java.util.Iterator;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.tasks.TownyTimerTask;

public class SiegeWarTimerTask extends TownyTimerTask
{
    public SiegeWarTimerTask(final Towny plugin) {
        super(plugin);
    }
    
    @Override
    public void run() {
        if (TownySettings.getWarSiegeEnabled()) {
            this.evaluateSiegeZones();
            this.evaluateSieges();
        }
    }
    
    private void evaluateSiegeZones() {
        final TownyUniverse universe = TownyUniverse.getInstance();
        for (final SiegeZone siegeZone : universe.getDataSource().getSiegeZones()) {
            evaluateSiegeZone(siegeZone);
        }
    }
    
    private void evaluateSieges() {
        for (final Siege siege : getAllSieges()) {
            evaluateSiege(siege);
        }
    }
    
    private static void evaluateSiegeZone(final SiegeZone siegeZone) {
        if (siegeZone.getSiege().getStatus() != SiegeStatus.IN_PROGRESS) {
            return;
        }
        final TownyUniverse universe = TownyUniverse.getInstance();
        boolean siegeZoneChanged = false;
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            try {
                final Resident resident = universe.getDataSource().getResident(player.getName());
                if (!resident.hasTown()) {
                    continue;
                }
                final Town residentTown = resident.getTown();
                if (resident.getTown().isOccupied()) {
                    continue;
                }
                if (residentTown == siegeZone.getDefendingTown() && universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode())) {
                    siegeZoneChanged |= evaluateSiegeZoneOccupant(player, siegeZone, siegeZone.getDefenderPlayerScoreTimeMap(), -TownySettings.getWarSiegePointsForDefenderOccupation());
                }
                else {
                    if (!residentTown.hasNation() || !universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
                        continue;
                    }
                    if (siegeZone.getDefendingTown().hasNation() && siegeZone.getDefendingTown().getNation() == residentTown.getNation()) {
                        siegeZoneChanged |= evaluateSiegeZoneOccupant(player, siegeZone, siegeZone.getDefenderPlayerScoreTimeMap(), -TownySettings.getWarSiegePointsForDefenderOccupation());
                    }
                    else if (siegeZone.getAttackingNation() == residentTown.getNation()) {
                        siegeZoneChanged |= evaluateSiegeZoneOccupant(player, siegeZone, siegeZone.getAttackerPlayerScoreTimeMap(), TownySettings.getWarSiegePointsForAttackerOccupation());
                    }
                    else if (siegeZone.getDefendingTown().hasNation() && siegeZone.getDefendingTown().getNation().hasMutualAlly(residentTown.getNation())) {
                        siegeZoneChanged |= evaluateSiegeZoneOccupant(player, siegeZone, siegeZone.getDefenderPlayerScoreTimeMap(), -TownySettings.getWarSiegePointsForDefenderOccupation());
                    }
                    else {
                        if (!siegeZone.getAttackingNation().hasMutualAlly(residentTown.getNation())) {
                            continue;
                        }
                        siegeZoneChanged |= evaluateSiegeZoneOccupant(player, siegeZone, siegeZone.getAttackerPlayerScoreTimeMap(), TownySettings.getWarSiegePointsForAttackerOccupation());
                    }
                }
            }
            catch (NotRegisteredException ex) {}
        }
        if (siegeZoneChanged) {
            universe.getDataSource().saveSiegeZone(siegeZone);
        }
    }
    
    private static void evaluateSiege(final Siege siege) {
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (siege.getStatus() == SiegeStatus.IN_PROGRESS) {
            if (System.currentTimeMillis() > siege.getScheduledEndTime()) {
                final TownyObject siegeWinner = SiegeWarPointsUtil.calculateSiegeWinner(siege);
                if (siegeWinner instanceof Town) {
                    DefenderWin.defenderWin(siege, (Town)siegeWinner);
                }
                else {
                    AttackerWin.attackerWin(siege, (Nation)siegeWinner);
                }
                final TownyUniverse townyUniverse = TownyUniverse.getInstance();
                townyUniverse.getDataSource().saveTown(siege.getDefendingTown());
            }
        }
        else if (System.currentTimeMillis() > siege.getDefendingTown().getSiegeImmunityEndTime()) {
            universe.getDataSource().removeSiege(siege);
        }
    }
    
    private static boolean evaluateSiegeZoneOccupant(final Player player, final SiegeZone siegeZone, final Map<Player, Long> playerScoreTimeMap, final int siegePointsForZoneOccupation) {
        if (playerScoreTimeMap.containsKey(player)) {
            if (!SiegeWarPointsUtil.isPlayerInSiegePointZone(player, siegeZone)) {
                playerScoreTimeMap.remove(player);
                siegeZone.getPlayerAfkTimeMap().remove(player);
                return false;
            }
            if (player.isDead()) {
                playerScoreTimeMap.remove(player);
                siegeZone.getPlayerAfkTimeMap().remove(player);
                return false;
            }
            if (player.isFlying() || player.getPotionEffect(PotionEffectType.INVISIBILITY) != null) {
                playerScoreTimeMap.remove(player);
                siegeZone.getPlayerAfkTimeMap().remove(player);
                return false;
            }
            if (SiegeWarBlockUtil.doesPlayerHaveANonAirBlockAboveThem(player)) {
                playerScoreTimeMap.remove(player);
                siegeZone.getPlayerAfkTimeMap().remove(player);
                return false;
            }
            if (System.currentTimeMillis() > siegeZone.getPlayerAfkTimeMap().get(player)) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_siege_war_cannot_occupy_zone_for_too_long"));
                playerScoreTimeMap.remove(player);
                return false;
            }
            siegeZone.adjustSiegePoints(siegePointsForZoneOccupation);
            return true;
        }
        else {
            if (!SiegeWarPointsUtil.isPlayerInSiegePointZone(player, siegeZone)) {
                return false;
            }
            if (player.isDead()) {
                return false;
            }
            if (player.isFlying() || player.getPotionEffect(PotionEffectType.INVISIBILITY) != null) {
                return false;
            }
            if (SiegeWarBlockUtil.doesPlayerHaveANonAirBlockAboveThem(player)) {
                return false;
            }
            playerScoreTimeMap.put(player, 0L);
            siegeZone.getPlayerAfkTimeMap().put(player, System.currentTimeMillis() + (long)(TownySettings.getWarSiegeZoneMaximumScoringDurationMinutes() * 60000.0));
            return false;
        }
    }
    
    private static List<Siege> getAllSieges() {
        final List<Siege> result = new ArrayList<Siege>();
        for (final Town town : TownyUniverse.getInstance().getDataSource().getTowns()) {
            if (town.hasSiege()) {
                result.add(town.getSiege());
            }
        }
        return result;
    }
}
