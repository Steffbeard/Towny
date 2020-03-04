// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class RenameNationEvent extends Event
{
    private static final HandlerList handlers;
    private String oldName;
    private Nation nation;
    
    public HandlerList getHandlers() {
        return RenameNationEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return RenameNationEvent.handlers;
    }
    
    public RenameNationEvent(final String oldName, final Nation nation) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.oldName = oldName;
        this.nation = nation;
    }
    
    public String getOldName() {
        return this.oldName;
    }
    
    public Nation getNation() {
        return this.nation;
    }
    
    static {
        handlers = new HandlerList();
    }
}
