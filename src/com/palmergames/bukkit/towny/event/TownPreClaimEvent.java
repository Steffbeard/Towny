// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class TownPreClaimEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private TownBlock townBlock;
    private Town town;
    private Player player;
    private boolean isCancelled;
    
    public HandlerList getHandlers() {
        return TownPreClaimEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownPreClaimEvent.handlers;
    }
    
    public TownPreClaimEvent(final Town _town, final TownBlock _townBlock, final Player _player) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.isCancelled = false;
        this.town = _town;
        this.townBlock = _townBlock;
        this.player = _player;
    }
    
    public boolean isCancelled() {
        return this.isCancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    public TownBlock getTownBlock() {
        return this.townBlock;
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    static {
        handlers = new HandlerList();
    }
}
