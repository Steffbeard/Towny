// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownAddResidentRankEvent extends Event
{
    private static final HandlerList handlers;
    private Resident resident;
    private String rank;
    private Town town;
    
    public TownAddResidentRankEvent(final Resident resident, final String rank, final Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.resident = resident;
        this.rank = rank;
        this.town = town;
    }
    
    public Resident getResident() {
        return this.resident;
    }
    
    public String getRank() {
        return this.rank;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public HandlerList getHandlers() {
        return TownAddResidentRankEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownAddResidentRankEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
