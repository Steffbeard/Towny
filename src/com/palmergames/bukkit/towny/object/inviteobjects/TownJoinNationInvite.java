// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.inviteobjects;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import java.util.List;
import com.palmergames.bukkit.towny.command.NationCommand;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.invites.TownyInviteSender;
import com.palmergames.bukkit.towny.invites.TownyInviteReceiver;
import com.palmergames.bukkit.towny.invites.Invite;

public class TownJoinNationInvite implements Invite
{
    private String directsender;
    private TownyInviteReceiver receiver;
    private TownyInviteSender sender;
    
    public TownJoinNationInvite(final String directsender, final TownyInviteSender sender, final TownyInviteReceiver receiver) {
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
        final Town town = (Town)this.getReceiver();
        final List<Town> towns = new ArrayList<Town>();
        towns.add(town);
        final Nation nation = (Nation)this.getSender();
        NationCommand.nationAdd(nation, towns);
        town.deleteReceivedInvite(this);
        nation.deleteSentInvite(this);
    }
    
    @Override
    public void decline(final boolean fromSender) {
        final Town town = (Town)this.getReceiver();
        final Nation nation = (Nation)this.getSender();
        town.deleteReceivedInvite(this);
        nation.deleteSentInvite(this);
        if (!fromSender) {
            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_deny_invite"), town.getName()));
        }
        else {
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("nation_revoke_invite"), nation.getName()));
        }
    }
}
