/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (c) 2022  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.murdermystery.arena.managers;

import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.api.arena.IArenaState;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.managers.PluginScoreboardManager;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.murdermystery.arena.role.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigerpanzer_02
 * <p>Created at 19.12.2021
 */
public class ScoreboardManager extends PluginScoreboardManager {

  private final PluginArena arena;

  public ScoreboardManager(PluginArena arena) {
    super(arena);
    this.arena = arena;
  }

  @Override
  public List<String> formatScoreboardLines(List<String> lines, Player player) {
  if(arena.getArenaState() == IArenaState.IN_GAME) {
    IUser user = arena.getPlugin().getUserManager().getUser(player);
      lines =
          arena
              .getPlugin()
              .getLanguageManager()
              .getLanguageList(
                  "Scoreboard.Content." + arena.getArenaState().getFormattedName() + (Role.isRole(Role.MURDERER, user) ? "-Murderer" : ""));
    } else {
      lines = arena.getPlugin().getLanguageManager().getLanguageList(arena.getArenaState() == IArenaState.FULL_GAME ? "Scoreboard.Content.Starting"
        : "Scoreboard.Content." + arena.getArenaState().getFormattedName());
    }
    List<String> formattedLines = new ArrayList<>();
    for(String line : lines) {
      formattedLines.add(new MessageBuilder(line).player(player).arena(arena).build());
    }
    return formattedLines;
  }
}
