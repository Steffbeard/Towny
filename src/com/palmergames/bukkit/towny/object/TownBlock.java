package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.PlotChangeTypeEvent;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.PlotChangeOwnerEvent;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import java.util.HashSet;

public class TownBlock
{
    private TownyWorld world;
    private Town town;
    private Resident resident;
    private TownBlockType type;
    private String name;
    private int x;
    private int z;
    private double plotPrice;
    private boolean locked;
    private boolean outpost;
    private HashSet<CustomDataField> metadata;
    private PlotObjectGroup plotGroup;
    protected TownyPermission permissions;
    protected boolean isChanged;
    
    public TownBlock(final int x, final int z, final TownyWorld world) {
        this.resident = null;
        this.type = TownBlockType.RESIDENTIAL;
        this.name = "";
        this.plotPrice = -1.0;
        this.locked = false;
        this.outpost = false;
        this.metadata = null;
        this.permissions = new TownyPermission();
        this.isChanged = false;
        this.x = x;
        this.z = z;
        this.setWorld(world);
    }
    
    public void setTown(final Town town) {
        try {
            if (this.hasTown()) {
                this.town.removeTownBlock(this);
            }
        }
        catch (NotRegisteredException ex) {}
        this.town = town;
        try {
            town.addTownBlock(this);
        }
        catch (AlreadyRegisteredException ex2) {}
        catch (NullPointerException ex3) {}
    }
    
    public Town getTown() throws NotRegisteredException {
        if (!this.hasTown()) {
            throw new NotRegisteredException(String.format("The TownBlock at (%s, %d, %d) is not registered to a town.", this.world.getName(), this.x, this.z));
        }
        return this.town;
    }
    
    public boolean hasTown() {
        return this.town != null;
    }
    
    public void setResident(final Resident resident) {
        try {
            if (this.hasResident()) {
                this.resident.removeTownBlock(this);
            }
        }
        catch (NotRegisteredException ex2) {}
        this.resident = resident;
        boolean successful;
        try {
            resident.addTownBlock(this);
            successful = true;
        }
        catch (AlreadyRegisteredException | NullPointerException e) {
            successful = false;
        }
        if (successful && resident != null) {
            Bukkit.getPluginManager().callEvent((Event)new PlotChangeOwnerEvent(this.resident, resident, this));
        }
        this.resident = resident;
    }
    
    public Resident getResident() throws NotRegisteredException {
        if (!this.hasResident()) {
            throw new NotRegisteredException(String.format("The TownBlock at (%s, %d, %d) is not registered to a resident.", this.world.getName(), this.x, this.z));
        }
        return this.resident;
    }
    
    public boolean hasResident() {
        return this.resident != null;
    }
    
    public boolean isOwner(final TownBlockOwner owner) {
        try {
            if (owner == this.getTown()) {
                return true;
            }
        }
        catch (NotRegisteredException ex) {}
        try {
            if (owner == this.getResident()) {
                return true;
            }
        }
        catch (NotRegisteredException ex2) {}
        return false;
    }
    
    public void setPlotPrice(final double ForSale) {
        this.plotPrice = ForSale;
    }
    
    public double getPlotPrice() {
        return this.plotPrice;
    }
    
    public boolean isForSale() {
        return this.getPlotPrice() != -1.0;
    }
    
    public void setPermissions(final String line) {
        this.permissions.load(line);
    }
    
    public TownyPermission getPermissions() {
        return this.permissions;
    }
    
    public boolean isChanged() {
        return this.isChanged;
    }
    
    public void setChanged(final boolean isChanged) {
        this.isChanged = isChanged;
    }
    
    public boolean isOutpost() {
        return this.outpost;
    }
    
    public void setOutpost(final boolean outpost) {
        this.outpost = outpost;
    }
    
    public TownBlockType getType() {
        return this.type;
    }
    
    public void setType(final TownBlockType type) {
        if (type != this.type) {
            this.permissions.reset();
        }
        if (type != null) {
            Bukkit.getPluginManager().callEvent((Event)new PlotChangeTypeEvent(this.type, type, this));
        }
        this.type = type;
        switch (type) {
            case RESIDENTIAL:
            case COMMERCIAL:
            case EMBASSY:
            case WILDS:
            case FARM:
            case BANK: {
                if (this.hasResident()) {
                    this.setPermissions(this.resident.getPermissions().toString());
                    break;
                }
                this.setPermissions(this.town.getPermissions().toString());
                break;
            }
            case ARENA: {
                this.setPermissions("pvp");
                break;
            }
            case SPLEEF:
            case JAIL: {
                this.setPermissions("denyAll");
                break;
            }
            case INN: {
                this.setPermissions("residentSwitch,allySwitch,outsiderSwitch");
                break;
            }
        }
        this.setChanged(false);
    }
    
    public void setType(final int typeId) {
        this.setType(TownBlockType.lookup(typeId));
    }
    
    public void setType(String typeName, final Resident resident) throws TownyException, EconomyException {
        if (typeName.equalsIgnoreCase("reset")) {
            typeName = "default";
        }
        final TownBlockType type = TownBlockType.lookup(typeName);
        if (type == null) {
            throw new TownyException(TownySettings.getLangString("msg_err_not_block_type"));
        }
        double cost = 0.0;
        switch (type) {
            case COMMERCIAL: {
                cost = TownySettings.getPlotSetCommercialCost();
                break;
            }
            case EMBASSY: {
                cost = TownySettings.getPlotSetEmbassyCost();
                break;
            }
            case ARENA: {
                cost = TownySettings.getPlotSetArenaCost();
                break;
            }
            case WILDS: {
                cost = TownySettings.getPlotSetWildsCost();
                break;
            }
            case INN: {
                cost = TownySettings.getPlotSetInnCost();
                break;
            }
            case JAIL: {
                cost = TownySettings.getPlotSetJailCost();
                break;
            }
            case FARM: {
                cost = TownySettings.getPlotSetFarmCost();
                break;
            }
            case BANK: {
                cost = TownySettings.getPlotSetBankCost();
                break;
            }
            default: {
                cost = 0.0;
                break;
            }
        }
        if (cost > 0.0 && TownySettings.isUsingEconomy() && !resident.getAccount().payTo(cost, EconomyAccount.SERVER_ACCOUNT, String.format("Plot set to %s", type))) {
            throw new EconomyException(String.format(TownySettings.getLangString("msg_err_cannot_afford_plot_set_type_cost"), type, TownyEconomyHandler.getFormattedBalance(cost)));
        }
        if (cost > 0.0) {
            TownyMessaging.sendMessage(resident, String.format(TownySettings.getLangString("msg_plot_set_cost"), TownyEconomyHandler.getFormattedBalance(cost), type));
        }
        if (this.isJail()) {
            this.getTown().removeJailSpawn(this.getCoord());
        }
        this.setType(type);
    }
    
    public boolean isHomeBlock() {
        try {
            return this.getTown().isHomeBlock(this);
        }
        catch (NotRegisteredException e) {
            return false;
        }
    }
    
    public void setName(final String newName) {
        this.name = newName.replace("_", " ");
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setX(final int x) {
        this.x = x;
    }
    
    public int getX() {
        return this.x;
    }
    
    public void setZ(final int z) {
        this.z = z;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public Coord getCoord() {
        return new Coord(this.x, this.z);
    }
    
    public WorldCoord getWorldCoord() {
        return new WorldCoord(this.world.getName(), this.x, this.z);
    }
    
    public boolean isLocked() {
        return this.locked;
    }
    
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }
    
    public void setWorld(final TownyWorld world) {
        this.world = world;
    }
    
    public TownyWorld getWorld() {
        return this.world;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TownBlock)) {
            return false;
        }
        final TownBlock o = (TownBlock)obj;
        return this.getX() == o.getX() && this.getZ() == o.getZ() && this.getWorld() == o.getWorld();
    }
    
    public void clear() {
        this.setTown(null);
        this.setResident(null);
        this.setWorld(null);
    }
    
    @Override
    public String toString() {
        return this.getWorld().getName() + " (" + this.getCoord() + ")";
    }
    
    public boolean isWarZone() {
        return this.getWorld().isWarZone(this.getCoord());
    }
    
    public boolean isJail() {
        return this.getType() == TownBlockType.JAIL;
    }
    
    public void addMetaData(final CustomDataField md) {
        if (this.getMetadata() == null) {
            this.metadata = new HashSet<CustomDataField>();
        }
        this.getMetadata().add(md);
        TownyUniverse.getInstance().getDataSource().saveTownBlock(this);
    }
    
    public void removeMetaData(final CustomDataField md) {
        if (!this.hasMeta()) {
            return;
        }
        this.getMetadata().remove(md);
        if (this.getMetadata().size() == 0) {
            this.metadata = null;
        }
        TownyUniverse.getInstance().getDataSource().saveTownBlock(this);
    }
    
    public HashSet<CustomDataField> getMetadata() {
        return this.metadata;
    }
    
    public boolean hasMeta() {
        return this.getMetadata() != null;
    }
    
    public void setMetadata(final String str) {
        if (this.metadata == null) {
            this.metadata = new HashSet<CustomDataField>();
        }
        final String[] split;
        final String[] objects = split = str.split(";");
        for (final String object : split) {
            this.metadata.add(CustomDataField.load(object));
        }
    }
    
    public boolean hasPlotObjectGroup() {
        return this.plotGroup != null;
    }
    
    public PlotObjectGroup getPlotObjectGroup() {
        return this.plotGroup;
    }
    
    public void removePlotObjectGroup() {
        this.plotGroup = null;
    }
    
    public void setPlotObjectGroup(final PlotObjectGroup group) {
        this.plotGroup = group;
        try {
            group.addTownBlock(this);
        }
        catch (NullPointerException e) {
            TownyMessaging.sendErrorMsg("Townblock failed to setPlotObjectGroup(group), group is null. " + String.valueOf(group));
        }
    }
}
