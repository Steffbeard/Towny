// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.Objects;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;

public class WorldCoord extends Coord
{
    private String worldName;
    
    public WorldCoord(final String worldName, final int x, final int z) {
        super(x, z);
        this.worldName = worldName;
    }
    
    public WorldCoord(final String worldName, final Coord coord) {
        super(coord);
        this.worldName = worldName;
    }
    
    public WorldCoord(final WorldCoord worldCoord) {
        super(worldCoord);
        this.worldName = worldCoord.getWorldName();
    }
    
    public String getWorldName() {
        return this.worldName;
    }
    
    public Coord getCoord() {
        return new Coord(this.x, this.z);
    }
    
    @Deprecated
    public TownyWorld getWorld() throws NotRegisteredException {
        return this.getTownyWorld();
    }
    
    @Deprecated
    public WorldCoord(final TownyWorld world, final int x, final int z) {
        super(x, z);
        this.worldName = world.getName();
    }
    
    @Deprecated
    public WorldCoord(final TownyWorld world, final Coord coord) {
        super(coord);
        this.worldName = world.getName();
    }
    
    public static WorldCoord parseWorldCoord(final Entity entity) {
        return parseWorldCoord(entity.getLocation());
    }
    
    public static WorldCoord parseWorldCoord(final Location loc) {
        return new WorldCoord(loc.getWorld().getName(), Coord.parseCoord(loc));
    }
    
    public static WorldCoord parseWorldCoord(final Block block) {
        return new WorldCoord(block.getWorld().getName(), Coord.parseCoord(block.getX(), block.getZ()));
    }
    
    @Override
    public WorldCoord add(final int xOffset, final int zOffset) {
        return new WorldCoord(this.getWorldName(), this.getX() + xOffset, this.getZ() + zOffset);
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
        if (!(obj instanceof Coord)) {
            return false;
        }
        if (!(obj instanceof WorldCoord)) {
            final Coord that = (Coord)obj;
            return this.x == that.x && this.z == that.z;
        }
        final WorldCoord that2 = (WorldCoord)obj;
        return this.x == that2.x && this.z == that2.z && ((this.worldName != null) ? this.worldName.equals(that2.worldName) : (that2.worldName == null));
    }
    
    @Override
    public String toString() {
        return this.worldName + "," + super.toString();
    }
    
    public World getBukkitWorld() {
        return Bukkit.getWorld(this.worldName);
    }
    
    public TownyWorld getTownyWorld() throws NotRegisteredException {
        return TownyUniverse.getInstance().getDataSource().getWorld(this.worldName);
    }
    
    public TownBlock getTownBlock() throws NotRegisteredException {
        return this.getTownyWorld().getTownBlock(this.getCoord());
    }
    
    public static boolean cellChanged(final Location from, final Location to) {
        return Coord.toCell(from.getBlockX()) != Coord.toCell(to.getBlockX()) || Coord.toCell(from.getBlockZ()) != Coord.toCell(to.getBlockZ()) || !Objects.equals(from.getWorld(), to.getWorld());
    }
}
