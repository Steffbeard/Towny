// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TownPreUnclaimEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private TownBlock townBlock;
    private Town town;
    private boolean isCancelled;
    
    public HandlerList getHandlers() {
        return TownPreUnclaimEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownPreUnclaimEvent.handlers;
    }
    
    public TownPreUnclaimEvent(final TownBlock _townBlock) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.townBlock = _townBlock;
        try {
            this.town = this.townBlock.getTown();
        }
        catch (NotRegisteredException ex) {}
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    static {
        handlers = new HandlerList();
    }
}
