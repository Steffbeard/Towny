// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.TownyMessaging;
import java.util.Iterator;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.Collection;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.Towny;

public class ResidentPurge extends Thread
{
    Towny plugin;
    private CommandSender sender;
    long deleteTime;
    boolean townless;
    
    public ResidentPurge(final Towny plugin, final CommandSender sender, final long deleteTime, final boolean townless) {
        this.sender = null;
        this.plugin = plugin;
        this.deleteTime = deleteTime;
        this.setPriority(5);
        this.townless = townless;
    }
    
    @Override
    public void run() {
        int count = 0;
        this.message("Scanning for old residents...");
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        for (final Resident resident : new ArrayList<Resident>(townyUniverse.getDataSource().getResidents())) {
            if (!resident.isNPC() && System.currentTimeMillis() - resident.getLastOnline() > this.deleteTime && !BukkitTools.isOnline(resident.getName())) {
                if (this.townless && resident.hasTown()) {
                    continue;
                }
                ++count;
                this.message("Deleting resident: " + resident.getName());
                townyUniverse.getDataSource().removeResident(resident);
                townyUniverse.getDataSource().removeResidentList(resident);
            }
        }
        this.message("Resident purge complete: " + count + " deleted.");
    }
    
    private void message(final String msg) {
        if (this.sender != null) {
            TownyMessaging.sendMessage(this.sender, msg);
        }
        else {
            TownyMessaging.sendMsg(msg);
        }
    }
}
