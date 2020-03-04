// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.eventwar;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Hashtable;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.tasks.TownyTimerTask;

public class WarTimerTask extends TownyTimerTask
{
    private War warEvent;
    
    public WarTimerTask(final Towny plugin, final War warEvent) {
        super(plugin);
        this.warEvent = warEvent;
    }
    
    @Override
    public void run() {
        if (!this.warEvent.isWarTime()) {
            this.warEvent.end();
            TownyAPI.getInstance().clearWarEvent();
            this.plugin.resetCache();
            TownyMessaging.sendDebugMsg("War ended.");
            return;
        }
        int numPlayers = 0;
        final Hashtable<TownBlock, WarZoneData> plotList = new Hashtable<TownBlock, WarZoneData>();
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                ++numPlayers;
                TownyMessaging.sendDebugMsg("[War] " + player.getName() + ": ");
                try {
                    final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
                    if (!resident.hasNation()) {
                        continue;
                    }
                    final Nation nation = resident.getTown().getNation();
                    TownyMessaging.sendDebugMsg("[War]   hasNation");
                    if (nation.isNeutral()) {
                        if (!this.warEvent.isWarringNation(nation)) {
                            continue;
                        }
                        this.warEvent.nationLeave(nation);
                    }
                    else {
                        TownyMessaging.sendDebugMsg("[War]   notPeaceful");
                        if (!this.warEvent.isWarringNation(nation)) {
                            continue;
                        }
                        TownyMessaging.sendDebugMsg("[War]   warringNation");
                        final WorldCoord worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord((Entity)player));
                        final War warEvent = this.warEvent;
                        if (!War.isWarZone(worldCoord)) {
                            continue;
                        }
                        TownyMessaging.sendDebugMsg("[War]   warZone");
                        if (player.getLocation().getBlockY() < TownySettings.getMinWarHeight()) {
                            continue;
                        }
                        TownyMessaging.sendDebugMsg("[War]   aboveMinHeight");
                        final TownBlock townBlock = worldCoord.getTownBlock();
                        final boolean healablePlots = TownySettings.getPlotsHealableInWar();
                        if (healablePlots && (nation == townBlock.getTown().getNation() || townBlock.getTown().getNation().hasAlly(nation))) {
                            if (plotList.containsKey(townBlock)) {
                                plotList.get(townBlock).addDefender(player);
                            }
                            else {
                                final WarZoneData wzd = new WarZoneData();
                                wzd.addDefender(player);
                                plotList.put(townBlock, wzd);
                            }
                            TownyMessaging.sendDebugMsg("[War]   healed");
                        }
                        else {
                            if (!resident.getTown().getNation().hasEnemy(townBlock.getTown().getNation())) {
                                continue;
                            }
                            TownyMessaging.sendDebugMsg("[War]   notAlly");
                            if (resident.isJailed()) {
                                continue;
                            }
                            final boolean edgesOnly = TownySettings.getOnlyAttackEdgesInWar();
                            if (edgesOnly && !isOnEdgeOfTown(townBlock, worldCoord, this.warEvent)) {
                                continue;
                            }
                            if (edgesOnly) {
                                TownyMessaging.sendDebugMsg("[War]   onEdge");
                            }
                            if (plotList.containsKey(townBlock)) {
                                plotList.get(townBlock).addAttacker(player);
                            }
                            else {
                                final WarZoneData wzd2 = new WarZoneData();
                                wzd2.addAttacker(player);
                                plotList.put(townBlock, wzd2);
                            }
                            TownyMessaging.sendDebugMsg("[War]   damaged");
                        }
                    }
                }
                catch (NotRegisteredException ex) {}
            }
        }
        for (final TownBlock tb : plotList.keySet()) {
            try {
                this.warEvent.updateWarZone(tb, plotList.get(tb));
            }
            catch (NotRegisteredException e) {
                TownyMessaging.sendDebugMsg("[War]   WarZone Update Failed");
            }
        }
        TownyMessaging.sendDebugMsg("[War] # Players: " + numPlayers);
    }
    
    public static boolean isOnEdgeOfTown(final TownBlock townBlock, final WorldCoord worldCoord, final War warEvent) {
        final int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int i = 0; i < 4; ++i) {
            try {
                final TownBlock edgeTownBlock = worldCoord.getTownyWorld().getTownBlock(new Coord(worldCoord.getX() + offset[i][0], worldCoord.getZ() + offset[i][1]));
                final boolean sameTown = edgeTownBlock.getTown() == townBlock.getTown();
                if (!sameTown || (sameTown && !War.isWarZone(edgeTownBlock.getWorldCoord()))) {
                    return true;
                }
            }
            catch (NotRegisteredException e) {
                return true;
            }
        }
        return false;
    }
}
