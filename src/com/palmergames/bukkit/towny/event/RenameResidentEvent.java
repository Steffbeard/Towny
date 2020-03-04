// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class RenameResidentEvent extends Event
{
    private static final HandlerList handlers;
    private String oldName;
    private Resident resident;
    
    public HandlerList getHandlers() {
        return RenameResidentEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return RenameResidentEvent.handlers;
    }
    
    public RenameResidentEvent(final String oldName, final Resident resident) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.oldName = oldName;
        this.resident = resident;
    }
    
    public String getOldName() {
        return this.oldName;
    }
    
    public Resident getResident() {
        return this.resident;
    }
    
    static {
        handlers = new HandlerList();
    }
}
