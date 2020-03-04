// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Material;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ShopPlotUtil
{
    public static boolean doesPlayerOwnShopPlot(final Player player, final Location location) {
        boolean owner = false;
        try {
            owner = TownyAPI.getInstance().getTownBlock(location).getResident().equals(TownyAPI.getInstance().getDataSource().getResident(player.getName()));
        }
        catch (NotRegisteredException e) {
            return false;
        }
        catch (NullPointerException npe) {
            return false;
        }
        return owner && isShopPlot(location);
    }
    
    public static boolean doesPlayerHaveAbilityToEditShopPlot(final Player player, final Location location) {
        final boolean build = PlayerCacheUtil.getCachePermission(player, location, Material.DIRT, TownyPermission.ActionType.BUILD);
        return build && isShopPlot(location);
    }
    
    public static boolean isShopPlot(final Location location) {
        if (!TownyAPI.getInstance().isWilderness(location)) {
            final TownBlock townblock = TownyAPI.getInstance().getTownBlock(location);
            return isShopPlot(townblock);
        }
        return false;
    }
    
    public static boolean isShopPlot(final TownBlock townblock) {
        return townblock != null && townblock.getType().equals(TownBlockType.COMMERCIAL);
    }
}
