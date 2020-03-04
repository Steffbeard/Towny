// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.event;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.inviteobjects.NationAllyNationInvite;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class NationRequestAllyNationEvent extends Event
{
    private static final HandlerList handlers;
    private NationAllyNationInvite invite;
    
    public HandlerList getHandlers() {
        return NationRequestAllyNationEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return NationRequestAllyNationEvent.handlers;
    }
    
    public NationRequestAllyNationEvent(final NationAllyNationInvite invite) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.invite = invite;
    }
    
    public NationAllyNationInvite getInvite() {
        return this.invite;
    }
    
    static {
        handlers = new HandlerList();
    }
}
