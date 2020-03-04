// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import java.util.Iterator;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.List;
import com.palmergames.bukkit.towny.object.ResidentList;
import org.bukkit.entity.Player;

public class ResidentUtil
{
    public static List<Resident> getOnlineResidentsViewable(final Player viewer, final ResidentList residentList) {
        final List<Resident> onlineResidents = new ArrayList<Resident>();
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                for (final Resident resident : residentList.getResidents()) {
                    if (resident.getName().equalsIgnoreCase(player.getName()) && (viewer == null || viewer.canSee(BukkitTools.getPlayerExact(resident.getName())))) {
                        onlineResidents.add(resident);
                    }
                }
            }
        }
        return onlineResidents;
    }
    
    public static List<Resident> getValidatedResidents(final Object sender, final String[] names) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final List<Resident> residents = new ArrayList<Resident>();
        for (final String name : names) {
            final List<Player> matches = BukkitTools.matchPlayer(name);
            if (matches.size() > 1) {
                final StringBuilder line = new StringBuilder("Multiple players selected: ");
                for (final Player p : matches) {
                    line.append(", ").append(p.getName());
                }
                TownyMessaging.sendErrorMsg(sender, line.toString());
            }
            else if (matches.size() == 1) {
                try {
                    final Resident target = townyUniverse.getDataSource().getResident(matches.get(0).getName());
                    residents.add(target);
                }
                catch (TownyException x) {
                    TownyMessaging.sendErrorMsg(sender, x.getMessage());
                }
            }
            else {
                try {
                    final Resident target = townyUniverse.getDataSource().getResident(name);
                    residents.add(target);
                }
                catch (NotRegisteredException x2) {
                    TownyMessaging.sendErrorMsg(sender, x2.getMessage());
                }
            }
        }
        return residents;
    }
}
