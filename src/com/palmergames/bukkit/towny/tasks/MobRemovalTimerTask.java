package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.util.Iterator;
import com.palmergames.bukkit.towny.TownyMessaging;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.MobRemovalEvent;
import org.bukkit.entity.Entity;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.EntityType;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.World;
import java.util.ArrayList;
import org.bukkit.entity.LivingEntity;
import com.palmergames.bukkit.towny.utils.EntityTypeUtil;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.Towny;
import java.util.List;
import org.bukkit.Server;

public class MobRemovalTimerTask extends TownyTimerTask
{
    private Server server;
    public static List<Class<?>> classesOfWorldMobsToRemove;
    public static List<Class<?>> classesOfTownMobsToRemove;
    private boolean isRemovingKillerBunny;
    
    public MobRemovalTimerTask(final Towny plugin, final Server server) {
        super(plugin);
        this.server = server;
        MobRemovalTimerTask.classesOfWorldMobsToRemove = EntityTypeUtil.parseLivingEntityClassNames(TownySettings.getWorldMobRemovalEntities(), "WorldMob: ");
        MobRemovalTimerTask.classesOfTownMobsToRemove = EntityTypeUtil.parseLivingEntityClassNames(TownySettings.getTownMobRemovalEntities(), "TownMob: ");
        this.isRemovingKillerBunny = TownySettings.isRemovingKillerBunny();
    }
    
    public static boolean isRemovingWorldEntity(final LivingEntity livingEntity) {
        return EntityTypeUtil.isInstanceOfAny(MobRemovalTimerTask.classesOfWorldMobsToRemove, livingEntity);
    }
    
    public static boolean isRemovingTownEntity(final LivingEntity livingEntity) {
        return EntityTypeUtil.isInstanceOfAny(MobRemovalTimerTask.classesOfTownMobsToRemove, livingEntity);
    }
    
    @Override
    public void run() {
        final List<LivingEntity> livingEntitiesToRemove = new ArrayList<LivingEntity>();
        for (final World world : this.server.getWorlds()) {
            TownyWorld townyWorld;
            try {
                townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(world.getName());
            }
            catch (NotRegisteredException | NullPointerException e) {
                continue;
            }
            if (!townyWorld.isUsingTowny()) {
                continue;
            }
            if (townyWorld.isForceTownMobs() && townyWorld.hasWorldMobs()) {
                continue;
            }
            for (final LivingEntity livingEntity : world.getLivingEntities()) {
                final Location livingEntityLoc = livingEntity.getLocation();
                if (!world.isChunkLoaded(livingEntityLoc.getBlockX() >> 4, livingEntityLoc.getBlockZ() >> 4)) {
                    continue;
                }
                final Coord coord = Coord.parseCoord(livingEntityLoc);
                if (!townyWorld.hasTownBlock(coord)) {
                    continue;
                }
                try {
                    final TownBlock townBlock = townyWorld.getTownBlock(coord);
                    if (townyWorld.isForceTownMobs()) {
                        continue;
                    }
                    if (townBlock.getPermissions().mobs) {
                        continue;
                    }
                    final Town town = townBlock.getTown();
                    if (town.hasMobs()) {
                        continue;
                    }
                    if (livingEntity.getType().equals((Object)EntityType.RABBIT) && this.isRemovingKillerBunny && ((Rabbit)livingEntity).getRabbitType().equals((Object)Rabbit.Type.THE_KILLER_BUNNY)) {
                        livingEntitiesToRemove.add(livingEntity);
                        continue;
                    }
                    if (!isRemovingTownEntity(livingEntity)) {
                        continue;
                    }
                }
                catch (NotRegisteredException x) {
                    if (townyWorld.hasWorldMobs()) {
                        continue;
                    }
                    if (!isRemovingWorldEntity(livingEntity)) {
                        continue;
                    }
                }
                if (this.plugin.isCitizens2() && CitizensAPI.getNPCRegistry().isNPC((Entity)livingEntity)) {
                    continue;
                }
                if (TownySettings.isSkippingRemovalOfNamedMobs() && livingEntity.getCustomName() != null) {
                    continue;
                }
                livingEntitiesToRemove.add(livingEntity);
            }
        }
        for (final LivingEntity livingEntity2 : livingEntitiesToRemove) {
            final MobRemovalEvent mobRemovalEvent = new MobRemovalEvent((Entity)livingEntity2);
            this.plugin.getServer().getPluginManager().callEvent((Event)mobRemovalEvent);
            if (!mobRemovalEvent.isCancelled()) {
                TownyMessaging.sendDebugMsg("MobRemoval Removed: " + livingEntity2.toString());
                livingEntity2.remove();
            }
        }
    }
    
    static {
        MobRemovalTimerTask.classesOfWorldMobsToRemove = new ArrayList<Class<?>>();
        MobRemovalTimerTask.classesOfTownMobsToRemove = new ArrayList<Class<?>>();
    }
}
