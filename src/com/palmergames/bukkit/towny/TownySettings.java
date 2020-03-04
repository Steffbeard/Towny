// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import java.util.TreeMap;
import java.util.Collections;
import com.palmergames.bukkit.util.NameValidation;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import org.bukkit.ChatColor;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.TownyObject;
import java.util.HashMap;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.palmergames.util.TimeTools;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.NationSpawnLevel;
import com.palmergames.bukkit.towny.object.TownSpawnLevel;
import org.bukkit.configuration.InvalidConfigurationException;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import org.bukkit.Material;
import com.palmergames.bukkit.config.ConfigNodes;
import java.io.File;
import com.palmergames.util.FileMgmt;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.SortedMap;
import com.palmergames.bukkit.config.CommentedConfiguration;

public class TownySettings
{
    private static CommentedConfiguration config;
    private static CommentedConfiguration newConfig;
    private static CommentedConfiguration language;
    private static CommentedConfiguration newLanguage;
    private static CommentedConfiguration playermap;
    private static final SortedMap<Integer, Map<TownLevel, Object>> configTownLevel;
    private static final SortedMap<Integer, Map<NationLevel, Object>> configNationLevel;
    
    public static void newTownLevel(final int numResidents, final String namePrefix, final String namePostfix, final String mayorPrefix, final String mayorPostfix, final int townBlockLimit, final double townUpkeepMultiplier, final int townOutpostLimit, final int townBlockBuyBonusLimit) {
        final ConcurrentHashMap<TownLevel, Object> m = new ConcurrentHashMap<TownLevel, Object>();
        m.put(TownLevel.NAME_PREFIX, namePrefix);
        m.put(TownLevel.NAME_POSTFIX, namePostfix);
        m.put(TownLevel.MAYOR_PREFIX, mayorPrefix);
        m.put(TownLevel.MAYOR_POSTFIX, mayorPostfix);
        m.put(TownLevel.TOWN_BLOCK_LIMIT, townBlockLimit);
        m.put(TownLevel.UPKEEP_MULTIPLIER, townUpkeepMultiplier);
        m.put(TownLevel.OUTPOST_LIMIT, townOutpostLimit);
        m.put(TownLevel.TOWN_BLOCK_BUY_BONUS_LIMIT, townBlockBuyBonusLimit);
        TownySettings.configTownLevel.put(numResidents, m);
    }
    
    public static void newNationLevel(final int numResidents, final String namePrefix, final String namePostfix, final String capitalPrefix, final String capitalPostfix, final String kingPrefix, final String kingPostfix, final int townBlockLimitBonus, final double nationUpkeepMultiplier, final double nationTownUpkeepMultiplier, final int nationZonesSize, final int nationBonusOutpostLimit) {
        final ConcurrentHashMap<NationLevel, Object> m = new ConcurrentHashMap<NationLevel, Object>();
        m.put(NationLevel.NAME_PREFIX, namePrefix);
        m.put(NationLevel.NAME_POSTFIX, namePostfix);
        m.put(NationLevel.CAPITAL_PREFIX, capitalPrefix);
        m.put(NationLevel.CAPITAL_POSTFIX, capitalPostfix);
        m.put(NationLevel.KING_PREFIX, kingPrefix);
        m.put(NationLevel.KING_POSTFIX, kingPostfix);
        m.put(NationLevel.TOWN_BLOCK_LIMIT_BONUS, townBlockLimitBonus);
        m.put(NationLevel.UPKEEP_MULTIPLIER, nationUpkeepMultiplier);
        m.put(NationLevel.NATION_TOWN_UPKEEP_MULTIPLIER, nationTownUpkeepMultiplier);
        m.put(NationLevel.NATIONZONES_SIZE, nationZonesSize);
        m.put(NationLevel.NATION_BONUS_OUTPOST_LIMIT, nationBonusOutpostLimit);
        TownySettings.configNationLevel.put(numResidents, m);
    }
    
    public static void loadTownLevelConfig() throws IOException {
        final List<Map<?, ?>> levels = (List<Map<?, ?>>)TownySettings.config.getMapList("levels.town_level");
        for (final Map<?, ?> level : levels) {
            try {
                newTownLevel((int)level.get("numResidents"), (String)level.get("namePrefix"), (String)level.get("namePostfix"), (String)level.get("mayorPrefix"), (String)level.get("mayorPostfix"), (int)level.get("townBlockLimit"), (double)level.get("upkeepModifier"), (int)level.get("townOutpostLimit"), (int)level.get("townBlockBuyBonusLimit"));
            }
            catch (NullPointerException e) {
                System.out.println("Your Towny config.yml's town_level section is out of date.");
                System.out.println("This can be fixed automatically by deleting the town_level section and letting Towny remake it on the next startup.");
                throw new IOException("Config.yml town_levels incomplete.");
            }
        }
    }
    
    public static void loadNationLevelConfig() throws IOException {
        final List<Map<?, ?>> levels = (List<Map<?, ?>>)TownySettings.config.getMapList("levels.nation_level");
        for (final Map<?, ?> level : levels) {
            try {
                newNationLevel((int)level.get("numResidents"), (String)level.get("namePrefix"), (String)level.get("namePostfix"), (String)level.get("capitalPrefix"), (String)level.get("capitalPostfix"), (String)level.get("kingPrefix"), (String)level.get("kingPostfix"), level.containsKey("townBlockLimitBonus") ? ((int)level.get("townBlockLimitBonus")) : 0, (double)level.get("upkeepModifier"), level.containsKey("nationTownUpkeepModifier") ? ((double)level.get("nationTownUpkeepModifier")) : 1.0, (int)level.get("nationZonesSize"), (int)level.get("nationBonusOutpostLimit"));
            }
            catch (Exception e) {
                System.out.println("Your Towny config.yml's nation_level section is out of date.");
                System.out.println("This can be fixed automatically by deleting the nation_level section and letting Towny remake it on the next startup.");
                throw new IOException("Config.yml nation_levels incomplete.");
            }
        }
    }
    
    public static Map<TownLevel, Object> getTownLevel(final int numResidents) {
        return TownySettings.configTownLevel.get(numResidents);
    }
    
    public static Map<NationLevel, Object> getNationLevel(final int numResidents) {
        return TownySettings.configNationLevel.get(numResidents);
    }
    
    public static Map<TownLevel, Object> getTownLevel(final Town town) {
        return getTownLevel(calcTownLevel(town));
    }
    
    public static Map<NationLevel, Object> getNationLevel(final Nation nation) {
        return getNationLevel(calcNationLevel(nation));
    }
    
    public static int calcTownLevel(final Town town) {
        final int n = town.getNumResidents();
        for (final Integer level : TownySettings.configTownLevel.keySet()) {
            if (n >= level) {
                return level;
            }
        }
        return 0;
    }
    
    public static int calcNationLevel(final Nation nation) {
        final int n = nation.getNumResidents();
        for (final Integer level : TownySettings.configNationLevel.keySet()) {
            if (n >= level) {
                return level;
            }
        }
        return 0;
    }
    
    public static void loadConfig(final String filepath, final String version) throws IOException {
        if (FileMgmt.checkOrCreateFile(filepath)) {
            final File file = new File(filepath);
            TownySettings.config = new CommentedConfiguration(file);
            if (!TownySettings.config.load()) {
                System.out.print("Failed to load Config!");
            }
            setDefaults(version, file);
            TownySettings.config.save();
            loadCachedObjects();
        }
    }
    
    public static void loadPlayerMap(final String filepath) {
        if (FileMgmt.checkOrCreateFile(filepath)) {
            final File file = new File(filepath);
            TownySettings.playermap = new CommentedConfiguration(file);
            if (!TownySettings.playermap.load()) {
                System.out.println("Failed to load playermap!");
            }
        }
    }
    
    public static void loadCachedObjects() throws IOException {
        TownyWarConfig.setFlagBaseMaterial(Material.matchMaterial(getString(ConfigNodes.WAR_ENEMY_FLAG_BASE_BLOCK)));
        TownyWarConfig.setFlagLightMaterial(Material.matchMaterial(getString(ConfigNodes.WAR_ENEMY_FLAG_LIGHT_BLOCK)));
        TownyWarConfig.setBeaconWireFrameMaterial(Material.matchMaterial(getString(ConfigNodes.WAR_ENEMY_BEACON_WIREFRAME_BLOCK)));
        loadTownLevelConfig();
        loadNationLevelConfig();
        TownyWarConfig.setEditableMaterialsInWarZone(getAllowedMaterials(ConfigNodes.WAR_WARZONE_EDITABLE_MATERIALS));
        ChunkNotification.loadFormatStrings();
    }
    
    public static void loadLanguage(final String filepath, final String defaultRes) throws IOException {
        final String res = getString(ConfigNodes.LANGUAGE.getRoot(), defaultRes);
        final String fullPath = filepath + File.separator + res;
        final File file = FileMgmt.unpackResourceFile(fullPath, res, defaultRes);
        if (file != null) {
            (TownySettings.language = new CommentedConfiguration(file)).load();
            TownySettings.newLanguage = new CommentedConfiguration(file);
            try {
                TownySettings.newLanguage.loadFromString(FileMgmt.convertStreamToString("/" + res));
            }
            catch (IOException e) {
                TownyMessaging.sendMsg("Custom language file detected, not updating.");
                return;
            }
            catch (InvalidConfigurationException e2) {
                TownyMessaging.sendMsg("Invalid Configuration in language file detected.");
            }
            final String resVersion = TownySettings.newLanguage.getString("version");
            final String langVersion = getLangString("version");
            if (!langVersion.equalsIgnoreCase(resVersion)) {
                TownySettings.language = TownySettings.newLanguage;
                TownySettings.newLanguage = null;
                TownyMessaging.sendMsg("Newer language file available, language file updated.");
                FileMgmt.stringToFile(FileMgmt.convertStreamToString("/" + res), file);
            }
        }
    }
    
    private static void sendError(final String msg) {
        System.out.println("[Towny] Error could not read " + msg);
    }
    
    private static String[] parseString(final String str) {
        return parseSingleLineString(str).split("@");
    }
    
    public static String parseSingleLineString(final String str) {
        return str.replaceAll("&", "§");
    }
    
    public static TownSpawnLevel.SpawnLevel getSpawnLevel(final ConfigNodes node) {
        TownSpawnLevel.SpawnLevel level = TownSpawnLevel.SpawnLevel.valueOf(TownySettings.config.getString(node.getRoot()).toUpperCase());
        if (level == null) {
            level = TownSpawnLevel.SpawnLevel.valueOf(node.getDefault().toUpperCase());
        }
        return level;
    }
    
    public static NationSpawnLevel.NSpawnLevel getNSpawnLevel(final ConfigNodes node) {
        NationSpawnLevel.NSpawnLevel level = NationSpawnLevel.NSpawnLevel.valueOf(TownySettings.config.getString(node.getRoot()).toUpperCase());
        if (level == null) {
            level = NationSpawnLevel.NSpawnLevel.valueOf(node.getDefault().toUpperCase());
        }
        return level;
    }
    
    public static boolean getBoolean(final ConfigNodes node) {
        return Boolean.parseBoolean(TownySettings.config.getString(node.getRoot().toLowerCase(), node.getDefault()));
    }
    
    public static double getDouble(final ConfigNodes node) {
        try {
            return Double.parseDouble(TownySettings.config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
        }
        catch (NumberFormatException e) {
            sendError(node.getRoot().toLowerCase() + " from config.yml");
            return 0.0;
        }
    }
    
    public static int getInt(final ConfigNodes node) {
        try {
            return Integer.parseInt(TownySettings.config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
        }
        catch (NumberFormatException e) {
            sendError(node.getRoot().toLowerCase() + " from config.yml");
            return 0;
        }
    }
    
    public static String getString(final ConfigNodes node) {
        return TownySettings.config.getString(node.getRoot().toLowerCase(), node.getDefault());
    }
    
    public static String getString(final String root, final String def) {
        final String data = TownySettings.config.getString(root.toLowerCase(), def);
        if (data == null) {
            sendError(root.toLowerCase() + " from config.yml");
            return "";
        }
        return data;
    }
    
    public static String getLangString(final String root) {
        final String data = TownySettings.language.getString(root.toLowerCase());
        if (data == null) {
            sendError(root.toLowerCase() + " from " + TownySettings.config.getString("language"));
            return "";
        }
        return parseSingleLineString(data);
    }
    
    public static String getConfigLang(final ConfigNodes node) {
        return parseSingleLineString(getString(node));
    }
    
    public static List<Integer> getIntArr(final ConfigNodes node) {
        final String[] strArray = getString(node.getRoot(), node.getDefault()).split(",");
        final List<Integer> list = new ArrayList<Integer>();
        if (strArray != null) {
            for (final String aStrArray : strArray) {
                if (aStrArray != null) {
                    try {
                        list.add(Integer.parseInt(aStrArray.trim()));
                    }
                    catch (NumberFormatException e) {
                        sendError(node.getRoot().toLowerCase() + " from config.yml");
                    }
                }
            }
        }
        return list;
    }
    
    public static List<String> getStrArr(final ConfigNodes node) {
        final String[] strArray = getString(node.getRoot().toLowerCase(), node.getDefault()).split(",");
        final List<String> list = new ArrayList<String>();
        if (strArray != null) {
            for (final String aStrArray : strArray) {
                if (aStrArray != null) {
                    list.add(aStrArray.trim());
                }
            }
        }
        return list;
    }
    
    public static long getSeconds(final ConfigNodes node) {
        try {
            return TimeTools.getSeconds(getString(node));
        }
        catch (NumberFormatException e) {
            sendError(node.getRoot().toLowerCase() + " from config.yml");
            return 1L;
        }
    }
    
    public static Set<Material> getAllowedMaterials(final ConfigNodes node) {
        final Set<Material> allowedMaterials = new HashSet<Material>();
        for (final String material : getStrArr(node)) {
            if (material.equals("*")) {
                allowedMaterials.addAll(Arrays.asList(Material.values()));
            }
            else if (material.startsWith("-")) {
                allowedMaterials.remove(Material.matchMaterial(material));
            }
            else {
                allowedMaterials.add(Material.matchMaterial(material));
            }
        }
        return allowedMaterials;
    }
    
    public static void addComment(final String root, final String... comments) {
        TownySettings.newConfig.addComment(root.toLowerCase(), comments);
    }
    
    private static void setDefaults(final String version, final File file) {
        (TownySettings.newConfig = new CommentedConfiguration(file)).load();
        for (final ConfigNodes root : ConfigNodes.values()) {
            if (root.getComments().length > 0) {
                addComment(root.getRoot(), root.getComments());
            }
            if (root.getRoot() == ConfigNodes.LEVELS.getRoot()) {
                setDefaultLevels();
            }
            else if (root.getRoot() != ConfigNodes.LEVELS_TOWN_LEVEL.getRoot()) {
                if (root.getRoot() != ConfigNodes.LEVELS_NATION_LEVEL.getRoot()) {
                    if (root.getRoot() == ConfigNodes.VERSION.getRoot()) {
                        setNewProperty(root.getRoot(), version);
                    }
                    else if (root.getRoot() == ConfigNodes.LAST_RUN_VERSION.getRoot()) {
                        setNewProperty(root.getRoot(), getLastRunVersion(version));
                    }
                    else if (root.getRoot() == ConfigNodes.PROT_ITEM_USE_MAT.getRoot()) {
                        setNewProperty(root.getRoot(), convertIds(getStrArr(ConfigNodes.PROT_ITEM_USE_MAT)));
                    }
                    else if (root.getRoot() == ConfigNodes.PROT_SWITCH_MAT.getRoot()) {
                        setNewProperty(root.getRoot(), convertIds(getStrArr(ConfigNodes.PROT_SWITCH_MAT)));
                    }
                    else if (root.getRoot() == ConfigNodes.NWS_PLOT_MANAGEMENT_DELETE.getRoot()) {
                        setNewProperty(root.getRoot(), convertIds(getStrArr(ConfigNodes.NWS_PLOT_MANAGEMENT_DELETE)));
                    }
                    else if (root.getRoot() == ConfigNodes.NWS_PLOT_MANAGEMENT_REVERT_IGNORE.getRoot()) {
                        setNewProperty(root.getRoot(), convertIds(getStrArr(ConfigNodes.NWS_PLOT_MANAGEMENT_REVERT_IGNORE)));
                    }
                    else if (root.getRoot() == ConfigNodes.UNCLAIMED_ZONE_IGNORE.getRoot()) {
                        setNewProperty(root.getRoot(), convertIds(getStrArr(ConfigNodes.UNCLAIMED_ZONE_IGNORE)));
                    }
                    else {
                        setNewProperty(root.getRoot(), (TownySettings.config.get(root.getRoot().toLowerCase()) != null) ? TownySettings.config.get(root.getRoot().toLowerCase()) : root.getDefault());
                    }
                }
            }
        }
        TownySettings.config = TownySettings.newConfig;
        TownySettings.newConfig = null;
    }
    
    private static String convertIds(final List<String> list) {
        final List<String> newValues = new ArrayList<String>();
        for (final String id : list) {
            try {
                final int value = Integer.parseInt(id);
                newValues.add(BukkitTools.getMaterial(value).name());
            }
            catch (NumberFormatException e) {
                newValues.add(id);
            }
            catch (NullPointerException e2) {
                if (!id.startsWith("X")) {
                    newValues.add("X" + id);
                }
                else {
                    newValues.add(id);
                }
            }
        }
        return StringMgmt.join(newValues, ",");
    }
    
    private static void setDefaultLevels() {
        addComment(ConfigNodes.LEVELS_TOWN_LEVEL.getRoot(), "# default Town levels.");
        if (!TownySettings.config.contains(ConfigNodes.LEVELS_TOWN_LEVEL.getRoot())) {
            final List<Map<String, Object>> levels = new ArrayList<Map<String, Object>>();
            final Map<String, Object> level = new HashMap<String, Object>();
            level.put("numResidents", 0);
            level.put("namePrefix", "");
            level.put("namePostfix", " Ruins");
            level.put("mayorPrefix", "Spirit ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 1);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 0);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 1);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Settlement)");
            level.put("mayorPrefix", "Hermit ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 16);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 0);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 2);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Hamlet)");
            level.put("mayorPrefix", "Chief ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 32);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 1);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 6);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Village)");
            level.put("mayorPrefix", "Baron Von ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 96);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 1);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 10);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Town)");
            level.put("mayorPrefix", "Viscount ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 160);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 2);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 14);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Large Town)");
            level.put("mayorPrefix", "Count Von ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 224);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 2);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 20);
            level.put("namePrefix", "");
            level.put("namePostfix", " (City)");
            level.put("mayorPrefix", "Earl ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 320);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 3);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 24);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Large City)");
            level.put("mayorPrefix", "Duke ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 384);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 3);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 28);
            level.put("namePrefix", "");
            level.put("namePostfix", " (Metropolis)");
            level.put("mayorPrefix", "Lord ");
            level.put("mayorPostfix", "");
            level.put("townBlockLimit", 448);
            level.put("upkeepModifier", 1.0);
            level.put("townOutpostLimit", 4);
            level.put("townBlockBuyBonusLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            TownySettings.newConfig.set(ConfigNodes.LEVELS_TOWN_LEVEL.getRoot(), (Object)levels);
        }
        else {
            TownySettings.newConfig.set(ConfigNodes.LEVELS_TOWN_LEVEL.getRoot(), TownySettings.config.get(ConfigNodes.LEVELS_TOWN_LEVEL.getRoot()));
        }
        addComment(ConfigNodes.LEVELS_NATION_LEVEL.getRoot(), "# default Nation levels.");
        if (!TownySettings.config.contains(ConfigNodes.LEVELS_NATION_LEVEL.getRoot())) {
            final List<Map<String, Object>> levels = new ArrayList<Map<String, Object>>();
            final Map<String, Object> level = new HashMap<String, Object>();
            level.put("numResidents", 0);
            level.put("namePrefix", "Land of ");
            level.put("namePostfix", " (Nation)");
            level.put("capitalPrefix", "");
            level.put("capitalPostfix", "");
            level.put("kingPrefix", "Leader ");
            level.put("kingPostfix", "");
            level.put("townBlockLimitBonus", 10);
            level.put("upkeepModifier", 1.0);
            level.put("nationTownUpkeepModifier", 1.0);
            level.put("nationZonesSize", 1);
            level.put("nationBonusOutpostLimit", 0);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 10);
            level.put("namePrefix", "Federation of ");
            level.put("namePostfix", " (Nation)");
            level.put("capitalPrefix", "");
            level.put("capitalPostfix", "");
            level.put("kingPrefix", "Count ");
            level.put("kingPostfix", "");
            level.put("townBlockLimitBonus", 20);
            level.put("upkeepModifier", 1.0);
            level.put("nationTownUpkeepModifier", 1.0);
            level.put("nationZonesSize", 1);
            level.put("nationBonusOutpostLimit", 1);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 20);
            level.put("namePrefix", "Dominion of ");
            level.put("namePostfix", " (Nation)");
            level.put("capitalPrefix", "");
            level.put("capitalPostfix", "");
            level.put("kingPrefix", "Duke ");
            level.put("kingPostfix", "");
            level.put("townBlockLimitBonus", 40);
            level.put("upkeepModifier", 1.0);
            level.put("nationTownUpkeepModifier", 1.0);
            level.put("nationZonesSize", 1);
            level.put("nationBonusOutpostLimit", 2);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 30);
            level.put("namePrefix", "Kingdom of ");
            level.put("namePostfix", " (Nation)");
            level.put("capitalPrefix", "");
            level.put("capitalPostfix", "");
            level.put("kingPrefix", "King ");
            level.put("kingPostfix", "");
            level.put("townBlockLimitBonus", 60);
            level.put("upkeepModifier", 1.0);
            level.put("nationTownUpkeepModifier", 1.0);
            level.put("nationZonesSize", 2);
            level.put("nationBonusOutpostLimit", 3);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 40);
            level.put("namePrefix", "The ");
            level.put("namePostfix", " Empire");
            level.put("capitalPrefix", "");
            level.put("capitalPostfix", "");
            level.put("kingPrefix", "Emperor ");
            level.put("kingPostfix", "");
            level.put("townBlockLimitBonus", 100);
            level.put("upkeepModifier", 1.0);
            level.put("nationTownUpkeepModifier", 1.0);
            level.put("nationZonesSize", 2);
            level.put("nationBonusOutpostLimit", 4);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            level.put("numResidents", 60);
            level.put("namePrefix", "The ");
            level.put("namePostfix", " Realm");
            level.put("capitalPrefix", "");
            level.put("capitalPostfix", "");
            level.put("kingPrefix", "God Emperor ");
            level.put("kingPostfix", "");
            level.put("townBlockLimitBonus", 140);
            level.put("upkeepModifier", 1.0);
            level.put("nationTownUpkeepModifier", 1.0);
            level.put("nationZonesSize", 3);
            level.put("nationBonusOutpostLimit", 5);
            levels.add(new HashMap<String, Object>(level));
            level.clear();
            TownySettings.newConfig.set(ConfigNodes.LEVELS_NATION_LEVEL.getRoot(), (Object)levels);
        }
        else {
            TownySettings.newConfig.set(ConfigNodes.LEVELS_NATION_LEVEL.getRoot(), TownySettings.config.get(ConfigNodes.LEVELS_NATION_LEVEL.getRoot()));
        }
    }
    
    public static String[] getRegistrationMsg(final String name) {
        return parseString(String.format(getLangString("MSG_REGISTRATION"), name));
    }
    
    public static String[] getNewTownMsg(final String who, final String town) {
        return parseString(String.format(getLangString("MSG_NEW_TOWN"), who, town));
    }
    
    public static String[] getNewNationMsg(final String who, final String nation) {
        return parseString(String.format(getLangString("MSG_NEW_NATION"), who, nation));
    }
    
    public static String[] getJoinTownMsg(final String who) {
        return parseString(String.format(getLangString("MSG_JOIN_TOWN"), who));
    }
    
    public static String[] getJoinNationMsg(final String who) {
        return parseString(String.format(getLangString("MSG_JOIN_NATION"), who));
    }
    
    public static String[] getNewMayorMsg(final String who) {
        return parseString(String.format(getLangString("MSG_NEW_MAYOR"), who));
    }
    
    public static String[] getNewKingMsg(final String who, final String nation) {
        return parseString(String.format(getLangString("MSG_NEW_KING"), who, nation));
    }
    
    public static String[] getJoinWarMsg(final TownyObject obj) {
        return parseString(String.format(getLangString("MSG_WAR_JOIN"), obj.getName()));
    }
    
    public static String[] getWarTimeEliminatedMsg(final String who) {
        return parseString(String.format(getLangString("MSG_WAR_ELIMINATED"), who));
    }
    
    public static String[] getWarTimeForfeitMsg(final String who) {
        return parseString(String.format(getLangString("MSG_WAR_FORFEITED"), who));
    }
    
    public static String[] getWarTimeLoseTownBlockMsg(final WorldCoord worldCoord, final String town) {
        return parseString(String.format(getLangString("MSG_WAR_LOSE_BLOCK"), worldCoord.toString(), town));
    }
    
    public static String[] getWarTimeScoreMsg(final Town town, final int n) {
        return parseString(String.format(getLangString("MSG_WAR_SCORE"), town.getName(), n));
    }
    
    public static String[] getWarTimeScoreNationEliminatedMsg(final Town town, final int n, final Nation fallenNation) {
        return parseString(String.format(getLangString("MSG_WAR_SCORE_NATION_ELIM"), town.getName(), n, fallenNation.getName()));
    }
    
    public static String[] getWarTimeScoreTownEliminatedMsg(final Town town, final int n, final Town fallenTown, final int fallenTownBlocks) {
        return parseString(String.format(getLangString("MSG_WAR_SCORE_TOWN_ELIM"), town.getName(), n, fallenTown.getName(), fallenTownBlocks));
    }
    
    public static String[] getWarTimeScoreTownBlockEliminatedMsg(final Town town, final int n, final TownBlock fallenTownBlock) {
        String townBlockName = "";
        try {
            final Town fallenTown = fallenTownBlock.getTown();
            townBlockName = "[" + fallenTown.getName() + "](" + fallenTownBlock.getCoord().toString() + ")";
        }
        catch (NotRegisteredException e) {
            townBlockName = "(" + fallenTownBlock.getCoord().toString() + ")";
        }
        return parseString(String.format(getLangString("MSG_WAR_SCORE_TOWNBLOCK_ELIM"), town.getName(), n, townBlockName));
    }
    
    public static String[] getWarTimeScorePlayerKillMsg(final Player attacker, final Player dead, final int n, final Town attackingTown) {
        return parseString(String.format(getLangString("MSG_WAR_SCORE_PLAYER_KILL"), attacker.getName(), dead.getName(), n, attackingTown.getName()));
    }
    
    public static String[] getWarTimeScorePlayerKillMsg(final Player attacker, final Player dead, final Player defender, final int n, final Town attackingTown) {
        return parseString(String.format(getLangString("MSG_WAR_SCORE_PLAYER_KILL_DEFENDING"), attacker.getName(), dead.getName(), defender.getName(), n, attackingTown.getName()));
    }
    
    public static String[] getWarTimeKingKilled(final Nation kingsNation) {
        return parseString(String.format(getLangString("MSG_WAR_KING_KILLED"), kingsNation.getName()));
    }
    
    public static String[] getWarTimeMayorKilled(final Town mayorsTown) {
        return parseString(String.format(getLangString("MSG_WAR_MAYOR_KILLED"), mayorsTown.getName()));
    }
    
    public static String[] getWarTimeWinningNationSpoilsMsg(final Nation winningNation, final String money) {
        return parseString(String.format(getLangString("MSG_WAR_WINNING_NATION_SPOILS"), winningNation.getName(), money));
    }
    
    public static String[] getWarTimeWinningTownSpoilsMsg(final Town winningTown, final String money, final int score) {
        return parseString(String.format(getLangString("MSG_WAR_WINNING_TOWN_SPOILS"), winningTown.getName(), money, score));
    }
    
    public static String[] getCouldntPayTaxesMsg(final TownyObject obj, final String type) {
        return parseString(String.format(getLangString("MSG_COULDNT_PAY_TAXES"), obj.getName(), type));
    }
    
    public static String getPayedTownTaxMsg() {
        return getLangString("MSG_PAYED_TOWN_TAX");
    }
    
    public static String getPayedResidentTaxMsg() {
        return getLangString("MSG_PAYED_RESIDENT_TAX");
    }
    
    public static String getTaxExemptMsg() {
        return getLangString("MSG_TAX_EXEMPT");
    }
    
    public static String[] getDelResidentMsg(final Resident resident) {
        return parseString(String.format(getLangString("MSG_DEL_RESIDENT"), resident.getName()));
    }
    
    public static String[] getDelTownMsg(final Town town) {
        return parseString(String.format(getLangString("MSG_DEL_TOWN"), town.getName()));
    }
    
    public static String[] getDelNationMsg(final Nation nation) {
        return parseString(String.format(getLangString("MSG_DEL_NATION"), nation.getName()));
    }
    
    public static String[] getBuyResidentPlotMsg(final String who, final String owner, final Double price) {
        return parseString(String.format(getLangString("MSG_BUY_RESIDENT_PLOT"), who, owner, price));
    }
    
    public static String[] getPlotForSaleMsg(final String who, final WorldCoord worldCoord) {
        return parseString(String.format(getLangString("MSG_PLOT_FOR_SALE"), who, worldCoord.toString()));
    }
    
    public static String getMayorAbondonMsg() {
        return parseSingleLineString(getLangString("MSG_MAYOR_ABANDON"));
    }
    
    public static String getNotPermToNewTownLine() {
        return parseSingleLineString(getLangString("MSG_ADMIN_ONLY_CREATE_TOWN"));
    }
    
    public static String getNotPermToNewNationLine() {
        return parseSingleLineString(getLangString("MSG_ADMIN_ONLY_CREATE_NATION"));
    }
    
    public static String getKingPrefix(final Resident resident) {
        try {
            return (String) getNationLevel(resident.getTown().getNation()).get(NationLevel.KING_PREFIX);
        }
        catch (NotRegisteredException e) {
            sendError("getKingPrefix.");
            return "";
        }
    }
    
    public static String getMayorPrefix(final Resident resident) {
        try {
        	return (String) getTownLevel(resident.getTown()).get(TownySettings.TownLevel.MAYOR_PREFIX);
        }
        catch (NotRegisteredException e) {
            sendError("getMayorPrefix.");
            return "";
        }
    }
    
    public static String getCapitalPostfix(final Town town) {
        try {
            return ChatColor.translateAlternateColorCodes('&', (String)getNationLevel(town.getNation()).get(NationLevel.CAPITAL_POSTFIX));
        }
        catch (NotRegisteredException e) {
            sendError("getCapitalPostfix.");
            return "";
        }
    }
    
    public static String getTownPostfix(final Town town) {
        try {
            return ChatColor.translateAlternateColorCodes('&', (String)getTownLevel(town).get(TownLevel.NAME_POSTFIX));
        }
        catch (Exception e) {
            sendError("getTownPostfix.");
            return "";
        }
    }
    
    public static String getNationPostfix(final Nation nation) {
        try {
            return ChatColor.translateAlternateColorCodes('&', (String)getNationLevel(nation).get(NationLevel.NAME_POSTFIX));
        }
        catch (Exception e) {
            sendError("getNationPostfix.");
            return "";
        }
    }
    
    public static String getNationPrefix(final Nation nation) {
        try {
            return ChatColor.translateAlternateColorCodes('&', (String)getNationLevel(nation).get(NationLevel.NAME_PREFIX));
        }
        catch (Exception e) {
            sendError("getNationPrefix.");
            return "";
        }
    }
    
    public static String getTownPrefix(final Town town) {
        try {
            return ChatColor.translateAlternateColorCodes('&', (String)getTownLevel(town).get(TownLevel.NAME_PREFIX));
        }
        catch (Exception e) {
            sendError("getTownPrefix.");
            return "";
        }
    }
    
    public static String getCapitalPrefix(final Town town) {
        try {
            return ChatColor.translateAlternateColorCodes('&', (String)getNationLevel(town.getNation()).get(NationLevel.CAPITAL_PREFIX));
        }
        catch (NotRegisteredException e) {
            sendError("getCapitalPrefix.");
            return "";
        }
    }
    
    public static String getKingPostfix(final Resident resident) {
        try {
            return (String) getNationLevel(resident.getTown().getNation()).get(NationLevel.KING_POSTFIX);
        }
        catch (NotRegisteredException e) {
            sendError("getKingPostfix.");
            return "";
        }
    }
    
    public static String getMayorPostfix(final Resident resident) {
        try {
            return (String) getTownLevel(resident.getTown()).get(TownLevel.MAYOR_POSTFIX);
        }
        catch (NotRegisteredException e) {
            sendError("getMayorPostfix.");
            return "";
        }
    }
    
    public static String getNPCPrefix() {
        return getString(ConfigNodes.FILTERS_NPC_PREFIX.getRoot(), ConfigNodes.FILTERS_NPC_PREFIX.getDefault());
    }
    
    public static long getInactiveAfter() {
        return getSeconds(ConfigNodes.RES_SETTING_INACTIVE_AFTER_TIME);
    }
    
    public static boolean getBedUse() {
        return getBoolean(ConfigNodes.RES_SETTING_DENY_BED_USE);
    }
    
    public static String getLoadDatabase() {
        return getString(ConfigNodes.PLUGIN_DATABASE_LOAD);
    }
    
    public static String getSaveDatabase() {
        return getString(ConfigNodes.PLUGIN_DATABASE_SAVE);
    }
    
    public static String getSQLHostName() {
        return getString(ConfigNodes.PLUGIN_DATABASE_HOSTNAME);
    }
    
    public static String getSQLPort() {
        return getString(ConfigNodes.PLUGIN_DATABASE_PORT);
    }
    
    public static String getSQLDBName() {
        return getString(ConfigNodes.PLUGIN_DATABASE_DBNAME);
    }
    
    public static String getSQLTablePrefix() {
        return getString(ConfigNodes.PLUGIN_DATABASE_TABLEPREFIX);
    }
    
    public static String getSQLUsername() {
        return getString(ConfigNodes.PLUGIN_DATABASE_USERNAME);
    }
    
    public static String getSQLPassword() {
        return getString(ConfigNodes.PLUGIN_DATABASE_PASSWORD);
    }
    
    public static boolean getSQLUsingSSL() {
        return getBoolean(ConfigNodes.PLUGIN_DATABASE_SSL);
    }
    
    public static int getMaxTownBlocks(final Town town) {
        final int ratio = getTownBlockRatio();
        int n = town.getBonusBlocks() + town.getPurchasedBlocks();
        if (ratio == 0) {
        	n += (Integer) getTownLevel(town).get(TownySettings.TownLevel.TOWN_BLOCK_LIMIT);
        }
        else {
            n += town.getNumResidents() * ratio;
        }
        if (town.hasNation()) {
            try {
            	n += (Integer) getNationLevel(town.getNation()).get(TownySettings.NationLevel.TOWN_BLOCK_LIMIT_BONUS);
            }
            catch (NotRegisteredException ex) {}
        }
        return n;
    }
    
    public static int getMaxOutposts(final Town town) {
    	int townOutposts = (Integer) getTownLevel(town).get(TownySettings.TownLevel.OUTPOST_LIMIT);
        int nationOutposts = 0;
        if (town.hasNation()) {
            try {
    			nationOutposts = (Integer) getNationLevel(town.getNation()).get(TownySettings.NationLevel.NATION_BONUS_OUTPOST_LIMIT);
            }
            catch (NotRegisteredException ex) {}
        }
        final int n = townOutposts + nationOutposts;
        return n;
    }
    
    public static int getMaxBonusBlocks(final Town town) {
        return (Integer) getTownLevel(town).get(TownLevel.TOWN_BLOCK_BUY_BONUS_LIMIT);
    }
    
    public static int getNationBonusBlocks(final Nation nation) {
        return (Integer) getNationLevel(nation).get(NationLevel.TOWN_BLOCK_LIMIT_BONUS);
    }
    
    public static int getNationBonusBlocks(final Town town) {
        if (town.hasNation()) {
            try {
                return getNationBonusBlocks(town.getNation());
            }
            catch (NotRegisteredException ex) {}
        }
        return 0;
    }
    
    public static int getTownBlockRatio() {
        return getInt(ConfigNodes.TOWN_TOWN_BLOCK_RATIO);
    }
    
    public static int getTownBlockSize() {
        return getInt(ConfigNodes.TOWN_TOWN_BLOCK_SIZE);
    }
    
    public static boolean getFriendlyFire() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_FRIENDLY_FIRE);
    }
    
    public static boolean isUsingEconomy() {
        return getBoolean(ConfigNodes.PLUGIN_USING_ECONOMY);
    }
    
    public static boolean isFakeResident(final String name) {
        return getString(ConfigNodes.PLUGIN_MODS_FAKE_RESIDENTS).toLowerCase().contains(name.toLowerCase());
    }
    
    public static boolean isUsingEssentials() {
        return getBoolean(ConfigNodes.PLUGIN_USING_ESSENTIALS);
    }
    
    public static void setUsingEssentials(final boolean newSetting) {
        setProperty(ConfigNodes.PLUGIN_USING_ESSENTIALS.getRoot(), newSetting);
    }
    
    public static double getNewTownPrice() {
        return getDouble(ConfigNodes.ECO_PRICE_NEW_TOWN);
    }
    
    public static double getNewNationPrice() {
        return getDouble(ConfigNodes.ECO_PRICE_NEW_NATION);
    }
    
    public static boolean getUnclaimedZoneBuildRights() {
        return getBoolean(ConfigNodes.UNCLAIMED_ZONE_BUILD);
    }
    
    public static boolean getUnclaimedZoneDestroyRights() {
        return getBoolean(ConfigNodes.UNCLAIMED_ZONE_DESTROY);
    }
    
    public static boolean getUnclaimedZoneItemUseRights() {
        return getBoolean(ConfigNodes.UNCLAIMED_ZONE_ITEM_USE);
    }
    
    public static boolean getDebug() {
        return getBoolean(ConfigNodes.PLUGIN_DEBUG_MODE);
    }
    
    public static String getTool() {
        return getString(ConfigNodes.PLUGIN_INFO_TOOL);
    }
    
    public static void setDebug(final boolean b) {
        setProperty(ConfigNodes.PLUGIN_DEBUG_MODE.getRoot(), b);
    }
    
    public static boolean getShowTownNotifications() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_SHOW_TOWN_NOTIFICATIONS);
    }
    
    public static boolean isNotificationOwnerShowingNationTitles() {
        return getBoolean(ConfigNodes.NOTIFICATION_OWNER_SHOWS_NATION_TITLE);
    }
    
    public static boolean isNotificationsAppearingInActionBar() {
        return getBoolean(ConfigNodes.NOTIFICATION_NOTIFICATIONS_APPEAR_IN_ACTION_BAR);
    }
    
    public static boolean getShowTownBoardOnLogin() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_DISPLAY_TOWNBOARD_ONLOGIN);
    }
    
    public static boolean getShowNationBoardOnLogin() {
        return getBoolean(ConfigNodes.GNATION_SETTINGS_DISPLAY_NATIONBOARD_ONLOGIN);
    }
    
    public static String getUnclaimedZoneName() {
        return getLangString("UNCLAIMED_ZONE_NAME");
    }
    
    public static int getMaxTitleLength() {
        return getInt(ConfigNodes.FILTERS_MODIFY_CHAT_MAX_LGTH);
    }
    
    public static int getMaxNameLength() {
        return getInt(ConfigNodes.FILTERS_MAX_NAME_LGTH);
    }
    
    public static long getDeleteTime() {
        return getSeconds(ConfigNodes.RES_SETTING_DELETE_OLD_RESIDENTS_TIME);
    }
    
    public static boolean isDeleteEcoAccount() {
        return getBoolean(ConfigNodes.RES_SETTING_DELETE_OLD_RESIDENTS_ECO);
    }
    
    public static boolean isDeleteTownlessOnly() {
        return getBoolean(ConfigNodes.RES_SETTING_DELETE_OLD_RESIDENTS_TOWNLESS_ONLY);
    }
    
    public static boolean isDeletingOldResidents() {
        return getBoolean(ConfigNodes.RES_SETTING_DELETE_OLD_RESIDENTS_ENABLE);
    }
    
    public static int getWarTimeWarningDelay() {
        return getInt(ConfigNodes.WAR_EVENT_WARNING_DELAY);
    }
    
    public static boolean isWarTimeTownsNeutral() {
        return getBoolean(ConfigNodes.WAR_EVENT_TOWNS_NEUTRAL);
    }
    
    public static boolean isAllowWarBlockGriefing() {
        return getBoolean(ConfigNodes.WAR_EVENT_BLOCK_GRIEFING);
    }
    
    public static int getWarzoneTownBlockHealth() {
        return getInt(ConfigNodes.WAR_EVENT_TOWN_BLOCK_HP);
    }
    
    public static int getWarzoneHomeBlockHealth() {
        return getInt(ConfigNodes.WAR_EVENT_HOME_BLOCK_HP);
    }
    
    public static String[] getWarTimeLoseTownBlockMsg(final WorldCoord worldCoord) {
        return getWarTimeLoseTownBlockMsg(worldCoord, "");
    }
    
    public static String getDefaultTownName() {
        return getString(ConfigNodes.RES_SETTING_DEFAULT_TOWN_NAME);
    }
    
    public static int getWarPointsForTownBlock() {
        return getInt(ConfigNodes.WAR_EVENT_POINTS_TOWNBLOCK);
    }
    
    public static int getWarPointsForTown() {
        return getInt(ConfigNodes.WAR_EVENT_POINTS_TOWN);
    }
    
    public static int getWarPointsForNation() {
        return getInt(ConfigNodes.WAR_EVENT_POINTS_NATION);
    }
    
    public static int getWarPointsForKill() {
        return getInt(ConfigNodes.WAR_EVENT_POINTS_KILL);
    }
    
    public static int getMinWarHeight() {
        return getInt(ConfigNodes.WAR_EVENT_MIN_HEIGHT);
    }
    
    public static List<String> getWorldMobRemovalEntities() {
        if (getDebug()) {
            System.out.println("[Towny] Debug: Reading World Mob removal entities. ");
        }
        return getStrArr(ConfigNodes.PROT_MOB_REMOVE_WORLD);
    }
    
    public static List<String> getTownMobRemovalEntities() {
        if (getDebug()) {
            System.out.println("[Towny] Debug: Reading Town Mob removal entities. ");
        }
        return getStrArr(ConfigNodes.PROT_MOB_REMOVE_TOWN);
    }
    
    public static boolean isEconomyAsync() {
        return getBoolean(ConfigNodes.ECO_USE_ASYNC);
    }
    
    public static boolean isRemovingVillagerBabiesWorld() {
        return getBoolean(ConfigNodes.PROT_MOB_REMOVE_VILLAGER_BABIES_WORLD);
    }
    
    public static boolean isCreatureTriggeringPressurePlateDisabled() {
        return getBoolean(ConfigNodes.PROT_MOB_DISABLE_TRIGGER_PRESSURE_PLATE_STONE);
    }
    
    public static boolean isRemovingVillagerBabiesTown() {
        return getBoolean(ConfigNodes.PROT_MOB_REMOVE_VILLAGER_BABIES_TOWN);
    }
    
    public static List<String> getWildExplosionProtectionEntities() {
        if (getDebug()) {
            System.out.println("[Towny] Debug: Wilderness explosion protection entities. ");
        }
        return getStrArr(ConfigNodes.NWS_PLOT_MANAGEMENT_WILD_ENTITY_REVERT_LIST);
    }
    
    public static long getMobRemovalSpeed() {
        return getSeconds(ConfigNodes.PROT_MOB_REMOVE_SPEED);
    }
    
    public static long getHealthRegenSpeed() {
        return getSeconds(ConfigNodes.GTOWN_SETTINGS_REGEN_SPEED);
    }
    
    public static boolean hasHealthRegen() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_REGEN_ENABLE);
    }
    
    public static boolean getTownDefaultPublic() {
        return getBoolean(ConfigNodes.TOWN_DEF_PUBLIC);
    }
    
    public static boolean getTownDefaultOpen() {
        return getBoolean(ConfigNodes.TOWN_DEF_OPEN);
    }
    
    public static boolean getNationDefaultOpen() {
        return getBoolean(ConfigNodes.GNATION_DEF_OPEN);
    }
    
    public static double getTownDefaultTax() {
        return getDouble(ConfigNodes.TOWN_DEF_TAXES_TAX);
    }
    
    public static double getTownDefaultShopTax() {
        return getDouble(ConfigNodes.TOWN_DEF_TAXES_SHOP_TAX);
    }
    
    public static double getTownDefaultEmbassyTax() {
        return getDouble(ConfigNodes.TOWN_DEF_TAXES_EMBASSY_TAX);
    }
    
    public static double getTownDefaultPlotTax() {
        return getDouble(ConfigNodes.TOWN_DEF_TAXES_PLOT_TAX);
    }
    
    public static boolean getTownDefaultTaxPercentage() {
        return getBoolean(ConfigNodes.TOWN_DEF_TAXES_TAXPERCENTAGE);
    }
    
    public static double getTownDefaultTaxMinimumTax() {
        return getDouble(ConfigNodes.TOWN_DEF_TAXES_MINIMUMTAX);
    }
    
    public static boolean hasTownLimit() {
        return getTownLimit() != 0;
    }
    
    public static int getTownLimit() {
        return getInt(ConfigNodes.TOWN_LIMIT);
    }
    
    public static int getMaxPurchedBlocks(final Town town) {
        if (isBonusBlocksPerTownLevel()) {
            return getMaxBonusBlocks(town);
        }
        return getInt(ConfigNodes.TOWN_MAX_PURCHASED_BLOCKS);
    }
    
    public static int getMaxClaimRadiusValue() {
        return getInt(ConfigNodes.TOWN_MAX_CLAIM_RADIUS_VALUE);
    }
    
    public static boolean isSellingBonusBlocks(final Town town) {
        return getMaxPurchedBlocks(town) != 0;
    }
    
    public static boolean isBonusBlocksPerTownLevel() {
        return getBoolean(ConfigNodes.TOWN_MAX_PURCHASED_BLOCKS_USES_TOWN_LEVELS);
    }
    
    public static double getPurchasedBonusBlocksCost() {
        return getDouble(ConfigNodes.ECO_PRICE_PURCHASED_BONUS_TOWNBLOCK);
    }
    
    public static double getPurchasedBonusBlocksIncreaseValue() {
        return getDouble(ConfigNodes.ECO_PRICE_PURCHASED_BONUS_TOWNBLOCK_INCREASE);
    }
    
    public static boolean isTownSpawnPaidToTown() {
        return getBoolean(ConfigNodes.ECO_PRICE_TOWN_SPAWN_PAID_TO_TOWN);
    }
    
    public static double getNationNeutralityCost() {
        return getDouble(ConfigNodes.ECO_PRICE_NATION_NEUTRALITY);
    }
    
    public static boolean isAllowingOutposts() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_ALLOW_OUTPOSTS);
    }
    
    public static boolean isOutpostsLimitedByLevels() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_LIMIT_OUTPOST_USING_LEVELS);
    }
    
    public static boolean isOutpostLimitStoppingTeleports() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_OVER_OUTPOST_LIMIT_STOP_TELEPORT);
    }
    
    public static double getOutpostCost() {
        return getDouble(ConfigNodes.ECO_PRICE_OUTPOST);
    }
    
    private static List<String> getSwitchMaterials() {
        return getStrArr(ConfigNodes.PROT_SWITCH_MAT);
    }
    
    private static List<String> getItemUseMaterials() {
        return getStrArr(ConfigNodes.PROT_ITEM_USE_MAT);
    }
    
    public static boolean isSwitchMaterial(final String mat) {
        return getSwitchMaterials().contains(mat);
    }
    
    public static boolean isItemUseMaterial(final String mat) {
        return getItemUseMaterials().contains(mat);
    }
    
    public static List<String> getUnclaimedZoneIgnoreMaterials() {
        return getStrArr(ConfigNodes.UNCLAIMED_ZONE_IGNORE);
    }
    
    public static List<String> getEntityTypes() {
        return getStrArr(ConfigNodes.PROT_MOB_TYPES);
    }
    
    public static List<String> getPotionTypes() {
        return getStrArr(ConfigNodes.PROT_POTION_TYPES);
    }
    
    private static void setProperty(final String root, final Object value) {
        TownySettings.config.set(root.toLowerCase(), (Object)value.toString());
    }
    
    private static void setNewProperty(final String root, Object value) {
        if (value == null) {
            value = "";
        }
        TownySettings.newConfig.set(root.toLowerCase(), (Object)value.toString());
    }
    
    public static Object getProperty(final String root) {
        return TownySettings.config.get(root.toLowerCase());
    }
    
    public static double getClaimPrice() {
        return getDouble(ConfigNodes.ECO_PRICE_CLAIM_TOWNBLOCK);
    }
    
    public static double getClaimPriceIncreaseValue() {
        return getDouble(ConfigNodes.ECO_PRICE_CLAIM_TOWNBLOCK_INCREASE);
    }
    
    public static double getClaimRefundPrice() {
        return getDouble(ConfigNodes.ECO_PRICE_CLAIM_TOWNBLOCK_REFUND);
    }
    
    public static boolean getUnclaimedZoneSwitchRights() {
        return getBoolean(ConfigNodes.UNCLAIMED_ZONE_SWITCH);
    }
    
    public static boolean getEndermanProtect() {
        return getBoolean(ConfigNodes.NWS_WORLD_ENDERMAN);
    }
    
    public static String getUnclaimedPlotName() {
        return getLangString("UNCLAIMED_PLOT_NAME");
    }
    
    public static long getDayInterval() {
        return getSeconds(ConfigNodes.PLUGIN_DAY_INTERVAL);
    }
    
    public static long getNewDayTime() {
        final long time = getSeconds(ConfigNodes.PLUGIN_NEWDAY_TIME);
        final long day = getDayInterval();
        if (time > day) {
            setProperty(ConfigNodes.PLUGIN_NEWDAY_TIME.getRoot(), day);
            return day;
        }
        return time;
    }
    
    public static TownSpawnLevel.SpawnLevel isAllowingTownSpawn() {
        return getSpawnLevel(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN);
    }
    
    public static TownSpawnLevel.SpawnLevel isAllowingPublicTownSpawnTravel() {
        return getSpawnLevel(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL);
    }
    
    public static NationSpawnLevel.NSpawnLevel isAllowingNationSpawn() {
        return getNSpawnLevel(ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN);
    }
    
    public static NationSpawnLevel.NSpawnLevel isAllowingPublicNationSpawnTravel() {
        return getNSpawnLevel(ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN_TRAVEL);
    }
    
    public static List<String> getDisallowedTownSpawnZones() {
        if (getDebug()) {
            System.out.println("[Towny] Debug: Reading disallowed town spawn zones. ");
        }
        return getStrArr(ConfigNodes.GTOWN_SETTINGS_PREVENT_TOWN_SPAWN_IN);
    }
    
    public static boolean isTaxingDaily() {
        return getBoolean(ConfigNodes.ECO_DAILY_TAXES_ENABLED);
    }
    
    public static double getMaxTax() {
        return getDouble(ConfigNodes.ECO_DAILY_TAXES_MAX_TAX);
    }
    
    public static double getMaxPlotPrice() {
        return getDouble(ConfigNodes.GTOWN_MAX_PLOT_PRICE_COST);
    }
    
    public static double getMaxTaxPercent() {
        return getDouble(ConfigNodes.ECO_DAILY_TAXES_MAX_TAX_PERCENT);
    }
    
    public static boolean isBackingUpDaily() {
        return getBoolean(ConfigNodes.PLUGIN_DAILY_BACKUPS);
    }
    
    public static double getBaseSpoilsOfWar() {
        return getDouble(ConfigNodes.WAR_EVENT_BASE_SPOILS);
    }
    
    public static boolean getOnlyAttackEdgesInWar() {
        return getBoolean(ConfigNodes.WAR_EVENT_ENEMY_ONLY_ATTACK_BORDER);
    }
    
    public static boolean getPlotsHealableInWar() {
        return getBoolean(ConfigNodes.WAR_EVENT_PLOTS_HEALABLE);
    }
    
    public static boolean getPlotsFireworkOnAttacked() {
        return getBoolean(ConfigNodes.WAR_EVENT_PLOTS_FIREWORK_ON_ATTACKED);
    }
    
    public static double getWartimeDeathPrice() {
        return getDouble(ConfigNodes.WAR_EVENT_PRICE_DEATH);
    }
    
    public static boolean getWarEventCostsTownblocks() {
        return getBoolean(ConfigNodes.WAR_EVENT_COSTS_TOWNBLOCKS);
    }
    
    public static boolean getWarEventWinnerTakesOwnershipOfTownblocks() {
        return getBoolean(ConfigNodes.WAR_EVENT_WINNER_TAKES_OWNERSHIP_OF_TOWNBLOCKS);
    }
    
    public static boolean getWarEventWinnerTakesOwnershipOfTown() {
        return getBoolean(ConfigNodes.WAR_EVENT_WINNER_TAKES_OWNERSHIP_OF_TOWN);
    }
    
    public static int getWarEventConquerTime() {
        return getInt(ConfigNodes.WAR_EVENT_CONQUER_TIME);
    }
    
    public static boolean isChargingDeath() {
        return getDeathPrice() > 0.0 || getDeathPriceTown() > 0.0 || getDeathPriceNation() > 0.0;
    }
    
    public static boolean isDeathPriceType() {
        return getString(ConfigNodes.ECO_PRICE_DEATH_TYPE).equalsIgnoreCase("fixed");
    }
    
    public static double getDeathPricePercentageCap() {
        return getDouble(ConfigNodes.ECO_PRICE_DEATH_PERCENTAGE_CAP);
    }
    
    public static boolean isDeathPricePercentageCapped() {
        return getDeathPricePercentageCap() > 0.0;
    }
    
    public static boolean isDeathPricePVPOnly() {
        return getBoolean(ConfigNodes.ECO_PRICE_DEATH_PVP_ONLY);
    }
    
    public static double getDeathPrice() {
        return getDouble(ConfigNodes.ECO_PRICE_DEATH);
    }
    
    public static double getDeathPriceTown() {
        return getDouble(ConfigNodes.ECO_PRICE_DEATH_TOWN);
    }
    
    public static double getDeathPriceNation() {
        return getDouble(ConfigNodes.ECO_PRICE_DEATH_NATION);
    }
    
    public static boolean isEcoClosedEconomyEnabled() {
        return getBoolean(ConfigNodes.ECO_CLOSED_ECONOMY_ENABLED);
    }
    
    public static boolean isJailingAttackingEnemies() {
        return getBoolean(ConfigNodes.JAIL_IS_JAILING_ATTACKING_ENEMIES);
    }
    
    public static boolean isJailingAttackingOutlaws() {
        return getBoolean(ConfigNodes.JAIL_IS_JAILING_ATTACKING_OUTLAWS);
    }
    
    public static boolean JailAllowsEnderPearls() {
        return getBoolean(ConfigNodes.JAIL_JAIL_ALLOWS_ENDER_PEARLS);
    }
    
    public static boolean JailDeniesTownLeave() {
        return getBoolean(ConfigNodes.JAIL_JAIL_DENIES_TOWN_LEAVE);
    }
    
    public static boolean isAllowingBail() {
        return getBoolean(ConfigNodes.JAIL_BAIL_IS_ALLOWING_BAIL);
    }
    
    public static double getBailAmount() {
        return getDouble(ConfigNodes.JAIL_BAIL_BAIL_AMOUNT);
    }
    
    public static double getBailAmountMayor() {
        return getDouble(ConfigNodes.JAIL_BAIL_BAIL_AMOUNT_MAYOR);
    }
    
    public static double getBailAmountKing() {
        return getDouble(ConfigNodes.JAIL_BAIL_BAIL_AMOUNT_KING);
    }
    
    public static double getWartimeTownBlockLossPrice() {
        return getDouble(ConfigNodes.WAR_EVENT_TOWN_BLOCK_LOSS_PRICE);
    }
    
    public static boolean isDevMode() {
        return getBoolean(ConfigNodes.PLUGIN_DEV_MODE_ENABLE);
    }
    
    public static void setDevMode(final boolean choice) {
        setProperty(ConfigNodes.PLUGIN_DEV_MODE_ENABLE.getRoot(), choice);
    }
    
    public static String getDevName() {
        return getString(ConfigNodes.PLUGIN_DEV_MODE_DEV_NAME);
    }
    
    public static boolean isDeclaringNeutral() {
        return getBoolean(ConfigNodes.WARTIME_NATION_CAN_BE_NEUTRAL);
    }
    
    public static void setDeclaringNeutral(final boolean choice) {
        setProperty(ConfigNodes.WARTIME_NATION_CAN_BE_NEUTRAL.getRoot(), choice);
    }
    
    public static boolean isRemovingOnMonarchDeath() {
        return getBoolean(ConfigNodes.WAR_EVENT_REMOVE_ON_MONARCH_DEATH);
    }
    
    public static double getTownUpkeepCost(final Town town) {
        double multiplier;
        if (town != null) {
            if (isUpkeepByPlot()) {
                multiplier = town.getTownBlocks().size();
            }
            else {
                multiplier = Double.valueOf(getTownLevel(town).get(TownLevel.UPKEEP_MULTIPLIER).toString());
            }
        }
        else {
            multiplier = 1.0;
        }
        Double amount = 0.0;
        if (town.hasNation()) {
            double nationMultiplier = 1.0;
            try {
                nationMultiplier = Double.valueOf(getNationLevel(town.getNation()).get(NationLevel.NATION_TOWN_UPKEEP_MULTIPLIER).toString());
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
            catch (NotRegisteredException e2) {
                e2.printStackTrace();
            }
            if (isUpkeepByPlot()) {
                if (isTownLevelModifiersAffectingPlotBasedUpkeep()) {
                    amount = getTownUpkeep() * multiplier * Double.valueOf(getTownLevel(town).get(TownLevel.UPKEEP_MULTIPLIER).toString()) * nationMultiplier;
                }
                else {
                    amount = getTownUpkeep() * multiplier * nationMultiplier;
                }
                if (getPlotBasedUpkeepMinimumAmount() > 0.0 && amount < getPlotBasedUpkeepMinimumAmount()) {
                    amount = getPlotBasedUpkeepMinimumAmount();
                }
                return amount;
            }
            return getTownUpkeep() * multiplier * nationMultiplier;
        }
        else {
            if (isUpkeepByPlot()) {
                if (isTownLevelModifiersAffectingPlotBasedUpkeep()) {
                    amount = getTownUpkeep() * multiplier * Double.valueOf(getTownLevel(town).get(TownLevel.UPKEEP_MULTIPLIER).toString());
                }
                else {
                    amount = getTownUpkeep() * multiplier;
                }
                if (getPlotBasedUpkeepMinimumAmount() > 0.0 && amount < getPlotBasedUpkeepMinimumAmount()) {
                    amount = getPlotBasedUpkeepMinimumAmount();
                }
                return amount;
            }
            return getTownUpkeep() * multiplier;
        }
    }
    
    public static double getTownUpkeep() {
        return getDouble(ConfigNodes.ECO_PRICE_TOWN_UPKEEP);
    }
    
    public static boolean isUpkeepByPlot() {
        return getBoolean(ConfigNodes.ECO_PRICE_TOWN_UPKEEP_PLOTBASED);
    }
    
    public static double getPlotBasedUpkeepMinimumAmount() {
        return getDouble(ConfigNodes.ECO_PRICE_TOWN_UPKEEP_PLOTBASED_MINIMUM_AMOUNT);
    }
    
    public static boolean isTownLevelModifiersAffectingPlotBasedUpkeep() {
        return getBoolean(ConfigNodes.ECO_PRICE_TOWN_UPKEEP_PLOTBASED_TOWNLEVEL_MODIFIER);
    }
    
    public static boolean isUpkeepPayingPlots() {
        return getBoolean(ConfigNodes.ECO_UPKEEP_PLOTPAYMENTS);
    }
    
    public static double getTownPenaltyUpkeepCost(final Town town) {
        if (getUpkeepPenalty() <= 0.0) {
            return 0.0;
        }
        final int claimed = town.getTownBlocks().size();
        final int allowedClaims = getMaxTownBlocks(town);
        final int overClaimed = claimed - allowedClaims;
        if (!town.isOverClaimed()) {
            return 0.0;
        }
        if (isUpkeepPenaltyByPlot()) {
            return getUpkeepPenalty() * overClaimed;
        }
        return getUpkeepPenalty();
    }
    
    public static double getUpkeepPenalty() {
        return getDouble(ConfigNodes.ECO_PRICE_TOWN_OVERCLAIMED_UPKEEP_PENALTY);
    }
    
    public static boolean isUpkeepPenaltyByPlot() {
        return getBoolean(ConfigNodes.ECO_PRICE_TOWN_OVERCLAIMED_UPKEEP_PENALTY_PLOTBASED);
    }
    
    public static double getNationUpkeep() {
        return getDouble(ConfigNodes.ECO_PRICE_NATION_UPKEEP);
    }
    
    public static double getNationUpkeepCost(final Nation nation) {
        double multiplier;
        if (nation != null) {
            if (isNationUpkeepPerTown()) {
                if (isNationLevelModifierAffectingNationUpkeepPerTown()) {
                    return getNationUpkeep() * nation.getTowns().size() * Double.valueOf(getNationLevel(nation).get(NationLevel.UPKEEP_MULTIPLIER).toString());
                }
                return getNationUpkeep() * nation.getTowns().size();
            }
            else {
                multiplier = Double.valueOf(getNationLevel(nation).get(NationLevel.UPKEEP_MULTIPLIER).toString());
            }
        }
        else {
            multiplier = 1.0;
        }
        return getNationUpkeep() * multiplier;
    }
    
    private static boolean isNationLevelModifierAffectingNationUpkeepPerTown() {
        return getBoolean(ConfigNodes.ECO_PRICE_NATION_UPKEEP_PERTOWN_NATIONLEVEL_MODIFIER);
    }
    
    private static boolean isNationUpkeepPerTown() {
        return getBoolean(ConfigNodes.ECO_PRICE_NATION_UPKEEP_PERTOWN);
    }
    
    public static boolean getNationDefaultPublic() {
        return getBoolean(ConfigNodes.GNATION_DEF_PUBLIC);
    }
    
    public static String getFlatFileBackupType() {
        return getString(ConfigNodes.PLUGIN_FLATFILE_BACKUP);
    }
    
    public static long getBackupLifeLength() {
        long t = TimeTools.getMillis(getString(ConfigNodes.PLUGIN_BACKUPS_ARE_DELETED_AFTER));
        final long minT = TimeTools.getMillis("1d");
        if (t >= 0L && t < minT) {
            t = minT;
        }
        return t;
    }
    
    public static boolean isUsingTowny() {
        return getBoolean(ConfigNodes.NWS_WORLD_USING_TOWNY);
    }
    
    public static boolean isPvP() {
        return getBoolean(ConfigNodes.NWS_WORLD_PVP);
    }
    
    public static boolean isForcingPvP() {
        return getBoolean(ConfigNodes.NWS_FORCE_PVP_ON);
    }
    
    public static boolean isPlayerTramplingCropsDisabled() {
        return getBoolean(ConfigNodes.NWS_DISABLE_PLAYER_CROP_TRAMPLING);
    }
    
    public static boolean isCreatureTramplingCropsDisabled() {
        return getBoolean(ConfigNodes.NWS_DISABLE_CREATURE_CROP_TRAMPLING);
    }
    
    public static boolean isWorldMonstersOn() {
        return getBoolean(ConfigNodes.NWS_WORLD_MONSTERS_ON);
    }
    
    public static boolean isExplosions() {
        return getBoolean(ConfigNodes.NWS_WORLD_EXPLOSION);
    }
    
    public static boolean isForcingExplosions() {
        return getBoolean(ConfigNodes.NWS_FORCE_EXPLOSIONS_ON);
    }
    
    public static boolean isForcingMonsters() {
        return getBoolean(ConfigNodes.NWS_FORCE_TOWN_MONSTERS_ON);
    }
    
    public static boolean isFire() {
        return getBoolean(ConfigNodes.NWS_WORLD_FIRE);
    }
    
    public static boolean isForcingFire() {
        return getBoolean(ConfigNodes.NWS_FORCE_FIRE_ON);
    }
    
    public static boolean isUsingPlotManagementDelete() {
        return getBoolean(ConfigNodes.NWS_PLOT_MANAGEMENT_DELETE_ENABLE);
    }
    
    public static List<String> getPlotManagementDeleteIds() {
        return getStrArr(ConfigNodes.NWS_PLOT_MANAGEMENT_DELETE);
    }
    
    public static boolean isUsingPlotManagementMayorDelete() {
        return getBoolean(ConfigNodes.NWS_PLOT_MANAGEMENT_MAYOR_DELETE_ENABLE);
    }
    
    public static List<String> getPlotManagementMayorDelete() {
        return getStrArr(ConfigNodes.NWS_PLOT_MANAGEMENT_MAYOR_DELETE);
    }
    
    public static boolean isUsingPlotManagementRevert() {
        return getBoolean(ConfigNodes.NWS_PLOT_MANAGEMENT_REVERT_ENABLE);
    }
    
    public static long getPlotManagementSpeed() {
        return getSeconds(ConfigNodes.NWS_PLOT_MANAGEMENT_REVERT_TIME);
    }
    
    public static boolean isUsingPlotManagementWildRegen() {
        return getBoolean(ConfigNodes.NWS_PLOT_MANAGEMENT_WILD_MOB_REVERT_ENABLE);
    }
    
    public static long getPlotManagementWildRegenDelay() {
        return getSeconds(ConfigNodes.NWS_PLOT_MANAGEMENT_WILD_MOB_REVERT_TIME);
    }
    
    public static List<String> getPlotManagementIgnoreIds() {
        return getStrArr(ConfigNodes.NWS_PLOT_MANAGEMENT_REVERT_IGNORE);
    }
    
    public static boolean isTownRespawning() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_TOWN_RESPAWN);
    }
    
    public static boolean isTownRespawningInOtherWorlds() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_TOWN_RESPAWN_SAME_WORLD_ONLY);
    }
    
    public static int getMaxResidentsPerTown() {
        return getInt(ConfigNodes.GTOWN_MAX_RESIDENTS_PER_TOWN);
    }
    
    public static boolean isTownyUpdating(final String currentVersion) {
        return !isTownyUpToDate(currentVersion);
    }
    
    public static boolean isTownyUpToDate(final String currentVersion) {
        return currentVersion.equals(getLastRunVersion(currentVersion));
    }
    
    public static String getLastRunVersion(final String currentVersion) {
        return getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
    }
    
    public static void setLastRunVersion(final String currentVersion) {
        setProperty(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
        TownySettings.config.save();
    }
    
    public static int getMinDistanceFromTownHomeblocks() {
        return getInt(ConfigNodes.TOWN_MIN_DISTANCE_FROM_TOWN_HOMEBLOCK);
    }
    
    public static int getMinDistanceForOutpostsFromPlot() {
        return getInt(ConfigNodes.TOWN_MIN_DISTANCE_FOR_OUTPOST_FROM_PLOT);
    }
    
    public static int getMinDistanceFromTownPlotblocks() {
        return getInt(ConfigNodes.TOWN_MIN_PLOT_DISTANCE_FROM_TOWN_PLOT);
    }
    
    public static int getMaxDistanceBetweenHomeblocks() {
        return getInt(ConfigNodes.TOWN_MAX_DISTANCE_BETWEEN_HOMEBLOCKS);
    }
    
    public static int getMaxResidentPlots(final Resident resident) {
        int maxPlots = TownyUniverse.getInstance().getPermissionSource().getGroupPermissionIntNode(resident.getName(), PermissionNodes.TOWNY_MAX_PLOTS.getNode());
        if (maxPlots == -1) {
            maxPlots = getInt(ConfigNodes.TOWN_MAX_PLOTS_PER_RESIDENT);
        }
        return maxPlots;
    }
    
    public static int getMaxResidentExtraPlots(final Resident resident) {
        int extraPlots = TownyUniverse.getInstance().getPermissionSource().getPlayerPermissionIntNode(resident.getName(), PermissionNodes.TOWNY_EXTRA_PLOTS.getNode());
        if (extraPlots == -1) {
            extraPlots = 0;
        }
        return extraPlots;
    }
    
    public static int getMaxResidentOutposts(final Resident resident) {
        final int maxOutposts = TownyUniverse.getInstance().getPermissionSource().getGroupPermissionIntNode(resident.getName(), PermissionNodes.TOWNY_MAX_OUTPOSTS.getNode());
        return maxOutposts;
    }
    
    public static boolean getPermFlag_Resident_Friend_Build() {
        return getBoolean(ConfigNodes.FLAGS_RES_FR_BUILD);
    }
    
    public static boolean getPermFlag_Resident_Friend_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_RES_FR_DESTROY);
    }
    
    public static boolean getPermFlag_Resident_Friend_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_RES_FR_ITEM_USE);
    }
    
    public static boolean getPermFlag_Resident_Friend_Switch() {
        return getBoolean(ConfigNodes.FLAGS_RES_FR_SWITCH);
    }
    
    public static boolean getPermFlag_Resident_Town_Build() {
        return getBoolean(ConfigNodes.FLAGS_RES_TOWN_BUILD);
    }
    
    public static boolean getPermFlag_Resident_Town_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_RES_TOWN_DESTROY);
    }
    
    public static boolean getPermFlag_Resident_Town_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_RES_TOWN_ITEM_USE);
    }
    
    public static boolean getPermFlag_Resident_Town_Switch() {
        return getBoolean(ConfigNodes.FLAGS_RES_TOWN_SWITCH);
    }
    
    public static boolean getPermFlag_Resident_Ally_Build() {
        return getBoolean(ConfigNodes.FLAGS_RES_ALLY_BUILD);
    }
    
    public static boolean getPermFlag_Resident_Ally_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_RES_ALLY_DESTROY);
    }
    
    public static boolean getPermFlag_Resident_Ally_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_RES_ALLY_ITEM_USE);
    }
    
    public static boolean getPermFlag_Resident_Ally_Switch() {
        return getBoolean(ConfigNodes.FLAGS_RES_ALLY_SWITCH);
    }
    
    public static boolean getPermFlag_Resident_Outsider_Build() {
        return getBoolean(ConfigNodes.FLAGS_RES_OUTSIDER_BUILD);
    }
    
    public static boolean getPermFlag_Resident_Outsider_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_RES_OUTSIDER_DESTROY);
    }
    
    public static boolean getPermFlag_Resident_Outsider_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_RES_OUTSIDER_ITEM_USE);
    }
    
    public static boolean getPermFlag_Resident_Outsider_Switch() {
        return getBoolean(ConfigNodes.FLAGS_RES_OUTSIDER_SWITCH);
    }
    
    public static boolean getPermFlag_Town_Default_PVP() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_DEF_PVP);
    }
    
    public static boolean getPermFlag_Town_Default_FIRE() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_DEF_FIRE);
    }
    
    public static boolean getPermFlag_Town_Default_Explosion() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_DEF_EXPLOSION);
    }
    
    public static boolean getPermFlag_Town_Default_Mobs() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_DEF_MOBS);
    }
    
    public static boolean getPermFlag_Town_Resident_Build() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_RES_BUILD);
    }
    
    public static boolean getPermFlag_Town_Resident_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_RES_DESTROY);
    }
    
    public static boolean getPermFlag_Town_Resident_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_RES_ITEM_USE);
    }
    
    public static boolean getPermFlag_Town_Resident_Switch() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_RES_SWITCH);
    }
    
    public static boolean getPermFlag_Town_Nation_Build() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_NATION_BUILD);
    }
    
    public static boolean getPermFlag_Town_Nation_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_NATION_DESTROY);
    }
    
    public static boolean getPermFlag_Town_Nation_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_NATION_ITEM_USE);
    }
    
    public static boolean getPermFlag_Town_Nation_Switch() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_NATION_SWITCH);
    }
    
    public static boolean getPermFlag_Town_Ally_Build() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_ALLY_BUILD);
    }
    
    public static boolean getPermFlag_Town_Ally_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_ALLY_DESTROY);
    }
    
    public static boolean getPermFlag_Town_Ally_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_ALLY_ITEM_USE);
    }
    
    public static boolean getPermFlag_Town_Ally_Switch() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_ALLY_SWITCH);
    }
    
    public static boolean getPermFlag_Town_Outsider_Build() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_OUTSIDER_BUILD);
    }
    
    public static boolean getPermFlag_Town_Outsider_Destroy() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_OUTSIDER_DESTROY);
    }
    
    public static boolean getPermFlag_Town_Outsider_ItemUse() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_OUTSIDER_ITEM_USE);
    }
    
    public static boolean getPermFlag_Town_Outsider_Switch() {
        return getBoolean(ConfigNodes.FLAGS_TOWN_OUTSIDER_SWITCH);
    }
    
    public static boolean getDefaultResidentPermission(final TownBlockOwner owner, final TownyPermission.ActionType type) {
        if (owner instanceof Resident) {
            switch (type) {
                case BUILD: {
                    return getPermFlag_Resident_Friend_Build();
                }
                case DESTROY: {
                    return getPermFlag_Resident_Friend_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Resident_Friend_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Resident_Friend_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
        else {
            if (!(owner instanceof Town)) {
                throw new UnsupportedOperationException();
            }
            switch (type) {
                case BUILD: {
                    return getPermFlag_Town_Resident_Build();
                }
                case DESTROY: {
                    return getPermFlag_Town_Resident_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Town_Resident_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Town_Resident_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
    
    public static boolean getDefaultNationPermission(final TownBlockOwner owner, final TownyPermission.ActionType type) {
        if (owner instanceof Resident) {
            switch (type) {
                case BUILD: {
                    return getPermFlag_Resident_Town_Build();
                }
                case DESTROY: {
                    return getPermFlag_Resident_Town_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Resident_Town_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Resident_Town_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
        else {
            if (!(owner instanceof Town)) {
                throw new UnsupportedOperationException();
            }
            switch (type) {
                case BUILD: {
                    return getPermFlag_Town_Nation_Build();
                }
                case DESTROY: {
                    return getPermFlag_Town_Nation_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Town_Nation_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Town_Nation_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
    
    public static boolean getDefaultAllyPermission(final TownBlockOwner owner, final TownyPermission.ActionType type) {
        if (owner instanceof Resident) {
            switch (type) {
                case BUILD: {
                    return getPermFlag_Resident_Ally_Build();
                }
                case DESTROY: {
                    return getPermFlag_Resident_Ally_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Resident_Ally_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Resident_Ally_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
        else {
            if (!(owner instanceof Town)) {
                throw new UnsupportedOperationException();
            }
            switch (type) {
                case BUILD: {
                    return getPermFlag_Town_Ally_Build();
                }
                case DESTROY: {
                    return getPermFlag_Town_Ally_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Town_Ally_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Town_Ally_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
    
    public static boolean getDefaultOutsiderPermission(final TownBlockOwner owner, final TownyPermission.ActionType type) {
        if (owner instanceof Resident) {
            switch (type) {
                case BUILD: {
                    return getPermFlag_Resident_Outsider_Build();
                }
                case DESTROY: {
                    return getPermFlag_Resident_Outsider_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Resident_Outsider_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Resident_Outsider_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
        else {
            if (!(owner instanceof Town)) {
                throw new UnsupportedOperationException();
            }
            switch (type) {
                case BUILD: {
                    return getPermFlag_Town_Outsider_Build();
                }
                case DESTROY: {
                    return getPermFlag_Town_Outsider_Destroy();
                }
                case SWITCH: {
                    return getPermFlag_Town_Outsider_Switch();
                }
                case ITEM_USE: {
                    return getPermFlag_Town_Outsider_ItemUse();
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
    
    public static boolean getDefaultPermission(final TownBlockOwner owner, final TownyPermission.PermLevel level, final TownyPermission.ActionType type) {
        switch (level) {
            case RESIDENT: {
                return getDefaultResidentPermission(owner, type);
            }
            case NATION: {
                return getDefaultNationPermission(owner, type);
            }
            case ALLY: {
                return getDefaultAllyPermission(owner, type);
            }
            case OUTSIDER: {
                return getDefaultOutsiderPermission(owner, type);
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    public static boolean isLogging() {
        return getBoolean(ConfigNodes.PLUGIN_LOGGING);
    }
    
    public static String getAcceptCommand() {
        return getString(ConfigNodes.INVITE_SYSTEM_ACCEPT_COMMAND);
    }
    
    public static String getDenyCommand() {
        return getString(ConfigNodes.INVITE_SYSTEM_DENY_COMMAND);
    }
    
    public static String getConfirmCommand() {
        return getString(ConfigNodes.INVITE_SYSTEM_CONFIRM_COMMAND);
    }
    
    public static String getCancelCommand() {
        return getString(ConfigNodes.INVITE_SYSTEM_CANCEL_COMMAND);
    }
    
    public static boolean getOutsidersPreventPVPToggle() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_OUTSIDERS_PREVENT_PVP_TOGGLE);
    }
    
    public static boolean isForcePvpNotAffectingHomeblocks() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_HOMEBLOCKS_PREVENT_FORCEPVP);
    }
    
    public static long getTownInviteCooldown() {
        return getSeconds(ConfigNodes.INVITE_SYSTEM_COOLDOWN_TIME);
    }
    
    public static boolean isAppendingToLog() {
        return !getBoolean(ConfigNodes.PLUGIN_RESET_LOG_ON_BOOT);
    }
    
    public static String getNameFilterRegex() {
        return getString(ConfigNodes.FILTERS_REGEX_NAME_FILTER_REGEX);
    }
    
    public static String getNameCheckRegex() {
        return getString(ConfigNodes.FILTERS_REGEX_NAME_CHECK_REGEX);
    }
    
    public static String getStringCheckRegex() {
        return getString(ConfigNodes.FILTERS_REGEX_STRING_CHECK_REGEX);
    }
    
    public static String getNameRemoveRegex() {
        return getString(ConfigNodes.FILTERS_REGEX_NAME_REMOVE_REGEX);
    }
    
    public static int getTeleportWarmupTime() {
        return getInt(ConfigNodes.GTOWN_SETTINGS_SPAWN_TIMER);
    }
    
    public static int getSpawnCooldownTime() {
        return getInt(ConfigNodes.GTOWN_SETTINGS_SPAWN_COOLDOWN_TIMER);
    }
    
    public static int getPVPCoolDownTime() {
        return getInt(ConfigNodes.GTOWN_SETTINGS_PVP_COOLDOWN_TIMER);
    }
    
    public static String getTownAccountPrefix() {
        return getString(ConfigNodes.ECO_TOWN_PREFIX);
    }
    
    public static String getNationAccountPrefix() {
        return getString(ConfigNodes.ECO_NATION_PREFIX);
    }
    
    public static double getTownBankCap() {
        return getDouble(ConfigNodes.ECO_BANK_CAP_TOWN);
    }
    
    public static double getNationBankCap() {
        return getDouble(ConfigNodes.ECO_BANK_CAP_NATION);
    }
    
    public static boolean getTownBankAllowWithdrawls() {
        return getBoolean(ConfigNodes.ECO_BANK_TOWN_ALLOW_WITHDRAWALS);
    }
    
    public static void SetTownBankAllowWithdrawls(final boolean newSetting) {
        setProperty(ConfigNodes.ECO_BANK_TOWN_ALLOW_WITHDRAWALS.getRoot(), newSetting);
    }
    
    public static boolean geNationBankAllowWithdrawls() {
        return getBoolean(ConfigNodes.ECO_BANK_NATION_ALLOW_WITHDRAWALS);
    }
    
    public static boolean isBankActionDisallowedOutsideTown() {
        return getBoolean(ConfigNodes.ECO_BANK_DISALLOW_BANK_ACTIONS_OUTSIDE_TOWN);
    }
    
    public static boolean isBankActionLimitedToBankPlots() {
        return getBoolean(ConfigNodes.BANK_IS_LIMTED_TO_BANK_PLOTS);
    }
    
    public static void SetNationBankAllowWithdrawls(final boolean newSetting) {
        setProperty(ConfigNodes.ECO_BANK_NATION_ALLOW_WITHDRAWALS.getRoot(), newSetting);
    }
    
    @Deprecated
    public static boolean isValidRegionName(final String name) {
        return !NameValidation.isBlacklistName(name);
    }
    
    @Deprecated
    public static boolean isValidName(final String name) {
        return NameValidation.isValidName(name);
    }
    
    @Deprecated
    public static String filterName(final String input) {
        return NameValidation.filterName(input);
    }
    
    public static boolean isDisallowOneWayAlliance() {
        return getBoolean(ConfigNodes.WAR_DISALLOW_ONE_WAY_ALLIANCE);
    }
    
    public static int getNumResidentsJoinNation() {
        return getInt(ConfigNodes.GTOWN_SETTINGS_REQUIRED_NUMBER_RESIDENTS_JOIN_NATION);
    }
    
    public static int getNumResidentsCreateNation() {
        return getInt(ConfigNodes.GTOWN_SETTINGS_REQUIRED_NUMBER_RESIDENTS_CREATE_NATION);
    }
    
    public static boolean isRefundNationDisbandLowResidents() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_REFUND_DISBAND_LOW_RESIDENTS);
    }
    
    public static double getNationRequiresProximity() {
        return getDouble(ConfigNodes.GTOWN_SETTINGS_NATION_REQUIRES_PROXIMITY);
    }
    
    public static List<String> getFarmPlotBlocks() {
        return getStrArr(ConfigNodes.GTOWN_FARM_PLOT_ALLOW_BLOCKS);
    }
    
    public static List<String> getFarmAnimals() {
        return getStrArr(ConfigNodes.GTOWN_FARM_ANIMALS);
    }
    
    public static boolean getKeepInventoryInTowns() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_KEEP_INVENTORY_ON_DEATH_IN_TOWN);
    }
    
    public static boolean getKeepExperienceInTowns() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_KEEP_EXPERIENCE_ON_DEATH_IN_TOWN);
    }
    
    public static String getListPageMsg(final int page, final int total) {
        return parseString(String.format(getLangString("LIST_PAGE"), String.valueOf(page), String.valueOf(total)))[0];
    }
    
    public static String getListNotEnoughPagesMsg(final int max) {
        return parseString(String.format(getLangString("LIST_ERR_NOT_ENOUGH_PAGES"), String.valueOf(max)))[0];
    }
    
    public static String[] getWarAPlayerHasNoTownMsg() {
        return parseString(String.format(getLangString("msg_war_a_player_has_no_town"), new Object[0]));
    }
    
    public static String[] getWarAPlayerHasNoNationMsg() {
        return parseString(String.format(getLangString("msg_war_a_player_has_no_nation"), new Object[0]));
    }
    
    public static String[] getWarAPlayerHasANeutralNationMsg() {
        return parseString(String.format(getLangString("msg_war_a_player_has_a_neutral_nation"), new Object[0]));
    }
    
    public static String[] getWarAPlayerHasBeenRemovedFromWarMsg() {
        return parseString(String.format(getLangString("msg_war_a_player_has_been_removed_from_war"), new Object[0]));
    }
    
    public static String[] getWarPlayerCannotBeJailedPlotFallenMsg() {
        return parseString(String.format(getLangString("msg_war_player_cant_be_jailed_plot_fallen"), new Object[0]));
    }
    
    public static String[] getWarAPlayerIsAnAllyMsg() {
        return parseString(String.format(getLangString("msg_war_a_player_is_an_ally"), new Object[0]));
    }
    
    public static boolean isNotificationUsingTitles() {
        return getBoolean(ConfigNodes.NOTIFICATION_USING_TITLES);
    }
    
    public static int getAmountOfResidentsForOutpost() {
        return getInt(ConfigNodes.GTOWN_SETTINGS_MINIMUM_AMOUNT_RESIDENTS_FOR_OUTPOSTS);
    }
    
    public static int getMaximumInvitesSentTown() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAXIMUM_INVITES_SENT_TOWN);
    }
    
    public static int getMaximumInvitesSentNation() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAXIMUM_INVITES_SENT_NATION);
    }
    
    public static int getMaximumRequestsSentNation() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAXIMUM_REQUESTS_SENT_NATION);
    }
    
    public static int getMaximumInvitesReceivedResident() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAXIMUM_INVITES_RECEIVED_PLAYER);
    }
    
    public static int getMaximumInvitesReceivedTown() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAXIMUM_INVITES_RECEIVED_TOWN);
    }
    
    public static int getMaximumRequestsReceivedNation() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAXIMUM_REQUESTS_RECEIVED_NATION);
    }
    
    public static boolean getNationZonesEnabled() {
        return getBoolean(ConfigNodes.GNATION_SETTINGS_NATIONZONE_ENABLE);
    }
    
    public static boolean getNationZonesCapitalsOnly() {
        return getBoolean(ConfigNodes.GNATION_SETTINGS_NATIONZONE_ONLY_CAPITALS);
    }
    
    public static boolean getNationZonesWarDisables() {
        return getBoolean(ConfigNodes.GNATION_SETTINGS_NATIONZONE_WAR_DISABLES);
    }
    
    public static boolean getNationZonesShowNotifications() {
        return getBoolean(ConfigNodes.GNATION_SETTINGS_NATIONZONE_SHOW_NOTIFICATIONS);
    }
    
    public static int getNationZonesCapitalBonusSize() {
        return getInt(ConfigNodes.GNATION_SETTINGS_NATIONZONE_CAPITAL_BONUS_SIZE);
    }
    
    public static boolean isShowingRegistrationMessage() {
        return getBoolean(ConfigNodes.RES_SETTING_IS_SHOWING_WELCOME_MESSAGE);
    }
    
    public static int getMaxTownsPerNation() {
        return getInt(ConfigNodes.GNATION_SETTINGS_MAX_TOWNS_PER_NATION);
    }
    
    public static double getSpawnTravelCost() {
        return getDouble(ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_PUBLIC);
    }
    
    public static boolean isAllySpawningRequiringPublicStatus() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_IS_ALLY_SPAWNING_REQUIRING_PUBLIC_STATUS);
    }
    
    public static String getNotificationTitlesTownTitle() {
        return getString(ConfigNodes.NOTIFICATION_TITLES_TOWN_TITLE);
    }
    
    public static String getNotificationTitlesTownSubtitle() {
        return getString(ConfigNodes.NOTIFICATION_TITLES_TOWN_SUBTITLE);
    }
    
    public static String getNotificationTitlesWildTitle() {
        return getString(ConfigNodes.NOTIFICATION_TITLES_WILDERNESS_TITLE);
    }
    
    public static String getNotificationTitlesWildSubtitle() {
        return getString(ConfigNodes.NOTIFICATION_TITLES_WILDERNESS_SUBTITLE);
    }
    
    public static double getTownRenameCost() {
        return getDouble(ConfigNodes.ECO_TOWN_RENAME_COST);
    }
    
    public static double getNationRenameCost() {
        return getDouble(ConfigNodes.ECO_NATION_RENAME_COST);
    }
    
    public static boolean isRemovingKillerBunny() {
        return getBoolean(ConfigNodes.PROT_MOB_REMOVE_TOWN_KILLER_BUNNY);
    }
    
    public static boolean isSkippingRemovalOfNamedMobs() {
        return getBoolean(ConfigNodes.PROT_MOB_REMOVE_SKIP_NAMED_MOBS);
    }
    
    public static List<String> getJailBlacklistedCommands() {
        return getStrArr(ConfigNodes.JAIL_BLACKLISTED_COMMANDS);
    }
    
    public static String getPAPIFormattingBoth() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_BOTH);
    }
    
    public static String getPAPIFormattingTown() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_TOWN);
    }
    
    public static String getPAPIFormattingNation() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_NATION);
    }
    
    public static String getPAPIFormattingNomad() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_RANKS_NOMAD);
    }
    
    public static String getPAPIFormattingResident() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_RANKS_RESIDENT);
    }
    
    public static String getPAPIFormattingMayor() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_RANKS_MAYOR);
    }
    
    public static String getPAPIFormattingKing() {
        return getString(ConfigNodes.FILTERS_PAPI_CHAT_FORMATTING_RANKS_KING);
    }
    
    public static double getPlotSetCommercialCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_COMMERCIAL);
    }
    
    public static double getPlotSetArenaCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_ARENA);
    }
    
    public static double getPlotSetEmbassyCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_EMBASSY);
    }
    
    public static double getPlotSetWildsCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_WILDS);
    }
    
    public static double getPlotSetInnCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_INN);
    }
    
    public static double getPlotSetJailCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_JAIL);
    }
    
    public static double getPlotSetFarmCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_FARM);
    }
    
    public static double getPlotSetBankCost() {
        return getDouble(ConfigNodes.ECO_PLOT_TYPE_COSTS_BANK);
    }
    
    public static int getMaxDistanceFromTownSpawnForInvite() {
        return getInt(ConfigNodes.INVITE_SYSTEM_MAX_DISTANCE_FROM_TOWN_SPAWN);
    }
    
    public static boolean getTownDisplaysXYZ() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_DISPLAY_XYZ_INSTEAD_OF_TOWNY_COORDS);
    }
    
    public static boolean isTownListRandom() {
        return getBoolean(ConfigNodes.GTOWN_SETTINGS_DISPLAY_TOWN_LIST_RANDOMLY);
    }
    
    public static boolean isWarAllowed() {
        return getBoolean(ConfigNodes.NWS_WAR_ALLOWED);
    }
    
    public static int timeToWaitAfterFlag() {
        return getInt(ConfigNodes.WAR_ENEMY_TIME_TO_WAIT_AFTER_FLAGGED);
    }
    
    public static boolean isFlaggedInteractionTown() {
        return getBoolean(ConfigNodes.WAR_ENEMY_PREVENT_INTERACTION_WHILE_FLAGGED);
    }
    
    public static boolean isFlaggedInteractionNation() {
        return getBoolean(ConfigNodes.WAR_ENEMY_PREVENT_NATION_INTERACTION_WHILE_FLAGGED);
    }
    
    public static boolean isNotificationsTownNamesVerbose() {
        return getBoolean(ConfigNodes.NOTIFICATION_TOWN_NAMES_ARE_VERBOSE);
    }
    
    public static boolean getWarSiegeEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_ENABLED);
    }
    
    public static boolean getWarSiegeAttackEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_ATTACK_ENABLED);
    }
    
    public static boolean getWarSiegeAbandonEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_ABANDON_ENABLED);
    }
    
    public static boolean getWarSiegeSurrenderEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_TOWN_SURRENDER_ENABLED);
    }
    
    public static boolean getWarSiegeInvadeEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_INVADE_ENABLED);
    }
    
    public static boolean getWarSiegePlunderEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_PLUNDER_ENABLED);
    }
    
    public static boolean getWarSiegeRevoltEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_REVOLT_ENABLED);
    }
    
    public static boolean getWarSiegeTownLeaveDisabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_TOWN_LEAVE_DISABLED);
    }
    
    public static boolean getWarSiegePvpAlwaysOnInBesiegedTowns() {
        return getBoolean(ConfigNodes.WAR_SIEGE_PVP_ALWAYS_ON_IN_BESIEGED_TOWNS);
    }
    
    public static boolean getWarSiegeDelayFullTownRemoval() {
        return getBoolean(ConfigNodes.WAR_SIEGE_DELAY_FULL_TOWN_REMOVAL);
    }
    
    public static boolean getWarSiegeClaimingDisabledNearSiegeZones() {
        return getBoolean(ConfigNodes.WAR_SIEGE_CLAIMING_DISABLED_NEAR_SIEGE_ZONES);
    }
    
    public static int getWarSiegeClaimDisableDistanceBlocks() {
        return getInt(ConfigNodes.WAR_SIEGE_CLAIM_DISABLE_DISTANCE_BLOCKS);
    }
    
    public static int getWarSiegeMaxAllowedBannerToTownDownwardElevationDifference() {
        return getInt(ConfigNodes.WAR_SIEGE_MAX_ALLOWED_BANNER_TO_TOWN_DOWNWARD_ELEVATION_DIFFERENCE);
    }
    
    public static double getWarSiegeRuinsRemovalDelayMinutes() {
        return getDouble(ConfigNodes.WAR_SIEGE_RUINS_REMOVAL_DELAY_MINUTES);
    }
    
    public static double getWarSiegeAttackerCostUpFrontPerPlot() {
        return getDouble(ConfigNodes.WAR_SIEGE_ATTACKER_COST_UPFRONT_PER_PLOT);
    }
    
    public static long getWarSiegeTimerIntervalSeconds() {
        return getInt(ConfigNodes.WAR_SIEGE_TIMER_TICK_INTERVAL_SECONDS);
    }
    
    public static double getWarSiegeSiegeImmunityTimeNewTownsHours() {
        return getDouble(ConfigNodes.WAR_SIEGE_SIEGE_IMMUNITY_TIME_NEW_TOWN_HOURS);
    }
    
    public static double getWarSiegeSiegeImmunityTimeModifier() {
        return getDouble(ConfigNodes.WAR_SIEGE_SIEGE_IMMUNITY_TIME_MODIFIER);
    }
    
    public static double getWarSiegeRevoltImmunityTimeHours() {
        return getDouble(ConfigNodes.WAR_SIEGE_REVOLT_IMMUNITY_TIME_HOURS);
    }
    
    public static double getWarSiegeAttackerPlunderAmountPerPlot() {
        return getDouble(ConfigNodes.WAR_SIEGE_ATTACKER_PLUNDER_AMOUNT_PER_PLOT);
    }
    
    public static double getWarSiegeMaxHoldoutTimeHours() {
        return getDouble(ConfigNodes.WAR_SIEGE_MAX_HOLDOUT_TIME_HOURS);
    }
    
    public static double getWarSiegeMinSiegeDurationBeforeSurrenderHours() {
        return getDouble(ConfigNodes.WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_SURRENDER_HOURS);
    }
    
    public static double getWarSiegeMinSiegeDurationBeforeAbandonHours() {
        return getDouble(ConfigNodes.WAR_SIEGE_MIN_SIEGE_DURATION_BEFORE_ABANDON_HOURS);
    }
    
    public static int getWarSiegePointsForAttackerOccupation() {
        return getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_ATTACKER_OCCUPATION);
    }
    
    public static int getWarSiegePointsForDefenderOccupation() {
        return getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_DEFENDER_OCCUPATION);
    }
    
    public static int getWarSiegePointsForAttackerDeath() {
        return getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_ATTACKER_DEATH);
    }
    
    public static int getWarSiegePointsForDefenderDeath() {
        return getInt(ConfigNodes.WAR_SIEGE_POINTS_FOR_DEFENDER_DEATH);
    }
    
    public static int getWarSiegeZoneDeathRadiusBlocks() {
        return getInt(ConfigNodes.WAR_SIEGE_ZONE_DEATH_RADIUS_BLOCKS);
    }
    
    public static double getWarSiegeZoneMaximumScoringDurationMinutes() {
        return getDouble(ConfigNodes.WAR_SIEGE_ZONE_MAXIMUM_SCORING_DURATION_MINUTES);
    }
    
    public static boolean getWarSiegeAttackerSpawnIntoBesiegedTownDisabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_ATTACKER_SPAWN_INTO_BESIEGED_TOWN_DISABLED);
    }
    
    public static double getWarSiegeNationCostRefundPercentageOnDelete() {
        return getDouble(ConfigNodes.WAR_SIEGE_NATION_COST_REFUND_PERCENTAGE_ON_DELETE);
    }
    
    public static boolean getWarSiegeRefundInitialNationCostOnDelete() {
        return getBoolean(ConfigNodes.WAR_SIEGE_REFUND_INITIAL_NATION_COST_ON_DELETE);
    }
    
    public static boolean getWarSiegeTownNeutralityEnabled() {
        return getBoolean(ConfigNodes.WAR_SIEGE_TOWN_NEUTRALITY_ENABLED);
    }
    
    public static int getWarSiegeTownNeutralityConfirmationRequirementDays() {
        return getInt(ConfigNodes.WAR_SIEGE_TOWN_NEUTRALITY_CONFIRMATION_REQUIREMENT_DAYS);
    }
    
    static {
        configTownLevel = Collections.synchronizedSortedMap(new TreeMap<Integer, Map<TownLevel, Object>>(Collections.reverseOrder()));
        configNationLevel = Collections.synchronizedSortedMap(new TreeMap<Integer, Map<NationLevel, Object>>(Collections.reverseOrder()));
    }
    
    public enum TownLevel
    {
        NAME_PREFIX, 
        NAME_POSTFIX, 
        MAYOR_PREFIX, 
        MAYOR_POSTFIX, 
        TOWN_BLOCK_LIMIT, 
        UPKEEP_MULTIPLIER, 
        OUTPOST_LIMIT, 
        TOWN_BLOCK_BUY_BONUS_LIMIT;
    }
    
    public enum NationLevel
    {
        NAME_PREFIX, 
        NAME_POSTFIX, 
        CAPITAL_PREFIX, 
        CAPITAL_POSTFIX, 
        KING_PREFIX, 
        KING_POSTFIX, 
        TOWN_BLOCK_LIMIT_BONUS, 
        UPKEEP_MULTIPLIER, 
        NATION_TOWN_UPKEEP_MULTIPLIER, 
        NATIONZONES_SIZE, 
        NATION_BONUS_OUTPOST_LIMIT;
    }
}
