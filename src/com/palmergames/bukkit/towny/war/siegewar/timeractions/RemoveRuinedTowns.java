// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.timeractions;

import java.util.ListIterator;
import java.util.List;
import java.util.Collection;
import com.palmergames.bukkit.towny.object.Town;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyUniverse;

public class RemoveRuinedTowns
{
    public static void removeRuinedTowns() {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Town> towns = new ArrayList<Town>(townyUniverse.getDataSource().getTowns());
        final ListIterator<Town> townItr = towns.listIterator();
        while (townItr.hasNext()) {
            final Town town = townItr.next();
            if (townyUniverse.getDataSource().hasTown(town.getName()) && town.isRuined()) {
                if (town.getRecentlyRuinedEndTime() != 999L) {
                    town.setRecentlyRuinedEndTime(999L);
                    townyUniverse.getDataSource().saveTown(town);
                }
                else {
                    townyUniverse.getDataSource().removeRuinedTown(town);
                }
            }
        }
    }
}
