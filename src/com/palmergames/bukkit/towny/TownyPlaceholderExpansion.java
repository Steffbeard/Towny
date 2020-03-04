// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class TownyPlaceholderExpansion extends PlaceholderExpansion
{
    private Towny plugin;
    
    public TownyPlaceholderExpansion(final Towny plugin) {
        this.plugin = plugin;
    }
    
    public boolean persist() {
        return true;
    }
    
    public boolean canRegister() {
        return true;
    }
    
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }
    
    public String getIdentifier() {
        return "townyadvanced";
    }
    
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
    
    public String onPlaceholderRequest(final Player player, final String identifier) {
        if (player == null) {
            return "";
        }
        Resident resident;
        try {
            resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());
        }
        catch (NotRegisteredException e) {
            return null;
        }
        String town = "";
        String nation = "";
        String balance = "";
        String tag = "";
        String title = "";
        String amount = "";
        String name = "";
        Double cost = 0.0;
        switch (identifier) {
            case "town": {
                try {
                    town = String.format(TownySettings.getPAPIFormattingTown(), resident.getTown().getName());
                }
                catch (NotRegisteredException ex) {}
                return town;
            }
            case "town_formatted": {
                try {
                    town = String.format(TownySettings.getPAPIFormattingTown(), resident.getTown().getFormattedName());
                }
                catch (NotRegisteredException ex2) {}
                return town;
            }
            case "nation": {
                try {
                    nation = String.format(TownySettings.getPAPIFormattingNation(), resident.getTown().getNation().getName());
                }
                catch (NotRegisteredException ex3) {}
                return nation;
            }
            case "nation_formatted": {
                try {
                    nation = String.format(TownySettings.getPAPIFormattingNation(), resident.getTown().getNation().getFormattedName());
                }
                catch (NotRegisteredException ex4) {}
                return nation;
            }
            case "town_balance": {
                try {
                    balance = resident.getTown().getAccount().getHoldingFormattedBalance();
                }
                catch (NotRegisteredException ex5) {}
                return balance;
            }
            case "nation_balance": {
                try {
                    balance = resident.getTown().getNation().getAccount().getHoldingFormattedBalance();
                }
                catch (NotRegisteredException ex6) {}
                return balance;
            }
            case "town_tag": {
                try {
                    tag = String.format(TownySettings.getPAPIFormattingTown(), resident.getTown().getTag());
                }
                catch (NotRegisteredException ex7) {}
                return tag;
            }
            case "town_tag_override": {
                try {
                    if (resident.getTown().hasTag()) {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), resident.getTown().getTag());
                    }
                    else {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), resident.getTown().getName());
                    }
                }
                catch (NotRegisteredException ex8) {}
                return tag;
            }
            case "nation_tag": {
                try {
                    tag = String.format(TownySettings.getPAPIFormattingNation(), resident.getTown().getNation().getTag());
                }
                catch (NotRegisteredException ex9) {}
                return tag;
            }
            case "nation_tag_override": {
                try {
                    if (resident.getTown().getNation().hasTag()) {
                        tag = String.format(TownySettings.getPAPIFormattingNation(), resident.getTown().getNation().getTag());
                    }
                    else {
                        tag = String.format(TownySettings.getPAPIFormattingNation(), resident.getTown().getNation().getName());
                    }
                }
                catch (NotRegisteredException ex10) {}
                return tag;
            }
            case "towny_tag": {
                try {
                    if (resident.hasTown()) {
                        if (resident.getTown().hasTag()) {
                            town = resident.getTown().getTag();
                        }
                        if (resident.getTown().hasNation() && resident.getTown().getNation().hasTag()) {
                            nation = resident.getTown().getNation().getTag();
                        }
                    }
                    if (!nation.isEmpty()) {
                        tag = TownySettings.getPAPIFormattingBoth().replace("%t", town).replace("%n", nation);
                    }
                    else if (!town.isEmpty()) {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), town);
                    }
                }
                catch (NotRegisteredException ex11) {}
                return tag;
            }
            case "towny_formatted": {
                try {
                    if (resident.hasTown()) {
                        town = resident.getTown().getFormattedName();
                        if (resident.getTown().hasNation()) {
                            nation = resident.getTown().getNation().getFormattedName();
                        }
                    }
                    if (!nation.isEmpty()) {
                        tag = TownySettings.getPAPIFormattingBoth().replace("%t", town).replace("%n", nation);
                    }
                    else if (!town.isEmpty()) {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), town);
                    }
                }
                catch (NotRegisteredException ex12) {}
                return tag;
            }
            case "towny_tag_formatted": {
                try {
                    if (resident.hasTown()) {
                        if (resident.getTown().hasTag()) {
                            town = resident.getTown().getTag();
                        }
                        else {
                            town = resident.getTown().getFormattedName();
                        }
                        if (resident.getTown().hasNation()) {
                            if (resident.getTown().getNation().hasTag()) {
                                nation = resident.getTown().getNation().getTag();
                            }
                            else {
                                nation = resident.getTown().getNation().getFormattedName();
                            }
                        }
                    }
                    if (!nation.isEmpty()) {
                        tag = TownySettings.getPAPIFormattingBoth().replace("%t", town).replace("%n", nation);
                    }
                    else if (!town.isEmpty()) {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), town);
                    }
                }
                catch (NotRegisteredException ex13) {}
                return tag;
            }
            case "towny_tag_override": {
                try {
                    if (resident.hasTown()) {
                        if (resident.getTown().hasTag()) {
                            town = resident.getTown().getTag();
                        }
                        else {
                            town = resident.getTown().getName();
                        }
                        if (resident.getTown().hasNation()) {
                            if (resident.getTown().getNation().hasTag()) {
                                nation = resident.getTown().getNation().getTag();
                            }
                            else {
                                nation = resident.getTown().getNation().getName();
                            }
                        }
                    }
                    if (!nation.isEmpty()) {
                        tag = TownySettings.getPAPIFormattingBoth().replace("%t", town).replace("%n", nation);
                    }
                    else if (!town.isEmpty()) {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), town);
                    }
                }
                catch (NotRegisteredException ex14) {}
                return tag;
            }
            case "title": {
                if (resident.hasTitle()) {
                    title = resident.getTitle();
                }
                return title;
            }
            case "surname": {
                if (resident.hasSurname()) {
                    title = resident.getSurname();
                }
                return title;
            }
            case "towny_name_prefix": {
                if (resident.isMayor()) {
                    title = TownySettings.getMayorPrefix(resident);
                }
                if (resident.isKing()) {
                    title = TownySettings.getKingPrefix(resident);
                }
                return title;
            }
            case "towny_name_postfix": {
                if (resident.isMayor()) {
                    title = TownySettings.getMayorPostfix(resident);
                }
                if (resident.isKing()) {
                    title = TownySettings.getKingPostfix(resident);
                }
                return title;
            }
            case "towny_prefix": {
                if (resident.hasTitle()) {
                    title = resident.getTitle();
                }
                else {
                    if (resident.isMayor()) {
                        title = TownySettings.getMayorPrefix(resident);
                    }
                    if (resident.isKing()) {
                        title = TownySettings.getKingPrefix(resident);
                    }
                }
                return title;
            }
            case "towny_postfix": {
                if (resident.hasSurname()) {
                    title = resident.getSurname();
                }
                else {
                    if (resident.isMayor()) {
                        title = TownySettings.getMayorPostfix(resident);
                    }
                    if (resident.isKing()) {
                        title = TownySettings.getKingPostfix(resident);
                    }
                }
                return title;
            }
            case "towny_colour": {
                String colour = "";
                if (!resident.hasTown()) {
                    colour = TownySettings.getPAPIFormattingNomad();
                }
                else {
                    colour = TownySettings.getPAPIFormattingResident();
                    if (resident.isMayor()) {
                        colour = TownySettings.getPAPIFormattingMayor();
                    }
                    if (resident.isKing()) {
                        colour = TownySettings.getPAPIFormattingKing();
                    }
                }
                return colour;
            }
            case "town_residents_amount": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(resident.getTown().getNumResidents());
                    }
                    catch (NotRegisteredException ex15) {}
                }
                return amount;
            }
            case "town_residents_online": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(TownyAPI.getInstance().getOnlinePlayers(resident.getTown()).size());
                    }
                    catch (NotRegisteredException ex16) {}
                }
                return amount;
            }
            case "town_townblocks_used": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(resident.getTown().getTownBlocks().size());
                    }
                    catch (NotRegisteredException ex17) {}
                }
                return amount;
            }
            case "town_townblocks_bought": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(resident.getTown().getPurchasedBlocks());
                    }
                    catch (NotRegisteredException ex18) {}
                }
                return amount;
            }
            case "town_townblocks_bonus": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(resident.getTown().getBonusBlocks());
                    }
                    catch (NotRegisteredException ex19) {}
                }
                return amount;
            }
            case "town_townblocks_maximum": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(TownySettings.getMaxTownBlocks(resident.getTown()));
                    }
                    catch (NotRegisteredException ex20) {}
                }
                return amount;
            }
            case "town_townblocks_natural_maximum": {
                if (resident.hasTown()) {
                    try {
                        amount = String.valueOf(TownySettings.getMaxTownBlocks(resident.getTown()) - resident.getTown().getBonusBlocks() - resident.getTown().getPurchasedBlocks());
                    }
                    catch (NotRegisteredException ex21) {}
                }
                return amount;
            }
            case "town_mayor": {
                if (resident.hasTown()) {
                    try {
                        name = resident.getTown().getMayor().getName();
                    }
                    catch (NotRegisteredException ex22) {}
                }
                return name;
            }
            case "nation_king": {
                if (resident.hasTown()) {
                    try {
                        if (resident.getTown().hasNation()) {
                            name = resident.getTown().getNation().getKing().getName();
                        }
                    }
                    catch (NotRegisteredException ex23) {}
                }
                return name;
            }
            case "resident_friends_amount": {
                amount = String.valueOf(resident.getFriends().size());
                return amount;
            }
            case "nation_residents_amount": {
                if (resident.hasTown()) {
                    try {
                        if (resident.getTown().hasNation()) {
                            amount = String.valueOf(resident.getTown().getNation().getNumResidents());
                        }
                    }
                    catch (NotRegisteredException ex24) {}
                }
                return amount;
            }
            case "nation_residents_online": {
                if (resident.hasTown()) {
                    try {
                        if (resident.getTown().hasNation()) {
                            amount = String.valueOf(TownyAPI.getInstance().getOnlinePlayers(resident.getTown().getNation()).size());
                        }
                    }
                    catch (NotRegisteredException ex25) {}
                }
                return amount;
            }
            case "nation_capital": {
                if (resident.hasTown()) {
                    try {
                        if (resident.getTown().hasNation()) {
                            name = resident.getTown().getNation().getCapital().getName();
                        }
                    }
                    catch (NotRegisteredException ex26) {}
                }
                return name;
            }
            case "daily_town_upkeep": {
                if (resident.hasTown()) {
                    try {
                        cost = TownySettings.getTownUpkeepCost(resident.getTown());
                    }
                    catch (NotRegisteredException ex27) {}
                }
                return String.valueOf(cost);
            }
            case "daily_nation_upkeep": {
                if (resident.hasTown()) {
                    try {
                        if (resident.getTown().hasNation()) {
                            cost = TownySettings.getNationUpkeepCost(resident.getTown().getNation());
                        }
                    }
                    catch (NotRegisteredException ex28) {}
                }
                return String.valueOf(cost);
            }
            case "has_town": {
                return String.valueOf(resident.hasTown());
            }
            case "has_nation": {
                return String.valueOf(resident.hasNation());
            }
            case "nation_tag_town_formatted": {
                try {
                    if (resident.hasTown()) {
                        town = resident.getTown().getFormattedName();
                        if (resident.getTown().hasNation() && resident.getTown().getNation().hasTag()) {
                            nation = resident.getTown().getNation().getTag();
                        }
                    }
                    if (!nation.isEmpty()) {
                        tag = TownySettings.getPAPIFormattingBoth().replace("%t", town).replace("%n", nation);
                    }
                    else if (!town.isEmpty()) {
                        tag = String.format(TownySettings.getPAPIFormattingTown(), town);
                    }
                }
                catch (NotRegisteredException ex29) {}
                return tag;
            }
            default: {
                return null;
            }
        }
    }
}
