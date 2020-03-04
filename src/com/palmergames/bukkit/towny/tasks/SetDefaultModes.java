// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.TimerTask;

public class SetDefaultModes extends TimerTask
{
    protected String name;
    protected boolean notify;
    
    public SetDefaultModes(final String name, final boolean notify) {
        this.name = name;
        this.notify = notify;
    }
    
    @Override
    public void run() {
        if (!BukkitTools.isOnline(this.name)) {
            return;
        }
        try {
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            final String modeString = townyUniverse.getPermissionSource().getPlayerPermissionStringNode(this.name, PermissionNodes.TOWNY_DEFAULT_MODES.getNode());
            String[] modes = new String[0];
            if (!modeString.isEmpty()) {
                modes = modeString.split(",");
            }
            try {
                townyUniverse.getDataSource().getResident(this.name).resetModes(modes, this.notify);
            }
            catch (NotRegisteredException ex) {}
        }
        catch (NullPointerException ex2) {}
    }
}
