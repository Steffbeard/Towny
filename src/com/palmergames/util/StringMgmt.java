// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class StringMgmt
{
    public static String join(final List<?> arr) {
        return join(arr, " ");
    }
    
    public static String join(final List<?> arr, final String separator) {
        if (arr == null || arr.size() == 0) {
            return "";
        }
        String out = arr.get(0).toString();
        for (int i = 1; i < arr.size(); ++i) {
            out = out + separator + arr.get(i);
        }
        return out;
    }
    
    public static String join(final Object[] arr) {
        return join(arr, " ");
    }
    
    public static String join(final Object[] arr, final String separator) {
        if (arr.length == 0) {
            return "";
        }
        String out = arr[0].toString();
        for (int i = 1; i < arr.length; ++i) {
            out = out + separator + arr[i];
        }
        return out;
    }
    
    public static String join(final Map<?, ?> map, final String keyValSeparator, final String tokenSeparator) {
        if (map.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(keyValSeparator).append(entry.getValue().toString()).append(tokenSeparator);
        }
        return sb.toString();
    }
    
    public static String[] remFirstArg(final String[] arr) {
        return remArgs(arr, 1);
    }
    
    public static String[] remLastArg(final String[] arr) {
        return subArray(arr, 0, arr.length - 1);
    }
    
    public static String[] remArgs(final String[] arr, final int startFromIndex) {
        if (arr.length == 0) {
            return arr;
        }
        if (arr.length < startFromIndex) {
            return new String[0];
        }
        final String[] newSplit = new String[arr.length - startFromIndex];
        System.arraycopy(arr, startFromIndex, newSplit, 0, arr.length - startFromIndex);
        return newSplit;
    }
    
    public static String[] subArray(final String[] arr, final int start, final int end) {
        if (arr.length == 0) {
            return arr;
        }
        if (end < start) {
            return new String[0];
        }
        final int length = end - start;
        final String[] newSplit = new String[length];
        System.arraycopy(arr, start, newSplit, 0, length);
        return newSplit;
    }
    
    public static String trimMaxLength(final String str, final int length) {
        if (str.length() < length) {
            return str;
        }
        if (length > 3) {
            return str.substring(0, length);
        }
        throw new UnsupportedOperationException("Minimum length of 3 characters.");
    }
    
    public static String maxLength(final String str, final int length) {
        if (str.length() < length) {
            return str;
        }
        if (length > 3) {
            return str.substring(0, length - 3) + "...";
        }
        throw new UnsupportedOperationException("Minimum length of 3 characters.");
    }
    
    public static boolean containsIgnoreCase(final List<String> arr, final String str) {
        for (final String s : arr) {
            if (s.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }
    
    public static String remUnderscore(final String str) {
        return str.replaceAll("_", " ");
    }
    
    public static String capitalize(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
