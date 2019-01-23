/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and Tigerpanzer
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

package pl.plajer.murdermystery.user.data;

import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.user.User;

/**
 * @author Plajer
 * <p>
 * Created at 23.01.2019
 */
public interface UserDatabase {

  /**
   * Saves player statistic into yaml or MySQL storage based on user choice
   *
   * @param user user to retrieve statistic from
   * @param stat stat to save to storage
   */
  void saveStatistic(User user, StatsStorage.StatisticType stat);

  /**
   * Loads player statistic from yaml or MySQL storage based on user choice
   *
   * @param user user to load statistic for
   * @param stat type of stat to load from storage
   */
  void loadStatistic(User user, StatsStorage.StatisticType stat);

}
