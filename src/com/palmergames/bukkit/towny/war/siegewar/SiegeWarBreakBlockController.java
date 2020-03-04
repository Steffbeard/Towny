// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarBlockUtil;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SiegeWarBreakBlockController
{
    public static boolean evaluateSiegeWarBreakBlockRequest(final Player player, final Block block, final BlockBreakEvent event) {
        if (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block)) {
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_siege_war_cannot_destroy_siege_banner"));
            return true;
        }
        return false;
    }
}
