// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.chat.types;

import java.util.Iterator;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.HashSet;
import java.util.Collection;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import net.tnemc.tnc.core.common.chat.ChatType;

public class AllyType extends ChatType
{
    public AllyType() {
        super("ally", "<gray>[<aqua>$nation<gray>]: <white>$message");
    }
    
    public boolean canChat(final Player player) {
        try {
            return TownyUniverse.getInstance().getDataSource().getResident(player.getName()).hasTown() && TownyUniverse.getInstance().getDataSource().getResident(player.getName()).getTown().hasNation();
        }
        catch (NotRegisteredException ex) {
            return false;
        }
    }
    
    public Collection<Player> getRecipients(final Collection<Player> recipients, final Player player) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final Nation nation = townyUniverse.getDataSource().getResident(player.getName()).getTown().getNation();
            final Collection<Player> newRecipients = new HashSet<Player>();
            for (final Player p : recipients) {
                if (!townyUniverse.getDataSource().getResident(p.getName()).getTown().getNation().getUuid().equals(nation.getUuid()) && !townyUniverse.getDataSource().getResident(p.getName()).getTown().getNation().hasAlly(nation)) {
                    continue;
                }
                newRecipients.add(p);
            }
            return newRecipients;
        }
        catch (NotRegisteredException ex) {
            return recipients;
        }
    }
}
