package com.palmergames.bukkit.towny.war.eventwar;

import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.util.KeyValueTable;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.util.KeyValue;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.TownyEconomyObject;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.util.TimeTools;
import org.bukkit.plugin.java.JavaPlugin;
import com.palmergames.bukkit.util.ServerBroadCastTimerTask;
import com.palmergames.util.TimeMgmt;
import java.util.Collection;
import java.util.Iterator;
import org.bukkit.scheduler.BukkitScheduler;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import java.util.List;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.Hashtable;

public class War
{
    private static Hashtable<WorldCoord, Integer> warZone;
    private Hashtable<Town, Integer> townScores;
    public static List<Town> warringTowns;
    public static List<Nation> warringNations;
    private WarSpoils warSpoils;
    private Towny plugin;
    private TownyUniverse universe;
    private boolean warTime;
    private List<Integer> warTaskIds;
    
    public War(final Towny plugin, final int startDelay) {
        this.townScores = new Hashtable<Town, Integer>();
        this.warSpoils = new WarSpoils();
        this.warTime = false;
        this.warTaskIds = new ArrayList<Integer>();
        this.plugin = plugin;
        this.universe = plugin.getTownyUniverse();
        this.setupDelay(startDelay);
    }
    
    public void addTaskId(final int id) {
        this.warTaskIds.add(id);
    }
    
    public void clearTaskIds() {
        this.warTaskIds.clear();
    }
    
    public void cancelTasks(final BukkitScheduler scheduler) {
        for (final Integer id : this.getTaskIds()) {
            scheduler.cancelTask((int)id);
        }
        this.clearTaskIds();
    }
    
    public void setPlugin(final Towny plugin) {
        this.plugin = plugin;
    }
    
    public List<Integer> getTaskIds() {
        return new ArrayList<Integer>(this.warTaskIds);
    }
    
    public Towny getPlugin() {
        return this.plugin;
    }
    
    public boolean isWarTime() {
        return this.warTime;
    }
    
    public TownyUniverse getTownyUniverse() {
        return this.universe;
    }
    
    public WarSpoils getWarSpoils() {
        return this.warSpoils;
    }
    
    public Hashtable<Town, Integer> getTownScores() {
        return this.townScores;
    }
    
    public Hashtable<WorldCoord, Integer> getWarZone() {
        return War.warZone;
    }
    
    public List<Town> getWarringTowns() {
        return War.warringTowns;
    }
    
    public static boolean isWarZone(final WorldCoord worldCoord) {
        return War.warZone.containsKey(worldCoord);
    }
    
    public boolean isWarringNation(final Nation nation) {
        return War.warringNations.contains(nation);
    }
    
    public static boolean isWarringTown(final Town town) {
        return War.warringTowns.contains(town);
    }
    
    public void toggleEnd() {
        this.warTime = false;
    }
    
    public void setupDelay(final int delay) {
        if (delay <= 0) {
            this.start();
        }
        else {
            for (final Long t : TimeMgmt.getCountdownDelays(delay, TimeMgmt.defaultCountdownDelays)) {
                final int id = BukkitTools.scheduleAsyncDelayedTask(new ServerBroadCastTimerTask(this.plugin, String.format("War starts in %s", TimeMgmt.formatCountdownTime(t))), TimeTools.convertToTicks(delay - t));
                if (id == -1) {
                    TownyMessaging.sendErrorMsg("Could not schedule a countdown message for war event.");
                    this.end();
                }
                else {
                    this.addTaskId(id);
                }
            }
            final int id2 = BukkitTools.scheduleAsyncDelayedTask(new StartWarTimerTask(this.plugin), TimeTools.convertToTicks(delay));
            if (id2 == -1) {
                TownyMessaging.sendErrorMsg("Could not schedule setup delay for war event.");
                this.end();
            }
            else {
                this.addTaskId(id2);
            }
        }
    }
    
    public void start() {
        this.warTime = true;
        try {
            this.warSpoils.collect(TownySettings.getBaseSpoilsOfWar(), "Start of War - Base Spoils");
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_war_seeding_spoils_with"), TownySettings.getBaseSpoilsOfWar()));
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_war_total_seeding_spoils"), this.warSpoils.getHoldingBalance()));
            TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_war_activate_war_hud_tip"), new Object[0]));
        }
        catch (EconomyException e2) {
            TownyMessaging.sendErrorMsg("[War] Could not seed spoils of war.");
        }
        for (final Nation nation : TownyUniverse.getDataSource().getNations()) {
            if (!nation.isNeutral()) {
                this.add(nation);
                TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_war_join_nation"), nation.getName()));
            }
            else {
                if (TownySettings.isDeclaringNeutral()) {
                    continue;
                }
                try {
                    nation.setNeutral(false);
                    this.add(nation);
                    TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_war_join_forced"), nation.getName()));
                }
                catch (TownyException e) {
                    e.printStackTrace();
                }
            }
        }
        final int id = BukkitTools.scheduleAsyncRepeatingTask(new WarTimerTask(this.plugin, this), 0L, TimeTools.convertToTicks(5L));
        if (id == -1) {
            TownyMessaging.sendErrorMsg("Could not schedule war event loop.");
            this.end();
        }
        else {
            this.addTaskId(id);
        }
        this.checkEnd();
    }
    
    public void end() {
        for (final Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null) {
                this.sendStats(player);
            }
        }
        new BukkitRunnable() {
            public void run() {
                War.this.plugin.getHUDManager().toggleAllWarHUD();
            }
        }.runTask((Plugin)this.plugin);
        try {
            final double halfWinnings = this.getWarSpoils().getHoldingBalance() / 2.0;
            try {
                final double nationWinnings = halfWinnings / War.warringNations.size();
                for (final Nation winningNation : War.warringNations) {
                    this.getWarSpoils().payTo(nationWinnings, winningNation, "War - Nation Winnings");
                    TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeWinningNationSpoilsMsg(winningNation, TownyEconomyHandler.getFormattedBalance(nationWinnings)));
                }
            }
            catch (ArithmeticException e) {
                TownyMessaging.sendDebugMsg("[War]   War ended with 0 nations.");
            }
            try {
                final KeyValue<Town, Integer> winningTownScore = this.getWinningTownScore();
                this.getWarSpoils().payTo(halfWinnings, winningTownScore.key, "War - Town Winnings");
                TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeWinningTownSpoilsMsg(winningTownScore.key, TownyEconomyHandler.getFormattedBalance(halfWinnings), winningTownScore.value));
            }
            catch (TownyException ex) {}
        }
        catch (EconomyException ex2) {}
    }
    
    private void add(final Nation nation) {
        for (final Town town : nation.getTowns()) {
            if (town.getTownBlocks().size() > 0) {
                this.add(town);
            }
        }
        War.warringNations.add(nation);
    }
    
    private void add(final Town town) {
        TownyMessaging.sendTownMessage(town, TownySettings.getJoinWarMsg(town));
        this.townScores.put(town, 0);
        War.warringTowns.add(town);
        for (final TownBlock townBlock : town.getTownBlocks()) {
            if (town.isHomeBlock(townBlock)) {
                War.warZone.put(townBlock.getWorldCoord(), TownySettings.getWarzoneHomeBlockHealth());
            }
            else {
                War.warZone.put(townBlock.getWorldCoord(), TownySettings.getWarzoneTownBlockHealth());
            }
        }
    }
    
    public void townScored(final Town town, final int n, final Object fallenObject, final int townBlocksFallen) {
        String[] pointMessage = { "error" };
        if (fallenObject instanceof Nation) {
            pointMessage = TownySettings.getWarTimeScoreNationEliminatedMsg(town, n, (Nation)fallenObject);
        }
        else if (fallenObject instanceof Town) {
            pointMessage = TownySettings.getWarTimeScoreTownEliminatedMsg(town, n, (Town)fallenObject, townBlocksFallen);
        }
        else if (fallenObject instanceof TownBlock) {
            pointMessage = TownySettings.getWarTimeScoreTownBlockEliminatedMsg(town, n, (TownBlock)fallenObject);
        }
        this.townScores.put(town, this.townScores.get(town) + n);
        TownyMessaging.sendGlobalMessage(pointMessage);
        final TownScoredEvent event = new TownScoredEvent(town, this.townScores.get(town));
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
    }
    
    public void townScored(final Town defenderTown, final Town attackerTown, final Player defenderPlayer, final Player attackerPlayer, final int n) {
        String[] pointMessage = { "error" };
        final TownBlock deathLoc = TownyUniverse.getTownBlock(defenderPlayer.getLocation());
        if (deathLoc == null) {
            pointMessage = TownySettings.getWarTimeScorePlayerKillMsg(attackerPlayer, defenderPlayer, n, attackerTown);
        }
        else if (War.warZone.containsKey(deathLoc.getWorldCoord()) && attackerTown.getTownBlocks().contains(deathLoc)) {
            pointMessage = TownySettings.getWarTimeScorePlayerKillMsg(attackerPlayer, defenderPlayer, attackerPlayer, n, attackerTown);
        }
        else if (War.warZone.containsKey(deathLoc.getWorldCoord()) && defenderTown.getTownBlocks().contains(deathLoc)) {
            pointMessage = TownySettings.getWarTimeScorePlayerKillMsg(attackerPlayer, defenderPlayer, defenderPlayer, n, attackerTown);
        }
        else {
            pointMessage = TownySettings.getWarTimeScorePlayerKillMsg(attackerPlayer, defenderPlayer, n, attackerTown);
        }
        this.townScores.put(attackerTown, this.townScores.get(attackerTown) + n);
        TownyMessaging.sendGlobalMessage(pointMessage);
        final TownScoredEvent event = new TownScoredEvent(attackerTown, this.townScores.get(attackerTown));
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
    }
    
    public void updateWarZone(final TownBlock townBlock, final WarZoneData wzd) throws NotRegisteredException {
        if (!wzd.hasAttackers()) {
            this.healPlot(townBlock, wzd);
        }
        else {
            this.attackPlot(townBlock, wzd);
        }
    }
    
    private void healPlot(final TownBlock townBlock, final WarZoneData wzd) throws NotRegisteredException {
        final WorldCoord worldCoord = townBlock.getWorldCoord();
        final int healthChange = wzd.getHealthChange();
        final int oldHP = War.warZone.get(worldCoord);
        final int hp = this.getHealth(townBlock, healthChange);
        if (oldHP == hp) {
            return;
        }
        War.warZone.put(worldCoord, hp);
        final String healString = "§8[Heal](" + townBlock.getCoord().toString() + ") HP: " + hp + " (" + "§a" + "+" + healthChange + "§8" + ")";
        TownyMessaging.sendMessageToMode(townBlock.getTown(), healString, "");
        for (final Player p : wzd.getDefenders()) {
            if (TownyUniverse.getDataSource().getResident(p.getName()).getTown() != townBlock.getTown()) {
                TownyMessaging.sendMessage(p, healString);
            }
        }
        this.launchFireworkAtPlot(townBlock, wzd.getRandomDefender(), FireworkEffect.Type.BALL, Color.LIME);
        final PlotAttackedEvent event = new PlotAttackedEvent(townBlock, wzd.getAllPlayers(), hp);
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
    }
    
    private void attackPlot(final TownBlock townBlock, final WarZoneData wzd) throws NotRegisteredException {
        final Player attackerPlayer = wzd.getRandomAttacker();
        final Resident attackerResident = TownyUniverse.getDataSource().getResident(attackerPlayer.getName());
        final Town attacker = attackerResident.getTown();
        final WorldCoord worldCoord = townBlock.getWorldCoord();
        final int healthChange = wzd.getHealthChange();
        final int hp = this.getHealth(townBlock, healthChange);
        final Color fwc = (healthChange < 0) ? Color.RED : ((healthChange > 0) ? Color.LIME : Color.GRAY);
        if (hp > 0) {
            War.warZone.put(worldCoord, hp);
            String healthChangeStringDef;
            String healthChangeStringAtk;
            if (healthChange > 0) {
                healthChangeStringDef = "(§a+" + healthChange + "§8" + ")";
                healthChangeStringAtk = "(§4+" + healthChange + "§8" + ")";
            }
            else if (healthChange < 0) {
                healthChangeStringDef = "(§4" + healthChange + "§8" + ")";
                healthChangeStringAtk = "(§a" + healthChange + "§8" + ")";
            }
            else {
                healthChangeStringDef = "(+0)";
                healthChangeStringAtk = "(+0)";
            }
            if (!townBlock.isHomeBlock()) {
                TownyMessaging.sendMessageToMode(townBlock.getTown(), "§8" + TownySettings.getLangString("msg_war_town_under_attack") + " (" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringDef, "");
                if ((hp >= 10 && hp % 10 == 0) || hp <= 5) {
                    this.launchFireworkAtPlot(townBlock, attackerPlayer, FireworkEffect.Type.BALL_LARGE, fwc);
                    for (final Town town : townBlock.getTown().getNation().getTowns()) {
                        if (town != townBlock.getTown()) {
                            TownyMessaging.sendMessageToMode(town, "§8" + TownySettings.getLangString("msg_war_nation_under_attack") + " [" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringDef, "");
                        }
                    }
                    for (final Nation nation : townBlock.getTown().getNation().getAllies()) {
                        if (nation != townBlock.getTown().getNation()) {
                            TownyMessaging.sendMessageToMode(nation, "§8" + String.format(TownySettings.getLangString("msg_war_nations_ally_under_attack"), townBlock.getTown().getName()) + " [" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringDef, "");
                        }
                    }
                }
                else {
                    this.launchFireworkAtPlot(townBlock, attackerPlayer, FireworkEffect.Type.BALL, fwc);
                }
                for (final Town attackingTown : wzd.getAttackerTowns()) {
                    TownyMessaging.sendMessageToMode(attackingTown, "§8[" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringAtk, "");
                }
            }
            else {
                TownyMessaging.sendMessageToMode(townBlock.getTown(), "§8" + TownySettings.getLangString("msg_war_homeblock_under_attack") + " (" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringDef, "");
                if ((hp >= 10 && hp % 10 == 0) || hp <= 5) {
                    this.launchFireworkAtPlot(townBlock, attackerPlayer, FireworkEffect.Type.BALL_LARGE, fwc);
                    for (final Town town : townBlock.getTown().getNation().getTowns()) {
                        if (town != townBlock.getTown()) {
                            TownyMessaging.sendMessageToMode(town, "§8" + String.format(TownySettings.getLangString("msg_war_nation_member_homeblock_under_attack"), townBlock.getTown().getName()) + " [" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringDef, "");
                        }
                    }
                    for (final Nation nation : townBlock.getTown().getNation().getAllies()) {
                        if (nation != townBlock.getTown().getNation()) {
                            TownyMessaging.sendMessageToMode(nation, "§8" + String.format(TownySettings.getLangString("msg_war_nation_ally_homeblock_under_attack"), townBlock.getTown().getName()) + " [" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringDef, "");
                        }
                    }
                }
                else {
                    this.launchFireworkAtPlot(townBlock, attackerPlayer, FireworkEffect.Type.BALL, fwc);
                }
                for (final Town attackingTown : wzd.getAttackerTowns()) {
                    TownyMessaging.sendMessageToMode(attackingTown, "§8[" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp + " " + healthChangeStringAtk, "");
                }
            }
        }
        else {
            this.launchFireworkAtPlot(townBlock, attackerPlayer, FireworkEffect.Type.CREEPER, fwc);
            this.remove(attacker, townBlock);
        }
        final PlotAttackedEvent event = new PlotAttackedEvent(townBlock, wzd.getAllPlayers(), hp);
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
    }
    
    private int getHealth(final TownBlock townBlock, final int healthChange) {
        final WorldCoord worldCoord = townBlock.getWorldCoord();
        final int hp = War.warZone.get(worldCoord) + healthChange;
        final boolean isHomeBlock = townBlock.isHomeBlock();
        if (isHomeBlock && hp > TownySettings.getWarzoneHomeBlockHealth()) {
            return TownySettings.getWarzoneHomeBlockHealth();
        }
        if (!isHomeBlock && hp > TownySettings.getWarzoneTownBlockHealth()) {
            return TownySettings.getWarzoneTownBlockHealth();
        }
        return hp;
    }
    
    private void launchFireworkAtPlot(final TownBlock townblock, final Player atPlayer, final FireworkEffect.Type type, final Color c) {
        if (!TownySettings.getPlotsFireworkOnAttacked()) {
            return;
        }
        BukkitTools.scheduleSyncDelayedTask(new Runnable() {
            @Override
            public void run() {
                final double x = townblock.getX() * (double)Coord.getCellSize() + Coord.getCellSize() / 2.0;
                final double z = townblock.getZ() * (double)Coord.getCellSize() + Coord.getCellSize() / 2.0;
                final double y = atPlayer.getLocation().getY() + 20.0;
                final Firework firework = (Firework)atPlayer.getWorld().spawn(new Location(atPlayer.getWorld(), x, y, z), (Class)Firework.class);
                final FireworkMeta data = firework.getFireworkMeta();
                data.addEffects(new FireworkEffect[] { FireworkEffect.builder().withColor(c).with(type).trail(false).build() });
                firework.setFireworkMeta(data);
                firework.detonate();
            }
        }, 0L);
    }
    
    private void remove(final Town attacker, final TownBlock townBlock) throws NotRegisteredException {
        if (TownySettings.getWarEventCostsTownblocks()) {
            townBlock.getTown().addBonusBlocks(-1);
            attacker.addBonusBlocks(1);
        }
        try {
            if (!townBlock.getTown().payTo(TownySettings.getWartimeTownBlockLossPrice(), attacker, "War - TownBlock Loss")) {
                TownyMessaging.sendTownMessage(townBlock.getTown(), TownySettings.getLangString("msg_war_town_ran_out_of_money"));
                TownyMessaging.sendTitleMessageToTown(townBlock.getTown(), TownySettings.getLangString("msg_war_town_removed_from_war_titlemsg"), "");
                if (townBlock.getTown().isCapital()) {
                    this.remove(attacker, townBlock.getTown().getNation());
                }
                else {
                    this.remove(attacker, townBlock.getTown());
                }
                TownyUniverse.getDataSource().saveTown(townBlock.getTown());
                TownyUniverse.getDataSource().saveTown(attacker);
                return;
            }
            TownyMessaging.sendTownMessage(townBlock.getTown(), String.format(TownySettings.getLangString("msg_war_town_lost_money_townblock"), TownyEconomyHandler.getFormattedBalance(TownySettings.getWartimeTownBlockLossPrice())));
        }
        catch (EconomyException ex) {}
        if (townBlock.getTown().isHomeBlock(townBlock) && townBlock.getTown().isCapital()) {
            this.remove(attacker, townBlock.getTown().getNation());
        }
        else if (townBlock.getTown().isHomeBlock(townBlock)) {
            this.remove(attacker, townBlock.getTown());
        }
        else {
            this.townScored(attacker, TownySettings.getWarPointsForTownBlock(), townBlock, 0);
            this.remove(townBlock.getWorldCoord());
            if (townBlock.getType().equals(TownBlockType.JAIL)) {
                final Town town = townBlock.getTown();
                int count = 0;
                for (final Resident resident : TownyUniverse.getDataSource().getResidents()) {
                    try {
                        if (!resident.isJailed() || !resident.getJailTown().equals(town.toString()) || !Coord.parseCoord(town.getJailSpawn(resident.getJailSpawn())).toString().equals(townBlock.getCoord().toString())) {
                            continue;
                        }
                        resident.setJailed(false);
                        TownyUniverse.getDataSource().saveResident(resident);
                        ++count;
                    }
                    catch (TownyException ex2) {}
                }
                if (count > 0) {
                    TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_war_jailbreak"), town, count));
                }
            }
        }
        TownyUniverse.getDataSource().saveTown(townBlock.getTown());
        TownyUniverse.getDataSource().saveTown(attacker);
    }
    
    public void remove(final Town attacker, final Nation nation) throws NotRegisteredException {
        this.townScored(attacker, TownySettings.getWarPointsForNation(), nation, 0);
        War.warringNations.remove(nation);
        for (final Town town : nation.getTowns()) {
            if (War.warringTowns.contains(town)) {
                this.remove(attacker, town);
            }
        }
        this.checkEnd();
    }
    
    public void remove(final Town attacker, final Town town) throws NotRegisteredException {
        int fallenTownBlocks = 0;
        War.warringTowns.remove(town);
        for (final TownBlock townBlock : town.getTownBlocks()) {
            if (War.warZone.containsKey(townBlock.getWorldCoord())) {
                ++fallenTownBlocks;
                this.remove(townBlock.getWorldCoord());
            }
        }
        this.townScored(attacker, TownySettings.getWarPointsForTown(), town, fallenTownBlocks);
    }
    
    private void remove(final Nation nation) {
        War.warringNations.remove(nation);
        this.sendEliminateMessage(nation.getFormattedName());
        TownyMessaging.sendTitleMessageToNation(nation, TownySettings.getLangString("msg_war_nation_removed_from_war_titlemsg"), "");
        for (final Town town : nation.getTowns()) {
            this.remove(town);
        }
        this.checkEnd();
    }
    
    public void remove(final Town town) {
        try {
            if (town.isCapital() && War.warringNations.contains(town.getNation())) {
                this.remove(town.getNation());
                return;
            }
        }
        catch (NotRegisteredException ex) {}
        int fallenTownBlocks = 0;
        War.warringTowns.remove(town);
        for (final TownBlock townBlock : town.getTownBlocks()) {
            if (War.warZone.containsKey(townBlock.getWorldCoord())) {
                ++fallenTownBlocks;
                this.remove(townBlock.getWorldCoord());
            }
        }
        final StringBuilder sb = new StringBuilder(town.getFormattedName()).append(" (").append(fallenTownBlocks).append(TownySettings.getLangString("msg_war_append_townblocks_fallen"));
        this.sendEliminateMessage(sb.toString());
    }
    
    private void remove(final WorldCoord worldCoord) {
        War.warZone.remove(worldCoord);
    }
    
    private void sendEliminateMessage(final String name) {
        TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeEliminatedMsg(name));
    }
    
    public void nationLeave(final Nation nation) {
        this.remove(nation);
        TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeForfeitMsg(nation.getName()));
        this.checkEnd();
    }
    
    public void townLeave(final Town town) {
        this.remove(town);
        TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeForfeitMsg(town.getName()));
        this.checkEnd();
    }
    
    public boolean townsLeft(final Nation nation) {
        return this.countActiveTowns(nation) > 0;
    }
    
    public void checkEnd() {
        if (War.warringNations.size() <= 1) {
            this.toggleEnd();
        }
        else if (CombatUtil.areAllAllies(War.warringNations)) {
            this.toggleEnd();
        }
    }
    
    public int countActiveWarBlocks(final Town town) {
        int n = 0;
        for (final TownBlock townBlock : town.getTownBlocks()) {
            if (War.warZone.containsKey(townBlock.getWorldCoord())) {
                ++n;
            }
        }
        return n;
    }
    
    public int countActiveTowns(final Nation nation) {
        int n = 0;
        for (final Town town : nation.getTowns()) {
            if (War.warringTowns.contains(town)) {
                ++n;
            }
        }
        return n;
    }
    
    public List<String> getStats() {
        final List<String> output = new ArrayList<String>();
        output.add(ChatTools.formatTitle("War Stats"));
        output.add("§2  Nations: §a" + War.warringNations.size());
        output.add("§2  Towns: §a" + War.warringTowns.size() + " / " + this.townScores.size());
        output.add("§2  WarZone: §a" + War.warZone.size() + " Town blocks");
        try {
            output.add("§2  Spoils of War: §a" + TownyEconomyHandler.getFormattedBalance(this.warSpoils.getHoldingBalance()));
            return output;
        }
        catch (EconomyException ex) {
            return null;
        }
    }
    
    public void sendStats(final Player player) {
        for (final String line : this.getStats()) {
            player.sendMessage(line);
        }
    }
    
    public List<String> getScores(final int maxListing) {
        final List<String> output = new ArrayList<String>();
        output.add(ChatTools.formatTitle("War - Top Scores"));
        final KeyValueTable<Town, Integer> kvTable = new KeyValueTable<Town, Integer>(this.townScores);
        kvTable.sortByValue();
        kvTable.reverse();
        int n = 0;
        for (final KeyValue<Town, Integer> kv : kvTable.getKeyValues()) {
            ++n;
            if (maxListing != -1 && n > maxListing) {
                break;
            }
            final Town town = kv.key;
            final int score = kv.value;
            if (score <= 0) {
                continue;
            }
            output.add(String.format("§3%40s §6|§7 %4d", TownyFormatter.getFormattedName(town), score));
        }
        return output;
    }
    
    public String[] getTopThree() {
        final KeyValueTable<Town, Integer> kvTable = new KeyValueTable<Town, Integer>(this.townScores);
        kvTable.sortByValue();
        kvTable.reverse();
        final String[] top = { (kvTable.getKeyValues().size() >= 1) ? (kvTable.getKeyValues().get(0).value + "-" + kvTable.getKeyValues().get(0).key) : "", (kvTable.getKeyValues().size() >= 2) ? (kvTable.getKeyValues().get(1).value + "-" + kvTable.getKeyValues().get(1).key) : "", (kvTable.getKeyValues().size() >= 3) ? (kvTable.getKeyValues().get(2).value + "-" + kvTable.getKeyValues().get(2).key) : "" };
        return top;
    }
    
    public KeyValue<Town, Integer> getWinningTownScore() throws TownyException {
        final KeyValueTable<Town, Integer> kvTable = new KeyValueTable<Town, Integer>(this.townScores);
        kvTable.sortByValue();
        kvTable.reverse();
        if (kvTable.getKeyValues().size() > 0) {
            return kvTable.getKeyValues().get(0);
        }
        throw new TownyException();
    }
    
    public void sendScores(final Player player) {
        this.sendScores(player, 10);
    }
    
    public void sendScores(final Player player, final int maxListing) {
        for (final String line : this.getScores(maxListing)) {
            player.sendMessage(line);
        }
    }
    
    static {
        War.warZone = new Hashtable<WorldCoord, Integer>();
        War.warringTowns = new ArrayList<Town>();
        War.warringNations = new ArrayList<Nation>();
    }
}
