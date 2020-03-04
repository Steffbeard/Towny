// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.eventwar;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.entity.Player;

public class WarUtil
{
    public static boolean isPlayerNeutral(final Player player) {
        if (TownyAPI.getInstance().isWarTime()) {
            try {
                final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
                if (resident.isJailed()) {
                    return true;
                }
                if (resident.hasTown() && !War.isWarringTown(resident.getTown())) {
                    return true;
                }
            }
            catch (NotRegisteredException ex) {}
        }
        return false;
    }
}
