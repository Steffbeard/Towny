// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class EventWarPreStartEvent extends Event
{
    private static final HandlerList handlers;
    private double warSpoils;
    
    public HandlerList getHandlers() {
        return EventWarPreStartEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return EventWarPreStartEvent.handlers;
    }
    
    public EventWarPreStartEvent() {
        super(!Bukkit.getServer().isPrimaryThread());
        this.warSpoils = 0.0;
    }
    
    public double getWarSpoils() {
        return this.warSpoils;
    }
    
    public void setWarSpoils(final double warSpoils) {
        this.warSpoils = warSpoils;
    }
    
    static {
        handlers = new HandlerList();
    }
}
