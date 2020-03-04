// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.command.commandobjects;

import com.palmergames.bukkit.towny.command.InviteCommand;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class DenyCommand extends BukkitCommand
{
    public DenyCommand(final String name) {
        super(name);
        this.description = "Deny command for Towny";
        this.usageMessage = "/" + name + " <Town>";
    }
    
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        if (commandSender instanceof Player) {
            InviteCommand.parseDeny((Player)commandSender, strings);
            return true;
        }
        return true;
    }
}
