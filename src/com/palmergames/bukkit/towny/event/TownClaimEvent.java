// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownClaimEvent extends Event
{
    private static final HandlerList handlers;
    private TownBlock townBlock;
    
    public HandlerList getHandlers() {
        return TownClaimEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownClaimEvent.handlers;
    }
    
    public TownClaimEvent(final TownBlock townBlock) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.townBlock = townBlock;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    static {
        handlers = new HandlerList();
    }
}
