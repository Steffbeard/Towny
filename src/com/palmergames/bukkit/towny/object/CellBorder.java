// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.Arrays;
import org.bukkit.World;
import com.palmergames.bukkit.util.DrawUtil;
import com.palmergames.bukkit.util.LocationRunnable;

public class CellBorder extends WorldCoord
{
    public boolean[] border;
    
    public CellBorder(final WorldCoord worldCoord, final boolean[] border) {
        super(worldCoord);
        this.border = border;
    }
    
    public void setBorderAt(final Section s, final boolean b) {
        this.border[s.ordinal()] = b;
    }
    
    public boolean hasBorderAt(final Section s) {
        return this.border[s.ordinal()];
    }
    
    public boolean[] getBorder() {
        return this.border;
    }
    
    public boolean hasAnyBorder() {
        for (final boolean b : this.border) {
            if (b) {
                return true;
            }
        }
        return false;
    }
    
    public int getBlockX() {
        return this.getX() * getCellSize();
    }
    
    public int getBlockZ() {
        return this.getZ() * getCellSize();
    }
    
    public void runBorderedOnSurface(final int wallHeight, final int cornerHeight, final LocationRunnable runnable) {
        final int x = this.getBlockX();
        final int z = this.getBlockZ();
        final int w = Coord.getCellSize() - 1;
        final World world = this.getBukkitWorld();
        for (final Section section : Section.values()) {
            if (this.border[section.ordinal()] && ((section.getType() == Section.Type.WALL && wallHeight > 0) || (section.getType() == Section.Type.CORNER && cornerHeight > 0))) {
                switch (section) {
                    case N: {
                        DrawUtil.runOnSurface(world, x, z, x, z + w, wallHeight, runnable);
                        break;
                    }
                    case NE: {
                        DrawUtil.runOnSurface(world, x, z, x, z, cornerHeight, runnable);
                        break;
                    }
                    case E: {
                        DrawUtil.runOnSurface(world, x, z, x + w, z, wallHeight, runnable);
                        break;
                    }
                    case SE: {
                        DrawUtil.runOnSurface(world, x + w, z, x + w, z, cornerHeight, runnable);
                        break;
                    }
                    case S: {
                        DrawUtil.runOnSurface(world, x + w, z, x + w, z + w, wallHeight, runnable);
                        break;
                    }
                    case SW: {
                        DrawUtil.runOnSurface(world, x + w, z + w, x + w, z + w, cornerHeight, runnable);
                        break;
                    }
                    case W: {
                        DrawUtil.runOnSurface(world, x, z + w, x + w, z + w, wallHeight, runnable);
                        break;
                    }
                    case NW: {
                        DrawUtil.runOnSurface(world, x, z + w, x, z + w, cornerHeight, runnable);
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + Arrays.toString(this.getBorder());
    }
    
    public enum Section
    {
        N(Type.WALL), 
        NE(Type.CORNER), 
        E(Type.WALL), 
        SE(Type.CORNER), 
        S(Type.WALL), 
        SW(Type.CORNER), 
        W(Type.WALL), 
        NW(Type.CORNER);
        
        private Type type;
        
        private Section(final Type type) {
            this.type = type;
        }
        
        public Type getType() {
            return this.type;
        }
        
        public static int numParts() {
            return values().length;
        }
        
        public enum Type
        {
            WALL, 
            CORNER;
        }
    }
}
