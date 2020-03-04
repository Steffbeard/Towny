// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TownPreRenameEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private String oldName;
    private String newName;
    private Town town;
    private boolean isCancelled;
    
    public HandlerList getHandlers() {
        return TownPreRenameEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownPreRenameEvent.handlers;
    }
    
    public TownPreRenameEvent(final Town town, final String newName) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.oldName = town.getName();
        this.town = town;
        this.newName = newName;
    }
    
    public String getOldName() {
        return this.oldName;
    }
    
    public String getNewName() {
        return this.newName;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancel) {
        this.isCancelled = cancel;
    }
    
    static {
        handlers = new HandlerList();
    }
}
