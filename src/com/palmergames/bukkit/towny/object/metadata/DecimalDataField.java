// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.metadata;

public class DecimalDataField extends CustomDataField<Double>
{
    public DecimalDataField(final String key, final Double value) {
        super(key, CustomDataFieldType.DecimalField, value);
    }
    
    public DecimalDataField(final String key, final Double value, final String label) {
        super(key, CustomDataFieldType.DecimalField, value, label);
    }
    
    public DecimalDataField(final String key) {
        super(key, CustomDataFieldType.DecimalField, 0.0);
    }
}
