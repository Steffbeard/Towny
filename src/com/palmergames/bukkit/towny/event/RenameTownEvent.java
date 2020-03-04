// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class RenameTownEvent extends Event
{
    private static final HandlerList handlers;
    private String oldName;
    private Town town;
    
    public HandlerList getHandlers() {
        return RenameTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return RenameTownEvent.handlers;
    }
    
    public RenameTownEvent(final String oldName, final Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.oldName = oldName;
        this.town = town;
    }
    
    public String getOldName() {
        return this.oldName;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    static {
        handlers = new HandlerList();
    }
}
