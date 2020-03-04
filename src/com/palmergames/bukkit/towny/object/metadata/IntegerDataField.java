// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.metadata;

public class IntegerDataField extends CustomDataField<Integer>
{
    public IntegerDataField(final String key) {
        super(key, CustomDataFieldType.IntegerField);
    }
    
    public IntegerDataField(final String key, final Integer value, final String label) {
        super(key, CustomDataFieldType.IntegerField, value, label);
    }
    
    public IntegerDataField(final String key, final Integer value) {
        super(key, CustomDataFieldType.IntegerField, value);
    }
}
