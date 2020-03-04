// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NewTownEvent extends Event
{
    private static final HandlerList handlers;
    private Town town;
    
    public HandlerList getHandlers() {
        return NewTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NewTownEvent.handlers;
    }
    
    public NewTownEvent(final Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    static {
        handlers = new HandlerList();
    }
}
