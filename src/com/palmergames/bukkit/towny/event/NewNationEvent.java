// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NewNationEvent extends Event
{
    private static final HandlerList handlers;
    private Nation nation;
    
    public HandlerList getHandlers() {
        return NewNationEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NewNationEvent.handlers;
    }
    
    public NewNationEvent(final Nation nation) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.nation = nation;
    }
    
    public Nation getNation() {
        return this.nation;
    }
    
    static {
        handlers = new HandlerList();
    }
}
