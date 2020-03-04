// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.config.ConfigNodes;

public enum TownSpawnLevel
{
    TOWN_RESIDENT(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN, "msg_err_town_spawn_forbidden", "msg_err_town_spawn_forbidden_war", "msg_err_town_spawn_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL, PermissionNodes.TOWNY_SPAWN_TOWN.getNode()), 
    TOWN_RESIDENT_OUTPOST(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN, "msg_err_town_spawn_forbidden", "msg_err_town_spawn_forbidden_war", "msg_err_town_spawn_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL, PermissionNodes.TOWNY_SPAWN_OUTPOST.getNode()), 
    PART_OF_NATION(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL_NATION, "msg_err_town_spawn_nation_forbidden", "msg_err_town_spawn_nation_forbidden_war", "msg_err_town_spawn_nation_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_NATION, PermissionNodes.TOWNY_SPAWN_NATION.getNode()), 
    NATION_ALLY(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL_ALLY, "msg_err_town_spawn_ally_forbidden", "msg_err_town_spawn_nation_forbidden_war", "msg_err_town_spawn_nation_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_ALLY, PermissionNodes.TOWNY_SPAWN_ALLY.getNode()), 
    UNAFFILIATED(ConfigNodes.GTOWN_SETTINGS_ALLOW_TOWN_SPAWN_TRAVEL, "msg_err_public_spawn_forbidden", "msg_err_town_spawn_forbidden_war", "msg_err_town_spawn_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_PUBLIC, PermissionNodes.TOWNY_SPAWN_PUBLIC.getNode()), 
    ADMIN((ConfigNodes)null, (String)null, (String)null, (String)null, (ConfigNodes)null, (String)null);
    
    private ConfigNodes isAllowingConfigNode;
    private ConfigNodes ecoPriceConfigNode;
    private String permissionNode;
    private String notAllowedLangNode;
    private String notAllowedLangNodeWar;
    private String notAllowedLangNodePeace;
    
    private TownSpawnLevel(final ConfigNodes isAllowingConfigNode, final String notAllowedLangNode, final String notAllowedLangNodeWar, final String notAllowedLangNodePeace, final ConfigNodes ecoPriceConfigNode, final String permissionNode) {
        this.isAllowingConfigNode = isAllowingConfigNode;
        this.notAllowedLangNode = notAllowedLangNode;
        this.notAllowedLangNodeWar = notAllowedLangNodeWar;
        this.notAllowedLangNodePeace = notAllowedLangNodePeace;
        this.ecoPriceConfigNode = ecoPriceConfigNode;
        this.permissionNode = permissionNode;
    }
    
    public void checkIfAllowed(final Towny plugin, final Player player, final Town town) throws TownyException {
        if (this.isAllowed(town) && this.hasPermissionNode(plugin, player, town)) {
            return;
        }
        final boolean war = TownyAPI.getInstance().isWarTime() || TownyWar.isUnderAttack(town);
        final SpawnLevel level = TownySettings.getSpawnLevel(this.isAllowingConfigNode);
        if (level == SpawnLevel.WAR && !war) {
            throw new TownyException(TownySettings.getLangString(this.notAllowedLangNodeWar));
        }
        if (level == SpawnLevel.PEACE && war) {
            throw new TownyException(TownySettings.getLangString(this.notAllowedLangNodePeace));
        }
        throw new TownyException(TownySettings.getLangString(this.notAllowedLangNode));
    }
    
    public boolean isAllowed(final Town town) {
        return this == TownSpawnLevel.ADMIN || this.isAllowedTown(town);
    }
    
    public boolean hasPermissionNode(final Towny plugin, final Player player, final Town town) {
        return this == TownSpawnLevel.ADMIN || (TownyUniverse.getInstance().getPermissionSource().has(player, this.permissionNode) && this.isAllowedTown(town));
    }
    
    private boolean isAllowedTown(final Town town) {
        final boolean war = TownyAPI.getInstance().isWarTime() || TownyWar.isUnderAttack(town);
        final SpawnLevel level = TownySettings.getSpawnLevel(this.isAllowingConfigNode);
        return level == SpawnLevel.TRUE || (level != SpawnLevel.FALSE && level == SpawnLevel.WAR == war);
    }
    
    public double getCost() {
        return (this == TownSpawnLevel.ADMIN) ? 0.0 : TownySettings.getDouble(this.ecoPriceConfigNode);
    }
    
    public double getCost(final Town town) {
        return (this == TownSpawnLevel.ADMIN) ? 0.0 : town.getSpawnCost();
    }
    
    public enum SpawnLevel
    {
        TRUE, 
        FALSE, 
        WAR, 
        PEACE;
    }
}
