// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar;

import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.Location;

public class Cell
{
    private String worldName;
    private int x;
    private int z;
    
    public Cell(final String worldName, final int x, final int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }
    
    public Cell(final Cell cell) {
        this.worldName = cell.getWorldName();
        this.x = cell.getX();
        this.z = cell.getZ();
    }
    
    public Cell(final Location location) {
        this(parse(location));
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
    
    public String getWorldName() {
        return this.worldName;
    }
    
    public void setWorldName(final String worldName) {
        this.worldName = worldName;
    }
    
    public static Cell parse(final String worldName, final int x, final int z) {
        final int cellSize = Coord.getCellSize();
        final int xresult = x / cellSize;
        final int zresult = z / cellSize;
        final boolean xneedfix = x % cellSize != 0;
        final boolean zneedfix = z % cellSize != 0;
        return new Cell(worldName, xresult - ((x < 0 && xneedfix) ? 1 : 0), zresult - ((z < 0 && zneedfix) ? 1 : 0));
    }
    
    public static Cell parse(final Location loc) {
        return parse(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ());
    }
    
    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 27 + ((this.worldName == null) ? 0 : this.worldName.hashCode());
        hash = hash * 27 + this.x;
        hash = hash * 27 + this.z;
        return hash;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Cell)) {
            return false;
        }
        final Cell that = (Cell)obj;
        return this.x == that.x && this.z == that.z && ((this.worldName != null) ? this.worldName.equals(that.worldName) : (that.worldName == null));
    }
    
    public boolean isUnderAttack() {
        return TownyWar.isUnderAttack(this);
    }
    
    public CellUnderAttack getAttackData() {
        return TownyWar.getAttackData(this);
    }
}
