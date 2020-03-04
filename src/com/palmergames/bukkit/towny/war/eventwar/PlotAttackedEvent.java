// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.eventwar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashSet;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class PlotAttackedEvent extends Event
{
    private static final HandlerList handlers;
    private TownBlock townBlock;
    private HashSet<Player> players;
    private int hp;
    
    public HandlerList getHandlers() {
        return PlotAttackedEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlotAttackedEvent.handlers;
    }
    
    public PlotAttackedEvent(final TownBlock townBlock, final HashSet<Player> players, final int hp) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.townBlock = townBlock;
        this.players = players;
        this.hp = hp;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    public HashSet<Player> getPlayers() {
        return this.players;
    }
    
    public int getHP() {
        return this.hp;
    }
    
    static {
        handlers = new HandlerList();
    }
}
