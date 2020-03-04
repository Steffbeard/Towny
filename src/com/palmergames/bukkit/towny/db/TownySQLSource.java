// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.db;

import com.palmergames.bukkit.towny.TownyAPI;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import java.util.UUID;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import java.util.Arrays;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import java.io.BufferedReader;
import java.io.FileReader;
import org.bukkit.World;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.sql.ResultSet;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.sql.SQLException;
import com.palmergames.bukkit.util.BukkitTools;
import java.sql.DriverManager;
import java.sql.Driver;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.util.FileMgmt;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;
import java.sql.Connection;
import org.bukkit.scheduler.BukkitTask;
import java.util.Queue;

public final class TownySQLSource extends TownyDatabaseHandler
{
    private final Queue<SQL_Task> queryQueue = new ConcurrentLinkedQueue<>();
    private BukkitTask task = null;
    
    private final String dsn;
    private final String db_name;
    private final String username;
    private final String password;
    private final String tb_prefix;
    
    
    private Connection cntx = null;
    private final String type;

    // private boolean ish2 = false;
    
    public TownySQLSource(Towny plugin, TownyUniverse universe, String type) {
        super(plugin, universe);
        this.type = type;
		if (!FileMgmt.checkOrCreateFolders(
			rootFolderPath,
			dataFolderPath,
			dataFolderPath + File.separator + "plot-block-data"
		) || !FileMgmt.checkOrCreateFiles(
			dataFolderPath + File.separator + "regen.txt",
			dataFolderPath + File.separator + "snapshot_queue.txt"
		)) {
			TownyMessaging.sendErrorMsg("Could not create flatfile default files and folders.");
		
		}
        /*
         *  Setup SQL connection
         */
        String hostname = TownySettings.getSQLHostName();
        String port = TownySettings.getSQLPort();
        db_name = TownySettings.getSQLDBName();
        tb_prefix = TownySettings.getSQLTablePrefix().toUpperCase();
    
        String driver1;
        if (this.type.equals("h2")) {
        
            driver1 = "org.h2.Driver";
            this.dsn = ("jdbc:h2:" + dataFolderPath + File.separator + db_name + ".h2db;AUTO_RECONNECT=TRUE");
            username = "sa";
            password = "sa";
        
        } else if (this.type.equals("mysql")) {
        
            driver1 = "com.mysql.jdbc.Driver";
            if (TownySettings.getSQLUsingSSL())
                this.dsn = ("jdbc:mysql://" + hostname + ":" + port + "/" + db_name + "?useUnicode=true&characterEncoding=utf-8");
            else
                this.dsn = ("jdbc:mysql://" + hostname + ":" + port + "/" + db_name + "?verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf-8");
            username = TownySettings.getSQLUsername();
            password = TownySettings.getSQLPassword();
        
        } else {
        
            driver1 = "org.sqlite.JDBC";
            this.dsn = ("jdbc:sqlite:" + dataFolderPath + File.separator + db_name + ".sqldb");
            username = "";
            password = "";
        
        }
    
        /*
         * Register the driver (if possible)
         */
        try {
            Driver driver = (Driver) Class.forName(driver1).newInstance();
            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            System.out.println("[Towny] Driver error: " + e);
        }
    
        /*
         * Attempt to get a connection to the database
         */
        if (getContext()) {
        
            TownyMessaging.sendDebugMsg("[Towny] Connected to Database");
        
        } else {
        
            TownyMessaging.sendErrorMsg("Failed when connecting to Database");
            return;
        
        }
    
        /*
         *  Initialise database Schema.
         */
        SQL_Schema.initTables(cntx, db_name);
    
        /*
         * Start our Async queue for pushing data to the database.
         */
        task = BukkitTools.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
        
            while (!TownySQLSource.this.queryQueue.isEmpty()) {
            
                SQL_Task query = TownySQLSource.this.queryQueue.poll();
            
                if (query.update) {
                
                    TownySQLSource.this.QueueUpdateDB(query.tb_name, query.args, query.keys);
                
                } else {
                
                    TownySQLSource.this.QueueDeleteDB(query.tb_name, query.args);
                
                }
            
            }
        
        }, 5L, 5L);
    }
    
    @Override
    public void cancelTask() {
        this.task.cancel();
    }
    
    public boolean getContext() {
        try {
            if (this.cntx == null || this.cntx.isClosed() || (!this.type.equals("sqlite") && !this.cntx.isValid(1))) {
                if (this.cntx != null && !this.cntx.isClosed()) {
                    try {
                        this.cntx.close();
                    }
                    catch (SQLException ex) {}
                    this.cntx = null;
                }
                if (this.username.equalsIgnoreCase("") && this.password.equalsIgnoreCase("")) {
                    this.cntx = DriverManager.getConnection(this.dsn);
                }
                else {
                    this.cntx = DriverManager.getConnection(this.dsn, this.username, this.password);
                }
                return this.cntx != null && !this.cntx.isClosed();
            }
            return true;
        }
        catch (SQLException e) {
            TownyMessaging.sendErrorMsg("Error could not Connect to db " + this.dsn + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean UpdateDB(final String tb_name, final HashMap<String, Object> args, final List<String> keys) {
        this.queryQueue.add(new SQL_Task(tb_name, args, keys));
        return true;
    }
    
    public boolean QueueUpdateDB(String tb_name, HashMap<String, Object> args, List<String> keys) {

		/*
		 *  Attempt to get a database connection.
		 */
        if (!getContext())
            return false;

        StringBuilder code;
        PreparedStatement stmt = null;
        List<Object> parameters = new ArrayList<>();
        int rs = 0;

        try {

            if (keys == null) {

				/*
				 * No keys so this is an INSERT not an UPDATE.
				 */

                // Push all values to a parameter list.

                parameters.addAll(args.values());

                String[] aKeys = args.keySet().toArray(new String[0]);

                // Build the prepared statement string appropriate for
                // the number of keys/values we are inserting.

                code = new StringBuilder("REPLACE INTO " + tb_prefix + (tb_name.toUpperCase()) + " ");
                StringBuilder keycode = new StringBuilder("(");
                StringBuilder valuecode = new StringBuilder(" VALUES (");

                for (int count = 0; count < args.size(); count++) {

                    keycode.append("`").append(aKeys[count]).append("`");
                    valuecode.append("?");

                    if ((count < (args.size() - 1))) {
                        keycode.append(", ");
                        valuecode.append(",");
                    } else {
                        keycode.append(")");
                        valuecode.append(")");
                    }
                }

                code.append(keycode);
                code.append(valuecode);

            } else {

				/*
				 * We have keys so this is a conditional UPDATE.
				 */

                String[] aKeys = args.keySet().toArray(new String[0]);

                // Build the prepared statement string appropriate for
                // the number of keys/values we are inserting.

                code = new StringBuilder("UPDATE " + tb_prefix + (tb_name.toUpperCase()) + " SET ");

                for (int count = 0; count < args.size(); count++) {

                    code.append("`").append(aKeys[count]).append("` = ?");

                    // Push value for each entry.

                    parameters.add(args.get(aKeys[count]));

                    if ((count < (args.size() - 1))) {
                        code.append(",");
                    }
                }

                code.append(" WHERE ");

                for (int count = 0; count < keys.size(); count++) {

                    code.append("`").append(keys.get(count)).append("` = ?");

                    // Add extra values for the WHERE conditionals.

                    parameters.add(args.get(keys.get(count)));

                    if ((count < (keys.size() - 1))) {
                        code.append(" AND ");
                    }
                }

            }

            // Populate the prepared statement parameters.

            stmt = cntx.prepareStatement(code.toString());

            for (int count = 0; count < parameters.size(); count++) {

                Object element = parameters.get(count);

                if (element instanceof String) {

                    stmt.setString(count + 1, (String) element);

                } else if (element instanceof Boolean) {

                    stmt.setString(count + 1, ((Boolean) element) ? "1" : "0");

                } else {

                    stmt.setObject(count + 1, element.toString());

                }

            }

            rs = stmt.executeUpdate();

        } catch (SQLException e) {

            TownyMessaging.sendErrorMsg("SQL: " + e.getMessage() + " --> " + stmt.toString());

        } finally {

            try {

                if (stmt != null) {
                    stmt.close();
                }

                if (rs == 0) // if entry doesn't exist then try to insert
                    return UpdateDB(tb_name, args, null);

            } catch (SQLException e) {
                TownyMessaging.sendErrorMsg("SQL closing: " + e.getMessage() + " --> " + stmt.toString());
            }

        }

        // Failed?
        return rs != 0;

        // Success!
    
    }
    
    public boolean DeleteDB(final String tb_name, final HashMap<String, Object> args) {
        this.queryQueue.add(new SQL_Task(tb_name, args));
        return true;
    }
    
    public boolean QueueDeleteDB(String tb_name, HashMap<String, Object> args) {

        if (!getContext())
            return false;
        try {
            StringBuilder wherecode = new StringBuilder("DELETE FROM " + tb_prefix + (tb_name.toUpperCase()) + " WHERE ");
            Set<Map.Entry<String, Object>> set = args.entrySet();
            Iterator<Map.Entry<String, Object>> i = set.iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> me = i.next();
                wherecode.append("`").append(me.getKey()).append("` = ");
                if (me.getValue() instanceof String)
                    wherecode.append("'").append(((String) me.getValue()).replace("'", "\''")).append("'");
                else if (me.getValue() instanceof Boolean)
                    wherecode.append("'").append(((Boolean) me.getValue()) ? "1" : "0").append("'");
                else
                    wherecode.append("'").append(me.getValue()).append("'");

                wherecode.append(i.hasNext() ? " AND " : "");
            }
            Statement s = cntx.createStatement();
            int rs = s.executeUpdate(wherecode.toString());
            s.close();
            if (rs == 0) {
                TownyMessaging.sendDebugMsg("SQL: delete returned 0: " + wherecode);
            }
        } catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: Error delete : " + e.getMessage());
        }
        return false;
    }


    /*
     * Load keys
     */
    @Override
    public boolean loadTownBlockList() {

        TownyMessaging.sendDebugMsg("Loading TownBlock List");
        if (!getContext())
            return false;
        try {
            Statement s = cntx.createStatement();
            ResultSet rs = s.executeQuery("SELECT world,x,z FROM " + tb_prefix + "TOWNBLOCKS");

            while (rs.next()) {

                TownyWorld world = getWorld(rs.getString("world"));
                int x = Integer.parseInt(rs.getString("x"));
                int z = Integer.parseInt(rs.getString("z"));

                try {
                    world.newTownBlock(x, z);
                } catch (AlreadyRegisteredException ignored) {
                }

            }

            s.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;


    }

    @Override
    public boolean loadResidentList() {

        TownyMessaging.sendDebugMsg("Loading Resident List");
        if (!getContext())
            return false;
        try {
            Statement s = cntx.createStatement();
            ResultSet rs = s.executeQuery("SELECT name FROM " + tb_prefix + "RESIDENTS");

            while (rs.next()) {
                try {
                    newResident(rs.getString("name"));
                } catch (AlreadyRegisteredException ignored) {
                }
            }
            s.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loadTownList() {

        TownyMessaging.sendDebugMsg("Loading Town List");
        if (!getContext())
            return false;
        try {
            Statement s = cntx.createStatement();
            ResultSet rs = s.executeQuery("SELECT name FROM " + tb_prefix + "TOWNS");

            while (rs.next()) {
                try {
                    newTown(rs.getString("name"));
                } catch (AlreadyRegisteredException ignored) {
                }
            }
            s.close();
            return true;
        } catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: town list sql error : " + e.getMessage());
        } catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: town list unknown error: ");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loadNationList() {

        TownyMessaging.sendDebugMsg("Loading Nation List");
        if (!getContext())
            return false;
        try {
            Statement s = cntx.createStatement();
            ResultSet rs = s.executeQuery("SELECT name FROM " + tb_prefix + "NATIONS");
            while (rs.next()) {
                try {
                    newNation(rs.getString("name"));
                } catch (AlreadyRegisteredException ignored) {
                }
            }
            s.close();
            return true;
        } catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: nation list sql error : " + e.getMessage());
        } catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: nation list unknown error : ");
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean loadSiegeZoneList() {
        TownyMessaging.sendDebugMsg("Loading Siege zones List");
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT siegeZoneName FROM " + this.tb_prefix + "SIEGEZONES");
            while (rs.next()) {
                try {
                    final String siegeZoneName = rs.getString("siegeZoneName").toLowerCase();
                    this.newSiegeZone(siegeZoneName);
                }
                catch (AlreadyRegisteredException e) {
                    e.printStackTrace();
                }
            }
            s.close();
            return true;
        }
        catch (SQLException e2) {
            TownyMessaging.sendErrorMsg("SQL: siege zone list sql error : " + e2.getMessage());
        }
        catch (Exception e3) {
            TownyMessaging.sendErrorMsg("SQL: siege zone list unknown error : ");
            e3.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean loadWorldList() {
        TownyMessaging.sendDebugMsg("Loading World List");
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT name FROM " + this.tb_prefix + "WORLDS");
            while (rs.next()) {
                try {
                    this.newWorld(rs.getString("name"));
                }
                catch (AlreadyRegisteredException ex) {}
            }
            s.close();
        }
        catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: world list sql error : " + e.getMessage());
        }
        catch (Exception e2) {
            TownyMessaging.sendErrorMsg("SQL: world list unknown error : ");
            e2.printStackTrace();
        }
        if (this.plugin != null) {
            for (final World world : this.plugin.getServer().getWorlds()) {
                try {
                    this.newWorld(world.getName());
                }
                catch (AlreadyRegisteredException ex2) {}
            }
        }
        return true;
    }
    
    @Override
    public boolean loadRegenList() {
        TownyMessaging.sendDebugMsg("Loading Regen List");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new FileReader(this.dataFolderPath + File.separator + "regen.txt"));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        final String[] split = line.split(",");
                        final PlotBlockData plotData = this.loadPlotData(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        if (plotData == null) {
                            continue;
                        }
                        TownyRegenAPI.addPlotChunk(plotData, false);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading Regen List at " + line + ", in towny\\data\\regen.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadSnapshotList() {
        TownyMessaging.sendDebugMsg("Loading Snapshot Queue");
        String line = null;
        try {
            final BufferedReader fin = new BufferedReader(new FileReader(this.dataFolderPath + File.separator + "snapshot_queue.txt"));
            try {
                while ((line = fin.readLine()) != null) {
                    if (!line.equals("")) {
                        final String[] split = line.split(",");
                        final WorldCoord worldCoord = new WorldCoord(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                        TownyRegenAPI.addWorldCoord(worldCoord);
                    }
                }
                final boolean b = true;
                fin.close();
                return b;
            }
            catch (Throwable t) {
                try {
                    fin.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Error Loading Snapshot Queue List at " + line + ", in towny\\data\\snapshot_queue.txt");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadResident(final Resident resident) {
        TownyMessaging.sendDebugMsg("Loading resident " + resident.getName());
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "RESIDENTS  WHERE name='" + resident.getName() + "'");
            if (rs.next()) {
                try {
                    resident.setLastOnline(rs.getLong("lastOnline"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setRegistered(rs.getLong("registered"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setNPC(rs.getBoolean("isNPC"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setJailed(rs.getBoolean("isJailed"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setJailSpawn(rs.getInt("JailSpawn"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setJailDays(rs.getInt("JailDays"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setJailTown(rs.getString("JailTown"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setTitle(rs.getString("title"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    resident.setSurname(rs.getString("surname"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                String line = rs.getString("town");
                if (line != null && !line.isEmpty()) {
                    resident.setTown(this.getTown(line));
                    TownyMessaging.sendDebugMsg("Resident " + resident.getName() + " set to Town " + line);
                }
                line = rs.getString("town-ranks");
                if (line != null && !line.isEmpty()) {
                    final String search = line.contains("#") ? "#" : ",";
                    resident.setTownRanks(new ArrayList<String>(Arrays.asList(line.split(search))));
                    TownyMessaging.sendDebugMsg("Resident " + resident.getName() + " set Town-ranks " + line);
                }
                line = rs.getString("nation-ranks");
                if (line != null && !line.isEmpty()) {
                    final String search = line.contains("#") ? "#" : ",";
                    resident.setNationRanks(new ArrayList<String>(Arrays.asList(line.split(search))));
                    TownyMessaging.sendDebugMsg("Resident " + resident.getName() + " set Nation-ranks " + line);
                }
                try {
                    line = rs.getString("friends");
                    if (line != null) {
                        final String search = line.contains("#") ? "#" : ",";
                        final String[] split;
                        split = line.split(search);
                        for (final String token : split) {
                            if (!token.isEmpty()) {
                                final Resident friend = this.getResident(token);
                                if (friend != null) {
                                    resident.addFriend(friend);
                                }
                            }
                        }
                    }
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    resident.setPermissions(rs.getString("protectionStatus").replaceAll("#", ","));
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    line = rs.getString("metadata");
                    if (line != null && !line.isEmpty()) {
                        resident.setMetadata(line);
                    }
                }
                catch (SQLException ex) {}
                try {
                    line = rs.getString("townBlocks");
                    if (line != null && !line.isEmpty()) {
                        this.utilLoadTownBlocks(line, null, resident);
                    }
                }
                catch (SQLException ex2) {}
                s.close();
                return true;
            }
            return false;
        }
        catch (SQLException e3) {
            TownyMessaging.sendErrorMsg("SQL: Load resident sql error : " + e3.getMessage());
        }
        catch (Exception e4) {
            TownyMessaging.sendErrorMsg("SQL: Load resident unknown error");
            e4.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean loadTown(final Town town) {
        TownyMessaging.sendDebugMsg("Loading town " + town.getName());
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "TOWNS  WHERE name='" + town.getName() + "'");
            if (rs.next()) {
                String line = rs.getString("residents");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] split;
                    split = line.split(search);
                    for (final String token : split) {
                        if (!token.isEmpty()) {
                            final Resident resident = this.getResident(token);
                            if (resident != null) {
                                town.addResident(resident);
                            }
                        }
                    }
                }
                line = rs.getString("mayor");
                if (line != null && !line.isEmpty()) {
                    town.setMayor(this.getResident(line));
                }
                town.setTownBoard(rs.getString("townBoard"));
                line = rs.getString("tag");
                if (line != null) {
                    try {
                        town.setTag(line);
                    }
                    catch (TownyException e4) {
                        town.setTag("");
                    }
                }
                town.setPermissions(rs.getString("protectionStatus").replaceAll("#", ","));
                town.setBonusBlocks(rs.getInt("bonus"));
                town.setTaxPercentage(rs.getBoolean("taxpercent"));
                town.setTaxes(rs.getFloat("taxes"));
                town.setHasUpkeep(rs.getBoolean("hasUpkeep"));
                town.setPlotPrice(rs.getFloat("plotPrice"));
                town.setPlotTax(rs.getFloat("plotTax"));
                town.setEmbassyPlotPrice(rs.getFloat("embassyPlotPrice"));
                town.setEmbassyPlotTax(rs.getFloat("embassyPlotTax"));
                town.setCommercialPlotPrice(rs.getFloat("commercialPlotPrice"));
                town.setCommercialPlotTax(rs.getFloat("commercialPlotTax"));
                town.setSpawnCost(rs.getFloat("spawnCost"));
                town.setOpen(rs.getBoolean("open"));
                town.setPublic(rs.getBoolean("public"));
                town.setConquered(rs.getBoolean("conquered"));
                town.setAdminDisabledPVP(rs.getBoolean("admindisabledpvp"));
                town.setAdminEnabledPVP(rs.getBoolean("adminenabledpvp"));
                town.setPurchasedBlocks(rs.getInt("purchased"));
                line = rs.getString("homeBlock");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] tokens = line.split(search);
                    if (tokens.length == 3) {
                        try {
                            final TownyWorld world = this.getWorld(tokens[0]);
                            try {
                                final int x = Integer.parseInt(tokens[1]);
                                final int z = Integer.parseInt(tokens[2]);
                                final TownBlock homeBlock = world.getTownBlock(x, z);
                                town.forceSetHomeBlock(homeBlock);
                            }
                            catch (NumberFormatException e5) {
                                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " homeBlock tried to load invalid location.");
                            }
                            catch (NotRegisteredException e6) {
                                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " homeBlock tried to load invalid TownBlock.");
                            }
                            catch (TownyException e7) {
                                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " does not have a home block.");
                            }
                        }
                        catch (NotRegisteredException e8) {
                            TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " homeBlock tried to load invalid world.");
                        }
                    }
                }
                line = rs.getString("spawn");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] tokens = line.split(search);
                    if (tokens.length >= 4) {
                        try {
                            final World world2 = this.plugin.getServerWorld(tokens[0]);
                            final double x2 = Double.parseDouble(tokens[1]);
                            final double y = Double.parseDouble(tokens[2]);
                            final double z2 = Double.parseDouble(tokens[3]);
                            final Location loc = new Location(world2, x2, y, z2);
                            if (tokens.length == 6) {
                                loc.setPitch(Float.parseFloat(tokens[4]));
                                loc.setYaw(Float.parseFloat(tokens[5]));
                            }
                            town.forceSetSpawn(loc);
                        }
                        catch (NumberFormatException ex3) {}
                        catch (NullPointerException ex4) {}
                        catch (NotRegisteredException ex5) {}
                    }
                }
                line = rs.getString("outpostSpawns");
                if (line != null) {
                    final String[] split2;
                    split2 = line.split(";");
                    for (final String spawn : split2) {
                        final String search = line.contains("#") ? "#" : ",";
                        final String[] tokens = spawn.split(search);
                        if (tokens.length >= 4) {
                            try {
                                final World world3 = this.plugin.getServerWorld(tokens[0]);
                                final double x3 = Double.parseDouble(tokens[1]);
                                final double y2 = Double.parseDouble(tokens[2]);
                                final double z3 = Double.parseDouble(tokens[3]);
                                final Location loc2 = new Location(world3, x3, y2, z3);
                                if (tokens.length == 6) {
                                    loc2.setPitch(Float.parseFloat(tokens[4]));
                                    loc2.setYaw(Float.parseFloat(tokens[5]));
                                }
                                town.forceAddOutpostSpawn(loc2);
                            }
                            catch (NumberFormatException ex6) {}
                            catch (NullPointerException ex7) {}
                            catch (NotRegisteredException ex8) {}
                        }
                    }
                }
                line = rs.getString("jailSpawns");
                if (line != null) {
                    final String[] split3;
                    split3 = line.split(";");
                    for (final String spawn : split3) {
                        final String search = line.contains("#") ? "#" : ",";
                        final String[] tokens = spawn.split(search);
                        if (tokens.length >= 4) {
                            try {
                                final World world3 = this.plugin.getServerWorld(tokens[0]);
                                final double x3 = Double.parseDouble(tokens[1]);
                                final double y2 = Double.parseDouble(tokens[2]);
                                final double z3 = Double.parseDouble(tokens[3]);
                                final Location loc2 = new Location(world3, x3, y2, z3);
                                if (tokens.length == 6) {
                                    loc2.setPitch(Float.parseFloat(tokens[4]));
                                    loc2.setYaw(Float.parseFloat(tokens[5]));
                                }
                                town.forceAddJailSpawn(loc2);
                            }
                            catch (NumberFormatException ex9) {}
                            catch (NullPointerException ex10) {}
                            catch (NotRegisteredException ex11) {}
                        }
                    }
                }
                line = rs.getString("outlaws");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] split4;
                    split4 = line.split(search);
                    for (final String token : split4) {
                        if (!token.isEmpty()) {
                            final Resident resident = this.getResident(token);
                            if (resident != null) {
                                town.addOutlaw(resident);
                            }
                        }
                    }
                }
                try {
                    town.setUuid(UUID.fromString(rs.getString("uuid")));
                }
                catch (IllegalArgumentException | NullPointerException e) {
                    town.setUuid(UUID.randomUUID());
                }
                line = rs.getString("conqueredDays");
                if (line != null) {
                    town.setConqueredDays(Integer.valueOf(line));
                }
                else {
                    town.setConqueredDays(0);
                }
                try {
                    line = rs.getString("townBlocks");
                    if (line != null) {
                        this.utilLoadTownBlocks(line, town, null);
                    }
                }
                catch (SQLException ex13) {}
                try {
                    line = rs.getString("registered");
                    if (line != null) {
                        town.setRegistered(Long.valueOf(line));
                    }
                    else {
                        town.setRegistered(0L);
                    }
                }
                catch (SQLException e) {}
                catch (NumberFormatException | NullPointerException e) {
                    town.setRegistered(0L);
                }
                try {
                    line = rs.getString("metadata");
                    if (line != null && !line.isEmpty()) {
                        town.setMetadata(line);
                    }
                }
                catch (SQLException e) {}
                town.setRecentlyRuinedEndTime(rs.getLong("recentlyRuinedEndTime"));
                town.setRevoltImmunityEndTime(rs.getLong("revoltCooldownEndTime"));
                town.setSiegeImmunityEndTime(rs.getLong("siegeCooldownEndTime"));
                line = rs.getString("siegeStatus");
                if (line != null && !line.isEmpty()) {
                    final Siege siege = new Siege(town);
                    town.setSiege(siege);
                    siege.setStatus(SiegeStatus.parseString(line));
                }
                else {
                    town.setSiege(null);
                }
                if (town.hasSiege()) {
                    final Siege siege = town.getSiege();
                    siege.setTownPlundered(rs.getBoolean("siegeTownPlundered"));
                    siege.setTownInvaded(rs.getBoolean("siegeTownInvaded"));
                    line = rs.getString("siegeAttackerWinner");
                    if (line != null && !line.isEmpty()) {
                        siege.setAttackerWinner(this.getNation(rs.getString("siegeAttackerWinner")));
                    }
                    else {
                        siege.setAttackerWinner(null);
                    }
                    siege.setStartTime(rs.getLong("siegeActualStartTime"));
                    siege.setScheduledEndTime(rs.getLong("siegeScheduledEndTime"));
                    siege.setActualEndTime(rs.getLong("siegeActualEndTime"));
                    line = rs.getString("siegeZones");
                    if (line != null && !line.isEmpty()) {
                        final String[] split5;
                        split5 = line.split(",");
                        for (final String siegeZoneName : split5) {
                            final SiegeZone siegeZone = this.universe.getDataSource().getSiegeZone(siegeZoneName);
                            town.getSiege().getSiegeZones().put(siegeZone.getAttackingNation(), siegeZone);
                        }
                    }
                }
                town.setOccupied(rs.getBoolean("occupied"));
                town.setNeutral(rs.getBoolean("neutral"));
                town.setDesiredNeutralityValue(rs.getBoolean("desiredNeutralityValue"));
                town.setNeutralityChangeConfirmationCounterDays(rs.getInt("neutralityChangeConfirmationCounterDays"));
                s.close();
                return true;
            }
            s.close();
            return false;
        }
        catch (SQLException e2) {
            TownyMessaging.sendErrorMsg("SQL: Load Town sql Error - " + e2.getMessage());
        }
        catch (Exception e3) {
            TownyMessaging.sendErrorMsg("SQL: Load Town unknown Error - ");
            e3.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean loadNation(final Nation nation) {
        TownyMessaging.sendDebugMsg("Loading nation " + nation.getName());
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "NATIONS WHERE name='" + nation.getName() + "'");
            while (rs.next()) {
                String line = rs.getString("towns");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] split;
                    split = line.split(search);
                    for (final String token : split) {
                        if (!token.isEmpty()) {
                            final Town town = this.getTown(token);
                            if (town != null) {
                                nation.addTown(town);
                            }
                        }
                    }
                }
                nation.setCapital(this.getTown(rs.getString("capital")));
                line = rs.getString("nationBoard");
                if (line != null) {
                    nation.setNationBoard(rs.getString("nationBoard"));
                }
                else {
                    nation.setNationBoard("");
                }
                nation.setTag(rs.getString("tag"));
                line = rs.getString("allies");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] split2;
                    split2 = line.split(search);
                    for (final String token : split2) {
                        if (!token.isEmpty()) {
                            final Nation friend = this.getNation(token);
                            if (friend != null) {
                                nation.addAlly(friend);
                            }
                        }
                    }
                }
                line = rs.getString("enemies");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] split3;
                    split3 = line.split(search);
                    for (final String token : split3) {
                        if (!token.isEmpty()) {
                            final Nation enemy = this.getNation(token);
                            if (enemy != null) {
                                nation.addEnemy(enemy);
                            }
                        }
                    }
                }
                line = rs.getString("siegeZones");
                if (line != null) {
                    final String[] split4;
                    split4 = line.split(",");
                    for (final String token : split4) {
                        if (!token.isEmpty()) {
                            final SiegeZone siegeZone = this.getSiegeZone(token);
                            if (siegeZone != null) {
                                nation.addSiegeZone(siegeZone);
                            }
                        }
                    }
                }
                nation.setTaxes(rs.getDouble("taxes"));
                nation.setSpawnCost(rs.getFloat("spawnCost"));
                nation.setNeutral(rs.getBoolean("neutral"));
                try {
                    nation.setUuid(UUID.fromString(rs.getString("uuid")));
                }
                catch (IllegalArgumentException | NullPointerException e) {
                    nation.setUuid(UUID.randomUUID());
                }
                line = rs.getString("nationSpawn");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] tokens = line.split(search);
                    if (tokens.length >= 4) {
                        try {
                            final World world = this.plugin.getServerWorld(tokens[0]);
                            final double x = Double.parseDouble(tokens[1]);
                            final double y = Double.parseDouble(tokens[2]);
                            final double z = Double.parseDouble(tokens[3]);
                            final Location loc = new Location(world, x, y, z);
                            if (tokens.length == 6) {
                                loc.setPitch(Float.parseFloat(tokens[4]));
                                loc.setYaw(Float.parseFloat(tokens[5]));
                            }
                            nation.forceSetNationSpawn(loc);
                        }
                        catch (NumberFormatException ex4) {}
                        catch (NullPointerException ex5) {}
                        catch (NotRegisteredException ex6) {}
                    }
                }
                nation.setPublic(rs.getBoolean("isPublic"));
                nation.setOpen(rs.getBoolean("isOpen"));
            }
            try {
                final String line = rs.getString("registered");
                if (line != null) {
                    nation.setRegistered(Long.valueOf(line));
                }
                else {
                    nation.setRegistered(0L);
                }
            }
            catch (SQLException e) {}
            catch (NumberFormatException | NullPointerException e) {
                nation.setRegistered(0L);
            }
            try {
                final String line = rs.getString("metadata");
                if (line != null && !line.isEmpty()) {
                    nation.setMetadata(line);
                }
            }
            catch (SQLException e) {}
            s.close();
            return true;
        }
        catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: Load Nation sql error " + e.getMessage());
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Load Nation unknown error - ");
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean loadSiegeZone(final SiegeZone siegeZone) {
        Statement s = null;
        ResultSet rs = null;
        TownyMessaging.sendDebugMsg("Loading siege zone " + siegeZone.getName());
        if (!this.getContext()) {
            return false;
        }
        try {
            s = this.cntx.createStatement();
            rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "SIEGEZONES WHERE siegeZoneName='" + siegeZone.getName() + "'");
            while (rs.next()) {
                final String line = rs.getString("flagLocation");
                final String[] listEntries = line.split(",");
                final World flagLocationWorld = BukkitTools.getWorld(listEntries[0]);
                final double flagLocationX = Double.parseDouble(listEntries[1]);
                final double flagLocationY = Double.parseDouble(listEntries[2]);
                final double flagLocationZ = Double.parseDouble(listEntries[3]);
                final Location flagLocation = new Location(flagLocationWorld, flagLocationX, flagLocationY, flagLocationZ);
                siegeZone.setFlagLocation(flagLocation);
                siegeZone.setAttackingNation(this.getNation(rs.getString("attackingNation")));
                siegeZone.setDefendingTown(this.getTown(rs.getString("defendingTown")));
                siegeZone.setSiegePoints(rs.getInt("siegePoints"));
                siegeZone.setWarChestAmount(rs.getDouble("warChestAmount"));
            }
            return true;
        }
        catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: Load Nation sql error " + e.getMessage());
        }
        catch (Exception e2) {
            TownyMessaging.sendErrorMsg("SQL: Load Nation unknown error - ");
            e2.printStackTrace();
        }
        finally {
            try {
                rs.close();
            }
            catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                s.close();
            }
            catch (Exception e3) {
                e3.printStackTrace();
            }
        }
        return false;
    }
    
    @Override
    public boolean loadWorld(final TownyWorld world) {
        TownyMessaging.sendDebugMsg("Loading world " + world.getName());
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "WORLDS WHERE name='" + world.getName() + "'");
            while (rs.next()) {
                String line = rs.getString("towns");
                if (line != null) {
                    final String search = line.contains("#") ? "#" : ",";
                    final String[] split2;
                    split2 = line.split(search);
                    for (final String token : split2) {
                        if (!token.isEmpty()) {
                            final Town town = this.getTown(token);
                            if (town != null) {
                                town.setWorld(world);
                            }
                        }
                    }
                }
                boolean result = rs.getBoolean("claimable");
                try {
                    world.setClaimable(result);
                }
                catch (Exception ex) {}
                result = rs.getBoolean("pvp");
                try {
                    world.setPVP(result);
                }
                catch (Exception ex2) {}
                result = rs.getBoolean("forcepvp");
                try {
                    world.setForcePVP(result);
                }
                catch (Exception ex3) {}
                result = rs.getBoolean("forcetownmobs");
                try {
                    world.setForceTownMobs(result);
                }
                catch (Exception ex4) {}
                result = rs.getBoolean("worldmobs");
                try {
                    world.setWorldMobs(result);
                }
                catch (Exception ex5) {}
                result = rs.getBoolean("firespread");
                try {
                    world.setFire(result);
                }
                catch (Exception ex6) {}
                result = rs.getBoolean("forcefirespread");
                try {
                    world.setForceFire(result);
                }
                catch (Exception ex7) {}
                result = rs.getBoolean("explosions");
                try {
                    world.setExpl(result);
                }
                catch (Exception ex8) {}
                result = rs.getBoolean("forceexplosions");
                try {
                    world.setForceExpl(result);
                }
                catch (Exception ex9) {}
                result = rs.getBoolean("endermanprotect");
                try {
                    world.setEndermanProtect(result);
                }
                catch (Exception ex10) {}
                result = rs.getBoolean("disableplayertrample");
                try {
                    world.setDisablePlayerTrample(result);
                }
                catch (Exception ex11) {}
                result = rs.getBoolean("disablecreaturetrample");
                try {
                    world.setDisableCreatureTrample(result);
                }
                catch (Exception ex12) {}
                result = rs.getBoolean("unclaimedZoneBuild");
                try {
                    world.setUnclaimedZoneBuild(result);
                }
                catch (Exception ex13) {}
                result = rs.getBoolean("unclaimedZoneDestroy");
                try {
                    world.setUnclaimedZoneDestroy(result);
                }
                catch (Exception ex14) {}
                result = rs.getBoolean("unclaimedZoneSwitch");
                try {
                    world.setUnclaimedZoneSwitch(result);
                }
                catch (Exception ex15) {}
                result = rs.getBoolean("unclaimedZoneItemUse");
                try {
                    world.setUnclaimedZoneItemUse(result);
                }
                catch (Exception ex16) {}
                line = rs.getString("unclaimedZoneName");
                try {
                    world.setUnclaimedZoneName(line);
                }
                catch (Exception ex17) {}
                line = rs.getString("unclaimedZoneIgnoreIds");
                if (line != null) {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        final String search = line.contains("#") ? "#" : ",";
                        for (final String split : line.split(search)) {
                            if (!split.isEmpty()) {
                                mats.add(split);
                            }
                        }
                        world.setUnclaimedZoneIgnore(mats);
                    }
                    catch (Exception ex18) {}
                }
                result = rs.getBoolean("usingPlotManagementDelete");
                try {
                    world.setUsingPlotManagementDelete(result);
                }
                catch (Exception ex19) {}
                line = rs.getString("plotManagementDeleteIds");
                if (line != null) {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        final String search = line.contains("#") ? "#" : ",";
                        for (final String split : line.split(search)) {
                            if (!split.isEmpty()) {
                                mats.add(split);
                            }
                        }
                        world.setPlotManagementDeleteIds(mats);
                    }
                    catch (Exception ex20) {}
                }
                result = rs.getBoolean("usingPlotManagementMayorDelete");
                try {
                    world.setUsingPlotManagementMayorDelete(result);
                }
                catch (Exception ex21) {}
                line = rs.getString("plotManagementMayorDelete");
                if (line != null) {
                    try {
                        final List<String> materials = new ArrayList<String>();
                        final String search = line.contains("#") ? "#" : ",";
                        for (final String split : line.split(search)) {
                            if (!split.isEmpty()) {
                                try {
                                    materials.add(split.toUpperCase().trim());
                                }
                                catch (NumberFormatException ex22) {}
                            }
                        }
                        world.setPlotManagementMayorDelete(materials);
                    }
                    catch (Exception ex23) {}
                }
                result = rs.getBoolean("usingPlotManagementRevert");
                try {
                    world.setUsingPlotManagementRevert(result);
                }
                catch (Exception ex24) {}
                long resultLong = rs.getLong("PlotManagementRevertSpeed");
                try {
                    world.setPlotManagementRevertSpeed(resultLong);
                }
                catch (Exception ex25) {}
                line = rs.getString("plotManagementIgnoreIds");
                if (line != null) {
                    try {
                        final List<String> mats = new ArrayList<String>();
                        final String search = line.contains("#") ? "#" : ",";
                        for (final String split : line.split(search)) {
                            if (!split.isEmpty()) {
                                mats.add(split);
                            }
                        }
                        world.setPlotManagementIgnoreIds(mats);
                    }
                    catch (Exception ex26) {}
                }
                result = rs.getBoolean("usingPlotManagementWildRegen");
                try {
                    world.setUsingPlotManagementWildRevert(result);
                }
                catch (Exception ex27) {}
                line = rs.getString("plotManagementWildRegenEntities");
                if (line != null) {
                    try {
                        final List<String> entities = new ArrayList<String>();
                        final String search = line.contains("#") ? "#" : ",";
                        for (final String split : line.split(search)) {
                            if (!split.isEmpty()) {
                                try {
                                    entities.add(split.trim());
                                }
                                catch (NumberFormatException ex28) {}
                            }
                        }
                        world.setPlotManagementWildRevertEntities(entities);
                    }
                    catch (Exception ex29) {}
                }
                resultLong = rs.getLong("plotManagementWildRegenSpeed");
                try {
                    world.setPlotManagementWildRevertDelay(resultLong);
                }
                catch (Exception ex30) {}
                result = rs.getBoolean("usingTowny");
                try {
                    world.setUsingTowny(result);
                }
                catch (Exception ex31) {}
                result = rs.getBoolean("warAllowed");
                try {
                    world.setWarAllowed(result);
                }
                catch (Exception ex32) {}
                try {
                    line = rs.getString("metadata");
                    if (line == null || line.isEmpty()) {
                        continue;
                    }
                    world.setMetadata(line);
                }
                catch (SQLException ex33) {}
            }
            s.close();
            return true;
        }
        catch (SQLException e) {
            TownyMessaging.sendErrorMsg("SQL: Load world sql error (" + world.getName() + ")" + e.getMessage());
        }
        catch (Exception e2) {
            TownyMessaging.sendErrorMsg("SQL: Load world unknown error - ");
            e2.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean loadTownBlocks() {
        String line = "";
        TownyMessaging.sendDebugMsg("Loading Town Blocks.");
        if (!this.getContext()) {
            return false;
        }
        for (final TownBlock townBlock : this.getAllTownBlocks()) {
            try {
                final Statement s = this.cntx.createStatement();
                final ResultSet rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "TOWNBLOCKS WHERE world='" + townBlock.getWorld().getName() + "' AND x='" + townBlock.getX() + "' AND z='" + townBlock.getZ() + "'");
                while (rs.next()) {
                    line = rs.getString("name");
                    if (line != null) {
                        try {
                            townBlock.setName(line.trim());
                        }
                        catch (Exception ex) {}
                    }
                    line = rs.getString("price");
                    if (line != null) {
                        try {
                            townBlock.setPlotPrice(Float.parseFloat(line.trim()));
                        }
                        catch (Exception ex2) {}
                    }
                    line = rs.getString("town");
                    if (line != null) {
                        try {
                            final Town town = this.getTown(line.trim());
                            townBlock.setTown(town);
                        }
                        catch (Exception ex3) {}
                    }
                    line = rs.getString("resident");
                    if (line != null && !line.isEmpty()) {
                        try {
                            final Resident res = this.getResident(line.trim());
                            townBlock.setResident(res);
                        }
                        catch (Exception ex4) {}
                    }
                    line = rs.getString("type");
                    if (line != null) {
                        try {
                            townBlock.setType(Integer.parseInt(line));
                        }
                        catch (Exception ex5) {}
                    }
                    final boolean outpost = rs.getBoolean("outpost");
                    if (line != null && !line.isEmpty()) {
                        try {
                            townBlock.setOutpost(outpost);
                        }
                        catch (Exception ex6) {}
                    }
                    line = rs.getString("permissions");
                    if (line != null && !line.isEmpty()) {
                        try {
                            townBlock.setPermissions(line.trim().replaceAll("#", ","));
                        }
                        catch (Exception ex7) {}
                    }
                    boolean result = rs.getBoolean("changed");
                    try {
                        townBlock.setChanged(result);
                    }
                    catch (Exception ex8) {}
                    result = rs.getBoolean("locked");
                    try {
                        townBlock.setLocked(result);
                    }
                    catch (Exception ex9) {}
                    try {
                        line = rs.getString("metadata");
                        if (line != null && !line.isEmpty()) {
                            townBlock.setMetadata(line);
                        }
                    }
                    catch (SQLException ex10) {}
                    try {
                        line = rs.getString("groupID");
                        if (line == null || line.isEmpty()) {
                            continue;
                        }
                        try {
                            final UUID groupID = UUID.fromString(line.trim());
                            final PlotObjectGroup group = this.getPlotObjectGroup(townBlock.getTown().toString(), groupID);
                            townBlock.setPlotObjectGroup(group);
                        }
                        catch (Exception ex11) {}
                    }
                    catch (SQLException ex12) {}
                }
                s.close();
            }
            catch (SQLException e) {
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading TownBlock: " + townBlock + " at line: " + line + " in the sql database");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    @Override
    public synchronized boolean saveResident(final Resident resident) {
        TownyMessaging.sendDebugMsg("Saving Resident");
        try {
            final HashMap<String, Object> res_hm = new HashMap<String, Object>();
            res_hm.put("name", resident.getName());
            res_hm.put("lastOnline", resident.getLastOnline());
            res_hm.put("registered", resident.getRegistered());
            res_hm.put("isNPC", resident.isNPC());
            res_hm.put("isJailed", resident.isJailed());
            res_hm.put("JailSpawn", resident.getJailSpawn());
            res_hm.put("JailDays", resident.getJailDays());
            res_hm.put("JailTown", resident.getJailTown());
            res_hm.put("title", resident.getTitle());
            res_hm.put("surname", resident.getSurname());
            res_hm.put("town", resident.hasTown() ? resident.getTown().getName() : "");
            res_hm.put("town-ranks", resident.hasTown() ? StringMgmt.join(resident.getTownRanks(), "#") : "");
            res_hm.put("nation-ranks", resident.hasTown() ? StringMgmt.join(resident.getNationRanks(), "#") : "");
            res_hm.put("friends", StringMgmt.join(resident.getFriends(), "#"));
            res_hm.put("protectionStatus", resident.getPermissions().toString().replaceAll(",", "#"));
            if (resident.hasMeta()) {
                res_hm.put("metadata", StringMgmt.join(new ArrayList<Object>(resident.getMetadata()), ";"));
            }
            else {
                res_hm.put("metadata", "");
            }
            this.UpdateDB("RESIDENTS", res_hm, Collections.singletonList("name"));
            return true;
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save Resident unknown error " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public synchronized boolean saveTown(final Town town) {
        TownyMessaging.sendDebugMsg("Saving town " + town.getName());
        try {
            final HashMap<String, Object> twn_hm = new HashMap<String, Object>();
            twn_hm.put("name", town.getName());
            twn_hm.put("residents", StringMgmt.join(town.getResidents(), "#"));
            twn_hm.put("outlaws", StringMgmt.join(town.getOutlaws(), "#"));
            twn_hm.put("mayor", town.hasMayor() ? town.getMayor().getName() : "");
            twn_hm.put("nation", town.hasNation() ? town.getNation().getName() : "");
            twn_hm.put("assistants", StringMgmt.join(town.getAssistants(), "#"));
            twn_hm.put("townBoard", town.getTownBoard());
            twn_hm.put("tag", town.getTag());
            twn_hm.put("protectionStatus", town.getPermissions().toString().replaceAll(",", "#"));
            twn_hm.put("bonus", town.getBonusBlocks());
            twn_hm.put("purchased", town.getPurchasedBlocks());
            twn_hm.put("commercialPlotPrice", town.getCommercialPlotPrice());
            twn_hm.put("commercialPlotTax", town.getCommercialPlotTax());
            twn_hm.put("embassyPlotPrice", town.getEmbassyPlotPrice());
            twn_hm.put("embassyPlotTax", town.getEmbassyPlotTax());
            twn_hm.put("spawnCost", town.getSpawnCost());
            twn_hm.put("plotPrice", town.getPlotPrice());
            twn_hm.put("plotTax", town.getPlotTax());
            twn_hm.put("taxes", town.getTaxes());
            twn_hm.put("hasUpkeep", town.hasUpkeep());
            twn_hm.put("taxpercent", town.isTaxPercentage());
            twn_hm.put("open", town.isOpen());
            twn_hm.put("public", town.isPublic());
            twn_hm.put("conquered", town.isConquered());
            twn_hm.put("conqueredDays", town.getConqueredDays());
            twn_hm.put("admindisabledpvp", town.isAdminDisabledPVP());
            twn_hm.put("adminenabledpvp", town.isAdminEnabledPVP());
            if (town.hasMeta()) {
                twn_hm.put("metadata", StringMgmt.join(new ArrayList<Object>(town.getMetadata()), ";"));
            }
            else {
                twn_hm.put("metadata", "");
            }
            twn_hm.put("homeblock", town.hasHomeBlock() ? (town.getHomeBlock().getWorld().getName() + "#" + town.getHomeBlock().getX() + "#" + town.getHomeBlock().getZ()) : "");
            twn_hm.put("spawn", town.hasSpawn() ? (town.getSpawn().getWorld().getName() + "#" + town.getSpawn().getX() + "#" + town.getSpawn().getY() + "#" + town.getSpawn().getZ() + "#" + town.getSpawn().getPitch() + "#" + town.getSpawn().getYaw()) : "");
            final StringBuilder outpostArray = new StringBuilder();
            if (town.hasOutpostSpawn()) {
                for (final Location spawn : new ArrayList<Location>(town.getAllOutpostSpawns())) {
                    outpostArray.append(spawn.getWorld().getName()).append("#").append(spawn.getX()).append("#").append(spawn.getY()).append("#").append(spawn.getZ()).append("#").append(spawn.getPitch()).append("#").append(spawn.getYaw()).append(";");
                }
            }
            twn_hm.put("outpostSpawns", outpostArray.toString());
            final StringBuilder jailArray = new StringBuilder();
            if (town.hasJailSpawn()) {
                for (final Location spawn2 : new ArrayList<Location>(town.getAllJailSpawns())) {
                    jailArray.append(spawn2.getWorld().getName()).append("#").append(spawn2.getX()).append("#").append(spawn2.getY()).append("#").append(spawn2.getZ()).append("#").append(spawn2.getPitch()).append("#").append(spawn2.getYaw()).append(";");
                }
            }
            twn_hm.put("jailSpawns", jailArray.toString());
            if (town.hasValidUUID()) {
                twn_hm.put("uuid", town.getUuid());
            }
            else {
                twn_hm.put("uuid", UUID.randomUUID());
            }
            twn_hm.put("registered", town.getRegistered());
            twn_hm.put("recentlyRuinedEndTime", Long.toString(town.getRecentlyRuinedEndTime()));
            twn_hm.put("revoltCooldownEndTime", Long.toString(town.getRevoltImmunityEndTime()));
            twn_hm.put("siegeCooldownEndTime", Long.toString(town.getSiegeImmunityEndTime()));
            if (town.hasSiege()) {
                final Siege siege = town.getSiege();
                twn_hm.put("siegeStatus", siege.getStatus().toString());
                twn_hm.put("siegeTownPlundered", siege.isTownPlundered());
                twn_hm.put("siegeTownInvaded", siege.isTownInvaded());
                if (siege.hasAttackerWinner()) {
                    twn_hm.put("siegeAttackerWinner", siege.getAttackerWinner().getName());
                }
                else {
                    twn_hm.put("siegeAttackerWinner", "");
                }
                twn_hm.put("siegeActualStartTime", siege.getStartTime());
                twn_hm.put("siegeScheduledEndTime", siege.getScheduledEndTime());
                twn_hm.put("siegeActualEndTime", siege.getActualEndTime());
                twn_hm.put("siegeZones", StringMgmt.join(siege.getSiegeZoneNames(), ","));
            }
            else {
                twn_hm.put("siegeStatus", "");
                twn_hm.put("siegeTownPlundered", false);
                twn_hm.put("siegeTownInvaded", false);
                twn_hm.put("siegeAttackerWinner", "");
                twn_hm.put("siegeActualStartTime", 0);
                twn_hm.put("siegeScheduledEndTime", 0);
                twn_hm.put("siegeActualEndTime", 0);
                twn_hm.put("siegeZones", "");
            }
            twn_hm.put("occupied", town.isOccupied());
            twn_hm.put("neutral=", town.isNeutral());
            twn_hm.put("desiredNeutralityValue", town.getDesiredNeutralityValue());
            twn_hm.put("neutralityChangeConfirmationCounterDays", town.getNeutralityChangeConfirmationCounterDays());
            this.UpdateDB("TOWNS", twn_hm, Collections.singletonList("name"));
            return true;
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save Town unknown error");
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public synchronized boolean savePlotGroup(final PlotObjectGroup group) {
        TownyMessaging.sendDebugMsg("Saving group " + group.getGroupName());
        try {
            final HashMap<String, Object> nat_hm = new HashMap<String, Object>();
            nat_hm.put("groupName", group.getGroupName());
            nat_hm.put("groupID", group.getID());
            nat_hm.put("groupPrice", group.getPrice());
            nat_hm.put("town", group.getTown().toString());
            this.UpdateDB("PLOTGROUPS", nat_hm, Collections.singletonList("name"));
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save Plot groups unknown error");
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public synchronized boolean saveNation(final Nation nation) {
        TownyMessaging.sendDebugMsg("Saving nation " + nation.getName());
        try {
            final HashMap<String, Object> nat_hm = new HashMap<String, Object>();
            nat_hm.put("name", nation.getName());
            nat_hm.put("towns", StringMgmt.join(nation.getTowns(), "#"));
            nat_hm.put("capital", nation.hasCapital() ? nation.getCapital().getName() : "");
            nat_hm.put("nationBoard", nation.getNationBoard());
            nat_hm.put("tag", nation.hasTag() ? nation.getTag() : "");
            nat_hm.put("assistants", StringMgmt.join(nation.getAssistants(), "#"));
            nat_hm.put("allies", StringMgmt.join(nation.getAllies(), "#"));
            nat_hm.put("enemies", StringMgmt.join(nation.getEnemies(), "#"));
            nat_hm.put("siegeZones", StringMgmt.join(nation.getSiegeZoneNames(), ","));
            nat_hm.put("taxes", nation.getTaxes());
            nat_hm.put("spawnCost", nation.getSpawnCost());
            nat_hm.put("neutral", nation.isNeutral());
            nat_hm.put("nationSpawn", nation.hasNationSpawn() ? (nation.getNationSpawn().getWorld().getName() + "#" + nation.getNationSpawn().getX() + "#" + nation.getNationSpawn().getY() + "#" + nation.getNationSpawn().getZ() + "#" + nation.getNationSpawn().getPitch() + "#" + nation.getNationSpawn().getYaw()) : "");
            if (nation.hasValidUUID()) {
                nat_hm.put("uuid", nation.getUuid());
            }
            else {
                nat_hm.put("uuid", UUID.randomUUID());
            }
            nat_hm.put("registered", nation.getRegistered());
            nat_hm.put("isPublic", nation.isPublic());
            nat_hm.put("isOpen", nation.isOpen());
            if (nation.hasMeta()) {
                nat_hm.put("metadata", StringMgmt.join(new ArrayList<Object>(nation.getMetadata()), ";"));
            }
            else {
                nat_hm.put("metadata", "");
            }
            this.UpdateDB("NATIONS", nat_hm, Collections.singletonList("name"));
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save Nation unknown error");
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public synchronized boolean saveSiegeZone(final SiegeZone siegeZone) {
        TownyMessaging.sendDebugMsg("Saving siege zone " + siegeZone.getName());
        try {
            final HashMap<String, Object> sg_hm = new HashMap<String, Object>();
            sg_hm.put("siegeZoneName", siegeZone.getName());
            sg_hm.put("flagLocation", siegeZone.getFlagLocation().getWorld().getName() + "," + siegeZone.getFlagLocation().getX() + "," + siegeZone.getFlagLocation().getY() + "," + siegeZone.getFlagLocation().getZ());
            sg_hm.put("attackingNation", siegeZone.getAttackingNation().getName());
            sg_hm.put("defendingTown", siegeZone.getDefendingTown().getName());
            sg_hm.put("siegePoints", siegeZone.getSiegePoints());
            sg_hm.put("warChestAmount", siegeZone.getWarChestAmount());
            this.UpdateDB("SIEGEZONES", sg_hm, Collections.singletonList("siegeZoneName"));
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save Siegezone unknown error");
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public synchronized boolean saveWorld(final TownyWorld world) {
        TownyMessaging.sendDebugMsg("Saving world " + world.getName());
        try {
            final HashMap<String, Object> nat_hm = new HashMap<String, Object>();
            nat_hm.put("name", world.getName());
            nat_hm.put("towns", StringMgmt.join(world.getTowns(), "#"));
            nat_hm.put("pvp", world.isPVP());
            nat_hm.put("forcepvp", world.isForcePVP());
            nat_hm.put("claimable", world.isClaimable());
            nat_hm.put("worldmobs", world.hasWorldMobs());
            nat_hm.put("forcetownmobs", world.isForceTownMobs());
            nat_hm.put("firespread", world.isFire());
            nat_hm.put("forcefirespread", world.isForceFire());
            nat_hm.put("explosions", world.isExpl());
            nat_hm.put("forceexplosions", world.isForceExpl());
            nat_hm.put("endermanprotect", world.isEndermanProtect());
            nat_hm.put("disableplayertrample", world.isDisablePlayerTrample());
            nat_hm.put("disablecreaturetrample", world.isDisableCreatureTrample());
            nat_hm.put("unclaimedZoneBuild", world.getUnclaimedZoneBuild());
            nat_hm.put("unclaimedZoneDestroy", world.getUnclaimedZoneDestroy());
            nat_hm.put("unclaimedZoneSwitch", world.getUnclaimedZoneSwitch());
            nat_hm.put("unclaimedZoneItemUse", world.getUnclaimedZoneItemUse());
            if (world.getUnclaimedZoneName() != null) {
                nat_hm.put("unclaimedZoneName", world.getUnclaimedZoneName());
            }
            if (world.getUnclaimedZoneIgnoreMaterials() != null) {
                nat_hm.put("unclaimedZoneIgnoreIds", StringMgmt.join(world.getUnclaimedZoneIgnoreMaterials(), "#"));
            }
            nat_hm.put("usingPlotManagementDelete", world.isUsingPlotManagementDelete());
            if (world.getPlotManagementDeleteIds() != null) {
                nat_hm.put("plotManagementDeleteIds", StringMgmt.join(world.getPlotManagementDeleteIds(), "#"));
            }
            nat_hm.put("usingPlotManagementMayorDelete", world.isUsingPlotManagementMayorDelete());
            if (world.getPlotManagementMayorDelete() != null) {
                nat_hm.put("plotManagementMayorDelete", StringMgmt.join(world.getPlotManagementMayorDelete(), "#"));
            }
            nat_hm.put("usingPlotManagementRevert", world.isUsingPlotManagementRevert());
            nat_hm.put("plotManagementRevertSpeed", world.getPlotManagementRevertSpeed());
            if (world.getPlotManagementIgnoreIds() != null) {
                nat_hm.put("plotManagementIgnoreIds", StringMgmt.join(world.getPlotManagementIgnoreIds(), "#"));
            }
            nat_hm.put("usingPlotManagementWildRegen", world.isUsingPlotManagementWildRevert());
            if (world.getPlotManagementWildRevertEntities() != null) {
                nat_hm.put("PlotManagementWildRegenEntities", StringMgmt.join(world.getPlotManagementWildRevertEntities(), "#"));
            }
            nat_hm.put("plotManagementWildRegenSpeed", world.getPlotManagementWildRevertDelay());
            nat_hm.put("usingTowny", world.isUsingTowny());
            nat_hm.put("warAllowed", world.isWarAllowed());
            if (world.hasMeta()) {
                nat_hm.put("metadata", StringMgmt.join(new ArrayList<Object>(world.getMetadata()), ";"));
            }
            else {
                nat_hm.put("metadata", "");
            }
            this.UpdateDB("WORLDS", nat_hm, Collections.singletonList("name"));
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save world unknown error (" + world.getName() + ")");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Override
    public boolean saveAllTownBlocks() {
        return false;
    }
    
    @Override
    public synchronized boolean saveTownBlock(final TownBlock townBlock) {
        TownyMessaging.sendDebugMsg("Saving town block " + townBlock.getWorld().getName() + ":" + townBlock.getX() + "x" + townBlock.getZ());
        try {
            final HashMap<String, Object> tb_hm = new HashMap<String, Object>();
            tb_hm.put("world", townBlock.getWorld().getName());
            tb_hm.put("x", townBlock.getX());
            tb_hm.put("z", townBlock.getZ());
            tb_hm.put("name", townBlock.getName());
            tb_hm.put("price", townBlock.getPlotPrice());
            tb_hm.put("town", townBlock.getTown().getName());
            tb_hm.put("resident", townBlock.hasResident() ? townBlock.getResident().getName() : "");
            tb_hm.put("type", townBlock.getType().getId());
            tb_hm.put("outpost", townBlock.isOutpost());
            tb_hm.put("permissions", townBlock.isChanged() ? townBlock.getPermissions().toString().replaceAll(",", "#") : "");
            tb_hm.put("locked", townBlock.isLocked());
            tb_hm.put("changed", townBlock.isChanged());
            if (townBlock.hasPlotObjectGroup()) {
                tb_hm.put("groupID", townBlock.getPlotObjectGroup().getID().toString());
            }
            else {
                tb_hm.put("groupID", "");
            }
            if (townBlock.hasMeta()) {
                tb_hm.put("metadata", StringMgmt.join(new ArrayList<Object>(townBlock.getMetadata()), ";"));
            }
            else {
                tb_hm.put("metadata", "");
            }
            this.UpdateDB("TOWNBLOCKS", tb_hm, Arrays.asList("world", "x", "z"));
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("SQL: Save TownBlock unknown error");
            e.printStackTrace();
        }
        return true;
    }
    
    @Override
    public boolean savePlotData(final PlotBlockData plotChunk) {
        FileMgmt.checkOrCreateFolder(this.dataFolderPath + File.separator + "plot-block-data" + File.separator + plotChunk.getWorldName());
        final String path = this.getPlotFilename(plotChunk);
        try {
            final DataOutputStream fout = new DataOutputStream(new FileOutputStream(path));
            try {
                switch (plotChunk.getVersion()) {
                    case 1:
                    case 2:
                    case 3:
                    case 4: {
                        fout.write("VER".getBytes(StandardCharsets.UTF_8));
                        fout.write(plotChunk.getVersion());
                        break;
                    }
                }
                fout.writeInt(plotChunk.getHeight());
                for (final String block : new ArrayList<String>(plotChunk.getBlockList())) {
                    fout.writeUTF(block);
                }
                fout.close();
            }
            catch (Throwable t) {
                try {
                    fout.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Saving Error: Exception while saving PlotBlockData file (" + path + ")");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Override
    public PlotBlockData loadPlotData(final String worldName, final int x, final int z) {
        try {
            final TownyWorld world = this.getWorld(worldName);
            final TownBlock townBlock = new TownBlock(x, z, world);
            return this.loadPlotData(townBlock);
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public PlotBlockData loadPlotData(final TownBlock townBlock) {
        final String fileName = this.getPlotFilename(townBlock);
        if (this.isFile(fileName)) {
            final PlotBlockData plotBlockData = new PlotBlockData(townBlock);
            final List<String> blockArr = new ArrayList<String>();
            int version = 0;
            try {
                final DataInputStream fin = new DataInputStream(new FileInputStream(fileName));
                try {
                    fin.mark(3);
                    final byte[] key = new byte[3];
                    fin.read(key, 0, 3);
                    final String test = new String(key);
                    if (TownyFlatFileSource.elements.fromString(test) == TownyFlatFileSource.elements.VER) {
                        version = fin.read();
                        plotBlockData.setVersion(version);
                        plotBlockData.setHeight(fin.readInt());
                    }
                    else {
                        plotBlockData.setVersion(version);
                        fin.reset();
                        plotBlockData.setHeight(fin.readInt());
                        blockArr.add(fin.readUTF());
                        blockArr.add(fin.readUTF());
                    }
                    switch (version) {
                        default: {
                            String value;
                            while ((value = fin.readUTF()) != null) {
                                blockArr.add(value);
                            }
                            break;
                        }
                        case 2: {
                            int temp = 0;
                            while ((temp = fin.readInt()) >= 0) {
                                blockArr.add(temp + "");
                            }
                            break;
                        }
                    }
                    fin.close();
                }
                catch (Throwable t) {
                    try {
                        fin.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                    throw t;
                }
            }
            catch (EOFException ex) {}
            catch (IOException e) {
                e.printStackTrace();
            }
            plotBlockData.setBlockList(blockArr);
            plotBlockData.resetBlockListRestored();
            return plotBlockData;
        }
        return null;
    }
    
    @Override
    public void deletePlotData(final PlotBlockData plotChunk) {
        final File file = new File(this.getPlotFilename(plotChunk));
        if (file.exists()) {
            file.delete();
        }
    }
    
    private boolean isFile(final String fileName) {
        final File file = new File(fileName);
        return file.exists() && file.isFile();
    }
    
    @Override
    public void deleteFile(final String fileName) {
        final File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Override
    public void deleteResident(final Resident resident) {
        final HashMap<String, Object> res_hm = new HashMap<String, Object>();
        res_hm.put("name", resident.getName());
        this.DeleteDB("RESIDENTS", res_hm);
    }
    
    @Override
    public void deleteTown(final Town town) {
        final HashMap<String, Object> twn_hm = new HashMap<String, Object>();
        twn_hm.put("name", town.getName());
        this.DeleteDB("TOWNS", twn_hm);
    }
    
    @Override
    public void deleteNation(final Nation nation) {
        final HashMap<String, Object> nat_hm = new HashMap<String, Object>();
        nat_hm.put("name", nation.getName());
        this.DeleteDB("NATIONS", nat_hm);
    }
    
    @Override
    public void deleteWorld(final TownyWorld world) {
    }
    
    @Override
    public void deleteSiegeZone(final SiegeZone siegeZone) {
        final HashMap<String, Object> siegeZones_hm = new HashMap<String, Object>();
        siegeZones_hm.put("siegeZoneName", siegeZone.getName());
        this.DeleteDB("SIEGEZONES", siegeZones_hm);
    }
    
    @Override
    public void deleteTownBlock(final TownBlock townBlock) {
        final HashMap<String, Object> twn_hm = new HashMap<String, Object>();
        twn_hm.put("world", townBlock.getWorld().getName());
        twn_hm.put("x", townBlock.getX());
        twn_hm.put("z", townBlock.getZ());
        this.DeleteDB("TOWNBLOCKS", twn_hm);
    }
    
    @Override
    public void deletePlotGroup(final PlotObjectGroup group) {
    }
    
    @Override
    public synchronized boolean backup() throws IOException {
        TownyMessaging.sendMsg("Performing backup");
        TownyMessaging.sendMsg("***** Warning *****");
        TownyMessaging.sendMsg("***** Only Snapshots and Regen files will be backed up");
        TownyMessaging.sendMsg("***** Make sure you schedule a backup in MySQL too!!!");
        final String backupType = TownySettings.getFlatFileBackupType();
        final long t = System.currentTimeMillis();
        final String newBackupFolder = this.backupFolderPath + File.separator + new SimpleDateFormat("yyyy-MM-dd HH-mm").format(t) + " - " + t;
        FileMgmt.checkOrCreateFolders(this.rootFolderPath, this.rootFolderPath + File.separator + "backup");
        final String lowerCase = backupType.toLowerCase();
        switch (lowerCase) {
            case "folder": {
                FileMgmt.checkOrCreateFolder(newBackupFolder);
                FileMgmt.copyDirectory(new File(this.dataFolderPath), new File(newBackupFolder));
                FileMgmt.copyDirectory(new File(this.logFolderPath), new File(newBackupFolder));
                FileMgmt.copyDirectory(new File(this.settingsFolderPath), new File(newBackupFolder));
                return true;
            }
            case "zip": {
                FileMgmt.zipDirectories(new File(newBackupFolder + ".zip"), new File(this.dataFolderPath), new File(this.logFolderPath), new File(this.settingsFolderPath));
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public void cleanupBackups() {
    }
    
    @Override
    public void deleteUnusedResidents() {
    }
    
    @Override
    public boolean loadPlotGroupList() {
        TownyMessaging.sendDebugMsg("Loading PlotGroup List");
        if (!this.getContext()) {
            return false;
        }
        try {
            final Statement s = this.cntx.createStatement();
            final ResultSet rs = s.executeQuery("SELECT groupID,town,groupName FROM " + this.tb_prefix + "PLOTGROUPS");
            while (rs.next()) {
                final UUID id = UUID.fromString(rs.getString("groupID"));
                final String groupName = rs.getString("groupName");
                Town town = null;
                try {
                    town = this.getTown(rs.getString("town"));
                }
                catch (NotRegisteredException e2) {
                    continue;
                }
                try {
                    TownyUniverse.getInstance().newGroup(town, groupName, id);
                }
                catch (AlreadyRegisteredException ex) {}
            }
            s.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean loadPlotGroups() {
        String line = "";
        TownyMessaging.sendDebugMsg("Loading plot groups.");
        if (!this.getContext()) {
            return false;
        }
        for (final PlotObjectGroup plotGroup : this.getAllPlotGroups()) {
            try {
                final Statement s = this.cntx.createStatement();
                final ResultSet rs = s.executeQuery("SELECT * FROM " + this.tb_prefix + "PLOTGROUPS WHERE groupID='" + plotGroup.getID().toString() + "'");
                while (rs.next()) {
                    line = rs.getString("groupName");
                    if (line != null) {
                        try {
                            plotGroup.setGroupName(line.trim());
                        }
                        catch (Exception ex) {}
                    }
                    line = rs.getString("groupID");
                    if (line != null) {
                        try {
                            plotGroup.setID(UUID.fromString(line.trim()));
                        }
                        catch (Exception ex2) {}
                    }
                    line = rs.getString("town");
                    if (line != null) {
                        try {
                            plotGroup.setTown(this.getTown(line.trim()));
                        }
                        catch (Exception ex3) {}
                    }
                    line = rs.getString("groupPrice");
                    if (line != null) {
                        try {
                            plotGroup.setPrice(Float.parseFloat(line.trim()));
                        }
                        catch (Exception ex4) {}
                    }
                }
                s.close();
            }
            catch (SQLException e) {
                TownyMessaging.sendErrorMsg("Loading Error: Exception while reading plot group: " + plotGroup.getGroupName() + " at line: " + line + " in the sql database");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public boolean cleanup() {
        if (!this.getContext()) {
            return false;
        }
        SQL_Schema.cleanup(this.cntx, this.db_name);
        return true;
    }
    
    @Override
    public boolean saveTownBlockList() {
        return true;
    }
    
    @Override
    public boolean saveResidentList() {
        return true;
    }
    
    @Override
    public boolean saveTownList() {
        return true;
    }
    
    @Override
    public boolean savePlotGroupList() {
        return true;
    }
    
    @Override
    public boolean saveNationList() {
        return true;
    }
    
    @Override
    public boolean saveSiegeZoneList() {
        return true;
    }
    
    @Override
    public boolean saveWorldList() {
        return true;
    }
    
    @Override
    public boolean saveRegenList() {
        try {
            final BufferedWriter fout = new BufferedWriter(new FileWriter(this.dataFolderPath + File.separator + "regen.txt"));
            try {
                for (final PlotBlockData plot : new ArrayList<PlotBlockData>(TownyRegenAPI.getPlotChunks().values())) {
                    fout.write(plot.getWorldName() + "," + plot.getX() + "," + plot.getZ() + System.getProperty("line.separator"));
                }
                fout.close();
            }
            catch (Throwable t) {
                try {
                    fout.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Saving Error: Exception while saving regen file");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Override
    public boolean saveSnapshotList() {
        try {
            final BufferedWriter fout = new BufferedWriter(new FileWriter(this.dataFolderPath + File.separator + "snapshot_queue.txt"));
            try {
                while (TownyRegenAPI.hasWorldCoords()) {
                    final WorldCoord worldCoord = TownyRegenAPI.getWorldCoord();
                    fout.write(worldCoord.getWorldName() + "," + worldCoord.getX() + "," + worldCoord.getZ() + System.getProperty("line.separator"));
                }
                fout.close();
            }
            catch (Throwable t) {
                try {
                    fout.close();
                }
                catch (Throwable exception) {
                    t.addSuppressed(exception);
                }
                throw t;
            }
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg("Saving Error: Exception while saving snapshot_queue file");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static void validateTownOutposts(final Town town) {
        final List<Location> validoutpostspawns = new ArrayList<Location>();
        if (town != null && town.hasOutpostSpawn()) {
            for (final Location outpostSpawn : town.getAllOutpostSpawns()) {
                final TownBlock outpostSpawnTB = TownyAPI.getInstance().getTownBlock(outpostSpawn);
                if (outpostSpawnTB != null) {
                    validoutpostspawns.add(outpostSpawn);
                }
            }
            town.setOutpostSpawns(validoutpostspawns);
        }
    }
    
    public String getPlotFilename(final PlotBlockData plotChunk) {
        return this.dataFolderPath + File.separator + "plot-block-data" + File.separator + plotChunk.getWorldName() + File.separator + plotChunk.getX() + "_" + plotChunk.getZ() + "_" + plotChunk.getSize() + ".data";
    }
    
    public String getPlotFilename(final TownBlock townBlock) {
        return this.dataFolderPath + File.separator + "plot-block-data" + File.separator + townBlock.getWorld().getName() + File.separator + townBlock.getX() + "_" + townBlock.getZ() + "_" + TownySettings.getTownBlockSize() + ".data";
    }
    
    @Deprecated
    public void utilLoadTownBlocks(final String line, final Town town, final Resident resident) {
        final String[] split2;
        split2 = line.split("\\|");
        for (final String w : split2) {
            final String[] split = w.split(":");
            if (split.length != 2) {
                TownyMessaging.sendErrorMsg("[Warning] " + town.getName() + " BlockList does not have a World or data.");
            }
            else {
                try {
                    final TownyWorld world = this.getWorld(split[0]);
                    for (String s : split[1].split(";")) {
                        String blockTypeData = null;
                        final int indexOfType = s.indexOf("[");
                        if (indexOfType != -1) {
                            final int endIndexOfType = s.indexOf("]");
                            if (endIndexOfType != -1) {
                                blockTypeData = s.substring(indexOfType + 1, endIndexOfType);
                            }
                            s = s.substring(endIndexOfType + 1);
                        }
                        final String[] tokens = s.split(",");
                        if (tokens.length >= 2) {
                            try {
                                final int x = Integer.parseInt(tokens[0]);
                                final int z = Integer.parseInt(tokens[1]);
                                try {
                                    world.newTownBlock(x, z);
                                }
                                catch (AlreadyRegisteredException ex) {}
                                final TownBlock townblock = world.getTownBlock(x, z);
                                if (town != null) {
                                    townblock.setTown(town);
                                }
                                if (resident != null && townblock.hasTown()) {
                                    townblock.setResident(resident);
                                }
                                if (blockTypeData != null) {
                                    this.utilLoadTownBlockTypeData(townblock, blockTypeData);
                                }
                                if (tokens.length >= 3) {
                                    if (tokens[2].equals("true")) {
                                        townblock.setPlotPrice(town.getPlotPrice());
                                    }
                                    else {
                                        townblock.setPlotPrice(Double.parseDouble(tokens[2]));
                                    }
                                }
                            }
                            catch (NumberFormatException ex2) {}
                            catch (NotRegisteredException ex3) {}
                        }
                    }
                }
                catch (NotRegisteredException ex4) {}
            }
        }
    }
    
    @Deprecated
    public void utilLoadTownBlockTypeData(final TownBlock townBlock, final String data) {
        final String[] tokens = data.split(",");
        if (tokens.length >= 1) {
            townBlock.setType(Integer.valueOf(tokens[0]));
        }
        if (tokens.length >= 2) {
            townBlock.setOutpost(tokens[1].equalsIgnoreCase("1"));
        }
    }
    
    @Deprecated
    public String utilSaveTownBlocks(final List<TownBlock> townBlocks) {
        final HashMap<TownyWorld, ArrayList<TownBlock>> worlds = new HashMap<TownyWorld, ArrayList<TownBlock>>();
        final StringBuilder out = new StringBuilder();
        for (final TownBlock townBlock : townBlocks) {
            final TownyWorld world = townBlock.getWorld();
            if (!worlds.containsKey(world)) {
                worlds.put(world, new ArrayList<TownBlock>());
            }
            worlds.get(world).add(townBlock);
        }
        for (final TownyWorld world2 : worlds.keySet()) {
            out.append(world2.getName()).append(":");
            for (final TownBlock townBlock2 : worlds.get(world2)) {
                out.append("[").append(townBlock2.getType().getId());
                out.append(",").append(townBlock2.isOutpost() ? "1" : "0");
                out.append("]").append(townBlock2.getX()).append(",").append(townBlock2.getZ()).append(",").append(townBlock2.getPlotPrice()).append(";");
            }
            out.append("|");
        }
        return out.toString();
    }
}
