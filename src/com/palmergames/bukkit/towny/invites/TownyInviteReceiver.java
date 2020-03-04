// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.invites;

import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import java.util.List;

public interface TownyInviteReceiver
{
    String getName();
    
    List<Invite> getReceivedInvites();
    
    void newReceivedInvite(final Invite p0) throws TooManyInvitesException;
    
    void deleteReceivedInvite(final Invite p0);
}
