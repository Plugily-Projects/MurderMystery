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

package plugily.projects.murdermystery.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;

/**
 * @author Plajer
 * <p>
 * Created at 08.08.2018
 */
public class PlaceholderManager extends PlaceholderExpansion {

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String getIdentifier() {
    return "murdermystery";
  }

  @Override
  public String getPlugin() {
    return null;
  }

  @Override
  public String getAuthor() {
    return "Plajer";
  }

  @Override
  public String getVersion() {
    return "1.0.1";
  }

  @Override
  public String onPlaceholderRequest(Player player, String id) {
    if (player == null) {
      return null;
    }
    switch (id.toLowerCase()) {
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
      default:
        return handleArenaPlaceholderRequest(id);
    }
  }

  private String handleArenaPlaceholderRequest(String id) {
    if (!id.contains(":")) {
      return null;
    }
    String[] data = id.split(":");
    Arena arena = ArenaRegistry.getArena(data[0]);
    if (arena == null) {
      return null;
    }
    switch (data[1].toLowerCase()) {
      case "players":
        return String.valueOf(arena.getPlayers().size());
      case "max_players":
        return String.valueOf(arena.getMaximumPlayers());
      case "state":
        return String.valueOf(arena.getArenaState());
      case "state_pretty":
        return arena.getArenaState().getFormattedName();
      case "mapname":
        return arena.getMapName();
      default:
        return null;
    }
  }

}
