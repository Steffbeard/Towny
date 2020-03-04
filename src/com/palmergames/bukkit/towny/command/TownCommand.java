// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import com.palmergames.bukkit.towny.event.TownTransactionEvent;
import com.palmergames.bukkit.towny.event.TownPreTransactionEvent;
import com.palmergames.bukkit.towny.object.Transaction;
import com.palmergames.bukkit.towny.object.TransactionType;
import com.palmergames.bukkit.towny.tasks.TownClaim;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.utils.AreaSelectionUtil;
import com.palmergames.bukkit.towny.utils.OutpostUtil;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.TownyPermissionChange;
import com.palmergames.bukkit.towny.object.TownyPermission;
import java.util.Arrays;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.event.TownInvitePlayerEvent;
import com.palmergames.bukkit.towny.object.inviteobjects.PlayerJoinTownInvite;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import com.palmergames.util.TimeMgmt;
import com.palmergames.bukkit.towny.utils.SpawnUtil;
import com.palmergames.bukkit.towny.object.SpawnType;
import com.palmergames.bukkit.towny.event.TownPreRenameEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import java.util.UUID;
import com.palmergames.bukkit.towny.event.PreNewTownEvent;
import java.text.DecimalFormat;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import javax.naming.InvalidNameException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.util.NameValidation;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.TownBlockSettingsChangedEvent;
import com.palmergames.bukkit.towny.war.siegewar.timeractions.UpdateTownNeutralityCounters;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.block.BlockFace;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.entity.Entity;
import java.util.Collections;
import com.palmergames.bukkit.towny.utils.ResidentUtil;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import java.io.InvalidObjectException;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.util.ChatTools;
import org.bukkit.Location;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.object.Town;
import java.util.Comparator;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class TownCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> output;
    private static final List<String> invite;
    private static final Comparator<Town> BY_NUM_RESIDENTS;
    private static final Comparator<Town> BY_OPEN;
    private static final Comparator<Town> BY_NAME;
    private static final Comparator<Town> BY_BANK_BALANCE;
    private static final Comparator<Town> BY_TOWNBLOCKS_CLAIMED;
    private static final Comparator<Town> BY_NUM_ONLINE;
    
    public TownCommand(final Towny instance) {
        TownCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            this.parseTownCommand(player, args);
        }
        else {
            try {
                this.parseTownCommandForConsole(sender, args);
            }
            catch (TownyException ex) {}
        }
        return true;
    }
    
    private void parseTownCommandForConsole(final CommandSender sender, final String[] split) throws TownyException {
        if (split.length == 0 || split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
            for (final String line : TownCommand.output) {
                sender.sendMessage(line);
            }
        }
        else if (split[0].equalsIgnoreCase("list")) {
            this.listTowns(sender, split);
        }
        else {
            try {
                final Town town = TownyUniverse.getInstance().getDataSource().getTown(split[0]);
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)TownCommand.plugin, () -> TownyMessaging.sendMessage(sender, TownyFormatter.getStatus(town)));
            }
            catch (NotRegisteredException x) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
            }
        }
    }
    
    @SuppressWarnings("static-access")
	private void parseTownCommand(final Player player, final String[] split) {
    	TownyUniverse townyUniverse = TownyUniverse.getInstance();

		try {

			if (split.length == 0) {
				Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
					try {
						Resident resident = townyUniverse.getDataSource().getResident(player.getName());
						Town town = resident.getTown();

						TownyMessaging.sendMessage(player, TownyFormatter.getStatus(town));
					} catch (NotRegisteredException x) {
						try {
							throw new TownyException(TownySettings.getLangString("msg_err_dont_belong_town"));
                        }
                        catch (TownyException e) {
                            TownyMessaging.sendErrorMsg(player, e.getMessage());
                        }
                    }
                });
            }
            else if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
                for (final String line : TownCommand.output) {
                    player.sendMessage(line);
                }
            }
            else if (split[0].equalsIgnoreCase("here")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_HERE.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.showTownStatusHere(player);
            }
            else if (split[0].equalsIgnoreCase("list")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_LIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.listTowns((CommandSender)player, split);
            }
            else if (split[0].equalsIgnoreCase("new") || split[0].equalsIgnoreCase("create")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_NEW.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (split.length == 1) {
                    throw new TownyException(TownySettings.getLangString("msg_specify_name"));
                }
                if (split.length >= 2) {
                    final String[] newSplit = StringMgmt.remFirstArg(split);
                    final String townName = String.join("_", (CharSequence[])newSplit);
                    newTown(player, townName, player.getName(), false);
                }
            }
            else if (split[0].equalsIgnoreCase("leave")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_LEAVE.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.townLeave(player);
            }
            else if (split[0].equalsIgnoreCase("withdraw")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_WITHDRAW.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (TownySettings.isBankActionLimitedToBankPlots()) {
                    if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                    final TownBlock tb = TownyAPI.getInstance().getTownBlock(player.getLocation());
                    final Town tbTown = tb.getTown();
                    final Town pTown = townyUniverse.getDataSource().getResident(player.getName()).getTown();
                    if (tbTown != pTown) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                    boolean goodPlot = false;
                    if (tb.getType().equals(TownBlockType.BANK) || tb.isHomeBlock()) {
                        goodPlot = true;
                    }
                    if (!goodPlot) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                }
                if (TownySettings.isBankActionDisallowedOutsideTown()) {
                    if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_your_town"));
                    }
                    final Coord coord = Coord.parseCoord(TownCommand.plugin.getCache(player).getLastLocation());
                    final Town town2 = townyUniverse.getDataSource().getWorld(player.getLocation().getWorld().getName()).getTownBlock(coord).getTown();
                    if (!townyUniverse.getDataSource().getResident(player.getName()).getTown().equals(town2)) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_your_town"));
                    }
                }
                if (split.length == 2) {
                    try {
                        this.townWithdraw(player, Integer.parseInt(split[1].trim()));
                        return;
                    }
                    catch (NumberFormatException e2) {
                        throw new TownyException(TownySettings.getLangString("msg_error_must_be_int"));
                    }
                }
                throw new TownyException(String.format(TownySettings.getLangString("msg_must_specify_amnt"), "/town withdraw"));
            }
            else if (split[0].equalsIgnoreCase("deposit")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_DEPOSIT.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (TownySettings.isBankActionLimitedToBankPlots()) {
                    if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                    final TownBlock tb = TownyAPI.getInstance().getTownBlock(player.getLocation());
                    final Town tbTown = tb.getTown();
                    final Town pTown = townyUniverse.getDataSource().getResident(player.getName()).getTown();
                    if (tbTown != pTown) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                    boolean goodPlot = false;
                    if (tb.getType().equals(TownBlockType.BANK) || tb.isHomeBlock()) {
                        goodPlot = true;
                    }
                    if (!goodPlot) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                }
                if (TownySettings.isBankActionDisallowedOutsideTown()) {
                    if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_your_town"));
                    }
                    final Coord coord = Coord.parseCoord(TownCommand.plugin.getCache(player).getLastLocation());
                    final Town town2 = townyUniverse.getDataSource().getWorld(player.getLocation().getWorld().getName()).getTownBlock(coord).getTown();
                    if (!townyUniverse.getDataSource().getResident(player.getName()).getTown().equals(town2)) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_your_town"));
                    }
                }
                if (split.length == 2) {
                    try {
                        this.townDeposit(player, Integer.parseInt(split[1].trim()));
                        return;
                    }
                    catch (NumberFormatException e2) {
                        throw new TownyException(TownySettings.getLangString("msg_error_must_be_int"));
                    }
                }
                throw new TownyException(String.format(TownySettings.getLangString("msg_must_specify_amnt"), "/town deposit"));
            }
            else if (split[0].equalsIgnoreCase("plots")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_PLOTS.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                Town town3 = null;
                try {
                    if (split.length == 1) {
                        town3 = townyUniverse.getDataSource().getResident(player.getName()).getTown();
                    }
                    else {
                        town3 = townyUniverse.getDataSource().getTown(split[1]);
                    }
                }
                catch (Exception e3) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
                    return;
                }
                this.townPlots(player, town3);
            }
            else {
                final String[] newSplit = StringMgmt.remFirstArg(split);
                if (split[0].equalsIgnoreCase("rank")) {
                    this.townRank(player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("set")) {
                    townSet(player, newSplit, false, null);
                }
                else if (split[0].equalsIgnoreCase("buy")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_BUY.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.townBuy(player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("toggle")) {
                    townToggle(player, newSplit, false, null);
                }
                else if (split[0].equalsIgnoreCase("mayor")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_MAYOR.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.townMayor(player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("spawn")) {
                    townSpawn(player, newSplit, false);
                }
                else if (split[0].equalsIgnoreCase("outpost")) {
                    if (split.length >= 2) {
                        if (split[1].equalsIgnoreCase("list")) {
                            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_OUTPOST_LIST.getNode())) {
                                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                            }
                            final Resident resident2 = townyUniverse.getDataSource().getResident(player.getName());
                            if (resident2.hasTown()) {
                                final Town town4 = resident2.getTown();
                                final List<Location> outposts = town4.getAllOutpostSpawns();
                                int page = 1;
                                final int total = (int)Math.ceil(outposts.size() / 10.0);
                                if (split.length == 3) {
                                    try {
                                        page = Integer.parseInt(split[2]);
                                        if (page < 0) {
                                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative"));
                                            return;
                                        }
                                        if (page == 0) {
                                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                                            return;
                                        }
                                    }
                                    catch (NumberFormatException e4) {
                                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                                        return;
                                    }
                                }
                                if (page > total) {
                                    TownyMessaging.sendErrorMsg(player, TownySettings.getListNotEnoughPagesMsg(total));
                                    return;
                                }
                                int iMax = page * 10;
                                if (page * 10 > outposts.size()) {
                                    iMax = outposts.size();
                                }
                                final List<String> outputs = new ArrayList<String>();
                                for (int i = (page - 1) * 10; i < iMax; ++i) {
                                    final Location outpost = outposts.get(i);
                                    final TownBlock tb2 = TownyAPI.getInstance().getTownBlock(outpost);
                                    String output;
                                    if (!tb2.getName().equalsIgnoreCase("")) {
                                        output = "§6" + (i + 1) + "§8" + " - " + "§a" + tb2.getName() + "§8" + " - " + "§b" + outpost.getWorld().getName() + "§8" + " - " + "§b" + "(" + outpost.getBlockX() + "," + outpost.getBlockZ() + ")";
                                    }
                                    else {
                                        output = "§6" + (i + 1) + "§8" + " - " + "§b" + outpost.getWorld().getName() + "§8" + " - " + "§b" + "(" + outpost.getBlockX() + "," + outpost.getBlockZ() + ")";
                                    }
                                    outputs.add(output);
                                }
                                player.sendMessage(ChatTools.formatList(TownySettings.getLangString("outpost_plu"), "§6#§8 - §a(Plot Name)§8 - §b(Outpost World)§8 - §b(Outpost Location)", outputs, TownySettings.getListPageMsg(page, total)));
                            }
                            else {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_must_belong_town"));
                            }
                        }
                        else {
                            townSpawn(player, newSplit, true);
                        }
                    }
                    else {
                        townSpawn(player, newSplit, true);
                    }
                }
                else if (split[0].equalsIgnoreCase("delete")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_DELETE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.townDelete(player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("reslist")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_RESLIST.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    Town town2 = null;
                    try {
                        if (split.length == 1) {
                            town2 = townyUniverse.getDataSource().getResident(player.getName()).getTown();
                        }
                        else {
                            town2 = townyUniverse.getDataSource().getTown(split[1]);
                        }
                    }
                    catch (Exception e5) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
                        return;
                    }
                    TownyMessaging.sendMessage(player, TownyFormatter.getFormattedResidents(town2));
                }
                else {
                    if (split[0].equalsIgnoreCase("ranklist")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_RANKLIST.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        try {
                            final Resident resident2 = townyUniverse.getDataSource().getResident(player.getName());
                            final Town town4 = resident2.getTown();
                            TownyMessaging.sendMessage(player, TownyFormatter.getRanks(town4));
                            return;
                        }
                        catch (NotRegisteredException x3) {
                            throw new TownyException(TownySettings.getLangString("msg_err_dont_belong_town"));
                        }
                    }
                    if (split[0].equalsIgnoreCase("outlawlist")) {
                        Town town2;
                        try {
                            if (split.length == 1) {
                                town2 = townyUniverse.getDataSource().getResident(player.getName()).getTown();
                            }
                            else {
                                town2 = townyUniverse.getDataSource().getTown(split[1]);
                            }
                        }
                        catch (Exception e5) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
                            return;
                        }
                        TownyMessaging.sendMessage(player, TownyFormatter.getFormattedOutlaws(town2));
                    }
                    else if (split[0].equalsIgnoreCase("join")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_JOIN.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        parseTownJoin((CommandSender)player, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("add")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        townAdd(player, null, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("invite") || split[0].equalsIgnoreCase("invites")) {
                        this.parseInviteCommand(player, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("kick")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_KICK.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        townKick(player, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("claim")) {
                        parseTownClaimCommand(player, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("unclaim")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        parseTownUnclaimCommand(player, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("online")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_ONLINE.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        this.parseTownOnlineCommand(player, newSplit);
                    }
                    else if (split[0].equalsIgnoreCase("say")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_SAY.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        try {
                            final Town town2 = townyUniverse.getDataSource().getResident(player.getName()).getTown();
                            final StringBuilder builder = new StringBuilder();
                            for (final String s : newSplit) {
                                builder.append(s + " ");
                            }
                            final String message = builder.toString();
                            TownyMessaging.sendPrefixedTownMessage(town2, message);
                        }
                        catch (Exception ex) {}
                    }
                    else if (split[0].equalsIgnoreCase("outlaw")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        this.parseTownOutlawCommand(player, newSplit);
                    }
                    else {
                        try {
                            final Town town2 = townyUniverse.getDataSource().getTown(split[0]);
                            final Resident resident3 = townyUniverse.getDataSource().getResident(player.getName());
                            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_OTHERTOWN.getNode()) && (resident3.getTown() != town2 || !resident3.hasTown())) {
                                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                            }
                            final Town town5 = townyUniverse.getDataSource().getTown(split[0]);
                            Bukkit.getScheduler().runTaskAsynchronously((Plugin)TownCommand.plugin, () -> TownyMessaging.sendMessage(player, TownyFormatter.getStatus(town5)));
                        }
                        catch (NotRegisteredException x3) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
                        }
                    }
                }
            }
        }
        catch (Exception x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    private void parseInviteCommand(final Player player, final String[] newSplit) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
        final String received = TownySettings.getLangString("town_received_invites").replace("%a", Integer.toString(InviteHandler.getReceivedInvitesAmount(resident.getTown()))).replace("%m", Integer.toString(InviteHandler.getReceivedInvitesMaxAmount(resident.getTown())));
        final String sent = TownySettings.getLangString("town_sent_invites").replace("%a", Integer.toString(InviteHandler.getSentInvitesAmount(resident.getTown()))).replace("%m", Integer.toString(InviteHandler.getSentInvitesMaxAmount(resident.getTown())));
        if (newSplit.length != 0) {
            if (newSplit.length >= 1) {
                if (newSplit[0].equalsIgnoreCase("help") || newSplit[0].equalsIgnoreCase("?")) {
                    for (final String msg : TownCommand.invite) {
                        player.sendMessage(Colors.strip(msg));
                    }
                    return;
                }
                if (newSplit[0].equalsIgnoreCase("sent")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_LIST_SENT.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final List<Invite> sentinvites = resident.getTown().getSentInvites();
                    int page = 1;
                    if (newSplit.length >= 2) {
                        try {
                            page = Integer.parseInt(newSplit[1]);
                        }
                        catch (NumberFormatException ex2) {}
                    }
                    InviteCommand.sendInviteList(player, sentinvites, page, true);
                    player.sendMessage(sent);
                }
                else if (newSplit[0].equalsIgnoreCase("received")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_LIST_RECEIVED.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final List<Invite> receivedinvites = resident.getTown().getReceivedInvites();
                    int page = 1;
                    if (newSplit.length >= 2) {
                        try {
                            page = Integer.parseInt(newSplit[1]);
                        }
                        catch (NumberFormatException ex3) {}
                    }
                    InviteCommand.sendInviteList(player, receivedinvites, page, false);
                    player.sendMessage(received);
                }
                else {
                    Nation nation = null;
                    Label_0710: {
                        if (newSplit[0].equalsIgnoreCase("accept")) {
                            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ACCEPT.getNode())) {
                                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                            }
                            final Town town = resident.getTown();
                            final List<Invite> invites = town.getReceivedInvites();
                            if (invites.size() == 0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_town_no_invites"));
                                return;
                            }
                            Label_0599: {
                                if (newSplit.length >= 2) {
                                    Label_0617: {
                                        try {
                                            nation = townyUniverse.getDataSource().getNation(newSplit[1]);
                                            break Label_0617;
                                        }
                                        catch (NotRegisteredException e3) {
                                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                                            return;
                                        }
                                    }
                                    Invite toAccept = null;
                                    for (final Invite invite : InviteHandler.getActiveInvites()) {
                                        if (invite.getSender().equals(nation) && invite.getReceiver().equals(town)) {
                                            toAccept = invite;
                                            break;
                                        }
                                    }
                                    if (toAccept != null) {
                                        try {
                                            InviteHandler.acceptInvite(toAccept);
                                            return;
                                        }
                                        catch (TownyException | InvalidObjectException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break Label_0710;
                                }
                            }
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_town_specify_invite"));
                            InviteCommand.sendInviteList(player, invites, 1, false);
                            return;
                        }
                    }
                    if (newSplit[0].equalsIgnoreCase("deny")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_DENY.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        final Town town = resident.getTown();
                        final List<Invite> invites = town.getReceivedInvites();
                        if (invites.size() == 0) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_town_no_invites"));
                            return;
                        }
                        Label_0818: {
                            if (newSplit.length >= 2) {
                                Label_0836: {
                                    try {
                                        nation = townyUniverse.getDataSource().getNation(newSplit[1]);
                                        break Label_0836;
                                    }
                                    catch (NotRegisteredException e3) {
                                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                                        return;
                                    }
                                }
                                Invite toDecline = null;
                                for (final Invite invite : InviteHandler.getActiveInvites()) {
                                    if (invite.getSender().equals(nation) && invite.getReceiver().equals(town)) {
                                        toDecline = invite;
                                        break;
                                    }
                                }
                                if (toDecline != null) {
                                    try {
                                        InviteHandler.declineInvite(toDecline, false);
                                        TownyMessaging.sendMessage(player, TownySettings.getLangString("successful_deny"));
                                    }
                                    catch (InvalidObjectException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                                return;
                            }
                        }
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_town_specify_invite"));
                        InviteCommand.sendInviteList(player, invites, 1, false);
                    }
                    else {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        townAdd(player, null, newSplit);
                    }
                }
            }
            return;
        }
        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_SEE_HOME.getNode())) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        final List<String> messages = new ArrayList<String>();
        for (final String msg2 : TownCommand.invite) {
            messages.add(Colors.strip(msg2));
        }
        messages.add(sent);
        messages.add(received);
        final String[] msgs = messages.toArray(new String[0]);
        player.sendMessage(msgs);
    }
    
    private void parseTownOutlawCommand(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/town outlaw"));
            player.sendMessage(ChatTools.formatCommand("", "/town outlaw", "add/remove [name]", ""));
        }
        else {
            Town town = null;
            Town targetTown = null;
            if (split.length < 2) {
                throw new TownyException("Eg: /town outlaw add/remove [name]");
            }
            Resident resident;
            Resident target;
            try {
                resident = townyUniverse.getDataSource().getResident(player.getName());
                target = townyUniverse.getDataSource().getResident(split[1]);
                town = resident.getTown();
            }
            catch (TownyException x) {
                throw new TownyException(x.getMessage());
            }
            Label_0459: {
                if (split[0].equalsIgnoreCase("add")) {
                    try {
                        try {
                            targetTown = target.getTown();
                        }
                        catch (Exception ex) {}
                        if (resident.getTown().getMayor().equals(target)) {
                            return;
                        }
                        if (targetTown != null && targetTown == town) {
                            townRemoveResident(town, target);
                            TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_kicked_by"), player.getName()));
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_kicked"), player.getName(), target.getName()));
                        }
                        town.addOutlaw(target);
                        townyUniverse.getDataSource().saveTown(town);
                        TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_been_declared_outlaw"), town.getName()));
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_you_have_declared_an_outlaw"), target.getName(), town.getName()));
                        break Label_0459;
                    }
                    catch (AlreadyRegisteredException e2) {
                        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_err_resident_already_an_outlaw"));
                        return;
                    }
                    catch (EmptyTownException e) {
                        e.printStackTrace();
                        break Label_0459;
                    }
                }
                if (split[0].equalsIgnoreCase("remove")) {
                    try {
                        town.removeOutlaw(target);
                        townyUniverse.getDataSource().saveTown(town);
                        TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_been_undeclared_outlaw"), town.getName()));
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_you_have_undeclared_an_outlaw"), target.getName(), town.getName()));
                        break Label_0459;
                    }
                    catch (NotRegisteredException e3) {
                        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_err_player_not_an_outlaw"));
                        return;
                    }
                }
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), split[0]));
                return;
            }
            townyUniverse.getDataSource().saveTown(town);
        }
    }
    
    private void townPlots(final Player player, final Town town) {
        final List<String> out = new ArrayList<String>();
        int townOwned = 0;
        int resident = 0;
        int residentOwned = 0;
        int residentOwnedFS = 0;
        int embassy = 0;
        int embassyRO = 0;
        int embassyFS = 0;
        int shop = 0;
        int shopRO = 0;
        int shopFS = 0;
        int farm = 0;
        int arena = 0;
        int wilds = 0;
        int jail = 0;
        int inn = 0;
        for (final TownBlock townBlock : town.getTownBlocks()) {
            if (townBlock.getType() == TownBlockType.EMBASSY) {
                ++embassy;
                if (townBlock.hasResident()) {
                    ++embassyRO;
                }
                if (townBlock.isForSale()) {
                    ++embassyFS;
                }
            }
            else if (townBlock.getType() == TownBlockType.COMMERCIAL) {
                ++shop;
                if (townBlock.hasResident()) {
                    ++shopRO;
                }
                if (townBlock.isForSale()) {
                    ++shopFS;
                }
            }
            else if (townBlock.getType() == TownBlockType.FARM) {
                ++farm;
            }
            else if (townBlock.getType() == TownBlockType.ARENA) {
                ++arena;
            }
            else if (townBlock.getType() == TownBlockType.WILDS) {
                ++wilds;
            }
            else if (townBlock.getType() == TownBlockType.JAIL) {
                ++jail;
            }
            else if (townBlock.getType() == TownBlockType.INN) {
                ++inn;
            }
            else if (townBlock.getType() == TownBlockType.RESIDENTIAL) {
                ++resident;
                if (townBlock.hasResident()) {
                    ++residentOwned;
                }
                if (townBlock.isForSale()) {
                    ++residentOwnedFS;
                }
            }
            if (!townBlock.hasResident()) {
                ++townOwned;
            }
        }
        out.add(ChatTools.formatTitle(town + " Town Plots"));
        out.add("§2Town Size: §a" + town.getTownBlocks().size() + " / " + TownySettings.getMaxTownBlocks(town) + (TownySettings.isSellingBonusBlocks(town) ? ("§b [Bought: " + town.getPurchasedBlocks() + "/" + TownySettings.getMaxPurchedBlocks(town) + "]") : "") + ((town.getBonusBlocks() > 0) ? ("§b [Bonus: " + town.getBonusBlocks() + "]") : "") + ((TownySettings.getNationBonusBlocks(town) > 0) ? ("§b [NationBonus: " + TownySettings.getNationBonusBlocks(town) + "]") : ""));
        out.add("§2Town Owned Land: §a" + townOwned);
        out.add("§2Farms   : §a" + farm);
        out.add("§2Arenas : §a" + arena);
        out.add("§2Wilds    : §a" + wilds);
        out.add("§2Jails    : §a" + jail);
        out.add("§2Inns    : §a" + inn);
        out.add("§2Type: §aPlayer-Owned / ForSale / Total / Daily Revenue");
        out.add("§2Residential: §a" + residentOwned + " / " + residentOwnedFS + " / " + resident + " / " + residentOwned * town.getPlotTax());
        out.add("§2Embassies : §a" + embassyRO + " / " + embassyFS + " / " + embassy + " / " + embassyRO * town.getEmbassyPlotTax());
        out.add("§2Shops      : §a" + shopRO + " / " + shopFS + " / " + shop + " / " + shop * town.getCommercialPlotTax());
        out.add(String.format(TownySettings.getLangString("msg_town_plots_revenue_disclaimer"), new Object[0]));
        TownyMessaging.sendMessage(player, out);
    }
    
    private void parseTownOnlineCommand(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length > 0) {
            try {
                final Town town = townyUniverse.getDataSource().getTown(split[0]);
                final List<Resident> onlineResidents = ResidentUtil.getOnlineResidentsViewable(player, town);
                if (onlineResidents.size() > 0) {
                    TownyMessaging.sendMsg(player, TownyFormatter.getFormattedOnlineResidents(TownySettings.getLangString("msg_town_online"), town, player));
                }
                else {
                    TownyMessaging.sendMsg(player, TownySettings.getLangString("default_towny_prefix") + "§f" + "0 " + TownySettings.getLangString("res_list") + " " + TownySettings.getLangString("msg_town_online") + ": " + town);
                }
                return;
            }
            catch (NotRegisteredException e) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
            }
        }
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            final Town town2 = resident.getTown();
            TownyMessaging.sendMsg(player, TownyFormatter.getFormattedOnlineResidents(TownySettings.getLangString("msg_town_online"), town2, player));
        }
        catch (NotRegisteredException x) {
            TownyMessaging.sendMessage(player, TownySettings.getLangString("msg_err_dont_belong_town"));
        }
    }
    
    public void listTowns(final CommandSender sender, final String[] split) {
        if (split.length == 2 && split[1].equals("?")) {
            sender.sendMessage(ChatTools.formatTitle("/town list"));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #}", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #} by residents", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #} by open", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #} by balance", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #} by name", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #} by townblocks", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/town list", "{page #} by online", ""));
            return;
        }
        List<Town> townsToSort = TownyUniverse.getInstance().getDataSource().getTowns();
        int page = 1;
        boolean pageSet = false;
        boolean comparatorSet = false;
        Comparator<Town> comparator = TownCommand.BY_NUM_RESIDENTS;
        int total = (int)Math.ceil(townsToSort.size() / 10.0);
        for (int i = 1; i < split.length; ++i) {
            if (split[i].equalsIgnoreCase("by")) {
                if (comparatorSet) {
                    TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_multiple_comparators"));
                    return;
                }
                if (++i >= split.length) {
                    TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_missing_comparator"));
                    return;
                }
                comparatorSet = true;
                if (split[i].equalsIgnoreCase("residents")) {
                    comparator = TownCommand.BY_NUM_RESIDENTS;
                }
                else if (split[i].equalsIgnoreCase("balance")) {
                    comparator = TownCommand.BY_BANK_BALANCE;
                }
                else if (split[i].equalsIgnoreCase("name")) {
                    comparator = TownCommand.BY_NAME;
                }
                else if (split[i].equalsIgnoreCase("townblocks")) {
                    comparator = TownCommand.BY_TOWNBLOCKS_CLAIMED;
                }
                else if (split[i].equalsIgnoreCase("online")) {
                    comparator = TownCommand.BY_NUM_ONLINE;
                }
                else {
                    if (!split[i].equalsIgnoreCase("open")) {
                        TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_invalid_comparator_town"));
                        return;
                    }
                    comparator = TownCommand.BY_OPEN;
                }
                comparatorSet = true;
            }
            else {
                if (pageSet) {
                    TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_too_many_pages"));
                    return;
                }
                try {
                    page = Integer.parseInt(split[1]);
                    if (page < 0) {
                        TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_err_negative"));
                        return;
                    }
                    if (page == 0) {
                        TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_must_be_int"));
                        return;
                    }
                    pageSet = true;
                }
                catch (NumberFormatException e) {
                    TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_must_be_int"));
                    return;
                }
            }
        }
        if (comparator == TownCommand.BY_OPEN) {
            final List<Town> townsList = TownyUniverse.getInstance().getDataSource().getTowns();
            final List<Town> openTownsList = new ArrayList<Town>();
            for (final Town town : townsList) {
                if (town.isOpen()) {
                    openTownsList.add(town);
                }
            }
            if (openTownsList.isEmpty()) {
                TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("no_open_towns"));
                return;
            }
            townsToSort = openTownsList;
            total = (int)Math.ceil(townsToSort.size() / 10.0);
        }
        if (page > total) {
            TownyMessaging.sendErrorMsg(sender, TownySettings.getListNotEnoughPagesMsg(total));
            return;
        }
        try {
            if (!TownySettings.isTownListRandom()) {
                Collections.sort(townsToSort, comparator);
            }
            else {
                Collections.shuffle(townsToSort);
            }
        }
        catch (RuntimeException e2) {
            TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_comparator_failed"));
            return;
        }
        int iMax = page * 10;
        if (page * 10 > townsToSort.size()) {
            iMax = townsToSort.size();
        }
        final List<String> townsformatted = new ArrayList<String>();
        for (int j = (page - 1) * 10; j < iMax; ++j) {
            final Town town = townsToSort.get(j);
            String output = "§3" + StringMgmt.remUnderscore(town.getName()) + (TownySettings.isTownListRandom() ? "" : ("§8 - §b(" + town.getNumResidents() + ")"));
            if (town.isOpen()) {
                output += TownySettings.getLangString("status_title_open");
            }
            townsformatted.add(output);
        }
        sender.sendMessage(ChatTools.formatList(TownySettings.getLangString("town_plu"), "§3" + TownySettings.getLangString("town_name") + (TownySettings.isTownListRandom() ? "" : ("§8 - §b" + TownySettings.getLangString("number_of_residents"))), townsformatted, TownySettings.getListPageMsg(page, total)));
    }
    
    public void townMayor(final Player player, final String[] split) {
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            this.showTownMayorHelp(player);
        }
    }
    
    public void showTownStatusHere(final Player player) {
        try {
            final TownyWorld world = TownyUniverse.getInstance().getDataSource().getWorld(player.getWorld().getName());
            final Coord coord = Coord.parseCoord((Entity)player);
            this.showTownStatusAtCoord(player, world, coord);
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public void showTownStatusAtCoord(final Player player, final TownyWorld world, final Coord coord) throws TownyException {
        if (!world.hasTownBlock(coord)) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_not_claimed"), coord));
        }
        final Town town = world.getTownBlock(coord).getTown();
        TownyMessaging.sendMessage(player, TownyFormatter.getStatus(town));
    }
    
    public void showTownMayorHelp(final Player player) {
        player.sendMessage(ChatTools.formatTitle("Town Mayor Help"));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "withdraw [$]", ""));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "claim", "'/town claim ?' " + TownySettings.getLangString("res_5")));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "unclaim", "'/town " + TownySettings.getLangString("res_5")));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "[add/kick] " + TownySettings.getLangString("res_2") + " .. []", TownySettings.getLangString("res_6")));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "set [] .. []", "'/town set' " + TownySettings.getLangString("res_5")));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "buy [] .. []", "'/town buy' " + TownySettings.getLangString("res_5")));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "plots", ""));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "toggle", ""));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "rank add/remove [resident] [rank]", "'/town rank ?' " + TownySettings.getLangString("res_5")));
        player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "delete", ""));
    }
    
    public static void townToggle(final Player player, final String[] split, final boolean admin, Town town) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/town toggle"));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "pvp", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "public", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "explosion", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "fire", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "mobs", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "taxpercent", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "open", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "jail [number] [resident]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town toggle", "neutral", ""));
        }
        else {
            Resident resident;
            try {
                if (!admin) {
                    resident = townyUniverse.getDataSource().getResident(player.getName());
                    town = resident.getTown();
                }
                else {
                    resident = town.getMayor();
                }
            }
            catch (TownyException x) {
                throw new TownyException(x.getMessage());
            }
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            Label_2066: {
                if (split[0].equalsIgnoreCase("public")) {
                    town.setPublic(!town.isPublic());
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_public"), town.isPublic() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("pvp")) {
                    if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegePvpAlwaysOnInBesiegedTowns() && town.hasSiege() && town.getSiege().getStatus() == SiegeStatus.IN_PROGRESS) {
                        throw new TownyException("In besieged towns, PVP is automatically set to 'ON', and cannot be changed until the siege is over.");
                    }
                    toggleTest(player, town, StringMgmt.join(split, " "));
                    if (TownySettings.getPVPCoolDownTime() > 0 && !admin && CooldownTimerTask.hasCooldown(town.getName(), CooldownTimerTask.CooldownType.PVP) && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(town.getName(), CooldownTimerTask.CooldownType.PVP)));
                    }
                    boolean outsiderintown = false;
                    if (TownySettings.getOutsidersPreventPVPToggle()) {
                        for (final Player target : Bukkit.getOnlinePlayers()) {
                            final Resident targetresident = townyUniverse.getDataSource().getResident(target.getName());
                            final Block block = target.getLocation().getBlock().getRelative(BlockFace.DOWN);
                            if (!TownyAPI.getInstance().isWilderness(block.getLocation())) {
                                final WorldCoord coord = WorldCoord.parseWorldCoord(target.getLocation());
                                for (final TownBlock tb : town.getTownBlocks()) {
                                    if (coord.equals(tb.getWorldCoord()) && (!targetresident.hasTown() || !targetresident.getTown().equals(town))) {
                                        outsiderintown = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!outsiderintown) {
                        town.setPVP(!town.isPVP());
                        if (TownySettings.getPVPCoolDownTime() > 0 && !admin && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                            CooldownTimerTask.addCooldownTimer(town.getName(), CooldownTimerTask.CooldownType.PVP);
                        }
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_pvp"), town.getName(), town.isPVP() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                    }
                    else if (outsiderintown) {
                        throw new TownyException(TownySettings.getLangString("msg_cant_toggle_pvp_outsider_in_town"));
                    }
                }
                else if (split[0].equalsIgnoreCase("explosion")) {
                    toggleTest(player, town, StringMgmt.join(split, " "));
                    town.setBANG(!town.isBANG());
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_expl"), town.getName(), town.isBANG() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("fire")) {
                    toggleTest(player, town, StringMgmt.join(split, " "));
                    town.setFire(!town.isFire());
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_fire"), town.getName(), town.isFire() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("mobs")) {
                    toggleTest(player, town, StringMgmt.join(split, " "));
                    town.setHasMobs(!town.hasMobs());
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_mobs"), town.getName(), town.hasMobs() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("taxpercent")) {
                    town.setTaxPercentage(!town.isTaxPercentage());
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_taxpercent"), town.isTaxPercentage() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("open")) {
                    town.setOpen(!town.isOpen());
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_changed_open"), town.isOpen() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                    if (town.isOpen()) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_toggle_open_on_warning"), new Object[0]));
                    }
                }
                else {
                    if (split[0].equalsIgnoreCase("jail")) {
                        if (!town.hasJailSpawn()) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_town_has_no_jails"), new Object[0]));
                        }
                        if (split.length <= 2) {
                            player.sendMessage(ChatTools.formatTitle("/town toggle jail"));
                            player.sendMessage(ChatTools.formatCommand("", "/town toggle jail", "[number] [resident]", ""));
                            player.sendMessage(ChatTools.formatCommand("", "/town toggle jail", "[number] [resident] [days]", ""));
                            break Label_2066;
                        }
                        if (split.length <= 2) {
                            break Label_2066;
                        }
                        try {
                            Integer.parseInt(split[1]);
                            Integer index = Integer.valueOf(split[1]);
                            Integer days;
                            if (split.length == 4) {
                                days = Integer.valueOf(split[3]);
                                if (days < 1) {
                                    throw new TownyException(TownySettings.getLangString("msg_err_days_must_be_greater_than_zero"));
                                }
                            }
                            else {
                                days = 0;
                            }
                            final Resident jailedresident = townyUniverse.getDataSource().getResident(split[2]);
                            if (!player.hasPermission("towny.command.town.toggle.jail")) {
                                throw new TownyException(TownySettings.getLangString("msg_no_permission_to_jail_your_residents"));
                            }
                            if (!jailedresident.hasTown() && !jailedresident.isJailed()) {
                                throw new TownyException(TownySettings.getLangString("msg_resident_not_part_of_any_town"));
                            }
                            try {
                                if (jailedresident.isJailed() && index != jailedresident.getJailSpawn()) {
                                    index = jailedresident.getJailSpawn();
                                }
                                final Player jailedplayer = TownyAPI.getInstance().getPlayer(jailedresident);
                                if (jailedplayer == null) {
                                    throw new TownyException(String.format(TownySettings.getLangString("msg_player_is_not_online"), jailedresident.getName()));
                                }
                                final Town sendertown = resident.getTown();
                                if (jailedplayer.getUniqueId().equals(player.getUniqueId())) {
                                    throw new TownyException(TownySettings.getLangString("msg_no_self_jailing"));
                                }
                                if (jailedresident.isJailed()) {
                                    final Town jailTown = townyUniverse.getDataSource().getTown(jailedresident.getJailTown());
                                    if (jailTown != sendertown) {
                                        throw new TownyException(TownySettings.getLangString("msg_player_not_jailed_in_your_town"));
                                    }
                                    jailedresident.setJailedByMayor(jailedplayer, index, sendertown, days);
                                    return;
                                }
                                else {
                                    if (jailedresident.getTown() != sendertown) {
                                        throw new TownyException(TownySettings.getLangString("msg_resident_not_your_town"));
                                    }
                                    jailedresident.setJailedByMayor(jailedplayer, index, sendertown, days);
                                }
                            }
                            catch (NotRegisteredException x2) {
                                throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
                            }
                            break Label_2066;
                        }
                        catch (NumberFormatException e2) {
                            player.sendMessage(ChatTools.formatTitle("/town toggle jail"));
                            player.sendMessage(ChatTools.formatCommand("", "/town toggle jail", "[number] [resident]", ""));
                            player.sendMessage(ChatTools.formatCommand("", "/town toggle jail", "[number] [resident] [days]", ""));
                            return;
                        }
                        catch (NullPointerException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    if (!split[0].equalsIgnoreCase("neutral")) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_property"), split[0]));
                    }
                    if (!TownySettings.getWarSiegeEnabled() || !TownySettings.getWarSiegeTownNeutralityEnabled()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE.getNode(split[0].toLowerCase()))) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (town.hasNation()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_siege_war_cannot_change_neutrality_while_in_nation"));
                    }
                    if (admin) {
                        town.setNeutralityChangeConfirmationCounterDays(1);
                        UpdateTownNeutralityCounters.updateTownNeutralityCounter(town);
                    }
                    else if (town.getNeutralityChangeConfirmationCounterDays() == 0) {
                        town.setDesiredNeutralityValue(!town.isNeutral());
                        final int counterValue = TownySettings.getWarSiegeTownNeutralityConfirmationRequirementDays();
                        town.setNeutralityChangeConfirmationCounterDays(counterValue);
                        if (town.getDesiredNeutralityValue()) {
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_siege_war_town_declared_neutral"), counterValue));
                        }
                        else {
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_siege_war_town_declared_non_neutral"), counterValue));
                        }
                    }
                    else {
                        town.setDesiredNeutralityValue(town.isNeutral());
                        town.setNeutralityChangeConfirmationCounterDays(0);
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_siege_war_town_neutrality_countdown_cancelled"), new Object[0]));
                    }
                }
            }
            for (final TownBlock townBlock : town.getTownBlocks()) {
                if (!townBlock.hasResident() && !townBlock.isChanged()) {
                    townBlock.setType(townBlock.getType());
                    townyUniverse.getDataSource().saveTownBlock(townBlock);
                }
            }
            final TownBlockSettingsChangedEvent event = new TownBlockSettingsChangedEvent(town);
            Bukkit.getServer().getPluginManager().callEvent((Event)event);
            townyUniverse.getDataSource().saveTown(town);
        }
    }
    
    private static void toggleTest(final Player player, final Town town, String split) throws TownyException {
        split = split.toLowerCase();
        if (split.contains("mobs") && town.getWorld().isForceTownMobs()) {
            throw new TownyException(TownySettings.getLangString("msg_world_mobs"));
        }
        if (split.contains("fire") && town.getWorld().isForceFire()) {
            throw new TownyException(TownySettings.getLangString("msg_world_fire"));
        }
        if (split.contains("explosion") && town.getWorld().isForceExpl()) {
            throw new TownyException(TownySettings.getLangString("msg_world_expl"));
        }
        if (split.contains("pvp") && town.getWorld().isForcePVP()) {
            throw new TownyException(TownySettings.getLangString("msg_world_pvp"));
        }
    }
    
    public void townRank(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/town rank"));
            player.sendMessage(ChatTools.formatCommand("", "/town rank", "add/remove [resident] rank", ""));
        }
        else {
            Town town = null;
            if (split.length < 3) {
                throw new TownyException("Eg: /town rank add/remove [resident] [rank]");
            }
            Resident target;
            try {
                final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                target = townyUniverse.getDataSource().getResident(split[1]);
                town = resident.getTown();
                if (town != target.getTown()) {
                    throw new TownyException(TownySettings.getLangString("msg_resident_not_your_town"));
                }
            }
            catch (TownyException x) {
                throw new TownyException(x.getMessage());
            }
            String rank = split[2];
            for (final String ranks : TownyPerms.getTownRanks()) {
                if (ranks.equalsIgnoreCase(rank)) {
                    rank = ranks;
                }
            }
            if (!TownyPerms.getTownRanks().contains(rank)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_unknown_rank_available_ranks"), rank, StringMgmt.join(TownyPerms.getTownRanks(), ",")));
            }
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_RANK.getNode(rank.toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_no_permission_to_give_rank"));
            }
            Label_0613: {
                if (split[0].equalsIgnoreCase("add")) {
                    try {
                        if (target.addTownRank(rank)) {
                            if (BukkitTools.isOnline(target.getName())) {
                                TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_been_given_rank"), "Town", rank));
                                TownCommand.plugin.deleteCache(TownyAPI.getInstance().getPlayer(target));
                            }
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_have_given_rank"), "Town", rank, target.getName()));
                            break Label_0613;
                        }
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_resident_not_your_town"));
                        return;
                    }
                    catch (AlreadyRegisteredException e) {
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_resident_already_has_rank"), target.getName(), "Town"));
                        return;
                    }
                }
                if (split[0].equalsIgnoreCase("remove")) {
                    try {
                        if (target.removeTownRank(rank)) {
                            if (BukkitTools.isOnline(target.getName())) {
                                TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_had_rank_taken"), "Town", rank));
                                TownCommand.plugin.deleteCache(TownyAPI.getInstance().getPlayer(target));
                            }
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_have_taken_rank_from"), "Town", rank, target.getName()));
                        }
                        break Label_0613;
                    }
                    catch (NotRegisteredException e2) {
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_resident_doesnt_have_rank"), target.getName(), "Town"));
                        return;
                    }
                }
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), split[0]));
                return;
            }
            townyUniverse.getDataSource().saveResident(target);
        }
    }
    
    public static void townSet(final Player player, String[] split, final boolean admin, Town town) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/town set"));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "board [message ... ]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "mayor " + TownySettings.getLangString("town_help_2"), ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "homeblock", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "spawn/outpost/jail", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "perm ...", "'/town set perm' " + TownySettings.getLangString("res_5")));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "taxes [$]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "[plottax/shoptax/embassytax] [$]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "[plotprice/shopprice/embassyprice] [$]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "spawncost [$]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "name [name]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "tag [upto 4 letters] or clear", ""));
            player.sendMessage(ChatTools.formatCommand("", "/town set", "title/surname [resident] [text]", ""));
        }
        else {
            Nation nation = null;
            TownyWorld oldWorld = null;
            Resident resident;
            try {
                if (!admin) {
                    resident = townyUniverse.getDataSource().getResident(player.getName());
                    town = resident.getTown();
                }
                else {
                    resident = town.getMayor();
                }
                if (town.hasNation()) {
                    nation = town.getNation();
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
            Label_3700: {
                if (split[0].equalsIgnoreCase("board")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_SET_BOARD.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split.length < 2) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /town set board " + TownySettings.getLangString("town_help_9"));
                        return;
                    }
                    final String line = StringMgmt.join(StringMgmt.remFirstArg(split), " ");
                    if (!NameValidation.isValidString(line)) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_invalid_string_board_not_set"));
                        return;
                    }
                    town.setTownBoard(line);
                    TownyMessaging.sendTownBoard(player, town);
                }
                else if (split[0].equalsIgnoreCase("title")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_SET_TITLE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split.length < 2) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /town set title bilbo Jester ");
                    }
                    else {
                        resident = townyUniverse.getDataSource().getResident(split[1]);
                    }
                    if (!resident.hasTown()) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_town"), resident.getName()));
                        return;
                    }
                    if (resident.getTown() != townyUniverse.getDataSource().getResident(player.getName()).getTown()) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_town"), resident.getName()));
                        return;
                    }
                    split = StringMgmt.remArgs(split, 2);
                    if (StringMgmt.join(split).length() > TownySettings.getMaxTitleLength()) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_input_too_long"));
                        return;
                    }
                    final String title = StringMgmt.join(NameValidation.checkAndFilterArray(split));
                    resident.setTitle(title + " ");
                    townyUniverse.getDataSource().saveResident(resident);
                    if (resident.hasTitle()) {
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_set_title"), resident.getName(), resident.getTitle()));
                    }
                    else {
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Title", resident.getName()));
                    }
                }
                else if (split[0].equalsIgnoreCase("surname")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SURNAME.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split.length < 2) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /town set surname bilbo the dwarf ");
                    }
                    else {
                        resident = townyUniverse.getDataSource().getResident(split[1]);
                    }
                    if (!resident.hasTown()) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_town"), resident.getName()));
                        return;
                    }
                    if (resident.getTown() != townyUniverse.getDataSource().getResident(player.getName()).getTown()) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_town"), resident.getName()));
                        return;
                    }
                    split = StringMgmt.remArgs(split, 2);
                    if (StringMgmt.join(split).length() > TownySettings.getMaxTitleLength()) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_input_too_long"));
                        return;
                    }
                    final String surname = StringMgmt.join(NameValidation.checkAndFilterArray(split));
                    resident.setSurname(" " + surname);
                    townyUniverse.getDataSource().saveResident(resident);
                    if (resident.hasSurname()) {
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_set_surname"), resident.getName(), resident.getSurname()));
                    }
                    else {
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Surname", resident.getName()));
                    }
                }
                else {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_SET.getNode(split[0].toLowerCase()))) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split[0].equalsIgnoreCase("mayor")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set mayor Dumbo");
                            return;
                        }
                        try {
                            if (!resident.isMayor()) {
                                throw new TownyException(TownySettings.getLangString("msg_not_mayor"));
                            }
                            final String oldMayor = town.getMayor().getName();
                            final Resident newMayor = townyUniverse.getDataSource().getResident(split[1]);
                            town.setMayor(newMayor);
                            TownyPerms.assignPermissions(townyUniverse.getDataSource().getResident(oldMayor), null);
                            TownCommand.plugin.deleteCache(oldMayor);
                            TownCommand.plugin.deleteCache(newMayor.getName());
                            if (admin) {
                                TownyMessaging.sendMessage(player, TownySettings.getNewMayorMsg(newMayor.getName()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getNewMayorMsg(newMayor.getName()));
                            break Label_3700;
                        }
                        catch (TownyException e) {
                            TownyMessaging.sendErrorMsg(player, e.getMessage());
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("taxes")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set taxes 7");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            if (town.isTaxPercentage() && amount > 100.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_percentage"));
                                return;
                            }
                            if (TownySettings.getTownDefaultTaxMinimumTax() > amount) {
                                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_tax_minimum_not_met"), TownySettings.getTownDefaultTaxMinimumTax()));
                                return;
                            }
                            town.setTaxes(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_tax"), player.getName(), town.getTaxes()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_tax"), player.getName(), town.getTaxes()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("plottax")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set plottax 10");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            town.setPlotTax(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_plottax"), player.getName(), town.getPlotTax()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_plottax"), player.getName(), town.getPlotTax()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("shoptax")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set shoptax 10");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            town.setCommercialPlotTax(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_alttax"), player.getName(), "shop", town.getCommercialPlotTax()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_alttax"), player.getName(), "shop", town.getCommercialPlotTax()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("embassytax")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set embassytax 10");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            town.setEmbassyPlotTax(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_alttax"), player.getName(), "embassy", town.getEmbassyPlotTax()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_alttax"), player.getName(), "embassy", town.getEmbassyPlotTax()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("plotprice")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set plotprice 50");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            town.setPlotPrice(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_plotprice"), player.getName(), town.getPlotPrice()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_plotprice"), player.getName(), town.getPlotPrice()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("shopprice")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set shopprice 50");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            town.setCommercialPlotPrice(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_altprice"), player.getName(), "shop", town.getCommercialPlotPrice()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_altprice"), player.getName(), "shop", town.getCommercialPlotPrice()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("embassyprice")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set embassyprice 50");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            town.setEmbassyPlotPrice(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_town_set_altprice"), player.getName(), "embassy", town.getEmbassyPlotPrice()));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_altprice"), player.getName(), "embassy", town.getEmbassyPlotPrice()));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("spawncost")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set spawncost 50");
                            return;
                        }
                        try {
                            final Double amount = Double.parseDouble(split[1]);
                            if (amount < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            if (TownySettings.getSpawnTravelCost() < amount) {
                                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_cannot_set_spawn_cost_more_than"), TownySettings.getSpawnTravelCost()));
                                return;
                            }
                            town.setSpawnCost(amount);
                            if (admin) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_spawn_cost_set_to"), player.getName(), TownySettings.getLangString("town_sing"), split[1]));
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_spawn_cost_set_to"), player.getName(), TownySettings.getLangString("town_sing"), split[1]));
                            break Label_3700;
                        }
                        catch (NumberFormatException e4) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("name")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set name BillyBobTown");
                            return;
                        }
                        if (TownySettings.getTownRenameCost() > 0.0) {
                            try {
                                if (TownySettings.isUsingEconomy() && !town.getAccount().pay(TownySettings.getTownRenameCost(), String.format("Town renamed to: %s", split[1]))) {
                                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_no_money"), TownyEconomyHandler.getFormattedBalance(TownySettings.getTownRenameCost())));
                                }
                            }
                            catch (EconomyException e5) {
                                throw new TownyException("Economy Error");
                            }
                        }
                        if (!NameValidation.isBlacklistName(split[1])) {
                            townRename(player, town, split[1]);
                        }
                        else {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                        }
                    }
                    else if (split[0].equalsIgnoreCase("tag")) {
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /town set tag PLTC");
                        }
                        else if (split[1].equalsIgnoreCase("clear")) {
                            try {
                                town.setTag(" ");
                                if (admin) {
                                    TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_reset_town_tag"), player.getName()));
                                }
                                TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_reset_town_tag"), player.getName()));
                            }
                            catch (TownyException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                            }
                        }
                        else {
                            try {
                                town.setTag(NameValidation.checkAndFilterName(split[1]));
                                if (admin) {
                                    TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_set_town_tag"), player.getName(), town.getTag()));
                                }
                                TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_set_town_tag"), player.getName(), town.getTag()));
                            }
                            catch (TownyException | InvalidNameException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                            }
                        }
                    }
                    else if (split[0].equalsIgnoreCase("homeblock")) {
                        final Coord coord = Coord.parseCoord((Entity)player);
                        try {
                            if (TownyWar.isUnderAttack(town) && TownySettings.isFlaggedInteractionTown()) {
                                throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_town_under_attack"));
                            }
                            if (System.currentTimeMillis() - TownyWar.lastFlagged(town) < TownySettings.timeToWaitAfterFlag()) {
                                throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_recently_attacked"));
                            }
                            if (TownyAPI.getInstance().isWarTime()) {
                                throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                            }
                            final TownyWorld world = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
                            if (world.getMinDistanceFromOtherTowns(coord, resident.getTown()) < TownySettings.getMinDistanceFromTownHomeblocks()) {
                                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("homeblock")));
                            }
                            if (TownySettings.getMaxDistanceBetweenHomeblocks() > 0 && world.getMinDistanceFromOtherTowns(coord, resident.getTown()) > TownySettings.getMaxDistanceBetweenHomeblocks() && world.hasTowns()) {
                                throw new TownyException(TownySettings.getLangString("msg_too_far"));
                            }
                            final TownBlock townBlock = townyUniverse.getDataSource().getWorld(player.getWorld().getName()).getTownBlock(coord);
                            oldWorld = town.getWorld();
                            town.setHomeBlock(townBlock);
                            town.setSpawn(player.getLocation());
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_town_home"), coord.toString()));
                        }
                        catch (TownyException e3) {
                            TownyMessaging.sendErrorMsg(player, e3.getMessage());
                            return;
                        }
                    }
                    else {
                        if (split[0].equalsIgnoreCase("spawn")) {
                            try {
                                town.setSpawn(player.getLocation());
                                if (town.isCapital()) {
                                    nation.recheckTownDistance();
                                }
                                TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_town_spawn"));
                                break Label_3700;
                            }
                            catch (TownyException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                                return;
                            }
                        }
                        if (split[0].equalsIgnoreCase("outpost")) {
                            try {
                                final TownyWorld townyWorld = townyUniverse.getDataSource().getWorld(player.getLocation().getWorld().getName());
                                if (townyWorld.getTownBlock(Coord.parseCoord(player.getLocation())).getTown().getName().equals(town.getName())) {
                                    town.addOutpostSpawn(player.getLocation());
                                    TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_outpost_spawn"));
                                }
                                else {
                                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_not_own_area"));
                                }
                                break Label_3700;
                            }
                            catch (TownyException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                                return;
                            }
                        }
                        if (split[0].equalsIgnoreCase("jail")) {
                            try {
                                town.addJailSpawn(player.getLocation());
                                TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_jail_spawn"));
                                break Label_3700;
                            }
                            catch (TownyException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                                return;
                            }
                        }
                        if (!split[0].equalsIgnoreCase("perm")) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "town"));
                            return;
                        }
                        try {
                            toggleTest(player, town, StringMgmt.join(split, " "));
                        }
                        catch (Exception e2) {
                            TownyMessaging.sendErrorMsg(player, e2.getMessage());
                            return;
                        }
                        final String[] newSplit = StringMgmt.remFirstArg(split);
                        setTownBlockOwnerPermissions(player, town, newSplit);
                    }
                }
            }
            townyUniverse.getDataSource().saveTown(town);
            townyUniverse.getDataSource().saveTownList();
            if (nation != null) {
                townyUniverse.getDataSource().saveNation(nation);
            }
            if (oldWorld != null) {
                townyUniverse.getDataSource().saveWorld(town.getWorld());
                townyUniverse.getDataSource().saveWorld(oldWorld);
            }
        }
    }
    
    public void townBuy(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Town town;
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            town = resident.getTown();
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        if (!TownySettings.isSellingBonusBlocks(town) && !TownySettings.isBonusBlocksPerTownLevel()) {
            TownyMessaging.sendErrorMsg(player, "Config.yml max_purchased_blocks: '0' ");
            return;
        }
        if (TownySettings.isBonusBlocksPerTownLevel() && TownySettings.getMaxBonusBlocks(town) == 0) {
            TownyMessaging.sendErrorMsg(player, "Config.yml town_level townBlockBonusBuyAmount: 0");
            return;
        }
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/town buy"));
            final String line = "§e[Purchased Bonus] §2Cost: §a%s§8 | §2Max: §a%d";
            player.sendMessage(String.format(line, TownyEconomyHandler.getFormattedBalance(town.getBonusBlockCost()), TownySettings.getMaxPurchedBlocks(town)));
            if (TownySettings.getPurchasedBonusBlocksIncreaseValue() != 1.0) {
                player.sendMessage("§2Cost Increase per TownBlock: §a+" + new DecimalFormat("##.##%").format(TownySettings.getPurchasedBonusBlocksIncreaseValue() - 1.0));
            }
            player.sendMessage(ChatTools.formatCommand("", "/town buy", "bonus [n]", ""));
        }
        else {
            try {
                Label_0295: {
                    if (split[0].equalsIgnoreCase("bonus")) {
                        if (split.length == 2) {
                            try {
                                townBuyBonusTownBlocks(town, Integer.parseInt(split[1].trim()), player);
                                break Label_0295;
                            }
                            catch (NumberFormatException e) {
                                throw new TownyException(TownySettings.getLangString("msg_error_must_be_int"));
                            }
                        }
                        throw new TownyException(String.format(TownySettings.getLangString("msg_must_specify_amnt"), "/town buy bonus #"));
                    }
                }
                townyUniverse.getDataSource().saveTown(town);
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
            }
        }
    }
    
    public static int townBuyBonusTownBlocks(final Town town, final int inputN, final Object player) throws TownyException {
        if (inputN < 0) {
            throw new TownyException(TownySettings.getLangString("msg_err_negative"));
        }
        final int current = town.getPurchasedBlocks();
        int n;
        if (current + inputN > TownySettings.getMaxPurchedBlocks(town)) {
            n = TownySettings.getMaxPurchedBlocks(town) - current;
        }
        else {
            n = inputN;
        }
        if (n == 0) {
            return n;
        }
        final double cost = town.getBonusBlockCostN(n);
        try {
            final boolean pay = town.getAccount().pay(cost, String.format("Town Buy Bonus (%d)", n));
            if (TownySettings.isUsingEconomy() && !pay) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_no_funds_to_buy"), n, "bonus town blocks", TownyEconomyHandler.getFormattedBalance(cost)));
            }
            if (TownySettings.isUsingEconomy() && pay) {
                town.addPurchasedBlocks(n);
                TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_buy"), n, "bonus town blocks", TownyEconomyHandler.getFormattedBalance(cost)));
            }
        }
        catch (EconomyException e1) {
            throw new TownyException("Economy Error");
        }
        return n;
    }
    
    public static void newTown(final Player player, final String name, final String mayorName, final boolean noCharge) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final PreNewTownEvent preEvent = new PreNewTownEvent(player, name);
        Bukkit.getPluginManager().callEvent((Event)preEvent);
        if (preEvent.isCancelled()) {
            TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
            return;
        }
        try {
            if (TownyAPI.getInstance().isWarTime()) {
                throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
            }
            if (TownySettings.hasTownLimit() && townyUniverse.getDataSource().getTowns().size() >= TownySettings.getTownLimit()) {
                throw new TownyException(TownySettings.getLangString("msg_err_universe_limit"));
            }
            String filteredName;
            try {
                filteredName = NameValidation.checkAndFilterName(name);
            }
            catch (InvalidNameException e2) {
                filteredName = null;
            }
            if (filteredName == null || townyUniverse.getDataSource().hasTown(filteredName)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
            }
            final Resident resident = townyUniverse.getDataSource().getResident(mayorName);
            if (resident.hasTown()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_already_res"), resident.getName()));
            }
            final TownyWorld world = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
            if (!world.isUsingTowny()) {
                throw new TownyException(TownySettings.getLangString("msg_set_use_towny_off"));
            }
            if (!world.isClaimable()) {
                throw new TownyException(TownySettings.getLangString("msg_not_claimable"));
            }
            final Coord key = Coord.parseCoord((Entity)player);
            if (world.hasTownBlock(key)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_already_claimed_1"), key));
            }
            if (world.getMinDistanceFromOtherTownsPlots(key) < TownySettings.getMinDistanceFromTownPlotblocks()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("townblock")));
            }
            if (world.getMinDistanceFromOtherTowns(key) < TownySettings.getMinDistanceFromTownHomeblocks()) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("homeblock")));
            }
            if (TownySettings.getMaxDistanceBetweenHomeblocks() > 0 && world.getMinDistanceFromOtherTowns(key) > TownySettings.getMaxDistanceBetweenHomeblocks() && world.hasTowns()) {
                throw new TownyException(TownySettings.getLangString("msg_too_far"));
            }
            if (!noCharge && TownySettings.isUsingEconomy() && !resident.getAccount().pay(TownySettings.getNewTownPrice(), "New Town Cost")) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_no_funds_new_town2"), resident.getName().equals(player.getName()) ? "You" : resident.getName(), TownySettings.getNewTownPrice()));
            }
            newTown(world, name, resident, key, player.getLocation(), player);
            TownyMessaging.sendGlobalMessage(TownySettings.getNewTownMsg(player.getName(), StringMgmt.remUnderscore(name)));
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
        catch (EconomyException x2) {
            TownyMessaging.sendErrorMsg(player, "No valid economy found, your server admin might need to install Vault.jar or set using_economy: false in the Towny config.yml");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Town newTown(final TownyWorld world, final String name, final Resident resident, final Coord key, final Location spawn, final Player player) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        world.newTownBlock(key);
        townyUniverse.getDataSource().newTown(name);
        final Town town = townyUniverse.getDataSource().getTown(name);
        town.addResident(resident);
        town.setMayor(resident);
        final TownBlock townBlock = world.getTownBlock(key);
        townBlock.setTown(town);
        town.setHomeBlock(townBlock);
        townBlock.setType(townBlock.getType());
        town.setSpawn(spawn);
        town.setUuid(UUID.randomUUID());
        town.setRegistered(System.currentTimeMillis());
        if (world.isUsingPlotManagementRevert()) {
            PlotBlockData plotChunk = TownyRegenAPI.getPlotChunk(townBlock);
            if (plotChunk != null) {
                TownyRegenAPI.deletePlotChunk(plotChunk);
            }
            else {
                plotChunk = new PlotBlockData(townBlock);
                plotChunk.initialize();
            }
            TownyRegenAPI.addPlotChunkSnapshot(plotChunk);
            plotChunk = null;
        }
        TownyMessaging.sendDebugMsg("Creating new Town account: town-" + name);
        if (TownySettings.isUsingEconomy()) {
            try {
                town.getAccount().setBalance(0.0, "Deleting Town");
            }
            catch (EconomyException e) {
                e.printStackTrace();
            }
        }
        townyUniverse.getDataSource().saveResident(resident);
        townyUniverse.getDataSource().saveTownBlock(townBlock);
        townyUniverse.getDataSource().saveTown(town);
        townyUniverse.getDataSource().saveWorld(world);
        townyUniverse.getDataSource().saveTownList();
        townyUniverse.getDataSource().saveTownBlockList();
        TownCommand.plugin.updateCache(townBlock.getWorldCoord());
        BukkitTools.getPluginManager().callEvent((Event)new NewTownEvent(town));
        return town;
    }
    
    public static void townRename(final Player player, Town town, final String newName) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final TownPreRenameEvent event = new TownPreRenameEvent(town, newName);
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_rename_cancelled"));
            return;
        }
        try {
            townyUniverse.getDataSource().renameTown(town, newName);
            town = townyUniverse.getDataSource().getTown(newName);
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_name"), player.getName(), town.getName()));
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public void townLeave(final Player player) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Resident resident;
        Town town;
        try {
            if (TownyAPI.getInstance().isWarTime()) {
                throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
            }
            resident = townyUniverse.getDataSource().getResident(player.getName());
            town = resident.getTown();
            if (TownyWar.isUnderAttack(town) && TownySettings.isFlaggedInteractionTown()) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_war_flag_deny_town_under_attack"));
                return;
            }
            if (System.currentTimeMillis() - TownyWar.lastFlagged(town) < TownySettings.timeToWaitAfterFlag()) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_war_flag_deny_recently_attacked"));
                return;
            }
            TownCommand.plugin.deleteCache(resident.getName());
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        if (resident.isMayor()) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getMayorAbondonMsg());
            return;
        }
        if (resident.isJailed()) {
            try {
                if (resident.getJailTown().equals(resident.getTown().getName())) {
                    if (TownySettings.JailDeniesTownLeave()) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_cannot_abandon_town_while_jailed"));
                        return;
                    }
                    resident.setJailed(false);
                    resident.setJailSpawn(0);
                    resident.setJailTown("");
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_escaped_jail_by_leaving_town"), resident.getName()));
                }
            }
            catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
        try {
            townRemoveResident(town, resident);
        }
        catch (EmptyTownException et) {
            townyUniverse.getDataSource().removeTown(et.getTown());
        }
        catch (NotRegisteredException x2) {
            TownyMessaging.sendErrorMsg(player, x2.getMessage());
            return;
        }
        townyUniverse.getDataSource().saveResident(resident);
        townyUniverse.getDataSource().saveTown(town);
        TownCommand.plugin.resetCache();
        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_left_town"), resident.getName()));
        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_left_town"), resident.getName()));
        try {
            checkTownResidents(town, resident);
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }
    
    public static void townSpawn(final Player player, final String[] split, final Boolean outpost) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            if (split.length == 0 || (split.length > 0 && outpost)) {
                if (!resident.hasTown()) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_dont_belong_town"));
                    return;
                }
                final Town town = resident.getTown();
                final String notAffordMSG = TownySettings.getLangString("msg_err_cant_afford_tp");
                SpawnUtil.sendToTownySpawn(player, split, town, notAffordMSG, outpost, SpawnType.TOWN);
            }
            else {
                final Town town = townyUniverse.getDataSource().getTown(split[0]);
                final String notAffordMSG = String.format(TownySettings.getLangString("msg_err_cant_afford_tp_town"), town.getName());
                SpawnUtil.sendToTownySpawn(player, split, town, notAffordMSG, outpost, SpawnType.TOWN);
            }
        }
        catch (NotRegisteredException e) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
        }
    }
    
    public void townDelete(final Player player, final String[] split) {
        Town town = null;
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            try {
                final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeDelayFullTownRemoval()) {
                    final long durationMillis = (long)(TownySettings.getWarSiegeRuinsRemovalDelayMinutes() * 60000.0);
                    final String durationFormatted = TimeMgmt.getFormattedTimeValue((double)durationMillis);
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_siege_war_delete_town_warning"), durationFormatted));
                }
                ConfirmationHandler.addConfirmation(resident, ConfirmationType.TOWN_DELETE, null);
                TownyMessaging.sendConfirmationMessage((CommandSender)player, null, null, null, null);
                return;
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
        }
        try {
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_DELETE.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_admin_only_delete_town"));
            }
            town = townyUniverse.getDataSource().getTown(split[0]);
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        TownyMessaging.sendGlobalMessage(TownySettings.getDelTownMsg(town));
        townyUniverse.getDataSource().removeTown(town);
    }
    
    public static void townKick(final Player player, final String[] names) {
        Resident resident;
        Town town;
        try {
            resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            town = resident.getTown();
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        townKickResidents(player, resident, town, ResidentUtil.getValidatedResidents(player, names));
        TownCommand.plugin.resetCache();
    }
    
    public static void townAddResidents(final Object sender, final Town town, final List<Resident> invited) {
        String name;
        if (sender instanceof Player) {
            name = ((Player)sender).getName();
        }
        else {
            name = null;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        for (final Resident newMember : new ArrayList<Resident>(invited)) {
            try {
                final TownPreAddResidentEvent preEvent = new TownPreAddResidentEvent(town, newMember);
                Bukkit.getPluginManager().callEvent((Event)preEvent);
                if (preEvent.isCancelled()) {
                    TownyMessaging.sendErrorMsg(sender, preEvent.getCancelMessage());
                    return;
                }
                if (BukkitTools.matchPlayer(newMember.getName()).isEmpty()) {
                    TownyMessaging.sendErrorMsg(sender, String.format(TownySettings.getLangString("msg_offline_no_join"), newMember.getName()));
                    invited.remove(newMember);
                }
                else if (!townyUniverse.getPermissionSource().has(BukkitTools.getPlayer(newMember.getName()), PermissionNodes.TOWNY_TOWN_RESIDENT.getNode())) {
                    TownyMessaging.sendErrorMsg(sender, String.format(TownySettings.getLangString("msg_not_allowed_join"), newMember.getName()));
                    invited.remove(newMember);
                }
                else if (TownySettings.getMaxResidentsPerTown() > 0 && town.getResidents().size() >= TownySettings.getMaxResidentsPerTown()) {
                    TownyMessaging.sendErrorMsg(sender, String.format(TownySettings.getLangString("msg_err_max_residents_per_town_reached"), TownySettings.getMaxResidentsPerTown()));
                    invited.remove(newMember);
                }
                else if (TownySettings.getTownInviteCooldown() > 0L && System.currentTimeMillis() / 1000L - newMember.getRegistered() / 1000L < TownySettings.getTownInviteCooldown()) {
                    TownyMessaging.sendErrorMsg(sender, String.format(TownySettings.getLangString("msg_err_resident_doesnt_meet_invite_cooldown"), newMember));
                    invited.remove(newMember);
                }
                else {
                    town.addResidentCheck(newMember);
                    townInviteResident(name, town, newMember);
                }
            }
            catch (TownyException e) {
                invited.remove(newMember);
                TownyMessaging.sendErrorMsg(sender, e.getMessage());
            }
            if (town.hasOutlaw(newMember)) {
                try {
                    town.removeOutlaw(newMember);
                }
                catch (NotRegisteredException ex) {}
            }
        }
        if (invited.size() > 0) {
            StringBuilder msg = new StringBuilder();
            if (name == null) {
                name = "Console";
            }
            for (final Resident newMember2 : invited) {
                msg.append(newMember2.getName()).append(", ");
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            msg = new StringBuilder(String.format(TownySettings.getLangString("msg_invited_join_town"), name, msg.toString()));
            TownyMessaging.sendPrefixedTownMessage(town, msg.toString());
            townyUniverse.getDataSource().saveTown(town);
        }
        else {
            TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    public static void townAddResident(final Town town, final Resident resident) throws AlreadyRegisteredException {
        town.addResident(resident);
        TownCommand.plugin.deleteCache(resident.getName());
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.getDataSource().saveResident(resident);
        townyUniverse.getDataSource().saveTown(town);
    }
    
    private static void townInviteResident(final String sender, final Town town, final Resident newMember) throws TownyException {
        final PlayerJoinTownInvite invite = new PlayerJoinTownInvite(sender, town, newMember);
        try {
            if (InviteHandler.inviteIsActive(invite)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_player_already_invited"), newMember.getName()));
            }
            newMember.newReceivedInvite(invite);
            town.newSentInvite(invite);
            InviteHandler.addInvite(invite);
            TownyMessaging.sendRequestMessage((CommandSender)TownyAPI.getInstance().getPlayer(newMember), invite);
            Bukkit.getPluginManager().callEvent((Event)new TownInvitePlayerEvent(invite));
        }
        catch (TooManyInvitesException e) {
            newMember.deleteReceivedInvite(invite);
            town.deleteSentInvite(invite);
            throw new TownyException(e.getMessage());
        }
    }
    
    private static void townRevokeInviteResident(final Object sender, final Town town, final List<Resident> residents) {
        for (final Resident invited : residents) {
            if (InviteHandler.inviteIsActive(town, invited)) {
                for (final Invite invite : invited.getReceivedInvites()) {
                    if (invite.getSender().equals(town)) {
                        try {
                            InviteHandler.declineInvite(invite, true);
                            TownyMessaging.sendMessage(sender, TownySettings.getLangString("town_revoke_invite_successful"));
                            break;
                        }
                        catch (InvalidObjectException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    public static void townRemoveResident(final Town town, final Resident resident) throws EmptyTownException, NotRegisteredException {
        town.removeResident(resident);
        TownCommand.plugin.deleteCache(resident.getName());
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.getDataSource().saveResident(resident);
        townyUniverse.getDataSource().saveTown(town);
    }
    
    public static void townKickResidents(final Object sender, final Resident resident, final Town town, final List<Resident> kicking) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
        for (final Resident member : new ArrayList<Resident>(kicking)) {
            if (resident == member) {
                TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_you_cannot_kick_yourself"));
                kicking.remove(member);
            }
            if (member.isMayor() || town.hasAssistant(member)) {
                TownyMessaging.sendErrorMsg(sender, String.format(TownySettings.getLangString("msg_you_cannot_kick_this_resident"), member));
                kicking.remove(member);
            }
            else {
                try {
                    townRemoveResident(town, member);
                }
                catch (NotRegisteredException e2) {
                    kicking.remove(member);
                }
                catch (EmptyTownException ex) {}
            }
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (kicking.size() > 0) {
            StringBuilder msg = new StringBuilder();
            for (final Resident member2 : kicking) {
                msg.append(member2.getName()).append(", ");
                final Player p = BukkitTools.getPlayer(member2.getName());
                if (p != null) {
                    p.sendMessage(String.format(TownySettings.getLangString("msg_kicked_by"), (player != null) ? player.getName() : "CONSOLE"));
                }
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            msg = new StringBuilder(String.format(TownySettings.getLangString("msg_kicked"), (player != null) ? player.getName() : "CONSOLE", msg.toString()));
            TownyMessaging.sendPrefixedTownMessage(town, msg.toString());
            try {
                if (!(sender instanceof Player) || !townyUniverse.getDataSource().getResident(player.getName()).hasTown() || !TownyUniverse.getInstance().getDataSource().getResident(player.getName()).getTown().equals(town)) {
                    TownyMessaging.sendMessage(sender, msg.toString());
                }
            }
            catch (NotRegisteredException ex2) {}
            townyUniverse.getDataSource().saveTown(town);
        }
        else {
            TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_invalid_name"));
        }
        try {
            checkTownResidents(town, resident);
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }
    
    public static void checkTownResidents(final Town town, final Resident removedResident) throws NotRegisteredException {
        if (!town.hasNation()) {
            return;
        }
        final Nation nation = town.getNation();
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (town.isCapital() && TownySettings.getNumResidentsCreateNation() > 0 && town.getNumResidents() < TownySettings.getNumResidentsCreateNation()) {
            for (final Town newCapital : town.getNation().getTowns()) {
                if (newCapital.getNumResidents() >= TownySettings.getNumResidentsCreateNation()) {
                    town.getNation().setCapital(newCapital);
                    if (TownySettings.getNumResidentsJoinNation() > 0 && removedResident.getTown().getNumResidents() < TownySettings.getNumResidentsJoinNation()) {
                        try {
                            town.getNation().removeTown(town);
                            townyUniverse.getDataSource().saveTown(town);
                            townyUniverse.getDataSource().saveNation(nation);
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_capital_not_enough_residents_left_nation"), town.getName()));
                        }
                        catch (EmptyNationException e) {
                            e.printStackTrace();
                        }
                    }
                    TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_not_enough_residents_no_longer_capital"), newCapital.getName()));
                    return;
                }
            }
            TownyMessaging.sendPrefixedNationMessage(town.getNation(), String.format(TownySettings.getLangString("msg_nation_disbanded_town_not_enough_residents"), town.getName()));
            TownyMessaging.sendGlobalMessage(TownySettings.getDelNationMsg(town.getNation()));
            townyUniverse.getDataSource().removeNation(town.getNation());
            if (TownySettings.isRefundNationDisbandLowResidents()) {
                try {
                    town.getAccount().pay(TownySettings.getNewNationPrice(), "nation refund");
                }
                catch (EconomyException e2) {
                    e2.printStackTrace();
                }
                TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_not_enough_residents_refunded"), TownySettings.getNewNationPrice()));
            }
        }
        else if (!town.isCapital() && TownySettings.getNumResidentsJoinNation() > 0 && town.getNumResidents() < TownySettings.getNumResidentsJoinNation()) {
            try {
                TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_town_not_enough_residents_left_nation"), town.getName()));
                town.getNation().removeTown(town);
                townyUniverse.getDataSource().saveTown(town);
                townyUniverse.getDataSource().saveNation(nation);
            }
            catch (EmptyNationException e3) {
                e3.printStackTrace();
            }
        }
    }
    
    public static void parseTownJoin(final CommandSender sender, final String[] args) {
        try {
            final boolean console = false;
            String residentName;
            String townName;
            String contextualResidentName;
            String exceptionMsg;
            if (sender instanceof Player) {
                if (args.length < 1) {
                    throw new Exception(String.format("Usage: /town join [town]", new Object[0]));
                }
                final Player player = (Player)sender;
                residentName = player.getName();
                townName = args[0];
                contextualResidentName = "You";
                exceptionMsg = "msg_err_already_res2";
            }
            else {
                if (args.length < 2) {
                    throw new Exception(String.format("Usage: town join [resident] [town]", new Object[0]));
                }
                residentName = args[0];
                townName = args[1];
                contextualResidentName = residentName;
                exceptionMsg = "msg_err_already_res";
            }
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            final Resident resident = townyUniverse.getDataSource().getResident(residentName);
            final Town town = townyUniverse.getDataSource().getTown(townName);
            if (resident.hasTown()) {
                throw new Exception(String.format(TownySettings.getLangString(exceptionMsg), contextualResidentName));
            }
            if (!console) {
                if (!town.isOpen()) {
                    throw new Exception(String.format(TownySettings.getLangString("msg_err_not_open"), town.getFormattedName()));
                }
                if (TownySettings.getMaxResidentsPerTown() > 0 && town.getResidents().size() >= TownySettings.getMaxResidentsPerTown()) {
                    throw new Exception(String.format(TownySettings.getLangString("msg_err_max_residents_per_town_reached"), TownySettings.getMaxResidentsPerTown()));
                }
                if (town.hasOutlaw(resident)) {
                    throw new Exception(TownySettings.getLangString("msg_err_outlaw_in_open_town"));
                }
            }
            final TownPreAddResidentEvent preEvent = new TownPreAddResidentEvent(town, resident);
            Bukkit.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(sender, preEvent.getCancelMessage());
                return;
            }
            townAddResident(town, resident);
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_join_town"), resident.getName()));
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg(sender, e.getMessage());
        }
    }
    
    public static void townAdd(final Object sender, final Town specifiedTown, String[] names) throws TownyException {
        String name;
        if (sender instanceof Player) {
            name = ((Player)sender).getName();
        }
        else {
            name = "Console";
        }
        Town town;
        try {
            if (name.equalsIgnoreCase("Console")) {
                town = specifiedTown;
            }
            else {
                final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(name);
                if (specifiedTown == null) {
                    town = resident.getTown();
                }
                else {
                    town = specifiedTown;
                }
            }
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(sender, x.getMessage());
            return;
        }
        if (TownySettings.getMaxDistanceFromTownSpawnForInvite() != 0) {
            if (!town.hasSpawn()) {
                throw new TownyException(TownySettings.getLangString("msg_err_townspawn_has_not_been_set"));
            }
            final Location spawnLoc = town.getSpawn();
            final ArrayList<String> newNames = new ArrayList<String>();
            for (final String nameForDistanceTest : names) {
                final int maxDistance = TownySettings.getMaxDistanceFromTownSpawnForInvite();
                final Player player = BukkitTools.getPlayer(nameForDistanceTest);
                final Location playerLoc = player.getLocation();
                final Double distance = spawnLoc.distance(playerLoc);
                if (distance <= maxDistance) {
                    newNames.add(nameForDistanceTest);
                }
                else {
                    TownyMessaging.sendMessage(sender, String.format(TownySettings.getLangString("msg_err_player_too_far_from_town_spawn"), nameForDistanceTest, maxDistance));
                }
            }
            names = newNames.toArray(new String[0]);
        }
        final List<String> reslist = new ArrayList<String>(Arrays.asList(names));
        final List<String> newreslist = new ArrayList<String>();
        final List<String> removeinvites = new ArrayList<String>();
        for (final String residents : reslist) {
            if (residents.startsWith("-")) {
                removeinvites.add(residents.substring(1));
            }
            else {
                newreslist.add(residents);
            }
        }
        names = newreslist.toArray(new String[0]);
        final String[] namestoremove = removeinvites.toArray(new String[0]);
        if (namestoremove.length != 0) {
            final List<Resident> toRevoke = getValidatedResidentsForInviteRevoke(sender, namestoremove, town);
            if (!toRevoke.isEmpty()) {
                townRevokeInviteResident(sender, town, toRevoke);
            }
        }
        if (names.length != 0) {
            townAddResidents(sender, town, ResidentUtil.getValidatedResidents(sender, names));
        }
        if (!name.equalsIgnoreCase("Console")) {
            TownCommand.plugin.resetCache(BukkitTools.getPlayerExact(name));
        }
    }
    
    public static void setTownBlockOwnerPermissions(final Player player, final TownBlockOwner townBlockOwner, final String[] split) {
        setTownBlockPermissions(player, townBlockOwner, townBlockOwner.getPermissions(), split, false);
    }
    
    public static void setTownBlockPermissions(final Player player, final TownBlockOwner townBlockOwner, final TownyPermission perm, final String[] split, final boolean friend) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatTools.formatTitle("/... set perm"));
            if (townBlockOwner instanceof Town) {
                player.sendMessage(ChatTools.formatCommand("Level", "[resident/nation/ally/outsider]", "", ""));
            }
            if (townBlockOwner instanceof Resident) {
                player.sendMessage(ChatTools.formatCommand("Level", "[friend/town/ally/outsider]", "", ""));
            }
            player.sendMessage(ChatTools.formatCommand("Type", "[build/destroy/switch/itemuse]", "", ""));
            player.sendMessage(ChatTools.formatCommand("", "set perm", "[on/off]", "Toggle all permissions"));
            player.sendMessage(ChatTools.formatCommand("", "set perm", "[level/type] [on/off]", ""));
            player.sendMessage(ChatTools.formatCommand("", "set perm", "[level] [type] [on/off]", ""));
            player.sendMessage(ChatTools.formatCommand("", "set perm", "reset", ""));
            if (townBlockOwner instanceof Town) {
                player.sendMessage(ChatTools.formatCommand("Eg", "/town set perm", "ally off", ""));
            }
            if (townBlockOwner instanceof Resident) {
                player.sendMessage(ChatTools.formatCommand("Eg", "/resident set perm", "friend build on", ""));
            }
        }
        else {
            if (friend && split[0].equalsIgnoreCase("friend")) {
                split[0] = "resident";
            }
            if (friend && split[0].equalsIgnoreCase("town")) {
                split[0] = "nation";
            }
            Label_0820: {
                if (split.length == 1) {
                    if (split[0].equalsIgnoreCase("reset")) {
                        for (final TownBlock townBlock : townBlockOwner.getTownBlocks()) {
                            if ((townBlockOwner instanceof Town && !townBlock.hasResident()) || (townBlockOwner instanceof Resident && townBlock.hasResident())) {
                                townBlock.setType(townBlock.getType());
                                townyUniverse.getDataSource().saveTownBlock(townBlock);
                            }
                        }
                        if (townBlockOwner instanceof Town) {
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_perms_reset"), "Town owned"));
                        }
                        else {
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_perms_reset"), "your"));
                        }
                        TownCommand.plugin.resetCache();
                        return;
                    }
                    try {
                        final boolean b = TownCommand.plugin.parseOnOff(split[0]);
                        perm.change(TownyPermissionChange.Action.ALL_PERMS, b, new Object[0]);
                        break Label_0820;
                    }
                    catch (Exception e) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_town_set_perm_syntax_error"));
                        return;
                    }
                }
                if (split.length == 2) {
                    boolean b;
                    try {
                        b = TownCommand.plugin.parseOnOff(split[1]);
                    }
                    catch (Exception e2) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_town_set_perm_syntax_error"));
                        return;
                    }
                    if (split[0].equalsIgnoreCase("friend")) {
                        split[0] = "resident";
                    }
                    else if (split[0].equalsIgnoreCase("town")) {
                        split[0] = "nation";
                    }
                    else if (split[0].equalsIgnoreCase("itemuse")) {
                        split[0] = "item_use";
                    }
                    try {
                        final TownyPermission.PermLevel permLevel = TownyPermission.PermLevel.valueOf(split[0].toUpperCase());
                        perm.change(TownyPermissionChange.Action.PERM_LEVEL, b, permLevel);
                    }
                    catch (IllegalArgumentException permLevelException) {
                        try {
                            final TownyPermission.ActionType actionType = TownyPermission.ActionType.valueOf(split[0].toUpperCase());
                            perm.change(TownyPermissionChange.Action.ACTION_TYPE, b, actionType);
                        }
                        catch (IllegalArgumentException actionTypeException) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_town_set_perm_syntax_error"));
                            return;
                        }
                    }
                }
                else if (split.length == 3) {
                    if (split[0].equalsIgnoreCase("friend")) {
                        split[0] = "resident";
                    }
                    else if (split[0].equalsIgnoreCase("town")) {
                        split[0] = "nation";
                    }
                    if (split[1].equalsIgnoreCase("itemuse")) {
                        split[1] = "item_use";
                    }
                    TownyPermission.PermLevel permLevel2;
                    TownyPermission.ActionType actionType2;
                    try {
                        permLevel2 = TownyPermission.PermLevel.valueOf(split[0].toUpperCase());
                        actionType2 = TownyPermission.ActionType.valueOf(split[1].toUpperCase());
                    }
                    catch (IllegalArgumentException ignore) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_town_set_perm_syntax_error"));
                        return;
                    }
                    try {
                        final boolean b2 = TownCommand.plugin.parseOnOff(split[2]);
                        perm.change(TownyPermissionChange.Action.SINGLE_PERM, b2, permLevel2, actionType2);
                    }
                    catch (Exception e3) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_town_set_perm_syntax_error"));
                        return;
                    }
                }
            }
            for (final TownBlock townBlock : townBlockOwner.getTownBlocks()) {
                if (townBlockOwner instanceof Town && !townBlock.hasResident() && !townBlock.isChanged()) {
                    townBlock.setType(townBlock.getType());
                    townyUniverse.getDataSource().saveTownBlock(townBlock);
                }
            }
            TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_perms"));
            TownyMessaging.sendMessage(player, "§2 Perm: " + ((townBlockOwner instanceof Resident) ? perm.getColourString().replace("n", "t") : perm.getColourString().replace("f", "r")));
            TownyMessaging.sendMessage(player, "§2 Perm: " + ((townBlockOwner instanceof Resident) ? perm.getColourString2().replace("n", "t") : perm.getColourString2().replace("f", "r")));
            TownyMessaging.sendMessage(player, "§2PvP: " + (perm.pvp ? "§4ON" : "§aOFF") + "§2" + "  Explosions: " + (perm.explosion ? "§4ON" : "§aOFF") + "§2" + "  Firespread: " + (perm.fire ? "§4ON" : "§aOFF") + "§2" + "  Mob Spawns: " + (perm.mobs ? "§4ON" : "§aOFF"));
            TownCommand.plugin.resetCache();
        }
    }
    
    public static void parseTownClaimCommand(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 1 && split[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatTools.formatTitle("/town claim"));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town claim", "", TownySettings.getLangString("msg_block_claim")));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town claim", "outpost", TownySettings.getLangString("mayor_help_3")));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town claim", "[circle/rect] [radius]", TownySettings.getLangString("mayor_help_4")));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town claim", "[circle/rect] auto", TownySettings.getLangString("mayor_help_5")));
        }
        else {
            try {
                if (TownyAPI.getInstance().isWarTime()) {
                    throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                }
                if (TownySettings.getWarSiegeClaimingDisabledNearSiegeZones()) {
                    final int claimDisableDistance = TownySettings.getWarSiegeClaimDisableDistanceBlocks();
                    for (final SiegeZone siegeZone : townyUniverse.getDataSource().getSiegeZones()) {
                        if (siegeZone.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && siegeZone.getFlagLocation().distance(player.getLocation()) < claimDisableDistance) {
                            throw new TownyException(TownySettings.getLangString("msg_err_siege_claim_too_near_siege_zone"));
                        }
                    }
                }
                final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                final Town town = resident.getTown();
                final TownyWorld world = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
                if (!world.isUsingTowny()) {
                    throw new TownyException(TownySettings.getLangString("msg_set_use_towny_off"));
                }
                double blockCost = 0.0;
                boolean attachedToEdge = true;
                boolean outpost = false;
                final boolean isAdmin = townyUniverse.getPermissionSource().isTownyAdmin(player);
                final Coord key = Coord.parseCoord(TownCommand.plugin.getCache(player).getLastLocation());
                List<WorldCoord> selection;
                if (split.length == 1 && split[0].equalsIgnoreCase("outpost")) {
                    if (!TownySettings.isAllowingOutposts()) {
                        throw new TownyException(TownySettings.getLangString("msg_outpost_disable"));
                    }
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    OutpostUtil.OutpostTests(town, resident, world, key, isAdmin, false);
                    if (world.hasTownBlock(key)) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_already_claimed_1"), key));
                    }
                    selection = AreaSelectionUtil.selectWorldCoordArea(town, new WorldCoord(world.getName(), key), new String[0]);
                    attachedToEdge = false;
                    outpost = true;
                }
                else {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    selection = AreaSelectionUtil.selectWorldCoordArea(town, new WorldCoord(world.getName(), key), split);
                    if (selection.size() > 1 && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN_MULTIPLE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (TownySettings.isUsingEconomy()) {
                        blockCost = town.getTownBlockCost();
                    }
                }
                if (world.getMinDistanceFromOtherTownsPlots(key, town) < TownySettings.getMinDistanceFromTownPlotblocks()) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("townblock")));
                }
                if (!town.hasHomeBlock() && world.getMinDistanceFromOtherTowns(key) < TownySettings.getMinDistanceFromTownHomeblocks()) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_too_close2"), TownySettings.getLangString("homeblock")));
                }
                TownyMessaging.sendDebugMsg("townClaim: Pre-Filter Selection [" + selection.size() + "] " + Arrays.toString(selection.toArray(new WorldCoord[0])));
                selection = AreaSelectionUtil.filterTownOwnedBlocks(selection);
                selection = AreaSelectionUtil.filterInvalidProximityTownBlocks(selection, town);
                TownyMessaging.sendDebugMsg("townClaim: Post-Filter Selection [" + selection.size() + "] " + Arrays.toString(selection.toArray(new WorldCoord[0])));
                checkIfSelectionIsValid(town, selection, attachedToEdge, blockCost, false);
                int blockedClaims = 0;
                for (final WorldCoord coord : selection) {
                    final TownPreClaimEvent preClaimEvent = new TownPreClaimEvent(town, new TownBlock(coord.getX(), coord.getZ(), world), player);
                    BukkitTools.getPluginManager().callEvent((Event)preClaimEvent);
                    if (preClaimEvent.isCancelled()) {
                        ++blockedClaims;
                    }
                }
                if (blockedClaims > 0) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_claim_error"), blockedClaims, selection.size()));
                }
                try {
                    if (selection.size() == 1 && !outpost) {
                        blockCost = town.getTownBlockCost();
                    }
                    else if (selection.size() == 1 && outpost) {
                        blockCost = TownySettings.getOutpostCost();
                    }
                    else {
                        blockCost = town.getTownBlockCostN(selection.size());
                    }
                    final double missingAmount = blockCost - town.getAccount().getHoldingBalance();
                    if (TownySettings.isUsingEconomy() && !town.getAccount().pay(blockCost, String.format("Town Claim (%d)", selection.size()))) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_no_funds_claim2"), selection.size(), TownyEconomyHandler.getFormattedBalance(blockCost), TownyEconomyHandler.getFormattedBalance(missingAmount), new DecimalFormat("#").format(missingAmount)));
                    }
                }
                catch (EconomyException e1) {
                    throw new TownyException("Economy Error");
                }
                new TownClaim(TownCommand.plugin, player, town, selection, outpost, true, false).start();
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
            }
        }
    }
    
    public static void parseTownUnclaimCommand(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 1 && split[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatTools.formatTitle("/town unclaim"));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town unclaim", "", TownySettings.getLangString("mayor_help_6")));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town unclaim", "[circle/rect] [radius]", TownySettings.getLangString("mayor_help_7")));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town unclaim", "all", TownySettings.getLangString("mayor_help_8")));
            player.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town unclaim", "outpost", TownySettings.getLangString("mayor_help_9")));
        }
        else {
            try {
                if (TownyAPI.getInstance().isWarTime()) {
                    throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                }
                final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                final Town town = resident.getTown();
                final TownyWorld world = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
                if (TownyWar.isUnderAttack(town) && TownySettings.isFlaggedInteractionTown()) {
                    throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_town_under_attack"));
                }
                if (System.currentTimeMillis() - TownyWar.lastFlagged(town) < TownySettings.timeToWaitAfterFlag()) {
                    throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_recently_attacked"));
                }
                if (split.length == 1 && split[0].equalsIgnoreCase("all")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM_ALL.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    new TownClaim(TownCommand.plugin, player, town, null, false, false, false).start();
                }
                else {
                    List<WorldCoord> selection = AreaSelectionUtil.selectWorldCoordArea(town, new WorldCoord(world.getName(), Coord.parseCoord(TownCommand.plugin.getCache(player).getLastLocation())), split);
                    selection = AreaSelectionUtil.filterOwnedBlocks(town, selection);
                    if (selection.isEmpty()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_empty_area_selection"));
                    }
                    if (selection.get(0).getTownBlock().isHomeBlock()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_cannot_unclaim_homeblock"));
                    }
                    new TownClaim(TownCommand.plugin, player, town, selection, false, false, false).start();
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_abandoned_area"), Arrays.toString(selection.toArray(new WorldCoord[0]))));
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
            }
        }
    }
    
    public static boolean isEdgeBlock(final TownBlockOwner owner, final List<WorldCoord> worldCoords) {
        for (final WorldCoord worldCoord : worldCoords) {
            if (isEdgeBlock(owner, worldCoord)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEdgeBlock(final TownBlockOwner owner, final WorldCoord worldCoord) {
        final int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int i = 0; i < 4; ++i) {
            try {
                final TownBlock edgeTownBlock = worldCoord.getTownyWorld().getTownBlock(new Coord(worldCoord.getX() + offset[i][0], worldCoord.getZ() + offset[i][1]));
                if (edgeTownBlock.isOwner(owner)) {
                    return true;
                }
            }
            catch (NotRegisteredException ex) {}
        }
        return false;
    }
    
    public static void checkIfSelectionIsValid(final TownBlockOwner owner, final List<WorldCoord> selection, final boolean attachedToEdge, double blockCost, final boolean force) throws TownyException {
        if (force) {
            return;
        }
        final Town town = (Town)owner;
        if (owner instanceof Town) {
            final int available = TownySettings.getMaxTownBlocks(town) - town.getTownBlocks().size();
            TownyMessaging.sendDebugMsg("Claim Check Available: " + available);
            TownyMessaging.sendDebugMsg("Claim Selection Size: " + selection.size());
            if (available - selection.size() < 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_enough_blocks"));
            }
        }
        try {
            if (selection.size() == 1) {
                blockCost = town.getTownBlockCost();
            }
            else {
                blockCost = town.getTownBlockCostN(selection.size());
            }
            final double missingAmount = blockCost - town.getAccount().getHoldingBalance();
            if (TownySettings.isUsingEconomy() && !((Town)owner).getAccount().canPayFromHoldings(blockCost)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_cant_afford_blocks2"), selection.size(), TownyEconomyHandler.getFormattedBalance(blockCost), TownyEconomyHandler.getFormattedBalance(missingAmount), new DecimalFormat("#").format(missingAmount)));
            }
        }
        catch (EconomyException e1) {
            throw new TownyException("Economy Error");
        }
        if (!attachedToEdge || isEdgeBlock(owner, selection) || town.getTownBlocks().isEmpty()) {
            return;
        }
        if (selection.size() == 0) {
            throw new TownyException(TownySettings.getLangString("msg_already_claimed_2"));
        }
        throw new TownyException(TownySettings.getLangString("msg_err_not_attached_edge"));
    }
    
    private void townWithdraw(final Player player, final int amount) {
        try {
            if (!TownySettings.getTownBankAllowWithdrawls()) {
                throw new TownyException(TownySettings.getLangString("msg_err_withdraw_disabled"));
            }
            if (amount < 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));
            }
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            final Town town = resident.getTown();
            if (System.currentTimeMillis() - TownyWar.lastFlagged(town) < TownySettings.timeToWaitAfterFlag()) {
                throw new TownyException("You cannot do this! You were attacked too recently!");
            }
            final Transaction transaction = new Transaction(TransactionType.WITHDRAW, player, amount);
            final TownPreTransactionEvent preEvent = new TownPreTransactionEvent(town, transaction);
            BukkitTools.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
                return;
            }
            town.withdrawFromBank(resident, amount);
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_xx_withdrew_xx"), resident.getName(), amount, "town"));
            BukkitTools.getPluginManager().callEvent((Event)new TownTransactionEvent(town, transaction));
        }
        catch (TownyException | EconomyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    private void townDeposit(final Player player, final int amount) {
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            final Town town = resident.getTown();
            final double bankcap = TownySettings.getTownBankCap();
            if (bankcap > 0.0 && amount + town.getAccount().getHoldingBalance() > bankcap) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
            }
            if (amount < 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));
            }
            final Transaction transaction = new Transaction(TransactionType.DEPOSIT, player, amount);
            final TownPreTransactionEvent preEvent = new TownPreTransactionEvent(town, transaction);
            BukkitTools.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
                return;
            }
            if (!resident.getAccount().payTo(amount, town, "Town Deposit")) {
                throw new TownyException(TownySettings.getLangString("msg_insuf_funds"));
            }
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_xx_deposited_xx"), resident.getName(), amount, "town"));
            BukkitTools.getPluginManager().callEvent((Event)new TownTransactionEvent(town, transaction));
        }
        catch (TownyException | EconomyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public static void townDeposit(final Player player, final Town town, final int amount) {
        try {
            final Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());
            final double bankcap = TownySettings.getTownBankCap();
            if (bankcap > 0.0 && amount + town.getAccount().getHoldingBalance() > bankcap) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
            }
            if (amount < 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));
            }
            final Transaction transaction = new Transaction(TransactionType.DEPOSIT, player, amount);
            final TownPreTransactionEvent preEvent = new TownPreTransactionEvent(town, transaction);
            BukkitTools.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
                return;
            }
            if (!resident.getAccount().payTo(amount, town, "Town Deposit from Nation member")) {
                throw new TownyException(TownySettings.getLangString("msg_insuf_funds"));
            }
            TownyMessaging.sendPrefixedNationMessage(resident.getTown().getNation(), String.format(TownySettings.getLangString("msg_xx_deposited_xx"), resident.getName(), amount, town + " town"));
            BukkitTools.getPluginManager().callEvent((Event)new TownTransactionEvent(town, transaction));
        }
        catch (EconomyException | TownyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public static List<Resident> getValidatedResidentsForInviteRevoke(final Object sender, final String[] names, final Town town) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Resident> toRevoke = new ArrayList<Resident>();
        for (final Invite invite : town.getSentInvites()) {
            for (final String name : names) {
                if (invite.getReceiver().getName().equalsIgnoreCase(name)) {
                    try {
                        toRevoke.add(townyUniverse.getDataSource().getResident(name));
                    }
                    catch (NotRegisteredException ex) {}
                }
            }
        }
        return toRevoke;
    }
    
    static {
        output = new ArrayList<String>();
        invite = new ArrayList<String>();
        BY_NUM_RESIDENTS = ((t1, t2) -> t2.getNumResidents() - t1.getNumResidents());
        BY_OPEN = ((t1, t2) -> t2.getNumResidents() - t1.getNumResidents());
        BY_NAME = ((t1, t2) -> t1.getName().compareTo(t2.getName()));
        BY_BANK_BALANCE = ((t1, t2) -> {
            try {
                return Double.compare(t2.getAccount().getHoldingBalance(), t1.getAccount().getHoldingBalance());
            }
            catch (EconomyException e) {
                throw new RuntimeException("Failed to get balance. Aborting.");
            }
        });
        BY_TOWNBLOCKS_CLAIMED = ((t1, t2) -> Double.compare(t2.getTownBlocks().size(), t1.getTownBlocks().size()));
        BY_NUM_ONLINE = ((t1, t2) -> TownyAPI.getInstance().getOnlinePlayers(t2).size() - TownyAPI.getInstance().getOnlinePlayers(t1).size());
        TownCommand.output.add(ChatTools.formatTitle("/town"));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "", TownySettings.getLangString("town_help_1")));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "[town]", TownySettings.getLangString("town_help_3")));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "new [name]", TownySettings.getLangString("town_help_11")));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "here", TownySettings.getLangString("town_help_4")));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "list", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "online", TownySettings.getLangString("town_help_10")));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "leave", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "reslist", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "ranklist", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "outlawlist", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "plots", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "outlaw add/remove [name]", ""));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "say", "[message]"));
        TownCommand.output.add(ChatTools.formatCommand("", "/town", "spawn", TownySettings.getLangString("town_help_5")));
        TownCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/town", "deposit [$]", ""));
        TownCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/town", "rank add/remove [resident] [rank]", ""));
        TownCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/town", "mayor ?", TownySettings.getLangString("town_help_8")));
        TownCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/town", "new [town] " + TownySettings.getLangString("town_help_2"), TownySettings.getLangString("town_help_7")));
        TownCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/town", "delete [town]", ""));
        TownCommand.invite.add(ChatTools.formatTitle("/town invite"));
        TownCommand.invite.add(ChatTools.formatCommand("", "/town", "invite [player]", TownySettings.getLangString("town_invite_help_1")));
        TownCommand.invite.add(ChatTools.formatCommand("", "/town", "invite -[player]", TownySettings.getLangString("town_invite_help_2")));
        TownCommand.invite.add(ChatTools.formatCommand("", "/town", "invite sent", TownySettings.getLangString("town_invite_help_3")));
        TownCommand.invite.add(ChatTools.formatCommand("", "/town", "invite received", TownySettings.getLangString("town_invite_help_4")));
        TownCommand.invite.add(ChatTools.formatCommand("", "/town", "invite accept [nation]", TownySettings.getLangString("town_invite_help_5")));
        TownCommand.invite.add(ChatTools.formatCommand("", "/town", "invite deny [nation]", TownySettings.getLangString("town_invite_help_6")));
    }
}
