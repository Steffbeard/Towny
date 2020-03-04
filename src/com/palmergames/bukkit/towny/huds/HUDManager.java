// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.huds;

import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.event.TownBlockSettingsChangedEvent;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.war.eventwar.TownScoredEvent;
import com.palmergames.bukkit.towny.war.eventwar.PlotAttackedEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import java.util.Iterator;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import org.bukkit.event.Listener;

public class HUDManager implements Listener
{
    ArrayList<Player> warUsers;
    ArrayList<Player> permUsers;
    
    public HUDManager(final Towny plugin) {
        this.warUsers = new ArrayList<Player>();
        this.permUsers = new ArrayList<Player>();
    }
    
    public void toggleWarHUD(final Player p) {
        if (!this.warUsers.contains(p)) {
            this.toggleAllOff(p);
            this.warUsers.add(p);
            WarHUD.toggleOn(p, TownyUniverse.getInstance().getWarEvent());
        }
        else {
            this.toggleAllOff(p);
        }
    }
    
    public void togglePermHUD(final Player p) {
        if (!this.permUsers.contains(p)) {
            this.toggleAllOff(p);
            this.permUsers.add(p);
            PermHUD.toggleOn(p);
        }
        else {
            this.toggleAllOff(p);
        }
    }
    
    public void toggleAllWarHUD() {
        for (final Player p : this.warUsers) {
            toggleOff(p);
        }
        this.warUsers.clear();
    }
    
    public void toggleAllOff(final Player p) {
        this.warUsers.remove(p);
        this.permUsers.remove(p);
        if (p.isOnline()) {
            toggleOff(p);
        }
    }
    
    public void toggleAllOffForQuit(final Player p) {
        this.warUsers.remove(p);
        this.permUsers.remove(p);
    }
    
    public static void toggleOff(final Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.toggleAllOffForQuit(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerMovePlotsEvent(final PlayerChangePlotEvent event) throws NotRegisteredException {
        final Player p = event.getPlayer();
        if (this.warUsers.contains(p)) {
            WarHUD.updateLocation(p, event.getTo());
            WarHUD.updateAttackable(p, event.getTo(), TownyUniverse.getInstance().getWarEvent());
            WarHUD.updateHealth(p, event.getTo(), TownyUniverse.getInstance().getWarEvent());
        }
        else if (this.permUsers.contains(p) && p.getScoreboard().getTeam("plot") != null) {
            if (event.getTo().getTownyWorld().isUsingTowny()) {
                PermHUD.updatePerms(p, event.getTo());
            }
            else {
                toggleOff(p);
            }
        }
    }
    
    @EventHandler
    public void onPlotAttacked(final PlotAttackedEvent event) {
        final boolean home = event.getTownBlock().isHomeBlock();
        for (final Player p : event.getPlayers()) {
            if (this.warUsers.contains(p)) {
                WarHUD.updateHealth(p, event.getHP(), home);
            }
        }
    }
    
    @EventHandler
    public void onTownScored(final TownScoredEvent event) {
        final War warEvent = TownyUniverse.getInstance().getWarEvent();
        for (final Resident r : event.getTown().getResidents()) {
            final Player player = BukkitTools.getPlayer(r.getName());
            if (player != null && this.warUsers.contains(player)) {
                WarHUD.updateScore(player, event.getScore());
            }
        }
        final String[] top = warEvent.getTopThree();
        for (final Player p : this.warUsers) {
            WarHUD.updateTopScores(p, top);
        }
    }
    
    @EventHandler
    public void onTownBlockSettingsChanged(final TownBlockSettingsChangedEvent e) {
        if (e.getTownyWorld() != null) {
            for (final Player p : this.permUsers) {
                PermHUD.updatePerms(p);
            }
        }
        else if (e.getTown() != null) {
            for (final Player p : this.permUsers) {
                try {
                    if (new WorldCoord(p.getWorld().getName(), Coord.parseCoord((Entity)p)).getTownBlock().getTown() != e.getTown()) {
                        continue;
                    }
                    PermHUD.updatePerms(p);
                }
                catch (Exception ex) {}
            }
        }
        else if (e.getTownBlock() != null) {
            for (final Player p : this.permUsers) {
                try {
                    if (new WorldCoord(p.getWorld().getName(), Coord.parseCoord((Entity)p)).getTownBlock() != e.getTownBlock()) {
                        continue;
                    }
                    PermHUD.updatePerms(p);
                }
                catch (Exception ex2) {}
            }
        }
    }
    
    public static String check(final String check) {
        return (check.length() > 16) ? check.substring(0, 16) : check;
    }
}
