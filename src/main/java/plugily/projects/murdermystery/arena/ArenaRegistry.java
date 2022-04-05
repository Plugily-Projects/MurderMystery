/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2022  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.PluginArenaRegistry;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.dimensional.Cuboid;
import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.special.SpecialBlock;
import plugily.projects.thebridge.Main;
import plugily.projects.thebridge.arena.base.Base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 27/07/2014.
 */
public class ArenaRegistry extends PluginArenaRegistry {

  private final Main plugin;

  public ArenaRegistry(Main plugin) {
    super(plugin);
    this.plugin = plugin;
  }


  @Override
  public PluginArena getNewArena(String id) {
    return new Arena(id);
  }

  @Override
  public boolean additionalValidatorChecks(ConfigurationSection section, PluginArena arena, String id) {
    boolean checks = super.additionalValidatorChecks(section, arena, id);
    if(!checks) return false;

    if(!section.getBoolean(id + ".isdone")) {
      plugin.getDebugger().sendConsoleMsg(new MessageBuilder("VALIDATOR_INVALID_ARENA_CONFIGURATION").asKey().value("NOT VALIDATED").arena(arena).build());
      return false;
    }

    List<Location> playerSpawnPoints = new ArrayList<>();
    for(String loc : section.getStringList(id + ".playerspawnpoints")) {
      org.bukkit.Location serialized = LocationSerializer.getLocation(loc);

      // Ignore the arena if world is not exist at least in spawn points
      if(serialized == null || serialized.getWorld() == null) {
        section.set(id + ".isdone", false);
      } else {
        playerSpawnPoints.add(serialized);
      }
    }

    ((Arena) arena).setSpawnGoldTime(section.getInt(id + ".spawngoldtime", 5));
    ((Arena) arena).setHideChances(section.getBoolean(id + ".hidechances"));
    arena.setArenaOption("MURDERER_DIVIDER",section.getInt(id + ".playerpermurderer", 5));
    arena.setArenaOption("DETECTIVE_DIVIDER",section.getInt(id + ".playerperdetective", 7));
    ((Arena) arena).setGoldVisuals(section.getBoolean(id + ".goldvisuals"));
    ((Arena) arena).setPlayerSpawnPoints(playerSpawnPoints);

    List<Location> goldSpawnPoints = new ArrayList<>();
    for(String loc : section.getStringList(id + ".goldspawnpoints")) {
      goldSpawnPoints.add(LocationSerializer.getLocation(loc));
    }
    ((Arena) arena).setGoldSpawnPoints(goldSpawnPoints);

    List<SpecialBlock> specialBlocks = new ArrayList<>();
    for(String loc : section.getStringList(id + ".mystery-cauldrons")) {
      specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
    }
    for(String loc : section.getStringList(id + ".confessionals")) {
      specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.PRAISE_DEVELOPER));
    }

    specialBlocks.forEach(((Arena) arena)::loadSpecialBlock);
    return true;
  }

  @Override
  public @Nullable Arena getArena(Player player) {
    PluginArena pluginArena = super.getArena(player);
    if(pluginArena instanceof Arena) {
      return (Arena) pluginArena;
    }
    return null;
  }

  @Override
  public @Nullable Arena getArena(String id) {
    PluginArena pluginArena = super.getArena(id);
    if(pluginArena instanceof Arena) {
      return (Arena) pluginArena;
    }
    return null;
  }

  public @NotNull List<Arena> getPluginArenas() {
    List<Arena> arenas = new ArrayList<>();
    for(PluginArena pluginArena : super.getArenas()) {
      if(pluginArena instanceof Arena) {
        arenas.add((Arena) pluginArena);
      }
    }
    return arenas;
  }
}
