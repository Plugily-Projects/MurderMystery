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

package plugily.projects.murdermystery.handlers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.misc.stuff.ComplementAccessor;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaManager;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.ArenaState;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class BungeeManager implements Listener {

  private final Main plugin;
  private final Map<ArenaState, String> gameStateToString = new EnumMap<>(ArenaState.class);
  private final String motd;
  private final FileConfiguration bungee;

  public BungeeManager(Main plugin) {
    this.plugin = plugin;
    bungee = ConfigUtils.getConfig(plugin, "bungee");

    ChatManager chatManager = plugin.getChatManager();

    gameStateToString.put(ArenaState.WAITING_FOR_PLAYERS, chatManager.colorRawMessage(bungee.getString("MOTD.Game-States.Inactive", "Inactive")));
    gameStateToString.put(ArenaState.STARTING, chatManager.colorRawMessage(bungee.getString("MOTD.Game-States.Starting", "Starting")));
    gameStateToString.put(ArenaState.IN_GAME, chatManager.colorRawMessage(bungee.getString("MOTD.Game-States.In-Game", "In-Game")));
    gameStateToString.put(ArenaState.ENDING, chatManager.colorRawMessage(bungee.getString("MOTD.Game-States.Ending", "Ending")));
    gameStateToString.put(ArenaState.RESTARTING, chatManager.colorRawMessage(bungee.getString("MOTD.Game-States.Restarting", "Restarting")));
    motd = chatManager.colorRawMessage(bungee.getString("MOTD.Message", "The actual game state of mm is %state%"));
    plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public void connectToHub(Player player) {
    if(!bungee.getBoolean("Connect-To-Hub", true)) {
      return;
    }
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Connect");
    out.writeUTF(bungee.getString("Hub"));
    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onServerListPing(ServerListPingEvent event) {
    if(!bungee.getBoolean("MOTD.Manager") || ArenaRegistry.getArenas().isEmpty()) {
      return;
    }
    Arena arena = ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena());
    event.setMaxPlayers(arena.getMaximumPlayers());
    ComplementAccessor.getComplement().setMotd(event, motd.replace("%state%", gameStateToString.get(arena.getArenaState())));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(final PlayerJoinEvent event) {
    if(ArenaRegistry.getArenas().isEmpty()) {
      return;
    }

    ComplementAccessor.getComplement().setJoinMessage(event, "");
    plugin.getServer().getScheduler().runTaskLater(plugin, () -> ArenaManager.joinAttempt(event.getPlayer(), ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena())), 1L);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuit(PlayerQuitEvent event) {
    if(ArenaRegistry.getArenas().isEmpty()) {
      return;
    }

    ComplementAccessor.getComplement().setQuitMessage(event, "");
    if(ArenaRegistry.isInArena(event.getPlayer())) {
      ArenaManager.leaveAttempt(event.getPlayer(), ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()));
    }
  }

}
