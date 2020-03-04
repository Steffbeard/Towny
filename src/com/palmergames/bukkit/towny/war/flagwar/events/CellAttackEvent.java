// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar.events;

import com.palmergames.bukkit.towny.war.flagwar.CellUnderAttack;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class CellAttackEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private Towny plugin;
    private Player player;
    private Block flagBaseBlock;
    private boolean cancelled;
    private String reason;
    private long time;
    
    public HandlerList getHandlers() {
        return CellAttackEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return CellAttackEvent.handlers;
    }
    
    public CellAttackEvent(final Towny plugin, final Player player, final Block flagBaseBlock) {
        this.cancelled = false;
        this.reason = null;
        this.plugin = plugin;
        this.player = player;
        this.flagBaseBlock = flagBaseBlock;
        this.time = TownyWarConfig.getFlagWaitingTime();
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Block getFlagBaseBlock() {
        return this.flagBaseBlock;
    }
    
    public CellUnderAttack getData() {
        return new CellUnderAttack(this.plugin, this.player.getName(), this.flagBaseBlock, this.time);
    }
    
    public long getTime() {
        return this.time;
    }
    
    public void setTime(final long time) {
        this.time = time;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public String getReason() {
        return this.reason;
    }
    
    public void setReason(final String reason) {
        this.reason = reason;
    }
    
    public boolean hasReason() {
        return this.reason != null;
    }
    
    static {
        handlers = new HandlerList();
    }
}
