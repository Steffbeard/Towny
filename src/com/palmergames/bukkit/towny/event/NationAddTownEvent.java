// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NationAddTownEvent extends Event
{
    private static final HandlerList handlers;
    private Town town;
    private Nation nation;
    
    public HandlerList getHandlers() {
        return NationAddTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationAddTownEvent.handlers;
    }
    
    public NationAddTownEvent(final Town town, final Nation nation) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
        this.nation = nation;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public Nation getNation() {
        return this.nation;
    }
    
    static {
        handlers = new HandlerList();
    }
}
