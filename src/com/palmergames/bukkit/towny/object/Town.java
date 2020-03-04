package com.palmergames.bukkit.towny.object;

import org.bukkit.World;
import com.palmergames.util.StringMgmt;
import com.palmergames.util.TimeMgmt;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.invites.TownyInviteSender;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.invites.TownyInviteReceiver;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import java.util.Collections;
import java.util.Arrays;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import java.util.Collection;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarMembershipController;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.TownTagChangeEvent;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.invites.Invite;
import java.util.UUID;
import java.util.HashMap;
import org.bukkit.Location;
import java.util.List;

public class Town extends TownyObject implements ResidentList, TownyInviter, ObjectGroupManageable<PlotObjectGroup>, EconomyHandler, TownBlockOwner
{
    private static final String ECONOMY_ACCOUNT_PREFIX;
    private List<Resident> residents;
    private List<Resident> outlaws;
    private List<Location> outpostSpawns;
    private List<Location> jailSpawns;
    private HashMap<String, PlotObjectGroup> plotGroups;
    private Resident mayor;
    private int bonusBlocks;
    private int purchasedBlocks;
    private double taxes;
    private double plotTax;
    private double commercialPlotTax;
    private double plotPrice;
    private double embassyPlotTax;
    private double commercialPlotPrice;
    private double embassyPlotPrice;
    private double spawnCost;
    private Nation nation;
    private boolean hasUpkeep;
    private boolean isPublic;
    private boolean isTaxPercentage;
    private boolean isOpen;
    private String townBoard;
    private String tag;
    private TownBlock homeBlock;
    private TownyWorld world;
    private Location spawn;
    private boolean adminDisabledPVP;
    private boolean adminEnabledPVP;
    private UUID uuid;
    private long registered;
    private transient List<Invite> receivedinvites;
    private transient List<Invite> sentinvites;
    private boolean isConquered;
    private int conqueredDays;
    private EconomyAccount account;
    private List<TownBlock> townBlocks;
    private TownyPermission permissions;
    private long recentlyRuinedEndTime;
    private long revoltImmunityEndTime;
    private long siegeImmunityEndTime;
    private Siege siege;
    private boolean occupied;
    private boolean neutral;
    private boolean desiredNeutralityValue;
    private int neutralityChangeConfirmationCounterDays;
    
    public Town(final String name) {
        super(name);
        this.residents = new ArrayList<Resident>();
        this.outlaws = new ArrayList<Resident>();
        this.outpostSpawns = new ArrayList<Location>();
        this.jailSpawns = new ArrayList<Location>();
        this.plotGroups = null;
        this.bonusBlocks = 0;
        this.purchasedBlocks = 0;
        this.taxes = TownySettings.getTownDefaultTax();
        this.plotTax = TownySettings.getTownDefaultPlotTax();
        this.commercialPlotTax = TownySettings.getTownDefaultShopTax();
        this.plotPrice = 0.0;
        this.embassyPlotTax = TownySettings.getTownDefaultEmbassyTax();
        this.hasUpkeep = true;
        this.isPublic = TownySettings.getTownDefaultPublic();
        this.isTaxPercentage = TownySettings.getTownDefaultTaxPercentage();
        this.isOpen = TownySettings.getTownDefaultOpen();
        this.townBoard = "/town set board [msg]";
        this.tag = "";
        this.adminDisabledPVP = false;
        this.adminEnabledPVP = false;
        this.receivedinvites = new ArrayList<Invite>();
        this.sentinvites = new ArrayList<Invite>();
        this.isConquered = false;
        this.townBlocks = new ArrayList<TownBlock>();
        (this.permissions = new TownyPermission()).loadDefault(this);
        this.recentlyRuinedEndTime = 0L;
        this.revoltImmunityEndTime = 0L;
        this.siegeImmunityEndTime = System.currentTimeMillis() + (long)(TownySettings.getWarSiegeSiegeImmunityTimeNewTownsHours() * 3600000.0);
        this.siege = null;
        this.occupied = false;
        this.neutral = false;
        this.desiredNeutralityValue = false;
        this.neutralityChangeConfirmationCounterDays = 0;
    }
    
    @Override
    public void setTownblocks(final List<TownBlock> townblocks) {
        this.townBlocks = townblocks;
    }
    
    @Override
    public List<TownBlock> getTownBlocks() {
        return this.townBlocks;
    }
    
    @Override
    public boolean hasTownBlock(final TownBlock townBlock) {
        return this.townBlocks.contains(townBlock);
    }
    
    @Override
    public void addTownBlock(final TownBlock townBlock) throws AlreadyRegisteredException {
        if (this.hasTownBlock(townBlock)) {
            throw new AlreadyRegisteredException();
        }
        this.townBlocks.add(townBlock);
        if (this.townBlocks.size() == 1 && !this.hasHomeBlock()) {
            try {
                this.setHomeBlock(townBlock);
            }
            catch (TownyException ex) {}
        }
    }
    
    public void setTag(final String text) throws TownyException {
        if (text.length() > 4) {
            throw new TownyException(TownySettings.getLangString("msg_err_tag_too_long"));
        }
        this.tag = text.toUpperCase();
        if (this.tag.matches(" ")) {
            this.tag = "";
        }
        Bukkit.getPluginManager().callEvent((Event)new TownTagChangeEvent(this.tag, this));
    }
    
    public String getTag() {
        return this.tag;
    }
    
    public boolean hasTag() {
        return !this.tag.isEmpty();
    }
    
    public Resident getMayor() {
        return this.mayor;
    }
    
    public void setTaxes(final double taxes) {
        if (this.isTaxPercentage) {
            if (taxes > TownySettings.getMaxTaxPercent()) {
                this.taxes = TownySettings.getMaxTaxPercent();
            }
            else {
                this.taxes = taxes;
            }
        }
        else if (taxes > TownySettings.getMaxTax()) {
            this.taxes = TownySettings.getMaxTax();
        }
        else {
            this.taxes = taxes;
        }
    }
    
    public double getTaxes() {
        this.setTaxes(this.taxes);
        return this.taxes;
    }
    
    public void setMayor(final Resident mayor) throws TownyException {
        if (!this.hasResident(mayor)) {
            throw new TownyException(TownySettings.getLangString("msg_err_mayor_doesnt_belong_to_town"));
        }
        TownyPerms.assignPermissions(this.mayor = mayor, null);
    }
    
    public Nation getNation() throws NotRegisteredException {
        if (this.hasNation()) {
            return this.nation;
        }
        throw new NotRegisteredException(TownySettings.getLangString("msg_err_town_doesnt_belong_to_any_nation"));
    }
    
    public void setNation(final Nation nation) throws AlreadyRegisteredException {
        if (nation == null) {
            this.nation = null;
            TownyPerms.updateTownPerms(this);
            return;
        }
        if (this.nation == nation) {
            return;
        }
        if (this.hasNation()) {
            throw new AlreadyRegisteredException();
        }
        this.nation = nation;
        TownyPerms.updateTownPerms(this);
    }
    
    @Override
    public List<Resident> getResidents() {
        return this.residents;
    }
    
    public List<Resident> getAssistants() {
        final List<Resident> assistants = new ArrayList<Resident>();
        for (final Resident assistant : this.residents) {
            if (assistant.hasTownRank("assistant")) {
                assistants.add(assistant);
            }
        }
        return assistants;
    }
    
    @Override
    public boolean hasResident(final String name) {
        for (final Resident resident : this.residents) {
            if (resident.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasResident(final Resident resident) {
        return this.residents.contains(resident);
    }
    
    public boolean hasAssistant(final Resident resident) {
        return this.getAssistants().contains(resident);
    }
    
    public void addResident(final Resident resident) throws AlreadyRegisteredException {
        this.addResidentCheck(resident);
        this.residents.add(resident);
        resident.setTown(this);
        BukkitTools.getPluginManager().callEvent((Event)new TownAddResidentEvent(resident, this));
    }
    
    public void addResidentCheck(final Resident resident) throws AlreadyRegisteredException {
        if (this.hasResident(resident)) {
            throw new AlreadyRegisteredException(String.format(TownySettings.getLangString("msg_err_already_in_town"), resident.getName(), this.getFormattedName()));
        }
        if (resident.hasTown()) {
            try {
                if (!resident.getTown().equals(this)) {
                    throw new AlreadyRegisteredException(String.format(TownySettings.getLangString("msg_err_already_in_town"), resident.getName(), resident.getTown().getFormattedName()));
                }
            }
            catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean isMayor(final Resident resident) {
        return resident == this.mayor;
    }
    
    public boolean hasNation() {
        return this.nation != null;
    }
    
    public int getNumResidents() {
        return this.residents.size();
    }
    
    public boolean isCapital() {
        return this.hasNation() && this.nation.isCapital(this);
    }
    
    public void setHasUpkeep(final boolean hasUpkeep) {
        this.hasUpkeep = hasUpkeep;
    }
    
    public boolean hasUpkeep() {
        return this.hasUpkeep;
    }
    
    public void setHasMobs(final boolean hasMobs) {
        this.permissions.mobs = hasMobs;
    }
    
    public boolean hasMobs() {
        return this.permissions.mobs;
    }
    
    public void setPVP(final boolean isPVP) {
        this.permissions.pvp = isPVP;
    }
    
    public void setAdminDisabledPVP(final boolean isPVPDisabled) {
        this.adminDisabledPVP = isPVPDisabled;
    }
    
    public void setAdminEnabledPVP(final boolean isPVPEnabled) {
        this.adminEnabledPVP = isPVPEnabled;
    }
    
    public boolean isPVP() {
        return this.isAdminEnabledPVP() || (!this.isAdminDisabledPVP() && ((TownySettings.getWarSiegeEnabled() && TownySettings.getWarSiegePvpAlwaysOnInBesiegedTowns() && (!TownySettings.getWarSiegeTownNeutralityEnabled() || !this.isNeutral()) && this.siege != null && this.siege.getStatus() == SiegeStatus.IN_PROGRESS) || this.permissions.pvp));
    }
    
    public boolean isAdminDisabledPVP() {
        return this.adminDisabledPVP;
    }
    
    public boolean isAdminEnabledPVP() {
        return this.adminEnabledPVP;
    }
    
    public void setBANG(final boolean isBANG) {
        this.permissions.explosion = isBANG;
    }
    
    public boolean isBANG() {
        return this.permissions.explosion;
    }
    
    public void setTaxPercentage(final boolean isPercentage) {
        this.isTaxPercentage = isPercentage;
        if (this.getTaxes() > 100.0) {
            this.setTaxes(0.0);
        }
    }
    
    public boolean isTaxPercentage() {
        return this.isTaxPercentage;
    }
    
    public void setFire(final boolean isFire) {
        this.permissions.fire = isFire;
    }
    
    public boolean isFire() {
        return this.permissions.fire;
    }
    
    public void setTownBoard(final String townBoard) {
        this.townBoard = townBoard;
    }
    
    public String getTownBoard() {
        return this.townBoard;
    }
    
    public void setBonusBlocks(final int bonusBlocks) {
        this.bonusBlocks = bonusBlocks;
    }
    
    public int getTotalBlocks() {
        return TownySettings.getMaxTownBlocks(this);
    }
    
    public int getBonusBlocks() {
        return this.bonusBlocks;
    }
    
    public double getBonusBlockCost() {
        final double nextprice = Math.pow(TownySettings.getPurchasedBonusBlocksIncreaseValue(), this.getPurchasedBlocks()) * TownySettings.getPurchasedBonusBlocksCost();
        return nextprice;
    }
    
    public double getTownBlockCost() {
        final double nextprice = Math.pow(TownySettings.getClaimPriceIncreaseValue(), this.getTownBlocks().size()) * TownySettings.getClaimPrice();
        return nextprice;
    }
    
    public double getTownBlockCostN(final int inputN) throws TownyException {
        if (inputN < 0) {
            throw new TownyException(TownySettings.getLangString("msg_err_negative"));
        }
        final int n = inputN;
        if (n == 0) {
            return n;
        }
        double nextprice = this.getTownBlockCost();
        int i = 1;
        double cost = nextprice;
        while (i < n) {
            nextprice = (double)Math.round(Math.pow(TownySettings.getClaimPriceIncreaseValue(), this.getTownBlocks().size() + i) * TownySettings.getClaimPrice());
            cost += nextprice;
            ++i;
        }
        cost = (double)Math.round(cost);
        return cost;
    }
    
    public double getBonusBlockCostN(final int inputN) throws TownyException {
        if (inputN < 0) {
            throw new TownyException(TownySettings.getLangString("msg_err_negative"));
        }
        final int current = this.getPurchasedBlocks();
        int n;
        if (current + inputN > TownySettings.getMaxPurchedBlocks(this)) {
            n = TownySettings.getMaxPurchedBlocks(this) - current;
        }
        else {
            n = inputN;
        }
        if (n == 0) {
            return n;
        }
        double nextprice = this.getBonusBlockCost();
        int i = 1;
        double cost = nextprice;
        while (i < n) {
            nextprice = (double)Math.round(Math.pow(TownySettings.getPurchasedBonusBlocksIncreaseValue(), this.getPurchasedBlocks() + i) * TownySettings.getPurchasedBonusBlocksCost());
            cost += nextprice;
            ++i;
        }
        cost = (double)Math.round(cost);
        return cost;
    }
    
    public void addBonusBlocks(final int bonusBlocks) {
        this.bonusBlocks += bonusBlocks;
    }
    
    public void setPurchasedBlocks(final int purchasedBlocks) {
        this.purchasedBlocks = purchasedBlocks;
    }
    
    public int getPurchasedBlocks() {
        return this.purchasedBlocks;
    }
    
    public void addPurchasedBlocks(final int purchasedBlocks) {
        this.purchasedBlocks += purchasedBlocks;
    }
    
    public boolean setHomeBlock(final TownBlock homeBlock) throws TownyException {
        if (homeBlock == null) {
            this.homeBlock = null;
            return false;
        }
        if (!this.hasTownBlock(homeBlock)) {
            throw new TownyException(TownySettings.getLangString("msg_err_town_has_no_claim_over_this_town_block"));
        }
        this.homeBlock = homeBlock;
        if (this.world != homeBlock.getWorld()) {
            if (this.world != null && this.world.hasTown(this)) {
                this.world.removeTown(this);
            }
            this.setWorld(homeBlock.getWorld());
        }
        try {
            this.setSpawn(this.spawn);
        }
        catch (TownyException e3) {
            this.spawn = null;
        }
        catch (NullPointerException ex) {}
        if (this.hasNation() && TownySettings.getNationRequiresProximity() > 0.0 && !this.getNation().getCapital().equals(this)) {
            final Nation nation = this.getNation();
            final Coord capitalCoord = nation.getCapital().getHomeBlock().getCoord();
            final Coord townCoord = this.getHomeBlock().getCoord();
            if (!nation.getCapital().getHomeBlock().getWorld().getName().equals(this.getHomeBlock().getWorld().getName())) {
                TownyMessaging.sendNationMessagePrefixed(nation, String.format(TownySettings.getLangString("msg_nation_town_moved_their_homeblock_too_far"), this.getName()));
                try {
                    nation.removeTown(this);
                }
                catch (EmptyNationException e) {
                    e.printStackTrace();
                }
            }
            final double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2.0) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2.0));
            if (distance > TownySettings.getNationRequiresProximity()) {
                TownyMessaging.sendNationMessagePrefixed(nation, String.format(TownySettings.getLangString("msg_nation_town_moved_their_homeblock_too_far"), this.getName()));
                try {
                    nation.removeTown(this);
                }
                catch (EmptyNationException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return true;
    }
    
    public void forceSetHomeBlock(final TownBlock homeBlock) throws TownyException {
        if (homeBlock == null) {
            this.homeBlock = null;
            return;
        }
        this.homeBlock = homeBlock;
        if (this.world != homeBlock.getWorld()) {
            if (this.world != null && this.world.hasTown(this)) {
                this.world.removeTown(this);
            }
            this.setWorld(homeBlock.getWorld());
        }
    }
    
    public TownBlock getHomeBlock() throws TownyException {
        if (this.hasHomeBlock()) {
            return this.homeBlock;
        }
        throw new TownyException("Town has not set a home block.");
    }
    
    public void setWorld(final TownyWorld world) {
        if (world == null) {
            this.world = null;
            return;
        }
        if (this.world == world) {
            return;
        }
        if (this.hasWorld()) {
            try {
                world.removeTown(this);
            }
            catch (NotRegisteredException ex) {}
        }
        this.world = world;
        try {
            this.world.addTown(this);
        }
        catch (AlreadyRegisteredException ex2) {}
    }
    
    public TownyWorld getWorld() {
        if (this.world != null) {
            return this.world;
        }
        return TownyUniverse.getInstance().getDataSource().getTownWorld(this.getName());
    }
    
    public boolean hasMayor() {
        return this.mayor != null;
    }
    
    public void removeResident(final Resident resident) throws EmptyTownException, NotRegisteredException {
        if (!this.hasResident(resident)) {
            throw new NotRegisteredException();
        }
        if (TownySettings.getWarSiegeEnabled()) {
            SiegeWarMembershipController.evaluateTownRemoveResident(resident);
        }
        this.remove(resident);
        if (this.getNumResidents() == 0) {
            throw new EmptyTownException(this);
        }
    }
    
    private void removeAllResidents() {
        for (final Resident resident : new ArrayList<Resident>(this.residents)) {
            this.remove(resident);
        }
    }
    
    private void remove(final Resident resident) {
        resident.setTitle("");
        resident.setSurname("");
        resident.updatePerms();
        for (final TownBlock townBlock : new ArrayList<TownBlock>(resident.getTownBlocks())) {
            if (townBlock.getType() != TownBlockType.EMBASSY) {
                townBlock.setResident(null);
                try {
                    townBlock.setPlotPrice(townBlock.getTown().getPlotPrice());
                }
                catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
                TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
                townBlock.setType(townBlock.getType());
            }
        }
        if (this.isMayor(resident) && this.residents.size() > 1) {
            for (final Resident assistant : new ArrayList<Resident>(this.getAssistants())) {
                if (assistant != resident && resident.hasTownRank("assistant")) {
                    try {
                        this.setMayor(assistant);
                    }
                    catch (TownyException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            if (this.isMayor(resident)) {
                for (final Resident newMayor : new ArrayList<Resident>(this.getResidents())) {
                    if (newMayor != resident) {
                        try {
                            this.setMayor(newMayor);
                        }
                        catch (TownyException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
        try {
            if (resident.hasTown()) {
                BukkitTools.getPluginManager().callEvent((Event)new TownRemoveResidentEvent(resident, resident.getTown()));
            }
            resident.setTown(null);
        }
        catch (AlreadyRegisteredException e) {}
        catch (IllegalStateException | NotRegisteredException e) {
            e.printStackTrace();
        }
        this.residents.remove(resident);
    }
    
    public void setSpawn(final Location spawn) throws TownyException {
        if (!this.hasHomeBlock()) {
            throw new TownyException(TownySettings.getLangString("msg_err_homeblock_has_not_been_set"));
        }
        final Coord spawnBlock = Coord.parseCoord(spawn);
        if (this.homeBlock.getX() == spawnBlock.getX() && this.homeBlock.getZ() == spawnBlock.getZ()) {
            this.spawn = spawn;
            return;
        }
        throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_homeblock"));
    }
    
    public void forceSetSpawn(final Location spawn) {
        this.spawn = spawn;
    }
    
    public Location getSpawn() throws TownyException {
        if (this.hasHomeBlock() && this.spawn != null) {
            return this.spawn;
        }
        this.spawn = null;
        throw new TownyException(TownySettings.getLangString("msg_err_town_has_not_set_a_spawn_location"));
    }
    
    public boolean hasSpawn() {
        return this.hasHomeBlock() && this.spawn != null;
    }
    
    public boolean hasHomeBlock() {
        return this.homeBlock != null;
    }
    
    public void clear() throws EmptyNationException {
        this.removeAllResidents();
        this.mayor = null;
        this.residents.clear();
        this.outlaws.clear();
        this.homeBlock = null;
        this.outpostSpawns.clear();
        this.jailSpawns.clear();
        this.revoltImmunityEndTime = 0L;
        this.siegeImmunityEndTime = 0L;
    }
    
    public boolean hasWorld() {
        return this.world != null;
    }
    
    @Override
    public void removeTownBlock(final TownBlock townBlock) throws NotRegisteredException {
        if (!this.hasTownBlock(townBlock)) {
            throw new NotRegisteredException();
        }
        if (townBlock.isOutpost()) {
            this.removeOutpostSpawn(townBlock.getCoord());
        }
        if (townBlock.isJail()) {
            this.removeJailSpawn(townBlock.getCoord());
        }
        try {
            if (this.getHomeBlock() == townBlock) {
                this.setHomeBlock(null);
            }
        }
        catch (TownyException ex) {}
        this.townBlocks.remove(townBlock);
        TownyUniverse.getInstance().getDataSource().saveTown(this);
    }
    
    @Override
    public void setPermissions(final String line) {
        this.permissions.load(line);
    }
    
    @Override
    public TownyPermission getPermissions() {
        return this.permissions;
    }
    
    public void addOutpostSpawn(final Location spawn) throws TownyException {
        this.removeOutpostSpawn(Coord.parseCoord(spawn));
        final Coord spawnBlock = Coord.parseCoord(spawn);
        try {
            final TownBlock outpost = TownyUniverse.getInstance().getDataSource().getWorld(spawn.getWorld().getName()).getTownBlock(spawnBlock);
            if (outpost.getX() == spawnBlock.getX() && outpost.getZ() == spawnBlock.getZ()) {
                if (!outpost.isOutpost()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_location_is_not_within_an_outpost_plot"));
                }
                this.outpostSpawns.add(spawn);
            }
        }
        catch (NotRegisteredException e) {
            throw new TownyException(TownySettings.getLangString("msg_err_location_is_not_within_a_town"));
        }
    }
    
    public void forceAddOutpostSpawn(final Location spawn) {
        this.outpostSpawns.add(spawn);
    }
    
    public Location getOutpostSpawn(final Integer index) throws TownyException {
        if (this.getMaxOutpostSpawn() == 0 && TownySettings.isOutpostsLimitedByLevels()) {
            throw new TownyException(TownySettings.getLangString("msg_err_town_has_no_outpost_spawns_set"));
        }
        return this.outpostSpawns.get(Math.min(this.getMaxOutpostSpawn() - 1, Math.max(0, index - 1)));
    }
    
    public int getMaxOutpostSpawn() {
        return this.outpostSpawns.size();
    }
    
    public boolean hasOutpostSpawn() {
        return this.outpostSpawns.size() > 0;
    }
    
    public List<Location> getAllOutpostSpawns() {
        return this.outpostSpawns;
    }
    
    public void removeOutpostSpawn(final Coord coord) {
        for (final Location spawn : new ArrayList<Location>(this.outpostSpawns)) {
            final Coord spawnBlock = Coord.parseCoord(spawn);
            if (coord.getX() == spawnBlock.getX() && coord.getZ() == spawnBlock.getZ()) {
                this.outpostSpawns.remove(spawn);
            }
        }
    }
    
    public void setPlotPrice(final double plotPrice) {
        if (plotPrice > TownySettings.getMaxPlotPrice()) {
            this.plotPrice = TownySettings.getMaxPlotPrice();
        }
        else {
            this.plotPrice = plotPrice;
        }
    }
    
    public double getPlotPrice() {
        return this.plotPrice;
    }
    
    public double getPlotTypePrice(final TownBlockType type) {
        double plotPrice = 0.0;
        switch (type.ordinal()) {
            case 0: {
                plotPrice = this.getPlotPrice();
                break;
            }
            case 1: {
                plotPrice = this.getCommercialPlotPrice();
                break;
            }
            case 3: {
                plotPrice = this.getEmbassyPlotPrice();
                break;
            }
            default: {
                plotPrice = this.getPlotPrice();
                break;
            }
        }
        if (plotPrice < 0.0) {
            plotPrice = 0.0;
        }
        return plotPrice;
    }
    
    public void setCommercialPlotPrice(final double commercialPlotPrice) {
        if (commercialPlotPrice > TownySettings.getMaxPlotPrice()) {
            this.commercialPlotPrice = TownySettings.getMaxPlotPrice();
        }
        else {
            this.commercialPlotPrice = commercialPlotPrice;
        }
    }
    
    public double getCommercialPlotPrice() {
        return this.commercialPlotPrice;
    }
    
    public void setEmbassyPlotPrice(final double embassyPlotPrice) {
        if (embassyPlotPrice > TownySettings.getMaxPlotPrice()) {
            this.embassyPlotPrice = TownySettings.getMaxPlotPrice();
        }
        else {
            this.embassyPlotPrice = embassyPlotPrice;
        }
    }
    
    public double getEmbassyPlotPrice() {
        return this.embassyPlotPrice;
    }
    
    public void setSpawnCost(final double spawnCost) {
        this.spawnCost = spawnCost;
    }
    
    public double getSpawnCost() {
        return this.spawnCost;
    }
    
    public boolean isHomeBlock(final TownBlock townBlock) {
        return this.hasHomeBlock() && townBlock == this.homeBlock;
    }
    
    public void setPlotTax(final double plotTax) {
        if (plotTax > TownySettings.getMaxTax()) {
            this.plotTax = TownySettings.getMaxTax();
        }
        else {
            this.plotTax = plotTax;
        }
    }
    
    public double getPlotTax() {
        return this.plotTax;
    }
    
    public void setCommercialPlotTax(final double commercialTax) {
        if (commercialTax > TownySettings.getMaxTax()) {
            this.commercialPlotTax = TownySettings.getMaxTax();
        }
        else {
            this.commercialPlotTax = commercialTax;
        }
    }
    
    public double getCommercialPlotTax() {
        return this.commercialPlotTax;
    }
    
    public void setEmbassyPlotTax(final double embassyPlotTax) {
        if (embassyPlotTax > TownySettings.getMaxTax()) {
            this.embassyPlotTax = TownySettings.getMaxTax();
        }
        else {
            this.embassyPlotTax = embassyPlotTax;
        }
    }
    
    public double getEmbassyPlotTax() {
        return this.embassyPlotTax;
    }
    
    public void setOpen(final boolean isOpen) {
        this.isOpen = isOpen;
    }
    
    public boolean isOpen() {
        return this.isOpen;
    }
    
    public void collect(final double amount) throws EconomyException {
        if (TownySettings.isUsingEconomy()) {
            final double bankcap = TownySettings.getTownBankCap();
            if (bankcap > 0.0 && amount + this.getAccount().getHoldingBalance() > bankcap) {
                TownyMessaging.sendPrefixedTownMessage(this, String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
                return;
            }
            this.getAccount().collect(amount, null);
        }
    }
    
    public void withdrawFromBank(final Resident resident, final int amount) throws EconomyException, TownyException {
        if (!TownySettings.isUsingEconomy()) {
            throw new TownyException(TownySettings.getLangString("msg_err_no_economy"));
        }
        if (!this.getAccount().payTo(amount, resident, "Town Withdraw")) {
            throw new TownyException(TownySettings.getLangString("msg_err_no_money"));
        }
    }
    
    @Override
    public List<String> getTreeString(final int depth) {
        final List<String> out = new ArrayList<String>();
        out.add(this.getTreeDepth(depth) + "Town (" + this.getName() + ")");
        out.add(this.getTreeDepth(depth + 1) + "Mayor: " + (this.hasMayor() ? this.getMayor().getName() : "None"));
        out.add(this.getTreeDepth(depth + 1) + "Home: " + this.homeBlock);
        out.add(this.getTreeDepth(depth + 1) + "Bonus: " + this.bonusBlocks);
        out.add(this.getTreeDepth(depth + 1) + "TownBlocks (" + this.getTownBlocks().size() + "): ");
        final List<Resident> assistants = this.getAssistants();
        if (assistants.size() > 0) {
            out.add(this.getTreeDepth(depth + 1) + "Assistants (" + assistants.size() + "): " + Arrays.toString(assistants.toArray(new Resident[0])));
        }
        out.add(this.getTreeDepth(depth + 1) + "Residents (" + this.getResidents().size() + "):");
        for (final Resident resident : this.getResidents()) {
            out.addAll(resident.getTreeString(depth + 2));
        }
        return out;
    }
    
    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public boolean isPublic() {
        return this.isPublic;
    }
    
    public List<Location> getJailSpawns() {
        return this.jailSpawns;
    }
    
    public void addJailSpawn(final Location spawn) throws TownyException {
        this.removeJailSpawn(Coord.parseCoord(spawn));
        final Coord spawnBlock = Coord.parseCoord(spawn);
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final TownBlock jail = townyUniverse.getDataSource().getWorld(spawn.getWorld().getName()).getTownBlock(spawnBlock);
            if (jail.getX() == spawnBlock.getX() && jail.getZ() == spawnBlock.getZ()) {
                if (!jail.isJail()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_location_is_not_within_a_jail_plot"));
                }
                this.jailSpawns.add(spawn);
                townyUniverse.getDataSource().saveTown(this);
            }
        }
        catch (NotRegisteredException e) {
            throw new TownyException(TownySettings.getLangString("msg_err_location_is_not_within_a_town"));
        }
    }
    
    public void removeJailSpawn(final Coord coord) {
        for (final Location spawn : new ArrayList<Location>(this.jailSpawns)) {
            final Coord spawnBlock = Coord.parseCoord(spawn);
            if (coord.getX() == spawnBlock.getX() && coord.getZ() == spawnBlock.getZ()) {
                this.jailSpawns.remove(spawn);
                TownyUniverse.getInstance().getDataSource().saveTown(this);
            }
        }
    }
    
    public void forceAddJailSpawn(final Location spawn) {
        this.jailSpawns.add(spawn);
    }
    
    public Location getJailSpawn(final Integer index) throws TownyException {
        if (this.getMaxJailSpawn() == 0) {
            throw new TownyException(TownySettings.getLangString("msg_err_town_has_no_jail_spawns_set"));
        }
        return this.jailSpawns.get(Math.min(this.getMaxJailSpawn() - 1, Math.max(0, index - 1)));
    }
    
    public int getMaxJailSpawn() {
        return this.jailSpawns.size();
    }
    
    public boolean hasJailSpawn() {
        return this.jailSpawns.size() > 0;
    }
    
    public List<Location> getAllJailSpawns() {
        return Collections.unmodifiableList((List<? extends Location>)this.jailSpawns);
    }
    
    @Override
    public List<Resident> getOutlaws() {
        return this.outlaws;
    }
    
    public boolean hasOutlaw(final String name) {
        for (final Resident outlaw : this.outlaws) {
            if (outlaw.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasOutlaw(final Resident outlaw) {
        return this.outlaws.contains(outlaw);
    }
    
    public void addOutlaw(final Resident resident) throws AlreadyRegisteredException {
        this.addOutlawCheck(resident);
        this.outlaws.add(resident);
    }
    
    public void addOutlawCheck(final Resident resident) throws AlreadyRegisteredException {
        if (this.hasOutlaw(resident)) {
            throw new AlreadyRegisteredException(TownySettings.getLangString("msg_err_resident_already_an_outlaw"));
        }
        if (resident.hasTown()) {
            try {
                if (resident.getTown().equals(this)) {
                    throw new AlreadyRegisteredException(TownySettings.getLangString("msg_err_not_outlaw_in_your_town"));
                }
            }
            catch (NotRegisteredException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void removeOutlaw(final Resident resident) throws NotRegisteredException {
        if (!this.hasOutlaw(resident)) {
            throw new NotRegisteredException();
        }
        this.outlaws.remove(resident);
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }
    
    public boolean hasValidUUID() {
        return this.uuid != null;
    }
    
    public void setRegistered(final long registered) {
        this.registered = registered;
    }
    
    public long getRegistered() {
        return this.registered;
    }
    
    public void setOutpostSpawns(final List<Location> outpostSpawns) {
        this.outpostSpawns = outpostSpawns;
    }
    
    public boolean isAlliedWith(final Town othertown) {
        if (this.hasNation() && othertown.hasNation()) {
            try {
                return this.getNation().hasAlly(othertown.getNation()) || this.getNation().equals(othertown.getNation());
            }
            catch (NotRegisteredException e) {
                return false;
            }
        }
        return false;
    }
    
    @Override
    public List<Invite> getReceivedInvites() {
        return this.receivedinvites;
    }
    
    @Override
    public void newReceivedInvite(final Invite invite) throws TooManyInvitesException {
        if (this.receivedinvites.size() <= InviteHandler.getReceivedInvitesMaxAmount(this) - 1) {
            this.receivedinvites.add(invite);
            return;
        }
        throw new TooManyInvitesException(String.format(TownySettings.getLangString("msg_err_town_has_too_many_invites"), this.getName()));
    }
    
    @Override
    public void deleteReceivedInvite(final Invite invite) {
        this.receivedinvites.remove(invite);
    }
    
    @Override
    public List<Invite> getSentInvites() {
        return this.sentinvites;
    }
    
    @Override
    public void newSentInvite(final Invite invite) throws TooManyInvitesException {
        if (this.sentinvites.size() <= InviteHandler.getSentInvitesMaxAmount(this) - 1) {
            this.sentinvites.add(invite);
            return;
        }
        throw new TooManyInvitesException(TownySettings.getLangString("msg_err_town_sent_too_many_invites"));
    }
    
    @Override
    public void deleteSentInvite(final Invite invite) {
        this.sentinvites.remove(invite);
    }
    
    public int getOutpostLimit() {
        return TownySettings.getMaxOutposts(this);
    }
    
    public boolean isOverOutpostLimit() {
        return this.getMaxOutpostSpawn() > this.getOutpostLimit();
    }
    
    public boolean isOverClaimed() {
        return this.getTownBlocks().size() > TownySettings.getMaxTownBlocks(this);
    }
    
    @Override
    public void addMetaData(final CustomDataField md) {
        super.addMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveTown(this);
    }
    
    @Override
    public void removeMetaData(final CustomDataField md) {
        super.removeMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveTown(this);
    }
    
    public void setConquered(final boolean conquered) {
        this.isConquered = conquered;
    }
    
    public boolean isConquered() {
        return this.isConquered;
    }
    
    public void setConqueredDays(final int conqueredDays) {
        this.conqueredDays = conqueredDays;
    }
    
    public int getConqueredDays() {
        return this.conqueredDays;
    }
    
    public Siege getSiege() {
        return this.siege;
    }
    
    public void setSiege(final Siege siege) {
        this.siege = siege;
    }
    
    public boolean isSiegeImmunityActive() {
        return (!this.hasSiege() || this.siege.getStatus() != SiegeStatus.IN_PROGRESS) && System.currentTimeMillis() < this.siegeImmunityEndTime;
    }
    
    public boolean isRevoltImmunityActive() {
        return System.currentTimeMillis() < this.revoltImmunityEndTime;
    }
    
    public void setSiegeImmunityEndTime(final long endTimeMillis) {
        this.siegeImmunityEndTime = endTimeMillis;
    }
    
    public void setRevoltImmunityEndTime(final long timeMillis) {
        this.revoltImmunityEndTime = timeMillis;
    }
    
    public boolean hasSiege() {
        return this.siege != null;
    }
    
    public long getSiegeImmunityEndTime() {
        return this.siegeImmunityEndTime;
    }
    
    public long getRevoltImmunityEndTime() {
        return this.revoltImmunityEndTime;
    }
    
    public String getFormattedHoursUntilRevoltCooldownEnds() {
        final double hoursRemainingMillis = (double)(this.revoltImmunityEndTime - System.currentTimeMillis());
        return TimeMgmt.getFormattedTimeValue(hoursRemainingMillis);
    }
    
    public double getPlunderValue() {
        return TownySettings.getWarSiegeAttackerPlunderAmountPerPlot() * this.townBlocks.size();
    }
    
    public double getSiegeCost() {
        return TownySettings.getWarSiegeAttackerCostUpFrontPerPlot() * this.townBlocks.size();
    }
    
    public String getFormattedHoursUntilSiegeImmunityEnds() {
        final double hoursUntilSiegeCooldownEnds = (double)(this.siegeImmunityEndTime - System.currentTimeMillis());
        return TimeMgmt.getFormattedTimeValue(hoursUntilSiegeCooldownEnds);
    }
    
    public boolean isRuined() {
        return this.residents.size() == 0;
    }
    
    public void setRecentlyRuinedEndTime(final long recentlyRuinedEndTime) {
        this.recentlyRuinedEndTime = recentlyRuinedEndTime;
    }
    
    public long getRecentlyRuinedEndTime() {
        return this.recentlyRuinedEndTime;
    }
    
    public void setOccupied(final boolean occupied) {
        this.occupied = occupied;
    }
    
    public boolean isOccupied() {
        return this.occupied;
    }
    
    public List<TownBlock> getTownBlocksForPlotGroup(final PlotObjectGroup group) {
        final ArrayList<TownBlock> retVal = new ArrayList<TownBlock>();
        TownyMessaging.sendErrorMsg(group.toString());
        for (final TownBlock townBlock : this.getTownBlocks()) {
            if (townBlock.hasPlotObjectGroup() && townBlock.getPlotObjectGroup().equals(group)) {
                retVal.add(townBlock);
            }
        }
        return retVal;
    }
    
    public void renamePlotGroup(final String oldName, final PlotObjectGroup group) {
        this.plotGroups.remove(oldName);
        this.plotGroups.put(group.getGroupName(), group);
    }
    
    public void addPlotGroup(final PlotObjectGroup group) {
        if (!this.hasObjectGroups()) {
            this.plotGroups = new HashMap<String, PlotObjectGroup>();
        }
        this.plotGroups.put(group.getGroupName(), group);
    }
    
    public void removePlotGroup(final PlotObjectGroup plotGroup) {
        if (this.hasObjectGroups() && this.plotGroups.remove(plotGroup.getGroupName()) != null) {
            for (final TownBlock tb : this.getTownBlocks()) {
                if (tb.hasPlotObjectGroup() && tb.getPlotObjectGroup().equals(plotGroup)) {
                    tb.getPlotObjectGroup().setID(null);
                    TownyUniverse.getInstance().getDataSource().saveTownBlock(tb);
                }
            }
        }
    }
    
    public int generatePlotGroupID() {
        return this.hasObjectGroups() ? this.getObjectGroups().size() : 0;
    }
    
    @Override
    public Collection<PlotObjectGroup> getObjectGroups() {
        if (this.plotGroups == null) {
            return null;
        }
        return this.plotGroups.values();
    }
    
    @Override
    public PlotObjectGroup getObjectGroupFromID(final UUID ID) {
        if (this.hasObjectGroups()) {
            for (final PlotObjectGroup pg : this.getObjectGroups()) {
                if (pg.getID().equals(ID)) {
                    return pg;
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean hasObjectGroups() {
        return this.plotGroups != null;
    }
    
    @Override
    public boolean hasObjectGroupName(final String name) {
        return this.hasObjectGroups() && this.plotGroups.containsKey(name);
    }
    
    public PlotObjectGroup getPlotObjectGroupFromName(final String name) {
        if (this.hasObjectGroups()) {
            return this.plotGroups.get(name);
        }
        return null;
    }
    
    public PlotObjectGroup getPlotObjectGroupFromID(final UUID ID) {
        return this.getObjectGroupFromID(ID);
    }
    
    public Collection<PlotObjectGroup> getPlotObjectGroups() {
        return this.getObjectGroups();
    }
    
    @Override
    public EconomyAccount getAccount() {
        if (this.account == null) {
            final String accountName = StringMgmt.trimMaxLength(Town.ECONOMY_ACCOUNT_PREFIX + this.getName(), 32);
            World world;
            if (this.hasWorld()) {
                world = BukkitTools.getWorld(this.getWorld().getName());
            }
            else {
                world = BukkitTools.getWorlds().get(0);
            }
            this.account = new EconomyAccount(accountName, world);
        }
        return this.account;
    }
    
    @Deprecated
    public World getBukkitWorld() {
        if (this.hasWorld()) {
            return BukkitTools.getWorld(this.getWorld().getName());
        }
        return BukkitTools.getWorlds().get(0);
    }
    
    @Deprecated
    public String getEconomyName() {
        return StringMgmt.trimMaxLength(Town.ECONOMY_ACCOUNT_PREFIX + this.getName(), 32);
    }
    
    public boolean isNeutral() {
        return this.neutral;
    }
    
    public int getNeutralityChangeConfirmationCounterDays() {
        return this.neutralityChangeConfirmationCounterDays;
    }
    
    public void decrementNeutralityChangeConfirmationCounterDays() {
        --this.neutralityChangeConfirmationCounterDays;
    }
    
    public void flipNeutral() {
        this.neutral = !this.neutral;
    }
    
    public void flipDesiredNeutralityValue() {
        this.desiredNeutralityValue = !this.desiredNeutralityValue;
    }
    
    public void setNeutralityChangeConfirmationCounterDays(final int counterValueDays) {
        this.neutralityChangeConfirmationCounterDays = counterValueDays;
    }
    
    public void setDesiredNeutralityValue(final boolean value) {
        this.desiredNeutralityValue = value;
    }
    
    public boolean getDesiredNeutralityValue() {
        return this.desiredNeutralityValue;
    }
    
    public void setNeutral(final boolean value) {
        this.neutral = value;
    }
    
    static {
        ECONOMY_ACCOUNT_PREFIX = TownySettings.getTownAccountPrefix();
    }
}
