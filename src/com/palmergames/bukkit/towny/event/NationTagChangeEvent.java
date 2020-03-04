// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NationTagChangeEvent extends Event
{
    private static final HandlerList handlers;
    private String newTag;
    
    public HandlerList getHandlers() {
        return NationTagChangeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationTagChangeEvent.handlers;
    }
    
    public NationTagChangeEvent(final String newTag) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.newTag = newTag;
    }
    
    public String getNewTag() {
        return this.newTag;
    }
    
    static {
        handlers = new HandlerList();
    }
}
