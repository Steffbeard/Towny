// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.Iterator;
import java.util.UUID;
import java.util.Collection;

interface ObjectGroupManageable<T extends ObjectGroup>
{
    Collection<T> getObjectGroups();
    
    T getObjectGroupFromID(final UUID p0);
    
    boolean hasObjectGroups();
    
    default boolean hasObjectGroup(final T group) {
        return this.hasObjectGroups() && this.getObjectGroups().contains(group);
    }
    
    default boolean hasObjectGroupName(final String name) {
        if (this.hasObjectGroups()) {
            for (final T group : this.getObjectGroups()) {
                if (group.getGroupName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
