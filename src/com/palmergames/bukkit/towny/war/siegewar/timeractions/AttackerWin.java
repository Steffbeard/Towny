// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.timeractions;

import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarMoneyUtil;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarSiegeCompletionUtil;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;

public class AttackerWin
{
    public static void attackerWin(final Siege siege, final Nation winnerNation) {
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.ATTACKER_WIN, winnerNation);
        TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_attacker_win"), TownyFormatter.getFormattedNationName(winnerNation), TownyFormatter.getFormattedTownName(siege.getDefendingTown())));
        SiegeWarMoneyUtil.giveWarChestsToWinnerNation(siege, winnerNation);
    }
}
