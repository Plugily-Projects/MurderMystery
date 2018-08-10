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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.database.FileStats;
import pl.plajer.murdermystery.database.MySQLConnectionUtils;
import pl.plajer.murdermystery.handlers.PermissionsManager;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajerlair.core.services.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class JoinEvent implements Listener {

  private Main plugin;

  public JoinEvent(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onLogin(PlayerLoginEvent e) {
    if (!plugin.isBungeeActivated() && !plugin.getServer().hasWhitelist()
            || e.getResult() != PlayerLoginEvent.Result.KICK_WHITELIST) {
      return;
    }
    if (e.getPlayer().hasPermission(PermissionsManager.getJoinFullGames())) {
      e.setResult(PlayerLoginEvent.Result.ALLOWED);
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (plugin.isBungeeActivated()) {
      return;
    }
    for (Player player : plugin.getServer().getOnlinePlayers()) {
      if (ArenaRegistry.getArena(player) == null) {
        continue;
      }
      player.hidePlayer(event.getPlayer());
      event.getPlayer().hidePlayer(player);
    }
  }

  @EventHandler
  public void onJoinCheckVersion(final PlayerJoinEvent event) {
    try {
      //we want to be the first :)
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (event.getPlayer().isOp() && !plugin.isDataEnabled()) {
          event.getPlayer().sendMessage(ChatColor.RED + "[Murder Mystery] It seems that you've disabled bStats statistics.");
          event.getPlayer().sendMessage(ChatColor.RED + "Please consider enabling it to help us develop our plugins better!");
          event.getPlayer().sendMessage(ChatColor.RED + "Enable it in plugins/bStats/config.yml file");
        }
        //todo
      /*if (event.getPlayer().hasPermission("murdermystery.updatenotify")) {
        if (plugin.getConfig().getBoolean("Update-Notifier.Enabled", true)) {
          String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("MurderMystery").getDescription().getVersion();
          String latestVersion;
          try {
            UpdateChecker.checkUpdate(currentVersion);
            latestVersion = UpdateChecker.getLatestVersion();
            if (latestVersion != null) {
              latestVersion = "v" + latestVersion;
              if (latestVersion.contains("b")) {
                event.getPlayer().sendMessage("");
                event.getPlayer().sendMessage(ChatColor.BOLD + "MURDER MYSTERY UPDATE NOTIFY");
                event.getPlayer().sendMessage(ChatColor.RED + "BETA version of software is ready for update! Proceed with caution.");
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + currentVersion + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + latestVersion);
              } else {
                event.getPlayer().sendMessage("");
                event.getPlayer().sendMessage(ChatColor.BOLD + "MURDER MYSTERY UPDATE NOTIFY");
                event.getPlayer().sendMessage(ChatColor.GREEN + "Software is ready for update! Download it to keep with latest changes and fixes.");
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + currentVersion + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + latestVersion);
              }
            }
          } catch (Exception ex) {
            event.getPlayer().sendMessage(ChatColor.RED + "[Murder Mystery] An error occured while checking for update!");
            event.getPlayer().sendMessage(ChatColor.RED + "Please check internet connection or check for update via WWW site directly!");
            event.getPlayer().sendMessage(ChatColor.RED + "WWW site todo");
          }
        }
      }*/
      }, 25);
      if (plugin.isBungeeActivated()) {
        ArenaRegistry.getArenas().get(0).teleportToLobby(event.getPlayer());
      }
      UserManager.registerUser(event.getPlayer().getUniqueId());
      if (!plugin.isDatabaseActivated()) {
        for (String s : FileStats.STATISTICS.keySet()) {
          plugin.getFileStats().loadStat(event.getPlayer(), s);
        }
        return;
      }
      final Player player = event.getPlayer();
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> MySQLConnectionUtils.loadPlayerStats(player, plugin));
    } catch (Exception ex){
      new ReportedException(plugin, ex);
    }
  }
}
