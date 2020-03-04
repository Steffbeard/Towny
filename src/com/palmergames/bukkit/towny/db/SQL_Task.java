// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.db;

import java.util.List;
import java.util.HashMap;

public class SQL_Task
{
    public final boolean update;
    public final String tb_name;
    public final HashMap<String, Object> args;
    public final List<String> keys;
    
    public SQL_Task(final String tb_name, final HashMap<String, Object> args) {
        this(false, tb_name, args, null);
    }
    
    public SQL_Task(final String tb_name, final HashMap<String, Object> args, final List<String> keys) {
        this(true, tb_name, args, keys);
    }
    
    private SQL_Task(final boolean update, final String tb_name, final HashMap<String, Object> args, final List<String> keys) {
        this.update = update;
        this.tb_name = tb_name;
        this.args = args;
        this.keys = keys;
    }
}
