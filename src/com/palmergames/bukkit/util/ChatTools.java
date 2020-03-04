// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatTools
{
    private static final int lineLength = 54;
    
    public static List<String> listArr(final String[] args, final String prefix) {
        return list(Arrays.asList(args), prefix);
    }
    
    public static List<String> list(final List<String> args) {
        return list(args, "");
    }
    
    public static List<String> list(final List<String> args, final String prefix) {
        if (args.size() > 0) {
            final StringBuilder line = new StringBuilder();
            for (int i = 0; i < args.size() - 1; ++i) {
                line.append(args.get(i)).append(", ");
            }
            line.append(args.get(args.size() - 1));
            return color(prefix + (Object)line);
        }
        return new ArrayList<String>();
    }
    
    private static List<String> wordWrap(final String[] tokens) {
        final List<String> out = new ArrayList<String>();
        out.add("");
        for (final String s : tokens) {
            if (stripColour(out.get(out.size() - 1)).length() + stripColour(s).length() + 1 > 54) {
                out.add("");
            }
            out.set(out.size() - 1, out.get(out.size() - 1) + s + " ");
        }
        return out;
    }
    
    public static List<String> color(final String line) {
        final List<String> out = wordWrap(line.split(" "));
        String c = "f";
        for (int i = 0; i < out.size(); ++i) {
            if (!out.get(i).startsWith("§") && !c.equalsIgnoreCase("f")) {
                out.set(i, "§" + c + out.get(i));
            }
            for (int index = 0; index < 54; ++index) {
                try {
                    if (out.get(i).substring(index, index + 1).equalsIgnoreCase("§")) {
                        c = out.get(i).substring(index + 1, index + 2);
                    }
                }
                catch (Exception ex) {}
            }
        }
        return out;
    }
    
    public static String stripColour(final String s) {
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            final String c = s.substring(i, i + 1);
            if (c.equals("§")) {
                ++i;
            }
            else {
                out.append(c);
            }
        }
        return out.toString();
    }
    
    public static String formatTitle(final String title) {
        final String line = ".oOo.__________________________________________________.oOo.";
        final int pivot = line.length() / 2;
        final String center = ".[ §e" + title + "§6" + " ].";
        String out = "§6" + line.substring(0, Math.max(0, pivot - center.length() / 2));
        out = out + center + line.substring(pivot + center.length() / 2);
        return out;
    }
    
    public static String formatCommand(final String requirement, final String command, final String subCommand, final String help) {
        String out = "  ";
        if (requirement.length() > 0) {
            out = out + "§c" + requirement + ": ";
        }
        out = out + "§3" + command;
        if (subCommand.length() > 0) {
            out = out + " §b" + subCommand;
        }
        if (help.length() > 0) {
            out = out + " §7 : " + help;
        }
        return out;
    }
    
    public static String[] formatList(final String title, final String subject, final List<String> list, final String page) {
        final List<String> output = new ArrayList<String>();
        output.add(0, formatTitle(title));
        output.add(1, subject);
        output.addAll(list);
        output.add(page);
        return output.toArray(new String[0]);
    }
}
