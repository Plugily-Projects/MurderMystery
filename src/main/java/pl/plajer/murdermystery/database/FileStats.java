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

package pl.plajer.murdermystery.database;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.murdermysteryapi.StatsStorage;
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

  public void saveStat(Player player, StatsStorage.StatisticType stat) {
    User user = UserManager.getUser(player.getUniqueId());
    config.set(player.getUniqueId().toString() + "." + stat, user.getStat(stat));
    ConfigUtils.saveConfig(plugin, config, "stats");
  }

  public void loadStat(Player player, StatsStorage.StatisticType stat) {
    User user = UserManager.getUser(player.getUniqueId());
    if (config.contains(player.getUniqueId().toString() + "." + stat)) {
      user.setStat(stat, config.getInt(player.getUniqueId().toString() + "." + stat));
    } else {
      if (stat == StatsStorage.StatisticType.CONTRIBUTION_DETECTIVE || stat == StatsStorage.StatisticType.CONTRIBUTION_MURDERER) {
        user.setStat(stat, 1);
      } else {
        user.setStat(stat, 0);
      }
    }
  }

  public void loadStatsForPlayersOnline() {
    for (final Player player : plugin.getServer().getOnlinePlayers()) {
      if (plugin.isBungeeActivated()) {
        ArenaRegistry.getArenas().get(0).teleportToLobby(player);
      }
      if (!plugin.isDatabaseActivated()) {
        for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
          loadStat(player, stat);
        }
        continue;
      }
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> MySQLConnectionUtils.loadPlayerStats(player, plugin));
    }
  }

}
