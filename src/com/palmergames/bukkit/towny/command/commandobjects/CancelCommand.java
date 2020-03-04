// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command.commandobjects;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.confirmations.ConfirmationType;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class CancelCommand extends BukkitCommand
{
    public CancelCommand(final String name) {
        super(name);
        this.description = "Cancel command for Towny";
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
            if (resident == null) {
                return true;
            }
            if (resident.getConfirmationType() != null) {
                ConfirmationHandler.removeConfirmation(resident, resident.getConfirmationType(), false);
                return true;
            }
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("no_confirmations_open"));
            return true;
        }
        else {
            if (!ConfirmationHandler.consoleConfirmationType.equals(ConfirmationType.NULL)) {
                ConfirmationHandler.removeConfirmation(ConfirmationHandler.consoleConfirmationType, false);
                return true;
            }
            TownyMessaging.sendMsg(TownySettings.getLangString("no_confirmations_open"));
            return true;
        }
    }
}
