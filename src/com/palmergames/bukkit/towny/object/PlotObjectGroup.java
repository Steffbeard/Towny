// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import java.util.ArrayList;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import java.util.UUID;
import java.util.List;

public class PlotObjectGroup extends ObjectGroup
{
    private Resident resident;
    private List<TownBlock> townBlocks;
    private double price;
    private Town town;
    private TownyPermission permissions;
    
    public PlotObjectGroup(final UUID id, final String name, final Town town) {
        super(id, name);
        this.resident = null;
        this.price = -1.0;
        this.town = town;
    }
    
    @Override
    public String toString() {
        return super.toString() + "," + this.getTown().toString() + "," + this.getPrice();
    }
    
    @Override
    public void setGroupName(final String name) {
        if (this.getGroupName() == null) {
            super.setGroupName(name);
        }
        else {
            final String oldName = this.getGroupName();
            super.setGroupName(name);
            this.town.renamePlotGroup(oldName, this);
        }
    }
    
    public void setTown(final Town town) {
        this.town = town;
        try {
            town.addPlotGroup(this);
        }
        catch (Exception e) {
            TownyMessaging.sendErrorMsg(e.getMessage());
        }
    }
    
    public Town getTown() {
        return this.town;
    }
    
    public String toModeString() {
        return "Group{" + this.toString() + "}";
    }
    
    public double getPrice() {
        return this.price;
    }
    
    public void setResident(final Resident resident) {
        if (this.hasResident()) {
            this.resident = resident;
        }
    }
    
    public Resident getResident() throws NotRegisteredException {
        if (!this.hasResident()) {
            throw new NotRegisteredException("The Group " + this.toString() + "is not registered to a resident.");
        }
        return this.resident;
    }
    
    public boolean hasResident() {
        return this.resident != null;
    }
    
    public void addTownBlock(final TownBlock townBlock) {
        if (this.townBlocks == null) {
            this.townBlocks = new ArrayList<TownBlock>();
        }
        this.townBlocks.add(townBlock);
    }
    
    public void removeTownBlock(final TownBlock townBlock) {
        if (this.townBlocks != null) {
            this.townBlocks.remove(townBlock);
        }
    }
    
    public List<TownBlock> getTownBlocks() {
        return this.townBlocks;
    }
    
    public void setPrice(final double price) {
        this.price = price;
    }
    
    public void addPlotPrice(final double pPrice) {
        if (this.getPrice() == -1.0) {
            this.price = pPrice;
            return;
        }
        this.price += pPrice;
    }
    
    public TownyPermission getPermissions() {
        return this.permissions;
    }
    
    public void setPermissions(final TownyPermission permissions) {
        this.permissions = permissions;
    }
}
