// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.timeractions;

import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;

public class DefenderWin
{
    public static void defenderWin(final Siege siege, final Town winnerTown) {
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_WIN, null);
        TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_defender_win"), TownyFormatter.getFormattedTownName(winnerTown)));
        SiegeWarMoneyUtil.giveWarChestsToWinnerTown(siege, winnerTown);
    }
}
