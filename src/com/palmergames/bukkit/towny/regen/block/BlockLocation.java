// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.regen.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Chunk;

public class BlockLocation
{
    protected int x;
    protected int z;
    protected int y;
    protected Chunk chunk;
    protected World world;
    
    public void setY(final int y) {
        this.y = y;
    }
    
    public BlockLocation(final Location loc) {
        this.x = loc.getBlockX();
        this.z = loc.getBlockZ();
        this.y = loc.getBlockY();
        this.chunk = loc.getChunk();
        this.world = loc.getWorld();
    }
    
    public Chunk getChunk() {
        return this.chunk;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public int getY() {
        return this.y;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public boolean isLocation(final Location loc) {
        return loc.getWorld() == this.getWorld() && loc.getBlockX() == this.getX() && loc.getBlockY() == this.getY() && loc.getBlockZ() == this.getZ();
    }
}
