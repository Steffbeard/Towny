// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar;

import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import java.util.Iterator;
import com.palmergames.bukkit.towny.object.Coord;
import java.util.ArrayList;
import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;

public class CellUnderAttack extends Cell
{
    private Towny plugin;
    private String nameOfFlagOwner;
    private List<Block> beaconFlagBlocks;
    private List<Block> beaconWireframeBlocks;
    private Block flagBaseBlock;
    private Block flagBlock;
    private Block flagLightBlock;
    private int flagColorId;
    private int thread;
    private long timeBetweenColorChange;
    
    public CellUnderAttack(final Towny plugin, final String nameOfFlagOwner, final Block flagBaseBlock) {
        this(plugin, nameOfFlagOwner, flagBaseBlock, TownyWarConfig.getTimeBetweenFlagColorChange());
    }
    
    public CellUnderAttack(final Towny plugin, final String nameOfFlagOwner, final Block flagBaseBlock, final long timeBetweenColorChange) {
        super(flagBaseBlock.getLocation());
        this.plugin = plugin;
        this.nameOfFlagOwner = nameOfFlagOwner;
        this.flagBaseBlock = flagBaseBlock;
        this.flagColorId = 0;
        this.thread = -1;
        final World world = flagBaseBlock.getWorld();
        this.flagBlock = world.getBlockAt(flagBaseBlock.getX(), flagBaseBlock.getY() + 1, flagBaseBlock.getZ());
        this.flagLightBlock = world.getBlockAt(flagBaseBlock.getX(), flagBaseBlock.getY() + 2, flagBaseBlock.getZ());
        this.timeBetweenColorChange = timeBetweenColorChange;
    }
    
    public void loadBeacon() {
        this.beaconFlagBlocks = new ArrayList<Block>();
        this.beaconWireframeBlocks = new ArrayList<Block>();
        if (!TownyWarConfig.isDrawingBeacon()) {
            return;
        }
        final int beaconSize = TownyWarConfig.getBeaconSize();
        if (Coord.getCellSize() < beaconSize) {
            return;
        }
        final Block minBlock = this.getBeaconMinBlock(this.getFlagBaseBlock().getWorld());
        if (this.getMinimumHeightForBeacon() >= minBlock.getY()) {
            return;
        }
        final int outerEdge = beaconSize - 1;
        for (int y = 0; y < beaconSize; ++y) {
            for (int z = 0; z < beaconSize; ++z) {
                for (int x = 0; x < beaconSize; ++x) {
                    final Block block = this.flagBaseBlock.getWorld().getBlockAt(minBlock.getX() + x, minBlock.getY() + y, minBlock.getZ() + z);
                    if (block.isEmpty()) {
                        final int edgeCount = this.getEdgeCount(x, y, z, outerEdge);
                        if (edgeCount == 1) {
                            this.beaconFlagBlocks.add(block);
                        }
                        else if (edgeCount > 1) {
                            this.beaconWireframeBlocks.add(block);
                        }
                    }
                }
            }
        }
    }
    
    private Block getTopOfFlagBlock() {
        return this.flagLightBlock;
    }
    
    private int getMinimumHeightForBeacon() {
        return this.getTopOfFlagBlock().getY() + TownyWarConfig.getBeaconMinHeightAboveFlag();
    }
    
    private int getEdgeCount(final int x, final int y, final int z, final int outerEdge) {
        return (this.zeroOr(x, outerEdge) + this.zeroOr(y, outerEdge) + this.zeroOr(z, outerEdge)) ? 1 : 0;
    }
    
    private boolean zeroOr(final int n, final int max) {
        return n == 0 || n == max;
    }
    
    private Block getBeaconMinBlock(final World world) {
        final int middle = (int)Math.floor(Coord.getCellSize() / 2.0);
        final int radiusCenterExpansion = TownyWarConfig.getBeaconRadius() - 1;
        final int fromCorner = middle - radiusCenterExpansion;
        final int x = this.getX() * Coord.getCellSize() + fromCorner;
        final int z = this.getZ() * Coord.getCellSize() + fromCorner;
        final int maxY = world.getMaxHeight();
        int y = this.getTopOfFlagBlock().getY() + TownyWarConfig.getBeaconMaxHeightAboveFlag();
        if (y > maxY) {
            y = maxY - TownyWarConfig.getBeaconSize();
        }
        return world.getBlockAt(x, y, z);
    }
    
    public Block getFlagBaseBlock() {
        return this.flagBaseBlock;
    }
    
    public String getNameOfFlagOwner() {
        return this.nameOfFlagOwner;
    }
    
    public boolean hasEnded() {
        return this.flagColorId >= TownyWarConfig.getWoolColors().length;
    }
    
    public void changeFlag() {
        ++this.flagColorId;
        this.updateFlag();
    }
    
    public void drawFlag() {
        this.loadBeacon();
        this.flagBaseBlock.setType(TownyWarConfig.getFlagBaseMaterial());
        this.updateFlag();
        this.flagLightBlock.setType(TownyWarConfig.getFlagLightMaterial());
        for (final Block block : this.beaconWireframeBlocks) {
            block.setType(TownyWarConfig.getBeaconWireFrameMaterial());
        }
    }
    
    public void updateFlag() {
        final Material[] woolColors = TownyWarConfig.getWoolColors();
        if (this.flagColorId < woolColors.length) {
            System.out.println(String.format("Flag at %s turned %s.", this.getCellString(), woolColors[this.flagColorId].toString()));
            this.flagBlock.setType(woolColors[this.flagColorId]);
            for (final Block block : this.beaconFlagBlocks) {
                block.setType(woolColors[this.flagColorId]);
            }
        }
    }
    
    public void destroyFlag() {
        this.flagLightBlock.setType(Material.AIR);
        this.flagBlock.setType(Material.AIR);
        this.flagBaseBlock.setType(Material.AIR);
        for (final Block block : this.beaconFlagBlocks) {
            block.setType(Material.AIR);
        }
        for (final Block block : this.beaconWireframeBlocks) {
            block.setType(Material.AIR);
        }
    }
    
    public void begin() {
        this.drawFlag();
        this.thread = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, (Runnable)new CellAttackThread(this), this.timeBetweenColorChange, this.timeBetweenColorChange);
    }
    
    public void cancel() {
        if (this.thread != -1) {
            this.plugin.getServer().getScheduler().cancelTask(this.thread);
        }
        this.destroyFlag();
    }
    
    public String getCellString() {
        return String.format("%s (%d, %d)", this.getWorldName(), this.getX(), this.getZ());
    }
    
    public boolean isFlagLight(final Block block) {
        return this.flagLightBlock.equals(block);
    }
    
    public boolean isFlag(final Block block) {
        return this.flagBlock.equals(block);
    }
    
    public boolean isFlagBase(final Block block) {
        return this.flagBaseBlock.equals(block);
    }
    
    public boolean isPartOfBeacon(final Block block) {
        return this.beaconFlagBlocks.contains(block) || this.beaconWireframeBlocks.contains(block);
    }
    
    public boolean isUneditableBlock(final Block block) {
        return this.isPartOfBeacon(block) || this.isFlagBase(block) || this.isFlagLight(block);
    }
}
