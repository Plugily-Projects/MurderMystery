/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.api.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.api.events.MurderMysteryEvent;
import pl.plajer.murdermystery.arena.Arena;

/**
 * @author Plajer
 * @see StatsStorage.StatisticType
 * @since 0.0.3b
 * <p>
 * Called when player receive new statistic.
 */
public class MMPlayerStatisticChangeEvent extends MurderMysteryEvent {

  private static final HandlerList HANDLERS = new HandlerList();
  private Player player;
  private StatsStorage.StatisticType statisticType;
  private int number;

  public MMPlayerStatisticChangeEvent(Arena eventArena, Player player, StatsStorage.StatisticType statisticType, int number) {
    super(eventArena);
    this.player = player;
    this.statisticType = statisticType;
    this.number = number;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public Player getPlayer() {
    return player;
  }

  public StatsStorage.StatisticType getStatisticType() {
    return statisticType;
  }

  public int getNumber() {
    return number;
  }

}
