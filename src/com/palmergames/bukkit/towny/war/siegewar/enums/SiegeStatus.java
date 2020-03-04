// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.enums;

public enum SiegeStatus
{
    IN_PROGRESS, 
    ATTACKER_WIN, 
    DEFENDER_WIN, 
    ATTACKER_ABANDON, 
    DEFENDER_SURRENDER, 
    UNKNOWN;
    
    public static SiegeStatus parseString(final String line) {
        switch (line) {
            case "IN_PROGRESS": {
                return SiegeStatus.IN_PROGRESS;
            }
            case "ATTACKER_WIN": {
                return SiegeStatus.ATTACKER_WIN;
            }
            case "DEFENDER_WIN": {
                return SiegeStatus.DEFENDER_WIN;
            }
            case "ATTACKER_ABANDON": {
                return SiegeStatus.ATTACKER_ABANDON;
            }
            case "DEFENDER_SURRENDER": {
                return SiegeStatus.DEFENDER_SURRENDER;
            }
            default: {
                return SiegeStatus.UNKNOWN;
            }
        }
    }
}
