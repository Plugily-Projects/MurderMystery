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

package plugily.projects.murdermystery.user.data;

import org.bukkit.configuration.file.FileConfiguration;
import plugily.projects.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.api.StatsStorage;
import plugily.projects.murdermystery.user.User;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class FileStats implements UserDatabase {

  private final Main plugin;
  private final FileConfiguration config;

  public FileStats(Main plugin) {
    this.plugin = plugin;
    config = ConfigUtils.getConfig(plugin, "stats");
  }

  @Override
  public void saveStatistic(User user, StatsStorage.StatisticType stat) {
    config.set(user.getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));
    ConfigUtils.saveConfig(plugin, config, "stats");
  }

  @Override
  public void saveAllStatistic(User user) {
    for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
      if(!stat.isPersistent()) continue;
      config.set(user.getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));
    }
    ConfigUtils.saveConfig(plugin, config, "stats");
  }

  @Override
  public void loadStatistics(User user) {
    for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
      user.setStat(stat, config.getInt(user.getUniqueId().toString() + "." + stat.getName(), 0));
    }
  }

}
