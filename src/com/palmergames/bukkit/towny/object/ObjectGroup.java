// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.UUID;

public abstract class ObjectGroup
{
    private UUID id;
    private String name;
    
    public ObjectGroup(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    public UUID getID() {
        return this.id;
    }
    
    public void setID(final UUID ID) {
        this.id = ID;
    }
    
    public String getGroupName() {
        return this.name;
    }
    
    public void setGroupName(final String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ObjectGroup && ((ObjectGroup)obj).id.equals(this.id);
    }
    
    @Override
    public String toString() {
        return this.name + "," + this.id;
    }
    
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
