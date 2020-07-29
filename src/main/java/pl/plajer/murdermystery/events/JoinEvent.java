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

package pl.plajer.murdermystery.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import pl.plajer.murdermystery.ConfigPreferences;
import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.handlers.PermissionsManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.UpdateChecker;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;

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
    if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && !plugin.getServer().hasWhitelist()
      || e.getResult() != PlayerLoginEvent.Result.KICK_WHITELIST) {
      return;
    }
    if (e.getPlayer().hasPermission(PermissionsManager.getJoinFullGames())) {
      e.setResult(PlayerLoginEvent.Result.ALLOWED);
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    //Load statistics first
    plugin.getUserManager().loadStatistics(plugin.getUserManager().getUser(event.getPlayer()));
    //Teleport to lobby on bungee mode
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
      ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()).teleportToLobby(event.getPlayer());
      return;
    }
    for (Player player : plugin.getServer().getOnlinePlayers()) {
      if (ArenaRegistry.getArena(player) == null) {
        continue;
      }
      player.hidePlayer(event.getPlayer());
      event.getPlayer().hidePlayer(player);
    }
    //load player inventory in case of server crash, file is deleted once loaded so if file was already
    //deleted player won't receive his backup, in case of crash he will get it back
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
      InventorySerializer.loadInventory(plugin, event.getPlayer());
    }
  }

  @EventHandler
  public void onJoinCheckVersion(final PlayerJoinEvent event) {
    if (!plugin.getConfig().getBoolean("Update-Notifier.Enabled", true) || !event.getPlayer().hasPermission("murdermystery.updatenotify")) {
      return;
    }
    //we want to be the first :)
    Bukkit.getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 66614).requestUpdateCheck().whenComplete((result, exception) -> {
      if (!result.requiresUpdate()) {
        return;
      }
      if (result.getNewestVersion().contains("b")) {
        event.getPlayer().sendMessage("");
        event.getPlayer().sendMessage(ChatColor.BOLD + "MURDER MYSTERY UPDATE NOTIFY");
        event.getPlayer().sendMessage(ChatColor.RED + "BETA version of software is ready for update! Proceed with caution.");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + plugin.getDescription().getVersion() + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + result.getNewestVersion());
      } else {
        event.getPlayer().sendMessage("");
        event.getPlayer().sendMessage(ChatColor.BOLD + "MURDER MYSTERY UPDATE NOTIFY");
        event.getPlayer().sendMessage(ChatColor.GREEN + "Software is ready for update! Download it to keep with latest changes and fixes.");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + plugin.getDescription().getVersion() + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + result.getNewestVersion());
      }
    }), 25);
  }
}
