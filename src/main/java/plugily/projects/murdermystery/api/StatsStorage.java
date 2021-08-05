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

package plugily.projects.murdermystery.api;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import plugily.projects.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.commonsbox.sorter.SortUtils;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.user.data.MysqlManager;
import plugily.projects.murdermystery.utils.MessageUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Plajer
 * @since 0.0.1-alpha
 * <p>
 * Class for accessing users statistics.
 */
public class StatsStorage {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);

  /**
   * Get all UUID's sorted ascending by Statistic Type
   *
   * @param stat Statistic type to get (kills, deaths etc.)
   * @return Map of UUID keys and Integer values sorted in ascending order of requested statistic type
   */
  @NotNull
  @Contract("null -> fail")
  public static Map<UUID, Integer> getStats(StatisticType stat) {
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      try(Connection connection = plugin.getMysqlDatabase().getConnection()) {
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery("SELECT UUID, " + stat.getName() + " FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " ORDER BY " + stat.getName());
        Map<UUID, Integer> column = new LinkedHashMap<>();
        while(set.next()) {
          try {
            column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
          } catch (IllegalArgumentException e) {
          }
        }
        return column;
      } catch(SQLException e) {
        plugin.getLogger().log(Level.WARNING, "SQLException occurred! " + e.getSQLState() + " (" + e.getErrorCode() + ")");
        MessageUtils.errorOccurred();
        Bukkit.getConsoleSender().sendMessage("Cannot get contents from MySQL database!");
        Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
        return Collections.emptyMap();
      }
    }
    FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
    Map<UUID, Integer> stats = new TreeMap<>();
    for(String string : config.getKeys(false)) {
      if(string.equals("data-version")) {
        continue;
      }

      try {
        stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
      } catch (IllegalArgumentException e) {
      }
    }
    return SortUtils.sortByValue(stats);
  }

  /**
   * Get user statistic based on StatisticType
   *
   * @param player        Online player to get data from
   * @param statisticType Statistic type to get (kills, deaths etc.)
   * @return int of statistic
   * @see StatisticType
   */
  public static int getUserStats(Player player, StatisticType statisticType) {
    return plugin.getUserManager().getUser(player).getStat(statisticType);
  }

  /**
   * Set user statistic based on StatisticType
   *
   * @param player        Online player to get data from
   * @param statisticType Statistic type to get (kills, deaths etc.)
   * @param value         int of statistic
   * @see StatisticType
   */
  public static void setUserStat(Player player, StatisticType statisticType, int value) {
    plugin.getUserManager().getUser(player).setStat(statisticType, value);
  }

  /**
   * Available statistics to get.
   */
  public enum StatisticType {
    CONTRIBUTION_DETECTIVE("contribdetective", true),
    CONTRIBUTION_MURDERER("contribmurderer", true), DEATHS("deaths", true), GAMES_PLAYED("gamesplayed", true), HIGHEST_SCORE("highestscore", true),
    KILLS("kills", true), LOSES("loses", true), WINS("wins", true), LOCAL_CURRENT_PRAY("local_pray", false), LOCAL_GOLD("gold", false), LOCAL_KILLS("local_kills", false),
    LOCAL_PRAISES("local_praises", false), LOCAL_SCORE("local_score", false), MURDERER_PASS("murderer_pass", true), DETECTIVE_PASS("murderer_pass", true);

    private final String name;
    private final boolean persistent;

    StatisticType(String name, boolean persistent) {
      this.name = name;
      this.persistent = persistent;
    }

    public String getName() {
      return name;
    }

    public boolean isPersistent() {
      return persistent;
    }
  }

}
