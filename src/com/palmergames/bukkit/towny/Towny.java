// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import org.apache.logging.log4j.LogManager;
import com.palmergames.bukkit.config.ConfigNodes;
import java.util.concurrent.Callable;
import com.palmergames.bukkit.metrics.Metrics;
import java.lang.reflect.Field;
import org.bukkit.command.CommandMap;
import com.palmergames.bukkit.towny.command.commandobjects.CancelCommand;
import com.palmergames.bukkit.towny.command.commandobjects.ConfirmCommand;
import com.palmergames.bukkit.towny.command.commandobjects.DenyCommand;
import com.palmergames.bukkit.towny.command.commandobjects.AcceptCommand;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import java.io.File;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.World;
import java.io.IOException;
import com.palmergames.util.JavaUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.chat.TNCRegister;
import org.bukkit.Bukkit;
import java.util.List;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.permissions.BukkitPermSource;
import net.milkbowl.vault.permission.Permission;
import com.palmergames.bukkit.towny.permissions.VaultPermSource;
import net.milkbowl.vault.chat.Chat;
import com.palmergames.bukkit.towny.permissions.GroupManagerSource;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.command.InviteCommand;
import com.palmergames.bukkit.towny.command.PlotCommand;
import com.palmergames.bukkit.towny.command.NationCommand;
import com.palmergames.bukkit.towny.command.TownCommand;
import com.palmergames.bukkit.towny.command.TownyCommand;
import com.palmergames.bukkit.towny.command.ResidentCommand;
import com.palmergames.bukkit.towny.command.TownyWorldCommand;
import org.bukkit.command.CommandExecutor;
import com.palmergames.bukkit.towny.command.TownyAdminCommand;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.utils.SpawnUtil;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.Collections;
import java.util.HashMap;
import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.object.PlayerCache;
import java.util.Map;
import com.palmergames.bukkit.towny.huds.HUDManager;
import com.palmergames.bukkit.towny.listeners.TownyLoginListener;
import com.palmergames.bukkit.towny.war.flagwar.listeners.TownyWarEntityListener;
import com.palmergames.bukkit.towny.war.flagwar.listeners.TownyWarCustomListener;
import com.palmergames.bukkit.towny.war.flagwar.listeners.TownyWarBlockListener;
import com.palmergames.bukkit.towny.listeners.TownyWorldListener;
import com.palmergames.bukkit.towny.listeners.TownyEntityMonitorListener;
import com.palmergames.bukkit.towny.listeners.TownyWeatherListener;
import com.palmergames.bukkit.towny.listeners.TownyEntityListener;
import com.palmergames.bukkit.towny.listeners.TownyCustomListener;
import com.palmergames.bukkit.towny.listeners.TownyBlockListener;
import com.palmergames.bukkit.towny.listeners.TownyVehicleListener;
import com.palmergames.bukkit.towny.listeners.TownyPlayerListener;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Towny extends JavaPlugin
{
    private static final Logger LOGGER;
    private String version;
    private final TownyPlayerListener playerListener;
    private final TownyVehicleListener vehicleListener;
    private final TownyBlockListener blockListener;
    private final TownyCustomListener customListener;
    private final TownyEntityListener entityListener;
    private final TownyWeatherListener weatherListener;
    private final TownyEntityMonitorListener entityMonitorListener;
    private final TownyWorldListener worldListener;
    private final TownyWarBlockListener townyWarBlockListener;
    private final TownyWarCustomListener townyWarCustomListener;
    private final TownyWarEntityListener townyWarEntityListener;
    private final TownyLoginListener loginListener;
    private final HUDManager HUDManager;
    private TownyUniverse townyUniverse;
    private Map<String, PlayerCache> playerCache;
    private Essentials essentials;
    private boolean citizens2;
    public static boolean isSpigot;
    private boolean error;
    private static Towny plugin;
    
    public Towny() {
        this.version = "2.0.0";
        this.playerListener = new TownyPlayerListener(this);
        this.vehicleListener = new TownyVehicleListener(this);
        this.blockListener = new TownyBlockListener(this);
        this.customListener = new TownyCustomListener(this);
        this.entityListener = new TownyEntityListener(this);
        this.weatherListener = new TownyWeatherListener(this);
        this.entityMonitorListener = new TownyEntityMonitorListener(this);
        this.worldListener = new TownyWorldListener(this);
        this.townyWarBlockListener = new TownyWarBlockListener(this);
        this.townyWarCustomListener = new TownyWarCustomListener(this);
        this.townyWarEntityListener = new TownyWarEntityListener();
        this.loginListener = new TownyLoginListener();
        this.HUDManager = new HUDManager(this);
        this.playerCache = Collections.synchronizedMap(new HashMap<String, PlayerCache>());
        this.essentials = null;
        this.citizens2 = false;
        this.error = false;
        Towny.plugin = this;
    }
    
    public void onEnable() {
        System.out.println("====================      Towny      ========================");
        this.version = this.getDescription().getVersion();
        this.townyUniverse = TownyUniverse.getInstance();
        Towny.isSpigot = BukkitTools.isSpigot();
        BukkitTools.initialize(this);
        TownyTimerHandler.initialize(this);
        TownyEconomyHandler.initialize(this);
        TownyFormatter.initialize(this);
        TownyRegenAPI.initialize(this);
        PlayerCacheUtil.initialize(this);
        SpawnUtil.initialize(this);
        TownyPerms.initialize(this);
        InviteHandler.initialize(this);
        ConfirmationHandler.initialize(this);
        if (this.load()) {
            this.registerSpecialCommands();
            this.getCommand("townyadmin").setExecutor((CommandExecutor)new TownyAdminCommand(this));
            this.getCommand("townyworld").setExecutor((CommandExecutor)new TownyWorldCommand(this));
            this.getCommand("resident").setExecutor((CommandExecutor)new ResidentCommand(this));
            this.getCommand("towny").setExecutor((CommandExecutor)new TownyCommand(this));
            this.getCommand("town").setExecutor((CommandExecutor)new TownCommand(this));
            this.getCommand("nation").setExecutor((CommandExecutor)new NationCommand(this));
            this.getCommand("plot").setExecutor((CommandExecutor)new PlotCommand(this));
            this.getCommand("invite").setExecutor((CommandExecutor)new InviteCommand(this));
            this.addMetricsCharts();
            TownyWar.onEnable();
            if (TownySettings.isTownyUpdating(this.getVersion())) {
                this.update();
            }
            TownyPerms.registerPermissionNodes();
        }
        this.registerEvents();
        System.out.println("=============================================================");
        if (this.isError()) {
            System.out.println("[WARNING] - ***** SAFE MODE ***** " + this.version);
        }
        else {
            System.out.println("[Towny] Version: " + this.version + " - Mod Enabled");
        }
        System.out.println("=============================================================");
        if (!this.isError()) {
            for (final Player player : BukkitTools.getOnlinePlayers()) {
                if (player != null) {
                    this.townyUniverse.onLogin(player);
                }
            }
        }
    }
    
    public void setWorldFlags() {
        final TownyUniverse universe = TownyUniverse.getInstance();
        for (final Town town : universe.getDataSource().getTowns()) {
            if (town.getWorld() == null) {
                Towny.LOGGER.warn("[Towny Error] Detected an error with the world files. Attempting to repair");
                if (town.hasHomeBlock()) {
                    try {
                        final TownyWorld world = town.getHomeBlock().getWorld();
                        if (world.hasTown(town)) {
                            continue;
                        }
                        world.addTown(town);
                        universe.getDataSource().saveTown(town);
                        universe.getDataSource().saveWorld(world);
                    }
                    catch (TownyException e) {
                        Towny.LOGGER.warn("[Towny Error] Failed get world data for: " + town.getName());
                    }
                }
                else {
                    Towny.LOGGER.warn("[Towny Error] No Homeblock - Failed to detect world for: " + town.getName());
                }
            }
        }
    }
    
    public void onDisable() {
        System.out.println("==============================================================");
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (townyUniverse.getDataSource() != null && !this.error) {
            townyUniverse.getDataSource().saveQueues();
        }
        if (!this.error) {
            TownyWar.onDisable();
        }
        if (TownyAPI.getInstance().isWarTime()) {
            TownyUniverse.getInstance().getWarEvent().toggleEnd();
        }
        TownyTimerHandler.toggleTownyRepeatingTimer(false);
        TownyTimerHandler.toggleDailyTimer(false);
        TownyTimerHandler.toggleSiegeWarTimer(false);
        TownyTimerHandler.toggleMobRemoval(false);
        TownyTimerHandler.toggleHealthRegen(false);
        TownyTimerHandler.toggleTeleportWarmup(false);
        TownyTimerHandler.toggleDrawSmokeTask(false);
        TownyRegenAPI.cancelProtectionRegenTasks();
        this.playerCache.clear();
        try {
            townyUniverse.getDataSource().cancelTask();
        }
        catch (NullPointerException ex) {}
        this.townyUniverse = null;
        System.out.println("[Towny] Version: " + this.version + " - Mod Disabled");
        System.out.println("=============================================================");
    }
    
    public boolean load() {
        if (!this.townyUniverse.loadSettings()) {
            this.setError(true);
            return false;
        }
        this.checkPlugins();
        this.setWorldFlags();
        TownyTimerHandler.toggleTownyRepeatingTimer(false);
        TownyTimerHandler.toggleDailyTimer(false);
        TownyTimerHandler.toggleSiegeWarTimer(false);
        TownyTimerHandler.toggleMobRemoval(false);
        TownyTimerHandler.toggleHealthRegen(false);
        TownyTimerHandler.toggleTeleportWarmup(false);
        TownyTimerHandler.toggleCooldownTimer(false);
        TownyTimerHandler.toggleDrawSmokeTask(false);
        TownyTimerHandler.toggleTownyRepeatingTimer(true);
        TownyTimerHandler.toggleDailyTimer(true);
        TownyTimerHandler.toggleSiegeWarTimer(true);
        TownyTimerHandler.toggleMobRemoval(true);
        TownyTimerHandler.toggleHealthRegen(TownySettings.hasHealthRegen());
        TownyTimerHandler.toggleTeleportWarmup(TownySettings.getTeleportWarmupTime() > 0);
        TownyTimerHandler.toggleCooldownTimer(TownySettings.getPVPCoolDownTime() > 0 || TownySettings.getSpawnCooldownTime() > 0);
        TownyTimerHandler.toggleDrawSmokeTask(true);
        this.resetCache();
        return true;
    }
    
    private void checkPlugins() {
        final List<String> using = new ArrayList<String>();
        Plugin test = this.getServer().getPluginManager().getPlugin("GroupManager");
        if (test != null) {
            TownyUniverse.getInstance().setPermissionSource(new GroupManagerSource(this, test));
            using.add(String.format("%s v%s", "GroupManager", test.getDescription().getVersion()));
        }
        else {
            test = this.getServer().getPluginManager().getPlugin("Vault");
            if (test != null) {
                final Chat chat = (Chat)this.getServer().getServicesManager().load((Class)Chat.class);
                if (chat == null) {
                    test = null;
                }
                else {
                    TownyUniverse.getInstance().setPermissionSource(new VaultPermSource(this, chat));
                    final RegisteredServiceProvider<Permission> vaultPermProvider = (RegisteredServiceProvider<Permission>)Towny.plugin.getServer().getServicesManager().getRegistration((Class)Permission.class);
                    if (vaultPermProvider != null) {
                        using.add(vaultPermProvider.getPlugin().getName() + " " + vaultPermProvider.getPlugin().getDescription().getVersion() + " via Vault " + test.getDescription().getVersion());
                    }
                    else {
                        using.add(String.format("%s v%s", "Vault", test.getDescription().getVersion()));
                    }
                }
            }
            if (test == null) {
                TownyUniverse.getInstance().setPermissionSource(new BukkitPermSource(this));
                using.add("BukkitPermissions");
            }
        }
        if (TownySettings.isUsingEconomy()) {
            if (TownyEconomyHandler.setupEconomy()) {
                using.add(TownyEconomyHandler.getVersion());
                if (TownyEconomyHandler.getVersion().startsWith("Essentials Economy")) {
                    System.out.println("[Towny] Warning: Essentials Economy has been known to reset town and nation bank accounts to their default amount. The authors of Essentials recommend using another economy plugin until they have fixed this bug.");
                }
            }
            else {
                TownyMessaging.sendErrorMsg("No compatible Economy plugins found. Install Vault.jar with any of the supported eco systems.");
                TownyMessaging.sendErrorMsg("If you do not want an economy to be used, set using_economy: false in your Towny config.yml.");
            }
        }
        test = this.getServer().getPluginManager().getPlugin("Essentials");
        if (test == null) {
            TownySettings.setUsingEssentials(false);
        }
        else if (TownySettings.isUsingEssentials()) {
            this.essentials = (Essentials)test;
            using.add(String.format("%s v%s", "Essentials", test.getDescription().getVersion()));
        }
        test = this.getServer().getPluginManager().getPlugin("Questioner");
        if (test != null) {
            TownyMessaging.sendErrorMsg("Questioner.jar present on server, Towny no longer requires Questioner for invites/confirmations.");
            TownyMessaging.sendErrorMsg("You may safely remove Questioner.jar from your plugins folder.");
        }
        test = this.getServer().getPluginManager().getPlugin("Citizens");
        if (test != null && this.getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            this.citizens2 = test.getDescription().getVersion().startsWith("2");
        }
        test = this.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (test != null) {
            new TownyPlaceholderExpansion(this).register();
            using.add(String.format("%s v%s", "PlaceholderAPI", test.getDescription().getVersion()));
        }
        if (using.size() > 0) {
            System.out.println("[Towny] Using: " + StringMgmt.join(using, ", "));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("TheNewChat")) {
            TNCRegister.initialize();
        }
    }
    
    private void registerEvents() {
        final PluginManager pluginManager = this.getServer().getPluginManager();
        if (!this.isError()) {
            pluginManager.registerEvents((Listener)this.townyWarBlockListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.townyWarEntityListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.HUDManager, (Plugin)this);
            pluginManager.registerEvents((Listener)this.entityMonitorListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.vehicleListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.weatherListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.townyWarCustomListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.customListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.worldListener, (Plugin)this);
            pluginManager.registerEvents((Listener)this.loginListener, (Plugin)this);
        }
        pluginManager.registerEvents((Listener)this.playerListener, (Plugin)this);
        pluginManager.registerEvents((Listener)this.blockListener, (Plugin)this);
        pluginManager.registerEvents((Listener)this.entityListener, (Plugin)this);
    }
    
    private void update() {
        try {
            final List<String> changeLog = JavaUtil.readTextFromJar("/ChangeLog.txt");
            boolean display = false;
            System.out.println("------------------------------------");
            System.out.println("[Towny] ChangeLog up until v" + this.getVersion());
            final String lastVersion = TownySettings.getLastRunVersion(this.getVersion()).split("_")[0];
            for (final String line : changeLog) {
                if (line.startsWith(lastVersion)) {
                    display = true;
                }
                if (display && line.replaceAll(" ", "").replaceAll("\t", "").length() > 0) {
                    System.out.println(line);
                }
            }
            System.out.println("------------------------------------");
        }
        catch (IOException e) {
            TownyMessaging.sendDebugMsg("Could not read ChangeLog.txt");
        }
        TownySettings.setLastRunVersion(this.getVersion());
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.getDataSource().saveAll();
        townyUniverse.getDataSource().cleanup();
    }
    
    @Deprecated
    public TownyUniverse getTownyUniverse() {
        return this.townyUniverse;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public boolean isError() {
        return this.error;
    }
    
    protected void setError(final boolean error) {
        this.error = error;
    }
    
    public boolean isEssentials() {
        return TownySettings.isUsingEssentials() && this.essentials != null;
    }
    
    public boolean isCitizens2() {
        return this.citizens2;
    }
    
    public Essentials getEssentials() throws TownyException {
        if (this.essentials == null) {
            throw new TownyException("Essentials is not installed, or not enabled!");
        }
        return this.essentials;
    }
    
    public World getServerWorld(final String name) throws NotRegisteredException {
        for (final World world : BukkitTools.getWorlds()) {
            if (world.getName().equals(name)) {
                return world;
            }
        }
        throw new NotRegisteredException(String.format("A world called '$%s' has not been registered.", name));
    }
    
    public boolean hasCache(final Player player) {
        return this.playerCache.containsKey(player.getName().toLowerCase());
    }
    
    public void newCache(final Player player) {
        try {
            this.playerCache.put(player.getName().toLowerCase(), new PlayerCache(TownyUniverse.getInstance().getDataSource().getWorld(player.getWorld().getName()), player));
        }
        catch (NotRegisteredException e) {
            TownyMessaging.sendErrorMsg(player, "Could not create permission cache for this world (" + player.getWorld().getName() + ".");
        }
    }
    
    public void deleteCache(final Player player) {
        this.deleteCache(player.getName());
    }
    
    public void deleteCache(final String name) {
        this.playerCache.remove(name.toLowerCase());
    }
    
    public PlayerCache getCache(final Player player) {
        if (!this.hasCache(player)) {
            this.newCache(player);
            this.getCache(player).setLastTownBlock(new WorldCoord(player.getWorld().getName(), Coord.parseCoord((Entity)player)));
        }
        return this.playerCache.get(player.getName().toLowerCase());
    }
    
    public void resetCache() {
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                this.getCache(player).resetAndUpdate(new WorldCoord(player.getWorld().getName(), Coord.parseCoord((Entity)player)));
            }
        }
    }
    
    public void updateCache(final WorldCoord worldCoord) {
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null && Coord.parseCoord((Entity)player).equals(worldCoord)) {
                this.getCache(player).resetAndUpdate(worldCoord);
            }
        }
    }
    
    public void updateCache() {
        WorldCoord worldCoord = null;
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord((Entity)player));
                final PlayerCache cache = this.getCache(player);
                if (cache.getLastTownBlock() == worldCoord) {
                    continue;
                }
                cache.resetAndUpdate(worldCoord);
            }
        }
    }
    
    public void updateCache(final Player player) {
        final WorldCoord worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord((Entity)player));
        final PlayerCache cache = this.getCache(player);
        if (cache.getLastTownBlock() != worldCoord) {
            cache.resetAndUpdate(worldCoord);
        }
    }
    
    public void resetCache(final Player player) {
        this.getCache(player).resetAndUpdate(new WorldCoord(player.getWorld().getName(), Coord.parseCoord((Entity)player)));
    }
    
    public void setPlayerMode(final Player player, final String[] modes, final boolean notify) {
        if (player == null) {
            return;
        }
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            resident.setModes(modes, notify);
        }
        catch (NotRegisteredException ex) {}
    }
    
    public void removePlayerMode(final Player player) {
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            resident.clearModes();
        }
        catch (NotRegisteredException ex) {}
    }
    
    public List<String> getPlayerMode(final Player player) {
        return this.getPlayerMode(player.getName());
    }
    
    public List<String> getPlayerMode(final String name) {
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(name);
            return resident.getModes();
        }
        catch (NotRegisteredException e) {
            return null;
        }
    }
    
    public boolean hasPlayerMode(final Player player, final String mode) {
        return this.hasPlayerMode(player.getName(), mode);
    }
    
    public boolean hasPlayerMode(final String name, final String mode) {
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(name);
            return resident.hasMode(mode);
        }
        catch (NotRegisteredException e) {
            return false;
        }
    }
    
    public String getConfigPath() {
        return this.getDataFolder().getPath() + File.separator + "settings" + File.separator + "config.yml";
    }
    
    public Object getSetting(final String root) {
        return TownySettings.getProperty(root);
    }
    
    public void log(final String msg) {
        if (TownySettings.isLogging()) {
            Towny.LOGGER.info(ChatColor.stripColor(msg));
        }
    }
    
    public boolean parseOnOff(final String s) throws Exception {
        if (s.equalsIgnoreCase("on")) {
            return true;
        }
        if (s.equalsIgnoreCase("off")) {
            return false;
        }
        throw new Exception(String.format(TownySettings.getLangString("msg_err_invalid_input"), " on/off."));
    }
    
    public static Towny getPlugin() {
        return Towny.plugin;
    }
    
    public TownyPlayerListener getPlayerListener() {
        return this.playerListener;
    }
    
    public TownyVehicleListener getVehicleListener() {
        return this.vehicleListener;
    }
    
    public TownyEntityListener getEntityListener() {
        return this.entityListener;
    }
    
    public TownyWeatherListener getWeatherListener() {
        return this.weatherListener;
    }
    
    public TownyEntityMonitorListener getEntityMonitorListener() {
        return this.entityMonitorListener;
    }
    
    public TownyWorldListener getWorldListener() {
        return this.worldListener;
    }
    
    public TownyWarBlockListener getTownyWarBlockListener() {
        return this.townyWarBlockListener;
    }
    
    public TownyWarCustomListener getTownyWarCustomListener() {
        return this.townyWarCustomListener;
    }
    
    public TownyWarEntityListener getTownyWarEntityListener() {
        return this.townyWarEntityListener;
    }
    
    public HUDManager getHUDManager() {
        return this.HUDManager;
    }
    
	private void registerSpecialCommands() {
		List<Command> commands = new ArrayList<>();
		commands.add(new AcceptCommand(TownySettings.getAcceptCommand()));
		commands.add(new DenyCommand(TownySettings.getDenyCommand()));
		commands.add(new ConfirmCommand(TownySettings.getConfirmCommand()));
		commands.add(new CancelCommand(TownySettings.getCancelCommand()));
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			commandMap.registerAll("towny", commands);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
        }
    }
    
    private void addMetricsCharts() {
        final Metrics metrics = new Metrics((Plugin)this);
        metrics.addCustomChart(new Metrics.SimplePie("language", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return TownySettings.getString(ConfigNodes.LANGUAGE);
            }
        }));
        metrics.addCustomChart(new Metrics.SimplePie("server_type", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (Bukkit.getServer().getName().equalsIgnoreCase("paper")) {
                    return "Paper";
                }
                if (!Bukkit.getServer().getName().equalsIgnoreCase("craftbukkit")) {
                    return "Unknown";
                }
                if (Towny.isSpigot) {
                    return "Spigot";
                }
                return "CraftBukkit";
            }
        }));
        metrics.addCustomChart(new Metrics.SimplePie("nation_zones_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (TownySettings.getNationZonesEnabled()) {
                    return "true";
                }
                return "false";
            }
        }));
        metrics.addCustomChart(new Metrics.SimplePie("database_type", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return TownySettings.getSaveDatabase();
            }
        }));
        metrics.addCustomChart(new Metrics.SimplePie("nation_zones_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return String.valueOf(TownySettings.getNationZonesEnabled());
            }
        }));
        metrics.addCustomChart(new Metrics.SimplePie("database_type", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return TownySettings.getSaveDatabase();
            }
        }));
        metrics.addCustomChart(new Metrics.SimplePie("town_block_size", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return String.valueOf(TownySettings.getTownBlockSize());
            }
        }));
    }
    
    static {
        LOGGER = LogManager.getLogger((Class)Towny.class);
        Towny.isSpigot = false;
    }
}
