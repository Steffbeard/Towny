// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.locations;

import java.util.HashMap;
import org.bukkit.entity.Player;
import java.util.Map;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.Location;

public class SiegeZone
{
    private Location siegeBannerLocation;
    private Nation attackingNation;
    private Town defendingTown;
    private int siegePoints;
    private Map<Player, Long> attackerPlayerScoreTimeMap;
    private Map<Player, Long> defenderPlayerScoreTimeMap;
    private Map<Player, Long> playerAfkTimeMap;
    private double warChestAmount;
    
    public SiegeZone() {
        this.attackingNation = null;
        this.defendingTown = null;
        this.siegePoints = 0;
        this.siegeBannerLocation = null;
        this.attackerPlayerScoreTimeMap = new HashMap<Player, Long>();
        this.defenderPlayerScoreTimeMap = new HashMap<Player, Long>();
        this.playerAfkTimeMap = new HashMap<Player, Long>();
        this.warChestAmount = 0.0;
    }
    
    public SiegeZone(final Nation attackingNation, final Town defendingTown) {
        this.defendingTown = defendingTown;
        this.attackingNation = attackingNation;
        this.siegePoints = 0;
        this.siegeBannerLocation = null;
        this.attackerPlayerScoreTimeMap = new HashMap<Player, Long>();
        this.defenderPlayerScoreTimeMap = new HashMap<Player, Long>();
        this.playerAfkTimeMap = new HashMap<Player, Long>();
        this.warChestAmount = 0.0;
    }
    
    public String getName() {
        return this.attackingNation.getName().toLowerCase() + "#vs#" + this.defendingTown.getName().toLowerCase();
    }
    
    public static String generateName(final String attackingNationName, final String defendingTownName) {
        return attackingNationName.toLowerCase() + "#vs#" + defendingTownName.toLowerCase();
    }
    
    public static String[] generateTownAndNationName(final String siegeZoneName) {
        return siegeZoneName.split("#vs#");
    }
    
    public Siege getSiege() {
        return this.defendingTown.getSiege();
    }
    
    public Nation getAttackingNation() {
        return this.attackingNation;
    }
    
    public Location getFlagLocation() {
        return this.siegeBannerLocation;
    }
    
    public void setFlagLocation(final Location location) {
        this.siegeBannerLocation = location;
    }
    
    public Map<Player, Long> getAttackerPlayerScoreTimeMap() {
        return this.attackerPlayerScoreTimeMap;
    }
    
    public Map<Player, Long> getDefenderPlayerScoreTimeMap() {
        return this.defenderPlayerScoreTimeMap;
    }
    
    public Integer getSiegePoints() {
        return this.siegePoints;
    }
    
    public void setSiegePoints(final int siegePoints) {
        this.siegePoints = siegePoints;
    }
    
    public void setAttackingNation(final Nation attackingNation) {
        this.attackingNation = attackingNation;
    }
    
    public void setDefendingTown(final Town defendingTown) {
        this.defendingTown = defendingTown;
    }
    
    public Town getDefendingTown() {
        return this.defendingTown;
    }
    
    public void adjustSiegePoints(final int adjustment) {
        this.siegePoints += adjustment;
    }
    
    public Map<Player, Long> getPlayerAfkTimeMap() {
        return this.playerAfkTimeMap;
    }
    
    public void setPlayerAfkTimeMap(final Map<Player, Long> playerAfkTimeMap) {
        this.playerAfkTimeMap = playerAfkTimeMap;
    }
    
    public double getWarChestAmount() {
        if (this.warChestAmount == 0.0) {
            try {
                return this.defendingTown.getSiegeCost();
            }
            catch (Exception e) {
                return 100.0;
            }
        }
        return this.warChestAmount;
    }
    
    public void setWarChestAmount(final double warChestAmount) {
        this.warChestAmount = warChestAmount;
    }
}
