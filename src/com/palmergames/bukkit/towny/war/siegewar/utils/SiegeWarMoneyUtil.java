// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.utils;

import com.palmergames.bukkit.towny.object.Town;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;

public class SiegeWarMoneyUtil
{
    public static void giveWarChestsToWinnerNation(final Siege siege, final Nation winnerNation) {
        if (TownySettings.isUsingEconomy()) {
            try {
                for (final SiegeZone siegeZone : siege.getSiegeZones().values()) {
                    winnerNation.getAccount().collect(siegeZone.getWarChestAmount(), "War Chest Captured/Returned");
                    final String message = String.format(TownySettings.getLangString("msg_siege_war_attack_recover_war_chest"), TownyFormatter.getFormattedNationName(winnerNation), TownyEconomyHandler.getFormattedBalance(siegeZone.getWarChestAmount()));
                    TownyMessaging.sendPrefixedNationMessage(winnerNation, message);
                    if (winnerNation != siegeZone.getAttackingNation()) {
                        TownyMessaging.sendPrefixedNationMessage(siegeZone.getAttackingNation(), message);
                    }
                    TownyMessaging.sendPrefixedTownMessage(siege.getDefendingTown(), message);
                }
            }
            catch (EconomyException e) {
                System.out.println("Problem paying war chest(s) to winner nation");
                e.printStackTrace();
            }
        }
    }
    
    public static void giveWarChestsToWinnerTown(final Siege siege, final Town winnerTown) {
        for (final SiegeZone siegeZone : siege.getSiegeZones().values()) {
            giveOneWarChestToWinnerTown(siegeZone, winnerTown);
        }
    }
    
    public static void giveOneWarChestToWinnerTown(final SiegeZone siegeZone, final Town winnerTown) {
        if (TownySettings.isUsingEconomy()) {
            try {
                winnerTown.getAccount().collect(siegeZone.getWarChestAmount(), "War Chest Captured");
                final String message = String.format(TownySettings.getLangString("msg_siege_war_attack_recover_war_chest"), TownyFormatter.getFormattedTownName(winnerTown), TownyEconomyHandler.getFormattedBalance(siegeZone.getWarChestAmount()));
                TownyMessaging.sendPrefixedNationMessage(siegeZone.getAttackingNation(), message);
                TownyMessaging.sendPrefixedTownMessage(winnerTown, message);
            }
            catch (EconomyException e) {
                System.out.println("Problem paying war chest(s) to winner town");
                e.printStackTrace();
            }
        }
    }
}
