// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.locations;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.util.TimeMgmt;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.war.siegewar.enums.SiegeStatus;
import com.palmergames.bukkit.towny.object.Town;

public class Siege
{
    private Town defendingTown;
    private SiegeStatus status;
    private boolean townPlundered;
    private boolean townInvaded;
    private Nation attackerWinner;
    private long startTime;
    private long scheduledEndTime;
    private long actualEndTime;
    private Map<Nation, SiegeZone> siegeZones;
    
    public Siege(final Town defendingTown) {
        this.defendingTown = defendingTown;
        this.status = SiegeStatus.IN_PROGRESS;
        this.attackerWinner = null;
        this.siegeZones = new HashMap<Nation, SiegeZone>();
    }
    
    public Town getDefendingTown() {
        return this.defendingTown;
    }
    
    public Map<Nation, SiegeZone> getSiegeZones() {
        return this.siegeZones;
    }
    
    public long getScheduledEndTime() {
        return this.scheduledEndTime;
    }
    
    public long getActualEndTime() {
        return this.actualEndTime;
    }
    
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
    
    public void setScheduledEndTime(final long scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }
    
    public void setActualEndTime(final long actualEndTime) {
        this.actualEndTime = actualEndTime;
    }
    
    public long getStartTime() {
        return this.startTime;
    }
    
    public List<String> getSiegeZoneNames() {
        final List<String> names = new ArrayList<String>();
        for (final SiegeZone siegeZone : this.siegeZones.values()) {
            names.add(siegeZone.getName());
        }
        return names;
    }
    
    public void setStatus(final SiegeStatus status) {
        this.status = status;
    }
    
    public void setTownPlundered(final boolean townPlundered) {
        this.townPlundered = townPlundered;
    }
    
    public void setTownInvaded(final boolean townInvaded) {
        this.townInvaded = townInvaded;
    }
    
    public void setAttackerWinner(final Nation attackerWinner) {
        this.attackerWinner = attackerWinner;
    }
    
    public SiegeStatus getStatus() {
        return this.status;
    }
    
    public boolean isTownPlundered() {
        return this.townPlundered;
    }
    
    public boolean isTownInvaded() {
        return this.townInvaded;
    }
    
    public Nation getAttackerWinner() {
        return this.attackerWinner;
    }
    
    public boolean hasAttackerWinner() {
        return this.attackerWinner != null;
    }
    
    public double getTimeUntilCompletionMillis() {
        return (double)(this.scheduledEndTime - System.currentTimeMillis());
    }
    
    public String getFormattedHoursUntilScheduledCompletion() {
        if (this.status == SiegeStatus.IN_PROGRESS) {
            final double timeUntilCompletionMillis = this.getTimeUntilCompletionMillis();
            return TimeMgmt.getFormattedTimeValue(timeUntilCompletionMillis);
        }
        return "0";
    }
    
    public boolean getTownPlundered() {
        return this.townPlundered;
    }
    
    public boolean getTownInvaded() {
        return this.townInvaded;
    }
    
    public long getDurationMillis() {
        return System.currentTimeMillis() - this.startTime;
    }
    
    public long getTimeUntilSurrenderIsAllowedMillis() {
        return (long)(TownySettings.getWarSiegeMinSiegeDurationBeforeSurrenderHours() * 3600000.0 - this.getDurationMillis());
    }
    
    public long getTimeUntilAbandonIsAllowedMillis() {
        return (long)(TownySettings.getWarSiegeMinSiegeDurationBeforeAbandonHours() * 3600000.0 - this.getDurationMillis());
    }
}
