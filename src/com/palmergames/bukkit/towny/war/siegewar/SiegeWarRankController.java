// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar;

import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarPointsUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.object.Resident;

public class SiegeWarRankController
{
    public static void evaluateTownRemoveRank(final Resident resident, final String rank) {
        if (TownyPerms.getTownRank(rank).contains(PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode())) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_resident_town_rank_removed"));
        }
    }
    
    public static void evaluateNationRemoveRank(final Resident resident, final String rank) {
        if (TownyPerms.getNationRank(rank).contains(PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_resident_nation_rank_removed"));
        }
    }
}
