// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.confirmations;

import java.util.Iterator;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyPermissionChange;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.event.TownBlockSettingsChangedEvent;
import com.palmergames.bukkit.towny.command.PlotCommand;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.List;
import com.palmergames.bukkit.towny.tasks.PlotClaim;
import com.palmergames.bukkit.towny.tasks.TownClaim;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.tasks.ResidentPurge;
import com.palmergames.util.TimeTools;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.HashMap;
import com.palmergames.bukkit.towny.Towny;

public class ConfirmationHandler
{
    private static Towny plugin;
    private static HashMap<Resident, Town> towndeleteconfirmations;
    private static HashMap<Resident, Town> townunclaimallconfirmations;
    private static HashMap<Resident, Nation> nationdeleteconfirmations;
    private static HashMap<Resident, String> townypurgeconfirmations;
    private static HashMap<Resident, Nation> nationmergeconfirmations;
    private static HashMap<Resident, GroupConfirmation> groupclaimconfirmations;
    private static HashMap<Resident, GroupConfirmation> groupremoveconfirmations;
    private static HashMap<Resident, GroupConfirmation> groupsetpermconfirmations;
    private static HashMap<Resident, GroupConfirmation> grouptoggleconfirmations;
    public static ConfirmationType consoleConfirmationType;
    private static Object consoleExtra;
    
    public static void initialize(final Towny plugin) {
        ConfirmationHandler.plugin = plugin;
    }
    
    public static void addConfirmation(final Resident r, final ConfirmationType type, final Object extra) throws TownyException {
        switch (type) {
            case TOWN_DELETE: {
                r.setConfirmationType(type);
                ConfirmationHandler.towndeleteconfirmations.put(r, r.getTown());
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case NATION_DELETE: {
                r.setConfirmationType(type);
                ConfirmationHandler.nationdeleteconfirmations.put(r, r.getTown().getNation());
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case UNCLAIM_ALL: {
                r.setConfirmationType(type);
                ConfirmationHandler.townunclaimallconfirmations.put(r, r.getTown());
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case PURGE: {
                r.setConfirmationType(type);
                ConfirmationHandler.townypurgeconfirmations.put(r, (String)extra);
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case NATION_MERGE: {
                r.setConfirmationType(type);
                ConfirmationHandler.nationmergeconfirmations.put(r, (Nation)extra);
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
            }
            case GROUP_CLAIM_ACTION: {
                r.setConfirmationType(type);
                ConfirmationHandler.groupclaimconfirmations.put(r, (GroupConfirmation)extra);
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case GROUP_UNCLAIM_ACTION: {
                r.setConfirmationType(type);
                ConfirmationHandler.groupremoveconfirmations.put(r, (GroupConfirmation)extra);
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case GROUP_SET_PERM_ACTION: {
                r.setConfirmationType(type);
                ConfirmationHandler.groupsetpermconfirmations.put(r, (GroupConfirmation)extra);
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
            case GROUP_TOGGLE_ACTION: {
                r.setConfirmationType(type);
                ConfirmationHandler.grouptoggleconfirmations.put(r, (GroupConfirmation)extra);
                new BukkitRunnable() {
                    public void run() {
                        ConfirmationHandler.removeConfirmation(r, type, false);
                    }
                }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
                break;
            }
        }
    }
    
    public static void removeConfirmation(final Resident r, final ConfirmationType type, final boolean successful) {
        boolean sendmessage = false;
        switch (type) {
            case TOWN_DELETE: {
                if (ConfirmationHandler.towndeleteconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.towndeleteconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case NATION_DELETE: {
                if (ConfirmationHandler.nationdeleteconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.nationdeleteconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case UNCLAIM_ALL: {
                if (ConfirmationHandler.townunclaimallconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.townunclaimallconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case PURGE: {
                if (ConfirmationHandler.townypurgeconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.townypurgeconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case NATION_MERGE: {
                if (ConfirmationHandler.nationmergeconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.nationmergeconfirmations.remove(r);
                r.setConfirmationType(null);
            }
            case GROUP_CLAIM_ACTION: {
                if (ConfirmationHandler.groupclaimconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.groupclaimconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case GROUP_UNCLAIM_ACTION: {
                if (ConfirmationHandler.groupremoveconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.groupclaimconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case GROUP_SET_PERM_ACTION: {
                if (ConfirmationHandler.groupsetpermconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.groupsetpermconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
            case GROUP_TOGGLE_ACTION: {
                if (ConfirmationHandler.grouptoggleconfirmations.containsKey(r) && !successful) {
                    sendmessage = true;
                }
                ConfirmationHandler.grouptoggleconfirmations.remove(r);
                r.setConfirmationType(null);
                break;
            }
        }
        if (sendmessage) {
            TownyMessaging.sendMsg(r, TownySettings.getLangString("successful_cancel"));
        }
    }
    
    public static void handleConfirmation(final Resident r, final ConfirmationType type) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (type == ConfirmationType.TOWN_DELETE && ConfirmationHandler.towndeleteconfirmations.containsKey(r) && ConfirmationHandler.towndeleteconfirmations.get(r).equals(r.getTown())) {
            TownyMessaging.sendGlobalMessage(TownySettings.getDelTownMsg(ConfirmationHandler.towndeleteconfirmations.get(r)));
            townyUniverse.getDataSource().removeTown(ConfirmationHandler.towndeleteconfirmations.get(r));
            removeConfirmation(r, type, true);
            return;
        }
        if (type == ConfirmationType.PURGE && ConfirmationHandler.townypurgeconfirmations.containsKey(r)) {
            final Player player = TownyAPI.getInstance().getPlayer(r);
            if (player == null) {
                throw new TownyException("Player could not be found!");
            }
            if (!townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PURGE.getNode())) {
                throw new TownyException(TownySettings.getLangString("msg_err_admin_only"));
            }
            int days = 1;
            boolean townless = false;
            if (ConfirmationHandler.townypurgeconfirmations.get(r).startsWith("townless")) {
                townless = true;
                days = Integer.parseInt(ConfirmationHandler.townypurgeconfirmations.get(r).substring(8));
            }
            else {
                days = Integer.parseInt(ConfirmationHandler.townypurgeconfirmations.get(r));
            }
            new ResidentPurge(ConfirmationHandler.plugin, (CommandSender)player, TimeTools.getMillis(days + "d"), townless).start();
            removeConfirmation(r, type, true);
        }
        if (type == ConfirmationType.UNCLAIM_ALL && ConfirmationHandler.townunclaimallconfirmations.containsKey(r) && ConfirmationHandler.townunclaimallconfirmations.get(r).equals(r.getTown())) {
            TownClaim.townUnclaimAll(ConfirmationHandler.plugin, ConfirmationHandler.townunclaimallconfirmations.get(r));
            removeConfirmation(r, type, true);
            return;
        }
        if (type == ConfirmationType.NATION_DELETE && ConfirmationHandler.nationdeleteconfirmations.containsKey(r) && ConfirmationHandler.nationdeleteconfirmations.get(r).equals(r.getTown().getNation())) {
            townyUniverse.getDataSource().removeNation(ConfirmationHandler.nationdeleteconfirmations.get(r));
            TownyMessaging.sendGlobalMessage(TownySettings.getDelNationMsg(ConfirmationHandler.nationdeleteconfirmations.get(r)));
            removeConfirmation(r, type, true);
        }
        if (type == ConfirmationType.NATION_MERGE && ConfirmationHandler.nationmergeconfirmations.containsKey(r)) {
            final Nation succumbingNation = r.getTown().getNation();
            final Nation prevailingNation = ConfirmationHandler.nationmergeconfirmations.get(r);
            townyUniverse.getDataSource().mergeNation(succumbingNation, prevailingNation);
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("nation1_has_merged_with_nation2"), succumbingNation, prevailingNation));
            removeConfirmation(r, type, true);
        }
        if (type == ConfirmationType.GROUP_CLAIM_ACTION && ConfirmationHandler.groupclaimconfirmations.containsKey(r)) {
            final GroupConfirmation confirmation = ConfirmationHandler.groupclaimconfirmations.get(r);
            final ArrayList<WorldCoord> coords = plotGroupBlocksToCoords(confirmation.getGroup());
            new PlotClaim(Towny.getPlugin(), confirmation.getPlayer(), r, coords, true, false, true).start();
            removeConfirmation(r, type, true);
        }
        if (type == ConfirmationType.GROUP_UNCLAIM_ACTION && ConfirmationHandler.groupremoveconfirmations.containsKey(r)) {
            final GroupConfirmation confirmation = ConfirmationHandler.groupremoveconfirmations.get(r);
            final ArrayList<WorldCoord> coords = plotGroupBlocksToCoords(confirmation.getGroup());
            new PlotClaim(Towny.getPlugin(), confirmation.getPlayer(), r, coords, false, false, false).start();
            removeConfirmation(r, type, true);
        }
        if (type == ConfirmationType.GROUP_SET_PERM_ACTION && ConfirmationHandler.groupsetpermconfirmations.containsKey(r)) {
            final GroupConfirmation confirmation = ConfirmationHandler.groupsetpermconfirmations.get(r);
            TownBlock tb = confirmation.getGroup().getTownBlocks().get(0);
            final TownBlockOwner townBlockOwner = confirmation.getTownBlockOwner();
            final TownyPermissionChange permChange = PlotCommand.setTownBlockPermissions(confirmation.getPlayer(), townBlockOwner, tb, confirmation.getArgs());
            if (permChange != null) {
                for (int i = 1; i < confirmation.getGroup().getTownBlocks().size(); ++i) {
                    tb = confirmation.getGroup().getTownBlocks().get(i);
                    tb.getPermissions().change(permChange);
                    tb.setChanged(true);
                    townyUniverse.getDataSource().saveTownBlock(tb);
                    final TownBlockSettingsChangedEvent event = new TownBlockSettingsChangedEvent(tb);
                    Bukkit.getServer().getPluginManager().callEvent((Event)event);
                }
                ConfirmationHandler.plugin.resetCache();
                final Player player2 = confirmation.getPlayer();
                final TownyPermission perm = confirmation.getGroup().getTownBlocks().get(0).getPermissions();
                TownyMessaging.sendMsg(player2, TownySettings.getLangString("msg_set_perms"));
                TownyMessaging.sendMessage(player2, "§2 Perm: " + ((townBlockOwner instanceof Resident) ? perm.getColourString().replace("n", "t") : perm.getColourString().replace("f", "r")));
                TownyMessaging.sendMessage(player2, "§2 Perm: " + ((townBlockOwner instanceof Resident) ? perm.getColourString2().replace("n", "t") : perm.getColourString2().replace("f", "r")));
                TownyMessaging.sendMessage(player2, "§2PvP: " + (perm.pvp ? "§aOFF" : "§4ON") + "§2" + "  Explosions: " + (perm.explosion ? "§4ON" : "§aOFF") + "§2" + "  Firespread: " + (perm.fire ? "§4ON" : "§aOFF") + "§2" + "  Mob Spawns: " + (perm.mobs ? "§4ON" : "§aOFF"));
            }
            removeConfirmation(r, type, true);
        }
        if (type == ConfirmationType.GROUP_TOGGLE_ACTION) {
            final GroupConfirmation confirmation = ConfirmationHandler.grouptoggleconfirmations.get(r);
            new PlotCommand(Towny.getPlugin()).plotGroupToggle(confirmation.getPlayer(), confirmation.getGroup(), confirmation.getArgs());
            removeConfirmation(r, type, true);
        }
    }
    
    private static ArrayList<WorldCoord> plotGroupBlocksToCoords(final PlotObjectGroup group) {
        final ArrayList<WorldCoord> coords = new ArrayList<WorldCoord>();
        for (final TownBlock tb : group.getTownBlocks()) {
            coords.add(tb.getWorldCoord());
        }
        return coords;
    }
    
    public static void addConfirmation(final ConfirmationType type, final Object extra) {
        if (ConfirmationHandler.consoleConfirmationType.equals(ConfirmationType.NULL)) {
            ConfirmationHandler.consoleExtra = extra;
            ConfirmationHandler.consoleConfirmationType = type;
            new BukkitRunnable() {
                public void run() {
                    ConfirmationHandler.removeConfirmation(type, false);
                }
            }.runTaskLater((Plugin)ConfirmationHandler.plugin, 400L);
        }
        else {
            TownyMessaging.sendMsg("Unable to start a new confirmation, one already exists of type: " + ConfirmationHandler.consoleConfirmationType.toString());
        }
    }
    
    public static void removeConfirmation(final ConfirmationType type, final boolean successful) {
        boolean sendmessage = false;
        if (!ConfirmationHandler.consoleConfirmationType.equals(ConfirmationType.NULL) && !successful) {
            sendmessage = true;
        }
        ConfirmationHandler.consoleConfirmationType = ConfirmationType.NULL;
        if (sendmessage) {
            TownyMessaging.sendMsg(TownySettings.getLangString("successful_cancel"));
        }
    }
    
    public static void handleConfirmation(final ConfirmationType type) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (type == ConfirmationType.TOWN_DELETE) {
            final Town town = (Town)ConfirmationHandler.consoleExtra;
            TownyMessaging.sendGlobalMessage(TownySettings.getDelTownMsg(town));
            townyUniverse.getDataSource().removeTown(town);
            removeConfirmation(type, true);
            ConfirmationHandler.consoleExtra = null;
            return;
        }
        if (type == ConfirmationType.PURGE) {
            int days = 1;
            boolean townless = false;
            if (((String)ConfirmationHandler.consoleExtra).startsWith("townless")) {
                townless = true;
                days = Integer.parseInt(((String)ConfirmationHandler.consoleExtra).substring(8));
            }
            else {
                days = Integer.parseInt((String)ConfirmationHandler.consoleExtra);
            }
            new ResidentPurge(ConfirmationHandler.plugin, null, TimeTools.getMillis(days + "d"), townless).start();
            removeConfirmation(type, true);
            ConfirmationHandler.consoleExtra = null;
        }
        if (type == ConfirmationType.NATION_DELETE) {
            final Nation nation = (Nation)ConfirmationHandler.consoleExtra;
            TownyMessaging.sendGlobalMessage(TownySettings.getDelNationMsg(nation));
            townyUniverse.getDataSource().removeNation(nation);
            removeConfirmation(type, true);
            ConfirmationHandler.consoleExtra = null;
        }
    }
    
    static {
        ConfirmationHandler.towndeleteconfirmations = new HashMap<Resident, Town>();
        ConfirmationHandler.townunclaimallconfirmations = new HashMap<Resident, Town>();
        ConfirmationHandler.nationdeleteconfirmations = new HashMap<Resident, Nation>();
        ConfirmationHandler.townypurgeconfirmations = new HashMap<Resident, String>();
        ConfirmationHandler.nationmergeconfirmations = new HashMap<Resident, Nation>();
        ConfirmationHandler.groupclaimconfirmations = new HashMap<Resident, GroupConfirmation>();
        ConfirmationHandler.groupremoveconfirmations = new HashMap<Resident, GroupConfirmation>();
        ConfirmationHandler.groupsetpermconfirmations = new HashMap<Resident, GroupConfirmation>();
        ConfirmationHandler.grouptoggleconfirmations = new HashMap<Resident, GroupConfirmation>();
        ConfirmationHandler.consoleConfirmationType = ConfirmationType.NULL;
        ConfirmationHandler.consoleExtra = null;
    }
}
