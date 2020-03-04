// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.huds;

import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.Bukkit;
import java.util.Hashtable;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.ChatColor;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Player;

public class WarHUD
{
    static final int home_health;
    static final int town_health;
    
    public static void updateLocation(final Player p, final WorldCoord at) {
        String town_loc;
        String homeblock;
        try {
            town_loc = at.getTownBlock().getTown().getName();
            if (at.getTownBlock().isHomeBlock()) {
                homeblock = TownySettings.getLangString("war_hud_homeblock");
            }
            else {
                homeblock = "";
            }
        }
        catch (NotRegisteredException e) {
            town_loc = TownySettings.getLangString("war_hud_wilderness");
            homeblock = "";
        }
        String nation_loc;
        try {
            nation_loc = at.getTownBlock().getTown().getNation().getName();
        }
        catch (NotRegisteredException e) {
            nation_loc = "";
        }
        p.getScoreboard().getTeam("nation").setSuffix(HUDManager.check(nation_loc));
        p.getScoreboard().getTeam("town").setSuffix(HUDManager.check(town_loc));
        p.getScoreboard().getTeam("home").setSuffix(HUDManager.check(homeblock));
    }
    
    public static void updateAttackable(final Player p, final WorldCoord at, final War war) {
        if (!TownySettings.getOnlyAttackEdgesInWar()) {
            return;
        }
        String onEdge;
        if (isOnEdgeOfTown(at, war)) {
            onEdge = TownySettings.getLangString("war_hud_true");
        }
        else {
            onEdge = TownySettings.getLangString("war_hud_false");
        }
        p.getScoreboard().getTeam("edge").setSuffix(HUDManager.check(onEdge));
    }
    
    public static void updateHealth(final Player p, final WorldCoord at, final War war) {
        boolean isTown = false;
        String health;
        try {
            if (War.isWarZone(at.getTownBlock().getWorldCoord())) {
                health = war.getWarZone().get(at) + "" + ChatColor.AQUA + "/" + (at.getTownBlock().isHomeBlock() ? WarHUD.home_health : WarHUD.town_health);
            }
            else {
                isTown = true;
                if (at.getTownBlock().getTown().getNation().isNeutral()) {
                    health = TownySettings.getLangString("war_hud_peaceful");
                }
                else {
                    health = TownySettings.getLangString("war_hud_fallen");
                }
            }
        }
        catch (NotRegisteredException e) {
            if (isTown) {
                health = TownySettings.getLangString("war_hud_peaceful");
            }
            else {
                health = "";
            }
        }
        p.getScoreboard().getTeam("health").setSuffix(health);
    }
    
    public static void updateHealth(final Player p, final int health, final boolean home) {
        if (health > 0) {
            p.getScoreboard().getTeam("health").setSuffix(health + "" + ChatColor.AQUA + "/" + (home ? WarHUD.home_health : WarHUD.town_health));
        }
        else {
            p.getScoreboard().getTeam("health").setSuffix(TownySettings.getLangString("war_hud_fallen"));
            if (TownySettings.getOnlyAttackEdgesInWar()) {
                p.getScoreboard().getTeam("edge").setSuffix("war_hud_false");
            }
        }
    }
    
    public static void updateHomeTown(final Player p) {
        String homeTown;
        try {
            homeTown = TownyUniverse.getInstance().getDataSource().getResident(p.getName()).getTown().getName();
        }
        catch (NotRegisteredException e) {
            homeTown = TownySettings.getLangString("war_hud_townless");
        }
        p.getScoreboard().getTeam("town_title").setSuffix(HUDManager.check(homeTown));
    }
    
    public static void updateScore(final Player p, final War war) {
        String score;
        try {
            final Town home = TownyUniverse.getInstance().getDataSource().getResident(p.getName()).getTown();
            final Hashtable<Town, Integer> scores = war.getTownScores();
            if (scores.containsKey(home)) {
                score = scores.get(home) + "";
            }
            else {
                score = "";
            }
        }
        catch (NotRegisteredException e) {
            score = "";
        }
        p.getScoreboard().getTeam("town_score").setSuffix(HUDManager.check(score));
    }
    
    public static void updateTopScores(final Player p, final String[] top) {
        final String fprefix = top[0].contains("-") ? (ChatColor.GOLD + top[0].split("-")[0] + ChatColor.WHITE + "-") : "";
        final String sprefix = top[1].contains("-") ? (ChatColor.GRAY + top[1].split("-")[0] + ChatColor.WHITE + "-") : "";
        final String tprefix = top[2].contains("-") ? (ChatColor.GRAY + top[2].split("-")[0] + ChatColor.WHITE + "-") : "";
        final String fsuffix = top[0].contains("-") ? top[0].split("-")[1] : "";
        final String ssuffix = top[1].contains("-") ? top[1].split("-")[1] : "";
        final String tsuffix = top[2].contains("-") ? top[2].split("-")[1] : "";
        p.getScoreboard().getTeam("first").setPrefix(HUDManager.check(fprefix));
        p.getScoreboard().getTeam("first").setSuffix(HUDManager.check(fsuffix));
        p.getScoreboard().getTeam("second").setPrefix(HUDManager.check(sprefix));
        p.getScoreboard().getTeam("second").setSuffix(HUDManager.check(ssuffix));
        p.getScoreboard().getTeam("third").setPrefix(HUDManager.check(tprefix));
        p.getScoreboard().getTeam("third").setSuffix(HUDManager.check(tsuffix));
    }
    
    public static void updateScore(final Player p, final int score) {
        p.getScoreboard().getTeam("town_score").setSuffix(HUDManager.check(score + ""));
    }
    
    public static void toggleOn(final Player p, final War war) {
        final boolean edges = TownySettings.getOnlyAttackEdgesInWar();
        final String WAR_HUD_TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + TownySettings.getLangString("war_hud_war");
        final String space1_entry = ChatColor.DARK_PURPLE.toString();
        final String town_title_entry = ChatColor.YELLOW + "" + ChatColor.UNDERLINE;
        final String town_score_entry = ChatColor.WHITE + TownySettings.getLangString("war_hud_score") + ChatColor.RED;
        final String space2_entry = ChatColor.DARK_BLUE.toString();
        final String location_title_entry = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + TownySettings.getLangString("war_hud_location");
        final String nation_entry = ChatColor.WHITE + TownySettings.getLangString("war_hud_nation") + ChatColor.GOLD;
        final String town_entry = ChatColor.WHITE + TownySettings.getLangString("war_hud_town") + ChatColor.DARK_AQUA;
        final String edge_entry = ChatColor.WHITE + TownySettings.getLangString("war_hud_attackable") + ChatColor.RED;
        final String health_entry = ChatColor.WHITE + TownySettings.getLangString("war_hud_health") + ChatColor.RED;
        final String home_entry = ChatColor.RED + "";
        final String space3_entry = ChatColor.DARK_GREEN.toString();
        final String top_title_entry = ChatColor.YELLOW + "" + ChatColor.UNDERLINE + TownySettings.getLangString("war_hud_top_towns");
        final String first_entry = ChatColor.DARK_GREEN + "" + ChatColor.DARK_AQUA + "";
        final String second_entry = ChatColor.BLACK + "" + ChatColor.DARK_AQUA + "";
        final String third_entry = ChatColor.YELLOW + "" + ChatColor.DARK_AQUA + "";
        final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        final Objective obj = board.registerNewObjective("WAR_HUD_OBJ", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(WAR_HUD_TITLE);
        final Team space1 = board.registerNewTeam("space1");
        final Team town_title = board.registerNewTeam("town_title");
        final Team town_score = board.registerNewTeam("town_score");
        final Team space2 = board.registerNewTeam("space2");
        final Team location_title = board.registerNewTeam("location_title");
        final Team nation = board.registerNewTeam("nation");
        final Team town = board.registerNewTeam("town");
        final Team health = board.registerNewTeam("health");
        final Team home = board.registerNewTeam("home");
        final Team space3 = board.registerNewTeam("space3");
        final Team top_title = board.registerNewTeam("top_title");
        final Team first = board.registerNewTeam("first");
        final Team second = board.registerNewTeam("second");
        final Team third = board.registerNewTeam("third");
        space1.addEntry(space1_entry);
        town_title.addEntry(town_title_entry);
        town_score.addEntry(town_score_entry);
        space2.addEntry(space2_entry);
        location_title.addEntry(location_title_entry);
        nation.addEntry(nation_entry);
        town.addEntry(town_entry);
        health.addEntry(health_entry);
        home.addEntry(home_entry);
        space3.addEntry(space3_entry);
        top_title.addEntry(top_title_entry);
        first.addEntry(first_entry);
        second.addEntry(second_entry);
        third.addEntry(third_entry);
        obj.getScore(space1_entry).setScore(14);
        obj.getScore(town_title_entry).setScore(13);
        obj.getScore(town_score_entry).setScore(12);
        obj.getScore(space2_entry).setScore(11);
        obj.getScore(location_title_entry).setScore(10);
        obj.getScore(nation_entry).setScore(9);
        obj.getScore(town_entry).setScore(8);
        obj.getScore(health_entry).setScore(edges ? 6 : 7);
        obj.getScore(home_entry).setScore(edges ? 5 : 6);
        obj.getScore(space3_entry).setScore(edges ? 4 : 5);
        obj.getScore(top_title_entry).setScore(edges ? 3 : 4);
        obj.getScore(first_entry).setScore(edges ? 2 : 3);
        obj.getScore(second_entry).setScore(edges ? 1 : 2);
        obj.getScore(third_entry).setScore((int)(edges ? 0 : 1));
        if (edges) {
            final Team edge = board.registerNewTeam("edge");
            edge.addEntry(edge_entry);
            obj.getScore(edge_entry).setScore(7);
        }
        p.setScoreboard(board);
        final WorldCoord at = new WorldCoord(p.getWorld().getName(), Coord.parseCoord((Entity)p));
        updateLocation(p, at);
        updateAttackable(p, at, war);
        updateHealth(p, at, war);
        updateHomeTown(p);
        updateScore(p, war);
        updateTopScores(p, war.getTopThree());
    }
    
    public static boolean isOnEdgeOfTown(final WorldCoord worldCoord, final War war) {
        Town currentTown;
        try {
            currentTown = worldCoord.getTownBlock().getTown();
            if (!War.isWarZone(worldCoord)) {
                return false;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        final int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int i = 0; i < 4; ++i) {
            try {
                final TownBlock edgeTownBlock = worldCoord.getTownyWorld().getTownBlock(new Coord(worldCoord.getX() + offset[i][0], worldCoord.getZ() + offset[i][1]));
                final boolean sameTown = edgeTownBlock.getTown() == currentTown;
                if (!sameTown || (sameTown && !War.isWarZone(edgeTownBlock.getWorldCoord()))) {
                    return true;
                }
            }
            catch (NotRegisteredException e2) {
                return true;
            }
        }
        return false;
    }
    
    static {
        home_health = TownySettings.getWarzoneHomeBlockHealth();
        town_health = TownySettings.getWarzoneTownBlockHealth();
    }
}
