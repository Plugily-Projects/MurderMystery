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

package pl.plajer.murdermystery.user.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class FileStats {

  private Main plugin;
  private FileConfiguration config;

  public FileStats(Main plugin) {
    this.plugin = plugin;
    config = ConfigUtils.getConfig(plugin, "stats");
  }

  public void saveStat(User user, StatsStorage.StatisticType stat) {
    config.set(user.toPlayer().getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));
    ConfigUtils.saveConfig(plugin, config, "stats");
  }

  public void loadStat(User user, StatsStorage.StatisticType stat) {
    user.setStat(stat, config.getInt(user.toPlayer().getUniqueId().toString() + "." + stat.getName(), 0));
  }

}
