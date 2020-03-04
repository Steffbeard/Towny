// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownAddResidentEvent extends Event
{
    private static final HandlerList handlers;
    private Resident resident;
    private Town town;
    
    public HandlerList getHandlers() {
        return TownAddResidentEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownAddResidentEvent.handlers;
    }
    
    public TownAddResidentEvent(final Resident resident, final Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.resident = resident;
        this.town = town;
    }
    
    public Resident getResident() {
        return this.resident;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    static {
        handlers = new HandlerList();
    }
}
