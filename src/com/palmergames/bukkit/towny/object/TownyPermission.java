// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.TownySettings;
import java.util.Arrays;

public class TownyPermission
{
    protected boolean[][] perms;
    public boolean pvp;
    public boolean fire;
    public boolean explosion;
    public boolean mobs;
    
    public TownyPermission() {
        this.perms = new boolean[PermLevel.values.length][ActionType.values.length];
        this.reset();
    }
    
    public void reset() {
        this.setAll(false);
    }
    
    public void change(final TownyPermissionChange permChange) {
        this.change(permChange.getChangeAction(), permChange.getChangeValue(), permChange.getArgs());
    }
    
    public void change(final TownyPermissionChange.Action permChange, final boolean toValue, final Object... args) {
        if (permChange == TownyPermissionChange.Action.SINGLE_PERM && args.length == 2) {
            this.perms[((PermLevel)args[0]).getIndex()][((ActionType)args[1]).getIndex()] = toValue;
        }
        else if (permChange == TownyPermissionChange.Action.PERM_LEVEL && args.length == 1) {
            Arrays.fill(this.perms[((PermLevel)args[0]).getIndex()], toValue);
        }
        else if (permChange == TownyPermissionChange.Action.ACTION_TYPE && args.length == 1) {
            for (final PermLevel permLevel : PermLevel.values) {
                this.perms[permLevel.getIndex()][((ActionType)args[0]).getIndex()] = toValue;
            }
        }
        else if (permChange == TownyPermissionChange.Action.ALL_PERMS) {
            this.setAllNonEnvironmental(toValue);
        }
        else if (permChange == TownyPermissionChange.Action.RESET && args.length == 1) {
            final TownBlock tb = (TownBlock)args[0];
            tb.setType(tb.getType());
        }
    }
    
    public void setAllNonEnvironmental(final boolean b) {
        for (final boolean[] permLevel : this.perms) {
            Arrays.fill(permLevel, b);
        }
    }
    
    public void setAll(final boolean b) {
        this.setAllNonEnvironmental(b);
        this.pvp = b;
        this.fire = b;
        this.explosion = b;
        this.mobs = b;
    }
    
    public void set(final String s, final boolean b) {
        final String lowerCase = s.toLowerCase();
        switch (lowerCase) {
            case "denyall": {
                this.reset();
                break;
            }
            case "residentbuild": {
                this.perms[PermLevel.RESIDENT.getIndex()][ActionType.BUILD.getIndex()] = b;
                break;
            }
            case "residentdestroy": {
                this.perms[PermLevel.RESIDENT.getIndex()][ActionType.DESTROY.getIndex()] = b;
                break;
            }
            case "residentswitch": {
                this.perms[PermLevel.RESIDENT.getIndex()][ActionType.SWITCH.getIndex()] = b;
                break;
            }
            case "residentitemuse": {
                this.perms[PermLevel.RESIDENT.getIndex()][ActionType.ITEM_USE.getIndex()] = b;
                break;
            }
            case "outsiderbuild": {
                this.perms[PermLevel.OUTSIDER.getIndex()][ActionType.BUILD.getIndex()] = b;
                break;
            }
            case "outsiderdestroy": {
                this.perms[PermLevel.OUTSIDER.getIndex()][ActionType.DESTROY.getIndex()] = b;
                break;
            }
            case "outsiderswitch": {
                this.perms[PermLevel.OUTSIDER.getIndex()][ActionType.SWITCH.getIndex()] = b;
                break;
            }
            case "outsideritemuse": {
                this.perms[PermLevel.OUTSIDER.getIndex()][ActionType.ITEM_USE.getIndex()] = b;
                break;
            }
            case "nationbuild": {
                this.perms[PermLevel.NATION.getIndex()][ActionType.BUILD.getIndex()] = b;
                break;
            }
            case "nationdestroy": {
                this.perms[PermLevel.NATION.getIndex()][ActionType.DESTROY.getIndex()] = b;
                break;
            }
            case "nationswitch": {
                this.perms[PermLevel.NATION.getIndex()][ActionType.SWITCH.getIndex()] = b;
                break;
            }
            case "nationitemuse": {
                this.perms[PermLevel.NATION.getIndex()][ActionType.ITEM_USE.getIndex()] = b;
                break;
            }
            case "allybuild": {
                this.perms[PermLevel.ALLY.getIndex()][ActionType.BUILD.getIndex()] = b;
                break;
            }
            case "allydestroy": {
                this.perms[PermLevel.ALLY.getIndex()][ActionType.DESTROY.getIndex()] = b;
                break;
            }
            case "allyswitch": {
                this.perms[PermLevel.ALLY.getIndex()][ActionType.SWITCH.getIndex()] = b;
                break;
            }
            case "allyitemuse": {
                this.perms[PermLevel.ALLY.getIndex()][ActionType.ITEM_USE.getIndex()] = b;
                break;
            }
            case "pvp": {
                this.pvp = b;
                break;
            }
            case "fire": {
                this.fire = b;
                break;
            }
            case "explosion": {
                this.explosion = b;
                break;
            }
            case "mobs": {
                this.mobs = b;
                break;
            }
        }
    }
    
    public void load(final String s) {
        this.setAll(false);
        final String[] split;
        final String[] tokens = split = s.split(",");
        for (final String token : split) {
            this.set(token, true);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder output = new StringBuilder("");
        for (final PermLevel permLevel : PermLevel.values) {
            final String permLevelName = permLevel.name().toLowerCase();
            for (final ActionType actionType : ActionType.values) {
                if (this.perms[permLevel.getIndex()][actionType.getIndex()]) {
                    if (output.length() != 0) {
                        output.append(',');
                    }
                    output.append(permLevelName).append(actionType.getCommonName());
                }
            }
        }
        if (this.pvp) {
            output.append((output.length() > 0) ? "," : "").append("pvp");
        }
        if (this.fire) {
            output.append((output.length() > 0) ? "," : "").append("fire");
        }
        if (this.explosion) {
            output.append((output.length() > 0) ? "," : "").append("explosion");
        }
        if (this.mobs) {
            output.append((output.length() > 0) ? "," : "").append("mobs");
        }
        if (output.length() == 0) {
            return "denyAll";
        }
        return output.toString();
    }
    
    public boolean getPerm(final PermLevel permLevel, final ActionType type) {
        return this.perms[permLevel.getIndex()][type.getIndex()];
    }
    
    public boolean getResidentPerm(final ActionType type) {
        return this.getPerm(PermLevel.RESIDENT, type);
    }
    
    public boolean getOutsiderPerm(final ActionType type) {
        return this.getPerm(PermLevel.OUTSIDER, type);
    }
    
    public boolean getAllyPerm(final ActionType type) {
        return this.getPerm(PermLevel.ALLY, type);
    }
    
    public boolean getNationPerm(final ActionType type) {
        return this.getPerm(PermLevel.NATION, type);
    }
    
    public String getColoredPermLevel(final ActionType type) {
        return this.getColoredPermLevel(type, type.getCommonName());
    }
    
    public String getColoredPermLevel(final ActionType type, final String typeCommonName) {
        final StringBuilder output = new StringBuilder("§a").append(typeCommonName).append(" = ").append("§7");
        for (final PermLevel permLevel : PermLevel.values) {
            if (this.perms[permLevel.getIndex()][type.getIndex()]) {
                output.append(permLevel.getShortChar());
            }
            else {
                output.append('-');
            }
        }
        return output.toString();
    }
    
    public String getColourString() {
        return this.getColoredPermLevel(ActionType.BUILD) + this.getColoredPermLevel(ActionType.DESTROY, " Destroy");
    }
    
    public String getColourString2() {
        return this.getColoredPermLevel(ActionType.SWITCH) + this.getColoredPermLevel(ActionType.ITEM_USE, " Item");
    }
    
    public void loadDefault(final TownBlockOwner owner) {
        for (final PermLevel permLevel : PermLevel.values) {
            for (final ActionType actionType : ActionType.values) {
                this.perms[permLevel.getIndex()][actionType.getIndex()] = TownySettings.getDefaultPermission(owner, permLevel, actionType);
            }
        }
        if (owner instanceof Town) {
            this.pvp = TownySettings.getPermFlag_Town_Default_PVP();
            this.fire = TownySettings.getPermFlag_Town_Default_FIRE();
            this.explosion = TownySettings.getPermFlag_Town_Default_Explosion();
            this.mobs = TownySettings.getPermFlag_Town_Default_Mobs();
        }
        else {
            this.pvp = owner.getPermissions().pvp;
            this.fire = owner.getPermissions().fire;
            this.explosion = owner.getPermissions().explosion;
            this.mobs = owner.getPermissions().mobs;
        }
    }
    
    public enum ActionType
    {
        BUILD(0, "Build"), 
        DESTROY(1, "Destroy"), 
        SWITCH(2, "Switch"), 
        ITEM_USE(3, "ItemUse");
        
        private static final ActionType[] values;
        private final int index;
        private final String commonName;
        
        private ActionType(final int index, final String commonName) {
            this.index = index;
            this.commonName = commonName;
        }
        
        public int getIndex() {
            return this.index;
        }
        
        public String getCommonName() {
            return this.commonName;
        }
        
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
        
        static {
            values = values();
        }
    }
    
    public enum PermLevel
    {
        RESIDENT(0, 'f'), 
        NATION(1, 'n'), 
        ALLY(2, 'a'), 
        OUTSIDER(3, 'o');
        
        private static final PermLevel[] values;
        private final int index;
        private final char shortVal;
        
        private PermLevel(final int index, final char shortVal) {
            this.index = index;
            this.shortVal = shortVal;
        }
        
        public int getIndex() {
            return this.index;
        }
        
        public char getShortChar() {
            return this.shortVal;
        }
        
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
        
        static {
            values = values();
        }
    }
}
