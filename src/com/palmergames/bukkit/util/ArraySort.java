// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

import org.bukkit.block.Block;
import java.util.Comparator;

public class ArraySort implements Comparator<Block>
{
    private static ArraySort instance;
    
    @Override
    public int compare(final Block blockA, final Block blockB) {
        return blockA.getY() - blockB.getY();
    }
    
    public static ArraySort getInstance() {
        if (ArraySort.instance == null) {
            ArraySort.instance = new ArraySort();
        }
        return ArraySort.instance;
    }
}
