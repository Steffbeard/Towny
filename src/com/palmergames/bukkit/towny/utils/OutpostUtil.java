// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class OutpostUtil
{
    public static boolean OutpostTests(final Town town, final Resident resident, final TownyWorld world, final Coord key, final boolean isAdmin, final boolean isPlotSetOutpost) throws TownyException {
        if (TownySettings.isOutpostsLimitedByLevels() && town.getMaxOutpostSpawn() >= town.getOutpostLimit()) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_enough_outposts_free_to_claim"), town.getMaxOutpostSpawn(), town.getOutpostLimit()));
        }
        if (TownySettings.getAmountOfResidentsForOutpost() != 0 && town.getResidents().size() < TownySettings.getAmountOfResidentsForOutpost()) {
            throw new TownyException(TownySettings.getLangString("msg_err_not_enough_residents"));
        }
        final int maxOutposts = TownySettings.getMaxResidentOutposts(resident);
        if (!isAdmin && maxOutposts != -1 && maxOutposts <= resident.getTown().getAllOutpostSpawns().size()) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_max_outposts_own"), maxOutposts));
        }
        if (world.getMinDistanceFromOtherTowns(key) < TownySettings.getMinDistanceFromTownHomeblocks()) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("homeblock")));
        }
        if (!isPlotSetOutpost) {
            if (world.getMinDistanceFromOtherTownsPlots(key) < TownySettings.getMinDistanceFromTownPlotblocks()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("townblock")));
            }
            if (world.getMinDistanceFromOtherTownsPlots(key) < TownySettings.getMinDistanceForOutpostsFromPlot()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("townblock")));
            }
        }
        else {
            if (world.getMinDistanceFromOtherTownsPlots(key, town) < TownySettings.getMinDistanceFromTownPlotblocks()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("townblock")));
            }
            if (world.getMinDistanceFromOtherTownsPlots(key, town) < TownySettings.getMinDistanceForOutpostsFromPlot()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("townblock")));
            }
        }
        return true;
    }
}
