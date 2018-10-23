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

package pl.plajer.murdermystery.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.core.database.MySQLDatabase;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class MySQLConnectionUtils {

  public static void loadPlayerStats(Player player, Main plugin) {
    boolean b = false;
    MySQLDatabase database = plugin.getMySQLDatabase();
    ResultSet resultSet = database.executeQuery("SELECT UUID from playerstats WHERE UUID='" + player.getUniqueId().toString() + "'");
    try {
      if (!resultSet.next()) {
        plugin.getMySQLManager().insertPlayer(player);
        b = true;
      }

      User user = UserManager.getUser(player.getUniqueId());
      for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
        user.setStat(stat, plugin.getMySQLManager().getStat(player, stat));
      }
      b = true;
    } catch (SQLException e1) {
      System.out.print("CONNECTION FAILED FOR PLAYER " + player.getName());
      e1.printStackTrace();
      MessageUtils.errorOccured();
      Bukkit.getConsoleSender().sendMessage("Cannot save contents to MySQL database!");
      Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
    }
    if (!b) {
      try {
        if (!resultSet.next()) {
          plugin.getMySQLManager().insertPlayer(player);
        }

        User user = UserManager.getUser(player.getUniqueId());
        for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
          user.setStat(stat, plugin.getMySQLManager().getStat(player, stat));
        }
      } catch (SQLException e1) {
        System.out.print("CONNECTION FAILED TWICE FOR PLAYER " + player.getName());
        e1.printStackTrace();
        MessageUtils.errorOccured();
        Bukkit.getConsoleSender().sendMessage("Cannot save contents to MySQL database!");
        Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
      }
    }
  }

}
