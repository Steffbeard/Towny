// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.chat.checks;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import net.tnemc.tnc.core.common.chat.ChatCheck;

public class KingCheck extends ChatCheck
{
    public String name() {
        return "isking";
    }
    
    public boolean runCheck(final Player player, final String checkString) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            if (townyUniverse.getDataSource().getResident(player.getName()).hasNation()) {
                return townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation().isKing(townyUniverse.getDataSource().getResident(player.getName()));
            }
        }
        catch (NotRegisteredException ex) {}
        return false;
    }
}
