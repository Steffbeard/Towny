// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.eventwar;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownScoredEvent extends Event
{
    private static final HandlerList handlers;
    private Town town;
    private int score;
    
    public HandlerList getHandlers() {
        return TownScoredEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownScoredEvent.handlers;
    }
    
    public TownScoredEvent(final Town town, final int score) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
        this.score = score;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public int getScore() {
        return this.score;
    }
    
    static {
        handlers = new HandlerList();
    }
}
