// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import java.util.regex.PatternSyntaxException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import com.palmergames.bukkit.towny.TownySettings;
import javax.naming.InvalidNameException;
import java.util.regex.Pattern;

public class NameValidation
{
    private static Pattern namePattern;
    private static Pattern stringPattern;
    
    public static String checkAndFilterName(final String name) throws InvalidNameException {
        final String out = filterName(name);
        if (isBlacklistName(out)) {
            throw new InvalidNameException(out + " is an invalid name.");
        }
        return out;
    }
    
    public static String checkAndFilterPlayerName(final String name) throws InvalidNameException {
        final String out = filterName(name);
        if (!isValidName(out)) {
            throw new InvalidNameException(out + " is an invalid name.");
        }
        return out;
    }
    
    public static String[] checkAndFilterArray(final String[] arr) {
        int count = 0;
        for (final String word : arr) {
            arr[count] = filterName(word);
            ++count;
        }
        return arr;
    }
    
    public static boolean isBlacklistName(final String name) {
        if (name.length() > TownySettings.getMaxNameLength()) {
            return true;
        }
        final ArrayList<String> bannedNames = new ArrayList<String>(Arrays.asList("list", "new", "here", "help", "?", "leave", "withdraw", "deposit", "set", "toggle", "mayor", "assistant", "kick", "add", "claim", "unclaim", "title", "outpost", "ranklist", "invite", "invites", "buy", "create"));
        return bannedNames.contains(name.toLowerCase()) || !isValidName(name);
    }
    
    public static boolean isValidName(final String name) {
        try {
            if (NameValidation.namePattern == null) {
                NameValidation.namePattern = Pattern.compile(TownySettings.getNameCheckRegex(), 258);
            }
            return NameValidation.namePattern.matcher(name).find();
        }
        catch (PatternSyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isValidString(final String name) {
        try {
            if (NameValidation.stringPattern == null) {
                NameValidation.stringPattern = Pattern.compile(TownySettings.getStringCheckRegex(), 258);
            }
            return NameValidation.stringPattern.matcher(name).find();
        }
        catch (PatternSyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static String filterName(final String input) {
        return input.replaceAll(TownySettings.getNameFilterRegex(), "_").replaceAll(TownySettings.getNameRemoveRegex(), "");
    }
    
    static {
        NameValidation.namePattern = null;
        NameValidation.stringPattern = null;
    }
}
