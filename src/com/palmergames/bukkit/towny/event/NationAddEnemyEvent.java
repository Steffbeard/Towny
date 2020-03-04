// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NationAddEnemyEvent extends Event
{
    private static HandlerList handlers;
    private Nation enemy;
    private Nation nation;
    
    public HandlerList getHandlers() {
        return NationAddEnemyEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationAddEnemyEvent.handlers;
    }
    
    public NationAddEnemyEvent(final Nation nation, final Nation enemy) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.enemy = enemy;
        this.nation = nation;
    }
    
    public Nation getNation() {
        return this.nation;
    }
    
    public Nation getEnemy() {
        return this.enemy;
    }
    
    static {
        NationAddEnemyEvent.handlers = new HandlerList();
    }
}
