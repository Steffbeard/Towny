package com.palmergames.bukkit.towny.war.siegewar.utils;

import org.bukkit.block.Banner;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.Location;
import java.util.Iterator;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SiegeWarBlockUtil
{
    public static List<TownBlock> getAdjacentTownBlocks(final Player player, final Block block) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<TownBlock> nearbyTownBlocks = new ArrayList<TownBlock>();
        TownyWorld townyWorld;
        try {
            townyWorld = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
        }
        catch (NotRegisteredException e) {
            return nearbyTownBlocks;
        }
        final List<Coord> nearbyCoOrdinates = new ArrayList<Coord>();
        final Coord blockCoordinate = Coord.parseCoord(block);
        nearbyCoOrdinates.add(blockCoordinate.add(0, -1));
        nearbyCoOrdinates.add(blockCoordinate.add(0, 1));
        nearbyCoOrdinates.add(blockCoordinate.add(1, 0));
        nearbyCoOrdinates.add(blockCoordinate.add(-1, 0));
        TownBlock nearbyTownBlock = null;
        for (final Coord nearbyCoord : nearbyCoOrdinates) {
            if (townyWorld.hasTownBlock(nearbyCoord)) {
                try {
                    nearbyTownBlock = townyWorld.getTownBlock(nearbyCoord);
                }
                catch (NotRegisteredException ex) {}
                if (!nearbyTownBlock.hasTown()) {
                    continue;
                }
                nearbyTownBlocks.add(nearbyTownBlock);
            }
        }
        return nearbyTownBlocks;
    }
    
    public static boolean doesPlayerHaveANonAirBlockAboveThem(final Player player) {
        return doesLocationHaveANonAirBlockAboveIt(player.getLocation());
    }
    
    public static boolean doesBlockHaveANonAirBlockAboveIt(final Block block) {
        return doesLocationHaveANonAirBlockAboveIt(block.getLocation());
    }
    
    private static boolean doesLocationHaveANonAirBlockAboveIt(final Location location) {
        location.add(0.0, 1.0, 0.0);
        while (location.getY() < 256.0) {
            if (location.getBlock().getType() != Material.AIR) {
                return true;
            }
            location.add(0.0, 1.0, 0.0);
        }
        return false;
    }
    
    public static boolean isBlockNearAnActiveSiegeBanner(final Block block) {
        if (isStandingColouredBanner(block) || isStandingColouredBanner(block.getRelative(BlockFace.UP))) {
            final Location locationOfBlock = block.getLocation();
            final Location locationOfBlockAbove = block.getRelative(BlockFace.UP).getLocation();
            final TownyUniverse universe = TownyUniverse.getInstance();
            for (final SiegeZone siegeZone : universe.getDataSource().getSiegeZones()) {
                if (siegeZone.getSiege().getStatus() != SiegeStatus.IN_PROGRESS) {
                    continue;
                }
                final Location locationOfSiegeBanner = siegeZone.getFlagLocation();
                if (locationOfBlock.equals((Object)locationOfSiegeBanner) || locationOfBlockAbove.equals((Object)locationOfSiegeBanner)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    static Location getSurfaceLocation(final Location topLocation) {
        topLocation.add(0.0, -1.0, 0.0);
        while (topLocation.getY() < 256.0) {
            if (topLocation.getBlock().getType() != Material.AIR) {
                return topLocation;
            }
            topLocation.add(0.0, -1.0, 0.0);
        }
        topLocation.setY(255.0);
        return topLocation;
    }
    
    private static boolean isStandingColouredBanner(final Block block) {
        switch (block.getType()) {
            case BLACK_BANNER:
            case BLUE_BANNER:
            case BROWN_BANNER:
            case CYAN_BANNER:
            case GRAY_BANNER:
            case GREEN_BANNER:
            case LIGHT_BLUE_BANNER:
            case LIGHT_GRAY_BANNER:
            case LIME_BANNER:
            case MAGENTA_BANNER:
            case ORANGE_BANNER:
            case PINK_BANNER:
            case PURPLE_BANNER:
            case RED_BANNER:
            case YELLOW_BANNER: {
                return true;
            }
            case WHITE_BANNER: {
                return ((Banner)block.getState()).getPatterns().size() > 0;
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean isSupportBlockUnstable(final Block block) {
        final Block blockBelowBanner = block.getRelative(BlockFace.DOWN);
        switch (blockBelowBanner.getType()) {
            case AIR:
            case GRAVEL:
            case SAND:
            case SOUL_SAND:
            case RED_SAND:
            case ACACIA_LOG:
            case BIRCH_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
            case OAK_LOG:
            case SPRUCE_LOG:
          
                return true;
            }
        }
    }
