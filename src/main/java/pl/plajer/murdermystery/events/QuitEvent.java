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

package pl.plajer.murdermystery.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.ArenaManager;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajer.murdermystery.utils.MessageUtils;
import pl.plajerlair.core.services.exception.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class QuitEvent implements Listener {

  private Main plugin;

  public QuitEvent(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    if (ArenaRegistry.getArena(event.getPlayer()) == null) {
      return;
    }
    if (!plugin.isBungeeActivated()) {
      ArenaManager.leaveAttempt(event.getPlayer(), ArenaRegistry.getArena(event.getPlayer()));
    }
  }

  @EventHandler
  public void onQuitSaveStats(PlayerQuitEvent event) {
    try {
      if (ArenaRegistry.getArena(event.getPlayer()) != null) {
        ArenaManager.leaveAttempt(event.getPlayer(), ArenaRegistry.getArena(event.getPlayer()));
      }
      final User user = UserManager.getUser(event.getPlayer().getUniqueId());
      final Player player = event.getPlayer();
      if (plugin.isDatabaseActivated()) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
          for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
            if(!stat.isPersistent()) {
              return;
            }
            int i;
            try {
              i = plugin.getMySQLManager().getStat(player, stat);
            } catch (NullPointerException npe) {
              i = 0;
              System.out.print("COULDN'T GET STATS FROM PLAYER: " + player.getName());
              npe.printStackTrace();
              MessageUtils.errorOccured();
              Bukkit.getConsoleSender().sendMessage("Cannot get stats from MySQL database!");
              Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
            }

            if (i > user.getStat(stat)) {
              plugin.getMySQLManager().setStat(player, stat, user.getStat(stat) + i);
            } else {
              plugin.getMySQLManager().setStat(player, stat, user.getStat(stat));
            }
            plugin.getMySQLDatabase().executeUpdate("UPDATE playerstats SET name='" + player.getName() + "' WHERE UUID='" + player.getUniqueId().toString() + "';");
          }
        });
      } else {
        for(StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
          plugin.getFileStats().saveStat(player, stat);
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

}
