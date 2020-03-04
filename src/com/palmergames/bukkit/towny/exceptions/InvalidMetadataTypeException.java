// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.exceptions;

import com.palmergames.bukkit.towny.object.metadata.CustomDataFieldType;

public class InvalidMetadataTypeException extends TownyException
{
    private static final long serialVersionUID = 2335936343233569066L;
    
    public InvalidMetadataTypeException(final CustomDataFieldType type) {
        super("The given string for type " + type.getTypeName() + " is not valid!");
    }
}
