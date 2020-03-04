package com.palmergames.bukkit.towny.war.siegewar;

import com.palmergames.bukkit.towny.war.siegewar.playeractions.PlunderTown;
import com.palmergames.bukkit.towny.war.siegewar.playeractions.SurrenderTown;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import com.palmergames.bukkit.towny.war.siegewar.playeractions.AttackTown;
import com.palmergames.bukkit.towny.war.siegewar.playeractions.InvadeTown;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarBlockUtil;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZoneDistance;
import com.palmergames.bukkit.towny.war.siegewar.playeractions.AbandonAttack;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarDistanceUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.block.Banner;
import org.bukkit.Material;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SiegeWarPlaceBlockController
{
    public static boolean evaluateSiegeWarPlaceBlockRequest(final Player player, final Block block, final BlockPlaceEvent event, final Towny plugin) {
        try {
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
                case YELLOW_BANNER:
                case WHITE_BANNER: {
                    return evaluatePlaceBanner(player, block, event, plugin);
                }
                case CHEST:
                case TRAPPED_CHEST: {
                    return evaluatePlaceChest(player, block, event);
                }
                default: {
                    return false;
                }
            }
        }
        catch (NotRegisteredException e) {
            TownyMessaging.sendErrorMsg(player, "Problem placing siege related block");
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean evaluatePlaceBanner(final Player player, final Block block, final BlockPlaceEvent event, final Towny plugin) throws NotRegisteredException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final TownyWorld townyWorld = townyUniverse.getDataSource().getWorld(block.getWorld().getName());
        final Coord blockCoord = Coord.parseCoord(block);
        if (townyWorld.hasTownBlock(blockCoord)) {
            return block.getType() == Material.WHITE_BANNER && ((Banner)block.getState()).getPatterns().size() == 0 && evaluatePlaceWhiteBannerInTown(player, blockCoord, event, townyWorld);
        }
        if (block.getType() == Material.WHITE_BANNER && ((Banner)block.getState()).getPatterns().size() == 0) {
            return evaluatePlaceWhiteBannerInWilderness(block, player, event);
        }
        return evaluatePlaceColouredBannerInWilderness(block, player, event, plugin);
    }
    
    private static boolean evaluatePlaceWhiteBannerInWilderness(final Block block, final Player player, final BlockPlaceEvent event) {
        if (!TownySettings.getWarSiegeAbandonEnabled()) {
            return false;
        }
        final SiegeZoneDistance nearestSiegeZoneDistance = SiegeWarDistanceUtil.findNearestSiegeZoneDistance(block);
        if (nearestSiegeZoneDistance == null || nearestSiegeZoneDistance.getDistance() > TownySettings.getTownBlockSize()) {
            return false;
        }
        AbandonAttack.processAbandonSiegeRequest(player, nearestSiegeZoneDistance.getSiegeZone(), event);
        return true;
    }
    
    private static boolean evaluatePlaceColouredBannerInWilderness(final Block block, final Player player, final BlockPlaceEvent event, final Towny plugin) {
        final List<TownBlock> nearbyTownBlocks = SiegeWarBlockUtil.getAdjacentTownBlocks(player, block);
        if (nearbyTownBlocks.size() == 0) {
            return false;
        }
        if (nearbyTownBlocks.size() > 1) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_siege_war_too_many_town_blocks_nearby"));
            event.setBuild(false);
            event.setCancelled(true);
            return true;
        }
        Town town = null;
        if (nearbyTownBlocks.get(0).hasTown()) {
            Label_0096: {
                try {
                    town = nearbyTownBlocks.get(0).getTown();
                    break Label_0096;
                }
                catch (NotRegisteredException e) {
                    return false;
                }
                return false;
            }
            final TownyUniverse universe = TownyUniverse.getInstance();
            try {
                final Resident resident = universe.getDataSource().getResident(player.getName());
                if (!resident.hasTown()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_siege_war_action_not_a_town_member"));
                }
                final Town townOfResident = resident.getTown();
                if (!townOfResident.hasNation()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_siege_war_action_not_a_nation_member"));
                }
                final Nation nationOfResident = townOfResident.getNation();
                if (town.hasSiege() && town.getSiege().getSiegeZones().containsKey(nationOfResident)) {
                    if (!TownySettings.getWarSiegeInvadeEnabled()) {
                        return false;
                    }
                    InvadeTown.processInvadeTownRequest(plugin, player, town, event);
                }
                else {
                    if (!TownySettings.getWarSiegeAttackEnabled()) {
                        return false;
                    }
                    if (SiegeWarBlockUtil.isSupportBlockUnstable(block)) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_siege_war_banner_support_block_not_stable"));
                        event.setBuild(false);
                        event.setCancelled(true);
                        return true;
                    }
                    AttackTown.processAttackTownRequest(player, block, nearbyTownBlocks.get(0), town, event);
                }
            }
            catch (TownyException x) {
                event.setBuild(false);
                event.setCancelled(true);
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return true;
            }
            return true;
        }
        return false;
    }
    
    private static boolean evaluatePlaceWhiteBannerInTown(final Player player, final Coord blockCoord, final BlockPlaceEvent event, final TownyWorld townyWorld) throws NotRegisteredException {
        if (!TownySettings.getWarSiegeSurrenderEnabled()) {
            return false;
        }
        TownBlock townBlock = null;
        if (townyWorld.hasTownBlock(blockCoord)) {
            townBlock = townyWorld.getTownBlock(blockCoord);
        }
        if (townBlock == null) {
            return false;
        }
        if (!townBlock.hasTown()) {
            return false;
        }
        final Town town = townBlock.getTown();
        if (!town.hasSiege()) {
            return false;
        }
        SurrenderTown.processTownSurrenderRequest(player, town, event);
        return true;
    }
    
    private static boolean evaluatePlaceChest(final Player player, final Block block, final BlockPlaceEvent event) throws NotRegisteredException {
        if (!TownySettings.getWarSiegePlunderEnabled()) {
            return false;
        }
        final TownyWorld townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(block.getWorld().getName());
        final Coord blockCoord = Coord.parseCoord(block);
        if (townyWorld.hasTownBlock(blockCoord)) {
            return false;
        }
        final List<TownBlock> nearbyTownBlocks = SiegeWarBlockUtil.getAdjacentTownBlocks(player, block);
        if (nearbyTownBlocks.size() == 0) {
            return false;
        }
        if (nearbyTownBlocks.size() > 1) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_siege_war_too_many_town_blocks_nearby"));
            event.setBuild(false);
            event.setCancelled(true);
            return true;
        }
        Town town = null;
        if (nearbyTownBlocks.get(0).hasTown()) {
            Label_0142: {
                try {
                    town = nearbyTownBlocks.get(0).getTown();
                    break Label_0142;
                }
                catch (NotRegisteredException e) {
                    return false;
                }
                return false;
            }
            if (town.hasSiege()) {
                PlunderTown.processPlunderTownRequest(player, town, event);
                return true;
            }
            return false;
        }
        return false;
    }
}
