// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.util.JavaUtil;
import org.bukkit.entity.LivingEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntityTypeUtil
{
    public static boolean isInstanceOfAny(final List<Class<?>> classesOfWorldMobsToRemove2, final Object obj) {
        for (final Class<?> c : classesOfWorldMobsToRemove2) {
            if (c.isInstance(obj)) {
                return true;
            }
        }
        return false;
    }
    
    public static List<Class<?>> parseLivingEntityClassNames(final List<String> mobClassNames, final String errorPrefix) {
        final List<Class<?>> livingEntityClasses = new ArrayList<Class<?>>();
        for (final String mobClassName : mobClassNames) {
            if (mobClassName.isEmpty()) {
                continue;
            }
            try {
                final Class<?> c = Class.forName("org.bukkit.entity." + mobClassName);
                if (!JavaUtil.isSubInterface(LivingEntity.class, c)) {
                    throw new Exception();
                }
                livingEntityClasses.add(c);
            }
            catch (ClassNotFoundException e) {
                TownyMessaging.sendErrorMsg(String.format("%s%s is not an acceptable class.", errorPrefix, mobClassName));
            }
            catch (Exception e2) {
                TownyMessaging.sendErrorMsg(String.format("%s%s is not an acceptable living entity.", errorPrefix, mobClassName));
            }
        }
        return livingEntityClasses;
    }
}
