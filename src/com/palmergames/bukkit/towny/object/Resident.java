// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import org.bukkit.World;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.event.TownRemoveResidentRankEvent;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarRankController;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.TownAddResidentRankEvent;
import com.palmergames.bukkit.towny.tasks.SetDefaultModes;
import com.palmergames.util.StringMgmt;
import java.util.Arrays;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import java.util.Iterator;
import java.util.Collection;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyUniverse;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import org.bukkit.Location;
import java.util.List;
import com.palmergames.bukkit.towny.invites.TownyInviteReceiver;

public class Resident extends TownyObject implements ResidentModes, TownyInviteReceiver, EconomyHandler, TownBlockOwner
{
    private List<Resident> friends;
    private Town town;
    private long lastOnline;
    private long registered;
    private boolean isNPC;
    private boolean isJailed;
    private int jailSpawn;
    private int jailDays;
    private String jailTown;
    private String title;
    private String surname;
    private long teleportRequestTime;
    private Location teleportDestination;
    private double teleportCost;
    private List<String> modes;
    private transient ConfirmationType confirmationType;
    private transient List<Invite> receivedinvites;
    private transient EconomyAccount account;
    private List<String> townRanks;
    private List<String> nationRanks;
    private List<TownBlock> townBlocks;
    private TownyPermission permissions;
    
    public Resident(final String name) {
        super(name);
        this.friends = new ArrayList<Resident>();
        this.town = null;
        this.isNPC = false;
        this.isJailed = false;
        this.jailTown = "";
        this.title = "";
        this.surname = "";
        this.teleportRequestTime = -1L;
        this.teleportCost = 0.0;
        this.modes = new ArrayList<String>();
        this.receivedinvites = new ArrayList<Invite>();
        this.account = new EconomyAccount(this.getName());
        this.townRanks = new ArrayList<String>();
        this.nationRanks = new ArrayList<String>();
        this.townBlocks = new ArrayList<TownBlock>();
        (this.permissions = new TownyPermission()).loadDefault(this);
    }
    
    public void setLastOnline(final long lastOnline) {
        this.lastOnline = lastOnline;
    }
    
    public long getLastOnline() {
        return this.lastOnline;
    }
    
    public void setNPC(final boolean isNPC) {
        this.isNPC = isNPC;
    }
    
    public boolean isNPC() {
        return this.isNPC;
    }
    
    public void setJailed(final boolean isJailed) {
        this.isJailed = isJailed;
        if (isJailed) {
            TownyUniverse.getInstance().getJailedResidentMap().add(this);
        }
        else {
            TownyUniverse.getInstance().getJailedResidentMap().remove(this);
        }
    }
    
    public void sendToJail(final Player player, final Integer index, final Town town) {
        this.setJailed(true);
        this.setJailSpawn(index);
        this.setJailTown(town.getName());
        TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_you_have_been_sent_to_jail"));
        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_has_been_sent_to_jail_number"), player.getName(), index));
    }
    
    public void freeFromJail(final Player player, final Integer index, final boolean escaped) {
        this.setJailed(false);
        this.removeJailSpawn();
        this.setJailTown(" ");
        if (!escaped) {
            TownyMessaging.sendMsg(this, TownySettings.getLangString("msg_you_have_been_freed_from_jail"));
            TownyMessaging.sendPrefixedTownMessage(this.town, String.format(TownySettings.getLangString("msg_player_has_been_freed_from_jail_number"), this.getName(), index));
        }
        else {
            try {
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_player_escaped_jail_into_wilderness"), player.getName(), TownyUniverse.getInstance().getDataSource().getWorld(player.getLocation().getWorld().getName()).getUnclaimedZoneName()));
            }
            catch (NotRegisteredException ex) {}
        }
    }
    
    public void setJailedByMayor(final Player player, final Integer index, final Town town, final Integer days) {
        if (this.isJailed) {
            try {
                final Location loc = this.getTown().getSpawn();
                if (BukkitTools.isOnline(player.getName())) {
                    player.sendMessage(String.format(TownySettings.getLangString("msg_town_spawn_warmup"), TownySettings.getTeleportWarmupTime()));
                    TownyAPI.getInstance().jailTeleport(player, loc);
                }
                this.freeFromJail(player, index, false);
            }
            catch (TownyException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                final Location loc = town.getJailSpawn(index);
                player.sendMessage(String.format(TownySettings.getLangString("msg_town_spawn_warmup"), TownySettings.getTeleportWarmupTime()));
                TownyAPI.getInstance().jailTeleport(player, loc);
                this.sendToJail(player, index, town);
                if (days > 0) {
                    this.setJailDays(days);
                    TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you've_been_jailed_for_x_days"), days));
                }
            }
            catch (TownyException e) {
                e.printStackTrace();
            }
        }
        TownyUniverse.getInstance().getDataSource().saveResident(this);
    }
    
    public void setJailed(final Resident resident, final Integer index, final Town town) {
        Player player = null;
        if (BukkitTools.isOnline(resident.getName())) {
            player = BukkitTools.getPlayer(resident.getName());
        }
        if (this.isJailed) {
            try {
                if (player != null) {
                    Location loc;
                    if (this.hasTown()) {
                        loc = this.getTown().getSpawn();
                    }
                    else {
                        loc = player.getWorld().getSpawnLocation();
                    }
                    player.teleport(loc);
                }
                this.freeFromJail(player, index, false);
            }
            catch (TownyException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                final Location loc = town.getJailSpawn(index);
                player.teleport(loc);
                this.sendToJail(player, index, town);
            }
            catch (TownyException e) {
                e.printStackTrace();
            }
        }
        TownyUniverse.getInstance().getDataSource().saveResident(this);
    }
    
    public boolean isJailed() {
        return this.isJailed;
    }
    
    public boolean hasJailSpawn() {
        return this.jailSpawn > 0;
    }
    
    public int getJailSpawn() {
        return this.jailSpawn;
    }
    
    public void setJailSpawn(final Integer index) {
        this.jailSpawn = index;
    }
    
    public void removeJailSpawn() {
        this.jailSpawn = 0;
    }
    
    public String getJailTown() {
        return this.jailTown;
    }
    
    public void setJailTown(final String jailTown) {
        if (jailTown == null) {
            this.jailTown = "";
            return;
        }
        this.jailTown = jailTown.trim();
    }
    
    public boolean hasJailTown(final String jailtown) {
        return this.jailTown.equalsIgnoreCase(jailtown);
    }
    
    public int getJailDays() {
        return this.jailDays;
    }
    
    public void setJailDays(final Integer days) {
        this.jailDays = days;
    }
    
    public boolean hasJailDays() {
        return this.jailDays > 0;
    }
    
    public void setTitle(final String title) {
        this.title = title.trim();
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public boolean hasTitle() {
        return !this.title.isEmpty();
    }
    
    public void setSurname(final String surname) {
        this.surname = surname.trim();
    }
    
    public String getSurname() {
        return this.surname;
    }
    
    public boolean hasSurname() {
        return !this.surname.isEmpty();
    }
    
    public boolean isKing() {
        try {
            return this.getTown().getNation().isKing(this);
        }
        catch (TownyException e) {
            return false;
        }
    }
    
    public boolean isMayor() {
        return this.hasTown() && this.town.isMayor(this);
    }
    
    public boolean hasTown() {
        return this.town != null;
    }
    
    public boolean hasNation() {
        return this.hasTown() && this.town.hasNation();
    }
    
    public Town getTown() throws NotRegisteredException {
        if (this.hasTown()) {
            return this.town;
        }
        throw new NotRegisteredException(TownySettings.getLangString("msg_err_resident_doesnt_belong_to_any_town"));
    }
    
    public void setTown(final Town town) throws AlreadyRegisteredException {
        if (town == null) {
            this.town = null;
            this.setTitle("");
            this.setSurname("");
            this.updatePerms();
            return;
        }
        if (this.town == town) {
            return;
        }
        if (this.hasTown()) {
            throw new AlreadyRegisteredException();
        }
        this.town = town;
        this.setTitle("");
        this.setSurname("");
        this.updatePerms();
    }
    
    public void setFriends(final List<Resident> newFriends) {
        this.friends = newFriends;
    }
    
    public List<Resident> getFriends() {
        return this.friends;
    }
    
    public boolean removeFriend(final Resident resident) throws NotRegisteredException {
        if (this.hasFriend(resident)) {
            return this.friends.remove(resident);
        }
        throw new NotRegisteredException();
    }
    
    public boolean hasFriend(final Resident resident) {
        return this.friends.contains(resident);
    }
    
    public void addFriend(final Resident resident) throws AlreadyRegisteredException {
        if (this.hasFriend(resident)) {
            throw new AlreadyRegisteredException();
        }
        this.friends.add(resident);
    }
    
    public void removeAllFriends() {
        for (final Resident resident : new ArrayList<Resident>(this.friends)) {
            try {
                this.removeFriend(resident);
            }
            catch (NotRegisteredException ex) {}
        }
    }
    
    public void clear() throws EmptyTownException {
        this.removeAllFriends();
        if (this.hasTown()) {
            try {
                this.town.removeResident(this);
                this.setTitle("");
                this.setSurname("");
                this.updatePerms();
            }
            catch (NotRegisteredException ex) {}
        }
    }
    
    public void updatePerms() {
        this.townRanks.clear();
        this.nationRanks.clear();
        TownyPerms.assignPermissions(this, null);
    }
    
    public void updatePermsForNationRemoval() {
        this.nationRanks.clear();
        TownyPerms.assignPermissions(this, null);
    }
    
    public void setRegistered(final long registered) {
        this.registered = registered;
    }
    
    public long getRegistered() {
        return this.registered;
    }
    
    @Override
    public List<String> getTreeString(final int depth) {
        final List<String> out = new ArrayList<String>();
        out.add(this.getTreeDepth(depth) + "Resident (" + this.getName() + ")");
        out.add(this.getTreeDepth(depth + 1) + "Registered: " + this.getRegistered());
        out.add(this.getTreeDepth(depth + 1) + "Last Online: " + this.getLastOnline());
        if (this.getFriends().size() > 0) {
            out.add(this.getTreeDepth(depth + 1) + "Friends (" + this.getFriends().size() + "): " + Arrays.toString(this.getFriends().toArray(new Resident[0])));
        }
        return out;
    }
    
    public void clearTeleportRequest() {
        this.teleportRequestTime = -1L;
    }
    
    public void setTeleportRequestTime() {
        this.teleportRequestTime = System.currentTimeMillis();
    }
    
    public long getTeleportRequestTime() {
        return this.teleportRequestTime;
    }
    
    public void setTeleportDestination(final Location spawnLoc) {
        this.teleportDestination = spawnLoc;
    }
    
    public Location getTeleportDestination() {
        return this.teleportDestination;
    }
    
    public boolean hasRequestedTeleport() {
        return this.teleportRequestTime != -1L;
    }
    
    public void setTeleportCost(final double cost) {
        this.teleportCost = cost;
    }
    
    public double getTeleportCost() {
        return this.teleportCost;
    }
    
    @Override
    public List<String> getModes() {
        return this.modes;
    }
    
    @Override
    public boolean hasMode(final String mode) {
        return this.modes.contains(mode.toLowerCase());
    }
    
    @Override
    public void toggleMode(final String[] newModes, final boolean notify) {
        for (String mode : newModes) {
            mode = mode.toLowerCase();
            if (this.modes.contains(mode)) {
                this.modes.remove(mode);
            }
            else {
                this.modes.add(mode);
            }
        }
        if (this.modes.isEmpty()) {
            this.clearModes();
            return;
        }
        if (notify) {
            TownyMessaging.sendMsg(this, TownySettings.getLangString("msg_modes_set") + StringMgmt.join(this.getModes(), ","));
        }
    }
    
    @Override
    public void setModes(final String[] modes, final boolean notify) {
        this.modes.clear();
        this.toggleMode(modes, false);
        if (notify) {
            TownyMessaging.sendMsg(this, TownySettings.getLangString("msg_modes_set") + StringMgmt.join(this.getModes(), ","));
        }
    }
    
    @Override
    public void clearModes() {
        this.modes.clear();
        if (BukkitTools.scheduleSyncDelayedTask(new SetDefaultModes(this.getName(), true), 1L) == -1) {
            TownyMessaging.sendErrorMsg(TownySettings.getLangString("msg_err_could_not_set_default_modes_for") + this.getName() + ".");
        }
    }
    
    public void resetModes(final String[] modes, final boolean notify) {
        if (modes.length > 0) {
            this.toggleMode(modes, false);
        }
        if (notify) {
            TownyMessaging.sendMsg(this, TownySettings.getLangString("msg_modes_set") + StringMgmt.join(this.getModes(), ","));
        }
    }
    
    public boolean addTownRank(final String rank) throws AlreadyRegisteredException {
        if (!this.hasTown() || !TownyPerms.getTownRanks().contains(rank)) {
            return false;
        }
        if (this.townRanks.contains(rank)) {
            throw new AlreadyRegisteredException();
        }
        this.townRanks.add(rank);
        if (BukkitTools.isOnline(this.getName())) {
            TownyPerms.assignPermissions(this, null);
        }
        BukkitTools.getPluginManager().callEvent((Event)new TownAddResidentRankEvent(this, rank, this.town));
        return true;
    }
    
    public void setTownRanks(final List<String> ranks) {
        this.townRanks.addAll(ranks);
    }
    
    public boolean hasTownRank(final String rank) {
        return this.townRanks.contains(rank.toLowerCase());
    }
    
    public List<String> getTownRanks() {
        return this.townRanks;
    }
    
    public boolean removeTownRank(final String rank) throws NotRegisteredException {
        if (TownySettings.getWarSiegeEnabled()) {
            SiegeWarRankController.evaluateTownRemoveRank(this, rank);
        }
        if (this.townRanks.contains(rank)) {
            this.townRanks.remove(rank);
            if (BukkitTools.isOnline(this.getName())) {
                TownyPerms.assignPermissions(this, null);
            }
            BukkitTools.getPluginManager().callEvent((Event)new TownRemoveResidentRankEvent(this, rank, this.town));
            return true;
        }
        throw new NotRegisteredException();
    }
    
    public boolean addNationRank(final String rank) throws AlreadyRegisteredException {
        if (!this.hasNation() || !TownyPerms.getNationRanks().contains(rank)) {
            return false;
        }
        if (this.nationRanks.contains(rank)) {
            throw new AlreadyRegisteredException();
        }
        this.nationRanks.add(rank);
        if (BukkitTools.isOnline(this.getName())) {
            TownyPerms.assignPermissions(this, null);
        }
        return true;
    }
    
    public void setNationRanks(final List<String> ranks) {
        this.nationRanks.addAll(ranks);
    }
    
    public boolean hasNationRank(final String rank) {
        return this.nationRanks.contains(rank.toLowerCase());
    }
    
    public List<String> getNationRanks() {
        return this.nationRanks;
    }
    
    public boolean removeNationRank(final String rank) throws NotRegisteredException {
        if (TownySettings.getWarSiegeEnabled()) {
            SiegeWarRankController.evaluateNationRemoveRank(this, rank);
        }
        if (this.nationRanks.contains(rank)) {
            this.nationRanks.remove(rank);
            if (BukkitTools.isOnline(this.getName())) {
                TownyPerms.assignPermissions(this, null);
            }
            return true;
        }
        throw new NotRegisteredException();
    }
    
    public boolean isAlliedWith(final Resident otherresident) {
        if (this.hasNation() && this.hasTown() && otherresident.hasTown() && otherresident.hasNation()) {
            try {
                return this.getTown().getNation().hasAlly(otherresident.getTown().getNation()) || this.getTown().getNation().equals(otherresident.getTown().getNation());
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
        throw new TooManyInvitesException(String.format(TownySettings.getLangString("msg_err_player_has_too_many_invites"), this.getName()));
    }
    
    @Override
    public void deleteReceivedInvite(final Invite invite) {
        this.receivedinvites.remove(invite);
    }
    
    public void setConfirmationType(final ConfirmationType confirmationType) {
        this.confirmationType = confirmationType;
    }
    
    public ConfirmationType getConfirmationType() {
        return this.confirmationType;
    }
    
    @Override
    public void addMetaData(final CustomDataField md) {
        super.addMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveResident(this);
    }
    
    @Override
    public void removeMetaData(final CustomDataField md) {
        super.removeMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveResident(this);
    }
    
    @Override
    public EconomyAccount getAccount() {
        if (this.account == null) {
            final String accountName = StringMgmt.trimMaxLength(this.getName(), 32);
            final Player player = BukkitTools.getPlayer(this.getName());
            World world;
            if (player != null) {
                world = player.getWorld();
            }
            else {
                world = BukkitTools.getWorlds().get(0);
            }
            this.account = new EconomyAccount(accountName, world);
        }
        return this.account;
    }
    
    @Override
    public void setTownblocks(final List<TownBlock> townBlocks) {
        this.townBlocks = townBlocks;
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
    }
    
    @Override
    public void removeTownBlock(final TownBlock townBlock) throws NotRegisteredException {
        if (!this.hasTownBlock(townBlock)) {
            throw new NotRegisteredException();
        }
        this.townBlocks.remove(townBlock);
    }
    
    @Override
    public void setPermissions(final String line) {
        this.permissions.load(line);
    }
    
    @Override
    public TownyPermission getPermissions() {
        return this.permissions;
    }
    
    @Deprecated
    public World getBukkitWorld() {
        final Player player = BukkitTools.getPlayer(this.getName());
        if (player != null) {
            return player.getWorld();
        }
        return BukkitTools.getWorlds().get(0);
    }
}
