package com.palmergames.bukkit.towny.utils;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.PlayerCache;
import java.util.List;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.entity.LivingEntity;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Material;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.event.DisallowedPVPEvent;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Wolf;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.Towny;

public class CombatUtil
{
    public static boolean preventDamageCall(final Towny plugin, Entity attacker, final Entity defender) {
        try {
            final TownyWorld world = TownyUniverse.getInstance().getDataSource().getWorld(defender.getWorld().getName());
            if (!world.isUsingTowny()) {
                return false;
            }
            Player a = null;
            Player b = null;
            if (attacker instanceof Projectile) {
                final Projectile projectile = (Projectile)attacker;
                final Object source = projectile.getShooter();
                if (!(source instanceof Entity)) {
                    return false;
                }
                attacker = (Entity)source;
            }
            if (attacker instanceof Player) {
                a = (Player)attacker;
            }
            if (defender instanceof Player) {
                b = (Player)defender;
            }
            return a != b && preventDamageCall(plugin, world, attacker, defender, a, b);
        }
        catch (Exception ex) {
            return false;
        }
    }
    
    public static boolean preventDamageCall(final Towny plugin, final TownyWorld world, final Entity attackingEntity, final Entity defendingEntity, final Player attackingPlayer, final Player defendingPlayer) throws NotRegisteredException {
        if (!world.isUsingTowny()) {
            return false;
        }
        final Coord coord = Coord.parseCoord(defendingEntity);
        TownBlock defenderTB = null;
        TownBlock attackerTB = null;
        try {
            attackerTB = world.getTownBlock(Coord.parseCoord(attackingEntity));
        }
        catch (NotRegisteredException ex) {}
        try {
            defenderTB = world.getTownBlock(coord);
        }
        catch (NotRegisteredException ex2) {}
        if (attackingPlayer != null) {
            if (defendingPlayer != null || (defenderTB != null && defendingEntity instanceof Wolf && ((Wolf)defendingEntity).isTamed() && !((Wolf)defendingEntity).getOwner().equals(attackingEntity))) {
                if (world.isWarZone(coord)) {
                    return false;
                }
                if (isPvPPlot(attackingPlayer, defendingPlayer)) {
                    return false;
                }
                if (preventFriendlyFire(attackingPlayer, defendingPlayer) || preventPvP(world, attackerTB) || preventPvP(world, defenderTB)) {
                    final DisallowedPVPEvent event = new DisallowedPVPEvent(attackingPlayer, defendingPlayer);
                    plugin.getServer().getPluginManager().callEvent((Event)event);
                    return !event.isCancelled();
                }
            }
            else {
                if (defenderTB != null) {
                    if (defenderTB.getType() == TownBlockType.FARM && TownySettings.getFarmAnimals().contains(defendingEntity.getType().toString()) && PlayerCacheUtil.getCachePermission(attackingPlayer, attackingPlayer.getLocation(), Material.WHEAT, TownyPermission.ActionType.DESTROY)) {
                        return false;
                    }
                    final List<Class<?>> prots = EntityTypeUtil.parseLivingEntityClassNames(TownySettings.getEntityTypes(), "TownMobPVM:");
                    if (EntityTypeUtil.isInstanceOfAny(prots, defendingEntity) && !PlayerCacheUtil.getCachePermission(attackingPlayer, attackingPlayer.getLocation(), Material.DIRT, TownyPermission.ActionType.DESTROY)) {
                        return true;
                    }
                }
                Material block = null;
                switch (defendingEntity.getType()) {
                    case ITEM_FRAME: {
                        block = Material.ITEM_FRAME;
                        break;
                    }
                    case PAINTING: {
                        block = Material.PAINTING;
                        break;
                    }
                    case MINECART: {
                        block = Material.MINECART;
                        break;
                    }
                    case MINECART_CHEST: {
                        block = Material.STORAGE_MINECART;
                        break;
                    }
                    case MINECART_FURNACE: {
                        block = Material.POWERED_MINECART;
                        break;
                    }
                    case MINECART_COMMAND: {
                        block = Material.COMMAND_MINECART;
                        break;
                    }
                    case MINECART_HOPPER: {
                        block = Material.HOPPER_MINECART;
                        break;
                    }
                }
                if (block != null) {
                    final boolean bDestroy = PlayerCacheUtil.getCachePermission(attackingPlayer, defendingEntity.getLocation(), block, TownyPermission.ActionType.DESTROY);
                    if (!bDestroy) {
                        final PlayerCache cache = plugin.getCache(attackingPlayer);
                        if (cache.hasBlockErrMsg()) {
                            TownyMessaging.sendErrorMsg(attackingPlayer, cache.getBlockErrMsg());
                        }
                        return true;
                    }
                }
            }
        }
        if (attackingEntity instanceof Wolf && ((Wolf)attackingEntity).isTamed() && defendingPlayer != null && (preventPvP(world, attackerTB) || preventPvP(world, defenderTB))) {
            ((Wolf)attackingEntity).setTarget((LivingEntity)null);
            return true;
        }
        return false;
    }
    
    public static boolean preventPvP(final TownyWorld world, final TownBlock townBlock) {
        if (townBlock != null) {
            try {
                if (townBlock.getTown().isAdminDisabledPVP()) {
                    return true;
                }
                if (!townBlock.getTown().isPVP() && !townBlock.getPermissions().pvp && !world.isForcePVP()) {
                    return true;
                }
                if (townBlock.isHomeBlock() && world.isForcePVP() && TownySettings.isForcePvpNotAffectingHomeblocks()) {
                    return true;
                }
            }
            catch (NotRegisteredException ex) {
                if (!isWorldPvP(world)) {
                    return true;
                }
            }
        }
        else if (!isWorldPvP(world)) {
            return true;
        }
        return false;
    }
    
    public static boolean isWorldPvP(final TownyWorld world) {
        return world.isForcePVP() || world.isPVP();
    }
    
    public static boolean preventFriendlyFire(final Player attacker, final Player defender) {
        if (attacker == defender) {
            return false;
        }
        if (attacker != null && defender != null && !TownySettings.getFriendlyFire() && isAlly(attacker.getName(), defender.getName())) {
            try {
                final TownBlock townBlock = new WorldCoord(defender.getWorld().getName(), Coord.parseCoord((Entity)defender)).getTownBlock();
                if (!townBlock.getType().equals(TownBlockType.ARENA)) {
                    attacker.sendMessage(TownySettings.getLangString("msg_err_friendly_fire_disable"));
                }
                return true;
            }
            catch (TownyException x) {
                attacker.sendMessage(TownySettings.getLangString("msg_err_friendly_fire_disable"));
                return true;
            }
        }
        return false;
    }
    
    public static boolean isPvPPlot(final Player attacker, final Player defender) {
        if (attacker != null && defender != null) {
            try {
                final TownBlock attackerTB = new WorldCoord(attacker.getWorld().getName(), Coord.parseCoord((Entity)attacker)).getTownBlock();
                final TownBlock defenderTB = new WorldCoord(defender.getWorld().getName(), Coord.parseCoord((Entity)defender)).getTownBlock();
                if (defenderTB.getType().equals(TownBlockType.ARENA) && attackerTB.getType().equals(TownBlockType.ARENA)) {
                    return true;
                }
            }
            catch (NotRegisteredException ex) {}
        }
        return false;
    }
    
    public static boolean isAlly(final String attackingResident, final String defendingResident) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final Resident residentA = townyUniverse.getDataSource().getResident(attackingResident);
            final Resident residentB = townyUniverse.getDataSource().getResident(defendingResident);
            if (residentA.getTown() == residentB.getTown()) {
                return true;
            }
            if (residentA.getTown().getNation() == residentB.getTown().getNation()) {
                return true;
            }
            if (residentA.getTown().getNation().hasAlly(residentB.getTown().getNation())) {
                return true;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public static boolean isAlly(final Town a, final Town b) {
        try {
            if (a == b) {
                return true;
            }
            if (a.getNation() == b.getNation()) {
                return true;
            }
            if (a.getNation().hasAlly(b.getNation())) {
                return true;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public static boolean isSameNation(final Town a, final Town b) {
        try {
            if (a == b) {
                return true;
            }
            if (a.getNation() == b.getNation()) {
                return true;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public static boolean isSameTown(final Town a, final Town b) {
        return a == b;
    }
    
    public static boolean canAttackEnemy(final String a, final String b) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final Resident residentA = townyUniverse.getDataSource().getResident(a);
            final Resident residentB = townyUniverse.getDataSource().getResident(b);
            if (residentA.getTown() == residentB.getTown()) {
                return false;
            }
            if (residentA.getTown().getNation() == residentB.getTown().getNation()) {
                return false;
            }
            final Nation nationA = residentA.getTown().getNation();
            final Nation nationB = residentB.getTown().getNation();
            if (nationA.isNeutral() || nationB.isNeutral()) {
                return false;
            }
            if (nationA.hasEnemy(nationB)) {
                return true;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public static boolean areAllAllies(final List<Nation> possibleAllies) {
        if (possibleAllies.size() <= 1) {
            return true;
        }
        for (int i = 0; i < possibleAllies.size() - 1; ++i) {
            if (!possibleAllies.get(i).hasAlly(possibleAllies.get(i + 1))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isEnemy(final String a, final String b) {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final Resident residentA = townyUniverse.getDataSource().getResident(a);
            final Resident residentB = townyUniverse.getDataSource().getResident(b);
            if (residentA.getTown() == residentB.getTown()) {
                return false;
            }
            if (residentA.getTown().getNation() == residentB.getTown().getNation()) {
                return false;
            }
            if (residentA.getTown().getNation().hasEnemy(residentB.getTown().getNation())) {
                return true;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public static boolean isEnemy(final Town a, final Town b) {
        try {
            if (a == b) {
                return false;
            }
            if (a.getNation() == b.getNation()) {
                return false;
            }
            if (a.getNation().hasEnemy(b.getNation())) {
                return true;
            }
        }
        catch (NotRegisteredException e) {
            return false;
        }
        return false;
    }
    
    public boolean isEnemyTownBlock(final Player player, final WorldCoord worldCoord) {
        try {
            return isEnemy(TownyUniverse.getInstance().getDataSource().getResident(player.getName()).getTown(), worldCoord.getTownBlock().getTown());
        }
        catch (NotRegisteredException e) {
            return false;
        }
    }
}
