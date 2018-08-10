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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.language.LanguageManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.user.UserManager;
import pl.plajerlair.core.services.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class ChatEvents implements Listener {

  private Main plugin;
  private String[] regexChars = new String[]{"$", "\\"};

  public ChatEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    try {
      if (ArenaRegistry.getArena(event.getPlayer()) == null) {
        for (Player player : event.getRecipients()) {
          if (ArenaRegistry.getArena(event.getPlayer()) == null) {
            return;
          }
          event.getRecipients().remove(player);
        }
      }
      event.getRecipients().clear();
      event.getRecipients().addAll(ArenaRegistry.getArena(event.getPlayer()).getPlayers());
    } catch (Exception ex){
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onChatIngame(AsyncPlayerChatEvent event) {
    try {
      Arena arena = ArenaRegistry.getArena(event.getPlayer());
      if (arena == null) {
        return;
      }
      if (plugin.isChatFormatEnabled()) {
        event.setCancelled(true);
        Iterator<Player> iterator = event.getRecipients().iterator();
        List<Player> remove = new ArrayList<>();
        while (iterator.hasNext()) {
          Player player = iterator.next();
          remove.add(player);
        }
        for (Player player : remove) {
          event.getRecipients().remove(player);
        }
        remove.clear();
        String message;
        String eventMessage = event.getMessage();
        boolean dead = !arena.getPlayersLeft().contains(event.getPlayer());
        for (String regexChar : regexChars) {
          if (eventMessage.contains(regexChar)) {
            eventMessage = eventMessage.replaceAll(Pattern.quote(regexChar), "");
          }
        }
        message = formatChatPlaceholders(LanguageManager.getLanguageMessage("In-Game.Game-Chat-Format"), UserManager.getUser(event.getPlayer().getUniqueId()), eventMessage);
        for (Player player : arena.getPlayers()) {
          if (dead && arena.getPlayersLeft().contains(player)) {
            continue;
          }
          player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
      } else {
        event.getRecipients().clear();
        event.getRecipients().addAll(new ArrayList<>(arena.getPlayers()));
      }
    } catch (Exception ex){
      new ReportedException(plugin, ex);
    }
  }

  private String formatChatPlaceholders(String message, User user, String saidMessage) {
    String formatted = message;
    formatted = ChatManager.colorRawMessage(formatted);
    formatted = StringUtils.replace(formatted, "%player%", user.toPlayer().getName());
    formatted = StringUtils.replace(formatted, "%message%", saidMessage);
    return formatted;
  }

}
