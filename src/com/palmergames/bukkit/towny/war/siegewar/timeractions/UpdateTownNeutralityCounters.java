// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.timeractions;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ListIterator;
import java.util.List;
import java.util.Collection;
import com.palmergames.bukkit.towny.object.Town;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyUniverse;

public class UpdateTownNeutralityCounters
{
    public static void updateTownNeutralityCounters() {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Town> towns = new ArrayList<Town>(townyUniverse.getDataSource().getTowns());
        final ListIterator<Town> townItr = towns.listIterator();
        while (townItr.hasNext()) {
            final Town town = townItr.next();
            if (townyUniverse.getDataSource().hasTown(town.getName()) && !town.isRuined()) {
                updateTownNeutralityCounter(town);
            }
        }
    }
    
    public static void updateTownNeutralityCounter(final Town town) {
        if (town.getNeutralityChangeConfirmationCounterDays() != 0) {
            town.decrementNeutralityChangeConfirmationCounterDays();
            if (town.getNeutralityChangeConfirmationCounterDays() == 0) {
                town.flipNeutral();
                if (town.isNeutral()) {
                    TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_town_became_neutral"), TownyFormatter.getFormattedTownName(town)));
                }
                else {
                    TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_town_became_non_neutral"), TownyFormatter.getFormattedTownName(town)));
                }
            }
        }
    }
}
