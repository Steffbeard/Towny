// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.util.BukkitTools;
import java.util.Collection;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.object.EconomyHandler;
import com.palmergames.bukkit.towny.object.TownBlockType;
import java.util.Iterator;
import java.util.Arrays;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.List;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;

public class PlotClaim extends Thread
{
    Towny plugin;
    private volatile Player player;
    private volatile Resident resident;
    private volatile TownyWorld world;
    private List<WorldCoord> selection;
    private boolean claim;
    private boolean admin;
    private boolean groupClaim;
    
    public PlotClaim(final Towny plugin, final Player player, final Resident resident, final List<WorldCoord> selection, final boolean claim, final boolean admin, final boolean groupClaim) {
        this.plugin = plugin;
        this.player = player;
        this.resident = resident;
        this.selection = selection;
        this.claim = claim;
        this.admin = admin;
        this.groupClaim = groupClaim;
        this.setPriority(1);
    }
    
    @Override
    public void run() {
        int claimed = 0;
        if (this.player != null) {
            if (this.claim) {
                TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_process_claim"));
            }
            else {
                TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_process_unclaim"));
            }
        }
        if (this.selection != null) {
            for (final WorldCoord worldCoord : this.selection) {
                try {
                    if (worldCoord.getTownBlock().hasPlotObjectGroup() && this.residentGroupClaim(this.selection)) {
                        ++claimed;
                        worldCoord.getTownBlock().getPlotObjectGroup().setResident(this.resident);
                        worldCoord.getTownBlock().getPlotObjectGroup().setPrice(-1.0);
                        TownyMessaging.sendPrefixedTownMessage(worldCoord.getTownBlock().getTown(), String.format(TownySettings.getLangString("msg_player_successfully_bought_group_x"), this.player.getName(), worldCoord.getTownBlock().getPlotObjectGroup().getGroupName()));
                        TownyUniverse.getInstance().getDataSource().savePlotGroup(worldCoord.getTownBlock().getPlotObjectGroup());
                        break;
                    }
                }
                catch (Exception e) {
                    TownyMessaging.sendErrorMsg(this.player, e.getMessage());
                }
                try {
                    this.world = worldCoord.getTownyWorld();
                }
                catch (NotRegisteredException e3) {
                    TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_err_not_configured"));
                    continue;
                }
                try {
                    if (this.claim) {
                        if (this.groupClaim) {
                            continue;
                        }
                        if (!this.admin) {
                            if (!this.residentClaim(worldCoord)) {
                                continue;
                            }
                            ++claimed;
                        }
                        else {
                            this.adminClaim(worldCoord);
                            ++claimed;
                        }
                    }
                    else {
                        this.residentUnclaim(worldCoord);
                    }
                }
                catch (EconomyException e2) {
                    TownyMessaging.sendErrorMsg(this.player, e2.getError());
                }
                catch (TownyException x) {
                    TownyMessaging.sendErrorMsg(this.player, x.getMessage());
                }
            }
        }
        else if (!this.claim) {
            this.residentUnclaimAll();
        }
        if (this.player != null) {
            if (this.claim) {
                if (this.selection != null && this.selection.size() > 0 && claimed > 0) {
                    TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_claimed") + ((this.selection.size() > 5) ? (TownySettings.getLangString("msg_total_townblocks") + this.selection.size()) : Arrays.toString(this.selection.toArray(new WorldCoord[0]))));
                }
                else {
                    TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_not_claimed_1"));
                }
            }
            else if (this.selection != null) {
                TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_unclaimed") + ((this.selection.size() > 5) ? (TownySettings.getLangString("msg_total_townblocks") + this.selection.size()) : Arrays.toString(this.selection.toArray(new WorldCoord[0]))));
            }
            else {
                TownyMessaging.sendMsg(this.player, TownySettings.getLangString("msg_unclaimed"));
            }
        }
        TownyUniverse.getInstance().getDataSource().saveResident(this.resident);
        this.plugin.resetCache();
    }
    
    private boolean residentGroupClaim(final List<WorldCoord> worldCoords) throws TownyException, EconomyException {
        for (int i = 0; i < worldCoords.size(); ++i) {
            final WorldCoord worldCoord = worldCoords.get(i);
            try {
                final TownBlock townBlock = worldCoord.getTownBlock();
                final Town town = townBlock.getTown();
                final PlotObjectGroup group = townBlock.getPlotObjectGroup();
                if ((this.resident.hasTown() && this.resident.getTown() != town && !townBlock.getType().equals(TownBlockType.EMBASSY)) || (!this.resident.hasTown() && !townBlock.getType().equals(TownBlockType.EMBASSY))) {
                    throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
                }
                final TownyUniverse townyUniverse = TownyUniverse.getInstance();
                try {
                    final Resident owner = townBlock.getPlotObjectGroup().getResident();
                    if (group.getPrice() != -1.0) {
                        if (TownySettings.isUsingEconomy() && !this.resident.getAccount().payTo(group.getPrice(), owner, "Plot Group - Buy From Seller")) {
                            throw new TownyException(TownySettings.getLangString("msg_no_money_purchase_plot"));
                        }
                        int maxPlots = TownySettings.getMaxResidentPlots(this.resident);
                        final int extraPlots = TownySettings.getMaxResidentExtraPlots(this.resident);
                        if (maxPlots != -1) {
                            maxPlots += extraPlots;
                        }
                        if (maxPlots >= 0 && this.resident.getTownBlocks().size() + group.getTownBlocks().size() > maxPlots) {
                            throw new TownyException(String.format(TownySettings.getLangString("msg_max_plot_own"), maxPlots));
                        }
                        TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getBuyResidentPlotMsg(this.resident.getName(), owner.getName(), townBlock.getPlotObjectGroup().getPrice()));
                        townBlock.setResident(this.resident);
                        townyUniverse.getDataSource().saveResident(owner);
                        townyUniverse.getDataSource().savePlotGroup(group);
                        townyUniverse.getDataSource().saveTownBlock(townBlock);
                        if (i >= worldCoords.size() - 2) {
                            TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_successfully_bought_group_x"), this.resident.getName(), group.getGroupName()));
                        }
                        this.plugin.updateCache(worldCoord);
                    }
                    else {
                        if (!this.player.hasPermission(PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode())) {
                            throw new AlreadyRegisteredException(String.format(TownySettings.getLangString("msg_already_claimed"), owner.getName()));
                        }
                        if (TownySettings.isUsingEconomy() && !town.getAccount().payTo(0.0, owner, "Plot - Buy Back")) {
                            throw new TownyException(TownySettings.getLangString("msg_town_no_money_purchase_plot"));
                        }
                        TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getBuyResidentPlotMsg(town.getName(), owner.getName(), 0.0));
                        townBlock.setResident(this.resident);
                        townyUniverse.getDataSource().saveResident(owner);
                        townyUniverse.getDataSource().savePlotGroup(group);
                        townyUniverse.getDataSource().saveTownBlock(townBlock);
                    }
                }
                catch (NotRegisteredException e) {
                    if (townBlock.getPlotObjectGroup().getPrice() == -1.0) {
                        throw new TownyException(TownySettings.getLangString("msg_err_plot_nfs"));
                    }
                    final double bankcap = TownySettings.getTownBankCap();
                    if (bankcap > 0.0 && townBlock.getPlotPrice() + town.getAccount().getHoldingBalance() > bankcap) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
                    }
                    if (TownySettings.isUsingEconomy() && !this.resident.getAccount().payTo(townBlock.getPlotObjectGroup().getPrice(), town, "Plot - Buy From Town")) {
                        throw new TownyException(TownySettings.getLangString("msg_no_money_purchase_plot"));
                    }
                    townBlock.setResident(this.resident);
                    townBlock.setType(townBlock.getType());
                    townyUniverse.getDataSource().saveTownBlock(townBlock);
                }
            }
            catch (NotRegisteredException e2) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
            }
        }
        return true;
    }
    
    private boolean residentClaim(final WorldCoord worldCoord) throws TownyException, EconomyException {
        try {
            final TownBlock townBlock = worldCoord.getTownBlock();
            final Town town = townBlock.getTown();
            if ((this.resident.hasTown() && this.resident.getTown() != town && !townBlock.getType().equals(TownBlockType.EMBASSY)) || (!this.resident.hasTown() && !townBlock.getType().equals(TownBlockType.EMBASSY))) {
                throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
            }
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            try {
                final Resident owner = townBlock.getResident();
                if (townBlock.getPlotPrice() != -1.0) {
                    if (TownySettings.isUsingEconomy() && !this.resident.getAccount().payTo(townBlock.getPlotPrice(), owner, "Plot - Buy From Seller")) {
                        throw new TownyException(TownySettings.getLangString("msg_no_money_purchase_plot"));
                    }
                    int maxPlots = TownySettings.getMaxResidentPlots(this.resident);
                    final int extraPlots = TownySettings.getMaxResidentExtraPlots(this.resident);
                    if (maxPlots != -1) {
                        maxPlots += extraPlots;
                    }
                    if (maxPlots >= 0 && this.resident.getTownBlocks().size() + 1 > maxPlots) {
                        throw new TownyException(String.format(TownySettings.getLangString("msg_max_plot_own"), maxPlots));
                    }
                    TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getBuyResidentPlotMsg(this.resident.getName(), owner.getName(), townBlock.getPlotPrice()));
                    townBlock.setPlotPrice(-1.0);
                    townBlock.setResident(this.resident);
                    townBlock.setType(townBlock.getType());
                    townyUniverse.getDataSource().saveResident(owner);
                    townyUniverse.getDataSource().saveTownBlock(townBlock);
                    this.plugin.updateCache(worldCoord);
                    return true;
                }
                else {
                    if (!this.player.hasPermission(PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR.getNode())) {
                        throw new AlreadyRegisteredException(String.format(TownySettings.getLangString("msg_already_claimed"), owner.getName()));
                    }
                    if (TownySettings.isUsingEconomy() && !town.getAccount().payTo(0.0, owner, "Plot - Buy Back")) {
                        throw new TownyException(TownySettings.getLangString("msg_town_no_money_purchase_plot"));
                    }
                    TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getBuyResidentPlotMsg(town.getName(), owner.getName(), 0.0));
                    townBlock.setResident(null);
                    townBlock.setPlotPrice(-1.0);
                    townBlock.setType(townBlock.getType());
                    townyUniverse.getDataSource().saveResident(owner);
                    townyUniverse.getDataSource().saveTownBlock(townBlock);
                    return true;
                }
            }
            catch (NotRegisteredException e) {
                if (townBlock.getPlotPrice() == -1.0) {
                    throw new TownyException(TownySettings.getLangString("msg_err_plot_nfs"));
                }
                final double bankcap = TownySettings.getTownBankCap();
                if (bankcap > 0.0 && townBlock.getPlotPrice() + town.getAccount().getHoldingBalance() > bankcap) {
                    throw new TownyException(String.format(TownySettings.getLangString("msg_err_deposit_capped"), bankcap));
                }
                if (TownySettings.isUsingEconomy() && !this.resident.getAccount().payTo(townBlock.getPlotPrice(), town, "Plot - Buy From Town")) {
                    throw new TownyException(TownySettings.getLangString("msg_no_money_purchase_plot"));
                }
                townBlock.setPlotPrice(-1.0);
                townBlock.setResident(this.resident);
                townBlock.setType(townBlock.getType());
                townyUniverse.getDataSource().saveTownBlock(townBlock);
                return true;
            }
        }
        catch (NotRegisteredException e2) {
            throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
        }
    }
    
    private boolean residentUnclaim(final WorldCoord worldCoord) throws TownyException {
        try {
            final TownBlock townBlock = worldCoord.getTownBlock();
            townBlock.setResident(null);
            townBlock.setPlotPrice(townBlock.getTown().getPlotTypePrice(townBlock.getType()));
            townBlock.setType(townBlock.getType());
            TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
            this.plugin.updateCache(worldCoord);
        }
        catch (NotRegisteredException e) {
            throw new TownyException(TownySettings.getLangString("msg_not_own_place"));
        }
        return true;
    }
    
    private void residentUnclaimAll() {
        final List<TownBlock> selection = new ArrayList<TownBlock>(this.resident.getTownBlocks());
        for (final TownBlock townBlock : selection) {
            try {
                this.residentUnclaim(townBlock.getWorldCoord());
            }
            catch (TownyException e) {
                TownyMessaging.sendErrorMsg(this.player, e.getMessage());
            }
        }
    }
    
    private void adminClaim(final WorldCoord worldCoord) throws TownyException {
        try {
            final TownBlock townBlock = worldCoord.getTownBlock();
            final Town town = townBlock.getTown();
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            townBlock.setPlotPrice(-1.0);
            townBlock.setResident(this.resident);
            townBlock.setType(townBlock.getType());
            townyUniverse.getDataSource().saveTownBlock(townBlock);
            TownyMessaging.sendMessage(BukkitTools.getPlayer(this.resident.getName()), String.format(TownySettings.getLangString("msg_admin_has_given_you_a_plot"), worldCoord.toString()));
        }
        catch (NotRegisteredException e) {
            throw new TownyException(TownySettings.getLangString("msg_not_claimed_1"));
        }
    }
}
