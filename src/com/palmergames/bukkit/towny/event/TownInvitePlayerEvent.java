// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.inviteobjects.PlayerJoinTownInvite;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class TownInvitePlayerEvent extends Event
{
    private static final HandlerList handlers;
    private PlayerJoinTownInvite invite;
    
    public HandlerList getHandlers() {
        return TownInvitePlayerEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return TownInvitePlayerEvent.handlers;
    }
    
    public TownInvitePlayerEvent(final PlayerJoinTownInvite invite) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.invite = invite;
    }
    
    public PlayerJoinTownInvite getInvite() {
        return this.invite;
    }
    
    static {
        handlers = new HandlerList();
    }
}
