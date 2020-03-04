// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

public class MemMgmt
{
    public static String getMemoryBar(final int size, final Runtime run) {
        final StringBuilder line = new StringBuilder();
        final double percentUsed = (double)((run.totalMemory() - run.freeMemory()) / run.maxMemory());
        final int pivot = (int)Math.floor(size * percentUsed);
        for (int i = 0; i < pivot - 1; ++i) {
            line.append("=");
        }
        if (pivot < size - 1) {
            line.append("+");
        }
        for (int i = pivot + 1; i < size; ++i) {
            line.append("-");
        }
        return line.toString();
    }
    
    public static String getMemSize(final long num) {
        String[] s;
        double n;
        int w;
        for (s = new String[] { "By", "Kb", "Mb", "Gb", "Tb" }, n = (double)num, w = 0; n > 1024.0 && w < s.length - 1; n /= 1024.0, ++w) {}
        return String.format("%.2f %s", n, s[w]);
    }
}
