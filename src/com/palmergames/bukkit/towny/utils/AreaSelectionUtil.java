// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import java.util.HashSet;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayList;
import java.util.List;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.TownBlockOwner;

public class AreaSelectionUtil
{
    public static List<WorldCoord> selectWorldCoordArea(final TownBlockOwner owner, final WorldCoord pos, final String[] args) throws TownyException {
        List<WorldCoord> out = new ArrayList<WorldCoord>();
        if (args.length == 0) {
            if (!pos.getTownyWorld().isClaimable()) {
                throw new TownyException(TownySettings.getLangString("msg_not_claimable"));
            }
            out.add(pos);
        }
        else if (args.length > 1) {
            if (args[0].equalsIgnoreCase("rect")) {
                out = selectWorldCoordAreaRect(owner, pos, StringMgmt.remFirstArg(args));
            }
            else {
                if (!args[0].equalsIgnoreCase("circle")) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_property"), StringMgmt.join(args, " ")));
                }
                out = selectWorldCoordAreaCircle(owner, pos, StringMgmt.remFirstArg(args));
            }
        }
        else if (args[0].equalsIgnoreCase("auto")) {
            out = selectWorldCoordAreaRect(owner, pos, args);
        }
        else if (args[0].equalsIgnoreCase("outpost")) {
            final TownBlock tb = pos.getTownBlock();
            if (!tb.isOutpost() && tb.hasTown()) {
                final Town town = tb.getTown();
                if (!TownyUniverse.getInstance().isTownBlockLocContainedInTownOutposts(town.getAllOutpostSpawns(), tb)) {
                    throw new TownyException(TownySettings.getLangString("msg_err_unclaim_not_outpost"));
                }
                tb.setOutpost(true);
                out.add(pos);
            }
            if (tb.isOutpost()) {
                out.add(pos);
            }
        }
        else {
            try {
                Integer.parseInt(args[0]);
                out = selectWorldCoordAreaRect(owner, pos, args);
            }
            catch (NumberFormatException e) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_property"), args[0]));
            }
        }
        return out;
    }
    
    public static List<WorldCoord> selectWorldCoordAreaRect(final TownBlockOwner owner, final WorldCoord pos, final String[] args) throws TownyException {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        if (pos.getTownyWorld().isClaimable()) {
            if (args.length <= 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_invalid_radius"));
            }
            int r = 0;
            int available = 1000;
            if (owner instanceof Town) {
                final Town town = (Town)owner;
                available = TownySettings.getMaxTownBlocks(town) - town.getTownBlocks().size();
            }
            else if (owner instanceof Resident) {
                available = TownySettings.getMaxResidentPlots((Resident)owner);
            }
            if (args[0].equalsIgnoreCase("auto")) {
                while (available - Math.pow((r + 1) * 2 - 1, 2.0) >= 0.0) {
                    ++r;
                }
            }
            else {
                try {
                    r = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e) {
                    throw new TownyException(TownySettings.getLangString("msg_err_invalid_radius"));
                }
            }
            if (r > TownySettings.getMaxClaimRadiusValue() && TownySettings.getMaxClaimRadiusValue() > 0) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_radius_number"), TownySettings.getMaxClaimRadiusValue()));
            }
            if (r > 1000) {
                r = 1000;
            }
            for (int z = -r; z <= r; ++z) {
                for (int x = -r; x <= r; ++x) {
                    if (out.size() < available) {
                        out.add(new WorldCoord(pos.getWorldName(), pos.getX() + x, pos.getZ() + z));
                    }
                }
            }
        }
        return out;
    }
    
    public static List<WorldCoord> selectWorldCoordAreaCircle(final TownBlockOwner owner, final WorldCoord pos, final String[] args) throws TownyException {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        if (pos.getTownyWorld().isClaimable()) {
            if (args.length <= 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_invalid_radius"));
            }
            int r = 0;
            int available = 0;
            if (owner instanceof Town) {
                final Town town = (Town)owner;
                available = TownySettings.getMaxTownBlocks(town) - town.getTownBlocks().size();
            }
            else if (owner instanceof Resident) {
                available = TownySettings.getMaxResidentPlots((Resident)owner);
            }
            if (args[0].equalsIgnoreCase("auto")) {
                if (available > 0) {
                    while (available - Math.ceil(3.141592653589793 * r * r) >= 0.0) {
                        ++r;
                    }
                }
            }
            else {
                try {
                    r = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e) {
                    throw new TownyException(TownySettings.getLangString("msg_err_invalid_radius"));
                }
            }
            if (r > TownySettings.getMaxClaimRadiusValue() && TownySettings.getMaxClaimRadiusValue() > 0) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_radius_number"), TownySettings.getMaxClaimRadiusValue()));
            }
            if (r > 1000) {
                r = 1000;
            }
            for (int z = -r; z <= r; ++z) {
                for (int x = -r; x <= r; ++x) {
                    if (x * x + z * z <= r * r && out.size() < available) {
                        out.add(new WorldCoord(pos.getWorldName(), pos.getX() + x, pos.getZ() + z));
                    }
                }
            }
        }
        return out;
    }
    
    public static List<WorldCoord> filterInvalidProximityTownBlocks(final List<WorldCoord> selection, final Town town) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (worldCoord.getTownyWorld().getMinDistanceFromOtherTownsPlots(worldCoord, town) >= TownySettings.getMinDistanceFromTownPlotblocks()) {
                    out.add(worldCoord);
                }
                else {
                    TownyMessaging.sendDebugMsg("AreaSelectionUtil:filterInvalidProximity - Coord: " + worldCoord + " too close to another town.");
                }
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static List<WorldCoord> filterTownOwnedBlocks(final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (worldCoord.getTownBlock().hasTown()) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException e) {
                out.add(worldCoord);
            }
        }
        return out;
    }
    
    public static List<WorldCoord> filterWildernessBlocks(final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (!worldCoord.getTownBlock().hasTown()) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static List<WorldCoord> filterOwnedBlocks(final TownBlockOwner owner, final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (!worldCoord.getTownBlock().isOwner(owner)) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static List<WorldCoord> filterPlotsByGroup(final PlotObjectGroup group, final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                final TownBlock townBlock = worldCoord.getTownBlock();
                if (!townBlock.hasPlotObjectGroup() || !townBlock.getPlotObjectGroup().equals(group)) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static HashSet<PlotObjectGroup> getPlotGroupsFromSelection(final List<WorldCoord> selection) {
        final HashSet<PlotObjectGroup> seenGroups = new HashSet<PlotObjectGroup>();
        for (final WorldCoord coord : selection) {
            PlotObjectGroup group = null;
            try {
                group = coord.getTownBlock().getPlotObjectGroup();
            }
            catch (NotRegisteredException ex) {}
            if (seenGroups.contains(group)) {
                continue;
            }
            seenGroups.add(group);
        }
        return seenGroups;
    }
    
    public static List<WorldCoord> filterPlotsForSale(final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (!worldCoord.getTownBlock().isForSale()) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static List<WorldCoord> filterPlotsNotForSale(final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (!worldCoord.getTownBlock().isForSale()) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static List<WorldCoord> filterUnownedPlots(final List<WorldCoord> selection) {
        final List<WorldCoord> out = new ArrayList<WorldCoord>();
        for (final WorldCoord worldCoord : selection) {
            try {
                if (worldCoord.getTownBlock().getPlotPrice() <= -1.0) {
                    continue;
                }
                out.add(worldCoord);
            }
            catch (NotRegisteredException ex) {}
        }
        return out;
    }
    
    public static int getAreaSelectPivot(final String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equalsIgnoreCase("within")) {
                return i;
            }
        }
        return -1;
    }
    
    public static boolean isOnEdgeOfOwnership(final TownBlockOwner owner, final WorldCoord worldCoord) {
        final int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int i = 0; i < 4; ++i) {
            try {
                final TownBlock edgeTownBlock = worldCoord.getTownyWorld().getTownBlock(new Coord(worldCoord.getX() + offset[i][0], worldCoord.getZ() + offset[i][1]));
                if (!edgeTownBlock.isOwner(owner)) {
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
