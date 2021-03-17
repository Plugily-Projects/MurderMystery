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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.api.events.player.MMPlayerStatisticChangeEvent;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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
  private final Player player;
  private boolean spectator = false;
  private boolean permanentSpectator = false;

  public User(Player player) {
    this.player = player;
  }

  public static void cooldownHandlerTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
  }

  public Arena getArena() {
    return ArenaRegistry.getArena(player);
  }

  public Player getPlayer() {
    return player;
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
    if(!stats.containsKey(stat)) {
      stats.put(stat, 0);
      return 0;
    }

    return stats.getOrDefault(stat, 0);
  }

  public void removeScoreboard(Arena arena) {
    arena.getScoreboardManager().removeScoreboard(this);
    //player.setScoreboard(scoreboardManager.getNewScoreboard());
  }

  public void setStat(StatsStorage.StatisticType stat, int i) {
    stats.put(stat, i);

    //statistics manipulation events are called async when using mysql
    Bukkit.getScheduler().runTask(plugin, () -> {
      MMPlayerStatisticChangeEvent playerStatisticChangeEvent = new MMPlayerStatisticChangeEvent(getArena(), player, stat, i);
      Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
    });
  }

  public void addStat(StatsStorage.StatisticType stat, int i) {
    stats.put(stat, getStat(stat) + i);

    //statistics manipulation events are called async when using mysql
    Bukkit.getScheduler().runTask(plugin, () -> {
      MMPlayerStatisticChangeEvent playerStatisticChangeEvent = new MMPlayerStatisticChangeEvent(getArena(), player, stat, getStat(stat));
      Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
    });
  }

  public void setCooldown(String s, double seconds) {
    cooldowns.put(s, seconds + cooldownCounter);
  }

  public double getCooldown(String s) {
    return (!cooldowns.containsKey(s) || cooldowns.get(s) <= cooldownCounter) ? 0 : cooldowns.get(s) - cooldownCounter;
  }

}
