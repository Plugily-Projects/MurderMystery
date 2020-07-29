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

package pl.plajer.murdermystery.commands.arguments.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.commands.arguments.ArgumentsRegistry;
import pl.plajer.murdermystery.commands.arguments.data.CommandArgument;
import pl.plajer.murdermystery.commands.arguments.data.LabelData;
import pl.plajer.murdermystery.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.setup.SetupInventory;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

/**
 * @author Plajer
 * <p>
 * Created at 18.05.2019
 */
public class CreateArgument {

  private ArgumentsRegistry registry;

  public CreateArgument(ArgumentsRegistry registry) {
    this.registry = registry;
    registry.mapArgument("murdermystery", new LabeledCommandArgument("create", "murdermystery.admin.create", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/mm create &6<arena>", "/mm create <arena>", "&7Create new arena\n&6Permission: &7murdermystery.admin.create")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(ChatManager.colorMessage("Commands.Type-Arena-Name"));
          return;
        }
        Player player = (Player) sender;
        for (Arena arena : ArenaRegistry.getArenas()) {
          if (arena.getId().equalsIgnoreCase(args[1])) {
            player.sendMessage(ChatColor.DARK_RED + "Arena with that ID already exists!");
            player.sendMessage(ChatColor.DARK_RED + "Usage: /mm create <ID>");
            return;
          }
        }
        if (ConfigUtils.getConfig(registry.getPlugin(), "arenas").contains("instances." + args[1])) {
          player.sendMessage(ChatColor.DARK_RED + "Instance/Arena already exists! Use another ID or delete it first!");
        } else {
          createInstanceInConfig(args[1], player.getWorld().getName());
          player.sendMessage(ChatColor.BOLD + "------------------------------------------");
          player.sendMessage(ChatColor.YELLOW + "      Instance " + args[1] + " created!");
          player.sendMessage("");
          player.sendMessage(ChatColor.GREEN + "Edit this arena via " + ChatColor.GOLD + "/mm " + args[1] + " edit" + ChatColor.GREEN + "!");
          player.sendMessage(ChatColor.GOLD + "Don't know where to start? Check out tutorial video:");
          player.sendMessage(ChatColor.GOLD + SetupInventory.VIDEO_LINK);
          player.sendMessage(ChatColor.BOLD + "------------------------------------------- ");
        }
      }
    });
  }

  private void createInstanceInConfig(String id, String worldName) {
    String path = "instances." + id + ".";
    FileConfiguration config = ConfigUtils.getConfig(registry.getPlugin(), "arenas");
    LocationSerializer.saveLoc(registry.getPlugin(), config, "arenas", path + "lobbylocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    LocationSerializer.saveLoc(registry.getPlugin(), config, "arenas", path + "Startlocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    LocationSerializer.saveLoc(registry.getPlugin(), config, "arenas", path + "Endlocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    config.set(path + "playerspawnpoints", new ArrayList<>());
    config.set(path + "goldspawnpoints", new ArrayList<>());
    config.set(path + "minimumplayers", 2);
    config.set(path + "maximumplayers", 10);
    config.set(path + "playerpermurderer", 5);
    config.set(path + "playerperdetective", 7);
    config.set(path + "mapname", id);
    config.set(path + "signs", new ArrayList<>());
    config.set(path + "isdone", false);
    config.set(path + "world", worldName);
    config.set(path + "mystery-cauldrons", new ArrayList<>());
    config.set(path + "confessionals", new ArrayList<>());
    ConfigUtils.saveConfig(registry.getPlugin(), config, "arenas");

    Arena arena = new Arena(id);

    List<Location> playerSpawnPoints = new ArrayList<>();
    for (String loc : config.getStringList(path + "playerspawnpoints")) {
      playerSpawnPoints.add(LocationSerializer.getLocation(loc));
    }
    arena.setPlayerSpawnPoints(playerSpawnPoints);
    List<Location> goldSpawnPoints = new ArrayList<>();
    for (String loc : config.getStringList(path + "goldspawnpoints")) {
      goldSpawnPoints.add(LocationSerializer.getLocation(loc));
    }
    arena.setGoldSpawnPoints(goldSpawnPoints);

    List<SpecialBlock> specialBlocks = new ArrayList<>();
    if (config.isSet("instances." + arena.getId() + ".mystery-cauldrons")) {
      for (String loc : config.getStringList("instances." + arena.getId() + ".mystery-cauldrons")) {
        specialBlocks.add(new SpecialBlock(LocationSerializer.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
      }
    }
    for (SpecialBlock block : specialBlocks) {
      arena.loadSpecialBlock(block);
    }
    arena.setMinimumPlayers(config.getInt(path + "minimumplayers"));
    arena.setMaximumPlayers(config.getInt(path + "maximumplayers"));
    arena.setDetectives(config.getInt(path + "playerperdetective"));
    arena.setMurderers(config.getInt(path + "playerpermurderer"));
    arena.setMapName(config.getString(path + "mapname"));
    arena.setLobbyLocation(LocationSerializer.getLocation(config.getString(path + "lobbylocation")));
    arena.setEndLocation(LocationSerializer.getLocation(config.getString(path + "Endlocation")));
    arena.setReady(false);

    ArenaRegistry.registerArena(arena);
  }

}
