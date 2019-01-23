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

package pl.plajer.murdermystery.user.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.core.database.MySQLDatabase;

/**
 * @author Plajer
 * <p>
 * Created at 03.10.2018
 */
public class MySQLManager implements UserDatabase {

  private MySQLDatabase database;

  public MySQLManager(Main plugin) {
    database = plugin.getMySQLDatabase();
    try {
      Connection conn = database.getManager().getConnection();
      conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `playerstats` (\n"
          + "  `UUID` text NOT NULL,\n"
          + "  `name` text NOT NULL,\n"
          + "  `kills` int(11) NOT NULL DEFAULT '0',\n"
          + "  `deaths` int(11) NOT NULL DEFAULT '0',\n"
          + "  `highestscore` int(11) NOT NULL DEFAULT '0',\n"
          + "  `gamesplayed` int(11) NOT NULL DEFAULT '0',\n"
          + "  `wins` int(11) NOT NULL DEFAULT '0',\n"
          + "  `loses` int(11) NOT NULL DEFAULT '0',\n"
          + "  `contribmurderer` int(11) NOT NULL DEFAULT '1'\n"
          + "  `contribdetective` int(11) NOT NULL DEFAULT '1'\n"
          + ");");
      database.getManager().closeConnection(conn);
    } catch (SQLException e) {
      e.printStackTrace();
      MessageUtils.errorOccured();
      Bukkit.getConsoleSender().sendMessage("Cannot save contents to MySQL database!");
      Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
    }
  }

  public void insertPlayer(Player player) {
    database.executeUpdate("INSERT INTO playerstats (UUID,name) VALUES ('" + player.getUniqueId().toString() + "','" + player.getName() + "')");
  }

  @Override
  public void saveStatistic(User user, StatsStorage.StatisticType stat) {
    database.executeUpdate("UPDATE playerstats SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
  }

  @Override
  public void loadStatistic(User user, StatsStorage.StatisticType stat) {
    ResultSet resultSet = database.executeQuery("SELECT UUID from playerstats WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "'");
    //insert into the database
    try {
      if (!resultSet.next()) {
        insertPlayer(user.getPlayer());
      }
    } catch (SQLException e1) {
      System.out.print("CONNECTION FAILED FOR PLAYER " + user.getPlayer().getName());
    }

    ResultSet set = database.executeQuery("SELECT " + stat.getName() + " FROM playerstats WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "'");
    try {
      if (!set.next()) {
        user.setStat(stat, 0);
        return;
      }
      user.setStat(stat, set.getInt(1));
    } catch (SQLException e) {
      e.printStackTrace();
      user.setStat(stat, 0);
    }
  }

  public Map<UUID, Integer> getColumn(String player) {
    ResultSet set = database.executeQuery("SELECT UUID, " + player + " FROM playerstats ORDER BY " + player + " ASC;");
    Map<UUID, Integer> column = new LinkedHashMap<>();
    try {
      while (set.next()) {
        column.put(java.util.UUID.fromString(set.getString("UUID")), set.getInt(player));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return column;
  }

}
