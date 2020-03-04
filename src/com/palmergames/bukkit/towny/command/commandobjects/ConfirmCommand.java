// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command.commandobjects;

import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class ConfirmCommand extends BukkitCommand
{
    public ConfirmCommand(final String name) {
        super(name);
        this.description = "Confirm command for Towny";
        this.usageMessage = "/" + name;
    }
    
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        if (commandSender instanceof Player) {
            final Player player = (Player)commandSender;
            Resident resident;
            try {
                resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            }
            catch (TownyException e) {
                return true;
            }
            if (resident != null) {
                if (resident.getConfirmationType() != null) {
                    try {
                        ConfirmationHandler.handleConfirmation(resident, resident.getConfirmationType());
                        return true;
                    }
                    catch (TownyException e) {
                        TownyMessaging.sendErrorMsg(player, e.getMessage());
                        return true;
                    }
                }
                TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("no_confirmations_open"));
                return true;
            }
            return true;
        }
        else {
            if (!ConfirmationHandler.consoleConfirmationType.equals(ConfirmationType.NULL)) {
                try {
                    ConfirmationHandler.handleConfirmation(ConfirmationHandler.consoleConfirmationType);
                }
                catch (TownyException e2) {
                    TownyMessaging.sendErrorMsg(Bukkit.getConsoleSender(), e2.getMessage());
                    return true;
                }
                return true;
            }
            TownyMessaging.sendMsg(TownySettings.getLangString("no_confirmations_open"));
            return true;
        }
    }
}
