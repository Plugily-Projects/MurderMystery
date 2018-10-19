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

package pl.plajer.murdermystery.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.special.SpecialBlock;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.setup.SetupInventory;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.LocationUtils;
import pl.plajerlair.core.utils.StringMatcher;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class MainCommand implements CommandExecutor {

  private Main plugin;
  private AdminCommands adminCommands;
  private GameCommands gameCommands;

  public MainCommand(Main plugin, boolean register) {
    this.plugin = plugin;
    if (register) {
      adminCommands = new AdminCommands(plugin);
      gameCommands = new GameCommands(plugin);
      TabCompletion completion = new TabCompletion(plugin);
      plugin.getCommand("murdermystery").setExecutor(this);
      plugin.getCommand("murdermystery").setTabCompleter(completion);
      plugin.getCommand("murdermysteryadmin").setExecutor(this);
      plugin.getCommand("murdermysteryadmin").setTabCompleter(completion);
    }
  }

  public AdminCommands getAdminCommands() {
    return adminCommands;
  }

  boolean checkSenderPlayer(CommandSender sender) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatManager.colorMessage("Commands.Only-By-Player"));
      return false;
    }
    return true;
  }

  boolean checkIsInGameInstance(Player player) {
    if (ArenaRegistry.getArena(player) == null) {
      player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Not-Playing"));
      return false;
    }
    return true;
  }

  boolean hasPermission(CommandSender sender, String perm) {
    if (sender.hasPermission(perm)) {
      return true;
    }
    sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Permission"));
    return false;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      if (cmd.getName().equalsIgnoreCase("murdermysteryadmin")) {
        if (args.length == 0) {
          adminCommands.sendHelp(sender);
          return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
          adminCommands.stopGame(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("list")) {
          adminCommands.printList(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("forcestart")) {
          adminCommands.forceStartGame(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
          adminCommands.reloadInstances(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("delete")) {
          if (args.length != 1) {
            adminCommands.deleteArena(sender, args[1]);
          } else {
            sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Type-Arena-Name"));
          }
          return true;
        }
        adminCommands.sendHelp(sender);
        List<StringMatcher.Match> matches = StringMatcher.match(args[0], Arrays.asList("stop", "list", "forcestart", "reload", "setshopchest", "delete"));
        if (!matches.isEmpty()) {
          sender.sendMessage(ChatManager.colorMessage("Commands.Did-You-Mean").replace("%command%", "mma " + matches.get(0).getMatch()));
        }
        return true;
      }
      if (cmd.getName().equalsIgnoreCase("murdermystery")) {
        if (args.length == 0) {
          sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Header"));
          sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Description"));
          if (sender.hasPermission("murdermystery.admin")) {
            sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Admin-Bonus-Description"));
          }
          sender.sendMessage(ChatManager.colorMessage("Commands.Main-Command.Footer"));
          return true;
        }
        if (args.length > 1) {
          if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("edit")) {
            if (!checkSenderPlayer(sender) || !hasPermission(sender, "murdermystery.admin.create")) {
              return true;
            }
            adminCommands.performSetup(sender, args);
            return true;
          }
        }
        if (args[0].equalsIgnoreCase("join")) {
          if (args.length == 2) {
            gameCommands.joinGame(sender, args[1]);
            return true;
          }
          sender.sendMessage(ChatManager.colorMessage("Commands.Type-Arena-Name"));
          return true;
        } else if (args[0].equalsIgnoreCase("randomjoin")) {
          gameCommands.joinRandomGame(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("stats")) {
          if (args.length == 2) {
            gameCommands.sendStatsOther(sender, args[1]);
          }
          gameCommands.sendStats(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("top")) {
          if (args.length == 2) {
            gameCommands.sendTopStatistics(sender, args[1]);
          } else {
            sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Type-Name"));
          }
          return true;
        } else if (args[0].equalsIgnoreCase("leave")) {
          gameCommands.leaveGame(sender);
          return true;
        } else if (args[0].equalsIgnoreCase("create")) {
          if (args.length == 2) {
            adminCommands.createArena(sender, args);
            return true;
          }
          sender.sendMessage(ChatManager.colorMessage("Commands.Type-Arena-Name"));
          return true;
        } else if (args[0].equalsIgnoreCase("admin")) {
          if (args.length == 1) {
            adminCommands.sendHelp(sender);
            return true;
          }
          if (args[1].equalsIgnoreCase("stop")) {
            adminCommands.stopGame(sender);
            return true;
          } else if (args[1].equalsIgnoreCase("list")) {
            adminCommands.printList(sender);
            return true;
          } else if (args[1].equalsIgnoreCase("forcestart")) {
            adminCommands.forceStartGame(sender);
            return true;
          } else if (args[1].equalsIgnoreCase("reload")) {
            adminCommands.reloadInstances(sender);
            return true;
          } else if (args[1].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
              adminCommands.deleteArena(sender, args[2]);
            } else {
              sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Type-Arena-Name"));
            }
            return true;
          }
          adminCommands.sendHelp(sender);
          List<StringMatcher.Match> matches = StringMatcher.match(args[1], Arrays.asList("stop", "list", "forcestart", "reload", "delete"));
          if (!matches.isEmpty()) {
            sender.sendMessage(ChatManager.colorMessage("Commands.Did-You-Mean").replace("%command%", "mm admin " + matches.get(0).getMatch()));
          }
          return true;
        } else {
          List<StringMatcher.Match> matches = StringMatcher.match(args[0], Arrays.asList("join", "leave", "stats", "top", "admin", "create"));
          if (!matches.isEmpty()) {
            sender.sendMessage(ChatManager.colorMessage("Commands.Did-You-Mean").replace("%command%", "mm " + matches.get(0).getMatch()));
          }
          return true;
        }
      }
      return false;
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
      return false;
    }
  }

  void performSetup(Player player, String[] args) {
    if (args[1].equalsIgnoreCase("setup") || args[1].equals("edit")) {
      if (ArenaRegistry.getArena(args[0]) == null) {
        player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Arena-Like-That"));
        return;
      }
      new SetupInventory(ArenaRegistry.getArena(args[0])).openInventory(player);
      return;
    }
    if (!(args.length > 2)) {
      return;
    }
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

    if (!config.contains("instances." + args[0])) {
      player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Arena-Like-That"));
      player.sendMessage(ChatColor.RED + "Usage: /mm <arena> set <minplayers/maxplayers/mapname/lobbylocation/endlocation> <value>");
      return;
    }


    if (args[1].equalsIgnoreCase("add")) {
      if (args.length == 3) {
        if (args[2].equalsIgnoreCase("startloc")) {
          String location = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ()
              + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
          List<String> locs = new ArrayList<>(config.getStringList("instances." + args[0] + ".playerspawnpoints"));
          locs.add(location);
          config.set("instances." + args[0] + ".playerspawnpoints", locs);
          player.sendMessage("Murder Mystery: New player spawn location for arena/instance " + args[0] + " was added");
        }
        if (args[2].equalsIgnoreCase("gold")) {
          String location = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ()
              + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
          List<String> locs = new ArrayList<>(config.getStringList("instances." + args[0] + ".goldspawnpoints"));
          locs.add(location);
          config.set("instances." + args[0] + ".goldspawnpoints", locs);
          player.sendMessage("Murder Mystery: New gold spawn location for arena/instance " + args[0] + " was added");
        }
      }
      ConfigUtils.saveConfig(plugin, config, "arenas");
      return;
    }
    if (!(args[1].equalsIgnoreCase("set"))) {
      return;
    }
    if (args.length == 3) {
      if (args[2].equalsIgnoreCase("lobbylocation") || args[2].equalsIgnoreCase("lobbyloc")) {
        String location = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ()
            + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
        config.set("instances." + args[0] + ".lobbylocation", location);
        player.sendMessage("Murder Mystery: Lobby location for arena/instance " + args[0] + " set to " + LocationUtils.locationToString(player.getLocation()));
      } else if (args[2].equalsIgnoreCase("Endlocation") || args[2].equalsIgnoreCase("Endloc")) {
        String location = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ()
            + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
        config.set("instances." + args[0] + ".Endlocation", location);
        player.sendMessage("Murder Mystery: End location for arena/instance " + args[0] + " set to " + LocationUtils.locationToString(player.getLocation()));
      } else {
        player.sendMessage(ChatColor.RED + "Invalid Command!");
        player.sendMessage(ChatColor.RED + "Usage: /mm <arena> set <lobbylocation/endlocation>");
      }
    } else if (args.length == 4) {
      if (args[2].equalsIgnoreCase("MAXPLAYERS") || args[2].equalsIgnoreCase("maximumplayers")) {
        config.set("instances." + args[0] + ".maximumplayers", Integer.parseInt(args[3]));
        player.sendMessage("Murder Mystery: Maximum players for arena/instance " + args[0] + " set to " + Integer.parseInt(args[3]));

      } else if (args[2].equalsIgnoreCase("MINPLAYERS") || args[2].equalsIgnoreCase("minimumplayers")) {
        config.set("instances." + args[0] + ".minimumplayers", Integer.parseInt(args[3]));
        player.sendMessage("Murder Mystery: Minimum players for arena/instance " + args[0] + " set to " + Integer.parseInt(args[3]));
      } else if (args[2].equalsIgnoreCase("MAPNAME") || args[2].equalsIgnoreCase("NAME")) {
        config.set("instances." + args[0] + ".mapname", args[3]);
        player.sendMessage("Murder Mystery: Map name for arena/instance " + args[0] + " set to " + args[3]);
      } else if (args[2].equalsIgnoreCase("WORLD") || args[2].equalsIgnoreCase("MAP")) {
        boolean exists = false;
        for (World world : Bukkit.getWorlds()) {
          if (world.getName().equalsIgnoreCase(args[3])) {
            exists = true;
          }
        }
        if (!exists) {
          player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatColor.RED + "That world doesn't exists!");
          return;
        }
        config.set("instances." + args[0] + ".world", args[3]);
        player.sendMessage("Murder Mystery: World for arena/instance " + args[0] + " set to " + args[3]);
      } else {
        player.sendMessage(ChatColor.RED + "Invalid Command!");
        player.sendMessage(ChatColor.RED + "Usage: /mm set <minplayers/maxplayers> <value>");
      }
    }
    ConfigUtils.saveConfig(plugin, config, "arenas");
  }

  void createArenaCommand(Player player, String[] args) {
    for (Arena arena : ArenaRegistry.getArenas()) {
      if (arena.getID().equalsIgnoreCase(args[1])) {
        player.sendMessage(ChatColor.DARK_RED + "Arena with that ID already exists!");
        player.sendMessage(ChatColor.DARK_RED + "Usage: /mm create <ID>");
        return;
      }
    }
    if (ConfigUtils.getConfig(plugin, "arenas").contains("instances." + args[1])) {
      player.sendMessage(ChatColor.DARK_RED + "Instance/Arena already exists! Use another ID or delete it first!");
    } else {
      createInstanceInConfig(args[1], player.getWorld().getName());
      player.sendMessage(ChatColor.BOLD + "------------------------------------------");
      player.sendMessage(ChatColor.YELLOW + "      Instance " + args[1] + " created!");
      player.sendMessage("");
      player.sendMessage(ChatColor.GREEN + "Edit this arena via " + ChatColor.GOLD + "/mm " + args[1] + " edit" + ChatColor.GREEN + "!");
      player.sendMessage(ChatColor.GOLD + "Don't know where to start? Check out tutorial video:");
      //todo link with tutorial
      player.sendMessage(ChatColor.GOLD + "link here");
      player.sendMessage(ChatColor.BOLD + "------------------------------------------- ");
    }
  }

  private void createInstanceInConfig(String ID, String worldName) {
    String path = "instances." + ID + ".";
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    LocationUtils.saveLoc(plugin, config, "arenas", path + "lobbylocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    LocationUtils.saveLoc(plugin, config, "arenas", path + "Startlocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    LocationUtils.saveLoc(plugin, config, "arenas", path + "Endlocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    config.set(path + "playerspawnpoints", new ArrayList<>());
    config.set(path + "goldspawnpoints", new ArrayList<>());
    config.set(path + "minimumplayers", 2);
    config.set(path + "maximumplayers", 10);
    config.set(path + "mapname", ID);
    config.set(path + "signs", new ArrayList<>());
    config.set(path + "isdone", false);
    config.set(path + "world", worldName);
    config.set(path + "mystery-cauldrons",  new ArrayList<>());
    config.set(path + "confessionals", new ArrayList<>());
    ConfigUtils.saveConfig(plugin, config, "arenas");

    Arena arena = new Arena(ID, plugin);

    List<Location> playerSpawnPoints = new ArrayList<>();
    for (String loc : config.getStringList(path + "playerspawnpoints")) {
      playerSpawnPoints.add(LocationUtils.getLocation(loc));
    }
    arena.setPlayerSpawnPoints(playerSpawnPoints);
    List<Location> goldSpawnPoints = new ArrayList<>();
    for (String loc : config.getStringList(path + "goldspawnpoints")) {
      goldSpawnPoints.add(LocationUtils.getLocation(loc));
    }
    arena.setGoldSpawnPoints(goldSpawnPoints);

    List<SpecialBlock> specialBlocks = new ArrayList<>();
    if(config.isSet("instances." + arena.getID() + ".mystery-cauldrons")) {
      for (String loc : config.getStringList("instances." + arena.getID() + ".mystery-cauldrons")) {
        specialBlocks.add(new SpecialBlock(LocationUtils.getLocation(loc), SpecialBlock.SpecialBlockType.MYSTERY_CAULDRON));
      }
    }
    for(SpecialBlock block : specialBlocks) {
      arena.loadSpecialBlock(block);
    }
    arena.setMinimumPlayers(ConfigUtils.getConfig(plugin, "arenas").getInt(path + "minimumplayers"));
    arena.setMaximumPlayers(ConfigUtils.getConfig(plugin, "arenas").getInt(path + "maximumplayers"));
    arena.setMapName(ConfigUtils.getConfig(plugin, "arenas").getString(path + "mapname"));
    arena.setLobbyLocation(LocationUtils.getLocation(ConfigUtils.getConfig(plugin, "arenas").getString(path + "lobbylocation")));
    arena.setEndLocation(LocationUtils.getLocation(ConfigUtils.getConfig(plugin, "arenas").getString(path + "Endlocation")));
    arena.setReady(false);

    ArenaRegistry.registerArena(arena);
  }

}
