// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.util;

public class Compass
{
    public static Point getCompassPointForDirection(final double inDegrees) {
        double degrees = (inDegrees - 90.0) % 360.0;
        if (degrees < 0.0) {
            degrees += 360.0;
        }
        if (0.0 <= degrees && degrees < 22.5) {
            return Point.W;
        }
        if (22.5 <= degrees && degrees < 67.5) {
            return Point.NW;
        }
        if (67.5 <= degrees && degrees < 112.5) {
            return Point.N;
        }
        if (112.5 <= degrees && degrees < 157.5) {
            return Point.NE;
        }
        if (157.5 <= degrees && degrees < 202.5) {
            return Point.E;
        }
        if (202.5 <= degrees && degrees < 247.5) {
            return Point.SE;
        }
        if (247.5 <= degrees && degrees < 292.5) {
            return Point.S;
        }
        if (292.5 <= degrees && degrees < 337.5) {
            return Point.SW;
        }
        if (337.5 <= degrees && degrees < 360.0) {
            return Point.W;
        }
        return null;
    }
    
    public enum Point
    {
        N, 
        NE, 
        E, 
        SE, 
        S, 
        SW, 
        W, 
        NW;
    }
}
