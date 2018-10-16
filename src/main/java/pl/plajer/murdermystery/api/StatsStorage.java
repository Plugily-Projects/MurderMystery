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

package pl.plajer.murdermystery.api;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * @since 0.0.1-alpha
 * <p>
 * Class for accessing users statistics.
 */
public class StatsStorage {

  public static Main plugin;

  private static Map sortByValue(Map unsortMap) {
    List list = new LinkedList(unsortMap.entrySet());
    list.sort((o1, o2) -> ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue()));
    Map sortedMap = new LinkedHashMap();
    for (Object aList : list) {
      Map.Entry entry = (Map.Entry) aList;
      sortedMap.put(entry.getKey(), entry.getValue());
    }
    return sortedMap;
  }

  /**
   * Get all UUID's sorted ascending by Statistic Type
   *
   * @param stat Statistic type to get (kills, deaths etc.)
   * @return Map of UUID keys and Integer values sorted in ascending order of requested statistic type
   */
  public static Map<UUID, Integer> getStats(StatisticType stat) {
    Main.debug(Main.LogLevel.INFO, "Village API getStats(" + stat.getName() + ") run");
    if (plugin.isDatabaseActivated()) {
      return plugin.getMySQLManager().getColumn(stat.getName());
    } else {
      FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
      Map<UUID, Integer> stats = new TreeMap<>();
      for (String string : config.getKeys(false)) {
        stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
      }
      return sortByValue(stats);
    }
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
    Main.debug(Main.LogLevel.INFO, "Village API getUserStats(" + player.getName() + ", " + statisticType.getName() + ") run");
    return UserManager.getUser(player.getUniqueId()).getStat(statisticType);
  }

  /**
   * Available statistics to get.
   */
  public enum StatisticType {
    CONTRIBUTION_DETECTIVE("contribdetective", true), CONTRIBUTION_MURDERER("contribmurderer", true), DEATHS("deaths", true), GAMES_PLAYED("gamesplayed", true), HIGHEST_SCORE("highestscore", true),
    KILLS("kills", true), LOSES("loses", true), WINS("wins", true), LOCAL_CURRENT_PRAY("local_pray", false), LOCAL_GOLD("gold", false), LOCAL_KILLS("local_kills", false),
    LOCAL_PRAISES("local_praises", false), LOCAL_SCORE("local_score", false);

    private String name;
    private boolean persistent;

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
