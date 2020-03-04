// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.regen.block;

public class BlockSign extends BlockObject
{
    private String[] lines;
    
    public BlockSign(final int type, final byte data, final String[] lines) {
        super(type, data);
        this.lines = lines;
    }
    
    public BlockSign(final int type, final byte data) {
        super(type, data);
        this.lines = new String[] { "", "", "", "" };
    }
    
    public String[] getLines() {
        return this.lines;
    }
    
    public void setlines(final String[] lines) {
        this.lines = lines;
    }
}
