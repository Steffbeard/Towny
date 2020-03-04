// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.EconomyResponse;
import java.math.BigDecimal;
import org.bukkit.event.Event;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.event.TownyPreTransactionEvent;
import com.palmergames.bukkit.towny.event.TownyTransactionEvent;
import com.palmergames.bukkit.towny.object.Transaction;
import com.palmergames.bukkit.towny.object.TransactionType;
import org.bukkit.World;
import org.bukkit.Bukkit;
import java.util.UUID;
import net.tnemc.core.economy.ExtendedEconomyAPI;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import net.tnemc.core.Reserve;
import net.tnemc.core.economy.EconomyAPI;
import net.milkbowl.vault.economy.Economy;

public class TownyEconomyHandler
{
    private static Towny plugin;
    private static Economy vaultEconomy;
    private static EconomyAPI reserveEconomy;
    private static EcoType Type;
    private static String version;
    
    public static void initialize(final Towny plugin) {
        TownyEconomyHandler.plugin = plugin;
    }
    
    public static EcoType getType() {
        return TownyEconomyHandler.Type;
    }
    
    public static boolean isActive() {
        return TownyEconomyHandler.Type != EcoType.NONE;
    }
    
    public static String getVersion() {
        return TownyEconomyHandler.version;
    }
    
    private static void setVersion(final String version) {
        TownyEconomyHandler.version = version;
    }
    
    public static Boolean setupEconomy() {
        Plugin economyProvider = null;
        try {
            final RegisteredServiceProvider<Economy> vaultEcoProvider = (RegisteredServiceProvider<Economy>)TownyEconomyHandler.plugin.getServer().getServicesManager().getRegistration((Class)Economy.class);
            if (vaultEcoProvider != null) {
                TownyEconomyHandler.vaultEconomy = (Economy)vaultEcoProvider.getProvider();
                setVersion(String.format("%s %s", ((Economy)vaultEcoProvider.getProvider()).getName(), "via Vault"));
                TownyEconomyHandler.Type = EcoType.VAULT;
                return true;
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {}
        economyProvider = TownyEconomyHandler.plugin.getServer().getPluginManager().getPlugin("Reserve");
        if (economyProvider != null && ((Reserve)economyProvider).economyProvided()) {
            TownyEconomyHandler.reserveEconomy = ((Reserve)economyProvider).economy();
            setVersion(String.format("%s %s", TownyEconomyHandler.reserveEconomy.name(), "via Reserve"));
            TownyEconomyHandler.Type = EcoType.RESERVE;
            return true;
        }
        return false;
    }
    
    private static Object getEconomyAccount(final String accountName) {
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                if (TownyEconomyHandler.reserveEconomy instanceof ExtendedEconomyAPI) {
                    return ((ExtendedEconomyAPI)TownyEconomyHandler.reserveEconomy).getAccount(accountName);
                }
                break;
            }
        }
        return null;
    }
    
    public static boolean hasEconomyAccount(final String accountName) {
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                return TownyEconomyHandler.reserveEconomy.hasAccountDetail(accountName).success();
            }
            case VAULT: {
                return TownyEconomyHandler.vaultEconomy.hasAccount(accountName);
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean hasEconomyAccount(final UUID uniqueId) {
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                return TownyEconomyHandler.reserveEconomy.hasAccountDetail(uniqueId).success();
            }
            case VAULT: {
                return TownyEconomyHandler.vaultEconomy.hasAccount(Bukkit.getOfflinePlayer(uniqueId));
            }
            default: {
                return false;
            }
        }
    }
    
    public static void removeAccount(final String accountName) {
        try {
            switch (TownyEconomyHandler.Type) {
                case RESERVE: {
                    TownyEconomyHandler.reserveEconomy.deleteAccountDetail(accountName);
                    break;
                }
                case VAULT: {
                    if (!TownyEconomyHandler.vaultEconomy.hasAccount(accountName)) {
                        TownyEconomyHandler.vaultEconomy.createPlayerAccount(accountName);
                    }
                    TownyEconomyHandler.vaultEconomy.withdrawPlayer(accountName, TownyEconomyHandler.vaultEconomy.getBalance(accountName));
                }
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {}
    }
    
    public static double getBalance(final String accountName, final World world) {
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                if (!TownyEconomyHandler.reserveEconomy.hasAccountDetail(accountName).success() && !TownyEconomyHandler.reserveEconomy.createAccountDetail(accountName).success()) {
                    return 0.0;
                }
                return TownyEconomyHandler.reserveEconomy.getHoldings(accountName, world.getName()).doubleValue();
            }
            case VAULT: {
                if (!TownyEconomyHandler.vaultEconomy.hasAccount(accountName)) {
                    TownyEconomyHandler.vaultEconomy.createPlayerAccount(accountName);
                }
                return TownyEconomyHandler.vaultEconomy.getBalance(accountName);
            }
            default: {
                return 0.0;
            }
        }
    }
    
    public static boolean hasEnough(final String accountName, final Double amount, final World world) {
        return getBalance(accountName, world) >= amount;
    }
    
    public static boolean subtract(final String accountName, final Double amount, final World world) {
        final Player player = Bukkit.getServer().getPlayer(accountName);
        final Transaction transaction = new Transaction(TransactionType.SUBTRACT, player, amount.intValue());
        final TownyTransactionEvent event = new TownyTransactionEvent(transaction);
        final TownyPreTransactionEvent preEvent = new TownyPreTransactionEvent(transaction);
        BukkitTools.getPluginManager().callEvent((Event)preEvent);
        if (preEvent.isCancelled()) {
            TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
            return false;
        }
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                if (!TownyEconomyHandler.reserveEconomy.hasAccountDetail(accountName).success() && !TownyEconomyHandler.reserveEconomy.createAccountDetail(accountName).success()) {
                    return false;
                }
                BukkitTools.getPluginManager().callEvent((Event)event);
                return TownyEconomyHandler.reserveEconomy.removeHoldingsDetail(accountName, new BigDecimal(amount), world.getName()).success();
            }
            case VAULT: {
                if (!TownyEconomyHandler.vaultEconomy.hasAccount(accountName)) {
                    TownyEconomyHandler.vaultEconomy.createPlayerAccount(accountName);
                }
                BukkitTools.getPluginManager().callEvent((Event)event);
                return TownyEconomyHandler.vaultEconomy.withdrawPlayer(accountName, (double)amount).type == EconomyResponse.ResponseType.SUCCESS;
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean add(final String accountName, final Double amount, final World world) {
        final Player player = Bukkit.getServer().getPlayer(accountName);
        final Transaction transaction = new Transaction(TransactionType.ADD, player, amount.intValue());
        final TownyTransactionEvent event = new TownyTransactionEvent(transaction);
        final TownyPreTransactionEvent preEvent = new TownyPreTransactionEvent(transaction);
        BukkitTools.getPluginManager().callEvent((Event)preEvent);
        if (preEvent.isCancelled()) {
            TownyMessaging.sendErrorMsg(player, preEvent.getCancelMessage());
            return false;
        }
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                if (!TownyEconomyHandler.reserveEconomy.hasAccountDetail(accountName).success() && !TownyEconomyHandler.reserveEconomy.createAccountDetail(accountName).success()) {
                    return false;
                }
                BukkitTools.getPluginManager().callEvent((Event)event);
                return TownyEconomyHandler.reserveEconomy.addHoldingsDetail(accountName, new BigDecimal(amount), world.getName()).success();
            }
            case VAULT: {
                if (!TownyEconomyHandler.vaultEconomy.hasAccount(accountName)) {
                    TownyEconomyHandler.vaultEconomy.createPlayerAccount(accountName);
                }
                Bukkit.getPluginManager().callEvent((Event)event);
                return TownyEconomyHandler.vaultEconomy.depositPlayer(accountName, (double)amount).type == EconomyResponse.ResponseType.SUCCESS;
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean setBalance(final String accountName, final Double amount, final World world) {
        switch (TownyEconomyHandler.Type) {
            case RESERVE: {
                return (TownyEconomyHandler.reserveEconomy.hasAccountDetail(accountName).success() || TownyEconomyHandler.reserveEconomy.createAccountDetail(accountName).success()) && TownyEconomyHandler.reserveEconomy.setHoldingsDetail(accountName, new BigDecimal(amount), world.getName()).success();
            }
            case VAULT: {
                if (!TownyEconomyHandler.vaultEconomy.hasAccount(accountName)) {
                    TownyEconomyHandler.vaultEconomy.createPlayerAccount(accountName);
                }
                return TownyEconomyHandler.vaultEconomy.depositPlayer(accountName, amount - TownyEconomyHandler.vaultEconomy.getBalance(accountName)).type == EconomyResponse.ResponseType.SUCCESS;
            }
            default: {
                return false;
            }
        }
    }
    
    public static String getFormattedBalance(final double balance) {
        try {
            switch (TownyEconomyHandler.Type) {
                case RESERVE: {
                    return TownyEconomyHandler.reserveEconomy.format(new BigDecimal(balance));
                }
                case VAULT: {
                    return TownyEconomyHandler.vaultEconomy.format(balance);
                }
            }
        }
        catch (Exception ex) {}
        return String.format("%.2f", balance);
    }
    
    static {
        TownyEconomyHandler.plugin = null;
        TownyEconomyHandler.vaultEconomy = null;
        TownyEconomyHandler.reserveEconomy = null;
        TownyEconomyHandler.Type = EcoType.NONE;
        TownyEconomyHandler.version = "";
    }
    
    public enum EcoType
    {
        NONE, 
        VAULT, 
        RESERVE;
    }
}
