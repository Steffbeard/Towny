// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import com.palmergames.bukkit.towny.utils.SpawnUtil;
import com.palmergames.bukkit.towny.object.SpawnType;
import com.palmergames.bukkit.towny.event.NationPreRenameEvent;
import com.palmergames.bukkit.towny.event.NationRemoveEnemyEvent;
import com.palmergames.bukkit.towny.event.NationPreRemoveEnemyEvent;
import com.palmergames.bukkit.towny.event.NationAddEnemyEvent;
import com.palmergames.bukkit.towny.event.NationPreAddEnemyEvent;
import com.palmergames.bukkit.towny.event.NationRequestAllyNationEvent;
import com.palmergames.bukkit.towny.object.inviteobjects.NationAllyNationInvite;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.event.NationInviteTownEvent;
import com.palmergames.bukkit.towny.object.inviteobjects.TownJoinNationInvite;
import java.io.InvalidObjectException;
import java.util.Arrays;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import java.util.UUID;
import javax.naming.InvalidNameException;
import com.palmergames.bukkit.util.NameValidation;
import java.util.Collections;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.event.NationTransactionEvent;
import com.palmergames.bukkit.towny.event.NationPreTransactionEvent;
import com.palmergames.bukkit.towny.object.Transaction;
import com.palmergames.bukkit.towny.object.TransactionType;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.utils.ResidentUtil;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import java.util.ArrayList;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.NationPreAddTownEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.util.ChatTools;
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
import com.palmergames.bukkit.towny.object.Nation;
import java.util.Comparator;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class NationCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> nation_help;
    private static final List<String> king_help;
    private static final List<String> alliesstring;
    private static final List<String> invite;
    private static final Comparator<Nation> BY_NUM_RESIDENTS;
    private static final Comparator<Nation> BY_NAME;
    private static final Comparator<Nation> BY_BANK_BALANCE;
    private static final Comparator<Nation> BY_TOWNBLOCKS_CLAIMED;
    private static final Comparator<Nation> BY_NUM_TOWNS;
    private static final Comparator<Nation> BY_NUM_ONLINE;
    
    public NationCommand(final Towny instance) {
        NationCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (args == null) {
                for (final String line : NationCommand.nation_help) {
                    player.sendMessage(line);
                }
                this.parseNationCommand(player, args);
            }
            else {
                this.parseNationCommand(player, args);
            }
        }
        else {
            try {
                this.parseNationCommandForConsole(sender, args);
            }
            catch (TownyException ex) {}
        }
        return true;
    }
    
    private void parseNationCommandForConsole(final CommandSender sender, final String[] split) throws TownyException {
        if (split.length == 0 || split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
            for (final String line : NationCommand.nation_help) {
                sender.sendMessage(line);
            }
        }
        else if (split[0].equalsIgnoreCase("list")) {
            this.listNations(sender, split);
        }
        else {
            try {
                final Nation nation = TownyUniverse.getInstance().getDataSource().getNation(split[0]);
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)NationCommand.plugin, () -> TownyMessaging.sendMessage(sender, TownyFormatter.getStatus(nation)));
            }
            catch (NotRegisteredException x) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
            }
        }
    }
    
    @SuppressWarnings("static-access")
	public void parseNationCommand(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final String nationCom = "/nation";
        try {
        	if (split.length == 0)
				Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
					try {
						Resident resident = townyUniverse.getDataSource().getResident(player.getName());
						Town town = resident.getTown();
						Nation nation = town.getNation();
						TownyMessaging.sendMessage(player, TownyFormatter.getStatus(nation));
					} catch (NotRegisteredException x) {
						TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_dont_belong_nation"));
                    }
                });
            
            else if (split[0].equalsIgnoreCase("?")) {
                for (final String line : NationCommand.nation_help) {
                    player.sendMessage(line);
                }
            }
            else if (split[0].equalsIgnoreCase("list")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_LIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.listNations((CommandSender)player, split);
            }
            else if (split[0].equalsIgnoreCase("townlist")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_TOWNLIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                Nation nation2 = null;
                try {
                    if (split.length == 1) {
                        nation2 = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                    }
                    else {
                        nation2 = townyUniverse.getDataSource().getNation(split[1]);
                    }
                }
                catch (Exception e) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
                    return;
                }
                TownyMessaging.sendMessage(player, ChatTools.formatTitle(TownyFormatter.getFormattedName(nation2)));
                TownyMessaging.sendMessage(player, ChatTools.listArr(TownyFormatter.getFormattedNames(nation2.getTowns().toArray(new Town[0])), String.format(TownySettings.getLangString("status_nation_towns"), nation2.getTowns().size())));
            }
            else if (split[0].equalsIgnoreCase("allylist")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLYLIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                Nation nation2 = null;
                try {
                    if (split.length == 1) {
                        nation2 = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                    }
                    else {
                        nation2 = townyUniverse.getDataSource().getNation(split[1]);
                    }
                }
                catch (Exception e) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
                    return;
                }
                if (nation2.getAllies().isEmpty()) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_nation_has_no_allies"));
                }
                else {
                    TownyMessaging.sendMessage(player, ChatTools.formatTitle(TownyFormatter.getFormattedName(nation2)));
                    TownyMessaging.sendMessage(player, ChatTools.listArr(TownyFormatter.getFormattedNames(nation2.getAllies().toArray(new Nation[0])), String.format(TownySettings.getLangString("status_nation_allies"), nation2.getAllies().size())));
                }
            }
            else if (split[0].equalsIgnoreCase("enemylist")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ENEMYLIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                Nation nation2 = null;
                try {
                    if (split.length == 1) {
                        nation2 = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                    }
                    else {
                        nation2 = townyUniverse.getDataSource().getNation(split[1]);
                    }
                }
                catch (Exception e) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_name"));
                    return;
                }
                if (nation2.getEnemies().isEmpty()) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_nation_has_no_enemies"));
                }
                else {
                    TownyMessaging.sendMessage(player, ChatTools.formatTitle(TownyFormatter.getFormattedName(nation2)));
                    TownyMessaging.sendMessage(player, ChatTools.listArr(TownyFormatter.getFormattedNames(nation2.getEnemies().toArray(new Nation[0])), String.format(TownySettings.getLangString("status_nation_enemies"), nation2.getEnemies().size())));
                }
            }
            else if (split[0].equalsIgnoreCase("new")) {
                final Resident resident2 = townyUniverse.getDataSource().getResident(player.getName());
                if (TownySettings.getNumResidentsCreateNation() > 0 && resident2.getTown().getNumResidents() < TownySettings.getNumResidentsCreateNation()) {
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_enough_residents_new_nation"), new Object[0]));
                    return;
                }
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_NEW.getNode())) {
                    throw new TownyException(TownySettings.getNotPermToNewNationLine());
                }
                if (split.length == 1) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_nation_name"));
                }
                else if (split.length >= 2) {
                    if (!resident2.isMayor() && !resident2.getTown().hasAssistant(resident2)) {
                        throw new TownyException(TownySettings.getLangString("msg_peasant_right"));
                    }
                    final String[] newSplit = StringMgmt.remFirstArg(split);
                    final String nationName = String.join("_", (CharSequence[])newSplit);
                    newNation(player, nationName, resident2.getTown().getName(), false);
                }
            }
            else if (split[0].equalsIgnoreCase("join")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_JOIN.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.parseNationJoin(player, StringMgmt.remFirstArg(split));
            }
            else if (split[0].equalsIgnoreCase("merge")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_MERGE.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (split.length == 1) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_specify_nation_name"));
                }
                else if (split.length == 2) {
                    final Resident resident2 = townyUniverse.getDataSource().getResident(player.getName());
                    if (!resident2.isKing()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_merging_for_kings_only"));
                    }
                    this.mergeNation(player, split[1]);
                }
            }
            else if (split[0].equalsIgnoreCase("withdraw")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_WITHDRAW.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (TownySettings.isBankActionLimitedToBankPlots()) {
                    if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                    final TownBlock tb = TownyAPI.getInstance().getTownBlock(player.getLocation());
                    final Nation tbNation = tb.getTown().getNation();
                    final Nation pNation = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                    if (tbNation != pNation || !tb.getTown().isCapital()) {
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
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_nation_capital"));
                    }
                    final Coord coord = Coord.parseCoord(NationCommand.plugin.getCache(player).getLastLocation());
                    final Town town2 = townyUniverse.getDataSource().getWorld(player.getLocation().getWorld().getName()).getTownBlock(coord).getTown();
                    if (!town2.isCapital()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_nation_capital"));
                    }
                    final Nation nation3 = town2.getNation();
                    if (!townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation().equals(nation3)) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_nation_capital"));
                    }
                }
                if (split.length == 2) {
                    try {
                        this.nationWithdraw(player, Integer.parseInt(split[1].trim()));
                    }
                    catch (NumberFormatException e2) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                    }
                }
                else {
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_must_specify_amnt"), nationCom));
                }
            }
            else if (split[0].equalsIgnoreCase("leave")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_LEAVE.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.nationLeave(player);
            }
            else if (split[0].equalsIgnoreCase("spawn")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SPAWN.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                final String[] newSplit2 = StringMgmt.remFirstArg(split);
                nationSpawn(player, newSplit2);
            }
            else if (split[0].equalsIgnoreCase("deposit")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_DEPOSIT.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (TownySettings.isBankActionLimitedToBankPlots()) {
                    if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_bank_plot"));
                    }
                    final TownBlock tb = TownyAPI.getInstance().getTownBlock(player.getLocation());
                    final Nation tbNation = tb.getTown().getNation();
                    final Nation pNation = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                    if (tbNation != pNation || !tb.getTown().isCapital()) {
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
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_nation_capital"));
                    }
                    final Coord coord = Coord.parseCoord(NationCommand.plugin.getCache(player).getLastLocation());
                    final Town town2 = townyUniverse.getDataSource().getWorld(player.getLocation().getWorld().getName()).getTownBlock(coord).getTown();
                    if (!town2.isCapital()) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_nation_capital"));
                    }
                    final Nation nation3 = town2.getNation();
                    if (!townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation().equals(nation3)) {
                        throw new TownyException(TownySettings.getLangString("msg_err_unable_to_use_bank_outside_nation_capital"));
                    }
                }
                if (split.length == 1) {
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_must_specify_amnt"), nationCom + " deposit"));
                    return;
                }
                if (split.length == 2) {
                    try {
                        this.nationDeposit(player, Integer.parseInt(split[1].trim()));
                    }
                    catch (NumberFormatException e2) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                    }
                }
                if (split.length == 3) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_DEPOSIT_OTHER.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final Town town3 = TownyAPI.getInstance().getDataSource().getTown(split[2]);
                    final Nation nation4 = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                    if (town3 == null) {
                        throw new NotRegisteredException();
                    }
                    if (!town3.hasNation()) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_same_nation"), town3.getName()));
                    }
                    if (!town3.getNation().equals(nation4)) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_same_nation"), town3.getName()));
                    }
                    try {
                        TownCommand.townDeposit(player, town3, Integer.parseInt(split[1].trim()));
                    }
                    catch (NumberFormatException e3) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                    }
                }
            }
            else {
                final String[] newSplit2 = StringMgmt.remFirstArg(split);
                if (split[0].equalsIgnoreCase("rank")) {
                    this.nationRank(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("king")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_KING.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.nationKing(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("add")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_INVITE_ADD.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.nationAdd(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("invite") || split[0].equalsIgnoreCase("invites")) {
                    this.parseInviteCommand(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("kick")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_KICK.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.nationKick(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("set")) {
                    nationSet(player, newSplit2, false, null);
                }
                else if (split[0].equalsIgnoreCase("toggle")) {
                    nationToggle(player, newSplit2, false, null);
                }
                else if (split[0].equalsIgnoreCase("ally")) {
                    this.nationAlly(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("enemy")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ENEMY.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.nationEnemy(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("delete")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_DELETE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.nationDelete(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("online")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ONLINE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.parseNationOnlineCommand(player, newSplit2);
                }
                else if (split[0].equalsIgnoreCase("say")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SAY.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    try {
                        final Nation nation4 = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
                        final StringBuilder builder = new StringBuilder();
                        for (final String s : newSplit2) {
                            builder.append(s + " ");
                        }
                        final String message = builder.toString();
                        TownyMessaging.sendPrefixedNationMessage(nation4, message);
                    }
                    catch (Exception e) {}
                }
                else {
                    try {
                        final Nation nation4 = townyUniverse.getDataSource().getNation(split[0]);
                        final Resident resident3 = townyUniverse.getDataSource().getResident(player.getName());
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_OTHERNATION.getNode()) && ((resident3.hasTown() && resident3.getTown().hasNation() && resident3.getTown().getNation() != nation4) || !resident3.hasTown())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        final Nation nation5 = townyUniverse.getDataSource().getNation(split[0]);
                        Bukkit.getScheduler().runTaskAsynchronously((Plugin)NationCommand.plugin, () -> TownyMessaging.sendMessage(player, TownyFormatter.getStatus(nation5)));
                    }
                    catch (NotRegisteredException x3) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
                    }
                }
            }
        }
        catch (Exception x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    private void parseNationJoin(final Player player, final String[] args) {
        try {
            if (args.length < 1) {
                throw new Exception(String.format("Usage: /nation join [nation]", new Object[0]));
            }
            final String nationName = args[0];
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            final Town town = resident.getTown();
            final Nation nation = townyUniverse.getDataSource().getNation(nationName);
            if (town.hasNation()) {
                throw new Exception(TownySettings.getLangString("msg_err_already_in_a_nation"));
            }
            if (!nation.isOpen()) {
                throw new Exception(String.format(TownySettings.getLangString("msg_err_nation_not_open"), nation.getFormattedName()));
            }
            if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeTownNeutralityEnabled() && (town.isNeutral() || (!town.isNeutral() && town.getNeutralityChangeConfirmationCounterDays() > 0))) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_neutral_town_cannot_join_nation"));
            }
            if (TownySettings.getNumResidentsJoinNation() > 0 && town.getNumResidents() < TownySettings.getNumResidentsJoinNation()) {
                throw new Exception(String.format(TownySettings.getLangString("msg_err_not_enough_residents_join_nation"), town.getName()));
            }
            if (TownySettings.getMaxTownsPerNation() > 0 && nation.getTowns().size() >= TownySettings.getMaxTownsPerNation()) {
                throw new Exception(String.format(TownySettings.getLangString("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
            }
            if (TownySettings.getNationRequiresProximity() > 0.0) {
                final Coord capitalCoord = nation.getCapital().getHomeBlock().getCoord();
                final Coord townCoord = town.getHomeBlock().getCoord();
                if (!nation.getCapital().getHomeBlock().getWorld().getName().equals(town.getHomeBlock().getWorld().getName())) {
                    throw new Exception(TownySettings.getLangString("msg_err_nation_homeblock_in_another_world"));
                }
                final double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2.0) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2.0));
                if (distance > TownySettings.getNationRequiresProximity()) {
                    throw new Exception(String.format(TownySettings.getLangString("msg_err_town_not_close_enough_to_nation"), town.getName()));
                }
            }
            final NationPreAddTownEvent preEvent = new NationPreAddTownEvent(nation, town);
            Bukkit.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
                return;
            }
            final List<Town> towns = new ArrayList<Town>();
            towns.add(town);
            nationAdd(nation, towns);
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    private void parseInviteCommand(final Player player, final String[] newSplit) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
        final String sent = TownySettings.getLangString("nation_sent_invites").replace("%a", Integer.toString(InviteHandler.getSentInvitesAmount(resident.getTown().getNation()))).replace("%m", Integer.toString(InviteHandler.getSentInvitesMaxAmount(resident.getTown().getNation())));
        if (newSplit.length != 0) {
            if (newSplit.length >= 1) {
                if (newSplit[0].equalsIgnoreCase("help") || newSplit[0].equalsIgnoreCase("?")) {
                    for (final String msg : NationCommand.invite) {
                        player.sendMessage(Colors.strip(msg));
                    }
                    return;
                }
                if (newSplit[0].equalsIgnoreCase("sent")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_INVITE_LIST_SENT.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final List<Invite> sentinvites = resident.getTown().getNation().getSentInvites();
                    int page = 1;
                    if (newSplit.length >= 2) {
                        try {
                            page = Integer.parseInt(newSplit[1]);
                        }
                        catch (NumberFormatException ex) {}
                    }
                    InviteCommand.sendInviteList(player, sentinvites, page, true);
                    player.sendMessage(sent);
                }
                else {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_INVITE_ADD.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    this.nationAdd(player, newSplit);
                }
            }
            return;
        }
        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_INVITE_SEE_HOME.getNode())) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        final List<String> messages = new ArrayList<String>();
        for (final String msg2 : NationCommand.invite) {
            messages.add(Colors.strip(msg2));
        }
        messages.add(sent);
        final String[] msgs = messages.toArray(new String[0]);
        player.sendMessage(msgs);
    }
    
    private void parseNationOnlineCommand(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length > 0) {
            try {
                final Nation nation = townyUniverse.getDataSource().getNation(split[0]);
                final List<Resident> onlineResidents = ResidentUtil.getOnlineResidentsViewable(player, nation);
                if (onlineResidents.size() > 0) {
                    TownyMessaging.sendMsg(player, TownyFormatter.getFormattedOnlineResidents(TownySettings.getLangString("msg_nation_online"), nation, player));
                }
                else {
                    TownyMessaging.sendMsg(player, "§f0 " + TownySettings.getLangString("res_list") + " " + TownySettings.getLangString("msg_nation_online") + ": " + nation);
                }
                return;
            }
            catch (NotRegisteredException e) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
            }
        }
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            final Town town = resident.getTown();
            final Nation nation2 = town.getNation();
            TownyMessaging.sendMsg(player, TownyFormatter.getFormattedOnlineResidents(TownySettings.getLangString("msg_nation_online"), nation2, player));
        }
        catch (NotRegisteredException x) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_dont_belong_nation"));
        }
    }
    
    public void nationRank(final Player player, final String[] split) throws TownyException {
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/nation rank"));
            player.sendMessage(ChatTools.formatCommand("", "/nation rank", "add/remove [resident] rank", ""));
        }
        else {
            Town town = null;
            Town targetTown = null;
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            if (split.length < 3) {
                TownyMessaging.sendErrorMsg(player, "Eg: /town rank add/remove [resident] [rank]");
                return;
            }
            Resident target;
            try {
                final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                target = townyUniverse.getDataSource().getResident(split[1]);
                town = resident.getTown();
                targetTown = target.getTown();
                if (town.getNation() != targetTown.getNation()) {
                    throw new TownyException("This resident is not a member of your Town!");
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
            final String rank = split[2];
            if (!TownyPerms.getNationRanks().contains(rank)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_unknown_rank_available_ranks"), rank, StringMgmt.join(TownyPerms.getNationRanks(), ",")));
                return;
            }
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_RANK.getNode(rank.toLowerCase()))) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_no_permission_to_give_rank"));
                return;
            }
            Label_0560: {
                if (split[0].equalsIgnoreCase("add")) {
                    try {
                        if (target.addNationRank(rank)) {
                            if (BukkitTools.isOnline(target.getName())) {
                                TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_been_given_rank"), "Nation", rank));
                                NationCommand.plugin.deleteCache(TownyAPI.getInstance().getPlayer(target));
                            }
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_have_given_rank"), "Nation", rank, target.getName()));
                            break Label_0560;
                        }
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_resident_not_part_of_any_town"));
                        return;
                    }
                    catch (AlreadyRegisteredException e) {
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_resident_already_has_rank"), target.getName(), "Nation"));
                        return;
                    }
                }
                if (split[0].equalsIgnoreCase("remove")) {
                    try {
                        if (target.removeNationRank(rank)) {
                            if (BukkitTools.isOnline(target.getName())) {
                                TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_had_rank_taken"), "Nation", rank));
                                NationCommand.plugin.deleteCache(TownyAPI.getInstance().getPlayer(target));
                            }
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_have_taken_rank_from"), "Nation", rank, target.getName()));
                        }
                        break Label_0560;
                    }
                    catch (NotRegisteredException e2) {
                        TownyMessaging.sendMsg(player, String.format("msg_resident_doesnt_have_rank", target.getName(), "Nation"));
                        return;
                    }
                }
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), split[0]));
                return;
            }
            townyUniverse.getDataSource().saveResident(target);
        }
    }
    
    private void nationWithdraw(final Player player, final int amount) {
        try {
            if (!TownySettings.geNationBankAllowWithdrawls()) {
                throw new TownyException(TownySettings.getLangString("msg_err_withdraw_disabled"));
            }
            if (amount < 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));
            }
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            final Nation nation = resident.getTown().getNation();
            boolean underAttack = false;
            for (final Town town : nation.getTowns()) {
                if (TownyWar.isUnderAttack(town) || System.currentTimeMillis() - TownyWar.lastFlagged(town) < TownySettings.timeToWaitAfterFlag()) {
                    underAttack = true;
                    break;
                }
            }
            if (underAttack && TownySettings.isFlaggedInteractionNation()) {
                throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_nation_under_attack"));
            }
            final Transaction transaction = new Transaction(TransactionType.WITHDRAW, player, amount);
            final NationPreTransactionEvent preEvent = new NationPreTransactionEvent(nation, transaction);
            BukkitTools.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
                return;
            }
            nation.withdrawFromBank(resident, amount);
            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_xx_withdrew_xx"), resident.getName(), amount, "nation"));
            BukkitTools.getPluginManager().callEvent((Event)new NationTransactionEvent(nation, transaction));
        }
        catch (TownyException | EconomyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    private void nationDeposit(final Player player, final int amount) {
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            final Nation nation = resident.getTown().getNation();
            final double bankcap = TownySettings.getNationBankCap();
            if (bankcap > 0.0 && amount + nation.getAccount().getHoldingBalance() > bankcap) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
            }
            if (amount < 0) {
                throw new TownyException(TownySettings.getLangString("msg_err_negative_money"));
            }
            final Transaction transaction = new Transaction(TransactionType.DEPOSIT, player, amount);
            final NationPreTransactionEvent preEvent = new NationPreTransactionEvent(nation, transaction);
            BukkitTools.getPluginManager().callEvent((Event)preEvent);
            if (preEvent.isCancelled()) {
                TownyMessaging.sendErrorMsg(preEvent.getCancelMessage());
                return;
            }
            if (!resident.getAccount().payTo(amount, nation, "Nation Deposit")) {
                throw new TownyException(TownySettings.getLangString("msg_insuf_funds"));
            }
            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_xx_deposited_xx"), resident.getName(), amount, "nation"));
            BukkitTools.getPluginManager().callEvent((Event)new NationTransactionEvent(nation, transaction));
        }
        catch (TownyException | EconomyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public void listNations(final CommandSender sender, final String[] split) {
        if (split.length == 2 && split[1].equals("?")) {
            sender.sendMessage(ChatTools.formatTitle("/nation list"));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #}", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by residents", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by towns", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by open", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by balance", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by name", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by townblocks", ""));
            sender.sendMessage(ChatTools.formatCommand("", "/nation list", "{page #} by online", ""));
            return;
        }
        final List<Nation> nationsToSort = TownyUniverse.getInstance().getDataSource().getNations();
        int page = 1;
        boolean pageSet = false;
        boolean comparatorSet = false;
        Comparator<Nation> comparator = NationCommand.BY_NUM_RESIDENTS;
        final int total = (int)Math.ceil(nationsToSort.size() / 10.0);
        for (int i = 1; i < split.length; ++i) {
            if (split[i].equalsIgnoreCase("by")) {
                if (comparatorSet) {
                    TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_multiple_comparators_nation"));
                    return;
                }
                if (++i >= split.length) {
                    TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_missing_comparator"));
                    return;
                }
                comparatorSet = true;
                if (split[i].equalsIgnoreCase("residents")) {
                    comparator = NationCommand.BY_NUM_RESIDENTS;
                }
                else if (split[i].equalsIgnoreCase("balance")) {
                    comparator = NationCommand.BY_BANK_BALANCE;
                }
                else if (split[i].equalsIgnoreCase("towns")) {
                    comparator = NationCommand.BY_NUM_TOWNS;
                }
                else if (split[i].equalsIgnoreCase("name")) {
                    comparator = NationCommand.BY_NAME;
                }
                else if (split[i].equalsIgnoreCase("townblocks")) {
                    comparator = NationCommand.BY_TOWNBLOCKS_CLAIMED;
                }
                else {
                    if (!split[i].equalsIgnoreCase("online")) {
                        TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_invalid_comparator_nation"));
                        return;
                    }
                    comparator = NationCommand.BY_NUM_ONLINE;
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
        if (page > total) {
            TownyMessaging.sendErrorMsg(sender, TownySettings.getListNotEnoughPagesMsg(total));
            return;
        }
        try {
            Collections.sort(nationsToSort, comparator);
        }
        catch (RuntimeException e2) {
            TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_error_comparator_failed"));
            return;
        }
        int iMax = page * 10;
        if (page * 10 > nationsToSort.size()) {
            iMax = nationsToSort.size();
        }
        final List<String> nationsordered = new ArrayList<String>();
        for (int j = (page - 1) * 10; j < iMax; ++j) {
            final Nation nation = nationsToSort.get(j);
            final String output = "§6" + StringMgmt.remUnderscore(nation.getName()) + "§8" + " - " + "§b" + "(" + nation.getNumResidents() + ")" + "§8" + " - " + "§b" + "(" + nation.getNumTowns() + ")";
            nationsordered.add(output);
        }
        sender.sendMessage(ChatTools.formatList(TownySettings.getLangString("nation_plu"), "§6" + TownySettings.getLangString("nation_name") + "§8" + " - " + "§b" + TownySettings.getLangString("number_of_residents") + "§8" + " - " + "§b" + TownySettings.getLangString("number_of_towns"), nationsordered, TownySettings.getListPageMsg(page, total)));
    }
    
    public static void newNation(final Player player, final String name, final String capitalName, final boolean noCharge) {
        final TownyUniverse universe = TownyUniverse.getInstance();
        try {
            final Town town = universe.getDataSource().getTown(capitalName);
            if (town.hasNation()) {
                throw new TownyException(TownySettings.getLangString("msg_err_already_nation"));
            }
            if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeTownNeutralityEnabled() && (town.isNeutral() || (!town.isNeutral() && town.getNeutralityChangeConfirmationCounterDays() > 0))) {
                throw new TownyException(TownySettings.getLangString("msg_err_siege_war_neutral_town_cannot_create_nation"));
            }
            String filteredName;
            try {
                filteredName = NameValidation.checkAndFilterName(name);
            }
            catch (InvalidNameException e) {
                filteredName = null;
            }
            if (filteredName == null || universe.getDataSource().hasNation(filteredName)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
            }
            if (!noCharge && TownySettings.isUsingEconomy() && !town.getAccount().pay(TownySettings.getNewNationPrice(), "New Nation Cost")) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_no_funds_new_nation2"), TownySettings.getNewNationPrice()));
            }
            newNation(name, town);
            TownyMessaging.sendGlobalMessage(TownySettings.getNewNationMsg(player.getName(), StringMgmt.remUnderscore(name)));
        }
        catch (TownyException | EconomyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public static Nation newNation(final String name, final Town town) throws AlreadyRegisteredException, NotRegisteredException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.getDataSource().newNation(name);
        final Nation nation = townyUniverse.getDataSource().getNation(name);
        nation.addTown(town);
        nation.setCapital(town);
        nation.setUuid(UUID.randomUUID());
        nation.setRegistered(System.currentTimeMillis());
        if (TownySettings.isUsingEconomy()) {
            try {
                nation.getAccount().setBalance(0.0, "Deleting Nation");
            }
            catch (EconomyException e) {
                e.printStackTrace();
            }
        }
        townyUniverse.getDataSource().saveTown(town);
        townyUniverse.getDataSource().saveNation(nation);
        townyUniverse.getDataSource().saveNationList();
        BukkitTools.getPluginManager().callEvent((Event)new NewNationEvent(nation));
        return nation;
    }
    
    public void mergeNation(final Player player, final String name) throws TownyException {
        final TownyUniverse universe = TownyUniverse.getInstance();
        Nation nation = null;
        Nation remainingNation = null;
        try {
            nation = universe.getDataSource().getNation(name);
            remainingNation = universe.getDataSource().getResident(player.getName()).getTown().getNation();
        }
        catch (NotRegisteredException e) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
        }
        if (remainingNation.getName().equalsIgnoreCase(name)) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
        }
        if (nation != null) {
            final Resident king = nation.getKing();
            if (!BukkitTools.isOnline(king.getName())) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_king_of_that_nation_is_not_online"), name, king.getName()));
            }
            TownyMessaging.sendMessage(BukkitTools.getPlayer(king.getName()), String.format(TownySettings.getLangString("msg_would_you_merge_your_nation_into_other_nation"), nation, remainingNation, remainingNation));
            ConfirmationHandler.addConfirmation(king, ConfirmationType.NATION_MERGE, remainingNation);
            TownyMessaging.sendConfirmationMessage((CommandSender)BukkitTools.getPlayer(king.getName()), null, null, null, null);
        }
    }
    
    public void nationLeave(final Player player) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Town town = null;
        Nation nation = null;
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            town = resident.getTown();
            nation = town.getNation();
            if (town.isConquered()) {
                throw new TownyException(TownySettings.getLangString("msg_err_your_conquered_town_cannot_leave_the_nation_yet"));
            }
            if (TownyWar.isUnderAttack(town) && TownySettings.isFlaggedInteractionTown()) {
                throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_town_under_attack"));
            }
            if (System.currentTimeMillis() - TownyWar.lastFlagged(town) < TownySettings.timeToWaitAfterFlag()) {
                throw new TownyException(TownySettings.getLangString("msg_war_flag_deny_recently_attacked"));
            }
            if (TownySettings.getWarSiegeEnabled()) {
                if (TownySettings.getWarSiegeTownLeaveDisabled() && !TownySettings.getWarSiegeRevoltEnabled()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_siege_war_town_voluntary_leave_impossible"));
                }
                if (town.isRevoltImmunityActive()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_siege_war_revolt_immunity_active"));
                }
                SiegeWarTimeUtil.activateRevoltImmunityTimer(town);
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_revolt"), TownyFormatter.getFormattedTownName(town), TownyFormatter.getFormattedResidentName(town.getMayor()), TownyFormatter.getFormattedNationName(nation)));
            }
            nation.removeTown(town);
            townyUniverse.getDataSource().saveNation(nation);
            townyUniverse.getDataSource().saveNationList();
            NationCommand.plugin.resetCache();
            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_nation_town_left"), StringMgmt.remUnderscore(town.getName())));
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_left_nation"), StringMgmt.remUnderscore(nation.getName())));
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
        catch (EmptyNationException en) {
            townyUniverse.getDataSource().removeNation(en.getNation());
            townyUniverse.getDataSource().saveNationList();
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_del_nation"), en.getNation().getName()));
        }
        finally {
            townyUniverse.getDataSource().saveTown(town);
        }
    }
    
    public void nationDelete(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            try {
                final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                final double amountToRefund = (double)Math.round(TownySettings.getNewNationPrice() * 0.01 * TownySettings.getWarSiegeNationCostRefundPercentageOnDelete());
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_siege_war_delete_nation_warning"), TownyEconomyHandler.getFormattedBalance(amountToRefund)));
                ConfirmationHandler.addConfirmation(resident, ConfirmationType.NATION_DELETE, null);
                TownyMessaging.sendConfirmationMessage((CommandSender)player, null, null, null, null);
                return;
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
        }
        try {
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_DELETE.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_admin_only_delete_nation"));
            }
            final Nation nation = townyUniverse.getDataSource().getNation(split[0]);
            townyUniverse.getDataSource().removeNation(nation);
            TownyMessaging.sendGlobalMessage(TownySettings.getDelNationMsg(nation));
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    public void nationKing(final Player player, final String[] split) {
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            for (final String line : NationCommand.king_help) {
                player.sendMessage(line);
            }
        }
    }
    
    public void nationAdd(final Player player, String[] names) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (names.length < 1) {
            TownyMessaging.sendErrorMsg(player, "Eg: /nation add [names]");
            return;
        }
        Nation nation;
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            nation = resident.getTown().getNation();
            if (TownySettings.getMaxTownsPerNation() > 0 && nation.getTowns().size() >= TownySettings.getMaxTownsPerNation()) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_nation_over_town_limit"), TownySettings.getMaxTownsPerNation()));
                return;
            }
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        final List<String> reslist = new ArrayList<String>(Arrays.asList(names));
        final List<String> newreslist = new ArrayList<String>();
        final List<String> removeinvites = new ArrayList<String>();
        for (final String townname : reslist) {
            if (townname.startsWith("-")) {
                removeinvites.add(townname.substring(1));
            }
            else {
                newreslist.add(townname);
            }
        }
        names = newreslist.toArray(new String[0]);
        final String[] namestoremove = removeinvites.toArray(new String[0]);
        if (namestoremove.length >= 1) {
            nationRevokeInviteTown(player, nation, townyUniverse.getDataSource().getTowns(namestoremove));
        }
        if (names.length >= 1) {
            nationAdd(player, nation, townyUniverse.getDataSource().getTowns(names));
        }
    }
    
    private static void nationRevokeInviteTown(final Object sender, final Nation nation, final List<Town> towns) {
        for (final Town town : towns) {
            if (InviteHandler.inviteIsActive(nation, town)) {
                for (final Invite invite : town.getReceivedInvites()) {
                    if (invite.getSender().equals(nation)) {
                        try {
                            InviteHandler.declineInvite(invite, true);
                            TownyMessaging.sendMessage(sender, TownySettings.getLangString("nation_revoke_invite_successful"));
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
    
    public static void nationAdd(final Player player, final Nation nation, final List<Town> invited) throws TownyException {
        final ArrayList<Town> remove = new ArrayList<Town>();
        for (final Town town : invited) {
            try {
                if (TownySettings.getNumResidentsJoinNation() > 0 && town.getNumResidents() < TownySettings.getNumResidentsJoinNation()) {
                    remove.add(town);
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_enough_residents_join_nation"), town.getName()));
                }
                else {
                    if (TownySettings.getNationRequiresProximity() > 0.0) {
                        final Coord capitalCoord = nation.getCapital().getHomeBlock().getCoord();
                        final Coord townCoord = town.getHomeBlock().getCoord();
                        if (!nation.getCapital().getHomeBlock().getWorld().getName().equals(town.getHomeBlock().getWorld().getName())) {
                            remove.add(town);
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_nation_homeblock_in_another_world"));
                            continue;
                        }
                        final double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2.0) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2.0));
                        if (distance > TownySettings.getNationRequiresProximity()) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_town_not_close_enough_to_nation"), town.getName()));
                            remove.add(town);
                            continue;
                        }
                    }
                    final NationPreAddTownEvent preEvent = new NationPreAddTownEvent(nation, town);
                    Bukkit.getPluginManager().callEvent((Event)preEvent);
                    if (preEvent.isCancelled()) {
                        TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
                        return;
                    }
                    nationInviteTown(player, nation, town);
                }
            }
            catch (AlreadyRegisteredException e) {
                remove.add(town);
            }
        }
        for (final Town town : remove) {
            invited.remove(town);
        }
        if (invited.size() > 0) {
            StringBuilder msg = new StringBuilder();
            for (final Town town2 : invited) {
                msg.append(town2.getName()).append(", ");
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            msg = new StringBuilder(String.format(TownySettings.getLangString("msg_invited_join_nation"), player.getName(), msg.toString()));
            TownyMessaging.sendPrefixedNationMessage(nation, msg.toString());
            return;
        }
        throw new TownyException(TownySettings.getLangString("msg_invalid_name"));
    }
    
    private static void nationInviteTown(final Player player, final Nation nation, final Town town) throws TownyException {
        final TownJoinNationInvite invite = new TownJoinNationInvite(player.getName(), nation, town);
        try {
            if (InviteHandler.inviteIsActive(invite)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_town_already_invited"), town.getName()));
            }
            town.newReceivedInvite(invite);
            nation.newSentInvite(invite);
            InviteHandler.addInvite(invite);
            final Player mayor = TownyAPI.getInstance().getPlayer(town.getMayor());
            TownyMessaging.sendRequestMessage((CommandSender)mayor, invite);
            Bukkit.getPluginManager().callEvent((Event)new NationInviteTownEvent(invite));
        }
        catch (TooManyInvitesException e) {
            town.deleteReceivedInvite(invite);
            nation.deleteSentInvite(invite);
            throw new TownyException(e.getMessage());
        }
    }
    
    public static void nationAdd(final Nation nation, final List<Town> towns) throws AlreadyRegisteredException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        for (final Town town : towns) {
            if (!town.hasNation()) {
                nation.addTown(town);
                townyUniverse.getDataSource().saveTown(town);
                TownyMessaging.sendNationMessagePrefixed(nation, String.format(TownySettings.getLangString("msg_join_nation"), town.getName()));
            }
        }
        NationCommand.plugin.resetCache();
        townyUniverse.getDataSource().saveNation(nation);
    }
    
    public void nationKick(final Player player, final String[] names) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (names.length < 1) {
            TownyMessaging.sendErrorMsg(player, "Eg: /nation kick [names]");
            return;
        }
        Resident resident;
        Nation nation;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
            nation = resident.getTown().getNation();
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        this.nationKick(player, resident, nation, townyUniverse.getDataSource().getTowns(names));
    }
    
    public void nationKick(final Player player, final Resident resident, final Nation nation, final List<Town> kicking) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final ArrayList<Town> remove = new ArrayList<Town>();
        for (final Town town : kicking) {
            if (town.isCapital()) {
                remove.add(town);
            }
            else {
                try {
                    nation.removeTown(town);
                    final List<Resident> titleRemove = new ArrayList<Resident>(town.getResidents());
                    for (final Resident res : titleRemove) {
                        if (res.hasTitle() || res.hasSurname()) {
                            res.setTitle("");
                            res.setSurname("");
                        }
                        res.updatePermsForNationRemoval();
                        townyUniverse.getDataSource().saveResident(res);
                    }
                    townyUniverse.getDataSource().saveTown(town);
                }
                catch (NotRegisteredException e) {
                    remove.add(town);
                }
                catch (EmptyNationException ex) {}
            }
        }
        for (final Town town : remove) {
            kicking.remove(town);
        }
        if (kicking.size() > 0) {
            StringBuilder msg = new StringBuilder();
            for (final Town town2 : kicking) {
                msg.append(town2.getName()).append(", ");
                TownyMessaging.sendPrefixedTownMessage(town2, String.format(TownySettings.getLangString("msg_nation_kicked_by"), player.getName()));
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            msg = new StringBuilder(String.format(TownySettings.getLangString("msg_nation_kicked"), player.getName(), msg.toString()));
            TownyMessaging.sendPrefixedNationMessage(nation, msg.toString());
            townyUniverse.getDataSource().saveNation(nation);
            NationCommand.plugin.resetCache();
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    @SuppressWarnings("unused")
	private void nationAlly(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length <= 0) {
            TownyMessaging.sendMessage(player, NationCommand.alliesstring);
            return;
        }
        Resident resident;
        Nation nation;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
            nation = resident.getTown().getNation();
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        final ArrayList<Nation> list = new ArrayList<Nation>();
        final ArrayList<Nation> remlist = new ArrayList<Nation>();
        final String[] names = StringMgmt.remFirstArg(split);
        if (split[0].equalsIgnoreCase("add")) {
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_ADD.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            for (final String name : names) {
                Label_0305: {
                    try {
                        final Nation ally = townyUniverse.getDataSource().getNation(name);
                        if (nation.equals(ally)) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_own_nation_disallow"));
                            return;
                        }
                        list.add(ally);
                    }
                    catch (NotRegisteredException e2) {
                        if (name.startsWith("-") && TownySettings.isDisallowOneWayAlliance()) {
                            try {
                                final Nation ally = townyUniverse.getDataSource().getNation(name.substring(1));
                                if (nation.equals(ally)) {
                                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_own_nation_disallow"));
                                    return;
                                }
                                remlist.add(ally);
                                break Label_0305;
                            }
                            catch (NotRegisteredException x2) {
                                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
                                return;
                            }
                        }
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
                        return;
                    }
                }
            }
            if (!list.isEmpty()) {
                if (TownySettings.isDisallowOneWayAlliance()) {
                    this.nationAlly(resident, nation, list, true);
                }
                else {
                    this.nationlegacyAlly(resident, nation, list, true);
                }
            }
            if (!remlist.isEmpty()) {
                this.nationRemoveAllyRequest(player, nation, remlist);
            }
        }
        else if (split[0].equalsIgnoreCase("remove")) {
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_REMOVE.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            for (final String name : names) {
                try {
                    final Nation ally = townyUniverse.getDataSource().getNation(name);
                    if (nation.equals(ally)) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_own_nation_disallow"));
                        return;
                    }
                    list.add(ally);
                }
                catch (NotRegisteredException ex) {}
            }
            if (!list.isEmpty()) {
                if (TownySettings.isDisallowOneWayAlliance()) {
                    this.nationAlly(resident, nation, list, false);
                }
                else {
                    this.nationlegacyAlly(resident, nation, list, false);
                }
            }
        }
        else {
            if (!TownySettings.isDisallowOneWayAlliance()) {
                TownyMessaging.sendMessage(player, NationCommand.alliesstring);
                return;
            }
            if (TownySettings.isDisallowOneWayAlliance()) {
                final String received = TownySettings.getLangString("nation_received_requests").replace("%a", Integer.toString(InviteHandler.getReceivedInvitesAmount(resident.getTown().getNation()))).replace("%m", Integer.toString(InviteHandler.getReceivedInvitesMaxAmount(resident.getTown().getNation())));
                final String sent = TownySettings.getLangString("nation_sent_ally_requests").replace("%a", Integer.toString(InviteHandler.getSentAllyRequestsAmount(resident.getTown().getNation()))).replace("%m", Integer.toString(InviteHandler.getSentAllyRequestsMaxAmount(resident.getTown().getNation())));
                if (split[0].equalsIgnoreCase("sent")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_LIST_SENT.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final List<Invite> sentinvites = resident.getTown().getNation().getSentAllyInvites();
                    int page = 1;
                    if (split.length >= 2) {
                        try {
                            page = Integer.parseInt(split[2]);
                        }
                        catch (NumberFormatException ex2) {}
                    }
                    InviteCommand.sendInviteList(player, sentinvites, page, true);
                    player.sendMessage(sent);
                }
                else if (split[0].equalsIgnoreCase("received")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_LIST_RECEIVED.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final List<Invite> receivedinvites = resident.getTown().getNation().getReceivedInvites();
                    int page = 1;
                    if (split.length >= 2) {
                        try {
                            page = Integer.parseInt(split[2]);
                        }
                        catch (NumberFormatException ex3) {}
                    }
                    InviteCommand.sendInviteList(player, receivedinvites, page, true);
                    player.sendMessage(received);
                }
                else {
                    Nation sendernation = null;
                    Label_1044: {
                        if (split[0].equalsIgnoreCase("accept")) {
                            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_ACCEPT.getNode())) {
                                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                            }
                            final List<Invite> invites = nation.getReceivedInvites();
                            if (invites.size() == 0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_nation_no_requests"));
                                return;
                            }
                            Label_0932: {
                                if (split.length >= 2) {
                                    Label_0951: {
                                        try {
                                            sendernation = townyUniverse.getDataSource().getNation(split[1]);
                                            break Label_0951;
                                        }
                                        catch (NotRegisteredException e2) {
                                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                                            return;
                                        }
                                    }
                                    Invite toAccept = null;
                                    for (final Invite invite : InviteHandler.getActiveInvites()) {
                                        if (invite.getSender().equals(sendernation) && invite.getReceiver().equals(nation)) {
                                            toAccept = invite;
                                            break;
                                        }
                                    }
                                    if (toAccept != null) {
                                        try {
                                            InviteHandler.acceptInvite(toAccept);
                                            return;
                                        }
                                        catch (InvalidObjectException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break Label_1044;
                                }
                            }
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_nation_specify_invite"));
                            InviteCommand.sendInviteList(player, invites, 1, false);
                            return;
                        }
                    }
                    if (!split[0].equalsIgnoreCase("deny")) {
                        TownyMessaging.sendMessage(player, NationCommand.alliesstring);
                        return;
                    }
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_DENY.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final List<Invite> invites = nation.getReceivedInvites();
                    if (invites.size() == 0) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_nation_no_requests"));
                        return;
                    }
                    Label_1148: {
                        if (split.length >= 2) {
                            Label_1167: {
                                try {
                                    sendernation = townyUniverse.getDataSource().getNation(split[1]);
                                    break Label_1167;
                                }
                                catch (NotRegisteredException e2) {
                                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                                    return;
                                }
                            }
                            Invite toDecline = null;
                            for (final Invite invite : InviteHandler.getActiveInvites()) {
                                if (invite.getSender().equals(sendernation) && invite.getReceiver().equals(nation)) {
                                    toDecline = invite;
                                    break;
                                }
                            }
                            if (toDecline != null) {
                                try {
                                    InviteHandler.declineInvite(toDecline, false);
                                    TownyMessaging.sendMessage(player, TownySettings.getLangString("successful_deny_request"));
                                }
                                catch (InvalidObjectException e) {
                                    e.printStackTrace();
                                }
                            }
                            return;
                        }
                    }
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_nation_specify_invite"));
                    InviteCommand.sendInviteList(player, invites, 1, false);
                }
            }
        }
    }
    
    private void nationRemoveAllyRequest(final Object sender, final Nation nation, final ArrayList<Nation> remlist) {
        for (final Nation invited : remlist) {
            if (InviteHandler.inviteIsActive(nation, invited)) {
                for (final Invite invite : invited.getReceivedInvites()) {
                    if (invite.getSender().equals(nation)) {
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
    
    private void nationCreateAllyRequest(final String sender, final Nation nation, final Nation receiver) throws TownyException {
        final NationAllyNationInvite invite = new NationAllyNationInvite(sender, nation, receiver);
        try {
            if (InviteHandler.inviteIsActive(invite)) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_player_already_invited"), receiver.getName()));
            }
            receiver.newReceivedInvite(invite);
            nation.newSentAllyInvite(invite);
            InviteHandler.addInvite(invite);
            final Player mayor = TownyAPI.getInstance().getPlayer(receiver.getCapital().getMayor());
            TownyMessaging.sendRequestMessage((CommandSender)mayor, invite);
            Bukkit.getPluginManager().callEvent((Event)new NationRequestAllyNationEvent(invite));
        }
        catch (TooManyInvitesException e) {
            receiver.deleteReceivedInvite(invite);
            nation.deleteSentAllyInvite(invite);
            throw new TownyException(e.getMessage());
        }
    }
    
    public void nationlegacyAlly(final Resident resident, final Nation nation, final List<Nation> allies, final boolean add) {
        final Player player = BukkitTools.getPlayer(resident.getName());
        final ArrayList<Nation> remove = new ArrayList<Nation>();
        for (final Nation targetNation : allies) {
            try {
                if (add && !nation.getAllies().contains(targetNation)) {
                    if (!targetNation.hasEnemy(nation)) {
                        try {
                            nation.addAlly(targetNation);
                        }
                        catch (AlreadyRegisteredException e) {
                            e.printStackTrace();
                        }
                        TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_allied_nations"), resident.getName(), targetNation.getName()));
                        TownyMessaging.sendPrefixedNationMessage(targetNation, String.format(TownySettings.getLangString("msg_added_ally"), nation.getName()));
                    }
                    else {
                        remove.add(targetNation);
                        TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_unable_ally_enemy"), targetNation.getName()));
                    }
                }
                else {
                    if (!nation.getAllies().contains(targetNation)) {
                        continue;
                    }
                    nation.removeAlly(targetNation);
                    TownyMessaging.sendPrefixedNationMessage(targetNation, String.format(TownySettings.getLangString("msg_removed_ally"), nation.getName()));
                    TownyMessaging.sendMessage(player, TownySettings.getLangString("msg_ally_removed_successfully"));
                    if (!targetNation.hasAlly(nation)) {
                        continue;
                    }
                    this.nationlegacyAlly(resident, targetNation, Arrays.asList(nation), false);
                }
            }
            catch (NotRegisteredException e2) {
                remove.add(targetNation);
            }
        }
        for (final Nation newAlly : remove) {
            allies.remove(newAlly);
        }
        if (allies.size() > 0) {
            TownyUniverse.getInstance().getDataSource().saveNations();
            NationCommand.plugin.resetCache();
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    public void nationAlly(final Resident resident, final Nation nation, final List<Nation> allies, final boolean add) throws TownyException {
        final Player player = BukkitTools.getPlayer(resident.getName());
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final ArrayList<Nation> remove = new ArrayList<Nation>();
        for (final Nation targetNation : allies) {
            if (add) {
                if (targetNation.hasEnemy(nation)) {
                    continue;
                }
                if (!targetNation.getCapital().getMayor().isNPC()) {
                    for (final Nation newAlly : allies) {
                        this.nationCreateAllyRequest(player.getName(), nation, targetNation);
                        TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_ally_req_sent"), newAlly.getName()));
                    }
                }
                else if (townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN.getNode())) {
                    try {
                        targetNation.addAlly(nation);
                        nation.addAlly(targetNation);
                    }
                    catch (AlreadyRegisteredException e) {
                        e.printStackTrace();
                    }
                    TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_allied_nations"), resident.getName(), targetNation.getName()));
                    TownyMessaging.sendPrefixedNationMessage(targetNation, String.format(TownySettings.getLangString("msg_added_ally"), nation.getName()));
                }
                else {
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_unable_ally_npc"), nation.getName()));
                }
            }
            else {
                if (!nation.getAllies().contains(targetNation)) {
                    continue;
                }
                try {
                    nation.removeAlly(targetNation);
                    TownyMessaging.sendPrefixedNationMessage(targetNation, String.format(TownySettings.getLangString("msg_removed_ally"), nation.getName()));
                    TownyMessaging.sendMessage(player, TownySettings.getLangString("msg_ally_removed_successfully"));
                }
                catch (NotRegisteredException e2) {
                    remove.add(targetNation);
                }
                if (!targetNation.hasAlly(nation)) {
                    continue;
                }
                try {
                    targetNation.removeAlly(nation);
                }
                catch (NotRegisteredException ex) {}
            }
        }
        for (final Nation newAlly2 : remove) {
            allies.remove(newAlly2);
        }
        if (allies.size() > 0) {
            townyUniverse.getDataSource().saveNations();
            NationCommand.plugin.resetCache();
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    public void nationEnemy(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length < 2) {
            TownyMessaging.sendErrorMsg(player, "Eg: /nation enemy [add/remove] [name]");
            return;
        }
        Resident resident;
        Nation nation;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
            nation = resident.getTown().getNation();
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        final ArrayList<Nation> list = new ArrayList<Nation>();
        final String test = split[0];
        final String[] newSplit = StringMgmt.remFirstArg(split);
        if ((test.equalsIgnoreCase("remove") || test.equalsIgnoreCase("add")) && newSplit.length > 0) {
            for (final String name : newSplit) {
                try {
                    final Nation enemy = townyUniverse.getDataSource().getNation(name);
                    if (nation.equals(enemy)) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_own_nation_disallow"));
                    }
                    else {
                        list.add(enemy);
                    }
                }
                catch (NotRegisteredException e) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_no_nation_with_that_name"), name));
                }
            }
            if (!list.isEmpty()) {
                this.nationEnemy(resident, nation, list, test.equalsIgnoreCase("add"));
            }
        }
        else {
            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "[add/remove]"));
        }
    }
    
    public void nationEnemy(final Resident resident, final Nation nation, final List<Nation> enemies, final boolean add) {
        final ArrayList<Nation> remove = new ArrayList<Nation>();
        for (final Nation targetNation : enemies) {
            try {
                if (add && !nation.getEnemies().contains(targetNation)) {
                    final NationPreAddEnemyEvent npaee = new NationPreAddEnemyEvent(nation, targetNation);
                    Bukkit.getPluginManager().callEvent((Event)npaee);
                    if (!npaee.isCancelled()) {
                        nation.addEnemy(targetNation);
                        final NationAddEnemyEvent naee = new NationAddEnemyEvent(nation, targetNation);
                        Bukkit.getPluginManager().callEvent((Event)naee);
                        TownyMessaging.sendPrefixedNationMessage(targetNation, String.format(TownySettings.getLangString("msg_added_enemy"), nation.getName()));
                        if (!targetNation.hasAlly(nation)) {
                            continue;
                        }
                        this.nationlegacyAlly(resident, targetNation, Arrays.asList(nation), false);
                    }
                    else {
                        TownyMessaging.sendMsg(TownyAPI.getInstance().getPlayer(resident), npaee.getCancelMessage());
                        remove.add(targetNation);
                    }
                }
                else {
                    if (!nation.getEnemies().contains(targetNation)) {
                        continue;
                    }
                    final NationPreRemoveEnemyEvent npree = new NationPreRemoveEnemyEvent(nation, targetNation);
                    Bukkit.getPluginManager().callEvent((Event)npree);
                    if (!npree.isCancelled()) {
                        nation.removeEnemy(targetNation);
                        final NationRemoveEnemyEvent nree = new NationRemoveEnemyEvent(nation, targetNation);
                        Bukkit.getPluginManager().callEvent((Event)nree);
                        TownyMessaging.sendPrefixedNationMessage(targetNation, String.format(TownySettings.getLangString("msg_removed_enemy"), nation.getName()));
                    }
                    else {
                        TownyMessaging.sendMsg(TownyAPI.getInstance().getPlayer(resident), npree.getCancelMessage());
                        remove.add(targetNation);
                    }
                }
            }
            catch (AlreadyRegisteredException | NotRegisteredException e) {
                remove.add(targetNation);
            }
        }
        for (final Nation newEnemy : remove) {
            enemies.remove(newEnemy);
        }
        if (enemies.size() > 0) {
            String msg = "";
            for (final Nation newEnemy2 : enemies) {
                msg = msg + newEnemy2.getName() + ", ";
            }
            msg = msg.substring(0, msg.length() - 2);
            if (add) {
                msg = String.format(TownySettings.getLangString("msg_enemy_nations"), resident.getName(), msg);
            }
            else {
                msg = String.format(TownySettings.getLangString("msg_enemy_to_neutral"), resident.getName(), msg);
            }
            TownyMessaging.sendPrefixedNationMessage(nation, msg);
            TownyUniverse.getInstance().getDataSource().saveNations();
            NationCommand.plugin.resetCache();
        }
        else {
            TownyMessaging.sendErrorMsg(resident, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    public static void nationSet(final Player player, String[] split, final boolean admin, Nation nation) throws TownyException, InvalidNameException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/nation set"));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "king " + TownySettings.getLangString("res_2"), ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "capital [town]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "taxes [$]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "name [name]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "title/surname [resident] [text]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "tag [upto 4 letters] or clear", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "board [message ... ]", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "spawn", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation set", "spawncost [$]", ""));
        }
        else {
            Resident resident;
            try {
                if (!admin) {
                    resident = townyUniverse.getDataSource().getResident(player.getName());
                    nation = resident.getTown().getNation();
                }
                else {
                    resident = nation.getKing();
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
            Label_2150: {
                if (split[0].equalsIgnoreCase("king")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_KING.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split.length < 2) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /nation set king Dumbo");
                    }
                    else {
                        try {
                            final Resident newKing = townyUniverse.getDataSource().getResident(split[1]);
                            final String oldKingsName = nation.getCapital().getMayor().getName();
                            if (TownySettings.getNumResidentsCreateNation() > 0 && newKing.getTown().getNumResidents() < TownySettings.getNumResidentsCreateNation()) {
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_not_enough_residents_capital"), newKing.getTown().getName()));
                                return;
                            }
                            nation.setKing(newKing);
                            NationCommand.plugin.deleteCache(oldKingsName);
                            NationCommand.plugin.deleteCache(newKing.getName());
                            TownyMessaging.sendPrefixedNationMessage(nation, TownySettings.getNewKingMsg(newKing.getName(), nation.getName()));
                        }
                        catch (TownyException e) {
                            TownyMessaging.sendErrorMsg(player, e.getMessage());
                        }
                    }
                }
                else if (split[0].equalsIgnoreCase("capital")) {
                    try {
                        final Town newCapital = townyUniverse.getDataSource().getTown(split[1]);
                        if (TownySettings.getNumResidentsCreateNation() > 0 && newCapital.getNumResidents() < TownySettings.getNumResidentsCreateNation()) {
                            TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_not_enough_residents_capital"), newCapital.getName()));
                            return;
                        }
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_CAPITOL.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set capital {town name}");
                        }
                        else {
                            nation.setCapital(newCapital);
                            NationCommand.plugin.resetCache();
                            TownyMessaging.sendPrefixedNationMessage(nation, TownySettings.getNewKingMsg(newCapital.getMayor().getName(), nation.getName()));
                        }
                    }
                    catch (TownyException e) {
                        TownyMessaging.sendErrorMsg(player, e.getMessage());
                    }
                }
                else if (split[0].equalsIgnoreCase("spawn")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_SPAWN.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    try {
                        nation.setNationSpawn(player.getLocation());
                        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_nation_spawn"));
                    }
                    catch (TownyException e) {
                        TownyMessaging.sendErrorMsg(player, e.getMessage());
                    }
                }
                else if (split[0].equalsIgnoreCase("taxes")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_TAXES.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split.length < 2) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /nation set taxes 70");
                    }
                    else {
                        final int amount = Integer.parseInt(split[1].trim());
                        if (amount < 0) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                            return;
                        }
                        try {
                            nation.setTaxes(amount);
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_town_set_nation_tax"), player.getName(), split[1]));
                        }
                        catch (NumberFormatException e2) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                        }
                    }
                }
                else {
                    if (split[0].equalsIgnoreCase("spawncost")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_SPAWNCOST.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set spawncost 70");
                            break Label_2150;
                        }
                        try {
                            final double amount2 = Double.parseDouble(split[1]);
                            if (amount2 < 0.0) {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                return;
                            }
                            if (TownySettings.getSpawnTravelCost() < amount2) {
                                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_cannot_set_spawn_cost_more_than"), TownySettings.getSpawnTravelCost()));
                                return;
                            }
                            nation.setSpawnCost(amount2);
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_spawn_cost_set_to"), player.getName(), TownySettings.getLangString("nation_sing"), split[1]));
                            break Label_2150;
                        }
                        catch (NumberFormatException e3) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_num"));
                            return;
                        }
                    }
                    if (split[0].equalsIgnoreCase("name")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_NAME.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set name Plutoria");
                        }
                        else {
                            if (TownySettings.getNationRenameCost() > 0.0) {
                                try {
                                    if (TownySettings.isUsingEconomy() && !nation.getAccount().pay(TownySettings.getNationRenameCost(), String.format("Nation renamed to: %s", split[1]))) {
                                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_no_money"), TownyEconomyHandler.getFormattedBalance(TownySettings.getNationRenameCost())));
                                    }
                                }
                                catch (EconomyException e4) {
                                    throw new TownyException("Economy Error");
                                }
                            }
                            if (!NameValidation.isBlacklistName(split[1])) {
                                nationRename(player, nation, split[1]);
                            }
                            else {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                            }
                        }
                    }
                    else if (split[0].equalsIgnoreCase("tag")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_TAG.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set tag PLT");
                        }
                        else if (split[1].equalsIgnoreCase("clear")) {
                            try {
                                nation.setTag(" ");
                                TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_reset_nation_tag"), player.getName()));
                            }
                            catch (TownyException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                            }
                        }
                        else {
                            nation.setTag(NameValidation.checkAndFilterName(split[1]));
                        }
                        TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_set_nation_tag"), player.getName(), nation.getTag()));
                    }
                    else if (split[0].equalsIgnoreCase("title")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_TITLE.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set title bilbo Jester ");
                        }
                        else {
                            resident = townyUniverse.getDataSource().getResident(split[1]);
                        }
                        if (!resident.hasNation()) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_nation"), resident.getName()));
                            return;
                        }
                        if (resident.getTown().getNation() != townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation()) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_nation"), resident.getName()));
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
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_set_title"), resident.getName(), resident.getTitle()));
                        }
                        else {
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Title", resident.getName()));
                        }
                    }
                    else if (split[0].equalsIgnoreCase("surname")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_SURNAME.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set surname bilbo the dwarf ");
                        }
                        else {
                            resident = townyUniverse.getDataSource().getResident(split[1]);
                        }
                        if (!resident.hasNation()) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_nation"), resident.getName()));
                            return;
                        }
                        if (resident.getTown().getNation() != townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation()) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_not_same_nation"), resident.getName()));
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
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_set_surname"), resident.getName(), resident.getSurname()));
                        }
                        else {
                            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Surname", resident.getName()));
                        }
                    }
                    else {
                        if (!split[0].equalsIgnoreCase("board")) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), split[0]));
                            return;
                        }
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_SET_BOARD.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        if (split.length < 2) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /nation set board " + TownySettings.getLangString("town_help_9"));
                            return;
                        }
                        final String line = StringMgmt.join(StringMgmt.remFirstArg(split), " ");
                        if (!NameValidation.isValidString(line)) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_invalid_string_nationboard_not_set"));
                            return;
                        }
                        nation.setNationBoard(line);
                        TownyMessaging.sendNationBoard(player, nation);
                    }
                }
            }
            townyUniverse.getDataSource().saveNation(nation);
            townyUniverse.getDataSource().saveNationList();
        }
    }
    
    public static void nationToggle(final Player player, final String[] split, final boolean admin, Nation nation) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/nation toggle"));
            player.sendMessage(ChatTools.formatCommand("", "/nation toggle", "peaceful", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation toggle", "public", ""));
            player.sendMessage(ChatTools.formatCommand("", "/nation toggle", "open", ""));
        }
        else {
            try {
                if (!admin) {
                    final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                    nation = resident.getTown().getNation();
                }
                else {
                    @SuppressWarnings("unused")
					final Resident resident = nation.getKing();
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
            if (split[0].equalsIgnoreCase("peaceful") || split[0].equalsIgnoreCase("neutral")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_NEUTRAL.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                try {
                    final boolean choice = !nation.isNeutral();
                    final double cost = TownySettings.getNationNeutralityCost();
                    if (choice && TownySettings.isUsingEconomy() && !nation.getAccount().pay(cost, "Peaceful Nation Cost")) {
                        throw new TownyException(TownySettings.getLangString("msg_nation_cant_peaceful"));
                    }
                    nation.setNeutral(choice);
                    if (TownySettings.isUsingEconomy() && cost > 0.0) {
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_paid"), TownyEconomyHandler.getFormattedBalance(cost)));
                    }
                    else {
                        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_nation_set_peaceful"));
                    }
                    TownyMessaging.sendPrefixedNationMessage(nation, TownySettings.getLangString("msg_nation_peaceful") + (nation.isNeutral() ? "§2" : "§4 not") + " peaceful.");
                }
                catch (TownyException e2) {
                    try {
                        nation.setNeutral(false);
                    }
                    catch (TownyException e1) {
                        e1.printStackTrace();
                    }
                    TownyMessaging.sendErrorMsg(player, e2.getMessage());
                }
                catch (Exception e3) {
                    TownyMessaging.sendErrorMsg(player, e3.getMessage());
                }
            }
            else if (split[0].equalsIgnoreCase("public")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_PUBLIC.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                nation.setPublic(!nation.isPublic());
                TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_nation_changed_public"), nation.isPublic() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
            }
            else {
                if (!split[0].equalsIgnoreCase("open")) {
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "nation"));
                    return;
                }
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_PUBLIC.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                nation.setOpen(!nation.isOpen());
                TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_nation_changed_open"), nation.isOpen() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
            }
            townyUniverse.getDataSource().saveNation(nation);
        }
    }
    
    public static void nationRename(final Player player, final Nation nation, final String newName) {
        final NationPreRenameEvent event = new NationPreRenameEvent(nation, newName);
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_rename_cancelled"));
            return;
        }
        try {
            TownyUniverse.getInstance().getDataSource().renameNation(nation, newName);
            TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_nation_set_name"), player.getName(), nation.getName()));
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public static void nationSpawn(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
            if (split.length == 0) {
                if (!resident.hasTown()) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_dont_belong_nation"));
                    return;
                }
                if (!resident.getTown().hasNation()) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_dont_belong_nation"));
                    return;
                }
                final Nation nation = resident.getTown().getNation();
                final String notAffordMSG = TownySettings.getLangString("msg_err_cant_afford_tp");
                SpawnUtil.sendToTownySpawn(player, split, nation, notAffordMSG, false, SpawnType.NATION);
            }
            else {
                final Nation nation = townyUniverse.getDataSource().getNation(split[0]);
                final String notAffordMSG = String.format(TownySettings.getLangString("msg_err_cant_afford_tp_nation"), nation.getName());
                SpawnUtil.sendToTownySpawn(player, split, nation, notAffordMSG, false, SpawnType.NATION);
            }
        }
        catch (NotRegisteredException e) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
        }
    }
    
    static {
        nation_help = new ArrayList<String>();
        king_help = new ArrayList<String>();
        alliesstring = new ArrayList<String>();
        invite = new ArrayList<String>();
        BY_NUM_RESIDENTS = ((n1, n2) -> n2.getNumResidents() - n1.getNumResidents());
        BY_NAME = ((n1, n2) -> n1.getName().compareTo(n2.getName()));
        BY_BANK_BALANCE = ((n1, n2) -> {
            try {
                return Double.compare(n2.getAccount().getHoldingBalance(), n1.getAccount().getHoldingBalance());
            }
            catch (EconomyException e) {
                throw new RuntimeException("Failed to get balance. Aborting.");
            }
        });
        BY_TOWNBLOCKS_CLAIMED = ((n1, n2) -> Double.compare(n2.getNumTownblocks(), n1.getNumTownblocks()));
        BY_NUM_TOWNS = ((n1, n2) -> n2.getTowns().size() - n1.getTowns().size());
        BY_NUM_ONLINE = ((n1, n2) -> TownyAPI.getInstance().getOnlinePlayers(n2).size() - TownyAPI.getInstance().getOnlinePlayers(n1).size());
        NationCommand.nation_help.add(ChatTools.formatTitle("/nation"));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "", TownySettings.getLangString("nation_help_1")));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", TownySettings.getLangString("nation_help_2"), TownySettings.getLangString("nation_help_3")));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "list", TownySettings.getLangString("nation_help_4")));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "townlist (nation)", ""));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "allylist (nation)", ""));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "enemylist (nation)", ""));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "online", TownySettings.getLangString("nation_help_9")));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "spawn", TownySettings.getLangString("nation_help_10")));
        NationCommand.nation_help.add(ChatTools.formatCommand("", "/nation", "join (nation)", "Used to join open nations."));
        NationCommand.nation_help.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/nation", "deposit [$]", ""));
        NationCommand.nation_help.add(ChatTools.formatCommand(TownySettings.getLangString("mayor_sing"), "/nation", "leave", TownySettings.getLangString("nation_help_5")));
        NationCommand.nation_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "king ?", TownySettings.getLangString("nation_help_7")));
        NationCommand.nation_help.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/nation", "new " + TownySettings.getLangString("nation_help_2") + " [capital]", TownySettings.getLangString("nation_help_8")));
        NationCommand.nation_help.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/nation", "delete " + TownySettings.getLangString("nation_help_2"), ""));
        NationCommand.nation_help.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/nation", "say", "[message]"));
        NationCommand.king_help.add(ChatTools.formatTitle(TownySettings.getLangString("king_help_1")));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "withdraw [$]", ""));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "[add/kick] [town] .. [town]", ""));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "rank [add/remove] " + TownySettings.getLangString("res_2"), "[Rank]"));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "set [] .. []", ""));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "toggle [] .. []", ""));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "ally [] .. [] " + TownySettings.getLangString("nation_help_2"), TownySettings.getLangString("king_help_2")));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "enemy [add/remove] " + TownySettings.getLangString("nation_help_2"), TownySettings.getLangString("king_help_3")));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "delete", ""));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "merge {nation}", ""));
        NationCommand.king_help.add(ChatTools.formatCommand(TownySettings.getLangString("king_sing"), "/nation", "say", "[message]"));
        NationCommand.alliesstring.add(ChatTools.formatTitle("/nation invite"));
        NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally add [nation]", TownySettings.getLangString("nation_ally_help_1")));
        if (TownySettings.isDisallowOneWayAlliance()) {
            NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally add -[nation]", TownySettings.getLangString("nation_ally_help_7")));
        }
        NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally remove [nation]", TownySettings.getLangString("nation_ally_help_2")));
        if (TownySettings.isDisallowOneWayAlliance()) {
            NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally sent", TownySettings.getLangString("nation_ally_help_3")));
            NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally received", TownySettings.getLangString("nation_ally_help_4")));
            NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally accept [nation]", TownySettings.getLangString("nation_ally_help_5")));
            NationCommand.alliesstring.add(ChatTools.formatCommand("", "/nation", "ally deny [nation]", TownySettings.getLangString("nation_ally_help_6")));
        }
        NationCommand.invite.add(ChatTools.formatTitle("/town invite"));
        NationCommand.invite.add(ChatTools.formatCommand("", "/nation", "invite [town]", TownySettings.getLangString("nation_invite_help_1")));
        NationCommand.invite.add(ChatTools.formatCommand("", "/nation", "invite -[town]", TownySettings.getLangString("nation_invite_help_2")));
        NationCommand.invite.add(ChatTools.formatCommand("", "/nation", "invite sent", TownySettings.getLangString("nation_invite_help_3")));
    }
}
