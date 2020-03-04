// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.chat.types;

import java.util.Iterator;
import java.util.UUID;
import java.util.HashSet;
import java.util.Collection;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import net.tnemc.tnc.core.common.chat.ChatType;

public class TownType extends ChatType
{
    public TownType() {
        super("town", "<aqua>$display: <white>$message");
    }
    
    public boolean canChat(final Player player) {
        try {
            return TownyUniverse.getInstance().getDataSource().getResident(player.getName()).hasTown();
        }
        catch (NotRegisteredException ex) {
            return false;
        }
    }
    
    public Collection<Player> getRecipients(final Collection<Player> recipients, final Player player) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final UUID town = townyUniverse.getDataSource().getResident(player.getName()).getTown().getUuid();
            final Collection<Player> newRecipients = new HashSet<Player>();
            for (final Player p : recipients) {
                if (townyUniverse.getDataSource().getResident(p.getName()).getTown().getUuid().equals(town)) {
                    newRecipients.add(p);
                }
            }
            return newRecipients;
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
            return recipients;
        }
    }
}
