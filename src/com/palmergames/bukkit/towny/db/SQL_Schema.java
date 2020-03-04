// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.db;

import com.palmergames.bukkit.towny.TownySettings;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.sql.Statement;
import java.sql.SQLException;
import com.palmergames.bukkit.towny.TownyMessaging;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class SQL_Schema
{
    private static final String tb_prefix;
    
    private static String getWORLDS() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "WORLDS (`name` VARCHAR(32) NOT NULL,PRIMARY KEY (`name`))";
    }
    
    private static List<String> getWorldColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`towns` mediumtext NOT NULL");
        columns.add("`claimable` bool NOT NULL DEFAULT '0'");
        columns.add("`pvp` bool NOT NULL DEFAULT '0'");
        columns.add("`forcepvp` bool NOT NULL DEFAULT '0'");
        columns.add("`forcetownmobs` bool NOT NULL DEFAULT '0'");
        columns.add("`worldmobs` bool NOT NULL DEFAULT '0'");
        columns.add("`firespread` bool NOT NULL DEFAULT '0'");
        columns.add("`forcefirespread` bool NOT NULL DEFAULT '0'");
        columns.add("`explosions` bool NOT NULL DEFAULT '0'");
        columns.add("`forceexplosions` bool NOT NULL DEFAULT '0'");
        columns.add("`endermanprotect` bool NOT NULL DEFAULT '0'");
        columns.add("`disableplayertrample` bool NOT NULL DEFAULT '0'");
        columns.add("`disablecreaturetrample` bool NOT NULL DEFAULT '0'");
        columns.add("`unclaimedZoneBuild` bool NOT NULL DEFAULT '0'");
        columns.add("`unclaimedZoneDestroy` bool NOT NULL DEFAULT '0'");
        columns.add("`unclaimedZoneSwitch` bool NOT NULL DEFAULT '0'");
        columns.add("`unclaimedZoneItemUse` bool NOT NULL DEFAULT '0'");
        columns.add("`unclaimedZoneName` mediumtext NOT NULL");
        columns.add("`unclaimedZoneIgnoreIds` mediumtext NOT NULL");
        columns.add("`usingPlotManagementDelete` bool NOT NULL DEFAULT '0'");
        columns.add("`plotManagementDeleteIds` mediumtext NOT NULL");
        columns.add("`usingPlotManagementMayorDelete` bool NOT NULL DEFAULT '0'");
        columns.add("`plotManagementMayorDelete` mediumtext NOT NULL");
        columns.add("`usingPlotManagementRevert` bool NOT NULL DEFAULT '0'");
        columns.add("`plotManagementRevertSpeed` long NOT NULL");
        columns.add("`plotManagementIgnoreIds` mediumtext NOT NULL");
        columns.add("`usingPlotManagementWildRegen` bool NOT NULL DEFAULT '0'");
        columns.add("`plotManagementWildRegenEntities` mediumtext NOT NULL");
        columns.add("`plotManagementWildRegenSpeed` long NOT NULL");
        columns.add("`usingTowny` bool NOT NULL DEFAULT '0'");
        columns.add("`warAllowed` bool NOT NULL DEFAULT '0'");
        columns.add("`metadata` text DEFAULT NULL");
        return columns;
    }
    
    private static String getNATIONS() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "NATIONS (`name` VARCHAR(32) NOT NULL,PRIMARY KEY (`name`))";
    }
    
    private static String getPLOTGROUPS() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "PLOTGROUPS (`groupID` VARCHAR(36) NOT NULL,PRIMARY KEY (`groupID`))";
    }
    
    private static List<String> getPlotGroupColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`groupName` mediumtext NOT NULL");
        columns.add("`groupPrice` float DEFAULT NULL");
        columns.add("`town` VARCHAR(32) NOT NULL");
        return columns;
    }
    
    private static List<String> getNationColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`towns` mediumtext NOT NULL");
        columns.add("`capital` mediumtext NOT NULL");
        columns.add("`assistants` mediumtext NOT NULL");
        columns.add("`tag` mediumtext NOT NULL");
        columns.add("`allies` mediumtext NOT NULL");
        columns.add("`enemies` mediumtext NOT NULL");
        columns.add("`siegeZones` mediumtext NOT NULL");
        columns.add("`taxes` float NOT NULL");
        columns.add("`spawnCost` float NOT NULL");
        columns.add("`neutral` bool NOT NULL DEFAULT '0'");
        columns.add("`uuid` VARCHAR(36) DEFAULT NULL");
        columns.add("`registered` BIGINT DEFAULT NULL");
        columns.add("`nationBoard` mediumtext DEFAULT NULL");
        columns.add("`nationSpawn` mediumtext DEFAULT NULL");
        columns.add("`isPublic` bool NOT NULL DEFAULT '1'");
        columns.add("`isOpen` bool NOT NULL DEFAULT '1'");
        columns.add("`metadata` text DEFAULT NULL");
        return columns;
    }
    
    private static String getSiegeZones() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "SIEGEZONES (`siegeZoneName` VARCHAR(32) NOT NULL,PRIMARY KEY (`siegeZoneName`))";
    }
    
    private static List<String> getSiegeZoneColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`flagLocation` mediumtext NOT NULL");
        columns.add("`attackingNation` mediumtext NOT NULL");
        columns.add("`defendingTown` mediumtext NOT NULL");
        columns.add("`siegePoints` mediumtext NOT NULL");
        columns.add("`warChestAmount` float NOT NULL");
        return columns;
    }
    
    private static String getTOWNS() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "TOWNS (`name` VARCHAR(32) NOT NULL,PRIMARY KEY (`name`))";
    }
    
    private static List<String> getTownColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`residents` mediumtext");
        columns.add("`mayor` mediumtext");
        columns.add("`nation` mediumtext NOT NULL");
        columns.add("`assistants` text DEFAULT NULL");
        columns.add("`townBoard` mediumtext DEFAULT NULL");
        columns.add("`tag` mediumtext DEFAULT NULL");
        columns.add("`protectionStatus` mediumtext DEFAULT NULL");
        columns.add("`bonus` int(11) DEFAULT 0");
        columns.add("`purchased` int(11)  DEFAULT 0");
        columns.add("`taxpercent` bool NOT NULL DEFAULT '0'");
        columns.add("`taxes` float DEFAULT 0");
        columns.add("`hasUpkeep` bool NOT NULL DEFAULT '0'");
        columns.add("`plotPrice` float DEFAULT NULL");
        columns.add("`plotTax` float DEFAULT NULL");
        columns.add("`commercialPlotPrice` float DEFAULT NULL");
        columns.add("`commercialPlotTax` float NOT NULL");
        columns.add("`embassyPlotPrice` float NOT NULL");
        columns.add("`embassyPlotTax` float NOT NULL");
        columns.add("`open` bool NOT NULL DEFAULT '0'");
        columns.add("`public` bool NOT NULL DEFAULT '0'");
        columns.add("`admindisabledpvp` bool NOT NULL DEFAULT '0'");
        columns.add("`adminenabledpvp` bool NOT NULL DEFAULT '0'");
        columns.add("`homeblock` mediumtext NOT NULL");
        columns.add("`spawn` mediumtext NOT NULL");
        columns.add("`outpostSpawns` mediumtext DEFAULT NULL");
        columns.add("`jailSpawns` mediumtext DEFAULT NULL");
        columns.add("`outlaws` mediumtext DEFAULT NULL");
        columns.add("`uuid` VARCHAR(36) DEFAULT NULL");
        columns.add("`registered` BIGINT DEFAULT NULL");
        columns.add("`spawnCost` float NOT NULL");
        columns.add("`metadata` text DEFAULT NULL");
        columns.add("`conqueredDays` mediumint");
        columns.add("`conquered` bool NOT NULL DEFAULT '0'");
        columns.add("`recentlyRuinedEndTime` BIGINT");
        columns.add("`revoltCooldownEndTime` BIGINT");
        columns.add("`siegeCooldownEndTime` BIGINT");
        columns.add("`siegeStatus` mediumtext");
        columns.add("`siegeTownPlundered` bool NOT NULL DEFAULT '0'");
        columns.add("`siegeTownInvaded` bool NOT NULL DEFAULT '0'");
        columns.add("`siegeAttackerWinner` mediumtext");
        columns.add("`siegeActualStartTime` BIGINT");
        columns.add("`siegeScheduledEndTime` BIGINT");
        columns.add("`siegeActualEndTime` BIGINT");
        columns.add("`siegeZones` mediumtext");
        columns.add("`occupied` bool NOT NULL DEFAULT '0'");
        columns.add("`neutral` bool NOT NULL DEFAULT '0'");
        columns.add("`desiredNeutralityValue` bool NOT NULL DEFAULT '0'");
        columns.add("`neutralityChangeConfirmationCounterDays` int(11) DEFAULT 0");
        return columns;
    }
    
    private static String getRESIDENTS() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "RESIDENTS ( `name` VARCHAR(16) NOT NULL,PRIMARY KEY (`name`))";
    }
    
    private static List<String> getResidentColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`town` mediumtext");
        columns.add("`town-ranks` mediumtext");
        columns.add("`nation-ranks` mediumtext");
        columns.add("`lastOnline` BIGINT NOT NULL");
        columns.add("`registered` BIGINT NOT NULL");
        columns.add("`isNPC` bool NOT NULL DEFAULT '0'");
        columns.add("`isJailed` bool NOT NULL DEFAULT '0'");
        columns.add("`JailSpawn` mediumint");
        columns.add("`JailDays` mediumint");
        columns.add("`JailTown` mediumtext");
        columns.add("`title` mediumtext");
        columns.add("`surname` mediumtext");
        columns.add("`protectionStatus` mediumtext");
        columns.add("`friends` mediumtext");
        columns.add("`metadata` text DEFAULT NULL");
        return columns;
    }
    
    private static String getTOWNBLOCKS() {
        return "CREATE TABLE IF NOT EXISTS " + SQL_Schema.tb_prefix + "TOWNBLOCKS (`world` VARCHAR(32) NOT NULL,`x` mediumint NOT NULL,`z` mediumint NOT NULL,PRIMARY KEY (`world`,`x`,`z`))";
    }
    
    private static List<String> getTownBlockColumns() {
        final List<String> columns = new ArrayList<String>();
        columns.add("`name` mediumtext");
        columns.add("`price` float DEFAULT '-1'");
        columns.add("`town` mediumtext");
        columns.add("`resident` mediumtext");
        columns.add("`type` TINYINT NOT  NULL DEFAULT '0'");
        columns.add("`outpost` bool NOT NULL DEFAULT '0'");
        columns.add("`permissions` mediumtext NOT NULL");
        columns.add("`locked` bool NOT NULL DEFAULT '0'");
        columns.add("`changed` bool NOT NULL DEFAULT '0'");
        columns.add("`metadata` text DEFAULT NULL");
        columns.add("`groupID` VARCHAR(36) DEFAULT NULL");
        return columns;
    }
    
    public static void initTables(final Connection cntx, final String db_name) {
        final String world_create = getWORLDS();
        try {
            final Statement s = cntx.createStatement();
            s.executeUpdate(world_create);
            TownyMessaging.sendDebugMsg("Table WORLDS is ok!");
        }
        catch (SQLException ee) {
            TownyMessaging.sendErrorMsg("Error Creating table WORLDS : " + ee.getMessage());
        }
        final List<String> worldColumns = getWorldColumns();
        for (final String column : worldColumns) {
            try {
                final String world_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "WORLDS` ADD COLUMN " + column;
                final PreparedStatement ps = cntx.prepareStatement(world_update);
                ps.executeUpdate();
            }
            catch (SQLException ee2) {
                if (ee2.getErrorCode() == 1060) {
                    continue;
                }
                TownyMessaging.sendErrorMsg("Error updating table WORLDS :" + ee2.getMessage());
            }
        }
        TownyMessaging.sendDebugMsg("Table WORLDS is updated!");
        TownyMessaging.sendDebugMsg("Checking done!");
        final String nation_create = getNATIONS();
        try {
            final Statement s2 = cntx.createStatement();
            s2.executeUpdate(nation_create);
            TownyMessaging.sendDebugMsg("Table NATIONS is ok!");
        }
        catch (SQLException ee3) {
            TownyMessaging.sendErrorMsg("Error Creating table NATIONS : " + ee3.getMessage());
        }
        final List<String> nationColumns = getNationColumns();
        for (final String column2 : nationColumns) {
            try {
                final String nation_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "NATIONS` ADD COLUMN " + column2;
                final PreparedStatement ps2 = cntx.prepareStatement(nation_update);
                ps2.executeUpdate();
            }
            catch (SQLException ee4) {
                if (ee4.getErrorCode() == 1060) {
                    continue;
                }
                TownyMessaging.sendErrorMsg("Error updating table NATIONS :" + ee4.getMessage());
            }
        }
        TownyMessaging.sendDebugMsg("Table NATIONS is updated!");
        final String siegeZones_create = getSiegeZones();
        try {
            final Statement s3 = cntx.createStatement();
            s3.executeUpdate(siegeZones_create);
            TownyMessaging.sendDebugMsg("Table SIEGEZONES is ok!");
        }
        catch (SQLException ee5) {
            TownyMessaging.sendErrorMsg("Error Creating table SIEGEZONES : " + ee5.getMessage());
        }
        final List<String> siegeZoneColumns = getSiegeZoneColumns();
        for (final String column3 : siegeZoneColumns) {
            try {
                final String siegeZone_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "SIEGEZONES` ADD COLUMN " + column3;
                final PreparedStatement ps3 = cntx.prepareStatement(siegeZone_update);
                ps3.executeUpdate();
            }
            catch (SQLException ee6) {
                if (ee6.getErrorCode() == 1060) {
                    continue;
                }
                TownyMessaging.sendErrorMsg("Error updating table SIEGEZONES :" + ee6.getMessage());
            }
        }
        TownyMessaging.sendDebugMsg("Table SIEGEZONES is updated!");
        final String town_create = getTOWNS();
        try {
            final Statement s4 = cntx.createStatement();
            s4.executeUpdate(town_create);
            TownyMessaging.sendDebugMsg("Table TOWNS is ok!");
        }
        catch (SQLException ee7) {
            TownyMessaging.sendErrorMsg("Creating table TOWNS :" + ee7.getMessage());
        }
        final List<String> townColumns = getTownColumns();
        for (final String column4 : townColumns) {
            try {
                final String town_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "TOWNS` ADD COLUMN " + column4;
                final PreparedStatement ps4 = cntx.prepareStatement(town_update);
                ps4.executeUpdate();
            }
            catch (SQLException ee8) {
                if (ee8.getErrorCode() == 1060) {
                    continue;
                }
                TownyMessaging.sendErrorMsg("Error updating table TOWNS :" + ee8.getMessage());
            }
        }
        TownyMessaging.sendDebugMsg("Table TOWNS is updated!");
        final String resident_create = getRESIDENTS();
        try {
            final Statement s5 = cntx.createStatement();
            s5.executeUpdate(resident_create);
            TownyMessaging.sendDebugMsg("Table RESIDENTS is ok!");
        }
        catch (SQLException ee9) {
            TownyMessaging.sendErrorMsg("Error Creating table RESIDENTS :" + ee9.getMessage());
        }
        final List<String> residentColumns = getResidentColumns();
        for (final String column5 : residentColumns) {
            try {
                final String resident_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "RESIDENTS` ADD COLUMN " + column5;
                final PreparedStatement ps5 = cntx.prepareStatement(resident_update);
                ps5.executeUpdate();
            }
            catch (SQLException ee10) {
                if (ee10.getErrorCode() == 1060) {
                    continue;
                }
                TownyMessaging.sendErrorMsg("Error updating table RESIDENTS :" + ee10.getMessage());
            }
        }
        TownyMessaging.sendDebugMsg("Table RESIDENTS is updated!");
        final String townblock_create = getTOWNBLOCKS();
        try {
            final Statement s6 = cntx.createStatement();
            s6.executeUpdate(townblock_create);
            TownyMessaging.sendDebugMsg("Table TOWNBLOCKS is ok!");
        }
        catch (SQLException ee11) {
            TownyMessaging.sendErrorMsg("Error Creating table TOWNBLOCKS : " + ee11.getMessage());
        }
        final List<String> townBlockColumns = getTownBlockColumns();
        for (final String column6 : townBlockColumns) {
            try {
                final String townblocks_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "TOWNBLOCKS` ADD COLUMN " + column6;
                final PreparedStatement ps6 = cntx.prepareStatement(townblocks_update);
                ps6.executeUpdate();
            }
            catch (SQLException ee12) {
                if (ee12.getErrorCode() == 1060) {
                    continue;
                }
                TownyMessaging.sendErrorMsg("Error updating table TOWNBLOCKS :" + ee12.getMessage());
            }
        }
        TownyMessaging.sendDebugMsg("Table TOWNBLOCKS is updated!");
        final String plotgroups_create = getPLOTGROUPS();
        try {
            final Statement s7 = cntx.createStatement();
            s7.executeUpdate(plotgroups_create);
            TownyMessaging.sendDebugMsg("Table PLOTGROUPS is ok!");
        }
        catch (SQLException ee13) {
            TownyMessaging.sendErrorMsg("Error Creating table PLOTGROUPS : " + ee13.getMessage());
        }
        final List<String> plotGroupColumns = getPlotGroupColumns();
        for (final String column7 : plotGroupColumns) {
            try {
                final String plotGroups_update = "ALTER TABLE `" + db_name + "`.`" + SQL_Schema.tb_prefix + "PLOTGROUPS` ADD COLUMN " + column7;
                final PreparedStatement ps7 = cntx.prepareStatement(plotGroups_update);
                ps7.executeUpdate();
            }
            catch (SQLException ee14) {
                if (ee14.getErrorCode() != 1060) {
                    TownyMessaging.sendErrorMsg("Error updating table PLOTGROUPS :" + ee14.getMessage());
                }
            }
            TownyMessaging.sendDebugMsg("Table PLOTGROUPS is updated!");
        }
    }
    
    @Deprecated
    public static void cleanup(final Connection cntx, final String db_name) {
    }
    
    static {
        tb_prefix = TownySettings.getSQLTablePrefix().toUpperCase();
    }
}
