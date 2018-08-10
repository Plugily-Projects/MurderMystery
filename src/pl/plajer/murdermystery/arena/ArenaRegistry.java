/*
 * Village Defense 3 - Protect villagers from hordes of zombies
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
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

package pl.plajer.murdermystery.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajerlair.core.services.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.MinigameUtils;

/**
 * Created by Tom on 27/07/2014.
 */
public class ArenaRegistry {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  private static List<Arena> arenas = new ArrayList<>();

  /**
   * Checks if player is in any arena
   *
   * @param player player to check
   * @return [b]true[/b] when player is in arena, [b]false[/b] if otherwise
   */
  public static boolean isInArena(Player player) {
    for (Arena arena : arenas) {
      if (arena.getPlayers().contains(player)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns arena where the player is
   *
   * @param p target player
   * @return Arena or null if not playing
   * @see #isInArena(Player) to check if player is playing
   */
  public static Arena getArena(Player p) {
    Arena arena = null;
    if (p == null || !p.isOnline()) {
      return null;
    }
    for (Arena loopArena : arenas) {
      for (Player player : loopArena.getPlayers()) {
        if (player.getUniqueId() == p.getUniqueId()) {
          arena = loopArena;
          break;
        }
      }
    }
    return arena;
  }

  public static void registerArena(Arena arena) {
    Main.debug("Registering new game instance, " + arena.getID(), System.currentTimeMillis());
    arenas.add(arena);
  }

  public static void unregisterArena(Arena arena) {
    Main.debug("Unegistering game instance, " + arena.getID(), System.currentTimeMillis());
    arenas.remove(arena);
  }

  /**
   * Returns arena based by ID
   *
   * @param ID name of arena
   * @return Arena or null if not found
   */
  public static Arena getArena(String ID) {
    Arena arena = null;
    for (Arena loopArena : arenas) {
      if (loopArena.getID().equalsIgnoreCase(ID)) {
        arena = loopArena;
        break;
      }
    }
    return arena;
  }

  public static void registerArenas() {
    try {
      Main.debug("Initial arenas registration", System.currentTimeMillis());
      FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
      if (ArenaRegistry.getArenas() != null) {
        if (ArenaRegistry.getArenas().size() > 0) {
          for (Arena arena : ArenaRegistry.getArenas()) {
            arena.cleanUpArena();
          }
        }
      }
      ArenaRegistry.getArenas().clear();
      if (!config.contains("instances")) {
        Bukkit.getConsoleSender().sendMessage(ChatManager.colorMessage("Validator.No-Instances-Created"));
        return;
      }

      for (String ID : config.getConfigurationSection("instances").getKeys(false)) {
        Arena arena;
        String s = "instances." + ID + ".";
        if (s.contains("default")) {
          continue;
        }
        arena = new Arena(ID, plugin);
        arena.setMinimumPlayers(config.getInt(s + "minimumplayers"));
        arena.setMaximumPlayers(config.getInt(s + "maximumplayers"));
        arena.setMapName(config.getString(s + "mapname"));
        List<Location> playerSpawnPoints = new ArrayList<>();
        for (String loc : config.getStringList(s + "playerspawnpoints")) {
          playerSpawnPoints.add(MinigameUtils.getLocation(loc));
        }
        arena.setPlayerSpawnPoints(playerSpawnPoints);
        List<Location> goldSpawnPoints = new ArrayList<>();
        for (String loc : config.getStringList(s + "goldspawnpoints")) {
          goldSpawnPoints.add(MinigameUtils.getLocation(loc));
        }
        arena.setGoldSpawnPoints(goldSpawnPoints);
        arena.setLobbyLocation(MinigameUtils.getLocation(config.getString(s + "lobbylocation")));
        arena.setEndLocation(MinigameUtils.getLocation(config.getString(s + "Endlocation")));

        if (!config.getBoolean(s + "isdone", false)) {
          Bukkit.getConsoleSender().sendMessage(ChatManager.colorMessage("Validator.Invalid-Arena-Configuration").replace("%arena%", ID).replace("%error%", "NOT VALIDATED"));
          arena.setReady(false);
          ArenaRegistry.registerArena(arena);
          continue;
        }
        ArenaRegistry.registerArena(arena);
        arena.start();
        Bukkit.getConsoleSender().sendMessage(ChatManager.colorMessage("Validator.Instance-Started").replace("%arena%", ID));
      }
      Main.debug("Arenas registration completed", System.currentTimeMillis());
    } catch (Exception ex){
      new ReportedException(plugin, ex);
    }
  }

  public static List<Arena> getArenas() {
    return arenas;
  }
}
