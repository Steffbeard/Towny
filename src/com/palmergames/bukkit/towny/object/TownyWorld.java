// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.entity.Entity;
import org.bukkit.Material;
import java.util.Collection;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import java.util.Iterator;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class TownyWorld extends TownyObject
{
    private List<Town> towns;
    private boolean isClaimable;
    private boolean isUsingPlotManagementDelete;
    private boolean isUsingPlotManagementMayorDelete;
    private boolean isUsingPlotManagementRevert;
    private boolean isUsingPlotManagementWildRevert;
    private long plotManagementRevertSpeed;
    private long plotManagementWildRevertDelay;
    private List<String> unclaimedZoneIgnoreBlockMaterials;
    private List<String> plotManagementDeleteIds;
    private List<String> plotManagementMayorDelete;
    private List<String> plotManagementIgnoreIds;
    private Boolean unclaimedZoneBuild;
    private Boolean unclaimedZoneDestroy;
    private Boolean unclaimedZoneSwitch;
    private Boolean unclaimedZoneItemUse;
    private String unclaimedZoneName;
    private ConcurrentHashMap<Coord, TownBlock> townBlocks;
    private List<Coord> warZones;
    private List<String> entityExplosionProtection;
    private boolean isUsingTowny;
    private boolean isWarAllowed;
    private boolean isPVP;
    private boolean isForcePVP;
    private boolean isFire;
    private boolean isForceFire;
    private boolean hasWorldMobs;
    private boolean isForceTownMobs;
    private boolean isExplosion;
    private boolean isForceExpl;
    private boolean isEndermanProtect;
    private boolean isDisablePlayerTrample;
    private boolean isDisableCreatureTrample;
    
    public TownyWorld(final String name) {
        super(name);
        this.towns = new ArrayList<Town>();
        this.isClaimable = true;
        this.isUsingPlotManagementDelete = TownySettings.isUsingPlotManagementDelete();
        this.isUsingPlotManagementMayorDelete = TownySettings.isUsingPlotManagementMayorDelete();
        this.isUsingPlotManagementRevert = TownySettings.isUsingPlotManagementRevert();
        this.isUsingPlotManagementWildRevert = TownySettings.isUsingPlotManagementWildRegen();
        this.plotManagementRevertSpeed = TownySettings.getPlotManagementSpeed();
        this.plotManagementWildRevertDelay = TownySettings.getPlotManagementWildRegenDelay();
        this.unclaimedZoneIgnoreBlockMaterials = null;
        this.plotManagementDeleteIds = null;
        this.plotManagementMayorDelete = null;
        this.plotManagementIgnoreIds = null;
        this.unclaimedZoneBuild = null;
        this.unclaimedZoneDestroy = null;
        this.unclaimedZoneSwitch = null;
        this.unclaimedZoneItemUse = null;
        this.unclaimedZoneName = null;
        this.townBlocks = new ConcurrentHashMap<Coord, TownBlock>();
        this.warZones = new ArrayList<Coord>();
        this.entityExplosionProtection = null;
        this.isUsingTowny = TownySettings.isUsingTowny();
        this.isWarAllowed = TownySettings.isWarAllowed();
        this.isPVP = TownySettings.isPvP();
        this.isForcePVP = TownySettings.isForcingPvP();
        this.isFire = TownySettings.isFire();
        this.isForceFire = TownySettings.isForcingFire();
        this.hasWorldMobs = TownySettings.isWorldMonstersOn();
        this.isForceTownMobs = TownySettings.isForcingMonsters();
        this.isExplosion = TownySettings.isExplosions();
        this.isForceExpl = TownySettings.isForcingExplosions();
        this.isEndermanProtect = TownySettings.getEndermanProtect();
        this.isDisablePlayerTrample = TownySettings.isPlayerTramplingCropsDisabled();
        this.isDisableCreatureTrample = TownySettings.isCreatureTramplingCropsDisabled();
    }
    
    public List<Town> getTowns() {
        return this.towns;
    }
    
    public boolean hasTowns() {
        return !this.towns.isEmpty();
    }
    
    public boolean hasTown(final String name) {
        for (final Town town : this.towns) {
            if (town.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasTown(final Town town) {
        return this.towns.contains(town);
    }
    
    public void addTown(final Town town) throws AlreadyRegisteredException {
        if (this.hasTown(town)) {
            throw new AlreadyRegisteredException();
        }
        this.towns.add(town);
        town.setWorld(this);
    }
    
    public TownBlock getTownBlock(final Coord coord) throws NotRegisteredException {
        final TownBlock townBlock = this.townBlocks.get(coord);
        if (townBlock == null) {
            throw new NotRegisteredException();
        }
        return townBlock;
    }
    
    public void newTownBlock(final int x, final int z) throws AlreadyRegisteredException {
        this.newTownBlock(new Coord(x, z));
    }
    
    public TownBlock newTownBlock(final Coord key) throws AlreadyRegisteredException {
        if (this.hasTownBlock(key)) {
            throw new AlreadyRegisteredException();
        }
        this.townBlocks.put(new Coord(key.getX(), key.getZ()), new TownBlock(key.getX(), key.getZ(), this));
        return this.townBlocks.get(new Coord(key.getX(), key.getZ()));
    }
    
    public boolean hasTownBlock(final Coord key) {
        return this.townBlocks.containsKey(key);
    }
    
    public TownBlock getTownBlock(final int x, final int z) throws NotRegisteredException {
        return this.getTownBlock(new Coord(x, z));
    }
    
    public List<TownBlock> getTownBlocks(final Town town) {
        final List<TownBlock> out = new ArrayList<TownBlock>();
        for (final TownBlock townBlock : town.getTownBlocks()) {
            if (townBlock.getWorld() == this) {
                out.add(townBlock);
            }
        }
        return out;
    }
    
    public Collection<TownBlock> getTownBlocks() {
        return this.townBlocks.values();
    }
    
    public void removeTown(final Town town) throws NotRegisteredException {
        if (!this.hasTown(town)) {
            throw new NotRegisteredException();
        }
        this.towns.remove(town);
    }
    
    public void removeTownBlock(final TownBlock townBlock) {
        if (this.hasTownBlock(townBlock.getCoord())) {
            try {
                if (townBlock.hasResident()) {
                    townBlock.getResident().removeTownBlock(townBlock);
                }
            }
            catch (NotRegisteredException ex) {}
            try {
                if (townBlock.hasTown()) {
                    townBlock.getTown().removeTownBlock(townBlock);
                }
            }
            catch (NotRegisteredException ex2) {}
            this.removeTownBlock(townBlock.getCoord());
        }
    }
    
    public void removeTownBlocks(final List<TownBlock> townBlocks) {
        for (final TownBlock townBlock : new ArrayList<TownBlock>(townBlocks)) {
            this.removeTownBlock(townBlock);
        }
    }
    
    public void removeTownBlock(final Coord coord) {
        this.townBlocks.remove(coord);
    }
    
    @Override
    public List<String> getTreeString(final int depth) {
        final List<String> out = new ArrayList<String>();
        out.add(this.getTreeDepth(depth) + "World (" + this.getName() + ")");
        out.add(this.getTreeDepth(depth + 1) + "TownBlocks (" + this.getTownBlocks().size() + "): ");
        return out;
    }
    
    public void setWarAllowed(final boolean isWarAllowed) {
        this.isWarAllowed = isWarAllowed;
    }
    
    public boolean isWarAllowed() {
        return this.isWarAllowed;
    }
    
    public void setPVP(final boolean isPVP) {
        this.isPVP = isPVP;
    }
    
    public boolean isPVP() {
        return this.isPVP;
    }
    
    public void setForcePVP(final boolean isPVP) {
        this.isForcePVP = isPVP;
    }
    
    public boolean isForcePVP() {
        return this.isForcePVP;
    }
    
    public void setExpl(final boolean isExpl) {
        this.isExplosion = isExpl;
    }
    
    public boolean isExpl() {
        return this.isExplosion;
    }
    
    public void setForceExpl(final boolean isExpl) {
        this.isForceExpl = isExpl;
    }
    
    public boolean isForceExpl() {
        return this.isForceExpl;
    }
    
    public void setFire(final boolean isFire) {
        this.isFire = isFire;
    }
    
    public boolean isFire() {
        return this.isFire;
    }
    
    public void setForceFire(final boolean isFire) {
        this.isForceFire = isFire;
    }
    
    public boolean isForceFire() {
        return this.isForceFire;
    }
    
    public void setDisablePlayerTrample(final boolean isDisablePlayerTrample) {
        this.isDisablePlayerTrample = isDisablePlayerTrample;
    }
    
    public boolean isDisablePlayerTrample() {
        return this.isDisablePlayerTrample;
    }
    
    public void setDisableCreatureTrample(final boolean isDisableCreatureTrample) {
        this.isDisableCreatureTrample = isDisableCreatureTrample;
    }
    
    public boolean isDisableCreatureTrample() {
        return this.isDisableCreatureTrample;
    }
    
    public void setWorldMobs(final boolean hasMobs) {
        this.hasWorldMobs = hasMobs;
    }
    
    public boolean hasWorldMobs() {
        return this.hasWorldMobs;
    }
    
    public void setForceTownMobs(final boolean setMobs) {
        this.isForceTownMobs = setMobs;
    }
    
    public boolean isForceTownMobs() {
        return this.isForceTownMobs;
    }
    
    public void setEndermanProtect(final boolean setEnder) {
        this.isEndermanProtect = setEnder;
    }
    
    public boolean isEndermanProtect() {
        return this.isEndermanProtect;
    }
    
    public void setClaimable(final boolean isClaimable) {
        this.isClaimable = isClaimable;
    }
    
    public boolean isClaimable() {
        return this.isUsingTowny() && this.isClaimable;
    }
    
    public void setUsingDefault() {
        this.setUnclaimedZoneBuild(null);
        this.setUnclaimedZoneDestroy(null);
        this.setUnclaimedZoneSwitch(null);
        this.setUnclaimedZoneItemUse(null);
        this.setUnclaimedZoneIgnore(null);
        this.setUnclaimedZoneName(null);
    }
    
    public void setUsingPlotManagementDelete(final boolean using) {
        this.isUsingPlotManagementDelete = using;
    }
    
    public boolean isUsingPlotManagementDelete() {
        return this.isUsingPlotManagementDelete;
    }
    
    public void setUsingPlotManagementMayorDelete(final boolean using) {
        this.isUsingPlotManagementMayorDelete = using;
    }
    
    public boolean isUsingPlotManagementMayorDelete() {
        return this.isUsingPlotManagementMayorDelete;
    }
    
    public void setUsingPlotManagementRevert(final boolean using) {
        this.isUsingPlotManagementRevert = using;
    }
    
    public boolean isUsingPlotManagementRevert() {
        return this.isUsingPlotManagementRevert;
    }
    
    public List<String> getPlotManagementDeleteIds() {
        if (this.plotManagementDeleteIds == null) {
            return TownySettings.getPlotManagementDeleteIds();
        }
        return this.plotManagementDeleteIds;
    }
    
    public boolean isPlotManagementDeleteIds(final String id) {
        return this.getPlotManagementDeleteIds().contains(id);
    }
    
    public void setPlotManagementDeleteIds(final List<String> plotManagementDeleteIds) {
        this.plotManagementDeleteIds = plotManagementDeleteIds;
    }
    
    public List<String> getPlotManagementMayorDelete() {
        if (this.plotManagementMayorDelete == null) {
            return TownySettings.getPlotManagementMayorDelete();
        }
        return this.plotManagementMayorDelete;
    }
    
    public boolean isPlotManagementMayorDelete(final String material) {
        return this.getPlotManagementMayorDelete().contains(material.toUpperCase());
    }
    
    public void setPlotManagementMayorDelete(final List<String> plotManagementMayorDelete) {
        this.plotManagementMayorDelete = plotManagementMayorDelete;
    }
    
    public List<String> getPlotManagementIgnoreIds() {
        if (this.plotManagementIgnoreIds == null) {
            return TownySettings.getPlotManagementIgnoreIds();
        }
        return this.plotManagementIgnoreIds;
    }
    
    public boolean isPlotManagementIgnoreIds(final Material mat) {
        return this.getPlotManagementIgnoreIds().contains(mat.toString());
    }
    
    @Deprecated
    public boolean isPlotManagementIgnoreIds(final String id, final Byte data) {
        return this.getPlotManagementIgnoreIds().contains(id + ":" + Byte.toString(data)) || this.getPlotManagementIgnoreIds().contains(id);
    }
    
    public void setPlotManagementIgnoreIds(final List<String> plotManagementIgnoreIds) {
        this.plotManagementIgnoreIds = plotManagementIgnoreIds;
    }
    
    public boolean isUsingPlotManagementWildRevert() {
        return this.isUsingPlotManagementWildRevert;
    }
    
    public void setUsingPlotManagementWildRevert(final boolean isUsingPlotManagementWildRevert) {
        this.isUsingPlotManagementWildRevert = isUsingPlotManagementWildRevert;
    }
    
    public long getPlotManagementRevertSpeed() {
        return this.plotManagementRevertSpeed;
    }
    
    public void setPlotManagementRevertSpeed(final long plotManagementRevertSpeed) {
        this.plotManagementRevertSpeed = plotManagementRevertSpeed;
    }
    
    public long getPlotManagementWildRevertDelay() {
        return this.plotManagementWildRevertDelay;
    }
    
    public void setPlotManagementWildRevertDelay(final long plotManagementWildRevertDelay) {
        this.plotManagementWildRevertDelay = plotManagementWildRevertDelay;
    }
    
    public void setPlotManagementWildRevertEntities(final List<String> entities) {
        this.entityExplosionProtection = new ArrayList<String>();
        for (final String mob : entities) {
            if (!mob.equals("")) {
                this.entityExplosionProtection.add(mob.toLowerCase());
            }
        }
    }
    
    public List<String> getPlotManagementWildRevertEntities() {
        if (this.entityExplosionProtection == null) {
            this.setPlotManagementWildRevertEntities(TownySettings.getWildExplosionProtectionEntities());
        }
        return this.entityExplosionProtection;
    }
    
    public boolean isProtectingExplosionEntity(final Entity entity) {
        if (this.entityExplosionProtection == null) {
            this.setPlotManagementWildRevertEntities(TownySettings.getWildExplosionProtectionEntities());
        }
        return this.entityExplosionProtection.contains(entity.getType().getEntityClass().getSimpleName().toLowerCase());
    }
    
    public void setUnclaimedZoneIgnore(final List<String> unclaimedZoneIgnoreIds) {
        this.unclaimedZoneIgnoreBlockMaterials = unclaimedZoneIgnoreIds;
    }
    
    public List<String> getUnclaimedZoneIgnoreMaterials() {
        if (this.unclaimedZoneIgnoreBlockMaterials == null) {
            return TownySettings.getUnclaimedZoneIgnoreMaterials();
        }
        return this.unclaimedZoneIgnoreBlockMaterials;
    }
    
    public boolean isUnclaimedZoneIgnoreMaterial(final Material mat) {
        return this.getUnclaimedZoneIgnoreMaterials().contains(mat);
    }
    
    public boolean getUnclaimedZonePerm(final TownyPermission.ActionType type) {
        switch (type) {
            case BUILD: {
                return this.getUnclaimedZoneBuild();
            }
            case DESTROY: {
                return this.getUnclaimedZoneDestroy();
            }
            case SWITCH: {
                return this.getUnclaimedZoneSwitch();
            }
            case ITEM_USE: {
                return this.getUnclaimedZoneItemUse();
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    public Boolean getUnclaimedZoneBuild() {
        if (this.unclaimedZoneBuild == null) {
            return TownySettings.getUnclaimedZoneBuildRights();
        }
        return this.unclaimedZoneBuild;
    }
    
    public void setUnclaimedZoneBuild(final Boolean unclaimedZoneBuild) {
        this.unclaimedZoneBuild = unclaimedZoneBuild;
    }
    
    public Boolean getUnclaimedZoneDestroy() {
        if (this.unclaimedZoneDestroy == null) {
            return TownySettings.getUnclaimedZoneDestroyRights();
        }
        return this.unclaimedZoneDestroy;
    }
    
    public void setUnclaimedZoneDestroy(final Boolean unclaimedZoneDestroy) {
        this.unclaimedZoneDestroy = unclaimedZoneDestroy;
    }
    
    public Boolean getUnclaimedZoneSwitch() {
        if (this.unclaimedZoneSwitch == null) {
            return TownySettings.getUnclaimedZoneSwitchRights();
        }
        return this.unclaimedZoneSwitch;
    }
    
    public void setUnclaimedZoneSwitch(final Boolean unclaimedZoneSwitch) {
        this.unclaimedZoneSwitch = unclaimedZoneSwitch;
    }
    
    public String getUnclaimedZoneName() {
        if (this.unclaimedZoneName == null) {
            return TownySettings.getUnclaimedZoneName();
        }
        return this.unclaimedZoneName;
    }
    
    public void setUnclaimedZoneName(final String unclaimedZoneName) {
        this.unclaimedZoneName = unclaimedZoneName;
    }
    
    public void setUsingTowny(final boolean isUsingTowny) {
        this.isUsingTowny = isUsingTowny;
    }
    
    public boolean isUsingTowny() {
        return this.isUsingTowny;
    }
    
    public void setUnclaimedZoneItemUse(final Boolean unclaimedZoneItemUse) {
        this.unclaimedZoneItemUse = unclaimedZoneItemUse;
    }
    
    public Boolean getUnclaimedZoneItemUse() {
        if (this.unclaimedZoneItemUse == null) {
            return TownySettings.getUnclaimedZoneItemUseRights();
        }
        return this.unclaimedZoneItemUse;
    }
    
    public int getMinDistanceFromOtherTowns(final Coord key) {
        return this.getMinDistanceFromOtherTowns(key, null);
    }
    
    public int getMinDistanceFromOtherTowns(final Coord key, final Town homeTown) {
        double min = 2.147483647E9;
        for (final Town town : this.getTowns()) {
            try {
                final Coord townCoord = town.getHomeBlock().getCoord();
                if (homeTown != null && homeTown.getHomeBlock().equals(town.getHomeBlock())) {
                    continue;
                }
                if (!town.getWorld().equals(this)) {
                    continue;
                }
                final double dist = Math.sqrt(Math.pow(townCoord.getX() - key.getX(), 2.0) + Math.pow(townCoord.getZ() - key.getZ(), 2.0));
                if (dist >= min) {
                    continue;
                }
                min = dist;
            }
            catch (TownyException ex) {}
        }
        return (int)Math.ceil(min);
    }
    
    public int getMinDistanceFromOtherTownsPlots(final Coord key) {
        return this.getMinDistanceFromOtherTownsPlots(key, null);
    }
    
    public int getMinDistanceFromOtherTownsPlots(final Coord key, final Town homeTown) {
        double min = 2.147483647E9;
        for (final Town town : this.getTowns()) {
            try {
                if (homeTown != null && homeTown.getHomeBlock().equals(town.getHomeBlock())) {
                    continue;
                }
                for (final TownBlock b : town.getTownBlocks()) {
                    if (!b.getWorld().equals(this)) {
                        continue;
                    }
                    final Coord townCoord = b.getCoord();
                    if (key.equals(townCoord)) {
                        continue;
                    }
                    final double dist = Math.sqrt(Math.pow(townCoord.getX() - key.getX(), 2.0) + Math.pow(townCoord.getZ() - key.getZ(), 2.0));
                    if (dist >= min) {
                        continue;
                    }
                    min = dist;
                }
            }
            catch (TownyException ex) {}
        }
        return (int)Math.ceil(min);
    }
    
    public Town getClosestTownFromCoord(final Coord key, Town nearestTown) {
        double min = 2.147483647E9;
        for (final Town town : this.getTowns()) {
            for (final TownBlock b : town.getTownBlocks()) {
                if (!b.getWorld().equals(this)) {
                    continue;
                }
                final Coord townCoord = b.getCoord();
                final double dist = Math.sqrt(Math.pow(townCoord.getX() - key.getX(), 2.0) + Math.pow(townCoord.getZ() - key.getZ(), 2.0));
                if (dist >= min) {
                    continue;
                }
                min = dist;
                nearestTown = town;
            }
        }
        return nearestTown;
    }
    
    public Town getClosestTownWithNationFromCoord(final Coord key, Town nearestTown) {
        double min = 2.147483647E9;
        for (final Town town : this.getTowns()) {
            if (!town.hasNation()) {
                continue;
            }
            for (final TownBlock b : town.getTownBlocks()) {
                if (!b.getWorld().equals(this)) {
                    continue;
                }
                final Coord townCoord = b.getCoord();
                final double dist = Math.sqrt(Math.pow(townCoord.getX() - key.getX(), 2.0) + Math.pow(townCoord.getZ() - key.getZ(), 2.0));
                if (dist >= min) {
                    continue;
                }
                min = dist;
                nearestTown = town;
            }
        }
        return nearestTown;
    }
    
    public void addWarZone(final Coord coord) {
        if (!this.isWarZone(coord)) {
            this.warZones.add(coord);
        }
    }
    
    public void removeWarZone(final Coord coord) {
        this.warZones.remove(coord);
    }
    
    public boolean isWarZone(final Coord coord) {
        return this.warZones.contains(coord);
    }
    
    @Override
    public void addMetaData(final CustomDataField md) {
        super.addMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveWorld(this);
    }
    
    @Override
    public void removeMetaData(final CustomDataField md) {
        super.removeMetaData(md);
        TownyUniverse.getInstance().getDataSource().saveWorld(this);
    }
}
