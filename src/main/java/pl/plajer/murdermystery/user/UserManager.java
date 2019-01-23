/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and Tigerpanzer
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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.user.data.FileStats;
import pl.plajer.murdermystery.user.data.MySQLManager;
import pl.plajer.murdermystery.user.data.UserDatabase;
import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class UserManager implements UserDatabase {

  private MySQLManager mySQLManager;
  private FileStats fileStats;
  private List<User> users = new ArrayList<>();
  private Main plugin;

  public UserManager(Main plugin) {
    this.plugin = plugin;
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      mySQLManager = new MySQLManager(plugin);
    } else {
      fileStats = new FileStats(plugin);
    }
    loadStatsForPlayersOnline();
  }

  private void loadStatsForPlayersOnline() {
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      User user = getUser(player);
      for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
        loadStatistic(user, stat);
      }
    }
  }

  public User getUser(Player player) {
    for (User user : users) {
      if (user.getPlayer().equals(player)) {
        return user;
      }
    }
    Debugger.debug(LogLevel.INFO, "Registering new user with UUID: " + player.getUniqueId() + " (" + player.getName() + ")");
    User user = new User(player);
    users.add(user);
    return user;
  }

  public List<User> getUsers(Arena arena) {
    List<User> users = new ArrayList<>();
    for (Player player : arena.getPlayers()) {
      users.add(getUser(player));
    }
    return users;
  }

  @Override
  public void saveStatistic(User user, StatsStorage.StatisticType stat) {
    if (!stat.isPersistent()) {
      return;
    }
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> mySQLManager.saveStatistic(user, stat));
      return;
    }
    fileStats.saveStatistic(user, stat);
  }

  @Override
  public void loadStatistic(User user, StatsStorage.StatisticType stat) {
    if (!stat.isPersistent()) {
      return;
    }
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> mySQLManager.loadStatistic(user, stat));
      return;
    }
    fileStats.loadStatistic(user, stat);
  }

  public void removeUser(User user) {
    users.remove(user);
  }

}
