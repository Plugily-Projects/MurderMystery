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
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.LocationUtils;

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
    Debugger.debug(LogLevel.INFO, "Registering new game instance, " + arena.getID());
    arenas.add(arena);
  }

  public static void unregisterArena(Arena arena) {
    Debugger.debug(LogLevel.INFO, "Unegistering game instance, " + arena.getID());
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
      Debugger.debug(LogLevel.INFO, "Initial arenas registration");
      if (ArenaRegistry.getArenas() != null) {
        if (ArenaRegistry.getArenas().size() > 0) {
          for (Arena arena : ArenaRegistry.getArenas()) {
            arena.cleanUpArena();
          }
          for (Arena arena : new ArrayList<>(ArenaRegistry.getArenas())) {
            unregisterArena(arena);
          }
        }
      }
      FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

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
        arena.setMinimumPlayers(config.getInt(s + "minimumplayers", 2));
        arena.setMaximumPlayers(config.getInt(s + "maximumplayers", 4));
        arena.setMapName(config.getString(s + "mapname", "none"));
        List<Location> playerSpawnPoints = new ArrayList<>();
        for (String loc : config.getStringList(s + "playerspawnpoints")) {
          playerSpawnPoints.add(LocationUtils.getLocation(loc));
        }
        arena.setPlayerSpawnPoints(playerSpawnPoints);
        List<Location> goldSpawnPoints = new ArrayList<>();
        for (String loc : config.getStringList(s + "goldspawnpoints")) {
          goldSpawnPoints.add(LocationUtils.getLocation(loc));
        }
        arena.setGoldSpawnPoints(goldSpawnPoints);

        List<SpecialBlock> specialBlocks = new ArrayList<>();
        if (config.isSet("instances." + arena.getID() + ".mystery-cauldrons")) {
          for (String loc : config.getStringList("instances." + arena.getID() + ".mystery-cauldrons")) {
            specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
          }
        }
        if (config.isSet("instances." + arena.getID() + ".confessionals")) {
          for (String loc : config.getStringList("instances." + arena.getID() + ".confessionals")) {
            specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
          }
        }
        for (SpecialBlock block : specialBlocks) {
          arena.loadSpecialBlock(block);
        }
        arena.setLobbyLocation(LocationUtils.getLocation(config.getString(s + "lobbylocation", "world,364.0,63.0,-72.0,0.0,0.0")));
        arena.setEndLocation(LocationUtils.getLocation(config.getString(s + "Endlocation", "world,364.0,63.0,-72.0,0.0,0.0")));

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
      Debugger.debug(LogLevel.INFO, "Arenas registration completed");
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  public static List<Arena> getArenas() {
    return arenas;
  }
}
