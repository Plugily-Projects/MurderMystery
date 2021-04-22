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

package plugily.projects.murdermystery.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Tom on 27/07/2014.
 */
public class ArenaRegistry {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private static final List<Arena> arenas = new ArrayList<>();
  private static int bungeeArena = -999;

  /**
   * Checks if player is in any arena
   *
   * @param player player to check
   * @return [b]true[/b] when player is in arena, [b]false[/b] if otherwise
   */
  public static boolean isInArena(Player player) {
    return getArena(player) != null;
  }

  /**
   * Returns arena where the player is
   *
   * @param p target player
   * @return Arena or null if not playing
   * @see #isInArena(Player) to check if player is playing
   */
  public static Arena getArena(Player p) {
    if(p == null || !p.isOnline()) {
      return null;
    }
    for(Arena arena : arenas) {
      for(Player player : arena.getPlayers()) {
        if(player.getUniqueId().equals(p.getUniqueId())) {
          return arena;
        }
      }
    }
    return null;
  }

  /**
   * Returns arena based by ID
   *
   * @param id name of arena
   * @return Arena or null if not found
   */
  public static Arena getArena(String id) {
    for(Arena loopArena : arenas) {
      if(loopArena.getId().equalsIgnoreCase(id)) {
        return loopArena;
      }
    }

    return null;
  }

  public static void registerArena(Arena arena) {
    Debugger.debug("Registering new game instance {0}", arena.getId());
    arenas.add(arena);
  }

  public static void unregisterArena(Arena arena) {
    Debugger.debug("Unegistering game instance {0}", arena.getId());
    arenas.remove(arena);
  }

  public static void registerArenas() {
    Debugger.debug("Initial arenas registration");
    long start = System.currentTimeMillis();

    if(!arenas.isEmpty()) {
      for (Arena arena : new ArrayList<>(arenas)) {
        arena.cleanUpArena();
        unregisterArena(arena);
      }
    }

    org.bukkit.configuration.ConfigurationSection section = ConfigUtils.getConfig(plugin, "arenas").getConfigurationSection("instances");
    if(section == null) {
      Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage("Validator.No-Instances-Created"));
      return;
    }

    for(String id : section.getKeys(false)) {
      if(id.equalsIgnoreCase("default")) {
        continue;
      }

      List<Location> playerSpawnPoints = new ArrayList<>();
      for(String loc : section.getStringList(id + ".playerspawnpoints")) {
        org.bukkit.Location serialized = LocationSerializer.getLocation(loc);

        // Remove the arena entirely if world is not exist at least in spawn points
        if (serialized == null || serialized.getWorld() == null) {
          section.set(id, null);
          return;
        }

        playerSpawnPoints.add(serialized);
      }

      Arena arena = new Arena(id);

      arena.setPlayerSpawnPoints(playerSpawnPoints);
      arena.setMinimumPlayers(section.getInt(id + ".minimumplayers", 2));
      arena.setMaximumPlayers(section.getInt(id + ".maximumplayers", 4));
      arena.setMapName(section.getString(id + ".mapname", "none"));
      arena.setSpawnGoldTime(section.getInt(id + ".spawngoldtime", 5));
      arena.setHideChances(section.getBoolean(id + ".hidechances"));
      arena.setMurderers(section.getInt(id + ".playerpermurderer", 5));
      arena.setDetectives(section.getInt(id + ".playerperdetective", 7));

      List<Location> goldSpawnPoints = new ArrayList<>();
      for(String loc : section.getStringList(id + ".goldspawnpoints")) {
        goldSpawnPoints.add(LocationSerializer.getLocation(loc));
      }
      arena.setGoldSpawnPoints(goldSpawnPoints);

      List<SpecialBlock> specialBlocks = new ArrayList<>();
      for(String loc : section.getStringList(id + ".mystery-cauldrons")) {
        specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
      }
      for(String loc : section.getStringList(id + ".confessionals")) {
        specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
      }

      specialBlocks.forEach(arena::loadSpecialBlock);

      arena.setLobbyLocation(LocationSerializer.getLocation(section.getString(id + ".lobbylocation", "world,364.0,63.0,-72.0,0.0,0.0")));
      arena.setEndLocation(LocationSerializer.getLocation(section.getString(id + ".Endlocation", "world,364.0,63.0,-72.0,0.0,0.0")));
      arena.setGoldVisuals(section.getBoolean(id + ".goldvisuals"));

      if(!section.getBoolean(id + ".isdone")) {
        Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage("Validator.Invalid-Arena-Configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
        arena.setReady(false);
        registerArena(arena);
        continue;
      }
      registerArena(arena);
      arena.start();
      Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage("Validator.Instance-Started").replace("%arena%", id));
    }
    Debugger.debug("Arenas registration completed, took {0}ms", System.currentTimeMillis() - start);
  }

  public static List<Arena> getArenas() {
    return arenas;
  }

  public static void shuffleBungeeArena() {
    bungeeArena = new Random().nextInt(arenas.size());
  }

  public static int getBungeeArena() {
    if(bungeeArena == -999) {
      bungeeArena = new Random().nextInt(arenas.size());
    }
    return bungeeArena;
  }
}
