// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import java.util.LinkedList;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import java.util.Arrays;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.TownyAPI;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.SpawnUtil;
import com.palmergames.bukkit.towny.object.SpawnType;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class ResidentCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> output;
    
    public ResidentCommand(final Towny instance) {
        ResidentCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (args == null) {
                for (final String line : ResidentCommand.output) {
                    player.sendMessage(line);
                }
                this.parseResidentCommand(player, args);
            }
            else {
                this.parseResidentCommand(player, args);
            }
        }
        else {
            try {
                this.parseResidentCommandForConsole(sender, args);
            }
            catch (TownyException ex) {}
        }
        return true;
    }
    
	@SuppressWarnings("static-access")
	private void parseResidentCommandForConsole(final CommandSender sender, String[] split) throws TownyException {

		if (split.length == 0 || split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
			
			for (String line : output)
				sender.sendMessage(line);
			
		} else if (split[0].equalsIgnoreCase("list")) {

			listResidents(sender);

		} else {
			try {
				final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(split[0]);
				Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
					Player player = null;
					TownyMessaging.sendMessage(sender, TownyFormatter.getStatus(resident, player));
				});
			} catch (NotRegisteredException x) {
				throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
            }
        }
    }
    
    @SuppressWarnings("static-access")
	public void parseResidentCommand(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            if (split.length == 0) {
                try {
                    final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                    TownyMessaging.sendMessage(player, TownyFormatter.getStatus(resident, player));
                    return;
                }
                catch (NotRegisteredException x2) {
                    throw new TownyException(TownySettings.getLangString("msg_err_not_registered"));
                }
            }
            if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
                for (final String line : ResidentCommand.output) {
                    player.sendMessage(line);
                }
            }
            else if (split[0].equalsIgnoreCase("list")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_LIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.listResidents(player);
            }
            else {
                if (split[0].equalsIgnoreCase("tax")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_TAX.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    try {
                        final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                        TownyMessaging.sendMessage(player, TownyFormatter.getTaxStatus(resident));
                        return;
                    }
                    catch (NotRegisteredException x2) {
                        throw new TownyException(TownySettings.getLangString("msg_err_not_registered"));
                    }
                }
                if (split[0].equalsIgnoreCase("jail")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_JAIL.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (!TownySettings.isAllowingBail()) {
                        TownyMessaging.sendErrorMsg(player, "§4" + TownySettings.getLangString("msg_err_bail_not_enabled"));
                        return;
                    }
                    if (split.length == 1) {
                        player.sendMessage(ChatTools.formatTitle("/resident jail"));
                        player.sendMessage(ChatTools.formatCommand("", "/resident", "jail paybail", ""));
                        player.sendMessage("§b" + TownySettings.getLangString("msg_resident_bail_amount") + "§2" + TownyEconomyHandler.getFormattedBalance(TownySettings.getBailAmount()));
                        player.sendMessage("§b" + TownySettings.getLangString("msg_mayor_bail_amount") + "§2" + TownyEconomyHandler.getFormattedBalance(TownySettings.getBailAmountMayor()));
                        player.sendMessage("§b" + TownySettings.getLangString("msg_king_bail_amount") + "§2" + TownyEconomyHandler.getFormattedBalance(TownySettings.getBailAmountKing()));
                        return;
                    }
                    if (!townyUniverse.getDataSource().getResident(player.getName()).isJailed()) {
                        return;
                    }
                    if (split[1].equalsIgnoreCase("paybail")) {
                        final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                        if (resident.getAccount().canPayFromHoldings(TownySettings.getBailAmount())) {
                            final Town JailTown = townyUniverse.getDataSource().getTown(resident.getJailTown());
                            resident.getAccount().payTo(TownySettings.getBailAmount(), JailTown, "Bail");
                            resident.setJailed(false);
                            resident.setJailSpawn(0);
                            resident.setJailTown("");
                            TownyMessaging.sendGlobalMessage("§4" + player.getName() + TownySettings.getLangString("msg_has_paid_bail"));
                            player.teleport(resident.getTown().getSpawn());
                            townyUniverse.getDataSource().saveResident(resident);
                        }
                        else {
                            TownyMessaging.sendErrorMsg(player, "§4" + TownySettings.getLangString("msg_err_unable_to_pay_bail"));
                        }
                    }
                    else {
                        player.sendMessage(ChatTools.formatTitle("/resident jail"));
                        player.sendMessage(ChatTools.formatCommand("", "/resident", "jail paybail", ""));
                        player.sendMessage("§b" + TownySettings.getLangString("msg_resident_bail_amount") + "§2" + TownyEconomyHandler.getFormattedBalance(TownySettings.getBailAmount()));
                        player.sendMessage("§b" + TownySettings.getLangString("msg_mayor_bail_amount") + "§2" + TownyEconomyHandler.getFormattedBalance(TownySettings.getBailAmountMayor()));
                        player.sendMessage("§b" + TownySettings.getLangString("msg_king_bail_amount") + "§2" + TownyEconomyHandler.getFormattedBalance(TownySettings.getBailAmountKing()));
                    }
                }
                else if (split[0].equalsIgnoreCase("set")) {
                    final String[] newSplit = StringMgmt.remFirstArg(split);
                    this.residentSet(player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("toggle")) {
                    final String[] newSplit = StringMgmt.remFirstArg(split);
                    this.residentToggle(player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("friend")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_FRIEND.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final String[] newSplit = StringMgmt.remFirstArg(split);
                    residentFriend(player, newSplit, false, null);
                }
                else if (split[0].equalsIgnoreCase("spawn")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_SPAWN.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                    SpawnUtil.sendToTownySpawn(player, split, resident, TownySettings.getLangString("msg_err_cant_afford_tp"), false, SpawnType.RESIDENT);
                
                }else {
    				
                	try {
    					final Resident resident = townyUniverse.getDataSource().getResident(split[0]);
    					if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_OTHERRESIDENT.getNode()) && (!resident.getName().equals(player.getName()))) {
    						throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
    					}
    					Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> TownyMessaging.sendMessage(player, TownyFormatter.getStatus(resident, player)));
    				} catch (NotRegisteredException x) {
    					throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[0]));
                    }
                }
            }
        }
        catch (Exception x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
        }
    }
    
    private void residentToggle(final Player player, final String[] newSplit) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Resident resident;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
        }
        catch (NotRegisteredException e) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered"), player.getName()));
        }
        if (newSplit.length == 0) {
            player.sendMessage(ChatTools.formatTitle("/res toggle"));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "pvp", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "fire", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "mobs", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "plotborder", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "constantplotborder", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "ignoreplots", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "townclaim", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "map", ""));
            player.sendMessage(ChatTools.formatCommand("", "/res toggle", "spy", ""));
            TownyMessaging.sendMsg(resident, TownySettings.getLangString("msg_modes_set") + StringMgmt.join(resident.getModes(), ","));
            return;
        }
        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE.getNode(newSplit[0].toLowerCase()))) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        final TownyPermission perm = resident.getPermissions();
        if (!newSplit[0].equalsIgnoreCase("spy")) {
            if (newSplit[0].equalsIgnoreCase("pvp")) {
                if (TownySettings.getPVPCoolDownTime() > 0 && resident.hasTown() && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                    if (CooldownTimerTask.hasCooldown(resident.getTown().getName(), CooldownTimerTask.CooldownType.PVP)) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(resident.getTown().getName(), CooldownTimerTask.CooldownType.PVP)));
                    }
                    if (CooldownTimerTask.hasCooldown(resident.getName(), CooldownTimerTask.CooldownType.PVP)) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(resident.getName(), CooldownTimerTask.CooldownType.PVP)));
                    }
                }
                perm.pvp = !perm.pvp;
                if (TownySettings.getPVPCoolDownTime() > 0 && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                    CooldownTimerTask.addCooldownTimer(resident.getName(), CooldownTimerTask.CooldownType.PVP);
                }
            }
            else if (newSplit[0].equalsIgnoreCase("fire")) {
                perm.fire = !perm.fire;
            }
            else if (newSplit[0].equalsIgnoreCase("explosion")) {
                perm.explosion = !perm.explosion;
            }
            else {
                if (!newSplit[0].equalsIgnoreCase("mobs")) {
                    resident.toggleMode(newSplit, true);
                    return;
                }
                perm.mobs = !perm.mobs;
            }
            this.notifyPerms(player, perm);
            townyUniverse.getDataSource().saveResident(resident);
            return;
        }
        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_CHAT_SPY.getNode(newSplit[0].toLowerCase()))) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        resident.toggleMode(newSplit, true);
    }
    
    private void notifyPerms(final Player player, final TownyPermission perm) {
        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_perms"));
        TownyMessaging.sendMessage(player, "§2PvP: " + (perm.pvp ? "§4ON" : "§aOFF") + "§2" + "  Explosions: " + (perm.explosion ? "§4ON" : "§aOFF") + "§2" + "  Firespread: " + (perm.fire ? "§4ON" : "§aOFF") + "§2" + "  Mob Spawns: " + (perm.mobs ? "§4ON" : "§aOFF"));
    }
    
    public void listResidents(final Player player) {
        player.sendMessage(ChatTools.formatTitle(TownySettings.getLangString("res_list")));
        final ArrayList<String> formatedList = new ArrayList<String>();
        for (final Resident resident : TownyAPI.getInstance().getActiveResidents()) {
            if (player.canSee(BukkitTools.getPlayerExact(resident.getName()))) {
                String colour;
                if (resident.isKing()) {
                    colour = "§6";
                }
                else if (resident.isMayor()) {
                    colour = "§b";
                }
                else {
                    colour = "§f";
                }
                formatedList.add(colour + resident.getName() + "§f");
            }
        }
        for (final String line : ChatTools.list(formatedList)) {
            player.sendMessage(line);
        }
    }
    
    public void listResidents(final CommandSender sender) {
        sender.sendMessage(ChatTools.formatTitle(TownySettings.getLangString("res_list")));
        final ArrayList<String> formatedList = new ArrayList<String>();
        for (final Resident resident : TownyAPI.getInstance().getActiveResidents()) {
            String colour;
            if (resident.isKing()) {
                colour = "§6";
            }
            else if (resident.isMayor()) {
                colour = "§b";
            }
            else {
                colour = "§f";
            }
            formatedList.add(colour + resident.getName() + "§f");
        }
        for (final String line : ChatTools.list(formatedList)) {
            sender.sendMessage(line);
        }
    }
    
    public void residentSet(final Player player, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatCommand("", "/resident set", "perm ...", "'/resident set perm' " + TownySettings.getLangString("res_5")));
            player.sendMessage(ChatTools.formatCommand("", "/resident set", "mode ...", "'/resident set mode' " + TownySettings.getLangString("res_5")));
        }
        else {
            Resident resident;
            try {
                resident = townyUniverse.getDataSource().getResident(player.getName());
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_SET.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split[0].equalsIgnoreCase("perm")) {
                final String[] newSplit = StringMgmt.remFirstArg(split);
                TownCommand.setTownBlockPermissions(player, resident, resident.getPermissions(), newSplit, true);
            }
            else {
                if (!split[0].equalsIgnoreCase("mode")) {
                    TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "town"));
                    return;
                }
                final String[] newSplit = StringMgmt.remFirstArg(split);
                this.setMode(player, newSplit);
            }
            townyUniverse.getDataSource().saveResident(resident);
        }
    }
    
    private void setMode(final Player player, final String[] split) {
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatCommand("", "/resident set mode", "clear", ""));
            player.sendMessage(ChatTools.formatCommand("", "/resident set mode", "[mode] ...[mode]", ""));
            player.sendMessage(ChatTools.formatCommand("Mode", "map", "", TownySettings.getLangString("mode_1")));
            player.sendMessage(ChatTools.formatCommand("Mode", "townclaim", "", TownySettings.getLangString("mode_2")));
            player.sendMessage(ChatTools.formatCommand("Mode", "townunclaim", "", TownySettings.getLangString("mode_3")));
            player.sendMessage(ChatTools.formatCommand("Mode", "tc", "", TownySettings.getLangString("mode_4")));
            player.sendMessage(ChatTools.formatCommand("Mode", "nc", "", TownySettings.getLangString("mode_5")));
            player.sendMessage(ChatTools.formatCommand("Mode", "ignoreplots", "", ""));
            player.sendMessage(ChatTools.formatCommand("Mode", "constantplotborder", "", ""));
            player.sendMessage(ChatTools.formatCommand("Mode", "plotborder", "", ""));
            player.sendMessage(ChatTools.formatCommand("Eg", "/resident set mode", "map townclaim town nation general", ""));
            return;
        }
        if (split[0].equalsIgnoreCase("reset") || split[0].equalsIgnoreCase("clear")) {
            ResidentCommand.plugin.removePlayerMode(player);
            return;
        }
        final List<String> list = Arrays.asList(split);
        if (list.contains("spy") && !TownyUniverse.getInstance().getPermissionSource().has(player, PermissionNodes.TOWNY_CHAT_SPY.getNode())) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_command_disable"));
            return;
        }
        ResidentCommand.plugin.setPlayerMode(player, split, true);
    }
    
    public static void residentFriend(final Player player, final String[] split, final boolean admin, Resident resident) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            player.sendMessage(ChatTools.formatCommand("", "/resident friend", "add " + TownySettings.getLangString("res_2"), ""));
            player.sendMessage(ChatTools.formatCommand("", "/resident friend", "remove " + TownySettings.getLangString("res_2"), ""));
            player.sendMessage(ChatTools.formatCommand("", "/resident friend", "list", ""));
            player.sendMessage(ChatTools.formatCommand("", "/resident friend", "clear", ""));
        }
        else {
            try {
                if (!admin) {
                    resident = townyUniverse.getDataSource().getResident(player.getName());
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return;
            }
            if (split[0].equalsIgnoreCase("add")) {
                final String[] names = StringMgmt.remFirstArg(split);
                residentFriendAdd(player, resident, townyUniverse.getDataSource().getResidents(player, names));
            }
            else if (split[0].equalsIgnoreCase("remove")) {
                final String[] names = StringMgmt.remFirstArg(split);
                residentFriendRemove(player, resident, townyUniverse.getDataSource().getResidents(player, names));
            }
            else if (split[0].equalsIgnoreCase("list")) {
                residentFriendList(player, resident);
            }
            else if (split[0].equalsIgnoreCase("clearlist") || split[0].equalsIgnoreCase("clear")) {
                residentFriendRemove(player, resident, resident.getFriends());
            }
        }
    }
    
    private static void residentFriendList(final Player player, final Resident resident) {
        player.sendMessage(ChatTools.formatTitle(TownySettings.getLangString("friend_list")));
        final ArrayList<String> formatedList = new ArrayList<String>();
        for (final Resident friends : resident.getFriends()) {
            String colour;
            if (friends.isKing()) {
                colour = "§6";
            }
            else if (friends.isMayor()) {
                colour = "§b";
            }
            else {
                colour = "§f";
            }
            formatedList.add(colour + friends.getName() + "§f");
        }
        for (final String line : ChatTools.list(formatedList)) {
            player.sendMessage(line);
        }
    }
    
    public static void residentFriendAdd(final Player player, final Resident resident, final List<Resident> invited) {
        final ArrayList<Resident> remove = new ArrayList<Resident>();
        for (final Resident newFriend : invited) {
            try {
                resident.addFriend(newFriend);
                ResidentCommand.plugin.deleteCache(newFriend.getName());
            }
            catch (AlreadyRegisteredException e) {
                remove.add(newFriend);
            }
        }
        for (final Resident newFriend : remove) {
            invited.remove(newFriend);
        }
        if (invited.size() > 0) {
            StringBuilder msg = new StringBuilder(TownySettings.getLangString("res_friend_added"));
            for (final Resident newFriend2 : invited) {
                msg.append(newFriend2.getName()).append(", ");
                final Player p = BukkitTools.getPlayer(newFriend2.getName());
                if (p != null) {
                    TownyMessaging.sendMsg(p, String.format(TownySettings.getLangString("msg_friend_add"), player.getName()));
                }
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            msg.append(TownySettings.getLangString("msg_to_list"));
            TownyMessaging.sendMsg(player, msg.toString());
            TownyUniverse.getInstance().getDataSource().saveResident(resident);
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    public static void residentFriendRemove(final Player player, final Resident resident, final List<Resident> kicking) {
        final List<Resident> remove = new ArrayList<Resident>();
        final List<Resident> toKick = new ArrayList<Resident>(kicking);
        for (final Resident friend : toKick) {
            try {
                resident.removeFriend(friend);
                ResidentCommand.plugin.deleteCache(friend.getName());
            }
            catch (NotRegisteredException e) {
                remove.add(friend);
            }
        }
        if (remove.size() > 0) {
            for (final Resident friend : remove) {
                toKick.remove(friend);
            }
        }
        if (toKick.size() > 0) {
            StringBuilder msg = new StringBuilder(TownySettings.getLangString("msg_removed"));
            for (final Resident member : toKick) {
                msg.append(member.getName()).append(", ");
                final Player p = BukkitTools.getPlayer(member.getName());
                if (p != null) {
                    TownyMessaging.sendMsg(p, String.format(TownySettings.getLangString("msg_friend_remove"), player.getName()));
                }
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
            msg.append(TownySettings.getLangString("msg_from_list"));
            TownyMessaging.sendMsg(player, msg.toString());
            TownyUniverse.getInstance().getDataSource().saveResident(resident);
        }
        else {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
        }
    }
    
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final LinkedList<String> output = new LinkedList<String>();
        String lastArg = "";
        if (args.length > 0) {
            lastArg = args[args.length - 1].toLowerCase();
        }
        if (!lastArg.equalsIgnoreCase("")) {
            for (final Resident resident : TownyUniverse.getInstance().getDataSource().getResidents()) {
                if (resident.getName().toLowerCase().startsWith(lastArg)) {
                    output.add(resident.getName());
                }
            }
        }
        return output;
    }
    
    static {
        (output = new ArrayList<String>()).add(ChatTools.formatTitle("/resident"));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "", TownySettings.getLangString("res_1")));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", TownySettings.getLangString("res_2"), TownySettings.getLangString("res_3")));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "list", TownySettings.getLangString("res_4")));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "tax", ""));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "jail", ""));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "toggle", "[mode]...[mode]"));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "set [] .. []", "'/resident set' " + TownySettings.getLangString("res_5")));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "friend [add/remove] " + TownySettings.getLangString("res_2"), TownySettings.getLangString("res_6")));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "friend [add+/remove+] " + TownySettings.getLangString("res_2") + " ", TownySettings.getLangString("res_7")));
        ResidentCommand.output.add(ChatTools.formatCommand("", "/resident", "spawn", ""));
    }
}
