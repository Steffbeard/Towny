// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import org.bukkit.Location;
import org.bukkit.World;

public class DrawUtil
{
    public static void runOnSurface(final World world, final int x1, final int z1, final int x2, final int z2, final int height, final LocationRunnable runnable) {
        final int _x1 = Math.min(x1, x2);
        final int _x2 = Math.max(x1, x2);
        final int _z1 = Math.min(z1, z2);
        for (int _z2 = Math.max(z1, z2), z3 = _z1; z3 <= _z2; ++z3) {
            for (int x3 = _x1; x3 <= _x2; ++x3) {
                final int start = world.getHighestBlockYAt(x3, z3);
                for (int end = (start + height < world.getMaxHeight()) ? (start + height - 1) : world.getMaxHeight(), y = start; y <= end; ++y) {
                    runnable.run(new Location(world, (double)x3, (double)y, (double)z3));
                }
            }
        }
    }
}
