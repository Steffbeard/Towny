package com.palmergames.bukkit.towny.object.metadata;

import com.palmergames.bukkit.towny.exceptions.InvalidMetadataTypeException;

public abstract class CustomDataField<T>
{
    private CustomDataFieldType type;
    private T value;
    private String key;
    private String label;
    
    public CustomDataField(final String key, final CustomDataFieldType type, final T value, final String label) {
        this.type = type;
        this.setValue(value);
        this.key = key;
        this.label = label;
    }
    
    public CustomDataField(final String key, final CustomDataFieldType type, final T value) {
        this(key, type, value, null);
    }
    
    public CustomDataField(final String key, final CustomDataFieldType type, final String label) {
        this(key, type, null, label);
    }
    
    public CustomDataField(final String key, final CustomDataFieldType type) {
        this(key, type, null, null);
    }
    
    public CustomDataFieldType getType() {
        return this.type;
    }
    
    public T getValue() {
        return this.value;
    }
    
    public void setValue(final T value) {
        this.value = value;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public String getLabel() {
        if (this.hasLabel()) {
            return this.label;
        }
        return "nil";
    }
    
    public boolean hasLabel() {
        return this.label != null;
    }
    
    public void setLabel(final String label) {
        this.label = label;
    }
    
    @Override
    public String toString() {
        String out = "";
        out += this.type.getValue().toString();
        out = out + "," + this.getKey();
        out = out + "," + this.getValue();
        out = out + "," + this.getLabel();
        return out;
    }
    
    public static CustomDataField load(final String str) {
        final String[] tokens = str.split(",");
        final CustomDataFieldType type = CustomDataFieldType.values()[Integer.parseInt(tokens[0])];
        final String key = tokens[1];
        CustomDataField field = null;
        switch (type) {
            case IntegerField: {
                final Integer intValue = Integer.parseInt(tokens[2]);
                field = new IntegerDataField(key, intValue);
                break;
            }
            case StringField: {
                field = new StringDataField(key, tokens[2]);
                break;
            }
            case BooleanField: {
                field = new BooleanDataField(key, Boolean.parseBoolean(tokens[2]));
                break;
            }
            case DecimalField: {
                field = new DecimalDataField(key, Double.parseDouble(tokens[2]));
                break;
            }
        }
        String label;
        if (tokens[3] == null || tokens[3].equalsIgnoreCase("nil")) {
            label = null;
        }
        else {
            label = tokens[3];
        }
        field.setLabel(label);
        return field;
    }
    
    public void isValidType(final String str) throws InvalidMetadataTypeException {
        switch (this.type) {
            case IntegerField: {
                try {
                    Integer.parseInt(str);
                }
                catch (NumberFormatException e) {
                    throw new InvalidMetadataTypeException(this.type);
                }
            }
            case DecimalField: {
                try {
                    Double.parseDouble(str);
                }
                catch (NumberFormatException e) {
                    throw new InvalidMetadataTypeException(this.type);
                }
                break;
            }
        }
    }
    
    @Override
    public boolean equals(final Object rhs) {
        return rhs instanceof CustomDataField && ((CustomDataField)rhs).getKey().equals(this.getKey());
    }
    
    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }
    
    public CustomDataField newCopy() {
        switch (this.type) {
            case BooleanField: {
                return new BooleanDataField(this.getKey(), this.getValue());
            }
            case IntegerField: {
                return new IntegerDataField(this.getKey(), this.getValue());
            }
            case DecimalField: {
                return new DecimalDataField(this.getKey(), this.getValue());
            }
            case StringField: {
                return new StringDataField(this.getKey(), this.getValue());
            }
            default: {
                return null;
            }
        }
    }
}
