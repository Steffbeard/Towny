// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.object.EconomyAccount;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.appender.FileAppender;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.Logger;

public class TownyLogger
{
    private static final TownyLogger instance;
    private static final Logger LOGGER_MONEY;
    
    private TownyLogger() {
        final LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final String logFolderName = TownyUniverse.getInstance().getRootFolder() + File.separator + "logs";
        final Appender townyMainAppender = (Appender)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)FileAppender.newBuilder().withFileName(logFolderName + File.separator + "towny.log").withName("Towny-Main-Log")).withAppend(TownySettings.isAppendingToLog()).withIgnoreExceptions(false)).withBufferedIo(false)).withBufferSize(0)).setConfiguration(config)).withLayout((Layout)PatternLayout.newBuilder().withCharset(StandardCharsets.UTF_8).withPattern("%d [%t]: %m%n").withConfiguration(config).build())).build();
        final Appender townyMoneyAppender = (Appender)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)FileAppender.newBuilder().withFileName(logFolderName + File.separator + "money.csv").withName("Towny-Money")).withAppend(TownySettings.isAppendingToLog()).withIgnoreExceptions(false)).withBufferedIo(false)).withBufferSize(0)).setConfiguration(config)).withLayout((Layout)PatternLayout.newBuilder().withCharset(StandardCharsets.UTF_8).withPattern("%d{dd MMM yyyy HH:mm:ss},%m%n").withConfiguration(config).build())).build();
        final Appender townyDebugAppender = (Appender)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)FileAppender.newBuilder().withFileName(logFolderName + File.separator + "debug.log").withName("Towny-Debug")).withAppend(TownySettings.isAppendingToLog()).withIgnoreExceptions(false)).withBufferedIo(false)).withBufferSize(0)).setConfiguration(config)).withLayout((Layout)PatternLayout.newBuilder().withCharset(StandardCharsets.UTF_8).withPattern("%d [%t]: %m%n").withConfiguration(config).build())).build();
        final Appender townyDatabaseAppender = (Appender)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)((FileAppender.Builder)FileAppender.newBuilder().withFileName(logFolderName + File.separator + "database.log").withName("Towny-Database")).withAppend(TownySettings.isAppendingToLog()).withIgnoreExceptions(false)).withBufferedIo(false)).withBufferSize(0)).setConfiguration(config)).withLayout((Layout)PatternLayout.newBuilder().withCharset(StandardCharsets.UTF_8).withPattern("%d [%t]: %m%n").withConfiguration(config).build())).build();
        townyMainAppender.start();
        townyMoneyAppender.start();
        townyDebugAppender.start();
        townyDatabaseAppender.start();
        final LoggerConfig townyMainConfig = LoggerConfig.createLogger(true, Level.ALL, "Towny", (String)null, new AppenderRef[0], (Property[])null, config, (Filter)null);
        townyMainConfig.addAppender(townyMainAppender, Level.ALL, (Filter)null);
        config.addLogger(Towny.class.getName(), townyMainConfig);
        final LoggerConfig townyDebugConfig = LoggerConfig.createLogger(TownySettings.getDebug(), Level.ALL, "Towny-Debug", (String)null, new AppenderRef[0], (Property[])null, config, (Filter)null);
        townyDebugConfig.addAppender(townyDebugAppender, Level.ALL, (Filter)null);
        config.addLogger("com.palmergames.bukkit.towny.debug", townyDebugConfig);
        final LoggerConfig townyMoneyConfig = LoggerConfig.createLogger(false, Level.ALL, "Towny-Money", (String)null, new AppenderRef[0], (Property[])null, config, (Filter)null);
        townyMoneyConfig.addAppender(townyMoneyAppender, Level.ALL, (Filter)null);
        config.addLogger("com.palmergames.bukkit.towny.money", townyMoneyConfig);
        final LoggerConfig townyDatabaseConfig = LoggerConfig.createLogger(false, Level.ALL, "Towny-Database", (String)null, new AppenderRef[0], (Property[])null, config, (Filter)null);
        townyDatabaseConfig.addAppender(townyDatabaseAppender, Level.ALL, (Filter)null);
        ctx.updateLoggers();
    }
    
    public void refreshDebugLogger() {
        final LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig townyDebugConfig = config.getLoggerConfig("com.palmergames.bukkit.towny.debug");
        townyDebugConfig.setAdditive(TownySettings.getDebug());
        ctx.updateLoggers();
    }
    
    public void logMoneyTransaction(final EconomyAccount a, final double amount, final EconomyAccount b, final String reason) {
        String sender;
        if (a == null) {
            sender = "None";
        }
        else {
            sender = a.getName();
        }
        String receiver;
        if (b == null) {
            receiver = "None";
        }
        else {
            receiver = b.getName();
        }
        if (reason == null) {
            TownyLogger.LOGGER_MONEY.info(String.format("%s,%s,%s,%s", "Unknown Reason", sender, amount, receiver));
        }
        else {
            TownyLogger.LOGGER_MONEY.info(String.format("%s,%s,%s,%s", reason, sender, amount, receiver));
        }
    }
    
    public void logMoneyTransaction(final String a, final double amount, final String b, final String reason) {
        if (reason == null) {
            TownyLogger.LOGGER_MONEY.info(String.format("%s,%s,%s,%s", "Unknown Reason", a, amount, b));
        }
        else {
            TownyLogger.LOGGER_MONEY.info(String.format("%s,%s,%s,%s", reason, a, amount, b));
        }
    }
    
    public static TownyLogger getInstance() {
        return TownyLogger.instance;
    }
    
    static {
        instance = new TownyLogger();
        LOGGER_MONEY = LogManager.getLogger("com.palmergames.bukkit.towny.money");
    }
}
