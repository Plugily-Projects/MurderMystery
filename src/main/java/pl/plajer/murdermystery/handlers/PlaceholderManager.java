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

package pl.plajer.murdermystery.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;

import pl.plajer.murdermystery.api.StatsStorage;

/**
 * @author Plajer
 * <p>
 * Created at 08.08.2018
 */
public class PlaceholderManager extends PlaceholderExpansion {

  public boolean persist() {
    return true;
  }

  public String getIdentifier() {
    return "murdermystery";
  }

  public String getPlugin() {
    return null;
  }

  public String getAuthor() {
    return "Plajer";
  }

  public String getVersion() {
    return "1.0.0";
  }

  public String onPlaceholderRequest(Player player, String id) {
    if (player == null) {
      return null;
    }
    switch (id) {
      case "kills":
        return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.KILLS));
      case "deaths":
        return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.DEATHS));
      case "games_played":
        return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.GAMES_PLAYED));
      case "highest_score":
        return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.HIGHEST_SCORE));
      case "wins":
        return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.WINS));
      case "loses":
        return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOSES));
    }
    return null;
  }
}
