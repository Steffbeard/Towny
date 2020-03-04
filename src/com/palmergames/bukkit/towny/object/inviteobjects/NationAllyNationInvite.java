// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.inviteobjects;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.invites.TownyInviteSender;
import com.palmergames.bukkit.towny.invites.TownyInviteReceiver;
import com.palmergames.bukkit.towny.invites.Invite;

public class NationAllyNationInvite implements Invite
{
    private String directsender;
    private TownyInviteReceiver receiver;
    private TownyInviteSender sender;
    
    public NationAllyNationInvite(final String directsender, final TownyInviteSender sender, final TownyInviteReceiver receiver) {
        this.directsender = directsender;
        this.sender = sender;
        this.receiver = receiver;
    }
    
    @Override
    public String getDirectSender() {
        return this.directsender;
    }
    
    @Override
    public TownyInviteReceiver getReceiver() {
        return this.receiver;
    }
    
    @Override
    public TownyInviteSender getSender() {
        return this.sender;
    }
    
    @Override
    public void accept() throws TownyException {
        final Nation receivernation = (Nation)this.getReceiver();
        final Nation sendernation = (Nation)this.getSender();
        receivernation.addAlly(sendernation);
        sendernation.addAlly(receivernation);
        TownyMessaging.sendPrefixedNationMessage(receivernation, String.format(TownySettings.getLangString("msg_added_ally"), sendernation.getName()));
        TownyMessaging.sendPrefixedNationMessage(sendernation, String.format(TownySettings.getLangString("msg_accept_ally"), receivernation.getName()));
        receivernation.deleteReceivedInvite(this);
        sendernation.deleteSentAllyInvite(this);
        TownyUniverse.getInstance().getDataSource().saveNation(receivernation);
        TownyUniverse.getInstance().getDataSource().saveNation(sendernation);
    }
    
    @Override
    public void decline(final boolean fromSender) {
        final Nation receivernation = (Nation)this.getReceiver();
        final Nation sendernation = (Nation)this.getSender();
        receivernation.deleteReceivedInvite(this);
        sendernation.deleteSentAllyInvite(this);
        if (!fromSender) {
            TownyMessaging.sendPrefixedNationMessage(sendernation, String.format(TownySettings.getLangString("msg_deny_ally"), TownySettings.getLangString("nation_sing") + ": " + receivernation.getName()));
        }
        else {
            TownyMessaging.sendPrefixedNationMessage(receivernation, String.format(TownySettings.getLangString("nation_revoke_ally"), sendernation.getName()));
        }
    }
}
