// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.ArrayList;
import java.io.InvalidObjectException;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.invites.TownyInviteReceiver;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.Iterator;
import com.palmergames.bukkit.util.Colors;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class InviteCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> invite_help;
    
    public InviteCommand(final Towny instance) {
        InviteCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (command.getName().equalsIgnoreCase("invite")) {
                this.parseInviteCommand(player, args);
            }
        }
        else {
            for (final String line : InviteCommand.invite_help) {
                sender.sendMessage(Colors.strip(line));
            }
        }
        return true;
    }
    
    private void parseInviteCommand(final Player player, final String[] split) {
        if (split.length == 0) {
            for (final String line : InviteCommand.invite_help) {
                player.sendMessage(line);
            }
        }
        else if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("?")) {
            for (final String line : InviteCommand.invite_help) {
                player.sendMessage(line);
            }
        }
        else if (split[0].equalsIgnoreCase("list")) {
            parseInviteList(player, split);
        }
        else if (split[0].equalsIgnoreCase(TownySettings.getAcceptCommand())) {
            parseAccept(player, StringMgmt.remFirstArg(split));
        }
        else if (split[0].equalsIgnoreCase(TownySettings.getDenyCommand())) {
            parseDeny(player, StringMgmt.remFirstArg(split));
        }
    }
    
    private static void parseInviteList(final Player player, final String[] split) {
        Resident resident;
        try {
            resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        final String received = TownySettings.getLangString("player_received_invites").replace("%a", Integer.toString(InviteHandler.getReceivedInvitesAmount(resident))).replace("%m", Integer.toString(InviteHandler.getReceivedInvitesMaxAmount(resident)));
        try {
            if (resident.getReceivedInvites().size() <= 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_player_no_invites"));
            }
            int page = 1;
            if (split != null && split.length >= 2) {
                try {
                    page = Integer.parseInt(split[1]);
                }
                catch (NumberFormatException ex) {}
            }
            sendInviteList(player, resident.getReceivedInvites(), page, false);
            player.sendMessage(received);
        }
        catch (TownyException x2) {
            TownyMessaging.sendErrorMsg(player, x2.getMessage());
        }
    }
    
    public static void parseDeny(final Player player, final String[] args) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Resident resident;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        final List<Invite> invites = resident.getReceivedInvites();
        if (invites.size() == 0) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_player_no_invites"));
            return;
        }
        Town town = null;
        Label_0143: {
            if (args.length >= 1) {
                try {
                    town = townyUniverse.getDataSource().getTown(args[0]);
                    break Label_0143;
                }
                catch (NotRegisteredException e2) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                    return;
                }
            }
            if (invites.size() != 1) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_player_has_multiple_invites"));
                parseInviteList(player, null);
                return;
            }
            town = (Town)invites.get(0).getSender();
        }
        Invite toDecline = null;
        for (final Invite invite : InviteHandler.getActiveInvites()) {
            if (invite.getSender().equals(town) && invite.getReceiver().equals(resident)) {
                toDecline = invite;
                break;
            }
        }
        if (toDecline != null) {
            try {
                InviteHandler.declineInvite(toDecline, false);
            }
            catch (InvalidObjectException e) {
                e.printStackTrace();
            }
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
        }
    }
    
    public static void parseAccept(final Player player, final String[] args) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Resident resident;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        final List<Invite> invites = resident.getReceivedInvites();
        if (invites.size() == 0) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_player_no_invites"));
            return;
        }
        Town town = null;
        Label_0143: {
            if (args.length >= 1) {
                try {
                    town = townyUniverse.getDataSource().getTown(args[0]);
                    break Label_0143;
                }
                catch (NotRegisteredException e2) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                    return;
                }
            }
            if (invites.size() != 1) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_player_has_multiple_invites"));
                parseInviteList(player, null);
                return;
            }
            town = (Town)invites.get(0).getSender();
        }
        Invite toAccept = null;
        for (final Invite invite : InviteHandler.getActiveInvites()) {
            if (invite.getSender().equals(town) && invite.getReceiver().equals(resident)) {
                toAccept = invite;
                break;
            }
        }
		if (toAccept != null) {
			try {
				InviteHandler.acceptInvite(toAccept);
			} catch (TownyException | InvalidObjectException e) {
				e.printStackTrace();
            }
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
        }
    }
    
    public static void sendInviteList(final Player player, final List<Invite> list, final int page, final boolean fromSender) {
        if (page < 0) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative"));
            return;
        }
        if (page == 0) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
            return;
        }
        final int total = (int)Math.ceil(list.size() / 10.0);
        if (page > total) {
            return;
        }
        final List<String> invitesformatted = new ArrayList<String>();
        int iMax = page * 10;
        if (page * 10 > list.size()) {
            iMax = list.size();
        }
        String object = null;
        for (int i = (page - 1) * 10; i < iMax; ++i) {
            final Invite invite = list.get(i);
            String name = invite.getDirectSender();
            if (name == null) {
                name = "Console";
            }
            else {
                try {
                    name = TownyUniverse.getInstance().getDataSource().getResident(name).getName();
                }
                catch (NotRegisteredException e) {
                    name = "Unknown";
                }
            }
            String output = null;
            if (fromSender) {
                if (invite.getSender() instanceof Town) {
                    output = "§3" + ((Resident)invite.getReceiver()).getName() + "§8" + " - " + "§2" + name;
                    object = TownySettings.getLangString("player_sing");
                }
                if (invite.getSender() instanceof Nation) {
                    if (invite.getReceiver() instanceof Town) {
                        output = "§3" + ((Town)invite.getReceiver()).getName() + "§8" + " - " + "§2" + name;
                        object = TownySettings.getLangString("town_sing");
                    }
                    if (invite.getReceiver() instanceof Nation) {
                        output = "§3" + ((Nation)invite.getReceiver()).getName() + "§8" + " - " + "§2" + name;
                        object = TownySettings.getLangString("nation_sing");
                    }
                }
            }
            else {
                if (invite.getReceiver() instanceof Resident) {
                    output = "§3" + ((Town)invite.getSender()).getName() + "§8" + " - " + "§2" + name;
                    object = TownySettings.getLangString("town_sing");
                }
                if (invite.getReceiver() instanceof Town) {
                    output = "§3" + ((Nation)invite.getSender()).getName() + "§8" + " - " + "§2" + name;
                    object = TownySettings.getLangString("nation_sing");
                }
                if (invite.getReceiver() instanceof Nation) {
                    output = "§3" + ((Nation)invite.getSender()).getName() + "§8" + " - " + "§2" + name;
                    object = TownySettings.getLangString("nation_sing");
                }
            }
            invitesformatted.add(output);
        }
        player.sendMessage(ChatTools.formatList(TownySettings.getLangString("invite_plu"), "§3" + object + "§8" + " - " + "§b" + TownySettings.getLangString("invite_sent_by"), invitesformatted, TownySettings.getListPageMsg(page, total)));
    }
    
    static {
        (invite_help = new ArrayList<String>()).add(ChatTools.formatTitle("/invite"));
        InviteCommand.invite_help.add(ChatTools.formatCommand("", "/invite", TownySettings.getAcceptCommand() + " [town]", TownySettings.getLangString("invite_help_1")));
        InviteCommand.invite_help.add(ChatTools.formatCommand("", "/invite", TownySettings.getDenyCommand() + " [town]", TownySettings.getLangString("invite_help_2")));
        InviteCommand.invite_help.add(ChatTools.formatCommand("", "/invite", "list", TownySettings.getLangString("invite_help_3")));
    }
}
