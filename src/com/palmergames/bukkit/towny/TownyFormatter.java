// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import java.math.RoundingMode;
import java.math.BigDecimal;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import java.util.Iterator;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import java.util.Calendar;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Collection;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.util.Colors;

import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.ResidentUtil;
import java.util.List;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.ResidentList;
import java.text.SimpleDateFormat;

public class TownyFormatter
{
    public static final SimpleDateFormat lastOnlineFormat;
    public static final SimpleDateFormat lastOnlineFormatIncludeYear;
    public static final SimpleDateFormat registeredFormat;
    public static final String residentListPrefixFormat = "%3$s%1$s %4$s[%2$d]%3$s:%5$s ";
    public static final String embassyTownListPrefixFormat = "%3$s%1$s:%5$s ";
    
    public static void initialize(final Towny plugin) {
    }
    
    public static List<String> getFormattedOnlineResidents(final String prefix, final ResidentList residentList, final Player player) {
        final List<Resident> onlineResidents = ResidentUtil.getOnlineResidentsViewable(player, residentList);
        return getFormattedResidents(prefix, onlineResidents);
    }
    
    public static List<String> getFormattedResidents(final Town town) {
        final List<String> out = new ArrayList<String>();
        final String[] residents = getFormattedNames(town.getResidents().toArray(new Resident[0]));
        out.addAll(ChatTools.listArr(residents, "§2" + TownySettings.getLangString("res_list") + " " + "§a" + "[" + town.getNumResidents() + "]" + "§2" + ":" + "§f" + " "));
        return out;
    }
    
    public static List<String> getFormattedOutlaws(final Town town) {
        final List<String> out = new ArrayList<String>();
        final String[] residents = getFormattedNames(town.getOutlaws().toArray(new Resident[0]));
        out.addAll(ChatTools.listArr(residents, TownySettings.getLangString("outlaws") + " "));
        return out;
    }
    
    public static List<String> getFormattedResidents(final String prefix, final List<Resident> residentList) {
        return ChatTools.listArr(getFormattedNames(residentList), String.format("%3$s%1$s %4$s[%2$d]%3$s:%5$s ", prefix, residentList.size(), TownySettings.getLangString("res_format_list_1"), TownySettings.getLangString("res_format_list_2"), TownySettings.getLangString("res_format_list_3")));
    }
    
    public static List<String> getFormattedTowns(final String prefix, final List<Town> townList) {
        final Town[] arrayTowns = townList.toArray(new Town[0]);
        return ChatTools.listArr(getFormattedNames(arrayTowns), String.format("%3$s%1$s:%5$s ", prefix, townList.size(), TownySettings.getLangString("res_format_list_1"), TownySettings.getLangString("res_format_list_2"), TownySettings.getLangString("res_format_list_3")));
    }
    
    public static String[] getFormattedNames(final List<Resident> residentList) {
        return getFormattedNames(residentList.toArray(new Resident[0]));
    }
    
    public static String getTime() {
        final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        return sdf.format(System.currentTimeMillis());
    }
    
    public static List<String> getStatus(final TownBlock townBlock) {
        List<String> out = new ArrayList<String>();
        try {
            final Town town = townBlock.getTown();
            final TownyWorld world = townBlock.getWorld();
            TownyObject owner;
            if (townBlock.hasResident()) {
                owner = townBlock.getResident();
            }
            else {
                owner = townBlock.getTown();
            }
            out.add(ChatTools.formatTitle(getFormattedName(owner) + (BukkitTools.isOnline(owner.getName()) ? TownySettings.getLangString("online") : "")));
            if (!townBlock.getType().equals(TownBlockType.RESIDENTIAL)) {
                out.add(TownySettings.getLangString("status_plot_type") + townBlock.getType().toString());
            }
            out.add(TownySettings.getLangString("status_perm") + ((owner instanceof Resident) ? townBlock.getPermissions().getColourString().replace("n", "t") : townBlock.getPermissions().getColourString().replace("f", "r")));
            out.add(TownySettings.getLangString("status_perm") + ((owner instanceof Resident) ? townBlock.getPermissions().getColourString2().replace("n", "t") : townBlock.getPermissions().getColourString2().replace("f", "r")));
            out.add(TownySettings.getLangString("status_pvp") + ((town.isPVP() || world.isForcePVP() || townBlock.getPermissions().pvp) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("explosions") + ((world.isForceExpl() || townBlock.getPermissions().explosion) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("firespread") + ((town.isFire() || world.isForceFire() || townBlock.getPermissions().fire) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("mobspawns") + ((town.hasMobs() || world.isForceTownMobs() || townBlock.getPermissions().mobs) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")));
            if (townBlock.hasPlotObjectGroup()) {
                out.add(String.format(TownySettings.getLangString("status_plot_group_name_and_size"), townBlock.getPlotObjectGroup().getGroupName(), townBlock.getPlotObjectGroup().getTownBlocks().size()));
            }
            out.addAll(getExtraFields(townBlock));
        }
        catch (NotRegisteredException e) {
            out.add("Error: " + e.getMessage());
        }
        out = formatStatusScreens(out);
        return out;
    }
    
    public static List<String> getStatus(final Resident resident, final Player player) {
        List<String> out = new ArrayList<String>();
        out.add(ChatTools.formatTitle(getFormattedName(resident) + ((BukkitTools.isOnline(resident.getName()) && player != null && player.canSee(BukkitTools.getPlayer(resident.getName()))) ? TownySettings.getLangString("online2") : "")));
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(resident.getLastOnline());
        final int currentYear = cal.get(1);
        cal.setTimeInMillis(System.currentTimeMillis());
        final int lastOnlineYear = cal.get(1);
        if (currentYear == lastOnlineYear) {
            out.add(String.format(TownySettings.getLangString("registered_last_online"), TownyFormatter.registeredFormat.format(resident.getRegistered()), TownyFormatter.lastOnlineFormat.format(resident.getLastOnline())));
        }
        else {
            out.add(String.format(TownySettings.getLangString("registered_last_online"), TownyFormatter.registeredFormat.format(resident.getRegistered()), TownyFormatter.lastOnlineFormatIncludeYear.format(resident.getLastOnline())));
        }
        out.add(String.format(TownySettings.getLangString("owner_of_x_plots"), resident.getTownBlocks().size()));
        out.add(TownySettings.getLangString("status_perm") + resident.getPermissions().getColourString().replace("n", "t"));
        out.add(TownySettings.getLangString("status_perm") + resident.getPermissions().getColourString2().replace("n", "t"));
        out.add(TownySettings.getLangString("status_pvp") + (resident.getPermissions().pvp ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("explosions") + (resident.getPermissions().explosion ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("firespread") + (resident.getPermissions().fire ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("mobspawns") + (resident.getPermissions().mobs ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")));
        if (TownySettings.isUsingEconomy() && TownyEconomyHandler.isActive()) {
            out.add(String.format(TownySettings.getLangString("status_bank"), resident.getAccount().getHoldingFormattedBalance()));
        }
        String line = TownySettings.getLangString("status_town");
        if (!resident.hasTown()) {
            line += TownySettings.getLangString("status_no_town");
        }
        else {
            try {
                line += getFormattedName(resident.getTown());
            }
            catch (TownyException e) {
                line = line + "Error: " + e.getMessage();
            }
        }
        out.add(line);
        final List<Town> townEmbassies = new ArrayList<Town>();
        try {
            final String actualTown = resident.hasTown() ? resident.getTown().getName() : "";
            for (final TownBlock tB : resident.getTownBlocks()) {
                if (!actualTown.equals(tB.getTown().getName()) && !townEmbassies.contains(tB.getTown())) {
                    townEmbassies.add(tB.getTown());
                }
            }
        }
        catch (NotRegisteredException ex) {}
        if (townEmbassies.size() > 0) {
            out.addAll(getFormattedTowns(TownySettings.getLangString("status_embassy_town"), townEmbassies));
        }
        if (resident.hasTown() && !resident.getTownRanks().isEmpty()) {
            out.add(TownySettings.getLangString("status_town_ranks") + StringMgmt.capitalize(StringMgmt.join(resident.getTownRanks(), ",")));
        }
        if (resident.hasNation() && !resident.getNationRanks().isEmpty()) {
            out.add(TownySettings.getLangString("status_nation_ranks") + StringMgmt.capitalize(StringMgmt.join(resident.getNationRanks(), ",")));
        }
        if (resident.isJailed()) {
            out.add(String.format(TownySettings.getLangString("jailed_in_town"), resident.getJailTown()) + (resident.hasJailDays() ? String.format(TownySettings.getLangString("msg_jailed_for_x_days"), resident.getJailDays()) : ""));
        }
        final List<Resident> friends = resident.getFriends();
        out.addAll(getFormattedResidents(TownySettings.getLangString("status_friends"), friends));
        out.addAll(getExtraFields(resident));
        out = formatStatusScreens(out);
        return out;
    }
    
    public static List<String> getRanks(final Town town) {
        final List<String> ranklist = new ArrayList<String>();
        String towntitle = getFormattedName(town);
        towntitle += TownySettings.getLangString("rank_list_title");
        ranklist.add(ChatTools.formatTitle(towntitle));
        ranklist.add(String.format(TownySettings.getLangString("rank_list_mayor"), getFormattedName(town.getMayor())));
        final List<Resident> residents = town.getResidents();
        final List<String> townranks = TownyPerms.getTownRanks();
        final List<Resident> residentwithrank = new ArrayList<Resident>();
        for (final String rank : townranks) {
            for (final Resident r : residents) {
                if (r.getTownRanks() != null && r.getTownRanks().contains(rank)) {
                    residentwithrank.add(r);
                }
            }
            ranklist.addAll(getFormattedResidents(StringMgmt.capitalize(rank), residentwithrank));
            residentwithrank.clear();
        }
        return ranklist;
    }
    
    public static List<String> formatStatusScreens(final List<String> out) {
        final List<String> formattedOut = new ArrayList<String>();
        for (final String line : out) {
            if (line.length() > 80) {
                int middle = line.length() / 2;
                final int before = line.lastIndexOf(32, middle);
                final int after = line.lastIndexOf(32, middle + 1);
                if (middle - before < after - middle) {
                    middle = before;
                }
                else {
                    middle = after;
                }
                final String first = line.substring(0, middle);
                final String second = line.substring(middle + 1);
                formattedOut.add(first);
                formattedOut.add(second);
            }
            else {
                formattedOut.add(line);
            }
        }
        return formattedOut;
    }
    
    public static List<String> getStatus(Town town) {
        List<String> out = new ArrayList<String>();
        TownyWorld world;
        try {
            world = town.getWorld();
        }
        catch (NullPointerException e) {
            world = TownyUniverse.getInstance().getDataSource().getWorlds().get(0);
        }
        String title = getFormattedName(town);
        title += ((!town.isAdminDisabledPVP() && (town.isPVP() || town.getWorld().isForcePVP())) ? TownySettings.getLangString("status_title_pvp") : "");
        title += (town.isOpen() ? TownySettings.getLangString("status_title_open") : "");
        title += (town.isNeutral() ? TownySettings.getLangString("status_town_title_neutral") : "");
        out.add(ChatTools.formatTitle(title));
        try {
            out.addAll(ChatTools.color(String.format(TownySettings.getLangString("status_town_board"), town.getTownBoard())));
        }
        catch (NullPointerException ex) {}
        final Long registered = town.getRegistered();
        if (registered != 0L) {
            out.add(String.format(TownySettings.getLangString("status_founded"), TownyFormatter.registeredFormat.format(town.getRegistered())));
        }
        try {
            out.add(String.format(TownySettings.getLangString("status_town_size_part_1"), town.getTownBlocks().size(), TownySettings.getMaxTownBlocks(town)) + (TownySettings.isSellingBonusBlocks(town) ? String.format(TownySettings.getLangString("status_town_size_part_2"), town.getPurchasedBlocks(), TownySettings.getMaxPurchedBlocks(town)) : "") + ((town.getBonusBlocks() > 0) ? String.format(TownySettings.getLangString("status_town_size_part_3"), town.getBonusBlocks()) : "") + ((TownySettings.getNationBonusBlocks(town) > 0) ? String.format(TownySettings.getLangString("status_town_size_part_4"), TownySettings.getNationBonusBlocks(town)) : "") + (town.isPublic() ? (TownySettings.getLangString("status_town_size_part_5") + (TownySettings.getTownDisplaysXYZ() ? ((town.hasSpawn() ? BukkitTools.convertCoordtoXYZ(town.getSpawn()) : TownySettings.getLangString("status_no_town")) + "]") : ((town.hasHomeBlock() ? town.getHomeBlock().getCoord().toString() : TownySettings.getLangString("status_no_town")) + "]"))) : ""));
        }
        catch (TownyException ex2) {}
        if (TownySettings.isAllowingOutposts()) {
            if (TownySettings.isOutpostsLimitedByLevels()) {
                if (town.hasOutpostSpawn()) {
                    if (!town.hasNation()) {
                        out.add(String.format(TownySettings.getLangString("status_town_outposts"), town.getMaxOutpostSpawn(), town.getOutpostLimit()));
                    }
                    else {
                        int nationBonus = 0;
                        try {
                        	nationBonus =  (Integer) TownySettings.getNationLevel(town.getNation()).get(TownySettings.NationLevel.NATION_BONUS_OUTPOST_LIMIT);
                        }
                        catch (NotRegisteredException ex3) {}
                        out.add(String.format(TownySettings.getLangString("status_town_outposts"), town.getMaxOutpostSpawn(), town.getOutpostLimit()) + ((nationBonus > 0) ? String.format(TownySettings.getLangString("status_town_outposts2"), nationBonus) : ""));
                    }
                }
                else {
                    out.add(String.format(TownySettings.getLangString("status_town_outposts3"), town.getOutpostLimit()));
                }
            }
            else if (town.hasOutpostSpawn()) {
                out.add(String.format(TownySettings.getLangString("status_town_outposts4"), town.getMaxOutpostSpawn()));
            }
        }
        out.add(TownySettings.getLangString("status_perm") + town.getPermissions().getColourString().replace("f", "r"));
        out.add(TownySettings.getLangString("status_perm") + town.getPermissions().getColourString2().replace("f", "r"));
        out.add(TownySettings.getLangString("explosions2") + ((town.isBANG() || world.isForceExpl()) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("firespread") + ((town.isFire() || world.isForceFire()) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + TownySettings.getLangString("mobspawns") + ((town.hasMobs() || world.isForceTownMobs()) ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")));
        if (!town.isRuined()) {
            String bankString = "";
            if (TownySettings.isUsingEconomy()) {
                if (TownyEconomyHandler.isActive()) {
                    bankString = String.format(TownySettings.getLangString("status_bank"), town.getAccount().getHoldingFormattedBalance());
                    if (town.hasUpkeep()) {
                        bankString += String.format(TownySettings.getLangString("status_bank_town2"), new BigDecimal(TownySettings.getTownUpkeepCost(town)).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    if (TownySettings.getUpkeepPenalty() > 0.0 && town.isOverClaimed()) {
                        bankString += String.format(TownySettings.getLangString("status_bank_town_penalty_upkeep"), TownySettings.getTownPenaltyUpkeepCost(town));
                    }
                    bankString = bankString + String.format(TownySettings.getLangString("status_bank_town3"), town.getTaxes()) + (town.isTaxPercentage() ? "%" : "");
                }
                out.add(bankString);
            }
        }
        if (town.hasMayor()) {
            out.add(String.format(TownySettings.getLangString("rank_list_mayor"), getFormattedName(town.getMayor())));
        }
        final List<String> ranklist = new ArrayList<String>();
        final List<Resident> residentss = town.getResidents();
        final List<String> townranks = TownyPerms.getTownRanks();
        final List<Resident> residentwithrank = new ArrayList<Resident>();
        for (final String rank : townranks) {
            for (final Resident r : residentss) {
                if (r.getTownRanks() != null && r.getTownRanks().contains(rank)) {
                    residentwithrank.add(r);
                }
            }
            ranklist.addAll(getFormattedResidents(StringMgmt.capitalize(rank), residentwithrank));
            residentwithrank.clear();
        }
        out.addAll(ranklist);
        try {
            out.add(String.format(TownySettings.getLangString("status_town_nation"), getFormattedName(town.getNation())) + (town.isConquered() ? (" " + TownySettings.getLangString("msg_conquered")) : "") + (town.isOccupied() ? (" " + TownySettings.getLangString("msg_occupier")) : ""));
        }
        catch (TownyException ex4) {}
        String[] residents = getFormattedNames(town.getResidents().toArray(new Resident[0]));
        if (residents.length > 34) {
            final String[] entire = residents;
            residents = new String[36];
            System.arraycopy(entire, 0, residents, 0, 35);
            residents[35] = TownySettings.getLangString("status_town_reslist_overlength");
        }
        out.addAll(ChatTools.listArr(residents, String.format(TownySettings.getLangString("status_town_reslist"), town.getNumResidents())));
        if (TownySettings.getWarSiegeEnabled() && !town.isRuined()) {
            if (TownySettings.getWarSiegeTownNeutralityEnabled() && town.getNeutralityChangeConfirmationCounterDays() > 0 && town.isNeutral() != town.getDesiredNeutralityValue()) {
                out.add(String.format(TownySettings.getLangString("status_town_neutrality_status_change_timer"), town.getNeutralityChangeConfirmationCounterDays()));
            }
            if (TownySettings.getWarSiegeRevoltEnabled() && town.isRevoltImmunityActive()) {
                out.add(String.format(TownySettings.getLangString("status_town_revolt_immunity_timer"), town.getFormattedHoursUntilRevoltCooldownEnds()));
            }
            if (town.hasSiege()) {
                final Siege siege = town.getSiege();
                switch (siege.getStatus()) {
                    case IN_PROGRESS: {
                        final String siegeStatus = String.format(TownySettings.getLangString("status_town_siege_status"), getStatusTownSiegeSummary(siege));
                        out.add(siegeStatus);
                        String[] bannerLocations = getBannerLocations(siege.getSiegeZones().values().toArray(new SiegeZone[0]));
                        if (bannerLocations.length > 10) {
                            final String[] entire2 = bannerLocations;
                            bannerLocations = new String[10];
                            System.arraycopy(entire2, 0, bannerLocations, 0, 10);
                            bannerLocations[10] = TownySettings.getLangString("status_town_siege_status_banners_xyz_list_overlength");
                        }
                        out.addAll(ChatTools.listArr(bannerLocations, String.format(TownySettings.getLangString("status_town_siege_status_banners_xyz_list"), bannerLocations.length)));
                        String[] siegeAttacks = getFormattedNames(siege.getSiegeZones().values().toArray(new SiegeZone[0]));
                        if (siegeAttacks.length > 10) {
                            final String[] entire3 = siegeAttacks;
                            siegeAttacks = new String[10];
                            System.arraycopy(entire3, 0, siegeAttacks, 0, 10);
                            siegeAttacks[10] = TownySettings.getLangString("status_town_siege_attacks_list_overlength");
                        }
                        out.addAll(ChatTools.listArr(siegeAttacks, String.format(TownySettings.getLangString("status_town_siege_attacks_list"), siegeAttacks.length)));
                        final String victoryTimer = String.format(TownySettings.getLangString("status_town_siege_victory_timer"), siege.getFormattedHoursUntilScheduledCompletion());
                        out.add(victoryTimer);
                        break;
                    }
                    case ATTACKER_WIN:
                    case DEFENDER_SURRENDER: {
                        final String siegeStatus = String.format(TownySettings.getLangString("status_town_siege_status"), getStatusTownSiegeSummary(siege));
                        final String invadedYesNo = siege.isTownInvaded() ? TownySettings.getLangString("status_yes") : TownySettings.getLangString("status_no_green");
                        final String plunderedYesNo = siege.isTownPlundered() ? TownySettings.getLangString("status_yes") : TownySettings.getLangString("status_no_green");
                        final String invadedPlunderedStatus = String.format(TownySettings.getLangString("status_town_siege_invaded_plundered_status"), invadedYesNo, plunderedYesNo);
                        final String siegeImmunityTimer = String.format(TownySettings.getLangString("status_town_siege_immunity_timer"), town.getFormattedHoursUntilSiegeImmunityEnds());
                        out.add(siegeStatus);
                        out.add(invadedPlunderedStatus);
                        out.add(siegeImmunityTimer);
                        break;
                    }
                    case DEFENDER_WIN:
                    case ATTACKER_ABANDON: {
                        final String siegeStatus = String.format(TownySettings.getLangString("status_town_siege_status"), getStatusTownSiegeSummary(siege));
                        final String siegeImmunityTimer = String.format(TownySettings.getLangString("status_town_siege_immunity_timer"), town.getFormattedHoursUntilSiegeImmunityEnds());
                        out.add(siegeStatus);
                        out.add(siegeImmunityTimer);
                        break;
                    }
                }
            }
            else if (TownySettings.getWarSiegeAttackEnabled() && town.isSiegeImmunityActive()) {
                out.add(String.format(TownySettings.getLangString("status_town_siege_status"), ""));
                out.add(String.format(TownySettings.getLangString("status_town_siege_immunity_timer"), town.getFormattedHoursUntilSiegeImmunityEnds()));
            }
        }
        out.addAll(getExtraFields(town));
        out = formatStatusScreens(out);
        return out;
    }
    
    public static List<String> getStatus(final Nation nation) {
        List<String> out = new ArrayList<String>();
        String title = getFormattedName(nation);
        title += (nation.isOpen() ? TownySettings.getLangString("status_title_open") : "");
        out.add(ChatTools.formatTitle(title));
        final Long registered = nation.getRegistered();
        if (registered != 0L) {
            out.add(String.format(TownySettings.getLangString("status_founded"), TownyFormatter.registeredFormat.format(nation.getRegistered())));
        }
        String line = "";
        if (TownySettings.isUsingEconomy() && TownyEconomyHandler.isActive()) {
            line = String.format(TownySettings.getLangString("status_bank"), nation.getAccount().getHoldingFormattedBalance());
            if (TownySettings.getNationUpkeepCost(nation) > 0.0) {
                line += String.format(TownySettings.getLangString("status_bank_town2"), TownySettings.getNationUpkeepCost(nation));
            }
        }
        if (nation.isNeutral()) {
            if (line.length() > 0) {
                line += "§8 | ";
            }
            line += TownySettings.getLangString("status_nation_peaceful");
        }
        if (nation.isPublic()) {
            if (line.length() > 0) {
                line += "§8 | ";
            }
            try {
                line += (nation.isPublic() ? (TownySettings.getLangString("status_town_size_part_5") + (nation.hasNationSpawn() ? Coord.parseCoord(nation.getNationSpawn()).toString() : TownySettings.getLangString("status_no_town")) + "]") : "");
            }
            catch (TownyException ex) {}
        }
        if (line.length() > 0) {
            out.add(line);
        }
        if (nation.getNumTowns() > 0 && nation.hasCapital() && nation.getCapital().hasMayor()) {
            out.add(String.format(TownySettings.getLangString("status_nation_king"), getFormattedName(nation.getCapital().getMayor())) + String.format(TownySettings.getLangString("status_nation_tax"), nation.getTaxes()));
        }
        final List<String> ranklist = new ArrayList<String>();
        final List<Town> towns = nation.getTowns();
        final List<Resident> residents = new ArrayList<Resident>();
        for (final Town town : towns) {
            residents.addAll(town.getResidents());
        }
        final List<String> nationranks = TownyPerms.getNationRanks();
        final List<Resident> residentwithrank = new ArrayList<Resident>();
        for (final String rank : nationranks) {
            for (final Resident r : residents) {
                if (r.getNationRanks() != null && r.getNationRanks().contains(rank)) {
                    residentwithrank.add(r);
                }
            }
            ranklist.addAll(getFormattedResidents(StringMgmt.capitalize(rank), residentwithrank));
            residentwithrank.clear();
        }
        if (ranklist != null) {
            out.addAll(ranklist);
        }
        String[] towns2 = getFormattedNames(nation.getTowns().toArray(new Town[0]));
        if (towns2.length > 10) {
            final String[] entire = towns2;
            towns2 = new String[12];
            System.arraycopy(entire, 0, towns2, 0, 11);
            towns2[11] = TownySettings.getLangString("status_town_reslist_overlength");
        }
        out.addAll(ChatTools.listArr(towns2, String.format(TownySettings.getLangString("status_nation_towns"), nation.getNumTowns())));
        String[] allies = getFormattedNames(nation.getAllies().toArray(new Nation[0]));
        if (allies.length > 10) {
            final String[] entire2 = allies;
            allies = new String[12];
            System.arraycopy(entire2, 0, allies, 0, 11);
            allies[11] = TownySettings.getLangString("status_town_reslist_overlength");
        }
        out.addAll(ChatTools.listArr(allies, String.format(TownySettings.getLangString("status_nation_allies"), nation.getAllies().size())));
        String[] enemies = getFormattedNames(nation.getEnemies().toArray(new Nation[0]));
        if (enemies.length > 10) {
            final String[] entire3 = enemies;
            enemies = new String[12];
            System.arraycopy(entire3, 0, enemies, 0, 11);
            enemies[11] = TownySettings.getLangString("status_town_reslist_overlength");
        }
        out.addAll(ChatTools.listArr(enemies, String.format(TownySettings.getLangString("status_nation_enemies"), nation.getEnemies().size())));
        final List<Town> siegeAttacks = nation.getTownsUnderSiegeAttack();
        final String[] formattedSiegeAttacks = getFormattedNames(siegeAttacks.toArray(new Town[0]));
        out.addAll(ChatTools.listArr(formattedSiegeAttacks, String.format(TownySettings.getLangString("status_nation_siege_attacks"), siegeAttacks.size())));
        final List<Town> siegeDefences = nation.getTownsUnderSiegeDefence();
        final String[] formattedSiegeDefences = getFormattedNames(siegeDefences.toArray(new Town[0]));
        out.addAll(ChatTools.listArr(formattedSiegeDefences, String.format(TownySettings.getLangString("status_nation_siege_defences"), siegeDefences.size())));
        out.addAll(getExtraFields(nation));
        out = formatStatusScreens(out);
        return out;
    }
    
    private static String getStatusTownSiegeSummary(final Siege siege) {
        switch (siege.getStatus()) {
            case IN_PROGRESS: {
                return TownySettings.getLangString("status_town_siege_status_in_progress");
            }
            case ATTACKER_WIN: {
                return String.format(TownySettings.getLangString("status_town_siege_status_attacker_win"), getFormattedNationName(siege.getAttackerWinner()));
            }
            case DEFENDER_SURRENDER: {
                return String.format(TownySettings.getLangString("status_town_siege_status_defender_surrender"), getFormattedNationName(siege.getAttackerWinner()));
            }
            case DEFENDER_WIN: {
                return TownySettings.getLangString("status_town_siege_status_defender_win");
            }
            case ATTACKER_ABANDON: {
                return TownySettings.getLangString("status_town_siege_status_attacker_abandon");
            }
            default: {
                return "???";
            }
        }
    }
    
    public static List<String> getStatus(final TownyWorld world) {
        List<String> out = new ArrayList<String>();
        String title = getFormattedName(world);
        title += ((world.isPVP() || world.isForcePVP()) ? TownySettings.getLangString("status_title_pvp") : "");
        title += (world.isClaimable() ? TownySettings.getLangString("status_world_claimable") : TownySettings.getLangString("status_world_noclaims"));
        out.add(ChatTools.formatTitle(title));
        if (!world.isUsingTowny()) {
            out.add(TownySettings.getLangString("msg_set_use_towny_off"));
        }
        else {
            out.add(TownySettings.getLangString("status_world_forcepvp") + (world.isForcePVP() ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + "§8" + " | " + TownySettings.getLangString("status_world_fire") + (world.isFire() ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + "§8" + " | " + TownySettings.getLangString("status_world_forcefire") + (world.isForceFire() ? TownySettings.getLangString("status_forced") : TownySettings.getLangString("status_adjustable")));
            out.add(TownySettings.getLangString("explosions2") + ": " + (world.isExpl() ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + "§8" + " | " + TownySettings.getLangString("status_world_forceexplosion") + (world.isForceExpl() ? TownySettings.getLangString("status_forced") : TownySettings.getLangString("status_adjustable")));
            out.add(TownySettings.getLangString("status_world_worldmobs") + (world.hasWorldMobs() ? TownySettings.getLangString("status_on") : TownySettings.getLangString("status_off")) + "§8" + " | " + TownySettings.getLangString("status_world_forcetownmobs") + (world.isForceTownMobs() ? TownySettings.getLangString("status_forced") : TownySettings.getLangString("status_adjustable")));
            out.add("§2" + (world.isWarAllowed() ? TownySettings.getLangString("msg_set_war_allowed_on") : TownySettings.getLangString("msg_set_war_allowed_off")));
            out.add(TownySettings.getLangString("status_world_unclaimrevert") + (world.isUsingPlotManagementRevert() ? TownySettings.getLangString("status_on_good") : TownySettings.getLangString("status_off_bad")) + "§8" + " | " + TownySettings.getLangString("status_world_explrevert") + (world.isUsingPlotManagementWildRevert() ? TownySettings.getLangString("status_on_good") : TownySettings.getLangString("status_off_bad")));
            out.add("§2" + world.getUnclaimedZoneName() + ":");
            out.add("    " + (world.getUnclaimedZoneBuild() ? "§a" : "§c") + "Build" + "§8" + ", " + (world.getUnclaimedZoneDestroy() ? "§a" : "§c") + "Destroy" + "§8" + ", " + (world.getUnclaimedZoneSwitch() ? "§a" : "§c") + "Switch" + "§8" + ", " + (world.getUnclaimedZoneItemUse() ? "§a" : "§c") + "ItemUse");
            out.add("    " + TownySettings.getLangString("status_world_ignoredblocks") + "§a" + " " + StringMgmt.join(world.getUnclaimedZoneIgnoreMaterials(), ", "));
            out.addAll(getExtraFields(world));
        }
        out = formatStatusScreens(out);
        return out;
    }
    
    public static List<String> getTaxStatus(final Resident resident) {
        final List<String> out = new ArrayList<String>();
        Town town = null;
        double plotTax = 0.0;
        out.add(ChatTools.formatTitle(getFormattedName(resident) + (BukkitTools.isOnline(resident.getName()) ? "§a (Online)" : "")));
        if (resident.hasTown()) {
            try {
                town = resident.getTown();
                out.add(String.format(TownySettings.getLangString("owner_of_x_plots"), resident.getTownBlocks().size()));
                if (TownyPerms.getResidentPerms(resident).containsKey("towny.tax_exempt")) {
                    out.add(TownySettings.getLangString("status_res_taxexempt"));
                }
                else if (town.isTaxPercentage()) {
                    out.add(String.format(TownySettings.getLangString("status_res_tax"), resident.getAccount().getHoldingBalance() * town.getTaxes() / 100.0));
                }
                else {
                    out.add(String.format(TownySettings.getLangString("status_res_tax"), town.getTaxes()));
                    if (resident.getTownBlocks().size() > 0) {
                        for (final TownBlock townBlock : new ArrayList<TownBlock>(resident.getTownBlocks())) {
                            plotTax += townBlock.getType().getTax(townBlock.getTown());
                        }
                        out.add(TownySettings.getLangString("status_res_plottax") + plotTax);
                    }
                    out.add(TownySettings.getLangString("status_res_totaltax") + (town.getTaxes() + plotTax));
                }
            }
            catch (NotRegisteredException ex) {}
            catch (EconomyException ex2) {}
        }
        return out;
    }
    
    public static String getNamePrefix(final Resident resident) {
        if (resident == null) {
            return "";
        }
        if (resident.isKing()) {
            return TownySettings.getKingPrefix(resident);
        }
        if (resident.isMayor()) {
            return TownySettings.getMayorPrefix(resident);
        }
        return "";
    }
    
    public static String getNamePostfix(final Resident resident) {
        if (resident == null) {
            return "";
        }
        if (resident.isKing()) {
            return TownySettings.getKingPostfix(resident);
        }
        if (resident.isMayor()) {
            return TownySettings.getMayorPostfix(resident);
        }
        return "";
    }
    
    public static String getFormattedName(final TownyObject obj) {
        if (obj == null) {
            return "Null";
        }
        if (obj instanceof Resident) {
            return getFormattedResidentName((Resident)obj);
        }
        if (obj instanceof Town) {
            return getFormattedTownName((Town)obj);
        }
        if (obj instanceof Nation) {
            return getFormattedNationName((Nation)obj);
        }
        return obj.getName().replaceAll("_", " ");
    }
    
    public static String getFormattedResidentName(final Resident resident) {
        if (resident == null) {
            return "null";
        }
        if (resident.isKing()) {
            return (resident.hasTitle() ? (resident.getTitle() + " ") : TownySettings.getKingPrefix(resident)) + resident.getName() + (resident.hasSurname() ? (" " + resident.getSurname()) : TownySettings.getKingPostfix(resident));
        }
        if (resident.isMayor()) {
            return (resident.hasTitle() ? (resident.getTitle() + " ") : TownySettings.getMayorPrefix(resident)) + resident.getName() + (resident.hasSurname() ? (" " + resident.getSurname()) : TownySettings.getMayorPostfix(resident));
        }
        return (resident.hasTitle() ? (resident.getTitle() + " ") : "") + resident.getName() + (resident.hasSurname() ? (" " + resident.getSurname()) : "");
    }
    
    public static String getFormattedTownName(final Town town) {
        if (town.isCapital()) {
            return TownySettings.getCapitalPrefix(town) + town.getName().replaceAll("_", " ") + TownySettings.getCapitalPostfix(town);
        }
        return TownySettings.getTownPrefix(town) + town.getName().replaceAll("_", " ") + TownySettings.getTownPostfix(town);
    }
    
    public static String getFormattedNationName(final Nation nation) {
        return TownySettings.getNationPrefix(nation) + nation.getName().replaceAll("_", " ") + TownySettings.getNationPostfix(nation);
    }
    
    public static String getFormattedResidentTitleName(final Resident resident) {
        if (!resident.hasTitle()) {
            return getFormattedName(resident);
        }
        return resident.getTitle() + " " + resident.getName();
    }
    
    public static String[] getFormattedNames(final Resident[] residents) {
        final List<String> names = new ArrayList<String>();
        for (final Resident resident : residents) {
            names.add(getFormattedName(resident));
        }
        return names.toArray(new String[0]);
    }
    
    public static String[] getFormattedNames(final Town[] towns) {
        final List<String> names = new ArrayList<String>();
        for (final Town town : towns) {
            names.add(getFormattedName(town));
        }
        return names.toArray(new String[0]);
    }
    
    public static String[] getFormattedNames(final Nation[] nations) {
        final List<String> names = new ArrayList<String>();
        for (final Nation nation : nations) {
            names.add(getFormattedName(nation));
        }
        return names.toArray(new String[0]);
    }
    
    public static String[] getFormattedNames(final SiegeZone[] siegeZones) {
        final List<String> names = new ArrayList<String>();
        for (final SiegeZone siegeZone : siegeZones) {
            names.add(getFormattedName(siegeZone));
        }
        return names.toArray(new String[0]);
    }
    
    public static String[] getBannerLocations(final SiegeZone[] siegeZones) {
        final List<String> locations = new ArrayList<String>();
        for (final SiegeZone siegeZone : siegeZones) {
            locations.add("{" + siegeZone.getFlagLocation().getBlockX() + "," + siegeZone.getFlagLocation().getBlockY() + "," + siegeZone.getFlagLocation().getBlockZ() + "}");
        }
        return locations.toArray(new String[0]);
    }
    
    public static String getFormattedName(final SiegeZone siegeZone) {
        final StringBuilder builder = new StringBuilder();
        builder.append(getFormattedName(siegeZone.getAttackingNation()));
        builder.append(" {");
        if (siegeZone.getSiegePoints() > 0) {
            builder.append("+");
        }
        builder.append(siegeZone.getSiegePoints());
        builder.append("}");
        return builder.toString();
    }
    
    public static List<String> getExtraFields(TownyObject to) {
		if (!to.hasMeta())
			return new ArrayList<>();
		
		List<String> extraFields = new ArrayList<>();
		
		String field = "";
		
		for (CustomDataField cdf : to.getMetadata()) {
			if (!cdf.hasLabel())
				continue;
			
			if (extraFields.contains(field))
				field = Colors.Green + cdf.getLabel() + ": ";
			else
				field += Colors.Green + cdf.getLabel() + ": ";
			
			switch (cdf.getType()) {
				case IntegerField:
					int ival = (int) cdf.getValue();
					field += (ival <= 0 ? Colors.Red : Colors.LightGreen) + ival;
					break;
				case StringField:
					field += Colors.White + cdf.getValue();
					break;
				case BooleanField:
					boolean bval = (boolean) cdf.getValue();
					field += (bval ? Colors.LightGreen : Colors.Red) + bval;
					break;
				case DecimalField:
					double dval = (double) cdf.getValue();
					field += (dval <= 0 ? Colors.Red : Colors.LightGreen) + dval;
					break;
			}
			
			field += "  ";
			
			if (field.length() > 40)
				extraFields.add(field);
		}
		
		extraFields.add(field);
		
		return extraFields;
	}

	public static List<String> getExtraFields(TownBlock tb) {
		if (!tb.hasMeta())
			return new ArrayList<>();

		List<String> extraFields = new ArrayList<>();

		String field = "";

		for (CustomDataField cdf : tb.getMetadata()) {
			if (!cdf.hasLabel())
				continue;

			if (extraFields.contains(field))
				field = Colors.Green + cdf.getLabel() + ": ";
			else
				field += Colors.Green + cdf.getLabel() + ": ";

			switch (cdf.getType()) {
				case IntegerField:
					int ival = (int) cdf.getValue();
					field += (ival <= 0 ? Colors.Red : Colors.LightGreen) + ival;
					break;
				case StringField:
					field += Colors.White + cdf.getValue();
					break;
				case BooleanField:
					boolean bval = (boolean) cdf.getValue();
					field += (bval ? Colors.LightGreen : Colors.Red) + bval;
					break;
				case DecimalField:
					double dval = (double) cdf.getValue();
					field += (dval <= 0 ? Colors.Red : Colors.LightGreen) + dval;
					break;
			}

			field += "  ";

			if (field.length() > 40)
				extraFields.add(field);
		}

		extraFields.add(field);

		return extraFields;
	}
    
    static {
        lastOnlineFormat = new SimpleDateFormat("MMMMM dd '@' HH:mm");
        lastOnlineFormatIncludeYear = new SimpleDateFormat("MMMMM dd yyyy");
        registeredFormat = new SimpleDateFormat("MMM d yyyy");
    }
}
