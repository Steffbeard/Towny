// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import java.util.UUID;
import java.util.LinkedList;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.event.TownBlockSettingsChangedEvent;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyPermissionChange;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import org.bukkit.Material;
import org.bukkit.event.Event;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.event.PlotPreClearEvent;
import com.palmergames.bukkit.towny.utils.OutpostUtil;
import com.palmergames.bukkit.util.NameValidation;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.tasks.PlotClaim;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.confirmations.GroupConfirmation;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.utils.AreaSelectionUtil;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class PlotCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    public static final List<String> output;
    
    public PlotCommand(final Towny instance) {
        PlotCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            try {
                if (!TownyUniverse.getInstance().getDataSource().getWorld(player.getWorld().getName()).isUsingTowny()) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_set_use_towny_off"));
                    return false;
                }
            }
            catch (NotRegisteredException ex) {}
            if (args == null) {
                for (final String line : PlotCommand.output) {
                    player.sendMessage(line);
                }
            }
            else {
                try {
                    return this.parsePlotCommand(player, args);
                }
                catch (TownyException x) {
                    x.getMessage();
                }
            }
        }
        else {
            for (final String line2 : PlotCommand.output) {
                sender.sendMessage(Colors.strip(line2));
            }
        }
        return true;
    }
    
    public boolean parsePlotCommand(final Player player, String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            for (final String line : PlotCommand.output) {
                player.sendMessage(line);
            }
        }
        else {
            Resident resident;
            String world;
            try {
                resident = townyUniverse.getDataSource().getResident(player.getName());
                world = player.getWorld().getName();
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
                return true;
            }
            try {
                if (split[0].equalsIgnoreCase("claim")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_CLAIM.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (TownyAPI.getInstance().isWarTime()) {
                        throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                    }
                    final List<WorldCoord> selection = AreaSelectionUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord((Entity)player)), StringMgmt.remFirstArg(split));
                    if (selection.size() > 0) {
                        double cost = 0.0;
                        for (final WorldCoord worldCoord : new ArrayList<WorldCoord>(selection)) {
                            try {
                                final TownBlock block = worldCoord.getTownBlock();
                                final double price = block.getPlotPrice();
                                if (block.hasPlotObjectGroup()) {
                                    final PlotObjectGroup group = block.getPlotObjectGroup();
                                    ConfirmationHandler.addConfirmation(resident, ConfirmationType.GROUP_CLAIM_ACTION, new GroupConfirmation(group, player));
                                    final String firstLine = String.format(TownySettings.getLangString("msg_plot_group_claim_confirmation"), group.getTownBlocks().size()) + " " + TownySettings.getLangString("are_you_sure_you_want_to_continue");
                                    TownyMessaging.sendConfirmationMessage((CommandSender)player, firstLine, null, null, null);
                                    return true;
                                }
                                if (price > -1.0) {
                                    cost += block.getPlotPrice();
                                }
                                else {
                                    if (block.getTown().isMayor(resident)) {
                                        continue;
                                    }
                                    selection.remove(worldCoord);
                                }
                            }
                            catch (NotRegisteredException e) {
                                selection.remove(worldCoord);
                            }
                        }
                        int maxPlots = TownySettings.getMaxResidentPlots(resident);
                        final int extraPlots = TownySettings.getMaxResidentExtraPlots(resident);
                        if (maxPlots != -1) {
                            maxPlots += extraPlots;
                        }
                        if (maxPlots >= 0 && resident.getTownBlocks().size() + selection.size() > maxPlots) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_max_plot_own"), maxPlots));
                        }
                        if (TownySettings.isUsingEconomy() && !resident.getAccount().canPayFromHoldings(cost)) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_no_funds_claim"), selection.size(), TownyEconomyHandler.getFormattedBalance(cost)));
                        }
                        new PlotClaim(PlotCommand.plugin, player, resident, selection, true, false, false).start();
                    }
                    else {
                        player.sendMessage(TownySettings.getLangString("msg_err_empty_area_selection"));
                    }
                }
                else if (split[0].equalsIgnoreCase("evict")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_EVICT.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (TownyAPI.getInstance().isWarTime()) {
                        throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                    }
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final TownBlock townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
                    final Town town = townBlock.getTown();
                    if (townBlock.getResident() == null) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_no_one_to_evict"));
                    }
                    else {
                        Resident owner = townBlock.getResident();
                        if (!town.equals(resident.getTown())) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_part_town"));
                            return false;
                        }
                        if (townBlock.hasPlotObjectGroup()) {
                            for (final TownBlock tb : townBlock.getPlotObjectGroup().getTownBlocks()) {
                                owner = tb.getResident();
                                tb.setResident(null);
                                tb.setPlotPrice(-1.0);
                                tb.setType(townBlock.getType());
                                townyUniverse.getDataSource().saveResident(owner);
                                townyUniverse.getDataSource().saveTownBlock(tb);
                            }
                            player.sendMessage(String.format(TownySettings.getLangString("msg_plot_evict_group"), townBlock.getPlotObjectGroup().getGroupName()));
                            return true;
                        }
                        townBlock.setResident(null);
                        townBlock.setPlotPrice(-1.0);
                        townBlock.setType(townBlock.getType());
                        townyUniverse.getDataSource().saveResident(owner);
                        townyUniverse.getDataSource().saveTownBlock(townBlock);
                        player.sendMessage(TownySettings.getLangString("msg_plot_evict"));
                    }
                }
                else if (split[0].equalsIgnoreCase("unclaim")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_UNCLAIM.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (TownyAPI.getInstance().isWarTime()) {
                        throw new TownyException(TownySettings.getLangString("msg_war_cannot_do"));
                    }
                    if (split.length == 2 && split[1].equalsIgnoreCase("all")) {
                        new PlotClaim(PlotCommand.plugin, player, resident, null, false, false, false).start();
                    }
                    else {
                        List<WorldCoord> selection = AreaSelectionUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord((Entity)player)), StringMgmt.remFirstArg(split));
                        selection = AreaSelectionUtil.filterOwnedBlocks(resident, selection);
                        if (selection.size() > 0) {
                            for (final WorldCoord coord : selection) {
                                final TownBlock block2 = coord.getTownBlock();
                                if (block2.hasPlotObjectGroup()) {
                                    ConfirmationHandler.addConfirmation(resident, ConfirmationType.GROUP_UNCLAIM_ACTION, new GroupConfirmation(block2.getPlotObjectGroup(), player));
                                    final String firstLine2 = String.format(TownySettings.getLangString("msg_plot_group_unclaim_confirmation"), block2.getPlotObjectGroup().getTownBlocks().size()) + " " + TownySettings.getLangString("are_you_sure_you_want_to_continue");
                                    TownyMessaging.sendConfirmationMessage((CommandSender)player, firstLine2, null, null, null);
                                    return true;
                                }
                                new PlotClaim(PlotCommand.plugin, player, resident, selection, false, false, false).start();
                            }
                        }
                        else {
                            player.sendMessage(TownySettings.getLangString("msg_err_empty_area_selection"));
                        }
                    }
                }
                else if (split[0].equalsIgnoreCase("notforsale") || split[0].equalsIgnoreCase("nfs")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_NOTFORSALE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    List<WorldCoord> selection = AreaSelectionUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord((Entity)player)), StringMgmt.remFirstArg(split));
                    final TownBlock townBlock2 = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
                    if (townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                        for (final WorldCoord worldCoord2 : selection) {
                            if (worldCoord2.getTownBlock().hasPlotObjectGroup()) {
                                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_plot_belongs_to_group_plot_nfs"), worldCoord2));
                                return false;
                            }
                            this.setPlotForSale(resident, worldCoord2, -1.0);
                        }
                        return true;
                    }
                    if (!townBlock2.getType().equals(TownBlockType.EMBASSY)) {
                        selection = AreaSelectionUtil.filterOwnedBlocks(resident.getTown(), selection);
                    }
                    else {
                        selection = AreaSelectionUtil.filterOwnedBlocks(resident, selection);
                    }
                    for (final WorldCoord worldCoord2 : selection) {
                        this.setPlotForSale(resident, worldCoord2, -1.0);
                    }
                    if (selection.isEmpty()) {
                        throw new TownyException(TownySettings.getLangString("msg_area_not_own"));
                    }
                }
                else if (split[0].equalsIgnoreCase("forsale") || split[0].equalsIgnoreCase("fs")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_FORSALE.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final WorldCoord pos = new WorldCoord(world, Coord.parseCoord((Entity)player));
                    double plotPrice = pos.getTownBlock().getTown().getPlotTypePrice(pos.getTownBlock().getType());
                    if (split.length > 1) {
                        final int areaSelectPivot = AreaSelectionUtil.getAreaSelectPivot(split);
                        List<WorldCoord> selection2;
                        if (areaSelectPivot >= 0) {
                            selection2 = AreaSelectionUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord((Entity)player)), StringMgmt.subArray(split, areaSelectPivot + 1, split.length));
                            selection2 = AreaSelectionUtil.filterOwnedBlocks(resident.getTown(), selection2);
                            if (selection2.size() == 0) {
                                player.sendMessage(TownySettings.getLangString("msg_err_empty_area_selection"));
                                return true;
                            }
                        }
                        else {
                            selection2 = new ArrayList<WorldCoord>();
                            selection2.add(pos);
                        }
                        if (areaSelectPivot != 1) {
                            try {
                                plotPrice = Double.parseDouble(split[1]);
                                if (plotPrice < 0.0) {
                                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative_money"));
                                    return true;
                                }
                            }
                            catch (NumberFormatException e2) {
                                player.sendMessage(String.format(TownySettings.getLangString("msg_error_must_be_num"), new Object[0]));
                                return true;
                            }
                        }
                        for (final WorldCoord worldCoord3 : selection2) {
                            final TownBlock townBlock3 = worldCoord3.getTownBlock();
                            if (townBlock3.hasPlotObjectGroup()) {
                                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_plot_belongs_to_group_plot_fs2"), worldCoord3));
                            }
                            else {
                                this.setPlotForSale(resident, worldCoord3, plotPrice);
                            }
                        }
                    }
                    else {
                        if (pos.getTownBlock().hasPlotObjectGroup()) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_plot_belongs_to_group_plot_fs2"), pos));
                            return false;
                        }
                        this.setPlotForSale(resident, pos, plotPrice);
                        if (pos.getTownBlock().hasPlotObjectGroup()) {
                            pos.getTownBlock().getPlotObjectGroup().addPlotPrice(plotPrice);
                        }
                    }
                }
                else if (split[0].equalsIgnoreCase("perm") || split[0].equalsIgnoreCase("info")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_PERM.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split.length > 1 && split[1].equalsIgnoreCase("hud")) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_PERM_HUD.getNode())) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        PlotCommand.plugin.getHUDManager().togglePermHUD(player);
                    }
                    else {
                        final TownBlock townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
                        TownyMessaging.sendMessage(player, TownyFormatter.getStatus(townBlock));
                    }
                }
                else if (split[0].equalsIgnoreCase("toggle")) {
                    final TownBlock townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
                    this.plotTestOwner(resident, townBlock);
                    this.plotToggle(player, new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock(), StringMgmt.remFirstArg(split));
                }
                else if (split[0].equalsIgnoreCase("set")) {
                    split = StringMgmt.remFirstArg(split);
                    if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
                        player.sendMessage(ChatTools.formatTitle("/... set"));
                        player.sendMessage(ChatTools.formatCommand("", "set", "[plottype]", "Ex: Inn, Wilds, Farm, Embassy etc"));
                        player.sendMessage(ChatTools.formatCommand("", "set", "outpost", "Costs " + TownyEconomyHandler.getFormattedBalance(TownySettings.getOutpostCost())));
                        player.sendMessage(ChatTools.formatCommand("", "set", "reset", "Removes a plot type"));
                        player.sendMessage(ChatTools.formatCommand("", "set", "[name]", "Names a plot"));
                        player.sendMessage(ChatTools.formatCommand("Level", "[resident/ally/outsider]", "", ""));
                        player.sendMessage(ChatTools.formatCommand("Type", "[build/destroy/switch/itemuse]", "", ""));
                        player.sendMessage(ChatTools.formatCommand("", "set perm", "[on/off]", "Toggle all permissions"));
                        player.sendMessage(ChatTools.formatCommand("", "set perm", "[level/type] [on/off]", ""));
                        player.sendMessage(ChatTools.formatCommand("", "set perm", "[level] [type] [on/off]", ""));
                        player.sendMessage(ChatTools.formatCommand("", "set perm", "reset", ""));
                        player.sendMessage(ChatTools.formatCommand("Eg", "/plot set perm", "ally off", ""));
                        player.sendMessage(ChatTools.formatCommand("Eg", "/plot set perm", "friend build on", ""));
                        player.sendMessage(String.format(TownySettings.getLangString("plot_perms"), "'friend'", "'resident'"));
                        player.sendMessage(TownySettings.getLangString("plot_perms_1"));
                    }
                    else if (split.length > 0) {
                        if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_SET.getNode(split[0].toLowerCase()))) {
                            throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                        }
                        final TownBlock townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
                        if (townBlock.hasPlotObjectGroup()) {
                            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_belongs_to_group_set"));
                            return false;
                        }
                        if (split[0].equalsIgnoreCase("perm")) {
                            final TownBlockOwner owner2 = this.plotTestOwner(resident, townBlock);
                            setTownBlockPermissions(player, owner2, townBlock, StringMgmt.remFirstArg(split));
                            return true;
                        }
                        if (split[0].equalsIgnoreCase("name")) {
                            this.plotTestOwner(resident, townBlock);
                            if (split.length == 1) {
                                townBlock.setName("");
                                TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_plot_name_removed"), new Object[0]));
                                townyUniverse.getDataSource().saveTownBlock(townBlock);
                                return true;
                            }
                            if (!NameValidation.isBlacklistName(split[1])) {
                                townBlock.setName(StringMgmt.join(StringMgmt.remFirstArg(split), ""));
                                townyUniverse.getDataSource().saveTownBlock(townBlock);
                                TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_plot_name_set_to"), townBlock.getName()));
                            }
                            else {
                                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_invalid_name"));
                            }
                            return true;
                        }
                        else if (split[0].equalsIgnoreCase("outpost") && TownySettings.isAllowingOutposts()) {
                            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST.getNode())) {
                                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                            }
                            this.plotTestOwner(resident, townBlock);
                            final Town town = townBlock.getTown();
                            final TownyWorld townyWorld = townBlock.getWorld();
                            final boolean isAdmin = townyUniverse.getPermissionSource().isTownyAdmin(player);
                            final Coord key = Coord.parseCoord(PlotCommand.plugin.getCache(player).getLastLocation());
                            if (OutpostUtil.OutpostTests(town, resident, townyWorld, key, isAdmin, true)) {
                                if (TownySettings.isUsingEconomy() && !town.getAccount().pay(TownySettings.getOutpostCost(), String.format("Plot Set Outpost", new Object[0]))) {
                                    throw new TownyException(TownySettings.getLangString("msg_err_cannot_afford_to_set_outpost"));
                                }
                                TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_plot_set_cost"), TownyEconomyHandler.getFormattedBalance(TownySettings.getOutpostCost()), TownySettings.getLangString("outpost")));
                                townBlock.setOutpost(true);
                                town.addOutpostSpawn(player.getLocation());
                                townyUniverse.getDataSource().saveTown(town);
                                townyUniverse.getDataSource().saveTownBlock(townBlock);
                            }
                            return true;
                        }
                        else {
                            final WorldCoord worldCoord4 = new WorldCoord(world, Coord.parseCoord((Entity)player));
                            this.setPlotType(resident, worldCoord4, split[0]);
                            player.sendMessage(String.format(TownySettings.getLangString("msg_plot_set_type"), split[0]));
                        }
                    }
                    else {
                        player.sendMessage(ChatTools.formatCommand("", "/plot set", "name", ""));
                        player.sendMessage(ChatTools.formatCommand("", "/plot set", "reset", ""));
                        player.sendMessage(ChatTools.formatCommand("", "/plot set", "shop|embassy|arena|wilds|spleef|inn|jail|farm|bank", ""));
                        player.sendMessage(ChatTools.formatCommand("", "/plot set perm", "?", ""));
                    }
                }
                else if (split[0].equalsIgnoreCase("clear")) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_CLEAR.getNode())) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    final TownBlock townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
                    if (townBlock != null) {
                        if (townBlock.hasResident()) {
                            if (!townBlock.isOwner(resident)) {
                                player.sendMessage(TownySettings.getLangString("msg_area_not_own"));
                                return true;
                            }
                        }
                        else if (!townBlock.getTown().equals(resident.getTown())) {
                            player.sendMessage(TownySettings.getLangString("msg_area_not_own"));
                            return true;
                        }
                        final PlotPreClearEvent preEvent = new PlotPreClearEvent(townBlock);
                        BukkitTools.getPluginManager().callEvent((Event)preEvent);
                        if (preEvent.isCancelled()) {
                            player.sendMessage(preEvent.getCancelMessage());
                            return false;
                        }
                        for (final String material : townyUniverse.getDataSource().getWorld(world).getPlotManagementMayorDelete()) {
                            if (Material.matchMaterial(material) == null) {
                                throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_property"), material));
                            }
                            TownyRegenAPI.deleteTownBlockMaterial(townBlock, Material.getMaterial(material));
                            player.sendMessage(String.format(TownySettings.getLangString("msg_clear_plot_material"), material));
                        }
                        BukkitTools.getPluginManager().callEvent((Event)new PlotClearEvent(townBlock));
                    }
                    else {
                        player.sendMessage(TownySettings.getLangString("msg_err_empty_area_selection"));
                    }
                }
                else {
                    if (split[0].equalsIgnoreCase("group")) {
                        return this.handlePlotGroupCommand(StringMgmt.remFirstArg(split), player);
                    }
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_invalid_property"), split[0]));
                }
            }
            catch (TownyException | EconomyException e) {
                TownyMessaging.sendErrorMsg(player, e.getMessage());
            }
        }
        return true;
    }
    
    public static TownyPermissionChange setTownBlockPermissions(final Player player, final TownBlockOwner townBlockOwner, final TownBlock townBlock, final String[] split) {
        TownyPermissionChange permChange = null;
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
            player.sendMessage(ChatTools.formatCommand("Eg", "/plot set perm", "friend build on", ""));
            return null;
        }
        final TownyPermission perm = townBlock.getPermissions();
        if (split.length == 1) {
            if (split[0].equalsIgnoreCase("reset")) {
                permChange = new TownyPermissionChange(TownyPermissionChange.Action.RESET, false, new Object[] { townBlock });
                perm.change(permChange);
                townyUniverse.getDataSource().saveTownBlock(townBlock);
                if (townBlockOwner instanceof Town) {
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_perms_reset"), "Town owned"));
                }
                else {
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_perms_reset"), "your"));
                }
                PlotCommand.plugin.resetCache();
                return permChange;
            }
            try {
                final boolean b = PlotCommand.plugin.parseOnOff(split[0]);
                permChange = new TownyPermissionChange(TownyPermissionChange.Action.ALL_PERMS, b, new Object[0]);
                perm.change(permChange);
                return permChange;
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_plot_set_perm_syntax_error"));
                return null;
            }
        }
        if (split.length == 2) {
            boolean b;
            try {
                b = PlotCommand.plugin.parseOnOff(split[1]);
            }
            catch (Exception e2) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_plot_set_perm_syntax_error"));
                return null;
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
                permChange = new TownyPermissionChange(TownyPermissionChange.Action.PERM_LEVEL, b, new Object[] { permLevel });
            }
            catch (IllegalArgumentException permLevelException) {
                try {
                    final TownyPermission.ActionType actionType = TownyPermission.ActionType.valueOf(split[0].toUpperCase());
                    permChange = new TownyPermissionChange(TownyPermissionChange.Action.ACTION_TYPE, b, new Object[] { actionType });
                }
                catch (IllegalArgumentException actionTypeException) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_plot_set_perm_syntax_error"));
                    return null;
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
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_plot_set_perm_syntax_error"));
                return null;
            }
            try {
                final boolean b2 = PlotCommand.plugin.parseOnOff(split[2]);
                permChange = new TownyPermissionChange(TownyPermissionChange.Action.SINGLE_PERM, b2, new Object[] { permLevel2, actionType2 });
            }
            catch (Exception e3) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_plot_set_perm_syntax_error"));
                return null;
            }
        }
        if (permChange != null) {
            perm.change(permChange);
        }
        townBlock.setChanged(true);
        townyUniverse.getDataSource().saveTownBlock(townBlock);
        if (!townBlock.hasPlotObjectGroup()) {
            TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_set_perms"));
            TownyMessaging.sendMessage(player, "§2 Perm: " + ((townBlockOwner instanceof Resident) ? perm.getColourString().replace("n", "t") : perm.getColourString().replace("f", "r")));
            TownyMessaging.sendMessage(player, "§2 Perm: " + ((townBlockOwner instanceof Resident) ? perm.getColourString2().replace("n", "t") : perm.getColourString2().replace("f", "r")));
            TownyMessaging.sendMessage(player, "§2PvP: " + (perm.pvp ? "§4ON" : "§aOFF") + "§2" + "  Explosions: " + (perm.explosion ? "§4ON" : "§aOFF") + "§2" + "  Firespread: " + (perm.fire ? "§4ON" : "§aOFF") + "§2" + "  Mob Spawns: " + (perm.mobs ? "§4ON" : "§aOFF"));
        }
        final TownBlockSettingsChangedEvent event = new TownBlockSettingsChangedEvent(townBlock);
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
        PlotCommand.plugin.resetCache();
        return permChange;
    }
    
    public void setPlotType(final Resident resident, final WorldCoord worldCoord, final String type) throws TownyException, EconomyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (resident.hasTown()) {
            try {
                final TownBlock townBlock = worldCoord.getTownBlock();
                this.plotTestOwner(resident, townBlock);
                townBlock.setType(type, resident);
                final Town town = resident.getTown();
                if (townBlock.isJail()) {
                    final Player p = TownyAPI.getInstance().getPlayer(resident);
                    if (p == null) {
                        throw new NotRegisteredException();
                    }
                    town.addJailSpawn(p.getLocation());
                }
                townyUniverse.getDataSource().saveTownBlock(townBlock);
                return;
            }
            catch (NotRegisteredException e) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
            }
        }
        if (resident.hasTown()) {
            throw new TownyException(TownySettings.getLangString("msg_err_must_belong_town"));
        }
        final TownBlock townBlock = worldCoord.getTownBlock();
        this.plotTestOwner(resident, townBlock);
        townBlock.setType(type, resident);
        final Town town = resident.getTown();
        if (townBlock.isJail()) {
            final Player p = TownyAPI.getInstance().getPlayer(resident);
            if (p == null) {
                throw new TownyException("Player could not be found.");
            }
            town.addJailSpawn(p.getLocation());
        }
        townyUniverse.getDataSource().saveTownBlock(townBlock);
    }
    
    public void setPlotForSale(final Resident resident, final WorldCoord worldCoord, final double forSale) throws TownyException {
        if (resident.hasTown()) {
            try {
                final TownBlock townBlock = worldCoord.getTownBlock();
                this.plotTestOwner(resident, townBlock);
                if (forSale > TownySettings.getMaxPlotPrice()) {
                    townBlock.setPlotPrice(TownySettings.getMaxPlotPrice());
                }
                else {
                    townBlock.setPlotPrice(forSale);
                }
                if (forSale != -1.0) {
                    TownyMessaging.sendPrefixedTownMessage(townBlock.getTown(), TownySettings.getPlotForSaleMsg(resident.getName(), worldCoord));
                    if (townBlock.getTown() != resident.getTown()) {
                        TownyMessaging.sendMessage(resident, TownySettings.getPlotForSaleMsg(resident.getName(), worldCoord));
                    }
                }
                else {
                    final Player p = TownyAPI.getInstance().getPlayer(resident);
                    if (p == null) {
                        throw new TownyException("Player could not be found.");
                    }
                    p.sendMessage(TownySettings.getLangString("msg_plot_set_to_nfs"));
                }
                TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
                return;
            }
            catch (NotRegisteredException e) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
            }

        }
        throw new TownyException(TownySettings.getLangString("msg_err_must_belong_town"));
    }
    
    public void setGroupForSale(final Resident resident, final PlotObjectGroup group, final double price) throws TownyException {
        group.setPrice(price);
        if (resident.hasTown()) {
            try {
                if (price > TownySettings.getMaxPlotPrice()) {
                    group.setPrice(TownySettings.getMaxPlotPrice());
                }
                else {
                    group.setPrice(price);
                }
                if (price != -1.0) {
                    TownyMessaging.sendPrefixedTownMessage(resident.getTown(), String.format(TownySettings.getLangString("msg_plot_group_set_for_sale"), group.getGroupName()));
                    if (group.getTown() != resident.getTown()) {
                        TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_plot_group_set_for_sale"), group.getGroupName()));
                    }
                }
                else {
                    final Player p = TownyAPI.getInstance().getPlayer(resident);
                    if (p == null) {
                        throw new TownyException("Player could not be found.");
                    }
                    p.sendMessage(TownySettings.getLangString("msg_plot_set_to_nfs"));
                    TownyUniverse.getInstance().getDataSource().saveTown(group.getTown());
                }
            }
            catch (NotRegisteredException e) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
            }
        }
    }
    
    public void plotToggle(final Player player, final TownBlock townBlock, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatTools.formatTitle("/plot toggle"));
            player.sendMessage(ChatTools.formatCommand("", "/plot toggle", "pvp", ""));
            player.sendMessage(ChatTools.formatCommand("", "/plot toggle", "explosion", ""));
            player.sendMessage(ChatTools.formatCommand("", "/plot toggle", "fire", ""));
            player.sendMessage(ChatTools.formatCommand("", "/plot toggle", "mobs", ""));
        }
        else {
            try {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (split[0].equalsIgnoreCase("pvp")) {
                    this.toggleTest(player, townBlock, StringMgmt.join(split, " "));
                    if (TownySettings.getPVPCoolDownTime() > 0 && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                        if (CooldownTimerTask.hasCooldown(townBlock.getTown().getName(), CooldownTimerTask.CooldownType.PVP)) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(townBlock.getTown().getName(), CooldownTimerTask.CooldownType.PVP)));
                        }
                        if (CooldownTimerTask.hasCooldown(townBlock.getWorldCoord().toString(), CooldownTimerTask.CooldownType.PVP)) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(townBlock.getWorldCoord().toString(), CooldownTimerTask.CooldownType.PVP)));
                        }
                    }
                    townBlock.getPermissions().pvp = !townBlock.getPermissions().pvp;
                    if (TownySettings.getPVPCoolDownTime() > 0 && !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_ADMIN.getNode())) {
                        CooldownTimerTask.addCooldownTimer(townBlock.getWorldCoord().toString(), CooldownTimerTask.CooldownType.PVP);
                    }
                    TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_changed_pvp"), "Plot", townBlock.getPermissions().pvp ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("explosion")) {
                    this.toggleTest(player, townBlock, StringMgmt.join(split, " "));
                    townBlock.getPermissions().explosion = !townBlock.getPermissions().explosion;
                    TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_changed_expl"), "the Plot", townBlock.getPermissions().explosion ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else if (split[0].equalsIgnoreCase("fire")) {
                    this.toggleTest(player, townBlock, StringMgmt.join(split, " "));
                    townBlock.getPermissions().fire = !townBlock.getPermissions().fire;
                    TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_changed_fire"), "the Plot", townBlock.getPermissions().fire ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                else {
                    if (!split[0].equalsIgnoreCase("mobs")) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "plot"));
                        return;
                    }
                    this.toggleTest(player, townBlock, StringMgmt.join(split, " "));
                    townBlock.getPermissions().mobs = !townBlock.getPermissions().mobs;
                    TownyMessaging.sendMessage(player, String.format(TownySettings.getLangString("msg_changed_mobs"), "the Plot", townBlock.getPermissions().mobs ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled")));
                }
                townBlock.setChanged(true);
                final TownBlockSettingsChangedEvent event = new TownBlockSettingsChangedEvent(townBlock);
                Bukkit.getServer().getPluginManager().callEvent((Event)event);
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg(player, e.getMessage());
            }
            townyUniverse.getDataSource().saveTownBlock(townBlock);
        }
    }
    
    public void plotGroupToggle(final Player player, final PlotObjectGroup plotGroup, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatTools.formatTitle("/plot group toggle"));
            player.sendMessage(ChatTools.formatCommand("", "/plot group toggle", "pvp", ""));
            player.sendMessage(ChatTools.formatCommand("", "/plot group toggle", "explosion", ""));
            player.sendMessage(ChatTools.formatCommand("", "/plot group toggle", "fire", ""));
            player.sendMessage(ChatTools.formatCommand("", "/plot group toggle", "mobs", ""));
        }
        else {
            try {
                String endingMessage = "";
                for (final TownBlock groupBlock : plotGroup.getTownBlocks()) {
                    if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE.getNode(split[0].toLowerCase()))) {
                        throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                    }
                    if (split[0].equalsIgnoreCase("pvp")) {
                        this.toggleTest(player, groupBlock, StringMgmt.join(split, " "));
                        if (TownySettings.getPVPCoolDownTime() > 0) {
                            if (CooldownTimerTask.hasCooldown(groupBlock.getTown().getName(), CooldownTimerTask.CooldownType.PVP)) {
                                throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(groupBlock.getTown().getName(), CooldownTimerTask.CooldownType.PVP)));
                            }
                            if (CooldownTimerTask.hasCooldown(groupBlock.getWorldCoord().toString(), CooldownTimerTask.CooldownType.PVP)) {
                                throw new TownyException(String.format(TownySettings.getLangString("msg_err_cannot_toggle_pvp_x_seconds_remaining"), CooldownTimerTask.getCooldownRemaining(groupBlock.getWorldCoord().toString(), CooldownTimerTask.CooldownType.PVP)));
                            }
                        }
                        groupBlock.getPermissions().pvp = !groupBlock.getPermissions().pvp;
                        if (TownySettings.getPVPCoolDownTime() > 0) {
                            CooldownTimerTask.addCooldownTimer(groupBlock.getWorldCoord().toString(), CooldownTimerTask.CooldownType.PVP);
                        }
                        endingMessage = String.format(TownySettings.getLangString("msg_changed_pvp"), "Plot Group", groupBlock.getPermissions().pvp ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                    }
                    else if (split[0].equalsIgnoreCase("explosion")) {
                        this.toggleTest(player, groupBlock, StringMgmt.join(split, " "));
                        groupBlock.getPermissions().explosion = !groupBlock.getPermissions().explosion;
                        endingMessage = String.format(TownySettings.getLangString("msg_changed_fire"), "the Plot Group", groupBlock.getPermissions().fire ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                    }
                    else if (split[0].equalsIgnoreCase("fire")) {
                        this.toggleTest(player, groupBlock, StringMgmt.join(split, " "));
                        groupBlock.getPermissions().fire = !groupBlock.getPermissions().fire;
                        endingMessage = String.format(TownySettings.getLangString("msg_changed_fire"), "the Plot Group", groupBlock.getPermissions().fire ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                    }
                    else {
                        if (!split[0].equalsIgnoreCase("mobs")) {
                            TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "plot"));
                            return;
                        }
                        this.toggleTest(player, groupBlock, StringMgmt.join(split, " "));
                        groupBlock.getPermissions().mobs = !groupBlock.getPermissions().mobs;
                        endingMessage = String.format(TownySettings.getLangString("msg_changed_mobs"), "the Plot Group", groupBlock.getPermissions().mobs ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                    }
                    groupBlock.setChanged(true);
                    final TownBlockSettingsChangedEvent event = new TownBlockSettingsChangedEvent(groupBlock);
                    Bukkit.getServer().getPluginManager().callEvent((Event)event);
                    townyUniverse.getDataSource().saveTownBlock(groupBlock);
                }
                TownyMessaging.sendMessage(player, endingMessage);
            }
            catch (Exception e) {
                TownyMessaging.sendErrorMsg(player, e.getMessage());
            }
        }
    }
    
    private void toggleTest(final Player player, final TownBlock townBlock, String split) throws TownyException {
        final Town town = townBlock.getTown();
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
        if ((split.contains("pvp") || split.trim().equalsIgnoreCase("off")) && townBlock.getType().equals(TownBlockType.ARENA)) {
            throw new TownyException(TownySettings.getLangString("msg_plot_pvp"));
        }
    }
    
    public TownBlockOwner plotTestOwner(final Resident resident, final TownBlock townBlock) throws TownyException {
        final Player player = BukkitTools.getPlayer(resident.getName());
        final boolean isAdmin = TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(player);
        if (townBlock.hasResident()) {
            final Resident owner = townBlock.getResident();
            if (!owner.hasTown() && player.hasPermission(PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode()) && townBlock.getTown() == resident.getTown()) {
                return owner;
            }
            final boolean isSameTown = resident.hasTown() && resident.getTown() == owner.getTown();
            if (resident == owner || (isSameTown && player.hasPermission(PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode())) || (townBlock.getTown() == resident.getTown() && player.hasPermission(PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode())) || isAdmin) {
                return owner;
            }
            throw new TownyException(TownySettings.getLangString("msg_area_not_own"));
        }
        else {
            final Town owner2 = townBlock.getTown();
            final boolean isSameTown = resident.hasTown() && resident.getTown() == owner2;
            if (isSameTown && !BukkitTools.getPlayer(resident.getName()).hasPermission(PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_not_mayor_ass"));
            }
            if (!isSameTown && !isAdmin) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
            }
            return owner2;
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
    
    private boolean handlePlotGroupCommand(final String[] split, final Player player) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
        final String world = player.getWorld().getName();
        final TownBlock townBlock = new WorldCoord(world, Coord.parseCoord((Entity)player)).getTownBlock();
        final Town town = townBlock.getTown();
        this.plotTestOwner(resident, townBlock);
        if (split.length <= 0 || split[0].equalsIgnoreCase("?")) {
            player.sendMessage(ChatTools.formatTitle("/plot group"));
            player.sendMessage(ChatTools.formatCommand("/plot group", "add | new | create", "[name]", "Ex: /plot group new ExpensivePlots"));
            player.sendMessage(ChatTools.formatCommand("/plot group", "remove", "", "Removes a plot from the specified group."));
            player.sendMessage(ChatTools.formatCommand("/plot group", "rename", "[newName]", "Renames the group you are standing in."));
            player.sendMessage(ChatTools.formatCommand("/plot group", "set", "...", "Ex: /plot group set perm resident on."));
            player.sendMessage(ChatTools.formatCommand("/plot group", "toggle", "...", "Ex: /plot group toggle [pvp|fire|mobs]"));
            player.sendMessage(ChatTools.formatCommand("/plot group", "forsale | fs", "[price]", "Ex: /plot group forsale 50"));
            player.sendMessage(ChatTools.formatCommand("/plot group", "notforsale | nfs", "", "Ex: /plot group notforsale"));
            if (townBlock.hasPlotObjectGroup()) {
                TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("status_plot_group_name_and_size"), townBlock.getPlotObjectGroup().getGroupName(), townBlock.getPlotObjectGroup().getTownBlocks().size()));
            }
            return true;
        }
        if (split[0].equalsIgnoreCase("add") || split[0].equalsIgnoreCase("new") || split[0].equalsIgnoreCase("create")) {
            PlotObjectGroup newGroup = null;
            if (townBlock.hasPlotObjectGroup()) {
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_plot_already_belongs_to_a_group"), townBlock.getPlotObjectGroup().getGroupName()));
                return false;
            }
            if (split.length != 2) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_group_name_required"));
                return false;
            }
            final UUID plotGroupID = townyUniverse.generatePlotGroupID();
            final String plotGroupName = split[1];
            newGroup = new PlotObjectGroup(plotGroupID, plotGroupName, town);
            if (town.hasObjectGroupName(newGroup.getGroupName())) {
                newGroup = town.getPlotObjectGroupFromName(newGroup.getGroupName());
            }
            townBlock.setPlotObjectGroup(newGroup);
            if (townBlock.getPlotPrice() >= 0.0) {
                newGroup.addPlotPrice(townBlock.getPlotPrice());
            }
            town.addPlotGroup(newGroup);
            townyUniverse.getDataSource().savePlotGroupList();
            townyUniverse.getDataSource().savePlotGroup(newGroup);
            townyUniverse.getDataSource().saveTownBlock(townBlock);
            townyUniverse.getDataSource().saveTown(town);
            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_plot_was_put_into_group_x"), townBlock.getX(), townBlock.getZ(), newGroup.getGroupName()));
        }
        else if (split[0].equalsIgnoreCase("remove")) {
            if (!townBlock.hasPlotObjectGroup()) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_not_associated_with_a_group"));
                return false;
            }
            final String name = townBlock.getPlotObjectGroup().getGroupName();
            townBlock.getPlotObjectGroup().removeTownBlock(townBlock);
            townBlock.removePlotObjectGroup();
            TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_plot_was_removed_from_group_x"), townBlock.getX(), townBlock.getZ(), name));
        }
        else if (split[0].equalsIgnoreCase("rename")) {
            final String newName = split[1];
            if (!townBlock.hasPlotObjectGroup()) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_not_associated_with_a_group"));
                return false;
            }
            final String oldName = townBlock.getPlotObjectGroup().getGroupName();
            TownyUniverse.getInstance().getDataSource().renameGroup(townBlock.getPlotObjectGroup(), newName);
            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_plot_renamed_from_x_to_y"), oldName, newName));
        }
        else if (split[0].equalsIgnoreCase("forsale") || split[0].equalsIgnoreCase("fs")) {
            final PlotObjectGroup group = townBlock.getPlotObjectGroup();
            if (group == null) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_not_associated_with_a_group"));
                return false;
            }
            if (split.length < 2) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_group_specify_price"));
                return false;
            }
            final int price = Integer.parseInt(split[1]);
            group.setPrice(price);
            TownyUniverse.getInstance().getDataSource().savePlotGroup(group);
            TownyUniverse.getInstance().getDataSource().savePlotGroupList();
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_put_group_up_for_sale"), player.getName(), group.getGroupName(), TownyEconomyHandler.getFormattedBalance(group.getPrice())));
        }
        else if (split[0].equalsIgnoreCase("notforsale") || split[0].equalsIgnoreCase("nfs")) {
            final PlotObjectGroup group = townBlock.getPlotObjectGroup();
            if (group == null) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_not_associated_with_a_group"));
                return false;
            }
            group.setPrice(-1.0);
            TownyUniverse.getInstance().getDataSource().savePlotGroup(group);
            TownyUniverse.getInstance().getDataSource().savePlotGroupList();
            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_made_group_not_for_sale"), player.getName(), group.getGroupName()));
        }
        else if (split[0].equalsIgnoreCase("toggle")) {
            if (townBlock.getPlotObjectGroup() == null) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_not_associated_with_a_group"));
                return false;
            }
            final GroupConfirmation confirmation = new GroupConfirmation(townBlock.getPlotObjectGroup(), player);
            confirmation.setArgs(StringMgmt.remArgs(split, 1));
            ConfirmationHandler.addConfirmation(resident, ConfirmationType.GROUP_TOGGLE_ACTION, confirmation);
            final String firstLine = String.format(TownySettings.getLangString("msg_plot_group_toggle_confirmation"), townBlock.getPlotObjectGroup().getTownBlocks().size()) + " " + TownySettings.getLangString("are_you_sure_you_want_to_continue");
            TownyMessaging.sendConfirmationMessage((CommandSender)player, firstLine, null, null, null);
            return true;
        }
        else {
            if (!split[0].equalsIgnoreCase("set")) {
                player.sendMessage(ChatTools.formatTitle("/plot group"));
                player.sendMessage(ChatTools.formatCommand("/plot group", "add | new | create", "[name]", "Ex: /plot group new ExpensivePlots"));
                player.sendMessage(ChatTools.formatCommand("/plot group", "remove", "", "Removes a plot from the specified group."));
                player.sendMessage(ChatTools.formatCommand("/plot group", "rename", "[newName]", "Renames the group you are standing in."));
                player.sendMessage(ChatTools.formatCommand("/plot group", "set", "...", "Ex: /plot group set perm resident on."));
                player.sendMessage(ChatTools.formatCommand("/plot group", "toggle", "...", "Ex: /plot group toggle [pvp|fire|mobs]"));
                player.sendMessage(ChatTools.formatCommand("/plot group", "forsale | fs", "[price]", "Ex: /plot group forsale 50"));
                player.sendMessage(ChatTools.formatCommand("/plot group", "notforsale | nfs", "", "Ex: /plot group notforsale"));
                if (townBlock.hasPlotObjectGroup()) {
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("status_plot_group_name_and_size"), townBlock.getPlotObjectGroup().getGroupName(), townBlock.getPlotObjectGroup().getTownBlocks().size()));
                }
                return true;
            }
            if (townBlock.getPlotObjectGroup() == null) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_plot_not_associated_with_a_group"));
                return false;
            }
            final TownBlockOwner townBlockOwner = this.plotTestOwner(resident, townBlock);
            if (split.length < 2) {
                player.sendMessage(ChatTools.formatTitle("/plot group set"));
                if (townBlockOwner instanceof Town) {
                    player.sendMessage(ChatTools.formatCommand("Level", "[resident/nation/ally/outsider]", "", ""));
                }
                if (townBlockOwner instanceof Resident) {
                    player.sendMessage(ChatTools.formatCommand("Level", "[friend/town/ally/outsider]", "", ""));
                }
                player.sendMessage(ChatTools.formatCommand("Type", "[build/destroy/switch/itemuse]", "", ""));
                player.sendMessage(ChatTools.formatCommand("/plot group set", "perm", "[on/off]", "Toggle all permissions"));
                player.sendMessage(ChatTools.formatCommand("/plot group set", "perm", "[level/type] [on/off]", ""));
                player.sendMessage(ChatTools.formatCommand("/plot group set", "perm", "[level] [type] [on/off]", ""));
                player.sendMessage(ChatTools.formatCommand("/plot group set", "perm", "reset", ""));
                player.sendMessage(ChatTools.formatCommand("Eg", "/plot group set perm", "friend build on", ""));
                player.sendMessage(ChatTools.formatCommand("/plot group set", "[townblocktype]", "", "Farm, Wilds, Bank, Embassy, etc."));
                return false;
            }
            if (split[1].equalsIgnoreCase("perm")) {
                final GroupConfirmation confirmation2 = new GroupConfirmation(townBlock.getPlotObjectGroup(), player);
                confirmation2.setTownBlockOwner(townBlockOwner);
                confirmation2.setArgs(StringMgmt.remArgs(split, 2));
                ConfirmationHandler.addConfirmation(resident, ConfirmationType.GROUP_SET_PERM_ACTION, confirmation2);
                final String firstLine2 = String.format(TownySettings.getLangString("msg_plot_group_set_perm_confirmation"), townBlock.getPlotObjectGroup().getTownBlocks().size()) + " " + TownySettings.getLangString("are_you_sure_you_want_to_continue");
                TownyMessaging.sendConfirmationMessage((CommandSender)player, firstLine2, null, null, null);
                return true;
            }
            if (split[1].equalsIgnoreCase("jail")) {
                throw new TownyException(TownySettings.getLangString(TownySettings.getLangString("msg_err_cannot_set_group_to_jail")));
            }
            for (final TownBlock tb : townBlock.getPlotObjectGroup().getTownBlocks()) {
                try {
                    this.setPlotType(resident, tb.getWorldCoord(), split[1]);
                }
                catch (Exception e) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_could_not_set_group_type") + e.getMessage());
                    return false;
                }
            }
            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_group_type_to_x"), split[1]));
        }
        return false;
    }
    
    static {
        (output = new ArrayList<String>()).add(ChatTools.formatTitle("/plot"));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/plot claim", "", TownySettings.getLangString("msg_block_claim")));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/plot claim", "[rect/circle] [radius]", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/plot perm", "[hud]", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot notforsale", "", TownySettings.getLangString("msg_plot_nfs")));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot notforsale", "[rect/circle] [radius]", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot forsale [$]", "", TownySettings.getLangString("msg_plot_fs")));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot forsale [$]", "within [rect/circle] [radius]", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot evict", "", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot clear", "", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing") + "/" + TownySettings.getLangString("mayor_sing"), "/plot set ...", "", TownySettings.getLangString("msg_plot_fs")));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/plot toggle", "[pvp/fire/explosion/mobs]", ""));
        PlotCommand.output.add(ChatTools.formatCommand(TownySettings.getLangString("res_sing"), "/plot group", "?", ""));
        PlotCommand.output.add(TownySettings.getLangString("msg_nfs_abr"));
    }
}
