// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.TimerTask;

public class ServerBroadCastTimerTask extends TimerTask
{
    private JavaPlugin plugin;
    private String msg;
    
    public ServerBroadCastTimerTask(final JavaPlugin plugin, final String msg) {
        this.plugin = plugin;
        this.msg = msg;
    }
    
    @Override
    public void run() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(this.msg);
        }
    }
}
