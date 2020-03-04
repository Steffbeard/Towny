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

public class EventWarStartEvent extends Event
{
    private static final HandlerList handlers;
    private List<Town> warringTowns;
    private List<Nation> warringNations;
    private double warSpoils;
    
    public HandlerList getHandlers() {
        return EventWarStartEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return EventWarStartEvent.handlers;
    }
    
    public EventWarStartEvent(final List<Town> warringTowns, final List<Nation> warringNations, final double warSpoils) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.warringTowns = new ArrayList<Town>();
        this.warringNations = new ArrayList<Nation>();
        this.warringNations = warringNations;
        this.warringTowns = warringTowns;
        this.warSpoils = warSpoils;
    }
    
    public List<Town> getWarringTowns() {
        return this.warringTowns;
    }
    
    public List<Nation> getWarringNations() {
        return this.warringNations;
    }
    
    public double getWarSpoils() {
        return this.warSpoils;
    }
    
    static {
        handlers = new HandlerList();
    }
}
