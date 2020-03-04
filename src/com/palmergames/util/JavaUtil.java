// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.util;

import java.io.IOException;
import java.util.ArrayList;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class JavaUtil
{
    public static boolean isSubInterface(final Class<?> sup, final Class<?> sub) {
        if (sup.isInterface() && sub.isInterface()) {
            if (sup.equals(sub)) {
                return true;
            }
            for (final Class<?> c : sub.getInterfaces()) {
                if (isSubInterface(sup, c)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static List<String> readTextFromJar(final String path) throws IOException {
        final BufferedReader fin = new BufferedReader(new InputStreamReader(JavaUtil.class.getResourceAsStream(path)));
        final List<String> out = new ArrayList<String>();
        try {
            String line;
            while ((line = fin.readLine()) != null) {
                out.add(line);
            }
        }
        catch (IOException e) {
            throw new IOException(e.getCause());
        }
        finally {
            fin.close();
        }
        return out;
    }
}
