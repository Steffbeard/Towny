// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import org.apache.logging.log4j.LogManager;
import com.palmergames.bukkit.towny.invites.Invite;
import org.bukkit.ChatColor;
import com.palmergames.bukkit.towny.object.ResidentList;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.List;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.Iterator;
import com.palmergames.bukkit.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.util.ChatTools;
import org.apache.logging.log4j.Logger;

public class TownyMessaging
{
    private static final Logger LOGGER;
    private static final Logger LOGGER_DEBUG;
    
    public static void sendErrorMsg(final String msg) {
        TownyMessaging.LOGGER.warn(ChatTools.stripColour("[Towny] Error: " + msg));
    }
    
    public static void sendErrorMsg(final Object sender, final String msg) {
        boolean isPlayer = false;
        if (sender instanceof Player) {
            isPlayer = true;
        }
        if (sender == null) {
            System.out.print("Message called with null sender");
        }
        for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + "§c" + msg)) {
            if (isPlayer) {
                ((Player)sender).sendMessage(line);
            }
            else {
                ((CommandSender)sender).sendMessage(Colors.strip(line));
            }
        }
        sendDevMsg(msg);
    }
    
    public static void sendErrorMsg(final Object sender, final String[] msg) {
        boolean isPlayer = false;
        if (sender instanceof Player) {
            isPlayer = true;
        }
        for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + "§c" + msg)) {
            if (isPlayer) {
                ((Player)sender).sendMessage(line);
            }
            else {
                ((CommandSender)sender).sendMessage(Colors.strip(line));
            }
        }
        sendDevMsg(msg);
    }
    
    public static void sendMsg(final String msg) {
        TownyMessaging.LOGGER.info("[Towny] " + ChatTools.stripColour(msg));
    }
    
    public static void sendMsg(final Object sender, final String msg) {
        for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + "§2" + msg)) {
            if (sender instanceof Player) {
                ((Player)sender).sendMessage(line);
            }
            else if (sender instanceof CommandSender) {
                ((CommandSender)sender).sendMessage(Colors.strip(line));
            }
            else {
                if (!(sender instanceof Resident)) {
                    continue;
                }
                final Player p = TownyAPI.getInstance().getPlayer((Resident)sender);
                if (p == null) {
                    return;
                }
                p.sendMessage(Colors.strip(line));
            }
        }
        sendDevMsg(msg);
    }
    
    public static void sendMsg(final Player player, final String[] msg) {
        for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + "§2" + msg)) {
            player.sendMessage(line);
        }
    }
    
    public static void sendMsg(final Player player, final List<String> msg) {
        for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + "§2" + msg)) {
            player.sendMessage(line);
        }
    }
    
    public static void sendDevMsg(final String msg) {
        if (TownySettings.isDevMode()) {
            final Player townyDev = BukkitTools.getPlayer(TownySettings.getDevName());
            if (townyDev == null) {
                return;
            }
            for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + " DevMode: " + "§c" + msg)) {
                townyDev.sendMessage(line);
            }
        }
    }
    
    public static void sendDevMsg(final String[] msg) {
        if (TownySettings.isDevMode()) {
            final Player townyDev = BukkitTools.getPlayer(TownySettings.getDevName());
            if (townyDev == null) {
                return;
            }
            for (final String line : ChatTools.color(TownySettings.getLangString("default_towny_prefix") + " DevMode: " + "§c" + msg)) {
                townyDev.sendMessage(line);
            }
        }
    }
    
    public static void sendDebugMsg(final String msg) {
        if (TownySettings.getDebug()) {
            TownyMessaging.LOGGER_DEBUG.info(ChatTools.stripColour("[Towny] Debug: " + msg));
        }
        sendDevMsg(msg);
    }
    
    public static void sendMessage(final Object sender, final List<String> lines) {
        sendMessage(sender, lines.toArray(new String[0]));
    }
    
    public static void sendMessage(final Object sender, final String line) {
        if (sender instanceof Player) {
            ((Player)sender).sendMessage(line);
        }
        else if (sender instanceof CommandSender) {
            ((CommandSender)sender).sendMessage(line);
        }
        else if (sender instanceof Resident) {
            final Player p = TownyAPI.getInstance().getPlayer((Resident)sender);
            if (p == null) {
                return;
            }
            p.sendMessage(Colors.strip(line));
        }
    }
    
    public static void sendMessage(final Object sender, final String[] lines) {
        boolean isPlayer = false;
        if (sender instanceof Player) {
            isPlayer = true;
        }
        for (final String line : lines) {
            if (isPlayer) {
                ((Player)sender).sendMessage(line);
            }
            else if (sender instanceof CommandSender) {
                ((CommandSender)sender).sendMessage(line);
            }
            else if (sender instanceof Resident) {
                final Player p = TownyAPI.getInstance().getPlayer((Resident)sender);
                if (p == null) {
                    return;
                }
                p.sendMessage(Colors.strip(line));
            }
        }
    }
    
    @Deprecated
    public static void sendTownMessage(final Town town, final List<String> lines) {
        sendTownMessage(town, lines.toArray(new String[0]));
    }
    
    @Deprecated
    public static void sendNationMessage(final Nation nation, final List<String> lines) {
        sendNationMessage(nation, lines.toArray(new String[0]));
    }
    
    public static void sendGlobalMessage(final List<String> lines) {
        sendGlobalMessage(lines.toArray(new String[0]));
    }
    
    public static void sendGlobalMessage(final String[] lines) {
        for (final String line : lines) {
            TownyMessaging.LOGGER.info(ChatTools.stripColour("[Global Msg] " + line));
        }
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                for (final String line2 : lines) {
                    player.sendMessage(TownySettings.getLangString("default_towny_prefix") + line2);
                }
            }
        }
    }
    
    public static void sendGlobalMessage(final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Global Message] " + line));
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                try {
                    if (!TownyUniverse.getInstance().getDataSource().getWorld(player.getLocation().getWorld().getName()).isUsingTowny()) {
                        continue;
                    }
                    player.sendMessage(TownySettings.getLangString("default_towny_prefix") + line);
                }
                catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void sendPlainGlobalMessage(final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Global Message] " + line));
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                try {
                    if (!TownyUniverse.getInstance().getDataSource().getWorld(player.getLocation().getWorld().getName()).isUsingTowny()) {
                        continue;
                    }
                    player.sendMessage(line);
                }
                catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void sendResidentMessage(final Resident resident, final String line) throws TownyException {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Resident Msg] " + resident.getName() + ": " + line));
        final Player player = TownyAPI.getInstance().getPlayer(resident);
        if (player == null) {
            throw new TownyException("Player could not be found!");
        }
        player.sendMessage(TownySettings.getLangString("default_towny_prefix") + line);
    }
    
    @Deprecated
    public static void sendTownMessage(final Town town, final String[] lines) {
        for (final String line : lines) {
            TownyMessaging.LOGGER.info(ChatTools.stripColour("[Town Msg] " + town.getName() + ": " + line));
        }
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(town)) {
            for (final String line2 : lines) {
                player.sendMessage(line2);
            }
        }
    }
    
    @Deprecated
    public static void sendTownMessage(final Town town, final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Town Msg] " + town.getName() + ": " + line));
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(town)) {
            player.sendMessage(line);
        }
    }
    
    public static void sendTownMessagePrefixed(final Town town, final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour(line));
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(town)) {
            player.sendMessage(TownySettings.getLangString("default_towny_prefix") + line);
        }
    }
    
    public static void sendPrefixedTownMessage(final Town town, final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Town Msg] " + town.getName() + ": " + line));
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(town)) {
            player.sendMessage(String.format(TownySettings.getLangString("default_town_prefix"), town.getName()) + line);
        }
    }
    
    public static void sendPrefixedTownMessage(final Town town, final String[] lines) {
        for (final String line : lines) {
            TownyMessaging.LOGGER.info(ChatTools.stripColour(line));
        }
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(town)) {
            for (final String line2 : lines) {
                player.sendMessage(String.format(TownySettings.getLangString("default_town_prefix"), town.getName()) + line2);
            }
        }
    }
    
    public static void sendPrefixedTownMessage(final Town town, final List<String> lines) {
        sendPrefixedTownMessage(town, lines.toArray(new String[0]));
    }
    
    @Deprecated
    public static void sendNationMessage(final Nation nation, final String[] lines) {
        for (final String line : lines) {
            TownyMessaging.LOGGER.info(ChatTools.stripColour("[Nation Msg] " + nation.getName() + ": " + line));
        }
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            for (final String line2 : lines) {
                player.sendMessage(line2);
            }
        }
    }
    
    @Deprecated
    public static void sendNationMessage(final Nation nation, final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Nation Msg] " + nation.getName() + ": " + line));
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            player.sendMessage(line);
        }
    }
    
    public static void sendPrefixedNationMessage(final Nation nation, final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Nation Msg] " + nation.getName() + ": " + line));
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            player.sendMessage(String.format(TownySettings.getLangString("default_nation_prefix"), nation.getName()) + line);
        }
    }
    
    public static void sendPrefixedNationMessage(final Nation nation, final List<String> lines) {
        sendPrefixedNationMessage(nation, lines.toArray(new String[0]));
    }
    
    public static void sendPrefixedNationMessage(final Nation nation, final String[] lines) {
        for (final String line : lines) {
            TownyMessaging.LOGGER.info(ChatTools.stripColour("[Nation Msg] " + nation.getName() + ": " + line));
        }
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            for (final String line2 : lines) {
                player.sendMessage(String.format(TownySettings.getLangString("default_nation_prefix"), nation.getName()) + line2);
            }
        }
    }
    
    public static void sendNationMessagePrefixed(final Nation nation, final String line) {
        TownyMessaging.LOGGER.info(ChatTools.stripColour("[Nation Msg] " + nation.getName() + ": " + line));
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            player.sendMessage(TownySettings.getLangString("default_towny_prefix") + line);
        }
    }
    
    public static void sendNationMessagePrefixed(final Nation nation, final List<String> lines) {
        for (final String line : lines) {
            TownyMessaging.LOGGER.info(ChatTools.stripColour("[Nation Msg] " + nation.getName() + ": " + line));
        }
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            for (final String line2 : lines) {
                player.sendMessage(TownySettings.getLangString("default_towny_prefix") + line2);
            }
        }
    }
    
    public static void sendTownBoard(final Player player, final Town town) {
        for (final String line : ChatTools.color(TownySettings.getLangString("townboard_message_colour_1") + "[" + town.getName() + "] " + TownySettings.getLangString("townboard_message_colour_2") + town.getTownBoard())) {
            player.sendMessage(line);
        }
    }
    
    public static void sendNationBoard(final Player player, final Nation nation) {
        for (final String line : ChatTools.color(TownySettings.getLangString("nationboard_message_colour_1") + "[" + nation.getName() + "] " + TownySettings.getLangString("townboard_message_colour_2") + nation.getNationBoard())) {
            player.sendMessage(line);
        }
    }
    
    public static void sendMessageToMode(final ResidentList residents, final String msg, final String modeRequired) {
        for (final Resident resident : TownyAPI.getInstance().getOnlineResidents(residents)) {
            if (resident.hasMode(modeRequired)) {
                sendMessage(resident, msg);
            }
        }
    }
    
    public static void sendMessageToMode(final Town town, final String msg, final String modeRequired) {
        for (final Resident resident : town.getResidents()) {
            if (BukkitTools.isOnline(resident.getName())) {
                sendMessage(resident, msg);
            }
        }
    }
    
    public static void sendMessageToMode(final Nation nation, final String msg, final String modeRequired) {
        for (final Resident resident : nation.getResidents()) {
            if (BukkitTools.isOnline(resident.getName())) {
                sendMessage(resident, msg);
            }
        }
    }
    
    public static void sendTitleMessageToResident(final Resident resident, final String title, final String subtitle) throws TownyException {
        final Player player = TownyAPI.getInstance().getPlayer(resident);
        if (player == null) {
            throw new TownyException("Player could not be found!");
        }
        player.sendTitle(title, subtitle, 10, 70, 10);
    }
    
    public static void sendTitleMessageToTown(final Town town, final String title, final String subtitle) {
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(town)) {
            player.sendTitle(title, subtitle, 10, 70, 10);
        }
    }
    
    public static void sendTitleMessageToNation(final Nation nation, final String title, final String subtitle) {
        for (final Player player : TownyAPI.getInstance().getOnlinePlayers(nation)) {
            player.sendTitle(title, subtitle, 10, 70, 10);
        }
    }
    
    public static void sendConfirmationMessage(final CommandSender player, String firstline, String confirmline, String cancelline, String lastline) {
        if (Towny.isSpigot) {
            TownySpigotMessaging.sendSpigotConfirmMessage(player, firstline, confirmline, cancelline, lastline);
            return;
        }
        if (firstline == null) {
            firstline = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Confirmation" + ChatColor.DARK_GRAY + "] " + ChatColor.BLUE + TownySettings.getLangString("are_you_sure_you_want_to_continue");
        }
        if (confirmline == null) {
            confirmline = ChatColor.GREEN + "          /" + TownySettings.getConfirmCommand();
        }
        if (cancelline == null) {
            cancelline = ChatColor.GREEN + "          /" + TownySettings.getCancelCommand();
        }
        if (lastline != null && lastline.equals("")) {
            final String[] message = { firstline, confirmline, cancelline };
            sendMessage(player, message);
            return;
        }
        if (lastline == null) {
            lastline = ChatColor.BLUE + TownySettings.getLangString("this_message_will_expire");
            final String[] message = { firstline, confirmline, cancelline, lastline };
            sendMessage(player, message);
        }
    }
    
    public static void sendRequestMessage(final CommandSender player, final Invite invite) {
        if (Towny.isSpigot) {
            TownySpigotMessaging.sendSpigotRequestMessage(player, invite);
            return;
        }
        if (invite.getSender() instanceof Town) {
            final String firstline = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Invitation" + ChatColor.DARK_GRAY + "] " + ChatColor.BLUE + String.format(TownySettings.getLangString("you_have_been_invited_to_join2"), invite.getSender().getName());
            final String secondline = ChatColor.GREEN + "          /" + TownySettings.getAcceptCommand() + " " + invite.getSender().getName();
            final String thirdline = ChatColor.GREEN + "          /" + TownySettings.getDenyCommand() + " " + invite.getSender().getName();
            sendConfirmationMessage(player, firstline, secondline, thirdline, "");
        }
        if (invite.getSender() instanceof Nation) {
            if (invite.getReceiver() instanceof Town) {
                final String firstline = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Invitation" + ChatColor.DARK_GRAY + "] " + ChatColor.BLUE + String.format(TownySettings.getLangString("you_have_been_invited_to_join2"), invite.getSender().getName());
                final String secondline = ChatColor.GREEN + "          /t invite accept " + invite.getSender().getName();
                final String thirdline = ChatColor.GREEN + "          /t invite deny " + invite.getSender().getName();
                sendConfirmationMessage(player, firstline, secondline, thirdline, "");
            }
            if (invite.getReceiver() instanceof Nation) {
                final String firstline = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Invitation" + ChatColor.DARK_GRAY + "] " + ChatColor.BLUE + String.format(TownySettings.getLangString("you_have_been_requested_to_ally2"), invite.getSender().getName());
                final String secondline = ChatColor.GREEN + "          /n ally accept " + invite.getSender().getName();
                final String thirdline = ChatColor.GREEN + "          /n ally deny " + invite.getSender().getName();
                sendConfirmationMessage(player, firstline, secondline, thirdline, "");
            }
        }
    }
    
    static {
        LOGGER = LogManager.getLogger((Class)Towny.class);
        LOGGER_DEBUG = LogManager.getLogger("com.palmergames.bukkit.towny.debug");
    }
}
