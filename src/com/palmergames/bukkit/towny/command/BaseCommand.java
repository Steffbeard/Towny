// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.TownyUniverse;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class BaseCommand implements TabCompleter
{
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final LinkedList<String> output = new LinkedList<String>();
        String lastArg = "";
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (args.length > 0) {
            lastArg = args[args.length - 1].toLowerCase();
        }
        if (!lastArg.equalsIgnoreCase("")) {
            for (final Nation nation : townyUniverse.getDataSource().getNations()) {
                if (nation.getName().toLowerCase().startsWith(lastArg)) {
                    output.add(nation.getName());
                }
            }
            for (final Town town : townyUniverse.getDataSource().getTowns()) {
                if (town.getName().toLowerCase().startsWith(lastArg)) {
                    output.add(town.getName());
                }
            }
            for (final Resident resident : townyUniverse.getDataSource().getResidents()) {
                if (resident.getName().toLowerCase().startsWith(lastArg)) {
                    output.add(resident.getName());
                }
            }
        }
        return output;
    }
}
