// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.util.regex.Pattern;

public class TimeTools
{
    public static long secondsFromDhms(String dhms) {
        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        int days = 0;
        if (dhms.contains("d")) {
            days = Integer.parseInt(dhms.split("d")[0].replaceAll(" ", ""));
            if (dhms.contains("h") || dhms.contains("m") || dhms.contains("s")) {
                dhms = dhms.split("d")[1];
            }
        }
        if (dhms.contains("h")) {
            hours = Integer.parseInt(dhms.split("h")[0].replaceAll(" ", ""));
            if (dhms.contains("m") || dhms.contains("s")) {
                dhms = dhms.split("h")[1];
            }
        }
        if (dhms.contains("m")) {
            minutes = Integer.parseInt(dhms.split("m")[0].replaceAll(" ", ""));
            if (dhms.contains("s")) {
                dhms = dhms.split("m")[1];
            }
        }
        if (dhms.contains("s")) {
            seconds = Integer.parseInt(dhms.split("s")[0].replaceAll(" ", ""));
        }
        return days * 86400 + hours * 3600 + minutes * 60 + seconds;
    }
    
    public static long getMillis(final String dhms) {
        return getSeconds(dhms) * 1000L;
    }
    
    public static long getSeconds(final String dhms) {
        if (Pattern.matches(".*[a-zA-Z].*", dhms)) {
            return secondsFromDhms(dhms);
        }
        return Long.parseLong(dhms);
    }
    
    public static long getTicks(final String dhms) {
        return convertToTicks(getSeconds(dhms));
    }
    
    public static long convertToTicks(final long t) {
        return t * 20L;
    }
}
