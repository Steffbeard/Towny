// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.invites;

import java.util.List;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;

public interface TownyAllySender
{
    String getName();
    
    void newSentAllyInvite(final Invite p0) throws TooManyInvitesException;
    
    void deleteSentAllyInvite(final Invite p0);
    
    List<Invite> getSentAllyInvites();
}
