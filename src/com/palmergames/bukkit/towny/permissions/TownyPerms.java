package com.palmergames.bukkit.towny.permissions;

import java.util.LinkedList;
import java.util.ListIterator;
import org.bukkit.configuration.MemorySection;
import org.bukkit.permissions.PermissionDefault;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.util.FileMgmt;
import java.io.File;
import java.lang.reflect.Field;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.config.CommentedConfiguration;
import org.bukkit.permissions.PermissionAttachment;
import java.util.HashMap;
import org.bukkit.permissions.Permission;
import java.util.LinkedHashMap;

public class TownyPerms
{
    protected static LinkedHashMap<String, Permission> registeredPermissions;
    protected static HashMap<String, PermissionAttachment> attachments;
    private static CommentedConfiguration perms;
    private static Towny plugin;
    private static Field permissions;
    
    public static void initialize(final Towny plugin) {
        TownyPerms.plugin = plugin;
    }
    
    public static void loadPerms(final String filepath, final String defaultRes) {
        final String fullPath = filepath + File.separator + defaultRes;
        final File file = FileMgmt.unpackResourceFile(fullPath, defaultRes, defaultRes);
        if (file != null) {
            (TownyPerms.perms = new CommentedConfiguration(file)).load();
        }
        collectPermissions();
    }
    
    public static void assignPermissions(Resident resident, Player player) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Label_0041: {
            if (resident == null) {
                try {
                    resident = townyUniverse.getDataSource().getResident(player.getName());
                    break Label_0041;
                }
                catch (NotRegisteredException e) {
                    e.printStackTrace();
                    return;
                }
            }
            player = BukkitTools.getPlayer(resident.getName());
        }
        if (player == null || !player.isOnline()) {
            TownyPerms.attachments.remove(resident.getName());
            return;
        }
        TownyWorld World;
        try {
            World = townyUniverse.getDataSource().getWorld(player.getLocation().getWorld().getName());
        }
        catch (NotRegisteredException e2) {
            e2.printStackTrace();
            return;
        }
        PermissionAttachment playersAttachment;
        if (TownyPerms.attachments.containsKey(resident.getName())) {
            playersAttachment = TownyPerms.attachments.get(resident.getName());
        }
        else {
            try {
                playersAttachment = BukkitTools.getPlayer(resident.getName()).addAttachment((Plugin)TownyPerms.plugin);
            }
            catch (Exception e3) {
                return;
            }
        }
        try {
            synchronized (playersAttachment) {
                final Map<String, Boolean> orig = (Map<String, Boolean>)TownyPerms.permissions.get(playersAttachment);
                orig.clear();
                if (World.isUsingTowny()) {
                    orig.putAll(getResidentPerms(resident));
                }
                playersAttachment.getPermissible().recalculatePermissions();
            }
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        TownyPerms.attachments.put(resident.getName(), playersAttachment);
    }
    
    public static void removeAttachment(final String name) {
        if (TownyPerms.attachments.containsKey(name)) {
            TownyPerms.attachments.remove(name);
        }
    }
    
    public static void updateOnlinePerms() {
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            assignPermissions(null, player);
        }
    }
    
    public static void updateTownPerms(final Town town) {
        for (final Resident resident : town.getResidents()) {
            assignPermissions(resident, null);
        }
    }
    
    public static void updateNationPerms(final Nation nation) {
        for (final Town town : nation.getTowns()) {
            updateTownPerms(town);
        }
    }
    
    private static List<String> getList(final String path) {
        if (TownyPerms.perms.contains(path)) {
            return (List<String>)TownyPerms.perms.getStringList(path);
        }
        return null;
    }
    
    public static LinkedHashMap<String, Boolean> getResidentPerms(final Resident resident) {
        final Set<String> permList = new HashSet<String>();
        permList.addAll(getDefault());
        if (resident.hasTown()) {
            try {
                permList.addAll(getTownDefault(resident.getTown()));
            }
            catch (NotRegisteredException ex) {}
            if (resident.isMayor()) {
                permList.addAll(getTownMayor());
            }
            for (final String rank : resident.getTownRanks()) {
                permList.addAll(getTownRank(rank));
            }
            if (resident.hasNation()) {
                permList.addAll(getNationDefault());
                if (resident.isKing()) {
                    permList.addAll(getNationKing());
                }
                for (final String rank : resident.getNationRanks()) {
                    permList.addAll(getNationRank(rank));
                }
            }
        }
        final List<String> playerPermArray = sort(new ArrayList<String>(permList));
        final LinkedHashMap<String, Boolean> newPerms = new LinkedHashMap<String, Boolean>();
        Boolean value = false;
        for (final String permission : playerPermArray) {
            if (permission.contains("{townname}")) {
                if (!resident.hasTown()) {
                    continue;
                }
                try {
                    final String placeholderPerm = permission.replace("{townname}", resident.getTown().getName().toLowerCase());
                    newPerms.put(placeholderPerm, true);
                }
                catch (NotRegisteredException ex2) {}
            }
            else if (permission.contains("{nationname}")) {
                if (!resident.hasNation()) {
                    continue;
                }
                try {
                    final String placeholderPerm = permission.replace("{nationname}", resident.getTown().getNation().getName().toLowerCase());
                    newPerms.put(placeholderPerm, true);
                }
                catch (NotRegisteredException ex3) {}
            }
            else {
                value = !permission.startsWith("-");
                newPerms.put(((boolean)value) ? permission : permission.substring(1), value);
            }
        }
        return newPerms;
    }
    
    public static void registerPermissionNodes() {
        TownyPerms.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)TownyPerms.plugin, (Runnable)new Runnable() {
            @Override
            public void run() {
                for (final String rank : TownyPerms.getTownRanks()) {
                    final Permission perm = new Permission(PermissionNodes.TOWNY_COMMAND_TOWN_RANK.getNode(rank), "User can grant this town rank to others..", PermissionDefault.FALSE, (Map)null);
                    perm.addParent(PermissionNodes.TOWNY_COMMAND_TOWN_RANK.getNode(), true);
                }
                for (final String rank : TownyPerms.getNationRanks()) {
                    final Permission perm = new Permission(PermissionNodes.TOWNY_COMMAND_NATION_RANK.getNode(rank), "User can grant this town rank to others..", PermissionDefault.FALSE, (Map)null);
                    perm.addParent(PermissionNodes.TOWNY_COMMAND_NATION_RANK.getNode(), true);
                }
            }
        }, 1L);
    }
    
    public static List<String> getDefault() {
        final List<String> permsList = getList("nomad");
        return (permsList == null) ? new ArrayList<String>() : permsList;
    }
    
    public static List<String> getTownRanks() {
        return new ArrayList<String>(((MemorySection)TownyPerms.perms.get("towns.ranks")).getKeys(false));
    }
    
    public static List<String> getTownDefault(final Town town) {
        final List<String> permsList = getList("towns.default");
        if (permsList == null) {
            final List<String> emptyPermsList = new ArrayList<String>();
            emptyPermsList.add("towny.town." + town.getName().toLowerCase());
            return emptyPermsList;
        }
        permsList.add("towny.town." + town.getName().toLowerCase());
        return permsList;
    }
    
    public static List<String> getTownMayor() {
        final List<String> permsList = getList("towns.mayor");
        return (permsList == null) ? new ArrayList<String>() : permsList;
    }
    
    public static List<String> getTownRank(final String rank) {
        final List<String> permsList = getList("towns.ranks." + rank);
        return (permsList == null) ? new ArrayList<String>() : permsList;
    }
    
    public static List<String> getNationRanks() {
        return new ArrayList<String>(((MemorySection)TownyPerms.perms.get("nations.ranks")).getKeys(false));
    }
    
    public static List<String> getNationDefault() {
        final List<String> permsList = getList("nations.default");
        return (permsList == null) ? new ArrayList<String>() : permsList;
    }
    
    public static List<String> getNationKing() {
        final List<String> permsList = getList("nations.king");
        return (permsList == null) ? new ArrayList<String>() : permsList;
    }
    
    public static List<String> getNationRank(final String rank) {
        final List<String> permsList = getList("nations.ranks." + rank);
        return (permsList == null) ? new ArrayList<String>() : permsList;
    }
    
    public static void collectPermissions() {
        TownyPerms.registeredPermissions.clear();
        for (final Permission perm : BukkitTools.getPluginManager().getPermissions()) {
            TownyPerms.registeredPermissions.put(perm.getName().toLowerCase(), perm);
        }
    }
    
    private static List<String> sort(final List<String> permList) {
        final List<String> result = new ArrayList<String>();
        for (final String key : permList) {
            final String a = (key.charAt(0) == '-') ? key.substring(1) : key;
            final Map<String, Boolean> allchildren = getAllChildren(a, new HashSet<String>());
            if (allchildren != null) {
                final ListIterator<String> itr = result.listIterator();
                while (itr.hasNext()) {
                    final String node = itr.next();
                    final String b = (node.charAt(0) == '-') ? node.substring(1) : node;
                    if (allchildren.containsKey(b)) {
                        itr.set(key);
                        itr.add(node);
                        break;
                    }
                }
            }
            if (!result.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }
    
    public List<String> getAllRegisteredPermissions(final boolean includeChildren) {
        final List<String> perms = new ArrayList<String>();
        for (final String key : TownyPerms.registeredPermissions.keySet()) {
            if (!perms.contains(key)) {
                perms.add(key);
                if (!includeChildren) {
                    continue;
                }
                final Map<String, Boolean> children = getAllChildren(key, new HashSet<String>());
                if (children == null) {
                    continue;
                }
                for (final String node : children.keySet()) {
                    if (!perms.contains(node)) {
                        perms.add(node);
                    }
                }
            }
        }
        return perms;
    }
    
    public static Map<String, Boolean> getAllChildren(final String node, final Set<String> playerPermArray) {
        final LinkedList<String> stack = new LinkedList<String>();
        final Map<String, Boolean> alreadyVisited = new HashMap<String, Boolean>();
        stack.push(node);
        alreadyVisited.put(node, true);
        while (!stack.isEmpty()) {
            final String now = stack.pop();
            final Map<String, Boolean> children = getChildren(now);
            if (children != null && !playerPermArray.contains("-" + now)) {
                for (final String childName : children.keySet()) {
                    if (!alreadyVisited.containsKey(childName)) {
                        stack.push(childName);
                        alreadyVisited.put(childName, children.get(childName));
                    }
                }
            }
        }
        alreadyVisited.remove(node);
        if (!alreadyVisited.isEmpty()) {
            return alreadyVisited;
        }
        return null;
    }
    
    public static Map<String, Boolean> getChildren(final String node) {
        final Permission perm = TownyPerms.registeredPermissions.get(node.toLowerCase());
        if (perm == null) {
            return null;
        }
        return (Map<String, Boolean>)perm.getChildren();
    }
    
    static {
        TownyPerms.registeredPermissions = new LinkedHashMap<String, Permission>();
        TownyPerms.attachments = new HashMap<String, PermissionAttachment>();
        try {
            (TownyPerms.permissions = PermissionAttachment.class.getDeclaredField("permissions")).setAccessible(true);
        }
        catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
