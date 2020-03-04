// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import java.util.Iterator;
import org.bukkit.event.Cancellable;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.Listener;

public class TownyWarEntityListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(final EntityExplodeEvent event) {
        for (final Block block : event.blockList()) {
            TownyWar.checkBlock(null, block, (Cancellable)event);
        }
    }
}
