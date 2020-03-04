// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.util.Compass;
import org.bukkit.entity.Player;

public class TownyAsciiMap
{
    public static final int lineWidth = 27;
    public static final int halfLineWidth = 13;
    public static final String[] help;
    
    public static String[] generateCompass(final Player player) {
        final Compass.Point dir = Compass.getCompassPointForDirection(player.getLocation().getYaw());
        return new String[] { "§0  -----  ", "§0  -" + ((dir == Compass.Point.NW) ? "§6\\" : "-") + ((dir == Compass.Point.N) ? "§6" : "§f") + "N" + ((dir == Compass.Point.NE) ? "§6/§0" : "§0-") + "-  ", "§0  -" + ((dir == Compass.Point.W) ? "§6W" : "§fW") + "§7" + "+" + ((dir == Compass.Point.E) ? "§6" : "§f") + "E" + "§0" + "-  ", "§0  -" + ((dir == Compass.Point.SW) ? "§6/" : "-") + ((dir == Compass.Point.S) ? "§6" : "§f") + "S" + ((dir == Compass.Point.SE) ? "§6\\§0" : "§0-") + "-  " };
    }
    
    public static void generateAndSend(final Towny plugin, final Player player, final int lineHeight) {
        boolean hasTown = false;
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Resident resident;
        try {
            resident = townyUniverse.getDataSource().getResident(player.getName());
            if (resident.hasTown()) {
                hasTown = true;
            }
        }
        catch (TownyException x) {
            TownyMessaging.sendErrorMsg(player, x.getMessage());
            return;
        }
        TownyWorld world;
        try {
            world = townyUniverse.getDataSource().getWorld(player.getWorld().getName());
        }
        catch (NotRegisteredException e1) {
            TownyMessaging.sendErrorMsg(player, "You are not in a registered world.");
            return;
        }
        if (!world.isUsingTowny()) {
            TownyMessaging.sendErrorMsg(player, "This world is not using towny.");
            return;
        }
        final Coord pos = Coord.parseCoord(plugin.getCache(player).getLastLocation());
        final int halfLineHeight = lineHeight / 2;
        final String[][] townyMap = new String[27][lineHeight];
        int y = 0;
        for (int tby = pos.getX() + 13; tby >= pos.getX() - 13; --tby) {
            int x2 = 0;
            for (int tbx = pos.getZ() - halfLineHeight; tbx <= pos.getZ() + (lineHeight - halfLineHeight - 1); ++tbx) {
                try {
                    final TownBlock townblock = world.getTownBlock(tby, tbx);
                    if (!townblock.hasTown()) {
                        throw new TownyException();
                    }
                    if (x2 == halfLineHeight && y == 13) {
                        townyMap[y][x2] = "§6";
                    }
                    else if (hasTown) {
                        if (resident.getTown() == townblock.getTown()) {
                            townyMap[y][x2] = "§a";
                            try {
                                if (resident == townblock.getResident()) {
                                    townyMap[y][x2] = "§e";
                                }
                            }
                            catch (NotRegisteredException ex) {}
                        }
                        else if (resident.hasNation()) {
                            if (resident.getTown().getNation().hasTown(townblock.getTown())) {
                                townyMap[y][x2] = "§2";
                            }
                            else if (townblock.getTown().hasNation()) {
                                final Nation nation = resident.getTown().getNation();
                                if (nation.hasAlly(townblock.getTown().getNation())) {
                                    townyMap[y][x2] = "§2";
                                }
                                else if (nation.hasEnemy(townblock.getTown().getNation())) {
                                    townyMap[y][x2] = "§4";
                                }
                                else {
                                    townyMap[y][x2] = "§f";
                                }
                            }
                            else {
                                townyMap[y][x2] = "§f";
                            }
                        }
                        else {
                            townyMap[y][x2] = "§f";
                        }
                    }
                    else {
                        townyMap[y][x2] = "§f";
                    }
                    if (townblock.getPlotPrice() != -1.0) {
                        if (townblock.getType().equals(TownBlockType.COMMERCIAL)) {
                            townyMap[y][x2] = "§3";
                        }
                        final StringBuilder sb = new StringBuilder();
                        final String[] array = townyMap[y];
                        final int n = x2;
                        array[n] = sb.append(array[n]).append("$").toString();
                    }
                    else if (townblock.isHomeBlock()) {
                        final StringBuilder sb2 = new StringBuilder();
                        final String[] array2 = townyMap[y];
                        final int n2 = x2;
                        array2[n2] = sb2.append(array2[n2]).append("H").toString();
                    }
                    else {
                        final StringBuilder sb3 = new StringBuilder();
                        final String[] array3 = townyMap[y];
                        final int n3 = x2;
                        array3[n3] = sb3.append(array3[n3]).append(townblock.getType().getAsciiMapKey()).toString();
                    }
                }
                catch (TownyException e2) {
                    if (x2 == halfLineHeight && y == 13) {
                        townyMap[y][x2] = "§6";
                    }
                    else {
                        townyMap[y][x2] = "§8";
                    }
                    final StringBuilder sb4 = new StringBuilder();
                    final String[] array4 = townyMap[y];
                    final int n4 = x2;
                    array4[n4] = sb4.append(array4[n4]).append("-").toString();
                }
                ++x2;
            }
            ++y;
        }
        final String[] compass = generateCompass(player);
        player.sendMessage(ChatTools.formatTitle(TownySettings.getLangString("towny_map_header") + "§f" + "(" + pos.toString() + ")"));
        int lineCount = 0;
        for (int my = 0; my < lineHeight; ++my) {
            String line = compass[0];
            if (lineCount < compass.length) {
                line = compass[lineCount];
            }
            for (int mx = 26; mx >= 0; --mx) {
                line += townyMap[mx][my];
            }
            if (lineCount < TownyAsciiMap.help.length) {
                line += TownyAsciiMap.help[lineCount];
            }
            player.sendMessage(line);
            ++lineCount;
        }
        try {
            final TownBlock townblock2 = world.getTownBlock(pos);
            TownyMessaging.sendMsg(player, TownySettings.getLangString("town_sing") + ": " + (townblock2.hasTown() ? townblock2.getTown().getName() : TownySettings.getLangString("status_no_town")) + " : " + TownySettings.getLangString("owner_status") + ": " + (townblock2.hasResident() ? townblock2.getResident().getName() : TownySettings.getLangString("status_no_town")));
        }
        catch (TownyException e3) {
            player.sendMessage("");
        }
    }
    
    static {
        help = new String[] { "  §8-§7 = " + TownySettings.getLangString("towny_map_unclaimed"), "  §f+§7 = " + TownySettings.getLangString("towny_map_claimed"), "  §f$§7 = " + TownySettings.getLangString("towny_map_forsale"), "  §a+§7 = " + TownySettings.getLangString("towny_map_yourtown"), "  §e+§7 = " + TownySettings.getLangString("towny_map_yourplot"), "  §2+§7 = " + TownySettings.getLangString("towny_map_ally"), "  §4+§7 = " + TownySettings.getLangString("towny_map_enemy") };
    }
}
