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

package plugily.projects.murdermystery.arena.states;

import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginRestartingState;
import plugily.projects.murdermystery.arena.Arena;

/**
 * @author Plajer
 * <p>
 * Created at 03.06.2019
 */
public class RestartingState extends PluginRestartingState {

  @Override
  public void handleCall(PluginArena arena) {
    super.handleCall(arena);
    Arena pluginArena = (Arena) getPlugin().getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    if(arena.getTimer() <= 0) {
      if(pluginArena.isGoldVisuals()) {
        pluginArena.startGoldVisuals();
      }
    }
  }
}
