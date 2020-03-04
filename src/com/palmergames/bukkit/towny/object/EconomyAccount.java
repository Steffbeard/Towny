// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.object;

import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyLogger;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.config.ConfigNodes;
import org.bukkit.World;

public class EconomyAccount extends TownyObject
{
    public static final TownyServerAccount SERVER_ACCOUNT;
    private World world;
    
    protected EconomyAccount(final String name, final World world) {
        super(name);
        this.world = world;
    }
    
    protected EconomyAccount(final String name) {
        super(name);
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public boolean pay(final double amount, final String reason) throws EconomyException {
        if (TownySettings.getBoolean(ConfigNodes.ECO_CLOSED_ECONOMY_ENABLED)) {
            return this.payTo(amount, EconomyAccount.SERVER_ACCOUNT, reason);
        }
        final boolean payed = this._pay(amount);
        if (payed) {
            TownyLogger.getInstance().logMoneyTransaction(this, amount, null, reason);
        }
        return payed;
    }
    
    private boolean _pay(final double amount) throws EconomyException {
        if (!this.canPayFromHoldings(amount) || !TownyEconomyHandler.isActive()) {
            return false;
        }
        if (amount > 0.0) {
            return TownyEconomyHandler.subtract(this.getName(), amount, this.getBukkitWorld());
        }
        return TownyEconomyHandler.add(this.getName(), Math.abs(amount), this.getBukkitWorld());
    }
    
    public boolean collect(final double amount, final String reason) throws EconomyException {
        if (TownySettings.getBoolean(ConfigNodes.ECO_CLOSED_ECONOMY_ENABLED)) {
            return EconomyAccount.SERVER_ACCOUNT.payTo(amount, this, reason);
        }
        final boolean collected = this._collect(amount);
        if (collected) {
            TownyLogger.getInstance().logMoneyTransaction(null, amount, this, reason);
        }
        return collected;
    }
    
    private boolean _collect(final double amount) throws EconomyException {
        return TownyEconomyHandler.add(this.getName(), amount, this.getBukkitWorld());
    }
    
    public boolean payTo(final double amount, final EconomyHandler collector, final String reason) throws EconomyException {
        return this.payTo(amount, collector.getAccount(), reason);
    }
    
    public boolean payTo(final double amount, final EconomyAccount collector, final String reason) throws EconomyException {
        final boolean payed = this._payTo(amount, collector);
        if (payed) {
            TownyLogger.getInstance().logMoneyTransaction(this, amount, collector, reason);
        }
        return payed;
    }
    
    private boolean _payTo(final double amount, final EconomyAccount collector) throws EconomyException {
        if (!this._pay(amount)) {
            return false;
        }
        if (!collector._collect(amount)) {
            this._collect(amount);
            return false;
        }
        return true;
    }
    
    protected World getBukkitWorld() {
        return BukkitTools.getWorlds().get(0);
    }
    
    public boolean setBalance(final double amount, final String reason) throws EconomyException {
        final double balance = this.getHoldingBalance();
        double diff = amount - balance;
        if (diff > 0.0) {
            return this.collect(diff, reason);
        }
        if (balance > amount) {
            diff = -diff;
            return this.pay(diff, reason);
        }
        return true;
    }
    
    public double getHoldingBalance() throws EconomyException {
        try {
            return TownyEconomyHandler.getBalance(this.getName(), this.getBukkitWorld());
        }
        catch (NoClassDefFoundError e) {
            e.printStackTrace();
            throw new EconomyException("Economy error getting holdings for " + this.getName());
        }
    }
    
    public boolean canPayFromHoldings(final double amount) throws EconomyException {
        return TownyEconomyHandler.hasEnough(this.getName(), amount, this.getBukkitWorld());
    }
    
    public String getHoldingFormattedBalance() {
        try {
            return TownyEconomyHandler.getFormattedBalance(this.getHoldingBalance());
        }
        catch (EconomyException e) {
            return "Error Accessing Bank Account";
        }
    }
    
    public void removeAccount() {
        TownyEconomyHandler.removeAccount(this.getName());
    }
    
    static {
        SERVER_ACCOUNT = new TownyServerAccount();
    }
    
    private static final class TownyServerAccount extends EconomyAccount
    {
        TownyServerAccount() {
            super(TownySettings.getString(ConfigNodes.ECO_CLOSED_ECONOMY_SERVER_ACCOUNT));
        }
    }
}
