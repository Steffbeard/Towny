// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.ArrayList;
import java.util.List;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class ChunkNotification
{
    public static String notificationFormat;
    public static String notificationSpliter;
    public static String areaWildernessNotificationFormat;
    public static String areaWildernessPvPNotificationFormat;
    public static String areaTownNotificationFormat;
    public static String areaTownPvPNotificationFormat;
    public static String ownerNotificationFormat;
    public static String noOwnerNotificationFormat;
    public static String plotNotficationSplitter;
    public static String plotNotificationFormat;
    public static String homeBlockNotification;
    public static String outpostBlockNotification;
    public static String forSaleNotificationFormat;
    public static String plotTypeNotificationFormat;
    public static String groupNotificationFormat;
    WorldCoord from;
    WorldCoord to;
    boolean fromWild;
    boolean toWild;
    boolean toForSale;
    boolean toHomeBlock;
    boolean toOutpostBlock;
    boolean toPlotGroupBlock;
    TownBlock fromTownBlock;
    TownBlock toTownBlock;
    Town fromTown;
    Town toTown;
    Resident fromResident;
    Resident toResident;
    TownBlockType fromPlotType;
    TownBlockType toPlotType;
    PlotObjectGroup fromPlotGroup;
    PlotObjectGroup toPlotGroup;
    
    public static void loadFormatStrings() {
        ChunkNotification.notificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_FORMAT);
        ChunkNotification.notificationSpliter = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_SPLITTER);
        ChunkNotification.areaWildernessNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_WILDERNESS);
        ChunkNotification.areaWildernessPvPNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_WILDERNESS_PVP);
        ChunkNotification.areaTownNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_TOWN);
        ChunkNotification.areaTownPvPNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_TOWN_PVP);
        ChunkNotification.ownerNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_OWNER);
        ChunkNotification.noOwnerNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_NO_OWNER);
        ChunkNotification.plotNotficationSplitter = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_SPLITTER);
        ChunkNotification.plotNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_FORMAT);
        ChunkNotification.homeBlockNotification = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_HOMEBLOCK);
        ChunkNotification.outpostBlockNotification = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_OUTPOSTBLOCK);
        ChunkNotification.forSaleNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_FORSALE);
        ChunkNotification.plotTypeNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_TYPE);
        ChunkNotification.groupNotificationFormat = TownySettings.getConfigLang(ConfigNodes.NOTIFICATION_GROUP);
    }
    
    public ChunkNotification(final WorldCoord from, final WorldCoord to) {
        this.fromWild = false;
        this.toWild = false;
        this.toForSale = false;
        this.toHomeBlock = false;
        this.toOutpostBlock = false;
        this.toPlotGroupBlock = false;
        this.toTownBlock = null;
        this.fromTown = null;
        this.toTown = null;
        this.fromResident = null;
        this.toResident = null;
        this.fromPlotType = null;
        this.toPlotType = null;
        this.fromPlotGroup = null;
        this.toPlotGroup = null;
        this.from = from;
        this.to = to;
        try {
            this.fromTownBlock = from.getTownBlock();
            this.fromPlotType = this.fromTownBlock.getType();
            try {
                this.fromTown = this.fromTownBlock.getTown();
            }
            catch (NotRegisteredException ex) {}
            try {
                this.fromResident = this.fromTownBlock.getResident();
            }
            catch (NotRegisteredException ex2) {}
        }
        catch (NotRegisteredException e) {
            this.fromWild = true;
        }
        try {
            this.toTownBlock = to.getTownBlock();
            this.toPlotType = this.toTownBlock.getType();
            try {
                this.toTown = this.toTownBlock.getTown();
            }
            catch (NotRegisteredException ex3) {}
            try {
                this.toResident = this.toTownBlock.getResident();
            }
            catch (NotRegisteredException ex4) {}
            this.toForSale = (this.toTownBlock.getPlotPrice() != -1.0);
            this.toHomeBlock = this.toTownBlock.isHomeBlock();
            this.toOutpostBlock = this.toTownBlock.isOutpost();
            this.toPlotGroupBlock = this.toTownBlock.hasPlotObjectGroup();
            if (this.toPlotGroupBlock) {
                this.toForSale = (this.toTownBlock.getPlotObjectGroup().getPrice() != -1.0);
            }
        }
        catch (NotRegisteredException e) {
            this.toWild = true;
        }
        try {
            if (this.toTownBlock.hasPlotObjectGroup()) {
                this.toPlotGroup = this.toTownBlock.getPlotObjectGroup();
            }
            if (this.fromTownBlock.hasPlotObjectGroup()) {
                this.fromPlotGroup = this.fromTownBlock.getPlotObjectGroup();
            }
        }
        catch (Exception ex5) {}
    }
    
    public String getNotificationString(final Resident resident) {
        if (ChunkNotification.notificationFormat.length() == 0) {
            return null;
        }
        final List<String> outputContent = this.getNotificationContent(resident);
        if (outputContent.size() == 0) {
            return null;
        }
        return String.format(ChunkNotification.notificationFormat, StringMgmt.join(outputContent, ChunkNotification.notificationSpliter));
    }
    
    public List<String> getNotificationContent(final Resident resident) {
        final List<String> out = new ArrayList<String>();
        String output = this.getAreaNotification(resident);
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        output = this.getAreaPvPNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        if (!resident.hasMode("ignoreplots")) {
            output = this.getOwnerNotification();
            if (output != null && output.length() > 0) {
                out.add(output);
            }
        }
        output = this.getTownPVPNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        if (!resident.hasMode("ignoreplots")) {
            output = this.getPlotNotification();
            if (output != null && output.length() > 0) {
                out.add(output);
            }
        }
        return out;
    }
    
    public String getAreaNotification(final Resident resident) {
        if ((this.fromWild ^ this.toWild) || (!this.fromWild && !this.toWild && this.fromTown != null && this.toTown != null && this.fromTown != this.toTown)) {
            if (this.toWild) {
                try {
                    if (TownySettings.getNationZonesEnabled() && TownySettings.getNationZonesShowNotifications()) {
                        final Player player = BukkitTools.getPlayer(resident.getName());
                        final TownyWorld toWorld = this.to.getTownyWorld();
                        try {
                            if (PlayerCacheUtil.getTownBlockStatus(player, this.to).equals(PlayerCache.TownBlockStatus.NATION_ZONE)) {
                                Town nearestTown = null;
                                nearestTown = toWorld.getClosestTownWithNationFromCoord(this.to.getCoord(), nearestTown);
                                return String.format(ChunkNotification.areaWildernessNotificationFormat, String.format(TownySettings.getLangString("nation_zone_this_area_under_protection_of"), toWorld.getUnclaimedZoneName(), nearestTown.getNation().getName()));
                            }
                        }
                        catch (NotRegisteredException ex) {}
                    }
                    return String.format(ChunkNotification.areaWildernessNotificationFormat, this.to.getTownyWorld().getUnclaimedZoneName());
                }
                catch (NotRegisteredException ex2) {
                    return null;
                }
            }
            if (TownySettings.isNotificationsTownNamesVerbose()) {
                return String.format(ChunkNotification.areaTownNotificationFormat, TownyFormatter.getFormattedName(this.toTown));
            }
            return String.format(ChunkNotification.areaTownNotificationFormat, this.toTown);
        }
        else if (this.fromWild && this.toWild) {
            try {
                if (TownySettings.getNationZonesEnabled() && TownySettings.getNationZonesShowNotifications()) {
                    final Player player = BukkitTools.getPlayer(resident.getName());
                    final TownyWorld toWorld = this.to.getTownyWorld();
                    try {
                        if (PlayerCacheUtil.getTownBlockStatus(player, this.to).equals(PlayerCache.TownBlockStatus.NATION_ZONE) && PlayerCacheUtil.getTownBlockStatus(player, this.from).equals(PlayerCache.TownBlockStatus.UNCLAIMED_ZONE)) {
                            Town nearestTown = null;
                            nearestTown = toWorld.getClosestTownWithNationFromCoord(this.to.getCoord(), nearestTown);
                            return String.format(ChunkNotification.areaWildernessNotificationFormat, String.format(TownySettings.getLangString("nation_zone_this_area_under_protection_of"), toWorld.getUnclaimedZoneName(), nearestTown.getNation().getName()));
                        }
                        if (PlayerCacheUtil.getTownBlockStatus(player, this.to).equals(PlayerCache.TownBlockStatus.UNCLAIMED_ZONE) && PlayerCacheUtil.getTownBlockStatus(player, this.from).equals(PlayerCache.TownBlockStatus.NATION_ZONE)) {
                            return String.format(ChunkNotification.areaWildernessNotificationFormat, this.to.getTownyWorld().getUnclaimedZoneName());
                        }
                    }
                    catch (NotRegisteredException ex3) {}
                }
            }
            catch (NotRegisteredException ex4) {}
        }
        return null;
    }
    
    public String getAreaPvPNotification() {
        if (((this.fromWild ^ this.toWild) || (!this.fromWild && !this.toWild && this.fromTown != null && this.toTown != null && this.fromTown != this.toTown)) && this.toWild) {
            try {
                return String.format(ChunkNotification.areaWildernessPvPNotificationFormat, (this.to.getTownyWorld().isPVP() && this.testWorldPVP()) ? "§4 (PvP)" : "");
            }
            catch (NotRegisteredException ex) {}
        }
        return null;
    }
    
    public String getOwnerNotification() {
        if ((this.fromResident == this.toResident && (this.fromTownBlock == null || this.toTownBlock == null || this.fromTownBlock.getName().equalsIgnoreCase(this.toTownBlock.getName()))) || this.toWild) {
            return null;
        }
        if (this.toResident == null) {
            return String.format(ChunkNotification.noOwnerNotificationFormat, this.toTownBlock.getName().isEmpty() ? TownySettings.getUnclaimedPlotName() : this.toTownBlock.getName());
        }
        if (TownySettings.isNotificationOwnerShowingNationTitles()) {
            return String.format(ChunkNotification.ownerNotificationFormat, this.toTownBlock.getName().isEmpty() ? TownyFormatter.getFormattedResidentTitleName(this.toResident) : this.toTownBlock.getName());
        }
        return String.format(ChunkNotification.ownerNotificationFormat, this.toTownBlock.getName().isEmpty() ? TownyFormatter.getFormattedName(this.toResident) : this.toTownBlock.getName());
    }
    
    public String getTownPVPNotification() {
        if (!this.toWild) {
            if (!this.fromWild) {
                if (this.toTownBlock.getPermissions().pvp == this.fromTownBlock.getPermissions().pvp || this.toTown.isPVP()) {
                    return null;
                }
            }
            try {
                return String.format(ChunkNotification.areaTownPvPNotificationFormat, (this.testWorldPVP() && !this.toTown.isAdminDisabledPVP() && (this.to.getTownyWorld().isForcePVP() || this.toTown.isPVP() || this.toTownBlock.getPermissions().pvp)) ? "§4(PvP)" : "§2(No PVP)");
            }
            catch (NotRegisteredException ex) {}
        }
        return null;
    }
    
    private boolean testWorldPVP() {
        try {
            return Bukkit.getServer().getWorld(this.to.getTownyWorld().getName()).getPVP();
        }
        catch (NotRegisteredException e) {
            return true;
        }
    }
    
    public String getPlotNotification() {
        if (ChunkNotification.plotNotificationFormat.length() == 0) {
            return null;
        }
        final List<String> outputContent = this.getPlotNotificationContent();
        if (outputContent.size() == 0) {
            return null;
        }
        return String.format(ChunkNotification.plotNotificationFormat, StringMgmt.join(outputContent, ChunkNotification.plotNotficationSplitter));
    }
    
    public List<String> getPlotNotificationContent() {
        final List<String> out = new ArrayList<String>();
        String output = this.getHomeblockNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        output = this.getOutpostblockNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        output = this.getForSaleNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        output = this.getPlotTypeNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        output = this.getGroupNotification();
        if (output != null && output.length() > 0) {
            out.add(output);
        }
        return out;
    }
    
    public String getHomeblockNotification() {
        if (this.toHomeBlock) {
            return ChunkNotification.homeBlockNotification;
        }
        return null;
    }
    
    public String getOutpostblockNotification() {
        if (this.toOutpostBlock) {
            return ChunkNotification.outpostBlockNotification;
        }
        return null;
    }
    
    public String getForSaleNotification() {
        if (this.toForSale && this.toPlotGroupBlock && this.fromPlotGroup != this.toPlotGroup) {
            return String.format(ChunkNotification.forSaleNotificationFormat, TownyEconomyHandler.getFormattedBalance(this.toTownBlock.getPlotObjectGroup().getPrice()));
        }
        if (this.toForSale && !this.toPlotGroupBlock) {
            return String.format(ChunkNotification.forSaleNotificationFormat, TownyEconomyHandler.getFormattedBalance(this.toTownBlock.getPlotPrice()));
        }
        return null;
    }
    
    public String getGroupNotification() {
        if (this.toPlotGroupBlock && this.fromPlotGroup != this.toPlotGroup) {
            return String.format(ChunkNotification.groupNotificationFormat, this.toTownBlock.getPlotObjectGroup().getGroupName());
        }
        return null;
    }
    
    public String getPlotTypeNotification() {
        if (this.fromPlotType != this.toPlotType && this.toPlotType != null && this.toPlotType != TownBlockType.RESIDENTIAL) {
            return String.format(ChunkNotification.plotTypeNotificationFormat, this.toPlotType.toString());
        }
        return null;
    }
    
    static {
        ChunkNotification.notificationFormat = "§6 ~ %s";
        ChunkNotification.notificationSpliter = "§7 - ";
        ChunkNotification.areaWildernessNotificationFormat = "§2%s";
        ChunkNotification.areaWildernessPvPNotificationFormat = "§2%s";
        ChunkNotification.areaTownNotificationFormat = "§2%s";
        ChunkNotification.areaTownPvPNotificationFormat = "§2%s";
        ChunkNotification.ownerNotificationFormat = "§a%s";
        ChunkNotification.noOwnerNotificationFormat = "§a%s";
        ChunkNotification.plotNotficationSplitter = " ";
        ChunkNotification.plotNotificationFormat = "%s";
        ChunkNotification.homeBlockNotification = "§b[Home]";
        ChunkNotification.outpostBlockNotification = "§b[Outpost]";
        ChunkNotification.forSaleNotificationFormat = "§e[For Sale: %s]";
        ChunkNotification.plotTypeNotificationFormat = "§6[%s]";
        ChunkNotification.groupNotificationFormat = "§f[%s]";
    }
}
