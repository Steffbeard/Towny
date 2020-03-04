// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar;

import com.palmergames.bukkit.towny.object.Nation;
import java.util.Iterator;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarPointsUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Resident;

public class SiegeWarMembershipController
{
    public static void evaluateTownRemoveResident(final Resident resident) {
        SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_resident_leave_town"));
    }
    
    public static void evaluateNationRemoveTown(final Town town) {
        for (final Resident resident : town.getResidents()) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_town_leave_nation"));
        }
    }
    
    public static void evaluateNationRemoveAlly(final Nation nation, final Nation ally) {
        for (final Resident resident : nation.getResidents()) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_ally_removed"));
        }
        for (final Resident resident : ally.getResidents()) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_ally_removed"));
        }
    }
    
    public static void evaluateNationsFormNewAlliance(final Nation nation, final Nation ally) {
        for (final Resident resident : nation.getResidents()) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_new_alliance_formed"));
        }
        for (final Resident resident : ally.getResidents()) {
            SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, TownySettings.getLangString("msg_siege_war_new_alliance_formed"));
        }
    }
}
