// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.util.Comparator;

public class Sorting
{
    static class ValueSort implements Comparator<Object>
    {
        @Override
        public int compare(final Object o1, final Object o2) {
            if (!(o1 instanceof KeyValue) || !(o2 instanceof KeyValue)) {
                return o1.hashCode() - o2.hashCode();
            }
            final KeyValue<?, ?> k1 = (KeyValue<?, ?>)o1;
            final KeyValue<?, ?> k2 = (KeyValue<?, ?>)o2;
            return k1.value.hashCode() - k2.value.hashCode();
        }
    }
    
    static class KeySort implements Comparator<Object>
    {
        @Override
        public int compare(final Object o1, final Object o2) {
            if (!(o1 instanceof KeyValue) || !(o2 instanceof KeyValue)) {
                return o1.hashCode() - o2.hashCode();
            }
            final KeyValue<?, ?> k1 = (KeyValue<?, ?>)o1;
            final KeyValue<?, ?> k2 = (KeyValue<?, ?>)o2;
            return k1.key.hashCode() - k2.key.hashCode();
        }
    }
}
