// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import org.bukkit.ChatColor;

public class Colors
{
    public static final String Black = "§0";
    public static final String Navy = "§1";
    public static final String Green = "§2";
    public static final String Blue = "§3";
    public static final String Red = "§4";
    public static final String Purple = "§5";
    public static final String Gold = "§6";
    public static final String LightGray = "§7";
    public static final String Gray = "§8";
    public static final String DarkPurple = "§9";
    public static final String LightGreen = "§a";
    public static final String LightBlue = "§b";
    public static final String Rose = "§c";
    public static final String LightPurple = "§d";
    public static final String Yellow = "§e";
    public static final String White = "§f";
    
    public static String strip(final String line) {
        for (final ChatColor cc : ChatColor.values()) {
            line.replaceAll(cc.toString(), "");
        }
        return line;
    }
}
