// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.metadata;

public class StringDataField extends CustomDataField<String>
{
    public StringDataField(final String key) {
        super(key, CustomDataFieldType.StringField);
    }
    
    public StringDataField(final String key, final String value, final String label) {
        super(key, CustomDataFieldType.StringField, value, label);
    }
    
    public StringDataField(final String key, final String value) {
        super(key, CustomDataFieldType.StringField, value, null);
    }
}
