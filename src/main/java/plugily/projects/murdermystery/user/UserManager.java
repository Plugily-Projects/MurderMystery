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
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.user.data.FileStats;
import plugily.projects.murdermystery.user.data.MysqlManager;
import plugily.projects.murdermystery.user.data.UserDatabase;
import plugily.projects.murdermystery.utils.Debugger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class UserManager {

  private final List<User> users = new ArrayList<>();
  private final UserDatabase database;

  public UserManager(Main plugin) {
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      database = new MysqlManager(plugin);
      Debugger.debug("MySQL Stats enabled");
    } else {
      database = new FileStats(plugin);
      Debugger.debug("File Stats enabled");
    }
    loadStatsForPlayersOnline();
  }

  private void loadStatsForPlayersOnline() {
    Bukkit.getServer().getOnlinePlayers().stream().map(this::getUser).forEach(this::loadStatistics);
  }

  public User getUser(Player player) {
    java.util.UUID playerId = player.getUniqueId();

    for(User user : users) {
      if(user.getUniqueId().equals(playerId)) {
        return user;
      }
    }

    Debugger.debug("Registering new user {0} ({1})", playerId, player.getName());
    User user = new User(playerId);
    users.add(user);
    return user;
  }

  public List<User> getUsers(Arena arena) {
    List<User> list = new ArrayList<>();

    for(Player player : arena.getPlayers()) {
      list.add(getUser(player));
    }

    return list;
  }

  public void saveStatistic(User user, StatsStorage.StatisticType stat) {
    if(!stat.isPersistent()) {
      return;
    }
    //apply before save
    fixContributionStat(user);
    database.saveStatistic(user, stat);
  }

  public void loadStatistics(User user) {
    database.loadStatistics(user);
    //apply after load to override
    fixContributionStat(user);
  }

  private void fixContributionStat(User user) {
    if(user.getStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE) <= 0) {
      user.setStat(StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE, 1);
    }
    if(user.getStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER) <= 0) {
      user.setStat(StatsStorage.StatisticType.CONTRIBUTION_MURDERER, 1);
    }
  }

  public void saveAllStatistic(User user) {
    database.saveAllStatistic(user);
  }

  public void removeUser(User user) {
    users.remove(user);
  }

  public UserDatabase getDatabase() {
    return database;
  }
}
