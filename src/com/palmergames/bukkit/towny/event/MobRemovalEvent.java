// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class MobRemovalEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    private Entity entity;
    
    public MobRemovalEvent(final Entity entity) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.cancelled = false;
        this.entity = entity;
    }
    
    public HandlerList getHandlers() {
        return MobRemovalEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return MobRemovalEvent.handlers;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean isCancelled) {
        this.cancelled = isCancelled;
    }
    
    public Entity getEntity() {
        return this.entity;
    }
    
    public EntityType getEntityType() {
        return this.entity.getType();
    }
    
    static {
        handlers = new HandlerList();
    }
}
