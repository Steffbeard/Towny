// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class EventWarEndEvent extends Event
{
    private static final HandlerList handlers;
    private List<Town> warringTowns;
    private List<Nation> warringNations;
    private Town winningTown;
    private double townWinnings;
    private double nationWinnings;
    
    public HandlerList getHandlers() {
        return EventWarEndEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return EventWarEndEvent.handlers;
    }
    
    public EventWarEndEvent(final List<Town> warringTowns, final Town winningTown, final double townWinnings, final List<Nation> warringNations, final double nationWinnings) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.warringTowns = new ArrayList<Town>();
        this.warringNations = new ArrayList<Nation>();
        this.warringNations = warringNations;
        this.warringTowns = warringTowns;
        this.winningTown = winningTown;
        this.townWinnings = townWinnings;
        this.nationWinnings = nationWinnings;
    }
    
    public List<Nation> getWarringNations() {
        return this.warringNations;
    }
    
    public List<Town> getWarringTowns() {
        return this.warringTowns;
    }
    
    public Town getWinningTown() {
        return this.winningTown;
    }
    
    public double getTownWinnings() {
        return this.townWinnings;
    }
    
    public double getNationWinnings() {
        return this.nationWinnings;
    }
    
    static {
        handlers = new HandlerList();
    }
}
