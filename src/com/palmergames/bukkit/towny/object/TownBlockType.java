package com.palmergames.bukkit.towny.object;

import java.util.Iterator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum TownBlockType
{
    RESIDENTIAL(0, 0, "default", "+") {
    }, 
    COMMERCIAL(1, 1, "Shop", "C") {
        @Override
        public double getTax(final Town town) {
            return town.getCommercialPlotTax() + town.getPlotTax();
        }
    }, 
    ARENA(2, 2, "Arena", "A") {
    }, 
    EMBASSY(3, 3, "Embassy", "E") {
        @Override
        public double getTax(final Town town) {
            return town.getEmbassyPlotTax() + town.getPlotTax();
        }
    }, 
    WILDS(4, 4, "Wilds", "W") {
    }, 
    SPLEEF(5, 5, "Spleef", "+") {
    }, 
    INN(6, 6, "Inn", "I") {
    }, 
    JAIL(7, 7, "Jail", "J") {
    }, 
    FARM(8, 8, "Farm", "F") {
    }, 
    BANK(9, 9, "Bank", "B") {
    };
    
    private int id;
    private String name;
    private String asciiMapKey;
    private static final Map<Integer, TownBlockType> idLookup;
    private static final Map<String, TownBlockType> nameLookup;
    
    private TownBlockType(final int id, final String name, final String asciiMapKey) {
        this.id = id;
        this.name = name;
        this.asciiMapKey = asciiMapKey;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public double getTax(final Town town) {
        return town.getPlotTax();
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getAsciiMapKey() {
        return this.asciiMapKey;
    }
    
    public static TownBlockType lookup(final int id) {
        return TownBlockType.idLookup.get(id);
    }
    
    public static TownBlockType lookup(final String name) {
        return TownBlockType.nameLookup.get(name.toLowerCase());
    }
    
    static {
        idLookup = new HashMap<Integer, TownBlockType>();
        nameLookup = new HashMap<String, TownBlockType>();
        for (final TownBlockType s : EnumSet.allOf(TownBlockType.class)) {
            TownBlockType.idLookup.put(s.getId(), s);
            TownBlockType.nameLookup.put(s.toString().toLowerCase(), s);
        }
    }
}
