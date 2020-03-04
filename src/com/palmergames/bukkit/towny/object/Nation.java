package com.palmergames.bukkit.towny.object;

import org.bukkit.World;
import com.palmergames.util.StringMgmt;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import java.util.Arrays;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.Iterator;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarMembershipController;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.NationTagChangeEvent;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.invites.Invite;
import org.bukkit.Location;
import java.util.UUID;
import com.palmergames.bukkit.towny.war.siegewar.locations.SiegeZone;
import java.util.List;
import com.palmergames.bukkit.towny.invites.TownyAllySender;

public class Nation extends TownyObject implements ResidentList, TownyInviter, TownyAllySender, EconomyHandler
{
    private static final String ECONOMY_ACCOUNT_PREFIX;
    private List<Town> towns;
    private List<Nation> allies;
    private List<Nation> enemies;
    private List<SiegeZone> siegeZones;
    private Town capital;
    private double taxes;
    private double spawnCost;
    private boolean neutral;
    private String nationBoard;
    private String tag;
    public UUID uuid;
    private long registered;
    private Location nationSpawn;
    private boolean isPublic;
    private boolean isOpen;
    private transient List<Invite> receivedinvites;
    private transient List<Invite> sentinvites;
    private transient List<Invite> sentallyinvites;
    private transient EconomyAccount account;
    
    public Nation(final String name) {
        super(name);
        this.towns = new ArrayList<Town>();
        this.allies = new ArrayList<Nation>();
        this.enemies = new ArrayList<Nation>();
        this.siegeZones = new ArrayList<SiegeZone>();
        this.neutral = false;
        this.nationBoard = "/nation set board [msg]";
        this.tag = "";
        this.isPublic = TownySettings.getNationDefaultPublic();
        this.isOpen = TownySettings.getNationDefaultOpen();
        this.receivedinvites = new ArrayList<Invite>();
        this.sentinvites = new ArrayList<Invite>();
        this.sentallyinvites = new ArrayList<Invite>();
    }
    
    public void setTag(final String text) throws TownyException {
        if (text.length() > 4) {
            throw new TownyException(TownySettings.getLangString("msg_err_tag_too_long"));
        }
        this.tag = text.toUpperCase().trim();
        Bukkit.getPluginManager().callEvent((Event)new NationTagChangeEvent(this.tag));
    }
    
    public String getTag() {
        return this.tag;
    }
    
    public boolean hasTag() {
        return !this.tag.isEmpty();
    }
    
    public void addAlly(final Nation nation) throws AlreadyRegisteredException {
        if (this.hasAlly(nation)) {
            throw new AlreadyRegisteredException();
        }
        try {
            this.removeEnemy(nation);
        }
        catch (NotRegisteredException ex) {}
        if (TownySettings.getWarSiegeEnabled() && nation.hasAlly(this)) {
            SiegeWarMembershipController.evaluateNationsFormNewAlliance(this, nation);
        }
        this.getAllies().add(nation);
    }
    
    public boolean removeAlly(final Nation nation) throws NotRegisteredException {
        if (!this.hasAlly(nation)) {
            throw new NotRegisteredException();
        }
        if (TownySettings.getWarSiegeEnabled()) {
            SiegeWarMembershipController.evaluateNationRemoveAlly(this, nation);
        }
        return this.getAllies().remove(nation);
    }
    
    public boolean removeAllAllies() {
        for (final Nation ally : new ArrayList<Nation>(this.getAllies())) {
            try {
                this.removeAlly(ally);
                ally.removeAlly(this);
            }
            catch (NotRegisteredException ex) {}
        }
        return this.getAllies().size() == 0;
    }
    
    public boolean hasAlly(final Nation nation) {
        return this.getAllies().contains(nation);
    }
    
    public boolean hasMutualAlly(final Nation nation) {
        return this.getAllies().contains(nation) && nation.getAllies().contains(this);
    }
    
    public boolean IsAlliedWith(final Nation nation) {
        return this.getAllies().contains(nation);
    }
    
    public void addEnemy(final Nation nation) throws AlreadyRegisteredException {
        if (this.hasEnemy(nation)) {
            throw new AlreadyRegisteredException();
        }
        try {
            this.removeAlly(nation);
        }
        catch (NotRegisteredException ex) {}
        this.getEnemies().add(nation);
    }
    
    public boolean removeEnemy(final Nation nation) throws NotRegisteredException {
        if (!this.hasEnemy(nation)) {
            throw new NotRegisteredException();
        }
        return this.getEnemies().remove(nation);
    }
    
    public boolean removeAllEnemies() {
        for (final Nation enemy : new ArrayList<Nation>(this.getEnemies())) {
            try {
                this.removeEnemy(enemy);
                enemy.removeEnemy(this);
            }
            catch (NotRegisteredException ex) {}
        }
        return this.getAllies().size() == 0;
    }
    
    public boolean hasEnemy(final Nation nation) {
        return this.getEnemies().contains(nation);
    }
    
    public List<Town> getTowns() {
        return this.towns;
    }
    
    public boolean isKing(final Resident resident) {
        return this.hasCapital() && this.getCapital().isMayor(resident);
    }
    
    public boolean hasCapital() {
        return this.getCapital() != null;
    }
    
    public boolean hasAssistant(final Resident resident) {
        return this.getAssistants().contains(resident);
    }
    
    public boolean isCapital(final Town town) {
        return town == this.getCapital();
    }
    
    public boolean hasTown(final String name) {
        for (final Town town : this.towns) {
            if (town.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasTown(final Town town) {
        return this.towns.contains(town);
    }
    
    public void addTown(final Town town) throws AlreadyRegisteredException {
        if (this.hasTown(town)) {
            throw new AlreadyRegisteredException();
        }
        if (town.hasNation()) {
            throw new AlreadyRegisteredException();
        }
        this.towns.add(town);
        town.setNation(this);
        BukkitTools.getPluginManager().callEvent((Event)new NationAddTownEvent(town, this));
    }
    
    public void setCapital(final Town capital) {
        this.capital = capital;
        try {
            this.recheckTownDistance();
            TownyPerms.assignPermissions(capital.getMayor(), null);
        }
        catch (Exception ex) {}
    }
    
    public Town getCapital() {
        return this.capital;
    }
    
    public Location getNationSpawn() throws TownyException {
        if (this.nationSpawn == null) {
            throw new TownyException(TownySettings.getLangString("msg_err_nation_has_not_set_a_spawn_location"));
        }
        return this.nationSpawn;
    }
    
    public boolean hasNationSpawn() {
        return this.nationSpawn != null;
    }
    
    public void setNationSpawn(final Location spawn) throws TownyException {
        final Coord spawnBlock = Coord.parseCoord(spawn);
        TownBlock townBlock = null;
        final TownyWorld world = TownyUniverse.getInstance().getDataSource().getWorld(spawn.getWorld().getName());
        if (world.hasTownBlock(spawnBlock)) {
            townBlock = world.getTownBlock(spawnBlock);
            if (TownySettings.getBoolean(ConfigNodes.GNATION_SETTINGS_CAPITAL_SPAWN)) {
                if (this.capital == null) {
                    throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_capital"));
                }
                if (!townBlock.hasTown()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_capital"));
                }
                if (townBlock.getTown() != this.getCapital()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_capital"));
                }
            }
            else {
                if (!townBlock.hasTown()) {
                    throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_nationtowns"));
                }
                if (!this.towns.contains(townBlock.getTown())) {
                    throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_nationtowns"));
                }
            }
            this.nationSpawn = spawn;
            return;
        }
        throw new TownyException(String.format(TownySettings.getLangString("msg_cache_block_error_wild"), "set spawn"));
    }
    
    public void forceSetNationSpawn(final Location nationSpawn) {
        this.nationSpawn = nationSpawn;
    }
    
    public boolean setAllegiance(final String type, final Nation nation) {
        try {
            if (type.equalsIgnoreCase("ally")) {
                this.removeEnemy(nation);
                this.addAlly(nation);
                if (!this.hasEnemy(nation) && this.hasAlly(nation)) {
                    return true;
                }
            }
            else if (type.equalsIgnoreCase("peaceful") || type.equalsIgnoreCase("neutral")) {
                this.removeEnemy(nation);
                this.removeAlly(nation);
                if (!this.hasEnemy(nation) && !this.hasAlly(nation)) {
                    return true;
                }
            }
            else if (type.equalsIgnoreCase("enemy")) {
                this.removeAlly(nation);
                this.addEnemy(nation);
                if (this.hasEnemy(nation) && !this.hasAlly(nation)) {
                    return true;
                }
            }
        }
        catch (AlreadyRegisteredException | NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public List<Resident> getAssistants() {
        final List<Resident> assistants = new ArrayList<Resident>();
        for (final Town town : this.towns) {
            for (final Resident assistant : town.getResidents()) {
                if (assistant.hasNationRank("assistant")) {
                    assistants.add(assistant);
                }
            }
        }
        return assistants;
    }
    
    public void setEnemies(final List<Nation> enemies) {
        this.enemies = enemies;
    }
    
    public List<Nation> getEnemies() {
        return this.enemies;
    }
    
    public void setAllies(final List<Nation> allies) {
        this.allies = allies;
    }
    
    public List<Nation> getAllies() {
        return this.allies;
    }
    
    public List<Nation> getMutualAllies() {
        final List<Nation> result = new ArrayList<Nation>();
        for (final Nation ally : this.getAllies()) {
            if (ally.hasAlly(this)) {
                result.add(ally);
            }
        }
        return result;
    }
    
    public int getNumTowns() {
        return this.towns.size();
    }
    
    public int getNumResidents() {
        int numResidents = 0;
        for (final Town town : this.getTowns()) {
            numResidents += town.getNumResidents();
        }
        return numResidents;
    }
    
    public void removeTown(final Town town) throws EmptyNationException, NotRegisteredException {
        if (!this.hasTown(town)) {
            throw new NotRegisteredException();
        }
        final boolean isCapital = town.isCapital();
        if (TownySettings.getWarSiegeEnabled()) {
            SiegeWarMembershipController.evaluateNationRemoveTown(town);
        }
        this.remove(town);
        if (this.getNumTowns() == 0) {
            throw new EmptyNationException(this);
        }
        if (isCapital) {
            int numResidents = 0;
            Town tempCapital = null;
            for (final Town newCapital : this.getTowns()) {
                if (newCapital.getNumResidents() > numResidents) {
                    tempCapital = newCapital;
                    numResidents = newCapital.getNumResidents();
                }
            }
            if (tempCapital != null) {
                this.setCapital(tempCapital);
            }
        }
    }
    
    private void remove(final Town town) {
        try {
            town.setNation(null);
        }
        catch (AlreadyRegisteredException ex) {}
        town.setOccupied(false);
        final List<Resident> titleRemove = new ArrayList<Resident>(town.getResidents());
        for (final Resident res : titleRemove) {
            if (res.hasTitle() || res.hasSurname()) {
                res.setTitle("");
                res.setSurname("");
            }
            res.updatePermsForNationRemoval();
            TownyUniverse.getInstance().getDataSource().saveResident(res);
        }
        this.towns.remove(town);
        BukkitTools.getPluginManager().callEvent((Event)new NationRemoveTownEvent(town, this));
    }
    
    public void removeSiegeZone(final SiegeZone siegeZone) {
        this.siegeZones.remove(siegeZone);
    }
    
    private void removeAllTowns() {
        for (final Town town : new ArrayList<Town>(this.towns)) {
            this.remove(town);
        }
    }
    
    private void removeAllSiegeZones() {
        for (final SiegeZone siegeZone : new ArrayList<SiegeZone>(this.siegeZones)) {
            this.siegeZones.remove(siegeZone);
        }
    }
    
    public void setTaxes(final double taxes) {
        if (taxes > TownySettings.getMaxTax()) {
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
    
    public void clear() {
        this.removeAllAllies();
        this.removeAllEnemies();
        this.removeAllTowns();
        this.removeAllSiegeZones();
        this.capital = null;
    }
    
    public void recheckTownDistance() throws TownyException {
        if (this.capital != null && TownySettings.getNationRequiresProximity() > 0.0) {
            final Coord capitalCoord = this.capital.getHomeBlock().getCoord();
            final Iterator<Town> it = this.towns.iterator();
            while (it.hasNext()) {
                final Town town = it.next();
                final Coord townCoord = town.getHomeBlock().getCoord();
                if (!this.capital.getHomeBlock().getWorld().getName().equals(town.getHomeBlock().getWorld().getName())) {
                    it.remove();
                }
                else {
                    final double distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2.0) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2.0));
                    if (distance <= TownySettings.getNationRequiresProximity()) {
                        continue;
                    }
                    town.setNation(null);
                    it.remove();
                }
            }
        }
    }
    
    public void setNeutral(final boolean neutral) throws TownyException {
        if (!TownySettings.isDeclaringNeutral() && neutral) {
            throw new TownyException(TownySettings.getLangString("msg_err_fight_like_king"));
        }
        if (neutral) {
            for (final Resident resident : this.getResidents()) {
                TownyWar.removeAttackerFlags(resident.getName());
            }
        }
        this.neutral = neutral;
    }
    
    public boolean isNeutral() {
        return this.neutral;
    }
    
    public void setKing(final Resident king) throws TownyException {
        if (!this.hasResident(king)) {
            throw new TownyException(TownySettings.getLangString("msg_err_king_not_in_nation"));
        }
        if (!king.isMayor()) {
            throw new TownyException(TownySettings.getLangString("msg_err_new_king_notmayor"));
        }
        this.setCapital(king.getTown());
    }
    
    public boolean hasResident(final Resident resident) {
        for (final Town town : this.getTowns()) {
            if (town.hasResident(resident)) {
                return true;
            }
        }
        return false;
    }
    
    public void collect(final double amount) throws EconomyException {
        if (TownySettings.isUsingEconomy()) {
            final double bankcap = TownySettings.getNationBankCap();
            if (bankcap > 0.0 && amount + this.getAccount().getHoldingBalance() > bankcap) {
                TownyMessaging.sendPrefixedNationMessage(this, String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
                return;
            }
            this.getAccount().collect(amount, null);
        }
    }
    
    public void withdrawFromBank(final Resident resident, final int amount) throws EconomyException, TownyException {
        if (!TownySettings.isUsingEconomy()) {
            throw new TownyException(TownySettings.getLangString("msg_err_no_economy"));
        }
        if (!this.getAccount().payTo(amount, resident, "Nation Withdraw")) {
            throw new TownyException(TownySettings.getLangString("msg_err_no_money"));
        }
    }
    
    @Override
    public List<Resident> getResidents() {
        final List<Resident> out = new ArrayList<Resident>();
        for (final Town town : this.getTowns()) {
            out.addAll(town.getResidents());
        }
        return out;
    }
    
    @Override
    public List<String> getTreeString(final int depth) {
        final List<String> out = new ArrayList<String>();
        out.add(this.getTreeDepth(depth) + "Nation (" + this.getName() + ")");
        out.add(this.getTreeDepth(depth + 1) + "Capital: " + this.getCapital().getName());
        final List<Resident> assistants = this.getAssistants();
        if (assistants.size() > 0) {
            out.add(this.getTreeDepth(depth + 1) + "Assistants (" + assistants.size() + "): " + Arrays.toString(assistants.toArray(new Resident[0])));
        }
        if (this.getAllies().size() > 0) {
            out.add(this.getTreeDepth(depth + 1) + "Allies (" + this.getAllies().size() + "): " + Arrays.toString(this.getAllies().toArray(new Nation[0])));
        }
        if (this.getEnemies().size() > 0) {
            out.add(this.getTreeDepth(depth + 1) + "Enemies (" + this.getEnemies().size() + "): " + Arrays.toString(this.getEnemies().toArray(new Nation[0])));
        }
        out.add(this.getTreeDepth(depth + 1) + "Towns (" + this.getTowns().size() + "):");
        for (final Town town : this.getTowns()) {
            out.addAll(town.getTreeString(depth + 2));
        }
        return out;
    }
    
    @Override
    public boolean hasResident(final String name) {
        for (final Town town : this.getTowns()) {
            if (town.hasResident(name)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<Resident> getOutlaws() {
        final List<Resident> out = new ArrayList<Resident>();
        for (final Town town : this.getTowns()) {
            out.addAll(town.getOutlaws());
        }
        return out;
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
    
    public long getRegistered() {
        return this.registered;
    }
    
    public void setRegistered(final long registered) {
        this.registered = registered;
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
        throw new TooManyInvitesException(String.format(TownySettings.getLangString("msg_err_nation_has_too_many_requests"), this.getName()));
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
        throw new TooManyInvitesException(TownySettings.getLangString("msg_err_nation_sent_too_many_invites"));
    }
    
    @Override
    public void deleteSentInvite(final Invite invite) {
        this.sentinvites.remove(invite);
    }
    
    @Override
    public void newSentAllyInvite(final Invite invite) throws TooManyInvitesException {
        if (this.sentallyinvites.size() <= InviteHandler.getSentAllyRequestsMaxAmount(this) - 1) {
            this.sentallyinvites.add(invite);
            return;
        }
        throw new TooManyInvitesException(TownySettings.getLangString("msg_err_nation_sent_too_many_requests"));
    }
    
    @Override
    public void deleteSentAllyInvite(final Invite invite) {
        this.sentallyinvites.remove(invite);
    }
    
    @Override
    public List<Invite> getSentAllyInvites() {
        return this.sentallyinvites;
    }
    
    public void setNationBoard(final String nationBoard) {
        this.nationBoard = nationBoard;
    }
    
    public String getNationBoard() {
        return this.nationBoard;
    }
    
    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public boolean isPublic() {
        return this.isPublic;
    }
    
    public void setOpen(final boolean isOpen) {
        this.isOpen = isOpen;
    }
    
    public boolean isOpen() {
        return this.isOpen;
    }
    
    public void setSpawnCost(final double spawnCost) {
        this.spawnCost = spawnCost;
    }
    
    public double getSpawnCost() {
        return this.spawnCost;
    }
    
    public void addSiegeZone(final SiegeZone siegeFront) {
        this.siegeZones.add(siegeFront);
    }
    
    public List<Town> getTownsUnderSiegeAttack() {
        final List<Town> result = new ArrayList<Town>();
        for (final SiegeZone siegeFront : this.siegeZones) {
            result.add(siegeFront.getSiege().getDefendingTown());
        }
        return result;
    }
    
    public List<Town> getTownsUnderSiegeDefence() {
        final List<Town> result = new ArrayList<Town>();
        for (final Town town : this.towns) {
            if (town.hasSiege() && town.getSiege().getAttackerWinner() != this) {
                result.add(town);
            }
        }
        return result;
    }
    
    public List<Town> getTownsUnderActiveSiegeDefence() {
        final List<Town> result = new ArrayList<Town>();
        for (final Town town : this.towns) {
            if (town.hasSiege() && town.getSiege().getStatus() == SiegeStatus.IN_PROGRESS) {
                result.add(town);
            }
        }
        return result;
    }
    
    public boolean isNationAttackingTown(final Town town) {
        return town.hasSiege() && town.getSiege().getStatus() == SiegeStatus.IN_PROGRESS && town.getSiege().getSiegeZones().containsKey(this);
    }
    
    public List<SiegeZone> getSiegeZones() {
        return this.siegeZones;
    }
    
    public List<String> getSiegeZoneNames() {
        final List<String> names = new ArrayList<String>();
        for (final SiegeZone siegeZone : this.siegeZones) {
            names.add(siegeZone.getName());
        }
        return names;
    }
    
    public int getNumTownblocks() {
        int townBlocksClaimed = 0;
        for (final Town towns : this.getTowns()) {
            townBlocksClaimed += towns.getTownBlocks().size();
        }
        return townBlocksClaimed;
    }
    
    public Resident getKing() {
        return this.capital.getMayor();
    }
    
    @Override
    public void addMetaData(final CustomDataField md) {
        super.addMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveNation(this);
    }
    
    @Override
    public void removeMetaData(final CustomDataField md) {
        super.removeMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveNation(this);
    }
    
    @Override
    public EconomyAccount getAccount() {
        if (this.account == null) {
            final String accountName = StringMgmt.trimMaxLength(Nation.ECONOMY_ACCOUNT_PREFIX + this.getName(), 32);
            World world;
            if (this.hasCapital() && this.getCapital().hasWorld()) {
                world = BukkitTools.getWorld(this.getCapital().getWorld().getName());
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
        if (this.hasCapital() && this.getCapital().hasWorld()) {
            return BukkitTools.getWorld(this.getCapital().getWorld().getName());
        }
        return BukkitTools.getWorlds().get(0);
    }
    
    @Deprecated
    public String getEconomyName() {
        return StringMgmt.trimMaxLength(Nation.ECONOMY_ACCOUNT_PREFIX + this.getName(), 32);
    }
    
    static {
        ECONOMY_ACCOUNT_PREFIX = TownySettings.getNationAccountPrefix();
    }
}
