// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.config.ConfigNodes;

public enum NationSpawnLevel
{
    PART_OF_NATION(ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN, "msg_err_nation_spawn_forbidden", "msg_err_nation_spawn_forbidden_war", "msg_err_nation_spawn_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL, PermissionNodes.TOWNY_NATION_SPAWN_NATION.getNode()), 
    NATION_ALLY(ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN_TRAVEL_ALLY, "msg_err_nation_spawn_ally_forbidden", "msg_err_nation_spawn_nation_forbidden_war", "msg_err_nation_spawn_nation_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_ALLY, PermissionNodes.TOWNY_NATION_SPAWN_ALLY.getNode()), 
    UNAFFILIATED(ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN_TRAVEL, "msg_err_public_nation_spawn_forbidden", "msg_err_public_nation_spawn_forbidden_war", "msg_err_public_nation_spawn_forbidden_peace", ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_PUBLIC, PermissionNodes.TOWNY_SPAWN_PUBLIC.getNode()), 
    ADMIN((ConfigNodes)null, (String)null, (String)null, (String)null, (ConfigNodes)null, (String)null);
    
    private ConfigNodes isAllowingConfigNode;
    private ConfigNodes ecoPriceConfigNode;
    private String permissionNode;
    private String notAllowedLangNode;
    private String notAllowedLangNodeWar;
    private String notAllowedLangNodePeace;
    
    private NationSpawnLevel(final ConfigNodes isAllowingConfigNode, final String notAllowedLangNode, final String notAllowedLangNodeWar, final String notAllowedLangNodePeace, final ConfigNodes ecoPriceConfigNode, final String permissionNode) {
        this.isAllowingConfigNode = isAllowingConfigNode;
        this.notAllowedLangNode = notAllowedLangNode;
        this.notAllowedLangNodeWar = notAllowedLangNodeWar;
        this.notAllowedLangNodePeace = notAllowedLangNodePeace;
        this.ecoPriceConfigNode = ecoPriceConfigNode;
        this.permissionNode = permissionNode;
    }
    
    public void checkIfAllowed(final Towny plugin, final Player player, final Nation nation) throws TownyException {
        if (this.isAllowed(nation) && this.hasPermissionNode(plugin, player, nation)) {
            return;
        }
        final boolean war = TownyAPI.getInstance().isWarTime();
        final NSpawnLevel level = TownySettings.getNSpawnLevel(this.isAllowingConfigNode);
        if (level == NSpawnLevel.WAR && !war) {
            throw new TownyException(TownySettings.getLangString(this.notAllowedLangNodeWar));
        }
        if (level == NSpawnLevel.PEACE && war) {
            throw new TownyException(TownySettings.getLangString(this.notAllowedLangNodePeace));
        }
        throw new TownyException(TownySettings.getLangString(this.notAllowedLangNode));
    }
    
    public boolean isAllowed(final Nation nation) {
        return this == NationSpawnLevel.ADMIN || this.isAllowedNation(nation);
    }
    
    public boolean hasPermissionNode(final Towny plugin, final Player player, final Nation nation) {
        return this == NationSpawnLevel.ADMIN || (TownyUniverse.getInstance().getPermissionSource().has(player, this.permissionNode) && this.isAllowedNation(nation));
    }
    
    private boolean isAllowedNation(final Nation nation) {
        final boolean war = TownyAPI.getInstance().isWarTime();
        final NSpawnLevel level = TownySettings.getNSpawnLevel(this.isAllowingConfigNode);
        return level == NSpawnLevel.TRUE || (level != NSpawnLevel.FALSE && level == NSpawnLevel.WAR == war);
    }
    
    public double getCost() {
        return (this == NationSpawnLevel.ADMIN) ? 0.0 : TownySettings.getDouble(this.ecoPriceConfigNode);
    }
    
    public double getCost(final Nation nation) {
        return (this == NationSpawnLevel.ADMIN) ? 0.0 : nation.getSpawnCost();
    }
    
    public enum NSpawnLevel
    {
        TRUE, 
        FALSE, 
        WAR, 
        PEACE;
    }
}
