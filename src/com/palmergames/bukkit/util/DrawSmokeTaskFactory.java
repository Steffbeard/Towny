// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;

public class DrawSmokeTaskFactory
{
    public static LocationRunnable sendToPlayer(final Player player) {
        return new LocationRunnable() {
            Vector offset = new Vector(0.5, 0.5, 0.5);
            
            @Override
            public void run(final Location loc) {
                player.spawnParticle(Particle.SMOKE_NORMAL, loc.add(this.offset), 5, 0.0, 0.0, 0.0, 0.0);
            }
        };
    }
}
