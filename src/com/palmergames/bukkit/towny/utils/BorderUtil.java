// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.utils;

import java.util.Iterator;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.CellBorder;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.List;

public class BorderUtil
{
    public static List<CellBorder> getOuterBorder(final List<WorldCoord> worldCoords) {
        final List<CellBorder> borderCoords = new ArrayList<CellBorder>();
        for (final WorldCoord worldCoord : worldCoords) {
            final CellBorder border = new CellBorder(worldCoord, new boolean[] { !worldCoords.contains(worldCoord.add(-1, 0)), !worldCoords.contains(worldCoord.add(-1, -1)), !worldCoords.contains(worldCoord.add(0, -1)), !worldCoords.contains(worldCoord.add(1, -1)), !worldCoords.contains(worldCoord.add(1, 0)), !worldCoords.contains(worldCoord.add(1, 1)), !worldCoords.contains(worldCoord.add(0, 1)), !worldCoords.contains(worldCoord.add(-1, 1)) });
            if (border.hasAnyBorder()) {
                borderCoords.add(border);
            }
        }
        return borderCoords;
    }
    
    public static List<CellBorder> getPlotBorder(final List<WorldCoord> worldCoords) {
        final List<CellBorder> borderCoords = new ArrayList<CellBorder>();
        for (final WorldCoord worldCoord : worldCoords) {
            final CellBorder border = getPlotBorder(worldCoord);
            borderCoords.add(border);
        }
        return borderCoords;
    }
    
    public static CellBorder getPlotBorder(final WorldCoord worldCoord) {
        return new CellBorder(worldCoord, new boolean[] { true, true, true, true, true, true, true, true });
    }
}
