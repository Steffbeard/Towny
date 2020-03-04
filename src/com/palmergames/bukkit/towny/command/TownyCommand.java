// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.util.KeyValue;
import com.palmergames.util.KeyValueTable;
import com.palmergames.bukkit.towny.object.EconomyAccount;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.TownyFormatter;
import java.text.DecimalFormat;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.object.ResidentList;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.war.eventwar.War;
import java.util.Collection;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyAsciiMap;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import java.util.Iterator;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.util.TimeMgmt;
import com.palmergames.bukkit.towny.TownyTimerHandler;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.util.Colors;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.util.ChatTools;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class TownyCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> towny_general_help;
    private static final List<String> towny_help;
    private static final List<String> towny_top;
    private static final List<String> towny_war;
    private static String towny_version;
    
    public TownyCommand(final Towny instance) {
        TownyCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        TownyCommand.towny_version = "§2Towny version: §a" + TownyCommand.plugin.getVersion();
        TownyCommand.towny_war.add(ChatTools.formatTitle("/towny war"));
        TownyCommand.towny_war.add(ChatTools.formatCommand("", "/towny war", "stats", ""));
        TownyCommand.towny_war.add(ChatTools.formatCommand("", "/towny war", "scores", ""));
        TownyCommand.towny_war.add(ChatTools.formatCommand("", "/towny war", "participants [page #]", ""));
        TownyCommand.towny_war.add(ChatTools.formatCommand("", "/towny war", "hud", ""));
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            this.parseTownyCommand(player, args);
        }
        else if (args.length == 0) {
            for (final String line : TownyCommand.towny_general_help) {
                sender.sendMessage(Colors.strip(line));
            }
        }
        else if (args[0].equalsIgnoreCase("tree")) {
            for (final String line : TownyUniverse.getInstance().getTreeString(0)) {
                sender.sendMessage(line);
            }
        }
        else if (args[0].equalsIgnoreCase("time")) {
            TownyMessaging.sendMsg(TownySettings.getLangString("msg_time_until_a_new_day") + TimeMgmt.formatCountdownTime(TownyTimerHandler.townyTime()));
        }
        else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
            sender.sendMessage(Colors.strip(TownyCommand.towny_version));
        }
        else if (args[0].equalsIgnoreCase("war")) {
            final boolean war = this.TownyWar(StringMgmt.remFirstArg(args), null);
            if (war) {
                for (final String line2 : TownyCommand.towny_war) {
                    sender.sendMessage(Colors.strip(line2));
                }
            }
            else {
                sender.sendMessage("The world isn't currently at war.");
            }
            TownyCommand.towny_war.clear();
        }
        else if (args[0].equalsIgnoreCase("universe")) {
            for (final String line : this.getUniverseStats()) {
                sender.sendMessage(Colors.strip(line));
            }
        }
        return true;
    }
    
    private void parseTownyCommand(final Player player, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            for (final String line : TownyCommand.towny_general_help) {
                player.sendMessage(line);
            }
            return;
        }
        if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
            for (final String line : TownyCommand.towny_help) {
                player.sendMessage(Colors.strip(line));
            }
            return;
        }
        try {
            if (split[0].equalsIgnoreCase("map")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_MAP.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                if (split.length > 1 && split[1].equalsIgnoreCase("big")) {
                    TownyAsciiMap.generateAndSend(TownyCommand.plugin, player, 18);
                }
                else {
                    showMap(player);
                }
            }
            else if (split[0].equalsIgnoreCase("prices")) {
                Town town = null;
                Label_0287: {
                    if (split.length > 1) {
                        try {
                            town = townyUniverse.getDataSource().getTown(split[1]);
                            break Label_0287;
                        }
                        catch (NotRegisteredException x) {
                            this.sendErrorMsg((CommandSender)player, x.getMessage());
                            return;
                        }
                    }
                    if (split.length == 1) {
                        try {
                            final Resident resident = townyUniverse.getDataSource().getResident(player.getName());
                            town = resident.getTown();
                        }
                        catch (NotRegisteredException ex) {}
                    }
                }
                for (final String line2 : this.getTownyPrices(town)) {
                    player.sendMessage(line2);
                }
            }
            else if (split[0].equalsIgnoreCase("top")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_TOP.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.TopCommand(player, StringMgmt.remFirstArg(split));
            }
            else if (split[0].equalsIgnoreCase("tree")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_TREE.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.consoleUseOnly(player);
            }
            else if (split[0].equalsIgnoreCase("time")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_TIME.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_time_until_a_new_day") + TimeMgmt.formatCountdownTime(TownyTimerHandler.townyTime()));
            }
            else if (split[0].equalsIgnoreCase("universe")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_UNIVERSE.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                for (final String line : this.getUniverseStats()) {
                    player.sendMessage(line);
                }
            }
            else if (split[0].equalsIgnoreCase("version") || split[0].equalsIgnoreCase("v")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_VERSION.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                player.sendMessage(TownyCommand.towny_version);
            }
            else if (split[0].equalsIgnoreCase("war")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNY_WAR.getNode(split[0].toLowerCase()))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                final boolean war = this.TownyWar(StringMgmt.remFirstArg(split), player);
                if (war) {
                    for (final String line2 : TownyCommand.towny_war) {
                        player.sendMessage(Colors.strip(line2));
                    }
                }
                else {
                    this.sendErrorMsg((CommandSender)player, "The world isn't currently at war.");
                }
                TownyCommand.towny_war.clear();
            }
            else if (split[0].equalsIgnoreCase("spy")) {
                if (townyUniverse.getPermissionSource().has(player, PermissionNodes.TOWNY_CHAT_SPY.getNode())) {
                    final Resident resident2 = townyUniverse.getDataSource().getResident(player.getName());
                    resident2.toggleMode(split, true);
                }
                else {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_command_disable"));
                }
            }
            else {
                this.sendErrorMsg((CommandSender)player, "Invalid sub command.");
            }
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    private boolean TownyWar(final String[] args, final Player p) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (TownyAPI.getInstance().isWarTime() && args.length > 0) {
            TownyCommand.towny_war.clear();
            if (args[0].equalsIgnoreCase("stats")) {
                TownyCommand.towny_war.addAll(townyUniverse.getWarEvent().getStats());
            }
            else if (args[0].equalsIgnoreCase("scores")) {
                TownyCommand.towny_war.addAll(townyUniverse.getWarEvent().getScores(-1));
            }
            else {
                if (args[0].equalsIgnoreCase("participants")) {
                    try {
                        this.parseWarParticipants(p, args);
                    }
                    catch (NotRegisteredException ex) {}
                    return true;
                }
                if (args[0].equalsIgnoreCase("hud") && p == null) {
                    TownyCommand.towny_war.add("No hud for console!");
                }
                else if (args[0].equalsIgnoreCase("hud") && p != null) {
                    if (townyUniverse.getPermissionSource().has(p, PermissionNodes.TOWNY_COMMAND_TOWNY_WAR_HUD.getNode())) {
                        TownyCommand.plugin.getHUDManager().toggleWarHUD(p);
                    }
                    else {
                        TownyMessaging.sendErrorMsg(p, TownySettings.getLangString("msg_err_command_disable"));
                    }
                }
            }
        }
        return TownyAPI.getInstance().isWarTime();
    }
    
    private void parseWarParticipants(final Player player, final String[] split) throws NotRegisteredException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Town> townsToSort = War.warringTowns;
        final List<Nation> nationsToSort = War.warringNations;
        int page = 1;
        final List<String> output = new ArrayList<String>();
        for (final Nation nations : nationsToSort) {
            String nationLine = "§6-" + nations.getName();
            if (townyUniverse.getDataSource().getResident(player.getName()).hasNation()) {
                if (townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation().hasEnemy(nations)) {
                    nationLine += "§4 (Enemy)";
                }
                else if (townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation().hasAlly(nations)) {
                    nationLine += "§2 (Ally)";
                }
            }
            output.add(nationLine);
            for (final Town towns : townsToSort) {
                if (towns.getNation().equals(nations)) {
                    String townLine = "§3  -" + towns.getName();
                    if (towns.isCapital()) {
                        townLine += "§b (Capital)";
                    }
                    output.add(townLine);
                }
            }
        }
        final int total = (int)Math.ceil(output.size() / 10.0);
        if (split.length > 1) {
            try {
                page = Integer.parseInt(split[1]);
                if (page < 0) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_negative"));
                    return;
                }
                if (page == 0) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                    return;
                }
            }
            catch (NumberFormatException e) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_error_must_be_int"));
                return;
            }
        }
        if (page > total) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getListNotEnoughPagesMsg(total));
            return;
        }
        int iMax = page * 10;
        if (page * 10 > output.size()) {
            iMax = output.size();
        }
        final List<String> warparticipantsformatted = new ArrayList<String>();
        for (int i = (page - 1) * 10; i < iMax; ++i) {
            final String line = output.get(i);
            warparticipantsformatted.add(line);
        }
        player.sendMessage(ChatTools.formatList("War Participants", "§6Nation Name§8 - §3Town Names", warparticipantsformatted, TownySettings.getListPageMsg(page, total)));
        output.clear();
    }
    
    private void TopCommand(final Player player, final String[] args) {
        final TownyUniverse universe = TownyUniverse.getInstance();
        if (args.length == 0 || args[0].equalsIgnoreCase("?")) {
            TownyCommand.towny_top.add(ChatTools.formatTitle("/towny top"));
            TownyCommand.towny_top.add(ChatTools.formatCommand("", "/towny top", "residents [all/town/nation]", ""));
            TownyCommand.towny_top.add(ChatTools.formatCommand("", "/towny top", "land [all/resident/town]", ""));
        }
        else if (args[0].equalsIgnoreCase("residents")) {
            if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
                final List<ResidentList> list = new ArrayList<ResidentList>(universe.getDataSource().getTowns());
                list.addAll(universe.getDataSource().getNations());
                TownyCommand.towny_top.add(ChatTools.formatTitle("Most Residents"));
                TownyCommand.towny_top.addAll(this.getMostResidents(list, 10));
            }
            else if (args[1].equalsIgnoreCase("town")) {
                TownyCommand.towny_top.add(ChatTools.formatTitle("Most Residents in a Town"));
                TownyCommand.towny_top.addAll(this.getMostResidents(new ArrayList<ResidentList>(universe.getDataSource().getTowns()), 10));
            }
            else if (args[1].equalsIgnoreCase("nation")) {
                TownyCommand.towny_top.add(ChatTools.formatTitle("Most Residents in a Nation"));
                TownyCommand.towny_top.addAll(this.getMostResidents(new ArrayList<ResidentList>(universe.getDataSource().getNations()), 10));
            }
            else {
                this.sendErrorMsg((CommandSender)player, "Invalid sub command.");
            }
        }
        else if (args[0].equalsIgnoreCase("land")) {
            if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
                final List<TownBlockOwner> list2 = new ArrayList<TownBlockOwner>(universe.getDataSource().getResidents());
                list2.addAll(universe.getDataSource().getTowns());
                TownyCommand.towny_top.add(ChatTools.formatTitle("Most Land Owned"));
                TownyCommand.towny_top.addAll(this.getMostLand(list2, 10));
            }
            else if (args[1].equalsIgnoreCase("resident")) {
                TownyCommand.towny_top.add(ChatTools.formatTitle("Most Land Owned by Resident"));
                TownyCommand.towny_top.addAll(this.getMostLand(new ArrayList<TownBlockOwner>(universe.getDataSource().getResidents()), 10));
            }
            else if (args[1].equalsIgnoreCase("town")) {
                TownyCommand.towny_top.add(ChatTools.formatTitle("Most Land Owned by Town"));
                TownyCommand.towny_top.addAll(this.getMostLand(new ArrayList<TownBlockOwner>(universe.getDataSource().getTowns()), 10));
            }
            else {
                this.sendErrorMsg((CommandSender)player, "Invalid sub command.");
            }
        }
        else {
            this.sendErrorMsg((CommandSender)player, "Invalid sub command.");
        }
        for (final String line : TownyCommand.towny_top) {
            player.sendMessage(line);
        }
        TownyCommand.towny_top.clear();
    }
    
    public List<String> getUniverseStats() {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<String> output = new ArrayList<String>();
        output.add("§0-§4###§0---§4###§0-");
        output.add("§4#§c###§4#§0-§4#§c###§4#§0   §6[§eTowny " + TownyCommand.plugin.getVersion() + "§6]");
        output.add("§4#§c####§4#§c####§4#   §3By: §bChris H (Shade)/ElgarL/LlmDl");
        output.add("§0-§4#§c#######§4#§0-");
        output.add("§0--§4##§c###§4##§0--   §3Residents: §b" + townyUniverse.getDataSource().getResidents().size() + "§8" + " | §3Towns: §b" + townyUniverse.getDataSource().getTowns().size() + "§8" + " | §3Nations: §b" + townyUniverse.getDataSource().getNations().size());
        output.add("§0----§4#§c#§4#§0----   §3Worlds: §b" + townyUniverse.getDataSource().getWorlds().size() + "§8" + " | §3TownBlocks: §b" + townyUniverse.getDataSource().getAllTownBlocks().size());
        output.add("§0-----§4#§0----- ");
        final Plugin test = Bukkit.getServer().getPluginManager().getPlugin("TownyChat");
        if (test != null) {
            output.add("§0-----------   §6[§eTownyChat " + BukkitTools.getPluginManager().getPlugin("TownyChat").getDescription().getVersion() + "§6]");
        }
        return output;
    }
    
    public static void showMap(final Player player) {
        TownyAsciiMap.generateAndSend(TownyCommand.plugin, player, 7);
    }
    
    public List<String> getTownyPrices(final Town town) {
        final List<String> output = new ArrayList<String>();
        Nation nation = null;
        if (town != null && town.hasNation()) {
            try {
                nation = town.getNation();
            }
            catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
        output.add(ChatTools.formatTitle("Prices"));
        output.add("§e[New] §2Town: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getNewTownPrice()) + "§8" + " | " + "§2" + "Nation: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getNewNationPrice()));
        if (town != null) {
            output.add("§e[Upkeep] §2Town: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getTownUpkeepCost(town)) + "§8" + " | " + "§2" + "Nation: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getNationUpkeepCost(nation)));
            if (town.isOverClaimed() && TownySettings.getUpkeepPenalty() > 0.0) {
                output.add("§e[Overclaimed Upkeep] §2Town: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getTownPenaltyUpkeepCost(town)));
            }
            output.add("§e[Claiming] §2TownBlock: §a" + TownyEconomyHandler.getFormattedBalance(town.getTownBlockCost()) + "§8" + (Double.valueOf(TownySettings.getClaimPriceIncreaseValue()).equals(1.0) ? "" : (" | §2Increase per TownBlock: §a+" + new DecimalFormat("##.##%").format(TownySettings.getClaimPriceIncreaseValue() - 1.0))));
            output.add("§e[Claiming] §2Outposts: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getOutpostCost()));
        }
        if (town == null) {
            output.add("§e[Upkeep] §2Town: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getTownUpkeep()) + "§8" + " | " + "§2" + "Nation: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getNationUpkeep()));
        }
        output.add("§8Town upkeep is based on §a the " + (TownySettings.isUpkeepByPlot() ? " number of plots" : " town level (num residents)."));
        if (TownySettings.getUpkeepPenalty() > 0.0) {
            output.add("§8Overclaimed upkeep is based on §a" + (TownySettings.isUpkeepPenaltyByPlot() ? ("the number of plots overclaimed * " + TownySettings.getUpkeepPenalty()) : ("a flat cost of " + TownySettings.getUpkeepPenalty())));
        }
        if (town != null) {
            output.add("§eTown [" + TownyFormatter.getFormattedName(town) + "]");
            output.add("§c    [Price] §2Plot: §a" + TownyEconomyHandler.getFormattedBalance(town.getPlotPrice()) + "§8" + " | " + "§2" + "Outpost: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getOutpostCost()));
            output.add("§c             §2Shop: §a" + TownyEconomyHandler.getFormattedBalance(town.getCommercialPlotPrice()) + "§8" + " | " + "§2" + "Embassy: " + "§a" + TownyEconomyHandler.getFormattedBalance(town.getEmbassyPlotPrice()));
            output.add("§c    [Taxes] §2Resident: §a" + (town.isTaxPercentage() ? (town.getTaxes() + "%") : TownyEconomyHandler.getFormattedBalance(town.getTaxes())) + "§8" + " | " + "§2" + "Plot: " + "§a" + TownyEconomyHandler.getFormattedBalance(town.getPlotTax()));
            output.add("§c              §2Shop: §a" + TownyEconomyHandler.getFormattedBalance(town.getCommercialPlotTax()) + "§8" + " | " + "§2" + "Embassy: " + "§a" + TownyEconomyHandler.getFormattedBalance(town.getEmbassyPlotTax()));
            output.add("§c    [Setting Plots] §2Shop: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetCommercialCost()) + "§8" + " | " + "§2" + "Embassy: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetEmbassyCost()) + "§8" + " | " + "§2" + "Wilds: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetWildsCost()));
            output.add("§c                      §2Inn: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetInnCost()) + "§8" + " | " + "§2" + "Jail: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetJailCost()) + "§8" + " | " + "§2" + "Farm: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetFarmCost()));
            output.add("§c                      §2Bank: §a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getPlotSetBankCost()));
            if (nation != null) {
                output.add("§eNation [" + TownyFormatter.getFormattedName(nation) + "]");
                output.add("§c    [Taxes] §2Town: §a" + nation.getTaxes() + "§8" + " | " + "§2" + "Peace: " + "§a" + TownyEconomyHandler.getFormattedBalance(TownySettings.getNationNeutralityCost()));
            }
        }
        return output;
    }
    
    public List<String> getTopBankBalance(final List<EconomyAccount> list, final int maxListing) throws EconomyException {
        final List<String> output = new ArrayList<String>();
        final KeyValueTable<EconomyAccount, Double> kvTable = new KeyValueTable<EconomyAccount, Double>();
        for (final EconomyAccount obj : list) {
            kvTable.put(obj, obj.getHoldingBalance());
        }
        kvTable.sortByValue();
        kvTable.reverse();
        int n = 0;
        for (final KeyValue<EconomyAccount, Double> kv : kvTable.getKeyValues()) {
            ++n;
            if (maxListing != -1 && n > maxListing) {
                break;
            }
            final EconomyAccount town = kv.key;
            output.add(String.format("§7%-20s §6|§3 %s", TownyFormatter.getFormattedName(town), TownyEconomyHandler.getFormattedBalance(kv.value)));
        }
        return output;
    }
    
    public List<String> getMostResidents(final List<ResidentList> list, final int maxListing) {
        final List<String> output = new ArrayList<String>();
        final KeyValueTable<ResidentList, Integer> kvTable = new KeyValueTable<ResidentList, Integer>();
        for (final ResidentList obj : list) {
            kvTable.put(obj, obj.getResidents().size());
        }
        kvTable.sortByValue();
        kvTable.reverse();
        int n = 0;
        for (final KeyValue<ResidentList, Integer> kv : kvTable.getKeyValues()) {
            ++n;
            if (maxListing != -1 && n > maxListing) {
                break;
            }
            final ResidentList residentList = kv.key;
            output.add(String.format("§3%30s §6|§7 %10d", TownyFormatter.getFormattedName((TownyObject)residentList), kv.value));
        }
        return output;
    }
    
    public List<String> getMostLand(final List<TownBlockOwner> list, final int maxListing) {
        final List<String> output = new ArrayList<String>();
        final KeyValueTable<TownBlockOwner, Integer> kvTable = new KeyValueTable<TownBlockOwner, Integer>();
        for (final TownBlockOwner obj : list) {
            kvTable.put(obj, obj.getTownBlocks().size());
        }
        kvTable.sortByValue();
        kvTable.reverse();
        int n = 0;
        for (final KeyValue<TownBlockOwner, Integer> kv : kvTable.getKeyValues()) {
            ++n;
            if (maxListing != -1 && n > maxListing) {
                break;
            }
            final Town town = (Town)kv.key;
            output.add(String.format("§3%30s §6|§7 %10d", TownyFormatter.getFormattedName(town), kv.value));
        }
        return output;
    }
    
    public void consoleUseOnly(final Player player) {
        TownyMessaging.sendErrorMsg(player, "This command was designed for use in the console only.");
    }
    
    public void inGameUseOnly(final CommandSender sender) {
        sender.sendMessage("[Towny] InputError: This command was designed for use in game only.");
    }
    
    public boolean sendErrorMsg(final CommandSender sender, final String msg) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            TownyMessaging.sendErrorMsg(player, msg);
        }
        else {
            sender.sendMessage("[Towny] ConsoleError: " + msg);
        }
        return false;
    }
    
    static {
        towny_general_help = new ArrayList<String>();
        towny_help = new ArrayList<String>();
        towny_top = new ArrayList<String>();
        towny_war = new ArrayList<String>();
        TownyCommand.towny_general_help.add(ChatTools.formatTitle(TownySettings.getLangString("help_0")));
        TownyCommand.towny_general_help.add(TownySettings.getLangString("help_1"));
        TownyCommand.towny_general_help.add(ChatTools.formatCommand("", "/resident", "?", "") + ", " + ChatTools.formatCommand("", "/town", "?", "") + ", " + ChatTools.formatCommand("", "/nation", "?", "") + ", " + ChatTools.formatCommand("", "/plot", "?", "") + ", " + ChatTools.formatCommand("", "/towny", "?", ""));
        TownyCommand.towny_general_help.add(ChatTools.formatCommand("", "/tc", "[msg]", TownySettings.getLangString("help_2")) + ", " + ChatTools.formatCommand("", "/nc", "[msg]", TownySettings.getLangString("help_3")).trim());
        TownyCommand.towny_general_help.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyadmin", "?", ""));
        TownyCommand.towny_help.add(ChatTools.formatTitle("/towny"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "", "General help for Towny"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "map", "Displays a map of the nearby townblocks"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "prices", "Display the prices used with Economy"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "top", "Display highscores"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "time", "Display time until a new day"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "universe", "Displays stats"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "v", "Displays the version of Towny"));
        TownyCommand.towny_help.add(ChatTools.formatCommand("", "/towny", "war", "'/towny war' for more info"));
    }
}
