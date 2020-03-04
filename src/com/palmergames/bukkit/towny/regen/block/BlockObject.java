package com.palmergames.bukkit.towny.regen.block;

import org.bukkit.Location;

public class BlockObject
{
    private int typeId;
    private byte data;
    private BlockLocation location;
    
    public BlockObject(final int typeId) {
        this.typeId = typeId;
        this.data = 0;
    }
    
    public BlockObject(final int typeId, final Location loc) {
        this.typeId = typeId;
        this.data = 0;
        this.setLocation(loc);
    }
    
    public BlockObject(final int typeId, final byte data) {
        this.typeId = typeId;
        this.data = data;
    }
    
    public BlockObject(final int typeId, final byte data, final Location loc) {
        this.typeId = typeId;
        this.data = data;
        this.setLocation(loc);
    }
    
    public int getTypeId() {
        return this.typeId;
    }
    
    public void setTypeId(final int typeId) {
        this.typeId = typeId;
    }
    
    public byte getData() {
        return this.data;
    }
    
    public void setData(final byte data) {
        this.data = data;
    }
    
    public BlockLocation getLocation() {
        return this.location;
    }
    
    public void setLocation(final Location loc) {
        this.location = new BlockLocation(loc);
    }
    
    public void setTypeIdAndData(final int typeId, final byte data) {
        this.typeId = typeId;
        this.data = data;
    }
}
