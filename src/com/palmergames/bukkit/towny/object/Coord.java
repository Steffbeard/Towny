// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class Coord
{
    protected static int cellSize;
    protected int x;
    protected int z;
    
    public Coord(final int x, final int z) {
        this.x = x;
        this.z = z;
    }
    
    public Coord(final Coord coord) {
        this.x = coord.getX();
        this.z = coord.getZ();
    }
    
    public int getX() {
        return this.x;
    }
    
    public void setX(final int x) {
        this.x = x;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public void setZ(final int z) {
        this.z = z;
    }
    
    public Coord add(final int xOffset, final int zOffset) {
        return new Coord(this.getX() + xOffset, this.getZ() + zOffset);
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 27 * result + this.x;
        result = 27 * result + this.z;
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Coord)) {
            return false;
        }
        final Coord o = (Coord)obj;
        return this.x == o.x && this.z == o.z;
    }
    
    protected static int toCell(final int value) {
        return Math.floorDiv(value, getCellSize());
    }
    
    public static Coord parseCoord(final int x, final int z) {
        return new Coord(toCell(x), toCell(z));
    }
    
    public static Coord parseCoord(final Entity entity) {
        return parseCoord(entity.getLocation());
    }
    
    public static Coord parseCoord(final Location loc) {
        return parseCoord(loc.getBlockX(), loc.getBlockZ());
    }
    
    public static Coord parseCoord(final Block block) {
        return parseCoord(block.getX(), block.getZ());
    }
    
    @Override
    public String toString() {
        return this.getX() + "," + this.getZ();
    }
    
    public static void setCellSize(final int cellSize) {
        Coord.cellSize = cellSize;
    }
    
    public static int getCellSize() {
        return Coord.cellSize;
    }
    
    public Location getTopNorthWestCornerLocation(final World world) {
        final int locX = this.x * getCellSize();
        final int locZ = this.z * getCellSize();
        return new Location(world, (double)locX, 255.0, (double)locZ);
    }
    
    static {
        Coord.cellSize = 16;
    }
}
