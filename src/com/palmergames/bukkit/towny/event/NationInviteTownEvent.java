// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.inviteobjects.TownJoinNationInvite;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NationInviteTownEvent extends Event
{
    private static final HandlerList handlers;
    private TownJoinNationInvite invite;
    
    public HandlerList getHandlers() {
        return NationInviteTownEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationInviteTownEvent.handlers;
    }
    
    public NationInviteTownEvent(final TownJoinNationInvite invite) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.invite = invite;
    }
    
    public TownJoinNationInvite getInvite() {
        return this.invite;
    }
    
    static {
        handlers = new HandlerList();
    }
}
