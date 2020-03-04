// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.tasks;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayDeque;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Resident;
import java.util.Queue;

public class TeleportWarmupTimerTask extends TownyTimerTask
{
    private static Queue<Resident> teleportQueue;
    
    public TeleportWarmupTimerTask(final Towny plugin) {
        super(plugin);
        TeleportWarmupTimerTask.teleportQueue = new ArrayDeque<Resident>();
    }
    
    @Override
    public void run() {
        final long currentTime = System.currentTimeMillis();
        while (true) {
            final Resident resident = TeleportWarmupTimerTask.teleportQueue.peek();
            if (resident == null) {
                break;
            }
            if (currentTime <= resident.getTeleportRequestTime() + TownySettings.getTeleportWarmupTime() * 1000) {
                break;
            }
            resident.clearTeleportRequest();
            final Chunk chunk = resident.getTeleportDestination().getWorld().getChunkAt(resident.getTeleportDestination().getBlock());
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            final Player p = TownyAPI.getInstance().getPlayer(resident);
            if (p == null) {
                return;
            }
            p.teleport(resident.getTeleportDestination());
            if (TownySettings.getSpawnCooldownTime() > 0) {
                CooldownTimerTask.addCooldownTimer(resident.getName(), CooldownTimerTask.CooldownType.TELEPORT);
            }
            TeleportWarmupTimerTask.teleportQueue.poll();
        }
    }
    
    public static void requestTeleport(final Resident resident, final Location spawnLoc) {
        resident.setTeleportRequestTime();
        resident.setTeleportDestination(spawnLoc);
        try {
            TeleportWarmupTimerTask.teleportQueue.add(resident);
        }
        catch (NullPointerException e) {
            System.out.println("[Towny] Error: Null returned from teleport queue.");
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
    
    public static void abortTeleportRequest(final Resident resident) {
        if (resident != null && TeleportWarmupTimerTask.teleportQueue.contains(resident)) {
            TeleportWarmupTimerTask.teleportQueue.remove(resident);
            if (resident.getTeleportCost() != 0.0 && TownySettings.isUsingEconomy()) {
                try {
                    resident.getAccount().collect(resident.getTeleportCost(), TownySettings.getLangString("msg_cost_spawn_refund"));
                    resident.setTeleportCost(0.0);
                    TownyMessaging.sendResidentMessage(resident, TownySettings.getLangString("msg_cost_spawn_refund"));
                }
                catch (EconomyException e) {
                    e.printStackTrace();
                }
                catch (TownyException ex) {}
            }
        }
    }
}
