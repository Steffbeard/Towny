// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.TownyFormatter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import java.util.HashSet;

public abstract class TownyObject
{
    private String name;
    private HashSet<CustomDataField> metadata;
    
    protected TownyObject(final String name) {
        this.metadata = null;
        this.name = name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<String> getTreeString(final int depth) {
        return new ArrayList<String>();
    }
    
    public String getTreeDepth(final int depth) {
        final char[] fill = new char[depth * 4];
        Arrays.fill(fill, ' ');
        if (depth > 0) {
            fill[0] = '|';
            final int offset = (depth - 1) * 4;
            fill[offset] = '+';
            fill[offset + 2] = (fill[offset + 1] = '-');
        }
        return new String(fill);
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
    
    public String getFormattedName() {
        return TownyFormatter.getFormattedName(this);
    }
    
    public void addMetaData(final CustomDataField md) {
        if (this.getMetadata() == null) {
            this.metadata = new HashSet<CustomDataField>();
        }
        this.getMetadata().add(md);
    }
    
    public void removeMetaData(final CustomDataField md) {
        if (!this.hasMeta()) {
            return;
        }
        this.getMetadata().remove(md);
        if (this.getMetadata().size() == 0) {
            this.metadata = null;
        }
    }
    
    public HashSet<CustomDataField> getMetadata() {
        return this.metadata;
    }
    
    public boolean hasMeta() {
        return this.getMetadata() != null;
    }
    
    public void setMetadata(final String str) {
        if (this.metadata == null) {
            this.metadata = new HashSet<CustomDataField>();
        }
        final String[] objects = str.split(";");
        for (int i = 0; i < objects.length; ++i) {
            this.metadata.add(CustomDataField.load(objects[i]));
        }
    }
}
