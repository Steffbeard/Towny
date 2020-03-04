// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.inviteobjects;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.command.TownCommand;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.invites.TownyInviteSender;
import com.palmergames.bukkit.towny.invites.TownyInviteReceiver;
import com.palmergames.bukkit.towny.invites.Invite;

public class PlayerJoinTownInvite implements Invite
{
    private String directsender;
    private TownyInviteReceiver receiver;
    private TownyInviteSender sender;
    
    public PlayerJoinTownInvite(final String directsender, final TownyInviteSender sender, final TownyInviteReceiver receiver) {
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
        final Resident resident = (Resident)this.getReceiver();
        final Town town = (Town)this.getSender();
        TownCommand.townAddResident(town, resident);
        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_join_town"), resident.getName()));
        resident.deleteReceivedInvite(this);
        town.deleteSentInvite(this);
    }
    
    @Override
    public void decline(final boolean fromSender) {
        final Resident resident = (Resident)this.getReceiver();
        final Town town = (Town)this.getSender();
        resident.deleteReceivedInvite(this);
        town.deleteSentInvite(this);
        if (!fromSender) {
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_deny_invite"), resident.getName()));
            TownyMessaging.sendMsg(this.getReceiver(), TownySettings.getLangString("successful_deny"));
        }
        else {
            TownyMessaging.sendMsg(resident, String.format(TownySettings.getLangString("town_revoke_invite"), town.getName()));
        }
    }
}
