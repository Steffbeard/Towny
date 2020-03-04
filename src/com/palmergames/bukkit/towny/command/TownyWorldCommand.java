// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import java.util.Collection;
import org.bukkit.Material;
import java.util.Arrays;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.event.TownBlockSettingsChangedEvent;
import java.util.HashMap;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.towny.TownyFormatter;
import java.util.Iterator;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.util.ChatTools;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.command.CommandExecutor;

public class TownyWorldCommand extends BaseCommand implements CommandExecutor
{
    private static Towny plugin;
    private static final List<String> townyworld_help;
    private static final List<String> townyworld_help_console;
    private static final List<String> townyworld_set;
    private static final List<String> townyworld_set_console;
    private static TownyWorld Globalworld;
    private boolean isConsole;
    
    public TownyWorldCommand(final Towny instance) {
        this.isConsole = false;
        TownyWorldCommand.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        TownyWorldCommand.townyworld_help.add(ChatTools.formatTitle("/townyworld"));
        TownyWorldCommand.townyworld_help.add(ChatTools.formatCommand("", "/townyworld", "", TownySettings.getLangString("world_help_1")));
        TownyWorldCommand.townyworld_help.add(ChatTools.formatCommand("", "/townyworld", TownySettings.getLangString("world_help_2"), TownySettings.getLangString("world_help_3")));
        TownyWorldCommand.townyworld_help.add(ChatTools.formatCommand("", "/townyworld", "list", TownySettings.getLangString("world_help_4")));
        TownyWorldCommand.townyworld_help.add(ChatTools.formatCommand("", "/townyworld", "toggle", ""));
        TownyWorldCommand.townyworld_help.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyworld", "set [] .. []", ""));
        TownyWorldCommand.townyworld_help.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyworld", "regen", TownySettings.getLangString("world_help_5")));
        TownyWorldCommand.townyworld_set.add(ChatTools.formatTitle("/townyworld set"));
        TownyWorldCommand.townyworld_set.add(ChatTools.formatCommand("", "/townyworld set", "wildname [name]", ""));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatTitle("/townyworld"));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatCommand("", "/townyworld {world}", "", TownySettings.getLangString("world_help_1")));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatCommand("", "/townyworld {world}", TownySettings.getLangString("world_help_2"), TownySettings.getLangString("world_help_3")));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatCommand("", "/townyworld {world}", "list", TownySettings.getLangString("world_help_4")));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatCommand("", "/townyworld {world}", "toggle", ""));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyworld {world}", "set [] .. []", ""));
        TownyWorldCommand.townyworld_help_console.add(ChatTools.formatCommand(TownySettings.getLangString("admin_sing"), "/townyworld {world}", "regen", TownySettings.getLangString("world_help_5")));
        TownyWorldCommand.townyworld_set_console.add(ChatTools.formatTitle("/townyworld set"));
        TownyWorldCommand.townyworld_set_console.add(ChatTools.formatCommand("", "/townyworld {world} set", "wildname [name]", ""));
        if (sender instanceof Player) {
            this.parseWorldCommand(sender, args);
        }
        else {
            this.isConsole = true;
            this.parseWorldFromConsole(sender, args);
        }
        TownyWorldCommand.townyworld_set_console.clear();
        TownyWorldCommand.townyworld_help_console.clear();
        TownyWorldCommand.townyworld_set.clear();
        TownyWorldCommand.townyworld_help.clear();
        TownyWorldCommand.Globalworld = null;
        return true;
    }
    
    private void parseWorldFromConsole(final CommandSender sender, String[] split) {
        final Player player = null;
        if (split.length == 0 || split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
            for (final String line : TownyWorldCommand.townyworld_help_console) {
                sender.sendMessage(line);
            }
            return;
        }
        if (split[0].equalsIgnoreCase("list")) {
            this.listWorlds(player, sender);
            return;
        }
        if (split[0].equalsIgnoreCase("regen") || split[0].equalsIgnoreCase("undo") || split[0].equalsIgnoreCase("set") || split[0].equalsIgnoreCase("toggle")) {
            for (final String line : TownyWorldCommand.townyworld_help_console) {
                sender.sendMessage(line);
            }
        }
        else {
            try {
                TownyWorldCommand.Globalworld = TownyUniverse.getInstance().getDataSource().getWorld(split[0].toLowerCase());
            }
            catch (NotRegisteredException e) {
                TownyMessaging.sendErrorMsg(sender, TownySettings.getLangString("msg_area_not_recog"));
                return;
            }
            split = StringMgmt.remFirstArg(split);
            this.parseWorldCommand(sender, split);
        }
    }
    
    public void parseWorldCommand(final CommandSender sender, final String[] split) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
            try {
                if (TownyWorldCommand.Globalworld == null) {
                    TownyWorldCommand.Globalworld = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
                }
            }
            catch (NotRegisteredException e2) {
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_area_not_recog"));
                return;
            }
        }
        if (split.length == 0) {
            if (player == null) {
                for (final String line : TownyFormatter.getStatus(TownyWorldCommand.Globalworld)) {
                    sender.sendMessage(Colors.strip(line));
                }
            }
            else {
                TownyMessaging.sendMessage(player, TownyFormatter.getStatus(TownyWorldCommand.Globalworld));
            }
            return;
        }
        try {
            if (split[0].equalsIgnoreCase("?")) {
                if (player == null) {
                    for (final String line : TownyWorldCommand.townyworld_help) {
                        sender.sendMessage(line);
                    }
                }
                else {
                    for (final String line : TownyWorldCommand.townyworld_help) {
                        player.sendMessage(line);
                    }
                }
            }
            else if (split[0].equalsIgnoreCase("list")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_LIST.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.listWorlds(player, sender);
            }
            else if (split[0].equalsIgnoreCase("set")) {
                if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_SET.getNode())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
                }
                this.worldSet(player, sender, StringMgmt.remFirstArg(split));
            }
            else if (split[0].equalsIgnoreCase("toggle")) {
                this.worldToggle(player, sender, StringMgmt.remFirstArg(split));
            }
            else if (split[0].equalsIgnoreCase("regen")) {
                TownyMessaging.sendErrorMsg(player, "This command has been removed for 1.13 compatibility, look for its return in the future.");
            }
        }
        catch (TownyException e) {
            TownyMessaging.sendErrorMsg(player, e.getMessage());
        }
    }
    
    public void listWorlds(final Player player, final CommandSender sender) {
        if (player == null) {
            sender.sendMessage(ChatTools.formatTitle(TownySettings.getLangString("world_plu")));
        }
        else {
            player.sendMessage(ChatTools.formatTitle(TownySettings.getLangString("world_plu")));
        }
        final ArrayList<String> formatedList = new ArrayList<String>();
        final HashMap<String, Integer> playersPerWorld = BukkitTools.getPlayersPerWorld();
        for (final TownyWorld world : TownyUniverse.getInstance().getDataSource().getWorlds()) {
            final int numPlayers = playersPerWorld.getOrDefault(world.getName(), 0);
            formatedList.add("§b" + world.getName() + "§3" + " [" + numPlayers + "]" + "§f");
        }
        if (player == null) {
            for (final String line : ChatTools.list(formatedList)) {
                sender.sendMessage(line);
            }
        }
        else {
            for (final String line : ChatTools.list(formatedList)) {
                player.sendMessage(line);
            }
        }
    }
    
    public void worldToggle(final Player player, final CommandSender sender, final String[] split) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (split.length == 0) {
            if (!this.isConsole) {
                player.sendMessage(ChatTools.formatTitle("/TownyWorld toggle"));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "claimable", ""));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "usingtowny", ""));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "pvp/forcepvp", ""));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "explosion/forceexplosion", ""));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "fire/forcefire", ""));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "townmobs/worldmobs", ""));
                player.sendMessage(ChatTools.formatCommand("", "/TownyWorld toggle", "revertunclaim/revertexpl", ""));
            }
            else {
                sender.sendMessage(ChatTools.formatTitle("/TownyWorld toggle"));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "claimable", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "usingtowny", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "warallowed", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "pvp/forcepvp", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "explosion/forceexplosion", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "fire/forcefire", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "townmobs/worldmobs", ""));
                sender.sendMessage(ChatTools.formatCommand("", "/TownyWorld {world} toggle", "revertunclaim/revertexpl", ""));
            }
        }
        else {
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE.getNode(split[0].toLowerCase()))) {
                throw new TownyException(TownySettings.getLangString("msg_err_command_disable"));
            }
            if (split[0].equalsIgnoreCase("claimable")) {
                TownyWorldCommand.Globalworld.setClaimable(!TownyWorldCommand.Globalworld.isClaimable());
                final String msg = String.format(TownySettings.getLangString("msg_set_claim"), TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isClaimable() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("usingtowny")) {
                TownyWorldCommand.Globalworld.setUsingTowny(!TownyWorldCommand.Globalworld.isUsingTowny());
                TownyWorldCommand.plugin.resetCache();
                final String msg = String.format(TownyWorldCommand.Globalworld.isUsingTowny() ? TownySettings.getLangString("msg_set_use_towny_on") : TownySettings.getLangString("msg_set_use_towny_off"), new Object[0]);
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("warallowed")) {
                TownyWorldCommand.Globalworld.setWarAllowed(!TownyWorldCommand.Globalworld.isWarAllowed());
                TownyWorldCommand.plugin.resetCache();
                final String msg = String.format(TownyWorldCommand.Globalworld.isWarAllowed() ? TownySettings.getLangString("msg_set_war_allowed_on") : TownySettings.getLangString("msg_set_war_allowed_off"), new Object[0]);
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("pvp")) {
                TownyWorldCommand.Globalworld.setPVP(!TownyWorldCommand.Globalworld.isPVP());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Global PVP", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isPVP() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("forcepvp")) {
                TownyWorldCommand.Globalworld.setForcePVP(!TownyWorldCommand.Globalworld.isForcePVP());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Force town PVP", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isForcePVP() ? TownySettings.getLangString("forced") : TownySettings.getLangString("adjustable"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("explosion")) {
                TownyWorldCommand.Globalworld.setExpl(!TownyWorldCommand.Globalworld.isExpl());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Explosions", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isExpl() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("forceexplosion")) {
                TownyWorldCommand.Globalworld.setForceExpl(!TownyWorldCommand.Globalworld.isForceExpl());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Force town Explosions", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isForceExpl() ? TownySettings.getLangString("forced") : TownySettings.getLangString("adjustable"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("fire")) {
                TownyWorldCommand.Globalworld.setFire(!TownyWorldCommand.Globalworld.isFire());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Fire Spread", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isFire() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("forcefire")) {
                TownyWorldCommand.Globalworld.setForceFire(!TownyWorldCommand.Globalworld.isForceFire());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Force town Fire Spread", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isForceFire() ? TownySettings.getLangString("forced") : TownySettings.getLangString("adjustable"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("townmobs")) {
                TownyWorldCommand.Globalworld.setForceTownMobs(!TownyWorldCommand.Globalworld.isForceTownMobs());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Town Mob spawns", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isForceTownMobs() ? TownySettings.getLangString("forced") : TownySettings.getLangString("adjustable"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("worldmobs")) {
                TownyWorldCommand.Globalworld.setWorldMobs(!TownyWorldCommand.Globalworld.hasWorldMobs());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "World Mob spawns", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.hasWorldMobs() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else if (split[0].equalsIgnoreCase("revertunclaim")) {
                TownyWorldCommand.Globalworld.setUsingPlotManagementRevert(!TownyWorldCommand.Globalworld.isUsingPlotManagementRevert());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Unclaim Revert", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isUsingPlotManagementRevert() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            else {
                if (!split[0].equalsIgnoreCase("revertexpl")) {
                    final String msg = String.format(TownySettings.getLangString("msg_err_invalid_property"), "'" + split[0] + "'");
                    if (player != null) {
                        TownyMessaging.sendErrorMsg(player, msg);
                    }
                    else {
                        TownyMessaging.sendErrorMsg(msg);
                    }
                    return;
                }
                TownyWorldCommand.Globalworld.setUsingPlotManagementWildRevert(!TownyWorldCommand.Globalworld.isUsingPlotManagementWildRevert());
                final String msg = String.format(TownySettings.getLangString("msg_changed_world_setting"), "Wilderness Explosion Revert", TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.isUsingPlotManagementWildRevert() ? TownySettings.getLangString("enabled") : TownySettings.getLangString("disabled"));
                if (player != null) {
                    TownyMessaging.sendMsg(player, msg);
                }
                else {
                    TownyMessaging.sendMsg(msg);
                }
            }
            townyUniverse.getDataSource().saveWorld(TownyWorldCommand.Globalworld);
            final TownBlockSettingsChangedEvent event = new TownBlockSettingsChangedEvent(TownyWorldCommand.Globalworld);
            Bukkit.getServer().getPluginManager().callEvent((Event)event);
        }
    }
    
    public void worldSet(final Player player, final CommandSender sender, final String[] split) {
        if (split.length == 0) {
            if (player == null) {
                for (final String line : TownyWorldCommand.townyworld_set) {
                    sender.sendMessage(line);
                }
            }
            else {
                for (final String line : TownyWorldCommand.townyworld_set) {
                    player.sendMessage(line);
                }
            }
        }
        else {
            if (split[0].equalsIgnoreCase("usedefault")) {
                TownyWorldCommand.Globalworld.setUsingDefault();
                TownyWorldCommand.plugin.resetCache();
                if (player != null) {
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_usedefault"), TownyWorldCommand.Globalworld.getName()));
                }
                else {
                    sender.sendMessage(String.format(TownySettings.getLangString("msg_usedefault"), TownyWorldCommand.Globalworld.getName()));
                }
            }
            else if (split[0].equalsIgnoreCase("wildperm")) {
                if (split.length < 2) {
                    TownyWorldCommand.Globalworld.setUsingDefault();
                    if (player != null) {
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_usedefault"), TownyWorldCommand.Globalworld.getName()));
                    }
                    else {
                        sender.sendMessage(String.format(TownySettings.getLangString("msg_usedefault"), TownyWorldCommand.Globalworld.getName()));
                    }
                }
                else {
                    try {
                        final List<String> perms = Arrays.asList(StringMgmt.remFirstArg(split));
                        TownyWorldCommand.Globalworld.setUnclaimedZoneBuild(perms.contains("build"));
                        TownyWorldCommand.Globalworld.setUnclaimedZoneDestroy(perms.contains("destroy"));
                        TownyWorldCommand.Globalworld.setUnclaimedZoneSwitch(perms.contains("switch"));
                        TownyWorldCommand.Globalworld.setUnclaimedZoneItemUse(perms.contains("itemuse"));
                        TownyWorldCommand.plugin.resetCache();
                        if (player != null) {
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_wild_perms"), TownyWorldCommand.Globalworld.getName(), perms.toString()));
                        }
                        else {
                            sender.sendMessage(String.format(TownySettings.getLangString("msg_set_wild_perms"), TownyWorldCommand.Globalworld.getName(), perms.toString()));
                        }
                    }
                    catch (Exception e) {
                        if (player != null) {
                            TownyMessaging.sendErrorMsg(player, "Eg: /townyworld set wildperm build destroy");
                        }
                        else {
                            sender.sendMessage("Eg: /townyworld set wildperm build destroy <world>");
                        }
                    }
                }
            }
            else if (split[0].equalsIgnoreCase("wildignore")) {
                if (split.length < 2) {
                    if (player != null) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /townyworld set wildignore SAPLING,GOLD_ORE,IRON_ORE");
                    }
                    else {
                        sender.sendMessage("Eg: /townyworld set wildignore SAPLING,GOLD_ORE,IRON_ORE <world>");
                    }
                }
                else {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        for (final String s : StringMgmt.remFirstArg(split)) {
                            mats.add(Material.matchMaterial(s.trim().toUpperCase()).name());
                        }
                        TownyWorldCommand.Globalworld.setUnclaimedZoneIgnore(mats);
                        TownyWorldCommand.plugin.resetCache();
                        if (player != null) {
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_wild_ignore"), TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.getUnclaimedZoneIgnoreMaterials()));
                        }
                        else {
                            sender.sendMessage(String.format(TownySettings.getLangString("msg_set_wild_ignore"), TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.getUnclaimedZoneIgnoreMaterials()));
                        }
                    }
                    catch (Exception e) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_input"), " on/off."));
                    }
                }
            }
            else if (split[0].equalsIgnoreCase("wildregen")) {
                if (split.length < 2) {
                    if (player != null) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /townyworld set wildregen Creeper,EnderCrystal,EnderDragon,Fireball,SmallFireball,LargeFireball,TNTPrimed,ExplosiveMinecart");
                    }
                    else {
                        sender.sendMessage("Eg: /townyworld set wildregen Creeper,EnderCrystal,EnderDragon,Fireball,SmallFireball,LargeFireball,TNTPrimed,ExplosiveMinecart <world>");
                    }
                }
                else {
                    final List<String> entities = new ArrayList<String>(Arrays.asList(StringMgmt.remFirstArg(split)));
                    TownyWorldCommand.Globalworld.setPlotManagementWildRevertEntities(entities);
                    if (player != null) {
                        TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_wild_regen"), TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.getPlotManagementWildRevertEntities()));
                    }
                    else {
                        sender.sendMessage(String.format(TownySettings.getLangString("msg_set_wild_regen"), TownyWorldCommand.Globalworld.getName(), TownyWorldCommand.Globalworld.getPlotManagementWildRevertEntities()));
                    }
                }
            }
            else {
                if (!split[0].equalsIgnoreCase("wildname")) {
                    if (player != null) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_property"), "world"));
                    }
                    return;
                }
                if (split.length < 2) {
                    if (player != null) {
                        TownyMessaging.sendErrorMsg(player, "Eg: /townyworld set wildname Wildy");
                    }
                }
                else {
                    try {
                        TownyWorldCommand.Globalworld.setUnclaimedZoneName(split[1]);
                        if (player != null) {
                            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_set_wild_name"), TownyWorldCommand.Globalworld.getName(), split[1]));
                        }
                        else {
                            sender.sendMessage(String.format(TownySettings.getLangString("msg_set_wild_name"), TownyWorldCommand.Globalworld.getName(), split[1]));
                        }
                    }
                    catch (Exception e) {
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_invalid_input"), " on/off."));
                    }
                }
            }
            TownyUniverse.getInstance().getDataSource().saveWorld(TownyWorldCommand.Globalworld);
        }
    }
    
    static {
        townyworld_help = new ArrayList<String>();
        townyworld_help_console = new ArrayList<String>();
        townyworld_set = new ArrayList<String>();
        townyworld_set_console = new ArrayList<String>();
    }
}
