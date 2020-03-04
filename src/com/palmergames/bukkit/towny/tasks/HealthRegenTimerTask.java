// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.attribute.Attribute;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.Server;

public class HealthRegenTimerTask extends TownyTimerTask
{
    private Server server;
    
    public HealthRegenTimerTask(final Towny plugin, final Server server) {
        super(plugin);
        this.server = server;
    }
    
    @Override
    public void run() {
        if (TownyAPI.getInstance().isWarTime()) {
            return;
        }
        for (final Player player : this.server.getOnlinePlayers()) {
            if (player.getHealth() <= 0.0) {
                continue;
            }
            final Coord coord = Coord.parseCoord((Entity)player);
            try {
                final TownyUniverse townyUniverse = TownyUniverse.getInstance();
                final TownyWorld world = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
                final TownBlock townBlock = world.getTownBlock(coord);
                if (!CombatUtil.isAlly(townBlock.getTown(), townyUniverse.getDataSource().getResident(player.getName()).getTown()) || townBlock.getType().equals(TownBlockType.ARENA)) {
                    continue;
                }
                this.incHealth(player);
            }
            catch (TownyException ex) {}
        }
    }
    
    public void incHealth(final Player player) {
        final float currentSat = player.getSaturation();
        if (currentSat == 0.0f) {
            player.setSaturation(1.0f);
        }
        double currentHP = player.getHealth();
        if (currentHP < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
            player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), ++currentHP));
            final EntityRegainHealthEvent event = new EntityRegainHealthEvent((Entity)player, currentHP, EntityRegainHealthEvent.RegainReason.REGEN);
            Bukkit.getServer().getPluginManager().callEvent((Event)event);
        }
    }
}
