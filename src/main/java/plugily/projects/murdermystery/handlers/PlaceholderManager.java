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
import org.jetbrains.annotations.NotNull;
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
  public @NotNull String getIdentifier() {
    return "murdermystery";
  }

  @Override
  public @NotNull String getAuthor() {
    return "Plugily Projects";
  }

  @Override
  public @NotNull String getVersion() {
    return "1.0.2";
  }

  @Override
  public String onPlaceholderRequest(Player player, String id) {
    if(player == null) {
      return null;
    }
    switch(id.toLowerCase()) {
      case "kills":
        return Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.KILLS));
      case "deaths":
        return Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.DEATHS));
      case "games_played":
        return Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.GAMES_PLAYED));
      case "highest_score":
        return Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.HIGHEST_SCORE));
      case "wins":
        return Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.WINS));
      case "loses":
        return Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOSES));
      case "arena_players_online":
        return Integer.toString(ArenaRegistry.getArenaPlayersOnline());
      default:
        return handleArenaPlaceholderRequest(id);
    }
  }

  private String handleArenaPlaceholderRequest(String id) {
    if(!id.contains(":")) {
      return null;
    }
    String[] data = id.split(":");
    Arena arena = ArenaRegistry.getArena(data[0]);
    if(arena == null) {
      return null;
    }
    switch(data[1].toLowerCase()) {
      case "players":
        return Integer.toString(arena.getPlayers().size());
      case "max_players":
        return Integer.toString(arena.getMaximumPlayers());
      case "state":
        return arena.getArenaState().getFormattedName();
      case "state_pretty":
        return arena.getArenaState().getPlaceholder();
      case "mapname":
        return arena.getMapName();
      default:
        return null;
    }
  }

}
