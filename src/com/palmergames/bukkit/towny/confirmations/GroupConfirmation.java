// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.confirmations;

import java.util.Objects;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.PlotObjectGroup;

public class GroupConfirmation
{
    private PlotObjectGroup group;
    private Player player;
    private String[] args;
    private TownBlockOwner owner;
    
    public GroupConfirmation(final PlotObjectGroup group, final Player player) {
        this.group = group;
        this.player = player;
    }
    
    public PlotObjectGroup getGroup() {
        return this.group;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String[] getArgs() {
        return this.args;
    }
    
    public void setArgs(final String[] args) {
        this.args = args;
    }
    
    public TownBlockOwner getTownBlockOwner() {
        return this.owner;
    }
    
    public void setTownBlockOwner(final TownBlockOwner owner) {
        this.owner = owner;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final GroupConfirmation that = (GroupConfirmation)o;
        return this.group.equals(that.group);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.group);
    }
}
