// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.utils;

import java.util.Iterator;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;

public class SiegeWarSiegeCompletionUtil
{
    public static void updateSiegeValuesToComplete(final Siege siege, final SiegeStatus siegeStatus, final Nation winnerNation) {
        siege.setStatus(siegeStatus);
        siege.setActualEndTime(System.currentTimeMillis());
        siege.setAttackerWinner(winnerNation);
        SiegeWarTimeUtil.activateSiegeImmunityTimer(siege.getDefendingTown(), siege);
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.getDataSource().saveTown(siege.getDefendingTown());
        for (final SiegeZone siegeZone : siege.getSiegeZones().values()) {
            townyUniverse.getDataSource().saveSiegeZone(siegeZone);
        }
    }
}
