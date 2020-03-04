// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.siegewar.utils;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.war.siegewar.locations.Siege;
import com.palmergames.bukkit.towny.object.Town;

public class SiegeWarTimeUtil
{
    public static void activateSiegeImmunityTimer(final Town town, final Siege siege) {
        final double siegeDuration = (double)(siege.getActualEndTime() - siege.getStartTime());
        final double cooldownDuration = siegeDuration * TownySettings.getWarSiegeSiegeImmunityTimeModifier();
        town.setSiegeImmunityEndTime(System.currentTimeMillis() + (long)(cooldownDuration + 0.5));
    }
    
    public static void activateRevoltImmunityTimer(final Town town) {
        final long immunityDuration = (long)(TownySettings.getWarSiegeRevoltImmunityTimeHours() * 3600000.0);
        town.setRevoltImmunityEndTime(System.currentTimeMillis() + immunityDuration);
    }
}
