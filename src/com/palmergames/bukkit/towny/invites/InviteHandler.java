// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.invites;

import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import java.io.InvalidObjectException;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;

public class InviteHandler
{
    private static Towny plugin;
    private static List<Invite> activeInvites;
    
    public static void initialize(final Towny plugin) {
        InviteHandler.plugin = plugin;
    }
    
    public static void acceptInvite(final Invite invite) throws InvalidObjectException, TownyException {
        if (InviteHandler.activeInvites.contains(invite)) {
            invite.accept();
            InviteHandler.activeInvites.remove(invite);
            return;
        }
        throw new InvalidObjectException("Invite not valid!");
    }
    
    public static void declineInvite(final Invite invite, final boolean fromSender) throws InvalidObjectException {
        if (InviteHandler.activeInvites.contains(invite)) {
            invite.decline(fromSender);
            InviteHandler.activeInvites.remove(invite);
            return;
        }
        throw new InvalidObjectException("Invite not valid!");
    }
    
    public static void addInvite(final Invite invite) {
        InviteHandler.activeInvites.add(invite);
    }
    
    public static List<Invite> getActiveInvites() {
        return InviteHandler.activeInvites;
    }
    
    public static boolean inviteIsActive(final Invite invite) {
        for (final Invite activeInvite : InviteHandler.activeInvites) {
            if (activeInvite.getReceiver().equals(invite.getReceiver()) && activeInvite.getSender().equals(invite.getSender())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean inviteIsActive(final TownyInviteSender sender, final TownyInviteReceiver receiver) {
        for (final Invite activeInvite : InviteHandler.activeInvites) {
            if (activeInvite.getReceiver().equals(receiver) && activeInvite.getSender().equals(sender)) {
                return true;
            }
        }
        return false;
    }
    
    public static int getReceivedInvitesAmount(final TownyInviteReceiver receiver) {
        final List<Invite> invites = receiver.getReceivedInvites();
        return invites.size();
    }
    
    public static int getSentInvitesAmount(final TownyInviteSender sender) {
        final List<Invite> invites = sender.getSentInvites();
        return invites.size();
    }
    
    public static int getSentAllyRequestsAmount(final TownyAllySender sender) {
        final List<Invite> invites = sender.getSentAllyInvites();
        return invites.size();
    }
    
    public static int getSentAllyRequestsMaxAmount(final TownyAllySender sender) {
        int amount = 0;
        if (sender instanceof Nation) {
            if (TownySettings.getMaximumRequestsSentNation() == 0) {
                amount = 100;
            }
            else {
                amount = TownySettings.getMaximumRequestsSentNation();
            }
        }
        return amount;
    }
    
    public static int getReceivedInvitesMaxAmount(final TownyInviteReceiver receiver) {
        int amount = 0;
        if (receiver instanceof Resident) {
            if (TownySettings.getMaximumInvitesReceivedResident() == 0) {
                amount = 100;
            }
            else {
                amount = TownySettings.getMaximumInvitesReceivedResident();
            }
        }
        if (receiver instanceof Town) {
            if (TownySettings.getMaximumInvitesReceivedTown() == 0) {
                amount = 100;
            }
            else {
                amount = TownySettings.getMaximumInvitesReceivedTown();
            }
        }
        if (receiver instanceof Nation) {
            if (TownySettings.getMaximumRequestsReceivedNation() == 0) {
                amount = 100;
            }
            else {
                amount = TownySettings.getMaximumRequestsReceivedNation();
            }
        }
        return amount;
    }
    
    public static int getSentInvitesMaxAmount(final TownyInviteSender sender) {
        int amount = 0;
        if (sender instanceof Town) {
            if (TownySettings.getMaximumInvitesSentTown() == 0) {
                amount = 100;
            }
            else {
                amount = TownySettings.getMaximumInvitesSentTown();
            }
        }
        if (sender instanceof Nation) {
            if (TownySettings.getMaximumInvitesSentNation() == 0) {
                amount = 100;
            }
            else {
                amount = TownySettings.getMaximumInvitesSentNation();
            }
        }
        return amount;
    }
    
    static {
        InviteHandler.activeInvites = new ArrayList<Invite>();
    }
}
