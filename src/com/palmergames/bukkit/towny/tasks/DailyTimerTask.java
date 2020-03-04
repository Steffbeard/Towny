// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.object.EconomyHandler;
import java.util.ListIterator;
import java.util.Collection;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.Iterator;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.io.IOException;
import com.palmergames.bukkit.towny.war.siegewar.timeractions.UpdateTownNeutralityCounters;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.scheduler.BukkitRunnable;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.war.siegewar.timeractions.RemoveRuinedTowns;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.Towny;
import java.util.List;

public class DailyTimerTask extends TownyTimerTask
{
    private double totalTownUpkeep;
    private double totalNationUpkeep;
    private List<String> removedTowns;
    private List<String> removedNations;
    
    public DailyTimerTask(final Towny plugin) {
        super(plugin);
        this.totalTownUpkeep = 0.0;
        this.totalNationUpkeep = 0.0;
        this.removedTowns = new ArrayList<String>();
        this.removedNations = new ArrayList<String>();
    }
    
    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        TownyMessaging.sendDebugMsg("New Day");
        if (TownyEconomyHandler.isActive() && TownySettings.isTaxingDaily()) {
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_new_day_tax"), new Object[0]));
            try {
                TownyMessaging.sendDebugMsg("Collecting Town Taxes");
                this.collectTownTaxes();
                TownyMessaging.sendDebugMsg("Collecting Nation Taxes");
                this.collectNationTaxes();
                TownyMessaging.sendDebugMsg("Collecting Town Costs");
                this.collectTownCosts();
                TownyMessaging.sendDebugMsg("Collecting Nation Costs");
                this.collectNationCosts();
                if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeDelayFullTownRemoval()) {
                    TownyMessaging.sendDebugMsg("Deleting old ruins");
                    RemoveRuinedTowns.removeRuinedTowns();
                }
                Bukkit.getServer().getPluginManager().callEvent((Event)new NewDayEvent(this.removedTowns, this.removedNations, this.totalTownUpkeep, this.totalNationUpkeep, start));
            }
            catch (EconomyException ignored) {
                System.out.println("Economy Exception");
            }
            catch (TownyException e) {
                e.printStackTrace();
            }
        }
        else {
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_new_day"), new Object[0]));
        }
        if (TownySettings.isDeletingOldResidents()) {
            new ResidentPurge(this.plugin, null, TownySettings.getDeleteTime() * 1000L, TownySettings.isDeleteTownlessOnly()).start();
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (!townyUniverse.getJailedResidentMap().isEmpty()) {
            for (final Resident resident : townyUniverse.getJailedResidentMap()) {
                if (resident.hasJailDays()) {
                    if (resident.getJailDays() == 1) {
                        resident.setJailDays(0);
                        new BukkitRunnable() {
                            public void run() {
                                Town jailTown = null;
                                try {
                                    jailTown = townyUniverse.getDataSource().getTown(resident.getJailTown());
                                }
                                catch (NotRegisteredException ex) {}
                                final int index = resident.getJailSpawn();
                                resident.setJailed(resident, index, jailTown);
                            }
                        }.runTaskLater((Plugin)this.plugin, 20L);
                    }
                    else {
                        resident.setJailDays(resident.getJailDays() - 1);
                    }
                }
                townyUniverse.getDataSource().saveResident(resident);
            }
        }
        for (final Town towns : TownyUniverse.getInstance().getDataSource().getTowns()) {
            if (towns.isConquered()) {
                if (towns.getConqueredDays() == 1) {
                    towns.setConquered(false);
                    towns.setConqueredDays(0);
                }
                else {
                    towns.setConqueredDays(towns.getConqueredDays() - 1);
                }
            }
        }
        if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeTownNeutralityEnabled()) {
            UpdateTownNeutralityCounters.updateTownNeutralityCounters();
        }
        TownyMessaging.sendDebugMsg("Cleaning up old backups.");
        townyUniverse.getDataSource().cleanupBackups();
        if (TownySettings.isBackingUpDaily()) {
            try {
                TownyMessaging.sendDebugMsg("Making backup.");
                townyUniverse.getDataSource().backup();
            }
            catch (IOException e2) {
                TownyMessaging.sendErrorMsg("Could not create backup.");
                e2.printStackTrace();
            }
        }
        TownyMessaging.sendDebugMsg("Finished New Day Code");
        TownyMessaging.sendDebugMsg("Universe Stats:");
        TownyMessaging.sendDebugMsg("    Residents: " + townyUniverse.getDataSource().getResidents().size());
        TownyMessaging.sendDebugMsg("    Towns: " + townyUniverse.getDataSource().getTowns().size());
        TownyMessaging.sendDebugMsg("    Nations: " + townyUniverse.getDataSource().getNations().size());
        for (final TownyWorld world : townyUniverse.getDataSource().getWorlds()) {
            TownyMessaging.sendDebugMsg("    " + world.getName() + " (townblocks): " + world.getTownBlocks().size());
        }
        TownyMessaging.sendDebugMsg("Memory (Java Heap):");
        TownyMessaging.sendDebugMsg(String.format("%8d Mb (max)", Runtime.getRuntime().maxMemory() / 1024L / 1024L));
        TownyMessaging.sendDebugMsg(String.format("%8d Mb (total)", Runtime.getRuntime().totalMemory() / 1024L / 1024L));
        TownyMessaging.sendDebugMsg(String.format("%8d Mb (free)", Runtime.getRuntime().freeMemory() / 1024L / 1024L));
        TownyMessaging.sendDebugMsg(String.format("%8d Mb (used=total-free)", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L));
        TownyMessaging.sendDebugMsg("newDay took " + (System.currentTimeMillis() - start) + "ms");
    }
    
    public void collectNationTaxes() throws EconomyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Nation> nations = new ArrayList<Nation>(townyUniverse.getDataSource().getNations());
        final ListIterator<Nation> nationItr = nations.listIterator();
        while (nationItr.hasNext()) {
            final Nation nation = nationItr.next();
            if (townyUniverse.getDataSource().hasNation(nation.getName())) {
                this.collectNationTaxes(nation);
            }
        }
    }
    
    protected void collectNationTaxes(final Nation nation) throws EconomyException {
        if (nation.getTaxes() > 0.0) {
            final List<Town> towns = new ArrayList<Town>(nation.getTowns());
            final ListIterator<Town> townItr = towns.listIterator();
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            while (townItr.hasNext()) {
                final Town town = townItr.next();
                if (townyUniverse.getDataSource().hasTown(town.getName())) {
                    if (town.isCapital() || !town.hasUpkeep() || town.isRuined()) {
                        continue;
                    }
                    if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeTownNeutralityEnabled() && town.isNeutral()) {
                        continue;
                    }
                    if (town.getAccount().payTo(nation.getTaxes(), nation, "Nation Tax")) {
                        continue;
                    }
                    try {
                        if (TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeInvadeEnabled()) {
                            this.universe.getDataSource().removeTown(town);
                            TownyMessaging.sendGlobalMessage(town.getName() + TownySettings.getLangString("msg_bankrupt_town"));
                        }
                        else {
                            this.removedTowns.add(town.getName());
                            nation.removeTown(town);
                        }
                    }
                    catch (EmptyNationException ex) {}
                    catch (NotRegisteredException ex2) {}
                    townyUniverse.getDataSource().saveTown(town);
                    townyUniverse.getDataSource().saveNation(nation);
                }
                else {
                    TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getPayedTownTaxMsg() + nation.getTaxes());
                }
            }
            if (this.removedTowns != null) {
                if (this.removedTowns.size() == 1) {
                    TownyMessaging.sendNationMessagePrefixed(nation, String.format(TownySettings.getLangString("msg_couldnt_pay_tax"), ChatTools.list(this.removedTowns), "nation"));
                }
                else {
                    TownyMessaging.sendNationMessagePrefixed(nation, ChatTools.list(this.removedTowns, TownySettings.getLangString("msg_couldnt_pay_nation_tax_multiple")));
                }
            }
        }
    }
    
    public void collectTownTaxes() throws EconomyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Town> towns = new ArrayList<Town>(townyUniverse.getDataSource().getTowns());
        final ListIterator<Town> townItr = towns.listIterator();
        while (townItr.hasNext()) {
            final Town town = townItr.next();
            if (townyUniverse.getDataSource().hasTown(town.getName()) && !town.isRuined()) {
                this.collectTownTaxes(town);
            }
        }
    }
    
    protected void collectTownTaxes(final Town town) throws EconomyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (town.getTaxes() > 0.0) {
            final List<Resident> residents = new ArrayList<Resident>(town.getResidents());
            final ListIterator<Resident> residentItr = residents.listIterator();
            final List<String> removedResidents = new ArrayList<String>();
            while (residentItr.hasNext()) {
                final Resident resident = residentItr.next();
                if (townyUniverse.getDataSource().hasResident(resident.getName())) {
                    if (!TownyPerms.getResidentPerms(resident).containsKey("towny.tax_exempt")) {
                        if (!resident.isNPC()) {
                            if (town.isTaxPercentage()) {
                                final double cost = resident.getAccount().getHoldingBalance() * town.getTaxes() / 100.0;
                                resident.getAccount().payTo(cost, town, "Town Tax (Percentage)");
                                continue;
                            }
                            if (!resident.getAccount().payTo(town.getTaxes(), town, "Town Tax")) {
                                removedResidents.add(resident.getName());
                                try {
                                    resident.clear();
                                    townyUniverse.getDataSource().saveTown(town);
                                }
                                catch (EmptyTownException e) {
                                    townyUniverse.getDataSource().removeTown(town);
                                }
                                townyUniverse.getDataSource().saveResident(resident);
                                continue;
                            }
                            continue;
                        }
                    }
                    try {
                        TownyMessaging.sendResidentMessage(resident, TownySettings.getTaxExemptMsg());
                    }
                    catch (TownyException ex) {}
                }
            }
            if (removedResidents != null) {
                if (removedResidents.size() == 1) {
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_couldnt_pay_tax"), ChatTools.list(removedResidents), "town"));
                }
                else {
                    TownyMessaging.sendPrefixedTownMessage(town, ChatTools.list(removedResidents, TownySettings.getLangString("msg_couldnt_pay_town_tax_multiple")));
                }
            }
        }
        if (town.getPlotTax() > 0.0 || town.getCommercialPlotTax() > 0.0 || town.getEmbassyPlotTax() > 0.0) {
            final List<TownBlock> townBlocks = new ArrayList<TownBlock>(town.getTownBlocks());
            final List<String> lostPlots = new ArrayList<String>();
            final ListIterator<TownBlock> townBlockItr = townBlocks.listIterator();
            while (townBlockItr.hasNext()) {
                final TownBlock townBlock = townBlockItr.next();
                if (!townBlock.hasResident()) {
                    continue;
                }
                try {
                    final Resident resident2 = townBlock.getResident();
                    if (!townyUniverse.getDataSource().hasResident(resident2.getName())) {
                        continue;
                    }
                    if (resident2.hasTown() && resident2.getTown() == townBlock.getTown() && (TownyPerms.getResidentPerms(resident2).containsKey("towny.tax_exempt") || resident2.isNPC())) {
                        continue;
                    }
                    if (resident2.getAccount().payTo(townBlock.getType().getTax(town), town, String.format("Plot Tax (%s)", townBlock.getType()))) {
                        continue;
                    }
                    if (!lostPlots.contains(resident2.getName())) {
                        lostPlots.add(resident2.getName());
                    }
                    townBlock.setResident(null);
                    townBlock.setPlotPrice(-1.0);
                    townBlock.setType(townBlock.getType());
                    townyUniverse.getDataSource().saveResident(resident2);
                    townyUniverse.getDataSource().saveTownBlock(townBlock);
                }
                catch (NotRegisteredException ex2) {}
            }
            if (lostPlots != null) {
                if (lostPlots.size() == 1) {
                    TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_couldnt_pay_plot_taxes"), ChatTools.list(lostPlots)));
                }
                else {
                    TownyMessaging.sendPrefixedTownMessage(town, ChatTools.list(lostPlots, TownySettings.getLangString("msg_couldnt_pay_plot_taxes_multiple")));
                }
            }
        }
    }
    
    public void collectTownCosts() throws EconomyException, TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Town> towns = new ArrayList<Town>(townyUniverse.getDataSource().getTowns());
        final ListIterator<Town> townItr = towns.listIterator();
        while (townItr.hasNext()) {
            final Town town = townItr.next();
            if (townyUniverse.getDataSource().hasTown(town.getName()) && town.hasUpkeep() && !town.isRuined()) {
                double upkeep = TownySettings.getTownUpkeepCost(town);
                final double upkeepPenalty = TownySettings.getTownPenaltyUpkeepCost(town);
                if (upkeepPenalty > 0.0 && upkeep > 0.0) {
                    upkeep += upkeepPenalty;
                }
                this.totalTownUpkeep += upkeep;
                if (upkeep > 0.0) {
                    if (town.getAccount().pay(upkeep, "Town Upkeep")) {
                        continue;
                    }
                    townyUniverse.getDataSource().removeTown(town);
                    this.removedTowns.add(town.getName());
                }
                else {
                    if (upkeep >= 0.0) {
                        continue;
                    }
                    if (TownySettings.isUpkeepPayingPlots()) {
                        final List<TownBlock> plots = new ArrayList<TownBlock>(town.getTownBlocks());
                        for (final TownBlock townBlock : plots) {
                            if (townBlock.hasResident()) {
                                townBlock.getResident().getAccount().pay(upkeep / plots.size(), "Negative Town Upkeep - Plot income");
                            }
                            else {
                                town.getAccount().pay(upkeep / plots.size(), "Negative Town Upkeep - Plot income");
                            }
                        }
                    }
                    else {
                        town.getAccount().pay(upkeep, "Negative Town Upkeep");
                    }
                }
            }
        }
        if (this.removedTowns != null) {
            if (this.removedTowns.size() == 1) {
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_bankrupt_town2"), ChatTools.list(this.removedTowns)));
            }
            else {
                TownyMessaging.sendGlobalMessage(ChatTools.list(this.removedTowns, TownySettings.getLangString("msg_bankrupt_town_multiple")));
            }
        }
    }
    
    public void collectNationCosts() throws EconomyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Nation> nations = new ArrayList<Nation>(townyUniverse.getDataSource().getNations());
        final ListIterator<Nation> nationItr = nations.listIterator();
        while (nationItr.hasNext()) {
            final Nation nation = nationItr.next();
            if (townyUniverse.getDataSource().hasNation(nation.getName())) {
                final double upkeep = TownySettings.getNationUpkeepCost(nation);
                this.totalNationUpkeep += upkeep;
                if (upkeep > 0.0) {
                    if (!nation.getAccount().pay(TownySettings.getNationUpkeepCost(nation), "Nation Upkeep")) {
                        townyUniverse.getDataSource().removeNation(nation);
                        this.removedNations.add(nation.getName());
                    }
                    if (!nation.isNeutral() || nation.getAccount().pay(TownySettings.getNationNeutralityCost(), "Nation Peace Upkeep")) {
                        continue;
                    }
                    try {
                        nation.setNeutral(false);
                    }
                    catch (TownyException e) {
                        e.printStackTrace();
                    }
                    townyUniverse.getDataSource().saveNation(nation);
                    TownyMessaging.sendPrefixedNationMessage(nation, TownySettings.getLangString("msg_nation_not_peaceful"));
                }
                else {
                    if (upkeep >= 0.0) {
                        continue;
                    }
                    nation.getAccount().pay(upkeep, "Negative Nation Upkeep");
                }
            }
        }
        if (this.removedNations != null) {
            if (this.removedNations.size() == 1) {
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_bankrupt_nation2"), ChatTools.list(this.removedNations)));
            }
            else {
                TownyMessaging.sendGlobalMessage(ChatTools.list(this.removedNations, TownySettings.getLangString("msg_bankrupt_nation_multiple")));
            }
        }
    }
}
