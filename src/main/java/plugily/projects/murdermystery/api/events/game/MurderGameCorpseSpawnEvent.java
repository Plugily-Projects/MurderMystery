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

package plugily.projects.murdermystery.api.events.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import plugily.projects.minigamesbox.classic.api.event.PlugilyEvent;
import plugily.projects.murdermystery.arena.Arena;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.04.2022
 */
public class MurderGameCorpseSpawnEvent extends PlugilyEvent implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();
  private boolean isCancelled = false;
  private final Player player;
  private final Location location;

  public MurderGameCorpseSpawnEvent(Arena arena, Player player, Location location) {
    super(arena);
    this.player = player;
    this.location = location;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  @Override
  public boolean isCancelled() {
    return isCancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    isCancelled = cancelled;
  }

  public Player getPlayer() {
    return player;
  }

  public Location getLocation() {
    return location;
  }

}
