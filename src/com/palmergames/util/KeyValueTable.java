// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.util.Comparator;
import java.util.Collections;
import java.util.Collection;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

public class KeyValueTable<K, V>
{
    private List<KeyValue<K, V>> keyValues;
    
    public List<KeyValue<K, V>> getKeyValues() {
        return this.keyValues;
    }
    
    public void setKeyValues(final List<KeyValue<K, V>> keyValues) {
        this.keyValues = keyValues;
    }
    
    public KeyValueTable() {
        this.keyValues = new ArrayList<KeyValue<K, V>>();
    }
    
    public KeyValueTable(final Hashtable<K, V> table) {
        this((List)new ArrayList(table.keySet()), (List)new ArrayList(table.values()));
    }
    
    public KeyValueTable(final List<K> keys, final List<V> values) {
        this.keyValues = new ArrayList<KeyValue<K, V>>();
        for (int i = 0; i < keys.size(); ++i) {
            this.keyValues.add(new KeyValue<K, V>(keys.get(i), values.get(i)));
        }
    }
    
    public void put(final K key, final V value) {
        this.keyValues.add(new KeyValue<K, V>(key, value));
    }
    
    public void add(final KeyValue<K, V> keyValue) {
        this.keyValues.add(keyValue);
    }
    
    public void sortByKey() {
        Collections.sort(this.keyValues, new Sorting.KeySort());
    }
    
    public void sortByValue() {
        Collections.sort(this.keyValues, new Sorting.ValueSort());
    }
    
    public void reverse() {
        Collections.reverse(this.keyValues);
    }
}
