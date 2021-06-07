/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.murdermystery.user;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.api.events.player.MMPlayerStatisticChangeEvent;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class User {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private static long cooldownCounter = 0;
  private final Map<StatsStorage.StatisticType, Integer> stats = new EnumMap<>(StatsStorage.StatisticType.class);
  private final Map<String, Double> cooldowns = new HashMap<>();
  private final UUID uuid;
  private boolean spectator = false;
  private boolean permanentSpectator = false;

  public Scoreboard lastBoard;

  @Deprecated
  public User(Player player) {
    this(player.getUniqueId());
  }

  public User(UUID uuid) {
    this.uuid = uuid;
  }

  public UUID getUniqueId() {
    return uuid;
  }

  public static void cooldownHandlerTask() {
    plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
  }

  public Arena getArena() {
    return ArenaRegistry.getArena(getPlayer());
  }

  public Player getPlayer() {
    return plugin.getServer().getPlayer(uuid);
  }

  public boolean isSpectator() {
    return spectator;
  }

  public void setSpectator(boolean b) {
    spectator = b;
  }

  public boolean isPermanentSpectator() {
    return permanentSpectator;
  }

  public void setPermanentSpectator(boolean permanentSpectator) {
    this.permanentSpectator = permanentSpectator;
  }

  public int getStat(StatsStorage.StatisticType stat) {
    Integer st = stats.get(stat);
    if(st == null) {
      stats.put(stat, 0);
      return 0;
    }

    return st.intValue();
  }

  public void removeScoreboard(Arena arena) {
    arena.getScoreboardManager().removeScoreboard(this);

    if (lastBoard != null) {
      getPlayer().setScoreboard(lastBoard);
      lastBoard = null;
    }
  }

  public void setStat(StatsStorage.StatisticType stat, int i) {
    stats.put(stat, i);

    //statistics manipulation events are called async when using mysql
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      Player player = getPlayer();
      plugin.getServer().getPluginManager().callEvent(new MMPlayerStatisticChangeEvent(
          ArenaRegistry.getArena(player), player, stat, i));
    });
  }

  public void addStat(StatsStorage.StatisticType stat, int i) {
    stats.put(stat, getStat(stat) + i);

    //statistics manipulation events are called async when using mysql
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      Player player = getPlayer();
      plugin.getServer().getPluginManager().callEvent(new MMPlayerStatisticChangeEvent(
          ArenaRegistry.getArena(player), player, stat, getStat(stat)));
    });
  }

  public void setCooldown(String s, double seconds) {
    cooldowns.put(s, seconds + cooldownCounter);
  }

  public double getCooldown(String s) {
    Double coold = cooldowns.get(s);
    return (coold == null || coold.doubleValue() <= cooldownCounter) ? 0 : coold.doubleValue() - cooldownCounter;
  }

}
