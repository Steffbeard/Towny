// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.regen.block;

import org.bukkit.inventory.ItemStack;

public class BlockInventoryHolder extends BlockObject
{
    private ItemStack[] items;
    
    public BlockInventoryHolder(final int typeId, final ItemStack[] items) {
        super(typeId);
        this.setItems(items);
    }
    
    public BlockInventoryHolder(final int typeId, final byte data, final ItemStack[] items) {
        super(typeId, data);
        this.setItems(items);
    }
    
    public ItemStack[] getItems() {
        return this.items;
    }
    
    public void setItems(final ItemStack[] items) {
        this.items = items.clone();
    }
}
