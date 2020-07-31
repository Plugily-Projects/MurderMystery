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

package plugily.projects.murdermystery.arena.options;

/**
 * @author Plajer
 * <p>
 * Created at 24.03.2019
 */
public enum ArenaOption {
  /**
   * Current arena timer, ex. 30 seconds before game starts.
   */
  TIMER(0),
  /**
   * Minimum players in arena needed to start.
   */
  MINIMUM_PLAYERS(2),
  /**
   * Maximum players arena can hold, users with full games permission can bypass this!
   */
  MAXIMUM_PLAYERS(10);

  private final int defaultValue;

  ArenaOption(int defaultValue) {
    this.defaultValue = defaultValue;
  }

  public int getDefaultValue() {
    return defaultValue;
  }
}
