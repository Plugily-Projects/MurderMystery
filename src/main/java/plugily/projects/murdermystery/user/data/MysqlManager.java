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

package plugily.projects.murdermystery.user.data;

import org.bukkit.Bukkit;
import pl.plajerlair.commonsbox.database.MysqlDatabase;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.user.User;
import plugily.projects.murdermystery.utils.Debugger;
import plugily.projects.murdermystery.utils.MessageUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * @author Plajer
 * <p>
 * Created at 03.10.2018
 */
public class MysqlManager implements UserDatabase {

  private final Main plugin;
  private final MysqlDatabase database;

  public MysqlManager(Main plugin) {
    this.plugin = plugin;
    database = plugin.getMysqlDatabase();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try(Connection connection = database.getConnection()) {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + getTableName() + "` (\n"
            + "  `UUID` char(36) NOT NULL PRIMARY KEY,\n"
            + "  `name` varchar(32) NOT NULL,\n"
            + "  `kills` int(11) NOT NULL DEFAULT '0',\n"
            + "  `deaths` int(11) NOT NULL DEFAULT '0',\n"
            + "  `highestscore` int(11) NOT NULL DEFAULT '0',\n"
            + "  `gamesplayed` int(11) NOT NULL DEFAULT '0',\n"
            + "  `wins` int(11) NOT NULL DEFAULT '0',\n"
            + "  `loses` int(11) NOT NULL DEFAULT '0',\n"
            + "  `contribmurderer` int(11) NOT NULL DEFAULT '1',\n"
            + "  `contribdetective` int(11) NOT NULL DEFAULT '1'\n"
            + "  `murderer_pass` int(11) NOT NULL DEFAULT '0',\n"
            + "  `detective_pass` int(11) NOT NULL DEFAULT '0',\n"
            + ");");
      } catch(SQLException e) {
        e.printStackTrace();
        MessageUtils.errorOccurred();
        Debugger.sendConsoleMsg("Cannot save contents to MySQL database!");
        Debugger.sendConsoleMsg("Check configuration of mysql.yml file or disable mysql option in config.yml");
      }
    });
  }

  @Override
  public void saveStatistic(User user, StatsStorage.StatisticType stat) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      database.executeUpdate("UPDATE " + getTableName() + " SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getUniqueId().toString() + "';");
      Debugger.debug(Level.INFO, "Executed MySQL: " + "UPDATE " + getTableName() + " SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getUniqueId().toString() + "';");
    });
  }

  @Override
  public void saveAllStatistic(User user) {
    StringBuilder update = new StringBuilder(" SET ");
    for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
      if(!stat.isPersistent()) continue;
      if(update.toString().equalsIgnoreCase(" SET ")) {
        update.append(stat.getName()).append('=').append(user.getStat(stat));
      }
      update.append(", ").append(stat.getName()).append('=').append(user.getStat(stat));
    }
    String finalUpdate = update.toString();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        database.executeUpdate("UPDATE " + getTableName() + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';"));
  }

  @Override
  public void loadStatistics(User user) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      String uuid = user.getUniqueId().toString();
      try(Connection connection = database.getConnection()) {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * from " + getTableName() + " WHERE UUID='" + uuid + "';");
        if(rs.next()) {
          //player already exists - get the stats
          Debugger.debug(Level.INFO, "MySQL Stats | Player {0} already exist. Getting Stats...", user.getPlayer().getName());
          for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
            if(!stat.isPersistent()) continue;
            int val = rs.getInt(stat.getName());
            user.setStat(stat, val);
          }
        } else {
          //player doesn't exist - make a new record
          Debugger.debug(Level.INFO, "MySQL Stats | Player {0} does not exist. Creating new one...", user.getPlayer().getName());
          statement.executeUpdate("INSERT INTO " + getTableName() + " (UUID,name) VALUES ('" + uuid + "','" + user.getPlayer().getName() + "');");
          for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
            if(!stat.isPersistent()) continue;
            if(stat == StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE || stat == StatsStorage.StatisticType.CONTRIBUTION_MURDERER) {
              user.setStat(stat, 1);
            } else {
              user.setStat(stat, 0);
            }
          }
        }
      } catch(SQLException e) {
        e.printStackTrace();
      }
    });
  }

  public String getTableName() {
    return ConfigUtils.getConfig(plugin, "mysql").getString("table", "playerstats");
  }

  public MysqlDatabase getDatabase() {
    return database;
  }
}
