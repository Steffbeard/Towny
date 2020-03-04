// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.Iterator;
import java.util.Arrays;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.List;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;

public class TownClaim extends Thread
{
    Towny plugin;
    private volatile Player player;
    private Location outpostLocation;
    private volatile Town town;
    private List<WorldCoord> selection;
    private boolean outpost;
    private boolean claim;
    private boolean forced;
    
    public TownClaim(final Towny plugin, final Player player, final Town town, final List<WorldCoord> selection, final boolean isOutpost, final boolean claim, final boolean forced) {
        this.plugin = plugin;
        this.player = player;
        if (this.player != null) {
            this.outpostLocation = player.getLocation();
        }
        this.town = town;
        this.selection = selection;
        this.outpost = isOutpost;
        this.claim = claim;
        this.forced = forced;
        this.setPriority(1);
    }
    
    @Override
    public void run() {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<TownyWorld> worlds = new ArrayList<TownyWorld>();
        final List<Town> towns = new ArrayList<Town>();
        if (this.player != null) {
            TownyMessaging.sendMsg(this.player, "Processing " + (this.claim ? "Town Claim..." : "Town unclaim..."));
        }
        if (this.selection != null) {
            for (final WorldCoord worldCoord : this.selection) {
                try {
                    final TownyWorld world = worldCoord.getTownyWorld();
                    if (!worlds.contains(world)) {
                        worlds.add(world);
                    }
                    if (this.claim) {
                        this.townClaim(this.town, worldCoord, this.outpost);
                        this.outpost = false;
                    }
                    else {
                        this.townUnclaim(this.town = worldCoord.getTownBlock().getTown(), worldCoord, this.forced);
                    }
                    if (towns.contains(this.town)) {
                        continue;
                    }
                    towns.add(this.town);
                }
                catch (NotRegisteredException e4) {
                    TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_err_not_configured"));
                }
                catch (TownyException x) {
                    TownyMessaging.sendErrorMsg(this.player, x.getMessage());
                }
            }
            if (!this.claim && TownySettings.getClaimRefundPrice() > 0.0) {
                try {
                    this.town.getAccount().collect(TownySettings.getClaimRefundPrice() * this.selection.size(), "Town Unclaim Refund");
                    TownyMessaging.sendMsg(this.player, String.format(TownySettings.getLangString("refund_message"), TownySettings.getClaimRefundPrice() * this.selection.size(), this.selection.size()));
                }
                catch (EconomyException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (!this.claim) {
            if (this.town == null) {
                TownyMessaging.sendMsg(this.player, "Nothing to unclaim!");
                return;
            }
            Resident resident = null;
            try {
                resident = townyUniverse.getDataSource().getResident(this.player.getName());
            }
            catch (TownyException ex) {}
            if (resident == null) {
                return;
            }
            final int townSize = this.town.getTownBlocks().size();
            try {
                ConfirmationHandler.addConfirmation(resident, ConfirmationType.UNCLAIM_ALL, null);
                TownyMessaging.sendConfirmationMessage((CommandSender)this.player, null, null, null, null);
            }
            catch (TownyException e2) {
                e2.printStackTrace();
            }
            if (TownySettings.getClaimRefundPrice() > 0.0) {
                try {
                    this.town.getAccount().collect(TownySettings.getClaimRefundPrice() * townSize, "Town Unclaim Refund");
                    TownyMessaging.sendMsg(this.player, String.format(TownySettings.getLangString("refund_message"), TownySettings.getClaimRefundPrice() * townSize, townSize));
                }
                catch (EconomyException e3) {
                    e3.printStackTrace();
                }
            }
        }
        if (!towns.isEmpty()) {
            for (final Town test : towns) {
                townyUniverse.getDataSource().saveTown(test);
            }
        }
        if (!worlds.isEmpty()) {
            for (final TownyWorld test2 : worlds) {
                townyUniverse.getDataSource().saveWorld(test2);
            }
        }
        this.plugin.resetCache();
        if (this.player != null) {
            if (this.claim) {
                TownyMessaging.sendMsg(this.player, String.format(TownySettings.getLangString("msg_annexed_area"), (this.selection.size() > 5) ? ("Total TownBlocks: " + this.selection.size()) : Arrays.toString(this.selection.toArray(new WorldCoord[0]))));
                if (this.town.getWorld().isUsingPlotManagementRevert()) {
                    TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_wait_locked"));
                }
            }
            else if (this.forced) {
                TownyMessaging.sendMsg(this.player, String.format(TownySettings.getLangString("msg_admin_unclaim_area"), (this.selection.size() > 5) ? ("Total TownBlocks: " + this.selection.size()) : Arrays.toString(this.selection.toArray(new WorldCoord[0]))));
                if (this.town != null && this.town.getWorld().isUsingPlotManagementRevert()) {
                    TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_wait_locked"));
                }
            }
        }
    }
    
    private void townClaim(final Town town, final WorldCoord worldCoord, final boolean isOutpost) throws TownyException {
        try {
            final TownBlock townBlock = worldCoord.getTownBlock();
            try {
                throw new AlreadyRegisteredException(String.format(TownySettings.getLangString("msg_already_claimed"), townBlock.getTown().getName()));
            }
            catch (NotRegisteredException e) {
                throw new AlreadyRegisteredException(TownySettings.getLangString("msg_already_claimed_2"));
            }
        }
        catch (NotRegisteredException e2) {
            final TownBlock townBlock2 = worldCoord.getTownyWorld().newTownBlock(worldCoord);
            townBlock2.setTown(town);
            if (!town.hasHomeBlock()) {
                town.setHomeBlock(townBlock2);
            }
            townBlock2.setType(townBlock2.getType());
            if (isOutpost) {
                townBlock2.setOutpost(true);
                town.addOutpostSpawn(this.outpostLocation);
            }
            if (town.getWorld().isUsingPlotManagementRevert() && TownySettings.getPlotManagementSpeed() > 0L) {
                final PlotBlockData plotChunk = TownyRegenAPI.getPlotChunk(townBlock2);
                if (plotChunk != null) {
                    TownyRegenAPI.deletePlotChunk(plotChunk);
                    townBlock2.setLocked(false);
                }
                else {
                    TownyRegenAPI.addWorldCoord(townBlock2.getWorldCoord());
                    townBlock2.setLocked(true);
                }
            }
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            townyUniverse.getDataSource().saveTownBlock(townBlock2);
            townyUniverse.getDataSource().saveTownBlockList();
            BukkitTools.getPluginManager().callEvent((Event)new TownClaimEvent(townBlock2));
        }
    }
    
    private void townUnclaim(final Town town, final WorldCoord worldCoord, final boolean force) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final TownBlock townBlock = worldCoord.getTownBlock();
            if (town != townBlock.getTown() && !force) {
                throw new TownyException(TownySettings.getLangString("msg_area_not_own"));
            }
            if (!townBlock.isOutpost() && townBlock.hasTown() && townyUniverse.isTownBlockLocContainedInTownOutposts(townBlock.getTown().getAllOutpostSpawns(), townBlock)) {
                townBlock.setOutpost(true);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, () -> townyUniverse.getDataSource().removeTownBlock(townBlock), 1L);
        }
        catch (NotRegisteredException e) {
            throw new TownyException(TownySettings.getLangString("msg_not_claimed_1"));
        }
    }
    
    public static void townUnclaimAll(final Towny plugin, final Town town) {
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)plugin, () -> {
            TownyUniverse.getInstance().getDataSource().removeTownBlocks(town);
            TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getLangString("msg_abandoned_area_1"));
        }, 1L);
    }
}
