// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.chat.variables;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import net.tnemc.tnc.core.common.chat.ChatVariable;

public class NationVariable extends ChatVariable
{
    public String name() {
        return "$nation";
    }
    
    public String parse(final Player player, final String message) {
        try {
            return TownyUniverse.getInstance().getDataSource().getResident(player.getName()).getTown().getNation().getName();
        }
        catch (NotRegisteredException ignore) {
            return "";
        }
    }
}
