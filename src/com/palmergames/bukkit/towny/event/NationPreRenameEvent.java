// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class NationPreRenameEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private String oldName;
    private String newName;
    private Nation nation;
    private boolean isCancelled;
    
    public HandlerList getHandlers() {
        return NationPreRenameEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationPreRenameEvent.handlers;
    }
    
    public NationPreRenameEvent(final Nation nation, final String newName) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.oldName = nation.getName();
        this.nation = nation;
        this.newName = newName;
    }
    
    public String getOldName() {
        return this.oldName;
    }
    
    public String getNewName() {
        return this.newName;
    }
    
    public Nation getNation() {
        return this.nation;
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
