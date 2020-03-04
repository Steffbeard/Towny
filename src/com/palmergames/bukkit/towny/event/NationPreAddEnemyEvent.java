// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class NationPreAddEnemyEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    private String enemyName;
    private Nation enemy;
    private String nationName;
    private Nation nation;
    private String cancelMessage;
    
    public HandlerList getHandlers() {
        return NationPreAddEnemyEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationPreAddEnemyEvent.handlers;
    }
    
    public NationPreAddEnemyEvent(final Nation nation, final Nation enemy) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.cancelled = false;
        this.cancelMessage = "Sorry this event was cancelled";
        this.enemyName = enemy.getName();
        this.enemy = enemy;
        this.nation = nation;
        this.nationName = nation.getName();
    }
    
    public String getEnemyName() {
        return this.enemyName;
    }
    
    public String getNationName() {
        return this.nationName;
    }
    
    public Nation getEnemy() {
        return this.enemy;
    }
    
    public Nation getNation() {
        return this.nation;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public String getCancelMessage() {
        return this.cancelMessage;
    }
    
    public void setCancelMessage(final String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
    
    static {
        handlers = new HandlerList();
    }
}
