// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.eventwar;

import java.util.Iterator;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import java.util.HashSet;

public class WarZoneData
{
    private HashSet<Player> attackers;
    private HashSet<Player> defenders;
    private HashSet<Town> attackerTowns;
    private HashSet<Player> allPlayers;
    
    public WarZoneData() {
        this.attackers = new HashSet<Player>();
        this.defenders = new HashSet<Player>();
        this.attackerTowns = new HashSet<Town>();
        this.allPlayers = new HashSet<Player>();
    }
    
    public int getHealthChange() {
        return this.defenders.size() - this.attackers.size();
    }
    
    public void addAttacker(final Player p) throws NotRegisteredException {
        if (!p.isDead()) {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(p.getName());
            this.attackerTowns.add(resident.getTown());
            this.allPlayers.add(p);
            this.attackers.add(p);
        }
    }
    
    public void addDefender(final Player p) {
        if (!p.isDead()) {
            this.allPlayers.add(p);
            this.defenders.add(p);
        }
    }
    
    public boolean hasAttackers() {
        return !this.attackers.isEmpty();
    }
    
    public boolean hasDefenders() {
        return !this.defenders.isEmpty();
    }
    
    public HashSet<Player> getAttackers() {
        return this.attackers;
    }
    
    public HashSet<Player> getDefenders() {
        return this.defenders;
    }
    
    public HashSet<Town> getAttackerTowns() {
        return this.attackerTowns;
    }
    
    public Player getRandomAttacker() {
        final int index = (int)(Math.random() * this.attackers.size());
        int curIndex = 0;
        for (final Player p : this.attackers) {
            if (curIndex == index) {
                return p;
            }
            ++curIndex;
        }
        return null;
    }
    
    public Player getRandomDefender() {
        final int index = (int)(Math.random() * this.defenders.size());
        int curIndex = 0;
        for (final Player p : this.defenders) {
            if (curIndex == index) {
                return p;
            }
            ++curIndex;
        }
        return null;
    }
    
    public HashSet<Player> getAllPlayers() {
        return this.allPlayers;
    }
}
