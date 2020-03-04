// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import org.bukkit.Bukkit;
import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;

public class OnPlayerLogin implements Runnable
{
    Towny plugin;
    TownyUniverse universe;
    volatile Player player;
    
    public OnPlayerLogin(final Towny plugin, final Player player) {
        this.plugin = plugin;
        this.universe = TownyUniverse.getInstance();
        this.player = player;
    }
    
    @Override
    public void run() {
        Resident resident = null;
        if (!this.universe.getDataSource().hasResident(this.player.getName())) {
            try {
                this.universe.getDataSource().newResident(this.player.getName());
                resident = this.universe.getDataSource().getResident(this.player.getName());
                if (TownySettings.isShowingRegistrationMessage()) {
                    TownyMessaging.sendMessage(this.player, TownySettings.getRegistrationMsg(this.player.getName()));
                }
                resident.setRegistered(System.currentTimeMillis());
                if (!TownySettings.getDefaultTownName().equals("")) {
                    try {
                        final Town town = TownyUniverse.getInstance().getDataSource().getTown(TownySettings.getDefaultTownName());
                        town.addResident(resident);
                        this.universe.getDataSource().saveTown(town);
                    }
                    catch (NotRegisteredException ex) {}
                    catch (AlreadyRegisteredException ex2) {}
                }
                this.universe.getDataSource().saveResident(resident);
                this.universe.getDataSource().saveResidentList();
            }
            catch (AlreadyRegisteredException | NotRegisteredException ex3) {}
        }
        else {
            try {
                resident = this.universe.getDataSource().getResident(this.player.getName());
                if (TownySettings.isUsingEssentials()) {
                    final Essentials ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
                    if (!ess.getUser(this.player).isVanished()) {
                        resident.setLastOnline(System.currentTimeMillis());
                    }
                }
                else {
                    resident.setLastOnline(System.currentTimeMillis());
                }
                this.universe.getDataSource().saveResident(resident);
            }
            catch (NotRegisteredException ex4) {}
        }
        if (resident != null) {
            TownyPerms.assignPermissions(resident, this.player);
        }
        try {
            if (TownySettings.getShowTownBoardOnLogin()) {
                TownyMessaging.sendTownBoard(this.player, resident.getTown());
            }
            if (TownySettings.getShowNationBoardOnLogin() && resident.getTown().hasNation()) {
                TownyMessaging.sendNationBoard(this.player, resident.getTown().getNation());
            }
            resident.getTown();
        }
        catch (NotRegisteredException ex5) {}
        if (TownyAPI.getInstance().isWarTime()) {
            this.universe.getWarEvent().sendScores(this.player, 3);
        }
        if (BukkitTools.scheduleSyncDelayedTask(new SetDefaultModes(this.player.getName(), false), 1L) == -1) {
            TownyMessaging.sendErrorMsg("Could not set default modes for " + this.player.getName() + ".");
        }
        this.warningMessage(resident);
    }
    
    private void warningMessage(final Resident resident) {
        if (TownyEconomyHandler.isActive() && TownySettings.isTaxingDaily() && resident.hasTown()) {
            try {
                final Town town = resident.getTown();
                if (town.hasUpkeep()) {
                    final double upkeep = TownySettings.getTownUpkeepCost(town);
                    try {
                        if (upkeep > 0.0 && !town.getAccount().canPayFromHoldings(upkeep)) {
                            TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_warning_delete"), town.getName()));
                        }
                    }
                    catch (EconomyException ex) {}
                }
                if (town.hasNation()) {
                    final Nation nation = town.getNation();
                    final double upkeep2 = TownySettings.getNationUpkeepCost(nation);
                    try {
                        if (upkeep2 > 0.0 && !nation.getAccount().canPayFromHoldings(upkeep2)) {
                            TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_warning_delete"), nation.getName()));
                        }
                    }
                    catch (EconomyException ex2) {}
                }
            }
            catch (NotRegisteredException ex3) {}
        }
    }
}
