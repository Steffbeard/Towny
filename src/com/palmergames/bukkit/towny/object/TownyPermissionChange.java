// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

public class TownyPermissionChange
{
    private Object[] args;
    private Action changeAction;
    private boolean changeValue;
    
    public TownyPermissionChange(final Action changeAction, final boolean changeValue, final Object... args) {
        this.changeAction = changeAction;
        this.changeValue = changeValue;
        this.args = args;
    }
    
    public Action getChangeAction() {
        return this.changeAction;
    }
    
    public boolean getChangeValue() {
        return this.changeValue;
    }
    
    public Object[] getArgs() {
        return this.args;
    }
    
    public enum Action
    {
        ALL_PERMS, 
        SINGLE_PERM, 
        PERM_LEVEL, 
        ACTION_TYPE, 
        RESET;
    }
}
