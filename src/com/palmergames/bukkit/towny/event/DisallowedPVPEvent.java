// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class DisallowedPVPEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    private final Player attacker;
    private final Player defender;
    
    public DisallowedPVPEvent(final Player attacker, final Player defender) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.cancelled = false;
        this.attacker = attacker;
        this.defender = defender;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public Player getAttacker() {
        return this.attacker;
    }
    
    public Player getDefender() {
        return this.defender;
    }
    
    public HandlerList getHandlers() {
        return DisallowedPVPEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return DisallowedPVPEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
