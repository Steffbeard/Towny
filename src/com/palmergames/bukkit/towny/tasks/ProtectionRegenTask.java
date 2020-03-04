package com.palmergames.bukkit.towny.tasks;

import org.bukkit.GrassSpecies;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import com.palmergames.bukkit.towny.regen.NeedsPlaceholder;
import org.bukkit.material.LongGrass;
import org.bukkit.material.Colorable;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;
import org.bukkit.material.Gate;
import org.bukkit.material.Stairs;
import org.bukkit.material.Tree;
import org.bukkit.material.Attachable;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.material.Directional;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Door;
import org.bukkit.block.Chest;
import org.bukkit.inventory.InventoryHolder;
import java.util.ArrayList;
import org.bukkit.block.Block;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import com.palmergames.bukkit.towny.regen.block.BlockLocation;
import org.bukkit.block.BlockState;

public class ProtectionRegenTask extends TownyTimerTask
{
    private BlockState state;
    private BlockState altState;
    private BlockLocation blockLocation;
    private int TaskId;
    private List<ItemStack> contents;
    private static final Material placeholder;
    
    public ProtectionRegenTask(final Towny plugin, final Block block, final boolean update) {
        super(plugin);
        this.contents = new ArrayList<ItemStack>();
        this.state = block.getState();
        this.altState = null;
        this.setBlockLocation(new BlockLocation(block.getLocation()));
        if (this.state instanceof InventoryHolder) {
            Inventory inven;
            if (this.state instanceof Chest) {
                inven = ((Chest)this.state).getBlockInventory();
            }
            else {
                inven = ((InventoryHolder)this.state).getInventory();
            }
            for (final ItemStack item : inven.getContents()) {
                this.contents.add((item != null) ? item.clone() : null);
            }
            inven.clear();
        }
        if (update) {
            if (this.state.getData() instanceof Door) {
                final Door door = (Door)this.state.getData();
                Block topHalf;
                Block bottomHalf;
                if (door.isTopHalf()) {
                    topHalf = block;
                    bottomHalf = block.getRelative(BlockFace.DOWN);
                }
                else {
                    bottomHalf = block;
                    topHalf = block.getRelative(BlockFace.UP);
                }
                bottomHalf.setTypeId(0);
                topHalf.setTypeId(0);
            }
            else if (this.state.getData() instanceof PistonExtensionMaterial) {
                final PistonExtensionMaterial extension = (PistonExtensionMaterial)this.state.getData();
                final Block piston = block.getRelative(extension.getAttachedFace());
                if (piston.getTypeId() != 0) {
                    this.altState = piston.getState();
                    piston.setTypeId(0, false);
                }
                block.setTypeId(0, false);
            }
            else {
                block.setTypeId(0, false);
            }
        }
    }
    
    @Override
    public void run() {
        this.replaceProtections();
        TownyRegenAPI.removeProtectionRegenTask(this);
    }
    
    public void replaceProtections() {
        try {
            final Block block = this.state.getBlock();
            if (this.state.getData() instanceof Door) {
                final Door door = (Door)this.state.getData();
                BlockFace face = null;
                boolean isOpen = false;
                boolean isHinge = false;
                Block topHalf = null;
                Block bottomHalf = null;
                if (door.isTopHalf()) {
                    topHalf = block;
                    bottomHalf = block.getRelative(BlockFace.DOWN);
                }
                else {
                    bottomHalf = block;
                    topHalf = block.getRelative(BlockFace.UP);
                }
                if (!door.isTopHalf()) {
                    isOpen = door.isOpen();
                    face = door.getFacing();
                    bottomHalf.setType(this.state.getType(), false);
                    topHalf.setType(this.state.getType(), false);
                    final BlockState topHalfState = topHalf.getState();
                    final Door topHalfData = (Door)topHalfState.getData();
                    topHalfData.setTopHalf(true);
                    topHalfState.setData((MaterialData)topHalfData);
                    topHalfState.update();
                    final BlockState bottomHalfState = bottomHalf.getState();
                    final Door bottomHalfData = (Door)bottomHalfState.getData();
                    bottomHalfData.setOpen(isOpen);
                    bottomHalfData.setFacingDirection(face);
                    bottomHalfState.setData((MaterialData)bottomHalfData);
                    bottomHalfState.update();
                }
                else {
                    topHalf.setType(this.state.getType(), false);
                    final BlockState topHalfState = topHalf.getState();
                    final Door topHalfData = (Door)topHalfState.getData();
                    isHinge = door.getHinge();
                    final Door otherdoor = (Door)topHalf.getRelative(BlockFace.DOWN).getState().getData();
                    isOpen = otherdoor.isOpen();
                    face = otherdoor.getFacing();
                    topHalfData.setFacingDirection(face);
                    topHalfData.setOpen(isOpen);
                    topHalfData.setHinge(isHinge);
                    topHalfData.setTopHalf(true);
                    topHalfState.setData((MaterialData)topHalfData);
                    topHalfState.update();
                }
            }
            else if (this.state instanceof Sign) {
                final org.bukkit.material.Sign oldSign = (org.bukkit.material.Sign)this.state.getData();
                if (this.state.getType().equals((Object)Material.WALL_SIGN)) {
                    final Block attachedBlock = block.getRelative(oldSign.getAttachedFace());
                    if (attachedBlock.getType().equals((Object)Material.AIR)) {
                        attachedBlock.setType(ProtectionRegenTask.placeholder, false);
                        TownyRegenAPI.addPlaceholder(attachedBlock);
                    }
                }
                block.setType(this.state.getType(), false);
                final MaterialData signData = this.state.getData();
                final BlockFace facing = ((Directional)this.state.getData()).getFacing();
                ((Directional)signData).setFacingDirection(facing);
                this.state.setData(signData);
                this.state.update();
                int i = 0;
                for (final String line : ((Sign)this.state).getLines()) {
                    ((Sign)this.state).setLine(i++, line);
                }
                this.state.update(true);
            }
            else if (this.state instanceof CreatureSpawner) {
                block.setType(Material.MOB_SPAWNER);
                final CreatureSpawner spawner = (CreatureSpawner)this.state;
                final EntityType type = ((CreatureSpawner)this.state).getSpawnedType();
                spawner.setSpawnedType(type);
                this.state.update();
            }
            else if (this.state instanceof Chest) {
                block.setType(this.state.getType(), false);
                final BlockFace facing2 = ((Directional)this.state.getData()).getFacing();
                final MaterialData chestData = this.state.getData();
                ((Directional)chestData).setFacingDirection(facing2);
                final Inventory container = ((Chest)block.getState()).getBlockInventory();
                container.setContents((ItemStack[])this.contents.toArray(new ItemStack[0]));
                this.state.setData(chestData);
                this.state.update();
            }
            else if (this.state instanceof ShulkerBox) {
                block.setType(this.state.getType());
                final MaterialData shulkerData = this.state.getData();
                final Inventory container2 = ((ShulkerBox)block.getState()).getInventory();
                container2.setContents((ItemStack[])this.contents.toArray(new ItemStack[0]));
                this.state.setData(shulkerData);
                this.state.update();
            }
            else if (this.state instanceof InventoryHolder) {
                block.setType(this.state.getType(), false);
                final BlockFace facing2 = ((Directional)this.state.getData()).getFacing();
                final MaterialData holderData = this.state.getData();
                ((Directional)holderData).setFacingDirection(facing2);
                final Inventory container = ((InventoryHolder)block.getState()).getInventory();
                container.setContents((ItemStack[])this.contents.toArray(new ItemStack[0]));
                this.state.setData(holderData);
                this.state.update();
            }
            else if (this.state.getData() instanceof PistonBaseMaterial) {
                if (block.getType().equals((Object)Material.AIR)) {
                    if (this.state.getType().equals((Object)Material.PISTON_BASE)) {
                        block.setType(Material.PISTON_BASE);
                    }
                    else if (this.state.getType().equals((Object)Material.PISTON_STICKY_BASE)) {
                        block.setType(Material.PISTON_STICKY_BASE);
                    }
                    final PistonBaseMaterial baseData = (PistonBaseMaterial)this.state.getData();
                    final BlockFace facing3 = ((Directional)this.state.getData()).getFacing();
                    baseData.setFacingDirection(facing3);
                    baseData.setPowered(false);
                    this.state.setData((MaterialData)baseData);
                    this.state.update();
                }
            }
            else if (this.state.getData() instanceof Attachable) {
                Block attachedBlock2;
                if (this.state.getData().getItemType().equals((Object)Material.COCOA)) {
                    attachedBlock2 = block.getRelative(((Attachable)this.state.getData()).getAttachedFace().getOppositeFace());
                }
                else {
                    attachedBlock2 = block.getRelative(((Attachable)this.state.getData()).getAttachedFace());
                }
                final BlockFace attachedfacing = block.getRelative(((Attachable)this.state.getData()).getAttachedFace().getOppositeFace()).getFace(block);
                if (attachedBlock2.getType().equals((Object)Material.AIR) && !attachedfacing.equals((Object)BlockFace.DOWN)) {
                    attachedBlock2.setType(ProtectionRegenTask.placeholder, false);
                    TownyRegenAPI.addPlaceholder(attachedBlock2);
                }
                block.setType(this.state.getType());
                final BlockFace facing = ((Directional)this.state.getData()).getFacing();
                final MaterialData stateData = this.state.getData();
                ((Directional)stateData).setFacingDirection(facing);
                this.state.setData(stateData);
                this.state.update();
            }
            else if (this.state.getData() instanceof Tree) {
                block.setType(this.state.getType());
                final Tree stateData2 = (Tree)this.state.getData();
                final BlockFace facing3 = ((Tree)this.state.getData()).getDirection();
                stateData2.setDirection(facing3);
                this.state.setData((MaterialData)stateData2);
                this.state.update();
            }
            else if (this.state.getData() instanceof Stairs) {
                block.setType(this.state.getType());
                final Stairs stateData3 = (Stairs)this.state.getData();
                final BlockFace facing3 = ((Directional)this.state.getData()).getFacing().getOppositeFace();
                final boolean isInverted = ((Stairs)this.state.getData()).isInverted();
                ((Directional)stateData3).setFacingDirection(facing3);
                stateData3.setInverted(isInverted);
                this.state.setData((MaterialData)stateData3);
                this.state.update();
            }
            else if (this.state.getData() instanceof Gate) {
                block.setType(this.state.getType());
                final Gate stateData4 = (Gate)this.state.getData();
                final BlockFace facing3 = ((Directional)this.state.getData()).getFacing();
                ((Directional)stateData4).setFacingDirection(facing3);
                this.state.setData((MaterialData)stateData4);
                this.state.update();
            }
            else if (this.state.getData() instanceof WoodenStep) {
                block.setType(this.state.getType());
                final WoodenStep stateData5 = (WoodenStep)this.state.getData();
                final boolean inverted = ((WoodenStep)this.state.getData()).isInverted();
                stateData5.setInverted(inverted);
                this.state.setData((MaterialData)stateData5);
                this.state.update();
            }
            else if (this.state.getData() instanceof Step) {
                block.setType(this.state.getType());
                final Step stateData6 = (Step)this.state.getData();
                final boolean inverted = ((Step)this.state.getData()).isInverted();
                stateData6.setInverted(inverted);
                this.state.setData((MaterialData)stateData6);
                this.state.update();
            }
            else if (this.state.getData() instanceof Colorable) {
                block.setType(this.state.getType());
                final Colorable stateData7 = (Colorable)this.state.getData();
                final DyeColor colour = ((Colorable)this.state.getData()).getColor();
                stateData7.setColor(colour);
                this.state.setData((MaterialData)stateData7);
                this.state.update();
            }
            else if (this.state.getData() instanceof LongGrass) {
                block.setType(this.state.getType());
                final LongGrass stateData8 = (LongGrass)this.state.getData();
                final GrassSpecies species = ((LongGrass)this.state.getData()).getSpecies();
                stateData8.setSpecies(species);
                this.state.setData((MaterialData)stateData8);
                this.state.update();
            }
            else if (this.state.getType().equals((Object)Material.CONCRETE) || this.state.getType().equals((Object)Material.CONCRETE_POWDER) || this.state.getType().equals((Object)Material.STAINED_CLAY) || this.state.getType().equals((Object)Material.STAINED_GLASS) || this.state.getType().equals((Object)Material.STAINED_GLASS_PANE)) {
                block.setType(this.state.getType());
                final Byte b = this.state.getRawData();
                this.state.setRawData((byte)b);
                this.state.update();
            }
            else {
                if (NeedsPlaceholder.contains(this.state.getType())) {
                    final Block blockBelow = block.getRelative(BlockFace.DOWN);
                    if (blockBelow.getType().equals((Object)Material.AIR)) {
                        if (this.state.getType().equals((Object)Material.CROPS)) {
                            blockBelow.setType(Material.SOIL, true);
                        }
                        else {
                            blockBelow.setType(ProtectionRegenTask.placeholder, true);
                        }
                        TownyRegenAPI.addPlaceholder(blockBelow);
                    }
                }
                if (!this.state.getType().equals((Object)Material.AIR)) {
                    block.setType(this.state.getType());
                }
            }
            TownyRegenAPI.removePlaceholder(block);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public BlockLocation getBlockLocation() {
        return this.blockLocation;
    }
    
    private void setBlockLocation(final BlockLocation blockLocation) {
        this.blockLocation = blockLocation;
    }
    
    public BlockState getState() {
        return this.state;
    }
    
    public int getTaskId() {
        return this.TaskId;
    }
    
    public void setTaskId(final int taskId) {
        this.TaskId = taskId;
    }
    
    static {
        placeholder = Material.DIRT;
    }
}
