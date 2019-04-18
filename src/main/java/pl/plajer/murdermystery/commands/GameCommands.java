/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaManager;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.user.User;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
@Deprecated
public class GameCommands extends MainCommand {

  private Main plugin;

  public GameCommands(Main plugin) {
    super(plugin, false);
    this.plugin = plugin;
  }

  public void sendStats(CommandSender sender) {
    if (!checkSenderPlayer(sender)) {
      return;
    }
    User user = plugin.getUserManager().getUser((Player) sender);
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Header"));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Kills") + user.getStat(StatsStorage.StatisticType.KILLS));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Deaths") + user.getStat(StatsStorage.StatisticType.DEATHS));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Wins") + user.getStat(StatsStorage.StatisticType.WINS));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Loses") + user.getStat(StatsStorage.StatisticType.LOSES));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Games-Played") + user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Highest-Score") + user.getStat(StatsStorage.StatisticType.HIGHEST_SCORE));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Footer"));
  }

  public void sendStatsOther(CommandSender sender, String p) {
    Player player = Bukkit.getPlayerExact(p);
    if (player == null || plugin.getUserManager().getUser(player) == null) {
      sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Admin-Commands.Player-Not-Found"));
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Header-Other").replace("%player%", player.getName()));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Kills") + user.getStat(StatsStorage.StatisticType.KILLS));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Deaths") + user.getStat(StatsStorage.StatisticType.DEATHS));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Wins") + user.getStat(StatsStorage.StatisticType.WINS));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Loses") + user.getStat(StatsStorage.StatisticType.LOSES));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Games-Played") + user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Highest-Score") + user.getStat(StatsStorage.StatisticType.HIGHEST_SCORE));
    sender.sendMessage(ChatManager.colorMessage("Commands.Stats-Command.Footer"));
  }

  public void sendTopStatistics(CommandSender sender, String stat) {
    try {
      StatsStorage.StatisticType statisticType = StatsStorage.StatisticType.valueOf(stat.toUpperCase());
      if (statisticType == StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE || statisticType == StatsStorage.StatisticType.CONTRIBUTION_MURDERER) {
        sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Invalid-Name"));
        return;
      }

      LinkedHashMap<UUID, Integer> stats = (LinkedHashMap<UUID, Integer>) StatsStorage.getStats(statisticType);
      sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Header"));
      String statistic = StringUtils.capitalize(statisticType.toString().toLowerCase().replace("_", " "));
      for (int i = 0; i < 10; i++) {
        try {
          UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
          sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Format")
              .replace("%position%", String.valueOf(i + 1))
              .replace("%name%", Bukkit.getOfflinePlayer(current).getName())
              .replace("%value%", String.valueOf(stats.get(current)))
              .replace("%statistic%", statistic)); //Games_played > Games played etc
          stats.remove(current);
        } catch (IndexOutOfBoundsException ex) {
          sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Format")
              .replace("%position%", String.valueOf(i + 1))
              .replace("%name%", "Empty")
              .replace("%value%", "0")
              .replace("%statistic%", statistic));
        } catch (NullPointerException ex) {
          UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
          if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
            ResultSet set = plugin.getMySQLDatabase().executeQuery("SELECT name FROM playerstats WHERE UUID='" + current.toString() + "'");
            try {
              if (set.next()) {
                sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Format")
                    .replace("%position%", String.valueOf(i + 1))
                    .replace("%name%", set.getString(1))
                    .replace("%value%", String.valueOf(stats.get(current)))
                    .replace("%statistic%", statistic));
                return;
              }
            } catch (SQLException ignored) {
            }
          }
          sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Format")
              .replace("%position%", String.valueOf(i + 1))
              .replace("%name%", "Unknown Player")
              .replace("%value%", String.valueOf(stats.get(current)))
              .replace("%statistic%", statistic));
        }
      }
    } catch (IllegalArgumentException e) {
      sender.sendMessage(ChatManager.colorMessage("Commands.Statistics.Invalid-Name"));
    }
  }

  public void leaveGame(CommandSender sender) {
    if (!checkSenderPlayer(sender)) {
      return;
    }
    if (!plugin.getConfig().getBoolean("Disable-Leave-Command", false)) {
      Player p = (Player) sender;
      if (!checkIsInGameInstance((Player) sender)) {
        return;
      }
      p.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Teleported-To-The-Lobby"));
      if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
        plugin.getBungeeManager().connectToHub(p);
        System.out.print(p.getName() + " is teleported to the Hub Server");
      } else {
        ArenaManager.leaveAttempt(p, ArenaRegistry.getArena(p));
        System.out.print(p.getName() + " has left the arena! He is teleported to the end location.");
      }
    }
  }

  public void joinGame(CommandSender sender, String arenaString) {
    if (!checkSenderPlayer(sender)) {
      return;
    }
    if (ArenaRegistry.isInArena(((Player) sender))) {
      sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Already-Playing"));
      return;
    }
    for (Arena arena : ArenaRegistry.getArenas()) {
      if (arenaString.equalsIgnoreCase(arena.getId())) {
        ArenaManager.joinAttempt((Player) sender, arena);
        return;
      }
    }
    sender.sendMessage(ChatManager.colorMessage("Commands.No-Arena-Like-That"));
  }

  public void joinRandomGame(CommandSender sender) {
    if (!checkSenderPlayer(sender)) {
      return;
    }
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
      return;
    }
    if (ArenaRegistry.isInArena(((Player) sender))) {
      sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Already-Playing"));
      return;
    }
    for (Arena arena : ArenaRegistry.getArenas()) {
      if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
        ArenaManager.joinAttempt((Player) sender, arena);
        return;
      }
    }
    sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Free-Arenas"));
  }

}
