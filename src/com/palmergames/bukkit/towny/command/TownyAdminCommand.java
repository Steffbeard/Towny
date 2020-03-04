// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import com.palmergames.bukkit.towny.exceptions.InvalidMetadataTypeException;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.TownyLogger;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Arrays;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.object.Nation;
import javax.naming.InvalidNameException;
import com.palmergames.bukkit.towny.event.NationPreRenameEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.utils.SpawnUtil;
import com.palmergames.bukkit.towny.object.SpawnType;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.TownPreRenameEvent;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import com.palmergames.bukkit.towny.utils.ResidentUtil;
import com.palmergames.bukkit.util.NameValidation;
import com.palmergames.bukkit.towny.tasks.TownClaim;
import com.palmergames.bukkit.towny.utils.AreaSelectionUtil;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.util.MemMgmt;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.tasks.PlotClaim;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.ArrayList;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.TownyTimerHandler;
import com.palmergames.bukkit.towny.db.TownyFlatFileSource;
import java.io.IOException;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class TownyAdminCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> ta_help;
    private static final List<String> ta_panel;
    private static final List<String> ta_unclaim;
    private boolean isConsole;
    private Player player;
    private CommandSender sender;
    
    public TownyAdminCommand(final Towny instance) {
        TownyAdminCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        this.sender = sender;
        if (sender instanceof Player) {
            this.player = (Player)sender;
            this.isConsole = false;
        }
        else {
            this.isConsole = true;
            this.player = null;
        }
        try {
            return this.parseTownyAdminCommand(args);
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(sender, e.getMessage());
            return true;
        }
    }
    
    private Object getSender() {
        if (this.isConsole) {
            return this.sender;
        }
        return this.player;
    }
    
    public boolean parseTownyAdminCommand(final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (this.getSender() == this.player && !townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SCREEN.getNode())) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        if (split.length == 0) {
            this.buildTAPanel();
            for (final String line : TownyAdminCommand.ta_panel) {
                this.sender.sendMessage(line);
            }
        }
        else if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
            for (final String line : TownyAdminCommand.ta_help) {
                this.sender.sendMessage(line);
            }
        }
        else {
            if (split[0].equalsIgnoreCase("set")) {
                this.adminSet(StringMgmt.remFirstArg(split));
                return true;
            }
            if (split[0].equalsIgnoreCase("resident")) {
                this.parseAdminResidentCommand(StringMgmt.remFirstArg(split));
                return true;
            }
            if (split[0].equalsIgnoreCase("town")) {
                this.parseAdminTownCommand(StringMgmt.remFirstArg(split));
                return true;
            }
            if (split[0].equalsIgnoreCase("nation")) {
                this.parseAdminNationCommand(StringMgmt.remFirstArg(split));
                return true;
            }
            if (split[0].equalsIgnoreCase("toggle")) {
                this.parseToggleCommand(StringMgmt.remFirstArg(split));
                return true;
            }
            if (split[0].equalsIgnoreCase("plot")) {
                this.parseAdminPlotCommand(StringMgmt.remFirstArg(split));
                return true;
            }
            if (!this.isConsole && !townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split[0].equalsIgnoreCase("givebonus") || split[0].equalsIgnoreCase("giveplots")) {
                this.giveBonus(StringMgmt.remFirstArg(split));
            }
            else if (split[0].equalsIgnoreCase("reload")) {
                this.reloadTowny(false);
            }
            else if (split[0].equalsIgnoreCase("reset")) {
                this.reloadTowny(true);
            }
            else if (split[0].equalsIgnoreCase("backup")) {
                try {
                    townyUniverse.getDataSource().backup();
                    TownyMessaging.sendMsg(this.getSender(), TownySettings.getLangString("mag_backup_success"));
                }
                catch (IOException e) {
                    TownyMessaging.sendErrorMsg(this.getSender(), "Error: " + e.getMessage());
                }
            }
            else {
                if (split[0].equalsIgnoreCase("database")) {
                    this.parseAdminDatabaseCommand(StringMgmt.remFirstArg(split));
                    return true;
                }
                if (split[0].equalsIgnoreCase("mysqldump")) {
                    if (TownySettings.getSaveDatabase().equalsIgnoreCase("mysql") && TownySettings.getLoadDatabase().equalsIgnoreCase("mysql")) {
                        final TownyDataSource dataSource = new TownyFlatFileSource(TownyAdminCommand.plugin, townyUniverse);
                        dataSource.saveAll();
                        TownyMessaging.sendMsg(this.getSender(), TownySettings.getLangString("msg_mysql_dump_success"));
                        return true;
                    }
                    throw new TownyException(TownySettings.getLangString("msg_err_mysql_not_being_used"));
                }
                else if (split[0].equalsIgnoreCase("newday")) {
                    TownyTimerHandler.newDay();
                }
                else if (split[0].equalsIgnoreCase("purge")) {
                    this.purge(StringMgmt.remFirstArg(split));
                }
                else if (split[0].equalsIgnoreCase("delete")) {
                    final String[] newSplit = StringMgmt.remFirstArg(split);
                    this.residentDelete(this.player, newSplit);
                }
                else if (split[0].equalsIgnoreCase("unclaim")) {
                    this.parseAdminUnclaimCommand(StringMgmt.remFirstArg(split));
                }
                else if (split[0].equalsIgnoreCase("checkperm")) {
                    this.parseAdminCheckPermCommand(StringMgmt.remFirstArg(split));
                }
                else {
                    if (!split[0].equalsIgnoreCase("tpplot")) {
                        TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_sub"));
                        return false;
                    }
                    this.parseAdminTpPlotCommand(StringMgmt.remFirstArg(split));
                }
            }
        }
        return true;
    }
    
    private void parseAdminDatabaseCommand(final String[] split) {
        if (split.length == 0 || split.length > 2 || split[0].equalsIgnoreCase("?")) {
            this.sender.sendMessage(ChatTools.formatTitle("/townyadmin database"));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin database", "save", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin database", "load", ""));
            return;
        }
        if (split[0].equalsIgnoreCase("save")) {
            TownyUniverse.getInstance().getDataSource().saveAll();
            TownyMessaging.sendMsg(this.getSender(), TownySettings.getLangString("msg_save_success"));
        }
        else if (split[0].equalsIgnoreCase("load")) {
            TownyUniverse.getInstance().clearAll();
            TownyUniverse.getInstance().getDataSource().loadAll();
            TownyMessaging.sendMsg(this.getSender(), TownySettings.getLangString("msg_load_success"));
        }
    }
    
    private void parseAdminPlotCommand(final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (this.isConsole) {
            this.sender.sendMessage("[Towny] InputError: This command was designed for use in game only.");
            return;
        }
        if (split.length == 0 || split.length < 1 || split[0].equalsIgnoreCase("?")) {
            this.sender.sendMessage(ChatTools.formatTitle("/townyadmin plot"));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin plot claim", "[player]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin plot meta", "", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin plot meta", "set [key] [value]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin plot meta", "[add|remove] [key]", ""));
            return;
        }
        if (split[0].equalsIgnoreCase("meta")) {
            handlePlotMetaCommand(this.player, split);
            return;
        }
        if (split[0].equalsIgnoreCase("claim")) {
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PLOT_CLAIM.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split.length == 1) {
                TownyMessaging.sendErrorMsg(this.sender, TownySettings.getLangString("msg_error_ta_plot_claim"));
                return;
            }
            Resident resident = null;
            try {
                resident = townyUniverse.getDataSource().getResident(split[1]);
            }
            catch (NotRegisteredException e) {
                TownyMessaging.sendErrorMsg(this.sender, String.format(TownySettings.getLangString("msg_error_no_player_with_that_name"), split[1].toString()));
            }
            final Player player = BukkitTools.getPlayer(this.sender.getName());
            final String world = player.getWorld().getName();
            final List<WorldCoord> selection = new ArrayList<WorldCoord>();
            selection.add(new WorldCoord(world, Coord.parseCoord((Entity)player)));
            if (resident != null) {
                new PlotClaim(TownyAdminCommand.plugin, player, resident, selection, true, true, false).start();
            }
        }
    }
    
    private void parseAdminCheckPermCommand(final String[] split) throws TownyException {
        if (split.length != 2) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_input"), "Eg: /ta checkperm {name} {node}"));
        }
        final Player player = TownyAPI.getInstance().getPlayer(TownyUniverse.getInstance().getDataSource().getResident(split[0]));
        if (player == null) {
            throw new TownyException("Player couldn't be found");
        }
        final String node = split[1];
        if (player.hasPermission(node)) {
            TownyMessaging.sendMessage(this.sender, "Permission true");
        }
        else {
            TownyMessaging.sendErrorMsg(this.sender, "Permission false");
        }
    }
    
    private void parseAdminTpPlotCommand(final String[] split) throws TownyException {
        if (split.length != 3) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_input"), "Eg: /ta tpplot world x z"));
        }
        final Player player = (Player)this.sender;
        double y = 1.0;
        if (Bukkit.getServer().getWorld(split[0]) != null) {
            final World world = Bukkit.getServer().getWorld(split[0]);
            final double x = Double.parseDouble(split[1]) * TownySettings.getTownBlockSize();
            final double z = Double.parseDouble(split[2]) * TownySettings.getTownBlockSize();
            y = Bukkit.getWorld(world.getName()).getHighestBlockYAt(new Location(world, x, y, z));
            final Location loc = new Location(world, x, y, z);
            player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            return;
        }
        throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_input"), "Eg: /ta tpplot world x z"));
    }
    
    private void giveBonus(final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        boolean isTown = false;
        try {
            if (split.length != 2) {
                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_input"), "Eg: givebonus [town/player] [n]"));
            }
            Town town;
            try {
                town = townyUniverse.getDataSource().getTown(split[0]);
                isTown = true;
            }
            catch (NotRegisteredException e2) {
                town = townyUniverse.getDataSource().getResident(split[0]).getTown();
            }
            try {
                town.setBonusBlocks(town.getBonusBlocks() + Integer.parseInt(split[1].trim()));
                TownyMessaging.sendMsg(this.getSender(), String.format(TownySettings.getLangString("msg_give_total"), town.getName(), split[1], town.getBonusBlocks()));
                if (!this.isConsole || isTown) {
                    TownyMessaging.sendTownMessagePrefixed(town, "You have been given " + Integer.parseInt(split[1].trim()) + " bonus townblocks.");
                }
                if (this.isConsole && !isTown) {
                    TownyMessaging.sendMessage(town, "You have been given " + Integer.parseInt(split[1].trim()) + " bonus townblocks.");
                    TownyMessaging.sendMessage(town, "If you have paid any real-life money for these townblocks please understand: the creators of Towny do not condone this transaction, the server you play on breaks the Minecraft EULA and, worse, is selling a part of Towny which the developers did not intend to be sold.");
                    TownyMessaging.sendMessage(town, "If you did pay real money you should consider playing on a Towny server that respects the wishes of the Towny Team.");
                }
            }
            catch (NumberFormatException nfe) {
                throw new TownyException(TownySettings.getLangString("msg_error_must_be_int"));
            }
            townyUniverse.getDataSource().saveTown(town);
        }
        catch (TownyException e) {
            throw new TownyException(e.getMessage());
        }
    }
    
    private void buildTAPanel() {
        TownyAdminCommand.ta_panel.clear();
        final Runtime run = Runtime.getRuntime();
        TownyAdminCommand.ta_panel.add(ChatTools.formatTitle(TownySettings.getLangString("ta_panel_1")));
        TownyAdminCommand.ta_panel.add("§3[§bTowny§3] §2" + TownySettings.getLangString("ta_panel_2") + "§a" + TownyAPI.getInstance().isWarTime() + "§8" + " | " + "§2" + TownySettings.getLangString("ta_panel_3") + (TownyTimerHandler.isHealthRegenRunning() ? "§aOn" : "§cOff") + "§8" + " | " + "§2" + TownySettings.getLangString("ta_panel_5") + (TownyTimerHandler.isDailyTimerRunning() ? "§aOn" : "§cOff"));
        TownyAdminCommand.ta_panel.add("§3[§b" + TownySettings.getLangString("ta_panel_8") + "§3" + "] " + "§2" + TownySettings.getLangString("ta_panel_9") + "§a" + MemMgmt.getMemSize(run.totalMemory()) + "§8" + " | " + "§2" + TownySettings.getLangString("ta_panel_10") + "§a" + Thread.getAllStackTraces().keySet().size() + "§8" + " | " + "§2" + TownySettings.getLangString("ta_panel_11") + "§a" + TownyFormatter.getTime());
        TownyAdminCommand.ta_panel.add("§e" + MemMgmt.getMemoryBar(50, run));
    }
    
    public void parseAdminUnclaimCommand(final String[] split) {
        if (split.length == 1 && split[0].equalsIgnoreCase("?")) {
            for (final String line : TownyAdminCommand.ta_unclaim) {
                ((CommandSender)this.getSender()).sendMessage(line);
            }
        }
        else {
            if (this.isConsole) {
                this.sender.sendMessage("[Towny] InputError: This command was designed for use in game only.");
                return;
            }
            try {
                if (TownyAPI.getInstance().isWarTime()) {
                    throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                }
                List<WorldCoord> selection = AreaSelectionUtil.selectWorldCoordArea(null, new WorldCoord(this.player.getWorld().getName(), Coord.parseCoord((Entity)this.player)), split);
                selection = AreaSelectionUtil.filterWildernessBlocks(selection);
                new TownClaim(TownyAdminCommand.plugin, this.player, null, selection, false, false, true).start();
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(this.player, x.getMessage());
            }
        }
    }
    
    public void parseAdminResidentCommand(final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            this.sender.sendMessage(ChatTools.formatTitle("/townyadmin resident"));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin resident", "[resident]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin resident", "[resident] rename [newname]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin resident", "[resident] friend... [add|remove] [resident]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin resident", "[resident] friend... |list|clear]", ""));
            return;
        }
        try {
            final Resident resident = townyUniverse.getDataSource().getResident(split[0]);
            if (split.length == 1) {
                TownyMessaging.sendMessage(this.getSender(), TownyFormatter.getStatus(resident, this.player));
                return;
            }
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RESIDENT.getNode(split[1].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split[1].equalsIgnoreCase("rename")) {
                if (!NameValidation.isBlacklistName(split[2])) {
                    townyUniverse.getDataSource().renamePlayer(resident, split[2]);
                }
                else {
                    TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_invalid_name"));
                }
            }
            else if (split[1].equalsIgnoreCase("friend")) {
                if (split.length == 2) {
                    this.sender.sendMessage(ChatTools.formatTitle("/townyadmin resident {resident} friend"));
                    this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin resident", "[resident] friend... [add|remove] [resident]", ""));
                    this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin resident", "[resident] friend... |list|clear]", ""));
                    return;
                }
                if (this.isConsole) {
                    throw new TownyException("/ta resident {resident} friend cannot be run from console.");
                }
                ResidentCommand.residentFriend(BukkitTools.getPlayer(this.sender.getName()), StringMgmt.remArgs(split, 2), true, resident);
            }
            else if (split[1].equalsIgnoreCase("unjail")) {
                final Player jailedPlayer = TownyAPI.getInstance().getPlayer(resident);
                if (this.player == null) {
                    throw new TownyException(String.format("%s is not online", resident.getName()));
                }
                if (!resident.isJailed()) {
                    throw new TownyException(TownySettings.getLangString("msg_player_not_jailed_in_your_town"));
                }
                resident.setJailed(false);
                final String town = resident.getJailTown();
                final int index = resident.getJailSpawn();
                try {
                    final Location loc = Bukkit.getWorld(townyUniverse.getDataSource().getTownWorld(town).getName()).getSpawnLocation();
                    jailedPlayer.sendMessage(String.format(TownySettings.getLangString("msg_town_spawn_warmup"), TownySettings.getTeleportWarmupTime()));
                    TownyAPI.getInstance().jailTeleport(jailedPlayer, loc);
                    resident.removeJailSpawn();
                    resident.setJailTown(" ");
                    TownyMessaging.sendMsg(this.player, "You have been freed from jail.");
                    TownyMessaging.sendPrefixedTownMessage(townyUniverse.getDataSource().getTown(town), jailedPlayer.getName() + " has been freed from jail number " + index);
                }
                catch (TownyException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (NotRegisteredException e2) {
            TownyMessaging.sendErrorMsg(this.getSender(), e2.getMessage());
        }
        catch (TownyException e3) {
            TownyMessaging.sendErrorMsg(this.getSender(), e3.getMessage());
        }
    }
    
    public void parseAdminTownCommand(final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            this.sender.sendMessage(ChatTools.formatTitle("/townyadmin town"));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "new [name] [mayor]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] add/kick [] .. []", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] rename [newname]", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] delete", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] spawn", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] outpost #", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] rank", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] set", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] toggle", ""));
            this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] meta", ""));
            return;
        }
        try {
            if (split[0].equalsIgnoreCase("new")) {
                if (split.length != 3) {
                    throw new TownyException(TownySettings.getLangString("msg_err_not_enough_variables") + "/ta town new [name] [mayor]");
                }
                if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_NEW.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                TownCommand.newTown(this.player, split[1], split[2], true);
            }
            else {
                final Town town = townyUniverse.getDataSource().getTown(split[0]);
                if (split.length == 1) {
                    TownyMessaging.sendMessage(this.getSender(), TownyFormatter.getStatus(town));
                    return;
                }
                if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN.getNode(split[1].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (split[1].equalsIgnoreCase("add")) {
                    TownCommand.townAdd(this.getSender(), town, StringMgmt.remArgs(split, 2));
                }
                else if (split[1].equalsIgnoreCase("kick")) {
                    TownCommand.townKickResidents(this.getSender(), town.getMayor(), town, ResidentUtil.getValidatedResidents(this.getSender(), StringMgmt.remArgs(split, 2)));
                }
                else if (split[1].equalsIgnoreCase("delete")) {
                    if (!this.isConsole) {
                        TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("town_deleted_by_admin"), town.getName()));
                        TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_del_town"), town.getName()));
                        townyUniverse.getDataSource().removeTown(town, false);
                    }
                    else {
                        ConfirmationHandler.addConfirmation(ConfirmationType.TOWN_DELETE, town);
                        TownyMessaging.sendConfirmationMessage((CommandSender)Bukkit.getConsoleSender(), null, null, null, null);
                    }
                }
                else if (split[1].equalsIgnoreCase("rename")) {
                    final TownPreRenameEvent event = new TownPreRenameEvent(town, split[2]);
                    Bukkit.getServer().getPluginManager().callEvent((Event)event);
                    if (event.isCancelled()) {
                        TownyMessaging.sendErrorMsg(this.sender, TownySettings.getLangString("msg_err_rename_cancelled"));
                        return;
                    }
                    if (!NameValidation.isBlacklistName(split[2])) {
                        townyUniverse.getDataSource().renameTown(town, split[2]);
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_town_set_name"), (this.getSender() instanceof Player) ? this.player.getName() : "CONSOLE", town.getName()));
                        TownyMessaging.sendMsg(this.getSender(), String.format(TownySettings.getLangString("msg_town_set_name"), (this.getSender() instanceof Player) ? this.player.getName() : "CONSOLE", town.getName()));
                    }
                    else {
                        TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_invalid_name"));
                    }
                }
                else if (split[1].equalsIgnoreCase("spawn")) {
                    SpawnUtil.sendToTownySpawn(this.player, StringMgmt.remArgs(split, 2), town, "", false, SpawnType.TOWN);
                }
                else if (split[1].equalsIgnoreCase("outpost")) {
                    SpawnUtil.sendToTownySpawn(this.player, StringMgmt.remArgs(split, 2), town, "", true, SpawnType.TOWN);
                }
                else if (split[1].equalsIgnoreCase("rank")) {
                    this.parseAdminTownRankCommand(this.player, town, StringMgmt.remArgs(split, 2));
                }
                else if (split[1].equalsIgnoreCase("toggle")) {
                    if (split.length == 2 || split[2].equalsIgnoreCase("?")) {
                        this.sender.sendMessage(ChatTools.formatTitle("/ta town {townname} toggle"));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "pvp", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "forcepvp", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "public", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "explosion", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "fire", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "mobs", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "taxpercent", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "open", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "jail [number] [resident]", ""));
                        this.sender.sendMessage(ChatTools.formatCommand("", "/ta town {townname} toggle", "forcepvp", ""));
                        return;
                    }
                    if (split[2].equalsIgnoreCase("forcepvp")) {
                        if (town.isAdminEnabledPVP()) {
                            town.setAdminEnabledPVP(false);
                        }
                        else {
                            town.setAdminEnabledPVP(true);
                        }
                        townyUniverse.getDataSource().saveTown(town);
                        TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("msg_town_forcepvp_setting_set_to"), town.getName(), town.isAdminEnabledPVP()));
                    }
                    else {
                        TownCommand.townToggle(this.player, StringMgmt.remArgs(split, 2), true, town);
                    }
                }
                else if (split[1].equalsIgnoreCase("set")) {
                    TownCommand.townSet(this.player, StringMgmt.remArgs(split, 2), true, town);
                }
                else {
                    if (!split[1].equalsIgnoreCase("meta")) {
                        this.sender.sendMessage(ChatTools.formatTitle("/townyadmin town"));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "new [name] [mayor]", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town]", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] add/kick [] .. []", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] rename [newname]", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] delete", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] spawn", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] outpost #", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] rank", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] set", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] toggle", ""));
                        this.sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin town", "[town] meta", ""));
                        return;
                    }
                    handleTownMetaCommand(this.player, town, split);
                }
            }
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(this.getSender(), e.getMessage());
        }
    }
    
    private void parseAdminTownRankCommand(final Player player, final Town town, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length < 3) {
            throw new TownyException("Eg: /townyadmin town [townname] rank add/remove [resident] [rank]");
        }
        Resident target;
        try {
            target = townyUniverse.getDataSource().getResident(split[1]);
            if (!target.hasTown()) {
                throw new TownyException(TownySettings.getLangString("msg_resident_not_your_town"));
            }
            if (target.getTown() != town) {
                throw new TownyException(TownySettings.getLangString("msg_err_townadmintownrank_wrong_town"));
            }
        }
        catch (TownyException x) {
            throw new TownyException(x.getMessage());
        }
        final String rank = split[2];
        if (!TownyPerms.getTownRanks().contains(rank)) {
            throw new TownyException(String.format(TownySettings.getLangString("msg_unknown_rank_available_ranks"), rank, StringMgmt.join(TownyPerms.getTownRanks(), ",")));
        }
        Label_0440: {
            if (split[0].equalsIgnoreCase("add")) {
                try {
                    if (target.addTownRank(rank)) {
                        TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_been_given_rank"), "Town", rank));
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_have_given_rank"), "Town", rank, target.getName()));
                        break Label_0440;
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
                        TownyMessaging.sendMsg(target, String.format(TownySettings.getLangString("msg_you_have_had_rank_taken"), "Town", rank));
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_have_taken_rank_from"), "Town", rank, target.getName()));
                    }
                    break Label_0440;
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
    
	public void parseAdminNationCommand(String[] split) throws TownyException {
		TownyUniverse townyUniverse = TownyUniverse.getInstance();

		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {

			sender.sendMessage(ChatTools.formatTitle("/townyadmin nation"));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "new", "[name] [capital]"));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation]", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation] add [] .. []", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation] rename [newname]", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation] delete", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation] recheck", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation] toggle", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[nation] set", ""));
			sender.sendMessage(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin nation", "[oldnation] merge [newnation]", ""));

			return;
		}
		try {
			
			if (split[0].equalsIgnoreCase("new")) {
				/*
				 * Moved from TownCommand as of 0.92.0.13
				 */
				if (split.length != 3)
					throw new TownyException(TownySettings.getLangString("msg_err_not_enough_variables") + "/ta town new [name] [mayor]");

				if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_NEW.getNode()))
					throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));

				NationCommand.newNation(player, split[1], split[2], true);
				return;
			}
			
			Nation nation = townyUniverse.getDataSource().getNation(split[0]);
			if (split.length == 1) {
				TownyMessaging.sendMessage(getSender(), TownyFormatter.getStatus(nation));
				return;
			}

			if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION.getNode(split[1].toLowerCase())))
				throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));

			if (split[1].equalsIgnoreCase("add")) {
				/*
				 * if (isConsole) { sender.sendMessage(
				 * "[Towny] InputError: This command was designed for use in game only."
				 * ); return; }
				 */
				NationCommand.nationAdd(nation, townyUniverse.getDataSource().getTowns(StringMgmt.remArgs(split, 2)));

			} else if (split[1].equalsIgnoreCase("delete")) {
				if (!isConsole) {
					TownyMessaging.sendMessage(sender, String.format(TownySettings.getLangString("nation_deleted_by_admin"), nation.getName()));
					TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_del_nation"), nation.getName()));
					townyUniverse.getDataSource().removeNation(nation);
				} else {
					ConfirmationHandler.addConfirmation(ConfirmationType.NATION_DELETE, nation); // It takes the nation, an admin deleting another town has no confirmation.
					TownyMessaging.sendConfirmationMessage(Bukkit.getConsoleSender(), null, null, null, null);
				}

			} else if(split[1].equalsIgnoreCase("recheck")) {
				
				nation.recheckTownDistance();
				TownyMessaging.sendMessage(sender, String.format(TownySettings.getLangString("nation_rechecked_by_admin"), nation.getName()));

			} else if (split[1].equalsIgnoreCase("rename")) {

				NationPreRenameEvent event = new NationPreRenameEvent(nation, split[2]);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_err_rename_cancelled"));
					return;
				}
				
				if (!NameValidation.isBlacklistName(split[2])) {
					townyUniverse.getDataSource().renameNation(nation, split[2]);
					TownyMessaging.sendPrefixedNationMessage(nation, String.format(TownySettings.getLangString("msg_nation_set_name"), ((getSender() instanceof Player) ? player.getName() : "CONSOLE"), nation.getName()));
				} else
					TownyMessaging.sendErrorMsg(getSender(), TownySettings.getLangString("msg_invalid_name"));

			} else if (split[1].equalsIgnoreCase("merge")) {
				
				Nation remainingNation = null;
				try {
					remainingNation = townyUniverse.getDataSource().getNation(split[2]);
				} catch (NotRegisteredException e) {
					throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_name"), split[2]));
				}
				if (remainingNation.equals(nation))
					throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_name"), split[2]));
				townyUniverse.getDataSource().mergeNation(nation, remainingNation);
				TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("nation1_has_merged_with_nation2"), nation, remainingNation));

			} else if(split[1].equalsIgnoreCase("set")) {
				
				NationCommand.nationSet(player, StringMgmt.remArgs(split, 2), true, nation);

			} else if(split[1].equalsIgnoreCase("toggle")) {
				
				NationCommand.nationToggle(player, StringMgmt.remArgs(split, 2), true, nation);
			}

		} catch (NotRegisteredException | AlreadyRegisteredException | InvalidNameException e) {
			TownyMessaging.sendErrorMsg(getSender(), e.getMessage());
		}
	}
    
    public void adminSet(String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET.getNode())) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        if (split.length == 0) {
            this.sender.sendMessage(ChatTools.formatTitle("/townyadmin set"));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin set", "mayor [town] " + TownySettings.getLangString("town_help_2"), ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin set", "mayor [town] npc", ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin set", "capital [town]", ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin set", "title [resident] [title]", ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin set", "surname [resident] [surname]", ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin set", "plot [town]", ""));
            return;
        }
        if (split[0].equalsIgnoreCase("mayor")) {
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_MAYOR.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split.length < 3) {
                this.sender.sendMessage(ChatTools.formatTitle("/townyadmin set mayor"));
                this.sender.sendMessage(ChatTools.formatCommand("Eg", "/townyadmin set mayor", "[town] " + TownySettings.getLangString("town_help_2"), ""));
                this.sender.sendMessage(ChatTools.formatCommand("Eg", "/townyadmin set mayor", "[town] npc", ""));
            }
            else {
                try {
                    final Town town = townyUniverse.getDataSource().getTown(split[1]);
                    Resident newMayor;
                    if (split[2].equalsIgnoreCase("npc")) {
                        final String name = this.nextNpcName();
                        townyUniverse.getDataSource().newResident(name);
                        newMayor = townyUniverse.getDataSource().getResident(name);
                        newMayor.setRegistered(System.currentTimeMillis());
                        newMayor.setLastOnline(0L);
                        newMayor.setNPC(true);
                        townyUniverse.getDataSource().saveResident(newMayor);
                        townyUniverse.getDataSource().saveResidentList();
                        town.setHasUpkeep(false);
                    }
                    else {
                        newMayor = townyUniverse.getDataSource().getResident(split[2]);
                    }
                    if (!town.hasResident(newMayor)) {
                        TownCommand.townAddResident(town, newMayor);
                    }
                    final Resident oldMayor = town.getMayor();
                    town.setMayor(newMayor);
                    if (oldMayor.isNPC()) {
                        try {
                            town.removeResident(oldMayor);
                            townyUniverse.getDataSource().removeResident(oldMayor);
                            townyUniverse.getDataSource().removeResidentList(oldMayor);
                            town.setHasUpkeep(true);
                        }
                        catch (EmptyTownException e) {
                            e.printStackTrace();
                        }
                    }
                    townyUniverse.getDataSource().saveTown(town);
                    final String[] msg = TownySettings.getNewMayorMsg(newMayor.getName());
                    TownyMessaging.sendPrefixedTownMessage(town, msg);
                }
                catch (TownyException e2) {
                    TownyMessaging.sendErrorMsg(this.getSender(), e2.getMessage());
                }
            }
        }
        else if (split[0].equalsIgnoreCase("capital")) {
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_CAPITAL.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split.length < 2) {
                this.sender.sendMessage(ChatTools.formatTitle("/townyadmin set capital"));
                this.sender.sendMessage(ChatTools.formatCommand("Eg", "/ta set capital", "[town name]", ""));
            }
            else {
                try {
                    final Town newCapital = townyUniverse.getDataSource().getTown(split[1]);
                    if (TownySettings.getNumResidentsCreateNation() > 0 && newCapital.getNumResidents() < TownySettings.getNumResidentsCreateNation()) {
                        TownyMessaging.sendErrorMsg(this.player, String.format(TownySettings.getLangString("msg_not_enough_residents_capital"), newCapital.getName()));
                        return;
                    }
                    final Nation nation = newCapital.getNation();
                    nation.setCapital(newCapital);
                    TownyAdminCommand.plugin.resetCache();
                    TownyMessaging.sendPrefixedNationMessage(nation, TownySettings.getNewKingMsg(newCapital.getMayor().getName(), nation.getName()));
                    townyUniverse.getDataSource().saveNation(nation);
                    townyUniverse.getDataSource().saveNationList();
                }
                catch (TownyException e2) {
                    TownyMessaging.sendErrorMsg(this.player, e2.getMessage());
                }
            }
        }
        else if (split[0].equalsIgnoreCase("title")) {
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_TITLE.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            Resident resident = null;
            if (split.length < 2) {
                TownyMessaging.sendErrorMsg(this.player, "Eg: /townyadmin set title bilbo Jester");
            }
            else {
                resident = townyUniverse.getDataSource().getResident(split[1]);
            }
            split = StringMgmt.remArgs(split, 2);
            if (StringMgmt.join(split).length() > TownySettings.getMaxTitleLength()) {
                TownyMessaging.sendErrorMsg(this.player, TownySettings.getLangString("msg_err_input_too_long"));
                return;
            }
            final String title = StringMgmt.join(NameValidation.checkAndFilterArray(split));
            resident.setTitle(title + " ");
            townyUniverse.getDataSource().saveResident(resident);
            if (resident.hasTitle()) {
                TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("msg_set_title"), resident.getName(), resident.getTitle()));
                TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_set_title"), resident.getName(), resident.getTitle()));
            }
            else {
                TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Title", resident.getName()));
                TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Title", resident.getName()));
            }
        }
        else if (split[0].equalsIgnoreCase("surname")) {
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_SURNAME.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            Resident resident = null;
            if (split.length < 2) {
                TownyMessaging.sendErrorMsg(this.player, "Eg: /townyadmin set surname bilbo Jester");
            }
            else {
                resident = townyUniverse.getDataSource().getResident(split[1]);
            }
            split = StringMgmt.remArgs(split, 2);
            if (StringMgmt.join(split).length() > TownySettings.getMaxTitleLength()) {
                TownyMessaging.sendErrorMsg(this.player, TownySettings.getLangString("msg_err_input_too_long"));
                return;
            }
            final String surname = StringMgmt.join(NameValidation.checkAndFilterArray(split));
            resident.setSurname(surname + " ");
            townyUniverse.getDataSource().saveResident(resident);
            if (resident.hasSurname()) {
                TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("msg_set_surname"), resident.getName(), resident.getSurname()));
                TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_set_surname"), resident.getName(), resident.getSurname()));
            }
            else {
                TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Surname", resident.getName()));
                TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_clear_title_surname"), "Surname", resident.getName()));
            }
        }
        else if (split[0].equalsIgnoreCase("plot")) {
            if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_PLOT.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            final TownBlock tb = TownyAPI.getInstance().getTownBlock(this.player.getLocation());
            if (split.length < 2) {
                this.sender.sendMessage(ChatTools.formatTitle("/townyadmin set plot"));
                this.sender.sendMessage(ChatTools.formatCommand("Eg", "/ta set plot", "[town name]", TownySettings.getLangString("msg_admin_set_plot_help_1")));
                this.sender.sendMessage(ChatTools.formatCommand("Eg", "/ta set plot", "[town name] {rect|circle} {radius}", TownySettings.getLangString("msg_admin_set_plot_help_2")));
                this.sender.sendMessage(ChatTools.formatCommand("Eg", "/ta set plot", "[town name] {rect|circle} auto", TownySettings.getLangString("msg_admin_set_plot_help_2")));
                return;
            }
            if (tb != null) {
                try {
                    final Town newTown = townyUniverse.getDataSource().getTown(split[1]);
                    if (newTown != null) {
                        tb.setResident(null);
                        tb.setTown(newTown);
                        tb.setType(TownBlockType.RESIDENTIAL);
                        tb.setName("");
                        TownyMessaging.sendMessage(this.player, String.format(TownySettings.getLangString("changed_plot_town"), newTown.getName()));
                    }
                }
                catch (TownyException e3) {
                    TownyMessaging.sendErrorMsg(this.player, e3.getMessage());
                }
            }
            else {
                final Town town = townyUniverse.getDataSource().getTown(split[1]);
                final TownyWorld world = townyUniverse.getDataSource().getWorld(this.player.getWorld().getName());
                final Coord key = Coord.parseCoord(TownyAdminCommand.plugin.getCache(this.player).getLastLocation());
                List<WorldCoord> selection;
                if (split.length == 2) {
                    selection = AreaSelectionUtil.selectWorldCoordArea(town, new WorldCoord(world.getName(), key), new String[0]);
                }
                else {
                    String[] newSplit = StringMgmt.remFirstArg(split);
                    newSplit = StringMgmt.remFirstArg(newSplit);
                    selection = AreaSelectionUtil.selectWorldCoordArea(town, new WorldCoord(world.getName(), key), newSplit);
                }
                TownyMessaging.sendDebugMsg("Admin Initiated townClaim: Pre-Filter Selection [" + selection.size() + "] " + Arrays.toString(selection.toArray(new WorldCoord[0])));
                selection = AreaSelectionUtil.filterTownOwnedBlocks(selection);
                TownyMessaging.sendDebugMsg("Admin Initiated townClaim: Post-Filter Selection [" + selection.size() + "] " + Arrays.toString(selection.toArray(new WorldCoord[0])));
                new TownClaim(TownyAdminCommand.plugin, this.player, town, selection, false, true, false).start();
            }
        }
        else {
            TownyMessaging.sendErrorMsg(this.getSender(), String.format(TownySettings.getLangString("msg_err_invalid_property"), "administrative"));
        }
    }
    
    public String nextNpcName() throws TownyException {
        int i = 0;
        do {
            final String name = TownySettings.getNPCPrefix() + ++i;
            if (!TownyUniverse.getInstance().getDataSource().hasResident(name)) {
                return name;
            }
        } while (i <= 100000);
        throw new TownyException(TownySettings.getLangString("msg_err_too_many_npc"));
    }
    
    public void reloadTowny(final Boolean reset) {
        if (reset) {
            TownyUniverse.getInstance().getDataSource().deleteFile(TownyAdminCommand.plugin.getConfigPath());
        }
        if (TownyAdminCommand.plugin.load()) {
            TownyPerms.registerPermissionNodes();
            TownyPerms.updateOnlinePerms();
        }
        TownyMessaging.sendMsg(this.sender, TownySettings.getLangString("msg_reloaded"));
    }
    
    public void purge(final String[] split) {
        if (split.length == 0) {
            this.sender.sendMessage(ChatTools.formatTitle("/townyadmin purge"));
            this.sender.sendMessage(ChatTools.formatCommand("", "/townyadmin purge", "[number of days] {townless}", ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "", "Removes offline residents not seen for this duration.", ""));
            this.sender.sendMessage(ChatTools.formatCommand("", "", "Optional {townless} flag limits purge to only people that have no town.", ""));
            return;
        }
        String days = "";
        if (split.length == 2 && split[1].equalsIgnoreCase("townless")) {
            days += "townless";
        }
        try {
            days += String.valueOf(split[0]);
        }
        catch (NumberFormatException e2) {
            TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_error_must_be_int"));
            return;
        }
        if (!this.isConsole) {
            Resident resident = null;
            try {
                resident = TownyUniverse.getInstance().getDataSource().getResident(this.player.getName());
            }
            catch (TownyException e) {
                TownyMessaging.sendErrorMsg(this.player, e.getMessage());
            }
            if (resident != null) {
                try {
                    ConfirmationHandler.addConfirmation(resident, ConfirmationType.PURGE, days);
                    TownyMessaging.sendConfirmationMessage((CommandSender)this.player, null, null, null, null);
                }
                catch (TownyException e) {
                    TownyMessaging.sendErrorMsg(this.player, e.getMessage());
                }
            }
        }
        else {
            ConfirmationHandler.addConfirmation(ConfirmationType.PURGE, days);
            TownyMessaging.sendConfirmationMessage((CommandSender)Bukkit.getConsoleSender(), null, null, null, null);
        }
    }
    
    public void residentDelete(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
        }
        else {
            try {
                if (!townyUniverse.getPermissionSource().isTownyAdmin(player)) {
                    throw new TownyException(TownySettings.getLangString("msg_err_admin_only_delete"));
                }
                for (final String name : split) {
                    try {
                        final Resident resident = townyUniverse.getDataSource().getResident(name);
                        if (!resident.isNPC() && !BukkitTools.isOnline(resident.getName())) {
                            townyUniverse.getDataSource().removeResident(resident);
                            townyUniverse.getDataSource().removeResidentList(resident);
                            TownyMessaging.sendGlobalMessage(TownySettings.getDelResidentMsg(resident));
                        }
                        else {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_online_or_npc"), name));
                        }
                    }
                    catch (NotRegisteredException x2) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_name"), name));
                    }
                }
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
            }
        }
    }
    
    public void parseToggleCommand(final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            this.player.sendMessage(ChatTools.formatTitle("/townyadmin toggle"));
            this.player.sendMessage(ChatTools.formatCommand("", "/townyadmin toggle", "war", ""));
            this.player.sendMessage(ChatTools.formatCommand("", "/townyadmin toggle", "peaceful", ""));
            this.player.sendMessage(ChatTools.formatCommand("", "/townyadmin toggle", "devmode", ""));
            this.player.sendMessage(ChatTools.formatCommand("", "/townyadmin toggle", "debug", ""));
            this.player.sendMessage(ChatTools.formatCommand("", "/townyadmin toggle", "townwithdraw/nationwithdraw", ""));
            this.player.sendMessage(ChatTools.formatCommand("", "/townyadmin toggle npc", "[resident]", ""));
            return;
        }
        if (!townyUniverse.getPermissionSource().testPermission(this.player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE.getNode(split[0].toLowerCase()))) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        if (split[0].equalsIgnoreCase("war")) {
            final boolean choice = TownyAPI.getInstance().isWarTime();
            if (!choice) {
                townyUniverse.startWarEvent();
                TownyMessaging.sendMsg(this.getSender(), TownySettings.getLangString("msg_war_started"));
            }
            else {
                townyUniverse.endWarEvent();
                TownyMessaging.sendMsg(this.getSender(), TownySettings.getLangString("msg_war_ended"));
            }
        }
        else {
            if (!split[0].equalsIgnoreCase("peaceful")) {
                if (!split[0].equalsIgnoreCase("neutral")) {
                    if (split[0].equalsIgnoreCase("devmode")) {
                        try {
                            final boolean choice = !TownySettings.isDevMode();
                            TownySettings.setDevMode(choice);
                            TownyMessaging.sendMsg(this.getSender(), "Dev Mode " + (choice ? ("§2" + TownySettings.getLangString("enabled")) : ("§4" + TownySettings.getLangString("disabled"))));
                        }
                        catch (Exception e) {
                            TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_choice"));
                        }
                        return;
                    }
                    if (split[0].equalsIgnoreCase("debug")) {
                        try {
                            final boolean choice = !TownySettings.getDebug();
                            TownySettings.setDebug(choice);
                            TownyLogger.getInstance().refreshDebugLogger();
                            TownyMessaging.sendMsg(this.getSender(), "Debug Mode " + (choice ? ("§2" + TownySettings.getLangString("enabled")) : ("§4" + TownySettings.getLangString("disabled"))));
                        }
                        catch (Exception e) {
                            TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_choice"));
                        }
                        return;
                    }
                    if (split[0].equalsIgnoreCase("townwithdraw")) {
                        try {
                            final boolean choice = !TownySettings.getTownBankAllowWithdrawls();
                            TownySettings.SetTownBankAllowWithdrawls(choice);
                            TownyMessaging.sendMsg(this.getSender(), "Town Withdrawls " + (choice ? ("§2" + TownySettings.getLangString("enabled")) : ("§4" + TownySettings.getLangString("disabled"))));
                        }
                        catch (Exception e) {
                            TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_choice"));
                        }
                        return;
                    }
                    if (split[0].equalsIgnoreCase("nationwithdraw")) {
                        try {
                            final boolean choice = !TownySettings.geNationBankAllowWithdrawls();
                            TownySettings.SetNationBankAllowWithdrawls(choice);
                            TownyMessaging.sendMsg(this.getSender(), "Nation Withdrawls " + (choice ? ("§2" + TownySettings.getLangString("enabled")) : ("§4" + TownySettings.getLangString("disabled"))));
                        }
                        catch (Exception e) {
                            TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_choice"));
                        }
                        return;
                    }
                    if (split[0].equalsIgnoreCase("npc")) {
                        if (split.length != 2) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_input"), "Eg: toggle npc [resident]"));
                        }
                        try {
                            final Resident resident = townyUniverse.getDataSource().getResident(split[1]);
                            resident.setNPC(!resident.isNPC());
                            townyUniverse.getDataSource().saveResident(resident);
                            TownyMessaging.sendMessage(this.sender, String.format(TownySettings.getLangString("msg_npc_flag"), resident.isNPC(), resident.getName()));
                            return;
                        }
                        catch (NotRegisteredException x) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_not_registered_1"), split[1]));
                        }
                    }
                    TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_choice"));
                    return;
                }
            }
            try {
                final boolean choice = !TownySettings.isDeclaringNeutral();
                TownySettings.setDeclaringNeutral(choice);
                TownyMessaging.sendMsg(this.getSender(), String.format(TownySettings.getLangString("msg_nation_allow_peaceful"), choice ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg(this.getSender(), TownySettings.getLangString("msg_err_invalid_choice"));
            }
        }
    }
    
    public static void handleTownMetaCommand(final Player player, final Town town, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_META.getNode())) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        if (split.length == 2) {
            if (town.hasMeta()) {
                player.sendMessage(ChatTools.formatTitle("Custom Meta Data"));
                for (final CustomDataField field : town.getMetadata()) {
                    player.sendMessage(field.getKey() + " = " + field.getValue());
                }
            }
            else {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_this_town_doesnt_have_any_associated_metadata"));
            }
            return;
        }
        if (split.length < 4) {
            player.sendMessage(ChatTools.formatTitle("/townyadmin town {townname} meta"));
            player.sendMessage(ChatTools.formatCommand("", "meta", "set", "The key of a registered data field"));
            player.sendMessage(ChatTools.formatCommand("", "meta", "add", "Add a key of a registered data field"));
            player.sendMessage(ChatTools.formatCommand("", "meta", "remove", "Remove a key from the town"));
            return;
        }
        if (split.length == 5) {
            final String mdKey = split[3];
            final String val = split[4];
            if (!townyUniverse.getRegisteredMetadataMap().containsKey(mdKey)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_the_metadata_for_key_is_not_registered"), mdKey));
                return;
            }
            if (split[2].equalsIgnoreCase("set")) {
                final CustomDataField md = townyUniverse.getRegisteredMetadataMap().get(mdKey);
                if (town.hasMeta()) {
                    for (final CustomDataField cdf : town.getMetadata()) {
                        if (cdf.equals(md)) {
                            try {
                                cdf.isValidType(val);
                            }
                            catch (InvalidMetadataTypeException e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                                return;
                            }
                            cdf.setValue(val);
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_key_x_was_successfully_updated_to_x"), mdKey, cdf.getValue()));
                            townyUniverse.getDataSource().saveTown(town);
                            return;
                        }
                    }
                }
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_key_x_is_not_part_of_this_town"), mdKey));
            }
        }
        else if (split[2].equalsIgnoreCase("add")) {
            final String mdKey = split[3];
            if (!townyUniverse.getRegisteredMetadataMap().containsKey(mdKey)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_the_metadata_for_key_is_not_registered"), mdKey));
                return;
            }
            final CustomDataField md2 = townyUniverse.getRegisteredMetadataMap().get(mdKey);
            if (town.hasMeta()) {
                for (final CustomDataField cdf2 : town.getMetadata()) {
                    if (cdf2.equals(md2)) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_key_x_already_exists"), mdKey));
                        return;
                    }
                }
            }
            TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_custom_data_was_successfully_added_to_town"));
            town.addMetaData(md2.newCopy());
        }
        else if (split[2].equalsIgnoreCase("remove")) {
            final String mdKey = split[3];
            if (!townyUniverse.getRegisteredMetadataMap().containsKey(mdKey)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_the_metadata_for_key_is_not_registered"), mdKey));
                return;
            }
            final CustomDataField md2 = townyUniverse.getRegisteredMetadataMap().get(mdKey);
            if (town.hasMeta()) {
                for (final CustomDataField cdf2 : town.getMetadata()) {
                    if (cdf2.equals(md2)) {
                        town.removeMetaData(cdf2);
                        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_data_successfully_deleted"));
                        return;
                    }
                }
            }
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_key_cannot_be_deleted"));
        }
    }
    
    public static boolean handlePlotMetaCommand(final Player player, final String[] split) throws TownyException {
        final String world = player.getWorld().getName();
        TownBlock townBlock = null;
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
            return false;
        }
        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PLOT_META.getNode())) {
            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
        }
        if (split.length == 1) {
            if (townBlock.hasMeta()) {
                player.sendMessage(ChatTools.formatTitle("Custom Meta Data"));
                for (final CustomDataField field : townBlock.getMetadata()) {
                    player.sendMessage(field.getKey() + " = " + field.getValue());
                }
            }
            else {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_this_plot_doesnt_have_any_associated_metadata"));
            }
            return true;
        }
        if (split.length < 3) {
            player.sendMessage(ChatTools.formatTitle("/townyadmin plot meta"));
            player.sendMessage(ChatTools.formatCommand("", "meta", "set", "The key of a registered data field"));
            player.sendMessage(ChatTools.formatCommand("", "meta", "add", "Add a key of a registered data field"));
            player.sendMessage(ChatTools.formatCommand("", "meta", "remove", "Remove a key from the town"));
            return false;
        }
        if (split.length == 4) {
            final String mdKey = split[2];
            final String val = split[3];
            if (!townyUniverse.getRegisteredMetadataMap().containsKey(mdKey)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_the_metadata_for_key_is_not_registered"), mdKey));
                return false;
            }
            if (split[1].equalsIgnoreCase("set")) {
                final CustomDataField md = townyUniverse.getRegisteredMetadataMap().get(mdKey);
                if (townBlock.hasMeta()) {
                    for (final CustomDataField cdf : townBlock.getMetadata()) {
                        if (cdf.equals(md)) {
                            try {
                                cdf.isValidType(val);
                            }
                            catch (InvalidMetadataTypeException e2) {
                                TownyMessaging.sendErrorMsg(player, e2.getMessage());
                                return false;
                            }
                            cdf.setValue(val);
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_key_x_was_successfully_updated_to_x"), mdKey, cdf.getValue()));
                            townyUniverse.getDataSource().saveTownBlock(townBlock);
                            return true;
                        }
                    }
                }
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_key_x_is_not_part_of_this_plot"), mdKey));
                return false;
            }
        }
        else if (split[1].equalsIgnoreCase("add")) {
            final String mdKey = split[2];
            if (!townyUniverse.getRegisteredMetadataMap().containsKey(mdKey)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_the_metadata_for_key_is_not_registered"), mdKey));
                return false;
            }
            final CustomDataField md2 = townyUniverse.getRegisteredMetadataMap().get(mdKey);
            if (townBlock.hasMeta()) {
                for (final CustomDataField cdf2 : townBlock.getMetadata()) {
                    if (cdf2.equals(md2)) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_key_x_already_exists"), mdKey));
                        return false;
                    }
                }
            }
            TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_custom_data_was_successfully_added_to_townblock"));
            townBlock.addMetaData(md2.newCopy());
        }
        else if (split[1].equalsIgnoreCase("remove")) {
            final String mdKey = split[2];
            if (!townyUniverse.getRegisteredMetadataMap().containsKey(mdKey)) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_the_metadata_for_key_is_not_registered"), mdKey));
                return false;
            }
            final CustomDataField md2 = townyUniverse.getRegisteredMetadataMap().get(mdKey);
            if (townBlock.hasMeta()) {
                for (final CustomDataField cdf2 : townBlock.getMetadata()) {
                    if (cdf2.equals(md2)) {
                        townBlock.removeMetaData(cdf2);
                        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_data_successfully_deleted"));
                        return true;
                    }
                }
            }
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_key_cannot_be_deleted"));
            return false;
        }
        return true;
    }
    
    static {
        ta_help = new ArrayList<String>();
        ta_panel = new ArrayList<String>();
        ta_unclaim = new ArrayList<String>();
        TownyAdminCommand.ta_help.add(ChatTools.formatTitle("/townyadmin"));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "", TownySettings.getLangString("admin_panel_1")));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "set [] .. []", "'/townyadmin set' " + TownySettings.getLangString("res_5")));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "unclaim [radius]", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "town/nation", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "plot", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "givebonus [town/player] [num]", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "toggle peaceful/war/debug/devmode", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "resident/town/nation", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "tpplot {world} {x} {z}", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "checkperm {name} {node}", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "reload", TownySettings.getLangString("admin_panel_2")));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "reset", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "backup", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "mysqldump", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "database [save/load]", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "newday", TownySettings.getLangString("admin_panel_3")));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "purge [number of days]", ""));
        TownyAdminCommand.ta_help.add(ChatTools.formatCommand("", "/townyadmin", "delete [] .. []", "delete a residents data files."));
        TownyAdminCommand.ta_unclaim.add(ChatTools.formatTitle("/townyadmin unclaim"));
        TownyAdminCommand.ta_unclaim.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin unclaim", "", TownySettings.getLangString("townyadmin_help_1")));
        TownyAdminCommand.ta_unclaim.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin unclaim", "[radius]", TownySettings.getLangString("townyadmin_help_2")));
    }
}
