// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.object.CellBorder;
import java.util.Iterator;
import java.util.Collection;
import com.palmergames.bukkit.util.DrawSmokeTaskFactory;
import com.palmergames.bukkit.towny.utils.BorderUtil;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.Towny;

public class DrawSmokeTask extends TownyTimerTask
{
    public DrawSmokeTask(final Towny plugin) {
        super(plugin);
    }
    
    @Override
    public void run() {
        final Collection<? extends Player> players = BukkitTools.getOnlinePlayers();
        for (final Player player : players) {
            if (this.plugin.hasPlayerMode(player, "constantplotborder")) {
                final WorldCoord wc = new WorldCoord(player.getWorld().getName(), Coord.parseCoord(player.getLocation()));
                final CellBorder cellBorder = BorderUtil.getPlotBorder(wc);
                cellBorder.runBorderedOnSurface(1, 2, DrawSmokeTaskFactory.sendToPlayer(player));
            }
        }
    }
}
