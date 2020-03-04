// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.regen;

import org.bukkit.Material;
import java.util.EnumSet;

public class NeedsPlaceholder
{
    private static EnumSet<Material> needsPlaceholder;
    
    public static boolean contains(final Material material) {
        return NeedsPlaceholder.needsPlaceholder.contains(material);
    }
    
    static {
        NeedsPlaceholder.needsPlaceholder = EnumSet.of(Material.SAND, Material.GRAVEL, Material.REDSTONE_WIRE, Material.REDSTONE_COMPARATOR, Material.SAPLING, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.WHEAT, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF, Material.SNOW, Material.SIGN, Material.WALL_SIGN);
    }
}
