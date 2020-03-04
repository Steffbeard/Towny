// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.locations;

public class SiegeZoneDistance
{
    private SiegeZone siegeZone;
    private double distance;
    
    public SiegeZoneDistance(final SiegeZone siegeZone, final double distance) {
        this.siegeZone = siegeZone;
        this.distance = distance;
    }
    
    public SiegeZone getSiegeZone() {
        return this.siegeZone;
    }
    
    public double getDistance() {
        return this.distance;
    }
}
