// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.utils;

import java.util.Iterator;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZoneDistance;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.block.Block;

public class SiegeWarDistanceUtil
{
    public static boolean isBannerToTownElevationDifferenceOk(final Block block, final TownBlock townBlock) {
        final int allowedDownwardElevationDifference = TownySettings.getWarSiegeMaxAllowedBannerToTownDownwardElevationDifference();
        final int averageDownwardElevationDifference = getAverageBlockToTownDownwardElevationDistance(block, townBlock);
        return averageDownwardElevationDifference <= allowedDownwardElevationDifference;
    }
    
    private static int getAverageBlockToTownDownwardElevationDistance(final Block block, final TownBlock townBlock) {
        final int blockElevation = block.getY();
        final Location topNorthWestCornerLocation = townBlock.getCoord().getTopNorthWestCornerLocation(block.getWorld());
        final int townBlockSize = TownySettings.getTownBlockSize();
        final Location[] surfaceCornerLocations = { SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation), SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation.add((double)townBlockSize, 0.0, 0.0)), SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation.add(0.0, 0.0, (double)townBlockSize)), SiegeWarBlockUtil.getSurfaceLocation(topNorthWestCornerLocation.add((double)townBlockSize, 0.0, (double)townBlockSize)) };
        int totalElevation = 0;
        for (final Location surfaceCornerLocation : surfaceCornerLocations) {
            totalElevation += surfaceCornerLocation.getBlockY();
        }
        final int averageTownElevation = totalElevation / 4;
        return blockElevation - averageTownElevation;
    }
    
    public static SiegeZoneDistance findNearestSiegeZoneDistance(final Block block) {
        SiegeZone nearestSiegeZone = null;
        double distanceToNearestSiegeZone = -1.0;
        for (final SiegeZone siegeZone : TownyUniverse.getInstance().getDataSource().getSiegeZones()) {
            if (nearestSiegeZone == null) {
                nearestSiegeZone = siegeZone;
                distanceToNearestSiegeZone = block.getLocation().distance(nearestSiegeZone.getFlagLocation());
            }
            else {
                final double distanceToNewTarget = block.getLocation().distance(siegeZone.getFlagLocation());
                if (distanceToNewTarget >= distanceToNearestSiegeZone) {
                    continue;
                }
                nearestSiegeZone = siegeZone;
                distanceToNearestSiegeZone = distanceToNewTarget;
            }
        }
        if (nearestSiegeZone == null) {
            return null;
        }
        return new SiegeZoneDistance(nearestSiegeZone, distanceToNearestSiegeZone);
    }
}
