// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.text.NumberFormat;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayList;
import java.util.List;

public class TimeMgmt
{
    public static final double ONE_SECOND_IN_MILLIS = 1000.0;
    public static final double ONE_MINUTE_IN_MILLIS = 60000.0;
    public static final double ONE_HOUR_IN_MILLIS = 3600000.0;
    public static final double ONE_DAY_IN_MILLIS = 8.64E7;
    public static final long[][] defaultCountdownDelays;
    
    public static List<Long> getCountdownDelays(final int start) {
        return getCountdownDelays(start, TimeMgmt.defaultCountdownDelays);
    }
    
    public static List<Long> getCountdownDelays(final int start, final long[][] delays) {
        final List<Long> out = new ArrayList<Long>();
        for (final long[] delay : delays) {
            if (delay.length != 2) {
                return null;
            }
        }
        Integer lastDelayIndex = null;
        long nextWarningAt = 2147483647L;
        for (long t = start; t > 0L; --t) {
            for (int d = 0; d < delays.length; ++d) {
                if (t <= delays[d][0] && (lastDelayIndex == null || t <= nextWarningAt || d < lastDelayIndex)) {
                    lastDelayIndex = d;
                    nextWarningAt = t - delays[d][1];
                    out.add(t);
                    break;
                }
            }
        }
        return out;
    }
    
    public static String formatCountdownTime(long l) {
        String out = "";
        if (l >= 3600L) {
            final int h = (int)Math.floor((double)(l / 3600L));
            out = h + TownySettings.getLangString("msg_hours");
            l -= h * 3600;
        }
        if (l >= 60L) {
            final int m = (int)Math.floor((double)(l / 60L));
            out = out + ((out.length() > 0) ? ", " : "") + m + TownySettings.getLangString("msg_minutes");
            l -= m * 60;
        }
        if (out.length() == 0 || l > 0L) {
            out = out + ((out.length() > 0) ? ", " : "") + l + TownySettings.getLangString("msg_seconds");
        }
        return out;
    }
    
    public static String getFormattedTimeValue(final double timeMillis) {
        if (timeMillis > 0.0) {
            final NumberFormat numberFormat = NumberFormat.getInstance();
            String timeUnit;
            double timeUtilCompletion;
            if (timeMillis / 8.64E7 > 1.0) {
                numberFormat.setMaximumFractionDigits(1);
                timeUnit = TownySettings.getLangString("msg_days");
                timeUtilCompletion = timeMillis / 8.64E7;
            }
            else if (timeMillis / 3600000.0 > 1.0) {
                numberFormat.setMaximumFractionDigits(1);
                timeUnit = TownySettings.getLangString("msg_hours");
                timeUtilCompletion = timeMillis / 3600000.0;
            }
            else if (timeMillis / 60000.0 > 1.0) {
                numberFormat.setMaximumFractionDigits(1);
                timeUnit = TownySettings.getLangString("msg_minutes");
                timeUtilCompletion = timeMillis / 60000.0;
            }
            else {
                numberFormat.setMaximumFractionDigits(0);
                timeUnit = TownySettings.getLangString("msg_seconds");
                timeUtilCompletion = timeMillis / 1000.0;
            }
            final double timeRoundedUp = Math.ceil(timeUtilCompletion * 10.0) / 10.0;
            return numberFormat.format(timeRoundedUp) + timeUnit;
        }
        return "0" + TownySettings.getLangString("msg_seconds");
    }
    
    static {
        defaultCountdownDelays = new long[][] { { 10L, 1L }, { 30L, 5L }, { 60L, 10L }, { 300L, 60L }, { 1800L, 300L }, { 3600L, 600L }, { 86400L, 3600L }, { 2147483647L, 86400L } };
    }
}
