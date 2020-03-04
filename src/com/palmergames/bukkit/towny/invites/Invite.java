// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.invites;

import com.palmergames.bukkit.towny.exceptions.TownyException;

public interface Invite
{
    String getDirectSender();
    
    TownyInviteReceiver getReceiver();
    
    TownyInviteSender getSender();
    
    void accept() throws TownyException;
    
    void decline(final boolean p0);
}
