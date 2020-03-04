// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object.metadata;

public enum CustomDataFieldType
{
    IntegerField(Integer.valueOf(0), "Integer"), 
    StringField(Integer.valueOf(1), "String"), 
    BooleanField(Integer.valueOf(2), "Boolean"), 
    DecimalField(Integer.valueOf(3), "Decimal");
    
    private Integer value;
    private String typeName;
    
    private CustomDataFieldType(final Integer type, final String typeName) {
        this.value = type;
        this.typeName = typeName;
    }
    
    public Integer getValue() {
        return this.value;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
}
