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

package plugily.projects.murdermystery.api.events.game;

import org.bukkit.event.HandlerList;
import plugily.projects.murdermystery.api.events.MurderMysteryEvent;
import plugily.projects.murdermystery.arena.Arena;

/**
 * @author Plajer
 * @since 0.0.1-alpha
 * <p>
 * Called when arena has started.
 */
public class MMGameStartEvent extends MurderMysteryEvent {

  private static final HandlerList HANDLERS = new HandlerList();

  public MMGameStartEvent(Arena arena) {
    super(arena);
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

}
