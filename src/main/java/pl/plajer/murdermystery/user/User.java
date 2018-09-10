/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.murdermystery.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.database.FileStats;
import pl.plajer.murdermystery.murdermysteryapi.MMPlayerStatisticChangeEvent;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class User {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  private static long cooldownCounter = 0;
  private ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
  private Scoreboard scoreboard;
  private UUID uuid;
  private boolean fakeDead = false;
  private boolean spectator = false;
  private Map<String, Integer> ints = new HashMap<>();
  private Map<String, Double> cooldowns = new HashMap<>();

  public User(UUID uuid) {
    scoreboard = scoreboardManager.getNewScoreboard();
    this.uuid = uuid;
  }

  public static void cooldownHandlerTask() {
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
  }

  public Arena getArena() {
    return ArenaRegistry.getArena(Bukkit.getPlayer(uuid));
  }

  public boolean isFakeDead() {
    return fakeDead;
  }

  public void setFakeDead(boolean b) {
    fakeDead = b;
  }

  public Player toPlayer() {
    return Bukkit.getServer().getPlayer(uuid);
  }

  public boolean isSpectator() {
    return spectator;
  }

  public void setSpectator(boolean b) {
    spectator = b;
  }

  public int getInt(String s) {
    if (!ints.containsKey(s)) {
      ints.put(s, 0);
      return 0;
    } else if (ints.get(s) == null) {
      return 0;
    }
    return ints.get(s);
  }

  public void removeScoreboard() {
    this.toPlayer().setScoreboard(scoreboardManager.getNewScoreboard());
  }

  public void setInt(String s, int i) {
    ints.put(s, i);

    //statistics manipulation events are called async when using mysql
    Bukkit.getScheduler().runTask(plugin, () -> {
      MMPlayerStatisticChangeEvent playerStatisticChangeEvent = new MMPlayerStatisticChangeEvent(getArena(), toPlayer(), FileStats.STATISTICS.get(s), i);
      Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
    });
  }

  public void addInt(String s, int i) {
    ints.put(s, getInt(s) + i);

    //statistics manipulation events are called async when using mysql
    Bukkit.getScheduler().runTask(plugin, () -> {
      MMPlayerStatisticChangeEvent playerStatisticChangeEvent = new MMPlayerStatisticChangeEvent(getArena(), toPlayer(), FileStats.STATISTICS.get(s), getInt(s));
      Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
    });
  }

  public void setCooldown(String s, double seconds) {
    cooldowns.put(s, seconds + cooldownCounter);
  }

  public double getCooldown(String s) {
    if (!cooldowns.containsKey(s) || cooldowns.get(s) <= cooldownCounter) {
      return 0;
    }
    return cooldowns.get(s) - cooldownCounter;
  }

}
