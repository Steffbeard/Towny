// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NewDayEvent extends Event
{
    private static final HandlerList handlers;
    private final List<String> fallenTowns;
    private final List<String> fallenNations;
    private final double townUpkeepCollected;
    private final double nationUpkeepCollected;
    private final long time;
    
    public NewDayEvent(final List<String> fallenTowns, final List<String> fallenNations, final double townUpkeepCollected, final double nationUpkeepCollected, final long time) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.fallenTowns = fallenTowns;
        this.fallenNations = fallenNations;
        this.townUpkeepCollected = townUpkeepCollected;
        this.nationUpkeepCollected = nationUpkeepCollected;
        this.time = time;
    }
    
    public HandlerList getHandlers() {
        return NewDayEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NewDayEvent.handlers;
    }
    
    public List<String> getFallenTowns() {
        return this.fallenTowns;
    }
    
    public List<String> getFallenNations() {
        return this.fallenNations;
    }
    
    public double getTownUpkeepCollected() {
        return this.townUpkeepCollected;
    }
    
    public double getNationUpkeepCollected() {
        return this.nationUpkeepCollected;
    }
    
    public long getTime() {
        return this.time;
    }
    
    static {
        handlers = new HandlerList();
    }
}
