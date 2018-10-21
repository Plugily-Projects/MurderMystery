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

package pl.plajer.murdermystery.arena;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.language.LanguageManager;
import pl.plajerlair.core.utils.GameScoreboard;

/**
 * @author Plajer
 * <p>
 * Created at 21.10.2018
 */
public class ScoreboardCache {

  private Map<String, GameScoreboard> cachedScoreboards = new HashMap<>();

  public ScoreboardCache() {
    registerCachedScoreboards();
  }

  /**
   * Cache all scoreboard states to retrieve them in game
   */
  public void registerCachedScoreboards() {
    List<String> lines;
    for (ArenaState state : ArenaState.values()) {
      if (state == ArenaState.RESTARTING) {
        continue;
      }
      lines = LanguageManager.getLanguageList("Scoreboard.Content." + state.getFormattedName());
      GameScoreboard scoreboard = new GameScoreboard("PL_MM", "MM_CR" + state.ordinal(), ChatManager.colorMessage("Scoreboard.Title"));
      for (String line : lines) {
        scoreboard.addRow(line);
      }
      scoreboard.finish();
      cachedScoreboards.put(state.getFormattedName(), scoreboard);
    }
    lines = LanguageManager.getLanguageList("Scoreboard.Content.Playing-Murderer");
    GameScoreboard scoreboard = new GameScoreboard("PL_MM", "MM_CR" + ArenaState.IN_GAME.ordinal() + "M", ChatManager.colorMessage("Scoreboard.Title"));
    for (String line : lines) {
      scoreboard.addRow(line);
    }
    scoreboard.finish();
    cachedScoreboards.put(ArenaState.IN_GAME.getFormattedName() + "M", scoreboard);
  }

  /**
   * @param id formatted arena state
   * @return cached scoreboard
   * @see GameScoreboard
   */
  public GameScoreboard getCachedScoreboard(String id) {
    return cachedScoreboards.get(id);
  }

}
