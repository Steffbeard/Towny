// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.metadata;

public class BooleanDataField extends CustomDataField<Boolean>
{
    public BooleanDataField(final String key, final Boolean value) {
        super(key, CustomDataFieldType.BooleanField, value);
    }
    
    public BooleanDataField(final String key, final Boolean value, final String label) {
        super(key, CustomDataFieldType.BooleanField, value, label);
    }
    
    public BooleanDataField(final String key) {
        super(key, CustomDataFieldType.BooleanField, false);
    }
}
