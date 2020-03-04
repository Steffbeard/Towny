package com.palmergames.bukkit.towny.regen.block;

import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class BlockMobSpawner extends BlockObject
{
    private EntityType mobType;
    private int delay;
    
    public BlockMobSpawner(final EntityType type) {
        super(BukkitTools.getMaterialId(Material.MOB_SPAWNER));
        this.mobType = type;
    }
    
    public EntityType getSpawnedType() {
        return this.mobType;
    }
    
    public void setSpawnedType(final EntityType mobType) {
        this.mobType = mobType;
    }
    
    public int getDelay() {
        return this.delay;
    }
    
    public void setDelay(final int i) {
        this.delay = i;
    }
}
