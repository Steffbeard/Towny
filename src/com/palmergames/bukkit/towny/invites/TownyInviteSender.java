// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.invites;

import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import java.util.List;

public interface TownyInviteSender
{
    String getName();
    
    List<Invite> getSentInvites();
    
    void newSentInvite(final Invite p0) throws TooManyInvitesException;
    
    void deleteSentInvite(final Invite p0);
}
