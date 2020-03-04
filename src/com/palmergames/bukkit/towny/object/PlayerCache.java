// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import java.util.HashMap;

public class PlayerCache
{
    private HashMap<Material, Boolean> buildMatPermission;
    private HashMap<Material, Boolean> destroyMatPermission;
    private HashMap<Material, Boolean> switchMatPermission;
    private HashMap<Material, Boolean> itemUseMatPermission;
    private HashMap<Integer, HashMap<Byte, Boolean>> buildPermission;
    private HashMap<Integer, HashMap<Byte, Boolean>> destroyPermission;
    private HashMap<Integer, HashMap<Byte, Boolean>> switchPermission;
    private HashMap<Integer, HashMap<Byte, Boolean>> itemUsePermission;
    private WorldCoord lastWorldCoord;
    private String blockErrMsg;
    private Location lastLocation;
    private TownBlockStatus townBlockStatus;
    
    public PlayerCache(final TownyWorld world, final Player player) {
        this(new WorldCoord(world.getName(), Coord.parseCoord((Entity)player)));
        this.setLastLocation(player.getLocation());
    }
    
    public PlayerCache(final WorldCoord worldCoord) {
        this.buildMatPermission = new HashMap<Material, Boolean>();
        this.destroyMatPermission = new HashMap<Material, Boolean>();
        this.switchMatPermission = new HashMap<Material, Boolean>();
        this.itemUseMatPermission = new HashMap<Material, Boolean>();
        this.buildPermission = new HashMap<Integer, HashMap<Byte, Boolean>>();
        this.destroyPermission = new HashMap<Integer, HashMap<Byte, Boolean>>();
        this.switchPermission = new HashMap<Integer, HashMap<Byte, Boolean>>();
        this.itemUsePermission = new HashMap<Integer, HashMap<Byte, Boolean>>();
        this.townBlockStatus = TownBlockStatus.UNKOWN;
        this.setLastTownBlock(worldCoord);
    }
    
    public void setLastTownBlock(final WorldCoord worldCoord) {
        this.lastWorldCoord = worldCoord;
    }
    
    public void resetAndUpdate(final WorldCoord worldCoord) {
        this.reset();
        this.setLastTownBlock(worldCoord);
    }
    
    public WorldCoord getLastTownBlock() {
        return this.lastWorldCoord;
    }
    
    public boolean updateCoord(final WorldCoord pos) {
        if (!this.getLastTownBlock().equals(pos)) {
            this.reset();
            this.setLastTownBlock(pos);
            return true;
        }
        return false;
    }
    
    public boolean getCachePermission(final Material material, final TownyPermission.ActionType action) throws NullPointerException {
        switch (action) {
            case BUILD: {
                return this.getBuildPermission(material);
            }
            case DESTROY: {
                return this.getDestroyPermission(material);
            }
            case SWITCH: {
                return this.getSwitchPermission(material);
            }
            case ITEM_USE: {
                return this.getItemUsePermission(material);
            }
            default: {
                throw new NullPointerException();
            }
        }
    }
    
    public void setBuildPermission(final Material material, final Boolean value) {
        this.updateMaps(this.buildMatPermission, material, value);
    }
    
    public void setDestroyPermission(final Material material, final Boolean value) {
        this.updateMaps(this.destroyMatPermission, material, value);
    }
    
    public void setSwitchPermission(final Material material, final Boolean value) {
        this.updateMaps(this.switchMatPermission, material, value);
    }
    
    public void setItemUsePermission(final Material material, final Boolean value) {
        this.updateMaps(this.itemUseMatPermission, material, value);
    }
    
    public boolean getBuildPermission(final Material material) throws NullPointerException {
        return this.getBlockPermission(this.buildMatPermission, material);
    }
    
    public boolean getDestroyPermission(final Material material) throws NullPointerException {
        return this.getBlockPermission(this.destroyMatPermission, material);
    }
    
    public boolean getSwitchPermission(final Material material) throws NullPointerException {
        return this.getBlockPermission(this.switchMatPermission, material);
    }
    
    public Boolean getItemUsePermission(final Material material) throws NullPointerException {
        return this.getBlockPermission(this.itemUseMatPermission, material);
    }
    
    private void updateMaps(final HashMap<Material, Boolean> blockMap, final Material material, final Boolean value) {
        if (!blockMap.containsKey(material)) {
            blockMap.put(material, value);
        }
        else {
            blockMap.get(material);
        }
    }
    
    private boolean getBlockPermission(final HashMap<Material, Boolean> blockMap, final Material material) throws NullPointerException {
        if (!blockMap.containsKey(material)) {
            throw new NullPointerException();
        }
        return blockMap.get(material);
    }
    
    private void reset() {
        this.lastWorldCoord = null;
        this.townBlockStatus = null;
        this.blockErrMsg = null;
        this.buildMatPermission = new HashMap<Material, Boolean>();
        this.destroyMatPermission = new HashMap<Material, Boolean>();
        this.switchMatPermission = new HashMap<Material, Boolean>();
        this.itemUseMatPermission = new HashMap<Material, Boolean>();
    }
    
    public void setStatus(final TownBlockStatus townBlockStatus) {
        this.townBlockStatus = townBlockStatus;
    }
    
    public TownBlockStatus getStatus() throws NullPointerException {
        if (this.townBlockStatus == null) {
            throw new NullPointerException();
        }
        return this.townBlockStatus;
    }
    
    public void setBlockErrMsg(final String blockErrMsg) {
        this.blockErrMsg = blockErrMsg;
    }
    
    public String getBlockErrMsg() {
        final String temp = this.blockErrMsg;
        this.setBlockErrMsg(null);
        return temp;
    }
    
    public boolean hasBlockErrMsg() {
        return this.blockErrMsg != null;
    }
    
    public void setLastLocation(final Location lastLocation) {
        this.lastLocation = lastLocation.clone();
    }
    
    public Location getLastLocation() throws NullPointerException {
        if (this.lastLocation == null) {
            throw new NullPointerException();
        }
        return this.lastLocation;
    }
    
    @Deprecated
    public boolean getCachePermission(final Integer id, final byte data, final TownyPermission.ActionType action) throws NullPointerException {
        switch (action) {
            case BUILD: {
                return this.getBuildPermission(id, data);
            }
            case DESTROY: {
                return this.getDestroyPermission(id, data);
            }
            case SWITCH: {
                return this.getSwitchPermission(id, data);
            }
            case ITEM_USE: {
                return this.getItemUsePermission(id, data);
            }
            default: {
                throw new NullPointerException();
            }
        }
    }
    
    @Deprecated
    public void setBuildPermission(final Integer id, final byte data, final Boolean value) {
        this.updateMaps(this.buildPermission, id, data, value);
    }
    
    @Deprecated
    public void setDestroyPermission(final Integer id, final byte data, final Boolean value) {
        this.updateMaps(this.destroyPermission, id, data, value);
    }
    
    @Deprecated
    public void setSwitchPermission(final Integer id, final byte data, final Boolean value) {
        this.updateMaps(this.switchPermission, id, data, value);
    }
    
    @Deprecated
    public void setItemUsePermission(final Integer id, final byte data, final Boolean value) {
        this.updateMaps(this.itemUsePermission, id, data, value);
    }
    
    @Deprecated
    public boolean getBuildPermission(final Integer id, final byte data) throws NullPointerException {
        return this.getBlockPermission(this.buildPermission, id, data);
    }
    
    @Deprecated
    public boolean getDestroyPermission(final Integer id, final byte data) throws NullPointerException {
        return this.getBlockPermission(this.destroyPermission, id, data);
    }
    
    @Deprecated
    public boolean getSwitchPermission(final Integer id, final byte data) throws NullPointerException {
        return this.getBlockPermission(this.switchPermission, id, data);
    }
    
    @Deprecated
    public Boolean getItemUsePermission(final Integer id, final byte data) throws NullPointerException {
        return this.getBlockPermission(this.itemUsePermission, id, data);
    }
    
    @Deprecated
    private void updateMaps(final HashMap<Integer, HashMap<Byte, Boolean>> blockMap, final Integer id, final byte data, final Boolean value) {
        if (!blockMap.containsKey(id)) {
            final HashMap<Byte, Boolean> map = new HashMap<Byte, Boolean>();
            map.put(data, value);
            blockMap.put(id, map);
        }
        else {
            blockMap.get(id).put(data, value);
        }
    }
    
    @Deprecated
    private boolean getBlockPermission(final HashMap<Integer, HashMap<Byte, Boolean>> blockMap, final Integer id, final byte data) throws NullPointerException {
        if (!blockMap.containsKey(id)) {
            throw new NullPointerException();
        }
        final HashMap<Byte, Boolean> map = blockMap.get(id);
        if (!map.containsKey(data)) {
            throw new NullPointerException();
        }
        return map.get(data);
    }
    
    public enum TownBlockStatus
    {
        UNKOWN, 
        NOT_REGISTERED, 
        OFF_WORLD, 
        ADMIN, 
        UNCLAIMED_ZONE, 
        NATION_ZONE, 
        LOCKED, 
        WARZONE, 
        OUTSIDER, 
        PLOT_OWNER, 
        PLOT_FRIEND, 
        PLOT_TOWN, 
        PLOT_ALLY, 
        TOWN_OWNER, 
        TOWN_RESIDENT, 
        TOWN_ALLY, 
        TOWN_NATION, 
        ENEMY;
    }
}
