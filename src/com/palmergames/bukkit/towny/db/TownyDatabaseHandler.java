// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.db;

import java.util.Map;
import com.palmergames.bukkit.towny.object.EconomyHandler;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.event.RenameResidentEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import java.util.Set;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarTimeUtil;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.event.PreDeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.object.EconomyAccount;
import com.palmergames.bukkit.towny.war.eventwar.WarSpoils;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.event.PreDeleteTownEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.event.TownUnclaimEvent;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.event.TownPreUnclaimEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.DeletePlayerEvent;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import java.util.Iterator;
import java.util.UUID;
import com.palmergames.bukkit.towny.object.Town;
import java.util.Collection;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.List;
import org.bukkit.entity.Player;
import javax.naming.InvalidNameException;
import com.palmergames.bukkit.util.NameValidation;
import com.palmergames.bukkit.towny.TownySettings;
import java.io.File;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;

public abstract class TownyDatabaseHandler extends TownyDataSource
{
    final String rootFolderPath;
    final String dataFolderPath;
    final String settingsFolderPath;
    final String logFolderPath;
    final String backupFolderPath;
    
    public TownyDatabaseHandler(final Towny plugin, final TownyUniverse universe) {
        super(plugin, universe);
        this.rootFolderPath = universe.getRootFolder();
        this.dataFolderPath = this.rootFolderPath + File.separator + "data";
        this.settingsFolderPath = this.rootFolderPath + File.separator + "settings";
        this.logFolderPath = this.rootFolderPath + File.separator + "logs";
        this.backupFolderPath = this.rootFolderPath + File.separator + "backup";
    }
    
    @Override
    public boolean hasResident(final String name) {
        try {
            return TownySettings.isFakeResident(name) || this.universe.getResidentMap().containsKey(NameValidation.checkAndFilterPlayerName(name).toLowerCase());
        }
        catch (InvalidNameException e) {
            return false;
        }
    }
    
    @Override
    public boolean hasTown(final String name) {
        return this.universe.getTownsMap().containsKey(name.toLowerCase());
    }
    
    @Override
    public boolean hasNation(final String name) {
        return this.universe.getNationsMap().containsKey(name.toLowerCase());
    }
    
    @Override
    public List<Resident> getResidents(final Player player, final String[] names) {
        final List<Resident> invited = new ArrayList<Resident>();
        for (final String name : names) {
            try {
                final Resident target = this.getResident(name);
                invited.add(target);
            }
            catch (TownyException x) {
                TownyMessaging.sendErrorMsg(player, x.getMessage());
            }
        }
        return invited;
    }
    
    @Override
    public List<Resident> getResidents(final String[] names) {
        final List<Resident> matches = new ArrayList<Resident>();
        for (final String name : names) {
            try {
                matches.add(this.getResident(name));
            }
            catch (NotRegisteredException ex) {}
        }
        return matches;
    }
    
    @Override
    public List<Resident> getResidents() {
        return new ArrayList<Resident>(this.universe.getResidentMap().values());
    }
    
    @Override
    public Resident getResident(String name) throws NotRegisteredException {
        try {
            name = NameValidation.checkAndFilterPlayerName(name).toLowerCase();
        }
        catch (InvalidNameException ex) {}
        if (!this.hasResident(name)) {
            throw new NotRegisteredException(String.format("The resident '%s' is not registered.", name));
        }
        if (TownySettings.isFakeResident(name)) {
            final Resident resident = new Resident(name);
            resident.setNPC(true);
            return resident;
        }
        return this.universe.getResidentMap().get(name);
    }
    
    @Override
    public List<Town> getTowns(final String[] names) {
        final List<Town> matches = new ArrayList<Town>();
        for (final String name : names) {
            try {
                matches.add(this.getTown(name));
            }
            catch (NotRegisteredException ex) {}
        }
        return matches;
    }
    
    @Override
    public List<Town> getTowns() {
        return new ArrayList<Town>(this.universe.getTownsMap().values());
    }
    
    @Override
    public Town getTown(String name) throws NotRegisteredException {
        try {
            name = NameValidation.checkAndFilterName(name).toLowerCase();
        }
        catch (InvalidNameException ex) {}
        if (!this.hasTown(name)) {
            throw new NotRegisteredException(String.format("The town '%s' is not registered.", name));
        }
        return this.universe.getTownsMap().get(name);
    }
    
    @Override
    public Town getTown(final UUID uuid) throws NotRegisteredException {
        String name = null;
        for (final Town town : this.getTowns()) {
            if (uuid.equals(town.getUuid())) {
                name = town.getName();
            }
        }
        if (name == null) {
            throw new NotRegisteredException(String.format("The town with uuid '%s' is not registered.", uuid));
        }
        try {
            name = NameValidation.checkAndFilterName(name).toLowerCase();
        }
        catch (InvalidNameException ex) {}
        return this.universe.getTownsMap().get(name);
    }
    
    public PlotObjectGroup getPlotObjectGroup(final String townName, final UUID groupID) {
        return this.universe.getGroup(townName, groupID);
    }
    
    @Override
    public List<Nation> getNations(final String[] names) {
        final List<Nation> matches = new ArrayList<Nation>();
        for (final String name : names) {
            try {
                matches.add(this.getNation(name));
            }
            catch (NotRegisteredException ex) {}
        }
        return matches;
    }
    
    @Override
    public List<Nation> getNations() {
        return new ArrayList<Nation>(this.universe.getNationsMap().values());
    }
    
    @Override
    public Nation getNation(String name) throws NotRegisteredException {
        try {
            name = NameValidation.checkAndFilterName(name).toLowerCase();
        }
        catch (InvalidNameException ex) {}
        if (!this.hasNation(name)) {
            throw new NotRegisteredException(String.format("The nation '%s' is not registered.", name));
        }
        return this.universe.getNationsMap().get(name.toLowerCase());
    }
    
    @Override
    public Nation getNation(final UUID uuid) throws NotRegisteredException {
        String name = null;
        for (final Nation nation : this.getNations()) {
            if (uuid.equals(nation.getUuid())) {
                name = nation.getName();
            }
        }
        if (name == null) {
            throw new NotRegisteredException(String.format("The town with uuid '%s' is not registered.", uuid));
        }
        try {
            name = NameValidation.checkAndFilterName(name).toLowerCase();
        }
        catch (InvalidNameException ex) {}
        return this.universe.getNationsMap().get(name);
    }
    
    @Override
    public TownyWorld getWorld(final String name) throws NotRegisteredException {
        final TownyWorld world = this.universe.getWorldMap().get(name.toLowerCase());
        if (world == null) {
            throw new NotRegisteredException("World not registered!");
        }
        return world;
    }
    
    @Override
    public List<TownyWorld> getWorlds() {
        return new ArrayList<TownyWorld>(this.universe.getWorldMap().values());
    }
    
    @Override
    public TownyWorld getTownWorld(final String townName) {
        for (final TownyWorld world : this.universe.getWorldMap().values()) {
            if (world.hasTown(townName)) {
                return world;
            }
        }
        return null;
    }
    
    @Override
    public void removeResident(final Resident resident) {
        Town town = null;
        if (resident.hasTown()) {
            try {
                town = resident.getTown();
            }
            catch (NotRegisteredException e1) {
                e1.printStackTrace();
            }
        }
        try {
            if (town != null) {
                town.removeResident(resident);
                if (town.hasNation()) {
                    this.saveNation(town.getNation());
                }
                this.saveTown(town);
            }
            resident.clear();
        }
        catch (EmptyTownException e3) {
            this.removeTown(town);
        }
        catch (NotRegisteredException e2) {
            e2.printStackTrace();
        }
        try {
            for (final Town townOutlaw : this.getTowns()) {
                if (townOutlaw.hasOutlaw(resident)) {
                    townOutlaw.removeOutlaw(resident);
                    this.saveTown(townOutlaw);
                }
            }
        }
        catch (NotRegisteredException e2) {
            e2.printStackTrace();
        }
        BukkitTools.getPluginManager().callEvent((Event)new DeletePlayerEvent(resident.getName()));
    }
    
    public void removeOneOfManyTownBlocks(final TownBlock townBlock, final Town town) {
        final TownPreUnclaimEvent event = new TownPreUnclaimEvent(townBlock);
        BukkitTools.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return;
        }
        Resident resident = null;
        try {
            resident = townBlock.getResident();
        }
        catch (NotRegisteredException ex) {}
        final TownyWorld world = townBlock.getWorld();
        final WorldCoord coord = townBlock.getWorldCoord();
        if (world.isUsingPlotManagementDelete()) {
            TownyRegenAPI.addDeleteTownBlockIdQueue(coord);
        }
        if (world.isUsingPlotManagementRevert()) {
            final PlotBlockData plotData = TownyRegenAPI.getPlotChunkSnapshot(townBlock);
            if (plotData != null && !plotData.getBlockList().isEmpty()) {
                TownyRegenAPI.addPlotChunk(plotData, true);
            }
        }
        if (resident != null) {
            this.saveResident(resident);
        }
        world.removeTownBlock(townBlock);
        this.deleteTownBlock(townBlock);
        BukkitTools.getPluginManager().callEvent((Event)new TownUnclaimEvent(town, coord));
    }
    
    @Override
    public void removeTownBlock(final TownBlock townBlock) {
        final TownPreUnclaimEvent event = new TownPreUnclaimEvent(townBlock);
        BukkitTools.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return;
        }
        Town town = null;
        try {
            town = townBlock.getTown();
        }
        catch (NotRegisteredException ex) {}
        final TownyWorld world = townBlock.getWorld();
        world.removeTownBlock(townBlock);
        this.saveWorld(world);
        this.deleteTownBlock(townBlock);
        this.saveTownBlockList();
        if (townBlock.getWorld().isUsingPlotManagementDelete()) {
            TownyRegenAPI.addDeleteTownBlockIdQueue(townBlock.getWorldCoord());
        }
        if (townBlock.getWorld().isUsingPlotManagementRevert()) {
            final PlotBlockData plotData = TownyRegenAPI.getPlotChunkSnapshot(townBlock);
            if (plotData != null && !plotData.getBlockList().isEmpty()) {
                TownyRegenAPI.addPlotChunk(plotData, true);
            }
        }
        BukkitTools.getPluginManager().callEvent((Event)new TownUnclaimEvent(town, townBlock.getWorldCoord()));
    }
    
    @Override
    public void removeTownBlocks(final Town town) {
        for (final TownBlock townBlock : new ArrayList<TownBlock>(town.getTownBlocks())) {
            this.removeTownBlock(townBlock);
        }
    }
    
    public void removeManyTownBlocks(final Town town) {
        for (final TownBlock townBlock : new ArrayList<TownBlock>(town.getTownBlocks())) {
            this.removeOneOfManyTownBlocks(townBlock, town);
        }
        this.saveTownBlockList();
    }
    
    @Override
    public List<TownBlock> getAllTownBlocks() {
        final List<TownBlock> townBlocks = new ArrayList<TownBlock>();
        for (final TownyWorld world : this.getWorlds()) {
            townBlocks.addAll(world.getTownBlocks());
        }
        return townBlocks;
    }
    
    @Override
    public List<PlotObjectGroup> getAllPlotGroups() {
        final List<PlotObjectGroup> groups = new ArrayList<PlotObjectGroup>();
        groups.addAll(this.universe.getGroups());
        return groups;
    }
    
    public void newPlotGroup(final PlotObjectGroup group) {
        this.universe.getGroups().add(group);
    }
    
    @Override
    public void newResident(final String name) throws AlreadyRegisteredException, NotRegisteredException {
        String filteredName;
        try {
            filteredName = NameValidation.checkAndFilterPlayerName(name);
        }
        catch (InvalidNameException e) {
            throw new NotRegisteredException(e.getMessage());
        }
        if (this.universe.getResidentMap().containsKey(filteredName.toLowerCase())) {
            throw new AlreadyRegisteredException("A resident with the name " + filteredName + " is already in use.");
        }
        this.universe.getResidentMap().put(filteredName.toLowerCase(), new Resident(filteredName));
    }
    
    @Override
    public void newTown(final String name) throws AlreadyRegisteredException, NotRegisteredException {
        this.lock.lock();
        try {
            String filteredName;
            try {
                filteredName = NameValidation.checkAndFilterName(name);
            }
            catch (InvalidNameException e) {
                throw new NotRegisteredException(e.getMessage());
            }
            if (this.universe.getTownsMap().containsKey(filteredName.toLowerCase())) {
                throw new AlreadyRegisteredException("The town " + filteredName + " is already in use.");
            }
            this.universe.getTownsMap().put(filteredName.toLowerCase(), new Town(filteredName));
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void newNation(final String name) throws AlreadyRegisteredException, NotRegisteredException {
        this.lock.lock();
        try {
            String filteredName;
            try {
                filteredName = NameValidation.checkAndFilterName(name);
            }
            catch (InvalidNameException e) {
                throw new NotRegisteredException(e.getMessage());
            }
            if (this.universe.getNationsMap().containsKey(filteredName.toLowerCase())) {
                throw new AlreadyRegisteredException("The nation " + filteredName + " is already in use.");
            }
            this.universe.getNationsMap().put(filteredName.toLowerCase(), new Nation(filteredName));
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void newWorld(final String name) throws AlreadyRegisteredException {
        if (this.universe.getWorldMap().containsKey(name.toLowerCase())) {
            throw new AlreadyRegisteredException("The world " + name + " is already in use.");
        }
        this.universe.getWorldMap().put(name.toLowerCase(), new TownyWorld(name));
    }
    
    @Override
    public void removeResidentList(final Resident resident) {
        final String name = resident.getName();
        final List<Resident> toSave = new ArrayList<Resident>();
        for (final Resident toCheck : new ArrayList<Resident>(this.universe.getResidentMap().values())) {
            TownyMessaging.sendDebugMsg("Checking friends of: " + toCheck.getName());
            if (toCheck.hasFriend(resident)) {
                try {
                    TownyMessaging.sendDebugMsg("       - Removing Friend: " + resident.getName());
                    toCheck.removeFriend(resident);
                    toSave.add(toCheck);
                }
                catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
            }
        }
        for (final Resident toCheck : toSave) {
            this.saveResident(toCheck);
        }
        try {
            resident.clear();
        }
        catch (EmptyTownException ex) {
            this.removeTown(ex.getTown());
        }
        this.deleteResident(resident);
        this.universe.getResidentMap().remove(name.toLowerCase());
        if (TownySettings.isUsingEconomy() && TownySettings.isDeleteEcoAccount()) {
            resident.getAccount().removeAccount();
        }
        this.plugin.deleteCache(name);
        this.saveResidentList();
    }
    
    @Override
    public void removeTown(final Town town) {
        final boolean delayFullRemoval = TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegeDelayFullTownRemoval();
        this.removeTown(town, delayFullRemoval);
    }
    
    @Override
    public void removeTown(final Town town, final boolean delayFullRemoval) {
        final PreDeleteTownEvent preEvent = new PreDeleteTownEvent(town);
        BukkitTools.getPluginManager().callEvent((Event)preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        if (delayFullRemoval) {
            town.setPublic(false);
            town.setOpen(false);
            for (final String element : new String[] { "residentBuild", "residentDestroy", "residentSwitch", "residentItemUse", "outsiderBuild", "outsiderDestroy", "outsiderSwitch", "outsiderItemUse", "allyBuild", "allyDestroy", "allySwitch", "allyItemUse", "nationBuild", "nationDestroy", "nationSwitch", "nationItemUse", "pvp", "fire", "explosion", "mobs" }) {
                town.getPermissions().set(element, true);
            }
            for (final TownBlock townBlock : town.getTownBlocks()) {
                townBlock.setType(townBlock.getType());
                this.saveTownBlock(townBlock);
            }
        }
        else {
            this.removeManyTownBlocks(town);
        }
        if (town.hasSiege()) {
            this.removeSiege(town.getSiege());
        }
        final List<Resident> toSave = new ArrayList<Resident>(town.getResidents());
        final TownyWorld townyWorld = town.getWorld();
        try {
            if (town.hasNation()) {
                final Nation nation = town.getNation();
                if (nation.hasTown(town)) {
                    nation.removeTown(town);
                    this.saveNation(nation);
                }
                town.setNation(null);
            }
            town.clear();
        }
        catch (EmptyNationException e) {
            this.removeNation(e.getNation());
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_del_nation"), e.getNation()));
        }
        catch (NotRegisteredException e2) {
            e2.printStackTrace();
        }
        catch (AlreadyRegisteredException ex) {}
        for (final Resident resident : toSave) {
            resident.clearModes();
            try {
                town.removeResident(resident);
            }
            catch (NotRegisteredException ex2) {}
            catch (EmptyTownException ex3) {}
            this.saveResident(resident);
        }
        for (final Resident jailedRes : TownyUniverse.getInstance().getJailedResidentMap()) {
            if (jailedRes.hasJailTown(town.getName())) {
                jailedRes.setJailed(jailedRes, 0, town);
                this.saveResident(jailedRes);
            }
        }
        if (TownyEconomyHandler.isActive()) {
            try {
                town.getAccount().payTo(town.getAccount().getHoldingBalance(), new WarSpoils(), "Remove Town");
                town.getAccount().removeAccount();
            }
            catch (Exception ex4) {}
        }
        if (delayFullRemoval) {
            this.saveTown(town);
            this.plugin.resetCache();
        }
        else {
            try {
                townyWorld.removeTown(town);
            }
            catch (NotRegisteredException ex5) {}
            this.saveWorld(townyWorld);
            this.universe.getTownsMap().remove(town.getName().toLowerCase());
            this.plugin.resetCache();
            this.deleteTown(town);
            this.saveTownList();
        }
        BukkitTools.getPluginManager().callEvent((Event)new DeleteTownEvent(town.getName()));
    }
    
    @Override
    public void removeNation(final Nation nation) {
        final PreDeleteNationEvent preEvent = new PreDeleteNationEvent(nation.getName());
        BukkitTools.getPluginManager().callEvent((Event)preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final List<Nation> toSaveNation = new ArrayList<Nation>();
        for (final Nation toCheck : new ArrayList<Nation>(this.universe.getNationsMap().values())) {
            if (!toCheck.hasAlly(nation)) {
                if (!toCheck.hasEnemy(nation)) {
                    continue;
                }
            }
            try {
                if (toCheck.hasAlly(nation)) {
                    toCheck.removeAlly(nation);
                }
                else {
                    toCheck.removeEnemy(nation);
                }
                toSaveNation.add(toCheck);
            }
            catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
        for (final Nation toCheck : toSaveNation) {
            this.saveNation(toCheck);
        }
        if (TownyEconomyHandler.isActive()) {
            try {
                nation.getAccount().payTo(nation.getAccount().getHoldingBalance(), new WarSpoils(), "Remove Nation");
                nation.getAccount().removeAccount();
            }
            catch (Exception ex) {}
        }
        if (TownySettings.getWarSiegeEnabled() && TownySettings.isUsingEconomy() && TownySettings.getWarSiegeRefundInitialNationCostOnDelete()) {
            try {
                final double amountToRefund = (double)Math.round(TownySettings.getNewNationPrice() * 0.01 * TownySettings.getWarSiegeNationCostRefundPercentageOnDelete());
                nation.getKing().getAccount().collect(amountToRefund, "Refund of Some of the Initial Nation Cost");
            }
            catch (EconomyException e2) {
                e2.printStackTrace();
            }
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_siege_war_refund_initial_cost_on_nation_delete"), TownyFormatter.getFormattedResidentName(nation.getKing()), TownySettings.getWarSiegeNationCostRefundPercentageOnDelete() + "%"));
        }
        this.deleteNation(nation);
        final List<Town> toSave = new ArrayList<Town>(nation.getTowns());
        final List<SiegeZone> siegeZonesToDelete = new ArrayList<SiegeZone>(nation.getSiegeZones());
        for (final SiegeZone siegeZone : siegeZonesToDelete) {
            final Siege siege = siegeZone.getSiege();
            siege.getSiegeZones().remove(nation);
            toSave.add(siegeZone.getDefendingTown());
            if (siege.getSiegeZones().size() == 0) {
                siege.getDefendingTown().setSiege(null);
                siege.setActualEndTime(System.currentTimeMillis());
                SiegeWarTimeUtil.activateSiegeImmunityTimer(siege.getDefendingTown(), siege);
            }
            this.deleteSiegeZone(siegeZone);
        }
        nation.clear();
        this.universe.getNationsMap().remove(nation.getName().toLowerCase());
        for (final SiegeZone siegeZone : siegeZonesToDelete) {
            this.universe.getSiegeZonesMap().remove(siegeZone.getName().toLowerCase());
        }
        for (final Town town : toSave) {
            final List<Resident> titleRemove = new ArrayList<Resident>(town.getResidents());
            for (final Resident res : titleRemove) {
                if (res.hasTitle() || res.hasSurname()) {
                    res.setTitle("");
                    res.setSurname("");
                    this.saveResident(res);
                }
            }
            this.saveTown(town);
        }
        this.plugin.resetCache();
        this.saveNationList();
        if (siegeZonesToDelete.size() > 0) {
            this.saveSiegeZoneList();
        }
        BukkitTools.getPluginManager().callEvent((Event)new DeleteNationEvent(nation.getName()));
    }
    
    @Override
    public void removeWorld(final TownyWorld world) throws UnsupportedOperationException {
        this.deleteWorld(world);
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Set<String> getResidentKeys() {
        return this.universe.getResidentMap().keySet();
    }
    
    @Override
    public Set<String> getTownsKeys() {
        return this.universe.getTownsMap().keySet();
    }
    
    @Override
    public Set<String> getNationsKeys() {
        return this.universe.getNationsMap().keySet();
    }
    
    @Override
    public List<Town> getTownsWithoutNation() {
        final List<Town> townFilter = new ArrayList<Town>();
        for (final Town town : this.getTowns()) {
            if (!town.hasNation()) {
                townFilter.add(town);
            }
        }
        return townFilter;
    }
    
    @Override
    public List<Resident> getResidentsWithoutTown() {
        final List<Resident> residentFilter = new ArrayList<Resident>();
        for (final Resident resident : this.universe.getResidentMap().values()) {
            if (!resident.hasTown()) {
                residentFilter.add(resident);
            }
        }
        return residentFilter;
    }
    
    @Override
    public void renameTown(final Town town, final String newName) throws AlreadyRegisteredException, NotRegisteredException {
        this.lock.lock();
        String oldName;
        try {
            String filteredName;
            try {
                filteredName = NameValidation.checkAndFilterName(newName);
            }
            catch (InvalidNameException e) {
                throw new NotRegisteredException(e.getMessage());
            }
            if (this.hasTown(filteredName)) {
                throw new AlreadyRegisteredException("The town " + filteredName + " is already in use.");
            }
            final List<Resident> toSave = new ArrayList<Resident>(town.getResidents());
            boolean isCapital = false;
            Nation nation = null;
            double townBalance = 0.0;
            oldName = town.getName();
            if (TownySettings.isUsingEconomy()) {
                try {
                    townBalance = town.getAccount().getHoldingBalance();
                    if (TownySettings.isEcoClosedEconomyEnabled()) {
                        town.getAccount().pay(townBalance, "Town Rename");
                    }
                    town.getAccount().removeAccount();
                }
                catch (EconomyException ex) {}
            }
            final UUID oldUUID = town.getUuid();
            final long oldregistration = town.getRegistered();
            if (town.hasNation()) {
                nation = town.getNation();
                isCapital = town.isCapital();
            }
            final TownyWorld world = town.getWorld();
            world.removeTown(town);
            this.deleteTown(town);
            if (town.hasSiege()) {
                for (final SiegeZone siegeZone : new ArrayList<SiegeZone>(town.getSiege().getSiegeZones().values())) {
                    this.deleteSiegeZone(siegeZone);
                }
            }
            this.universe.getTownsMap().remove(town.getName().toLowerCase());
            town.setName(filteredName);
            this.universe.getTownsMap().put(filteredName.toLowerCase(), town);
            world.addTown(town);
            if (town.hasSiege()) {
                for (final SiegeZone siegeZone2 : town.getSiege().getSiegeZones().values()) {
                    final String oldSiegeZoneName = SiegeZone.generateName(siegeZone2.getAttackingNation().getName(), oldName);
                    final String newSiegeZoneName = siegeZone2.getName();
                    this.universe.getSiegeZonesMap().remove(oldSiegeZoneName);
                    this.universe.getSiegeZonesMap().put(newSiegeZoneName.toLowerCase(), siegeZone2);
                }
            }
            if (isCapital) {
                nation.setCapital(town);
            }
            town.setUuid(oldUUID);
            town.setRegistered(oldregistration);
            if (TownySettings.isUsingEconomy()) {
                try {
                    town.getAccount().setBalance(townBalance, "Rename Town - Transfer to new account");
                }
                catch (EconomyException e2) {
                    e2.printStackTrace();
                }
            }
            for (final Resident resident : toSave) {
                this.saveResident(resident);
            }
            for (final Resident toCheck : this.getResidents()) {
                if (toCheck.hasJailTown(oldName)) {
                    toCheck.setJailTown(newName);
                    this.saveResident(toCheck);
                }
            }
            for (final TownBlock townBlock : town.getTownBlocks()) {
                this.saveTownBlock(townBlock);
            }
            if (town.hasObjectGroups()) {
                for (final PlotObjectGroup pg : town.getPlotObjectGroups()) {
                    pg.setTown(town);
                    this.savePlotGroup(pg);
                }
            }
            this.saveTown(town);
            for (final SiegeZone siegeZone : town.getSiege().getSiegeZones().values()) {
                this.saveSiegeZone(siegeZone);
                this.saveNation(siegeZone.getAttackingNation());
            }
            this.saveTownList();
            this.saveSiegeZoneList();
            this.savePlotGroupList();
            this.saveWorld(town.getWorld());
            if (nation != null) {
                this.saveNation(nation);
            }
        }
        finally {
            this.lock.unlock();
        }
        BukkitTools.getPluginManager().callEvent((Event)new RenameTownEvent(oldName, town));
    }
    
    @Override
    public void renameNation(final Nation nation, final String newName) throws AlreadyRegisteredException, NotRegisteredException {
        this.lock.lock();
        String oldName;
        try {
            String filteredName;
            try {
                filteredName = NameValidation.checkAndFilterName(newName);
            }
            catch (InvalidNameException e) {
                throw new NotRegisteredException(e.getMessage());
            }
            if (this.hasNation(filteredName)) {
                throw new AlreadyRegisteredException("The nation " + filteredName + " is already in use.");
            }
            final List<Town> toSave = new ArrayList<Town>(nation.getTowns());
            double nationBalance = 0.0;
            if (TownySettings.isUsingEconomy()) {
                try {
                    nationBalance = nation.getAccount().getHoldingBalance();
                    if (TownySettings.isEcoClosedEconomyEnabled()) {
                        nation.getAccount().pay(nationBalance, "Nation Rename");
                    }
                    nation.getAccount().removeAccount();
                }
                catch (EconomyException ex) {}
            }
            final UUID oldUUID = nation.getUuid();
            final long oldregistration = nation.getRegistered();
            this.deleteNation(nation);
            for (final SiegeZone siegeZone : new ArrayList<SiegeZone>(nation.getSiegeZones())) {
                this.deleteSiegeZone(siegeZone);
            }
            oldName = nation.getName();
            this.universe.getNationsMap().remove(oldName.toLowerCase());
            nation.setName(filteredName);
            this.universe.getNationsMap().put(filteredName.toLowerCase(), nation);
            for (final SiegeZone siegeZone2 : nation.getSiegeZones()) {
                final String oldSiegeZoneName = SiegeZone.generateName(oldName, siegeZone2.getDefendingTown().getName());
                final String newSiegeZoneName = siegeZone2.getName();
                this.universe.getSiegeZonesMap().remove(oldSiegeZoneName);
                this.universe.getSiegeZonesMap().put(newSiegeZoneName.toLowerCase(), siegeZone2);
            }
            if (TownyEconomyHandler.isActive()) {
                try {
                    nation.getAccount().setBalance(nationBalance, "Rename Nation - Transfer to new account");
                }
                catch (EconomyException e2) {
                    e2.printStackTrace();
                }
            }
            nation.setUuid(oldUUID);
            nation.setRegistered(oldregistration);
            for (final Town town : toSave) {
                this.saveTown(town);
            }
            this.saveNation(nation);
            for (final SiegeZone siegeZone2 : nation.getSiegeZones()) {
                this.saveSiegeZone(siegeZone2);
                this.saveTown(siegeZone2.getDefendingTown());
            }
            this.saveNationList();
            this.saveSiegeZoneList();
            final Nation oldNation = new Nation(oldName);
            final List<Nation> toSaveNation = new ArrayList<Nation>(this.getNations());
            for (final Nation toCheck : toSaveNation) {
                if (!toCheck.hasAlly(oldNation)) {
                    if (!toCheck.hasEnemy(oldNation)) {
                        toSave.remove(toCheck);
                        continue;
                    }
                }
                try {
                    if (toCheck.hasAlly(oldNation)) {
                        toCheck.removeAlly(oldNation);
                        toCheck.addAlly(nation);
                    }
                    else {
                        toCheck.removeEnemy(oldNation);
                        toCheck.addEnemy(nation);
                    }
                }
                catch (NotRegisteredException e3) {
                    e3.printStackTrace();
                }
            }
            for (final Nation toCheck : toSaveNation) {
                this.saveNation(toCheck);
            }
        }
        finally {
            this.lock.unlock();
        }
        BukkitTools.getPluginManager().callEvent((Event)new RenameNationEvent(oldName, nation));
    }
    
    @Override
    public void renameGroup(final PlotObjectGroup group, final String newName) throws AlreadyRegisteredException {
        group.setGroupName(newName);
        this.savePlotGroup(group);
        this.savePlotGroupList();
        this.deletePlotGroup(group);
    }
    
    @Override
    public void renamePlayer(final Resident resident, final String newName) throws AlreadyRegisteredException, NotRegisteredException {
        this.lock.lock();
        final String oldName = resident.getName();
        try {
            double balance = 0.0;
            Town town = null;
            if (TownyEconomyHandler.getVersion().startsWith("iConomy 5") && TownySettings.isUsingEconomy()) {
                try {
                    balance = resident.getAccount().getHoldingBalance();
                    resident.getAccount().removeAccount();
                }
                catch (EconomyException ex) {}
            }
            final List<Resident> friends = resident.getFriends();
            final List<String> nationRanks = resident.getNationRanks();
            final TownyPermission permissions = resident.getPermissions();
            final String surname = resident.getSurname();
            final String title = resident.getTitle();
            if (resident.hasTown()) {
                town = resident.getTown();
            }
            final List<TownBlock> townBlocks = resident.getTownBlocks();
            final List<String> townRanks = resident.getTownRanks();
            final long registered = resident.getRegistered();
            final long lastOnline = resident.getLastOnline();
            final boolean isMayor = resident.isMayor();
            final boolean isNPC = resident.isNPC();
            final boolean isJailed = resident.isJailed();
            final int JailSpawn = resident.getJailSpawn();
            if (resident.isJailed()) {
                try {
                    this.universe.getJailedResidentMap().remove(this.universe.getDataSource().getResident(oldName));
                    this.universe.getJailedResidentMap().add(this.universe.getDataSource().getResident(newName));
                }
                catch (Exception ex2) {}
            }
            this.deleteResident(resident);
            this.universe.getResidentMap().remove(oldName.toLowerCase());
            resident.setName(newName);
            this.universe.getResidentMap().put(newName.toLowerCase(), resident);
            if (TownyEconomyHandler.getVersion().startsWith("iConomy 5") && TownySettings.isUsingEconomy()) {
                try {
                    resident.getAccount().setBalance(balance, "Rename Player - Transfer to new account");
                }
                catch (EconomyException e) {
                    e.printStackTrace();
                }
            }
            resident.setFriends(friends);
            resident.setNationRanks(nationRanks);
            resident.setPermissions(permissions.toString());
            resident.setSurname(surname);
            resident.setTitle(title);
            resident.setTown(town);
            resident.setTownblocks(townBlocks);
            resident.setTownRanks(townRanks);
            resident.setRegistered(registered);
            resident.setLastOnline(lastOnline);
            if (isMayor) {
                try {
                    town.setMayor(resident);
                }
                catch (TownyException ex3) {}
            }
            if (isNPC) {
                resident.setNPC(true);
            }
            resident.setJailed(isJailed);
            resident.setJailSpawn(JailSpawn);
            this.saveResidentList();
            this.saveResident(resident);
            if (town != null) {
                this.saveTown(town);
            }
            for (final TownBlock tb : townBlocks) {
                this.saveTownBlock(tb);
            }
            final Resident oldResident = new Resident(oldName);
            final List<Resident> toSaveResident = new ArrayList<Resident>(this.getResidents());
            for (final Resident toCheck : toSaveResident) {
                if (toCheck.hasFriend(oldResident)) {
                    try {
                        toCheck.removeFriend(oldResident);
                        toCheck.addFriend(resident);
                    }
                    catch (NotRegisteredException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            for (final Resident toCheck : toSaveResident) {
                this.saveResident(toCheck);
            }
            final List<Town> toSaveTown = new ArrayList<Town>(this.getTowns());
            for (final Town toCheckTown : toSaveTown) {
                if (toCheckTown.hasOutlaw(oldResident)) {
                    try {
                        toCheckTown.removeOutlaw(oldResident);
                        toCheckTown.addOutlaw(resident);
                    }
                    catch (NotRegisteredException e3) {
                        e3.printStackTrace();
                    }
                }
            }
            for (final Town toCheckTown : toSaveTown) {
                this.saveTown(toCheckTown);
            }
        }
        finally {
            this.lock.unlock();
        }
        BukkitTools.getPluginManager().callEvent((Event)new RenameResidentEvent(oldName, resident));
    }
    
    @Override
    public void mergeNation(final Nation succumbingNation, final Nation prevailingNation) throws NotRegisteredException, AlreadyRegisteredException {
        this.lock.lock();
        final List<Town> towns = new ArrayList<Town>(succumbingNation.getTowns());
        Town lastTown = null;
        try {
            succumbingNation.getAccount().payTo(succumbingNation.getAccount().getHoldingBalance(), prevailingNation, "Nation merge bank accounts.");
            final Iterator<Town> iterator = towns.iterator();
            while (iterator.hasNext()) {
                final Town town = lastTown = iterator.next();
                for (final Resident res : town.getResidents()) {
                    if (res.hasTitle() || res.hasSurname()) {
                        res.setTitle("");
                        res.setSurname("");
                    }
                    res.updatePermsForNationRemoval();
                    this.saveResident(res);
                }
                succumbingNation.removeTown(town);
                prevailingNation.addTown(town);
                this.saveTown(town);
            }
        }
        catch (EconomyException ex) {}
        catch (EmptyNationException en) {
            prevailingNation.addTown(lastTown);
            this.saveTown(lastTown);
            final String name = en.getNation().getName();
            this.universe.getDataSource().removeNation(en.getNation());
            this.saveNation(prevailingNation);
            this.universe.getDataSource().saveNationList();
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_del_nation"), name));
            this.lock.unlock();
        }
    }
    
    @Override
    public List<SiegeZone> getSiegeZones() {
        return new ArrayList<SiegeZone>(this.universe.getSiegeZonesMap().values());
    }
    
    public void newSiegeZone(final String siegeZoneName) throws AlreadyRegisteredException {
        final String[] townAndNationArray = SiegeZone.generateTownAndNationName(siegeZoneName);
        this.newSiegeZone(townAndNationArray[0], townAndNationArray[1]);
    }
    
    @Override
    public void newSiegeZone(final String attackingNationName, final String defendingTownName) throws AlreadyRegisteredException {
        this.lock.lock();
        try {
            final String siegeZoneName = SiegeZone.generateName(attackingNationName, defendingTownName);
            if (this.universe.getSiegeZonesMap().containsKey(siegeZoneName.toLowerCase())) {
                throw new AlreadyRegisteredException("Siege Zone is already registered");
            }
            final Town town = this.universe.getTownsMap().get(defendingTownName.toLowerCase());
            final Nation nation = this.universe.getNationsMap().get(attackingNationName.toLowerCase());
            final SiegeZone siegeZone = new SiegeZone(nation, town);
            this.universe.getSiegeZonesMap().put(siegeZoneName.toLowerCase(), siegeZone);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public SiegeZone getSiegeZone(final String siegeZoneName) throws NotRegisteredException {
        if (!this.universe.getSiegeZonesMap().containsKey(siegeZoneName.toLowerCase())) {
            throw new NotRegisteredException("Siege Zone not found");
        }
        return this.universe.getSiegeZonesMap().get(siegeZoneName.toLowerCase());
    }
    
    @Override
    public void removeRuinedTown(final Town town) {
        this.removeManyTownBlocks(town);
        final TownyWorld townyWorld = town.getWorld();
        try {
            townyWorld.removeTown(town);
        }
        catch (NotRegisteredException ex) {}
        this.saveWorld(townyWorld);
        this.universe.getTownsMap().remove(town.getName().toLowerCase());
        this.plugin.resetCache();
        this.deleteTown(town);
        this.saveTownList();
    }
    
    @Override
    public void removeSiege(final Siege siege) {
        siege.getDefendingTown().setSiege(null);
        final List<SiegeZone> siegeZonesToRemove = new ArrayList<SiegeZone>();
        final List<Nation> nationsToSave = new ArrayList<Nation>();
        for (final Map.Entry<Nation, SiegeZone> entry : siege.getSiegeZones().entrySet()) {
            siegeZonesToRemove.add(entry.getValue());
            nationsToSave.add(entry.getKey());
        }
        for (final SiegeZone siegeZone : siegeZonesToRemove) {
            siegeZone.getAttackingNation().removeSiegeZone(siegeZone);
        }
        for (final SiegeZone siegeZone : siegeZonesToRemove) {
            this.universe.getSiegeZonesMap().remove(siegeZone.getName().toLowerCase());
        }
        this.saveTown(siege.getDefendingTown());
        for (final Nation nation : nationsToSave) {
            this.saveNation(nation);
        }
        for (final SiegeZone siegeZone : siegeZonesToRemove) {
            this.deleteSiegeZone(siegeZone);
        }
        this.saveSiegeZoneList();
    }
    
    @Override
    public void removeSiegeZone(final SiegeZone siegeZone) {
        siegeZone.getDefendingTown().getSiege().getSiegeZones().remove(siegeZone.getAttackingNation());
        siegeZone.getAttackingNation().removeSiegeZone(siegeZone);
        this.universe.getSiegeZonesMap().remove(siegeZone.getName().toLowerCase());
        this.saveTown(siegeZone.getDefendingTown());
        this.saveNation(siegeZone.getAttackingNation());
        this.deleteSiegeZone(siegeZone);
        this.saveSiegeZoneList();
    }
    
    @Override
    public Set<String> getSiegeZonesKeys() {
        return this.universe.getSiegeZonesMap().keySet();
    }
}
