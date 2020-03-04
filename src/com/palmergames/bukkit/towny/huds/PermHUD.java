// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.huds;

import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.World;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.scoreboard.Scoreboard;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.ChatColor;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.entity.Player;

public class PermHUD
{
    private static final String PLOTNAME_TITLE;
    
    public static void updatePerms(final Player p) {
        final WorldCoord worldCoord = new WorldCoord(p.getWorld().getName(), Coord.parseCoord((Entity)p));
        updatePerms(p, worldCoord);
    }
    
    public static void updatePerms(final Player p, final WorldCoord worldCoord) {
        final Scoreboard board = p.getScoreboard();
        if (board == null) {
            toggleOn(p);
            return;
        }
        String build;
        String destroy;
        String switching;
        String item;
        String type;
        String pvp;
        String explosions;
        String firespread;
        String mobspawn;
        String title;
        String plotName;
        try {
            final TownBlock townBlock = worldCoord.getTownBlock();
            final TownBlockOwner owner = (TownBlockOwner)(townBlock.hasResident() ? townBlock.getResident() : townBlock.getTown());
            final Town town = townBlock.getTown();
            final TownyWorld world = townBlock.getWorld();
            final TownyPermission tp = townBlock.getPermissions();
            final String v = (owner instanceof Resident) ? "f" : "r";
            final String u = (owner instanceof Resident) ? "t" : "n";
            build = (tp.getResidentPerm(TownyPermission.ActionType.BUILD) ? v : "-") + (tp.getNationPerm(TownyPermission.ActionType.BUILD) ? u : "-") + (tp.getAllyPerm(TownyPermission.ActionType.BUILD) ? "a" : "-") + (tp.getOutsiderPerm(TownyPermission.ActionType.BUILD) ? "o" : "-");
            destroy = (tp.getResidentPerm(TownyPermission.ActionType.DESTROY) ? v : "-") + (tp.getNationPerm(TownyPermission.ActionType.DESTROY) ? u : "-") + (tp.getAllyPerm(TownyPermission.ActionType.DESTROY) ? "a" : "-") + (tp.getOutsiderPerm(TownyPermission.ActionType.DESTROY) ? "o" : "-");
            switching = (tp.getResidentPerm(TownyPermission.ActionType.SWITCH) ? v : "-") + (tp.getNationPerm(TownyPermission.ActionType.SWITCH) ? u : "-") + (tp.getAllyPerm(TownyPermission.ActionType.SWITCH) ? "a" : "-") + (tp.getOutsiderPerm(TownyPermission.ActionType.SWITCH) ? "o" : "-");
            item = (tp.getResidentPerm(TownyPermission.ActionType.ITEM_USE) ? v : "-") + (tp.getNationPerm(TownyPermission.ActionType.ITEM_USE) ? u : "-") + (tp.getAllyPerm(TownyPermission.ActionType.ITEM_USE) ? "a" : "-") + (tp.getOutsiderPerm(TownyPermission.ActionType.ITEM_USE) ? "o" : "-");
            type = (townBlock.getType().equals(TownBlockType.RESIDENTIAL) ? " " : townBlock.getType().name());
            pvp = ((town.isPVP() || world.isForcePVP() || townBlock.getPermissions().pvp) ? (ChatColor.DARK_RED + "ON") : (ChatColor.GREEN + "OFF"));
            explosions = ((world.isForceExpl() || townBlock.getPermissions().explosion) ? (ChatColor.DARK_RED + "ON") : (ChatColor.GREEN + "OFF"));
            firespread = ((town.isFire() || world.isForceFire() || townBlock.getPermissions().fire) ? (ChatColor.DARK_RED + "ON") : (ChatColor.GREEN + "OFF"));
            mobspawn = ((town.hasMobs() || world.isForceTownMobs() || townBlock.getPermissions().mobs) ? (ChatColor.DARK_RED + "ON") : (ChatColor.GREEN + "OFF"));
            if (townBlock.hasResident()) {
                title = ChatColor.GOLD + townBlock.getResident().getName() + "(" + townBlock.getTown().getName() + ")";
            }
            else {
                title = ChatColor.GOLD + townBlock.getTown().getName();
            }
            plotName = (townBlock.getName().isEmpty() ? "" : (PermHUD.PLOTNAME_TITLE + townBlock.getName()));
        }
        catch (NotRegisteredException e) {
            clearPerms(p);
            return;
        }
        if (!plotName.isEmpty()) {
            board.getTeam("plot").setSuffix(HUDManager.check(plotName));
        }
        board.getTeam("build").setSuffix(build);
        board.getTeam("destroy").setSuffix(destroy);
        board.getTeam("switching").setSuffix(switching);
        board.getTeam("item").setSuffix(item);
        board.getTeam("plotType").setSuffix(type);
        board.getTeam("pvp").setSuffix(pvp);
        board.getTeam("explosions").setSuffix(explosions);
        board.getTeam("firespread").setSuffix(firespread);
        board.getTeam("mobspawn").setSuffix(mobspawn);
        board.getObjective("PERM_HUD_OBJ").setDisplayName(HUDManager.check(title));
    }
    
    private static void clearPerms(final Player p) {
        final Scoreboard board = p.getScoreboard();
        try {
            board.getTeam("plot").setSuffix(" ");
            board.getTeam("build").setSuffix(" ");
            board.getTeam("destroy").setSuffix(" ");
            board.getTeam("switching").setSuffix(" ");
            board.getTeam("item").setSuffix(" ");
            board.getTeam("plotType").setSuffix(" ");
            board.getTeam("pvp").setSuffix(" ");
            board.getTeam("explosions").setSuffix(" ");
            board.getTeam("firespread").setSuffix(" ");
            board.getTeam("mobspawn").setSuffix(" ");
            board.getObjective("PERM_HUD_OBJ").setDisplayName(HUDManager.check(getFormattedWildernessName(p.getWorld())));
        }
        catch (NullPointerException e) {
            toggleOn(p);
        }
    }
    
    private static String getFormattedWildernessName(final World w) {
        final StringBuilder wildernessName = new StringBuilder().append(ChatColor.DARK_RED).append(ChatColor.BOLD);
        try {
            wildernessName.append(TownyUniverse.getInstance().getDataSource().getWorld(w.getName()).getUnclaimedZoneName());
        }
        catch (NotRegisteredException e) {
            wildernessName.append("Unknown");
        }
        return wildernessName.toString();
    }
    
    public static void toggleOn(final Player p) {
        final String PERM_HUD_TITLE = ChatColor.GOLD + "";
        final String permsTitle_entry = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "Plot Perms";
        final String plotName_entry = ChatColor.DARK_GREEN + "";
        final String build_entry = ChatColor.DARK_GREEN + "Build: " + ChatColor.GRAY;
        final String destroy_entry = ChatColor.DARK_GREEN + "Destroy: " + ChatColor.GRAY;
        final String switching_entry = ChatColor.DARK_GREEN + "Switch: " + ChatColor.GRAY;
        final String item_entry = ChatColor.DARK_GREEN + "Item: " + ChatColor.GRAY;
        final String keyPlotType_entry = ChatColor.DARK_GREEN + "Type: ";
        final String pvp_entry = ChatColor.DARK_GREEN + "PvP: ";
        final String explosions_entry = ChatColor.DARK_GREEN + "Explosions: ";
        final String firespread_entry = ChatColor.DARK_GREEN + "Firespread: ";
        final String mobspawn_entry = ChatColor.DARK_GREEN + "Mob Spawns: ";
        final String keyTitle_entry = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "Key";
        final String keyResident_entry = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "f" + ChatColor.WHITE + " - " + ChatColor.GRAY + "friend" + ChatColor.DARK_GREEN + " " + ChatColor.BOLD + "r" + ChatColor.WHITE + " - " + ChatColor.GRAY + "resident";
        final String keyNation_entry = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "t" + ChatColor.WHITE + " - " + ChatColor.GRAY + "town" + ChatColor.DARK_GREEN + " " + ChatColor.BOLD + "n" + ChatColor.WHITE + " - " + ChatColor.GRAY + "nation";
        final String keyAlly_entry = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "a" + ChatColor.WHITE + " - " + ChatColor.GRAY + "ally" + ChatColor.DARK_GREEN + " " + ChatColor.BOLD + "o" + ChatColor.WHITE + " - " + ChatColor.GRAY + "outsider";
        final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        final Objective obj = board.registerNewObjective("PERM_HUD_OBJ", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(PERM_HUD_TITLE);
        final Team permsTitle = board.registerNewTeam("permsTitle");
        final Team plotName = board.registerNewTeam("plot");
        final Team build = board.registerNewTeam("build");
        final Team destroy = board.registerNewTeam("destroy");
        final Team switching = board.registerNewTeam("switching");
        final Team item = board.registerNewTeam("item");
        final Team keyPlotType = board.registerNewTeam("plotType");
        final Team pvp = board.registerNewTeam("pvp");
        final Team explosions = board.registerNewTeam("explosions");
        final Team firespread = board.registerNewTeam("firespread");
        final Team mobspawn = board.registerNewTeam("mobspawn");
        final Team keyTitle = board.registerNewTeam("keyTitle");
        final Team keyResident = board.registerNewTeam("keyResident");
        final Team keyFriend = board.registerNewTeam("keyFriend");
        final Team keyAlly = board.registerNewTeam("keyAlly");
        permsTitle.addEntry(permsTitle_entry);
        plotName.addEntry(plotName_entry);
        build.addEntry(build_entry);
        destroy.addEntry(destroy_entry);
        switching.addEntry(switching_entry);
        item.addEntry(item_entry);
        keyPlotType.addEntry(keyPlotType_entry);
        pvp.addEntry(pvp_entry);
        explosions.addEntry(explosions_entry);
        firespread.addEntry(firespread_entry);
        mobspawn.addEntry(mobspawn_entry);
        keyTitle.addEntry(keyTitle_entry);
        keyResident.addEntry(keyResident_entry);
        keyFriend.addEntry(keyNation_entry);
        keyAlly.addEntry(keyAlly_entry);
        obj.getScore(permsTitle_entry).setScore(15);
        obj.getScore(plotName_entry).setScore(14);
        obj.getScore(build_entry).setScore(13);
        obj.getScore(destroy_entry).setScore(12);
        obj.getScore(switching_entry).setScore(11);
        obj.getScore(item_entry).setScore(10);
        obj.getScore(pvp_entry).setScore(8);
        obj.getScore(keyPlotType_entry).setScore(9);
        obj.getScore(explosions_entry).setScore(7);
        obj.getScore(firespread_entry).setScore(6);
        obj.getScore(mobspawn_entry).setScore(5);
        obj.getScore(keyTitle_entry).setScore(4);
        obj.getScore(keyResident_entry).setScore(3);
        obj.getScore(keyNation_entry).setScore(2);
        obj.getScore(keyAlly_entry).setScore(1);
        p.setScoreboard(board);
        updatePerms(p);
    }
    
    static {
        PLOTNAME_TITLE = "Plot: " + ChatColor.GRAY;
    }
}
