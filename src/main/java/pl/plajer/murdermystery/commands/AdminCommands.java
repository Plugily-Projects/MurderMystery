/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.commands;

import java.util.LinkedList;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaManager;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
@Deprecated
public class AdminCommands extends MainCommand {

  private static List<CommandData> command = new LinkedList<>();

  static {
    ChatColor gray = ChatColor.GRAY;
    ChatColor gold = ChatColor.GOLD;
    command.add(new CommandData("/mm create " + gold + "<arena>", "/mm create <arena>",
        gray + "Create new arena\n" + gold + "Permission: " + gray + "murdermystery.admin.create"));
    command.add(new CommandData("/mm " + gold + "<arena>" + ChatColor.WHITE + " edit", "/mm <arena> edit",
        gray + "Edit existing arena\n" + gold + "Permission: " + gray + "murdermystery.admin.edit"));
    command.add(new CommandData("/mma list", "/mma list",
        gray + "Shows list with all loaded arenas\n" + gold + "Permission: " + gray + "murdermystery.admin.list"));
    command.add(new CommandData("/mma stop", "/mma stop",
        gray + "Stops the arena you're in\n" + gray + "" + ChatColor.BOLD + "You must be in target arena!\n" + gold + "Permission: " + gray + "murdermystery.admin.stop"));
    command.add(new CommandData("/mma forcestart", "/mma forcestart",
        gray + "Force starts arena you're in\n" + gold + "Permission: " + gray + "murdermystery.admin.forcestart"));
    command.add(new CommandData("/mma reload", "/mma reload", gray + "Reload all game arenas\n" + gray + "" + ChatColor.BOLD
        + "They will be stopped!\n" + gold + "Permission: " + gray + "murdermystery.admin.reload"));
    command.add(new CommandData("/mma delete " + gold + "<arena>", "/mma delete <arena>",
        gray + "Deletes specified arena\n" + gold + "Permission: " + gray + "murdermystery.admin.delete"));
  }

  private Main plugin;

  public AdminCommands(Main plugin) {
    super(plugin, false);
    this.plugin = plugin;
  }

  public void sendHelp(CommandSender sender) {
    if (!sender.hasPermission("murdermystery.admin")) {
      return;
    }
    sender.sendMessage(ChatColor.GREEN + "  " + ChatColor.BOLD + "Murder Mystery " + ChatColor.GRAY + plugin.getDescription().getVersion());
    if (!checkSenderPlayer(sender)) {
      sender.sendMessage(ChatColor.RED + " []" + ChatColor.GRAY + " = optional  " + ChatColor.GOLD + "<>" + ChatColor.GRAY + " = required");
      sender.sendMessage(ChatColor.GRAY + "Hover command to see more, click command to suggest it.");
      for (CommandData data : command) {
        TextComponent component = new TextComponent(data.getText());
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, data.getCommand()));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(data.getDescription()).create()));
        ((Player) sender).spigot().sendMessage(component);
      }
      return;
    }
    //must be updated manually
    sender.sendMessage(ChatColor.WHITE + "/mm create " + ChatColor.GOLD + "<arena>" + ChatColor.GRAY + ": Create new arena");
    sender.sendMessage(ChatColor.WHITE + "/mm " + ChatColor.GOLD + "<arena>" + ChatColor.WHITE + " edit" + ChatColor.GRAY + ": Edit existing arena");
    sender.sendMessage(ChatColor.WHITE + "/mma list" + ChatColor.GRAY + ": Print all loaded instances");
    sender.sendMessage(ChatColor.WHITE + "/mma stop" + ChatColor.GRAY + ": Stop the arena");
    sender.sendMessage(ChatColor.WHITE + "/mma forcestart" + ChatColor.GRAY + ": Force start the arena");
    sender.sendMessage(ChatColor.WHITE + "/mma reload" + ChatColor.GRAY + ": Stops and reloads all game instances");
    sender.sendMessage(ChatColor.WHITE + "/mma delete " + ChatColor.GOLD + "<arena>" + ChatColor.GRAY + ": Remove existing arena");
  }

  public void printList(CommandSender sender) {
    if (!hasPermission(sender, "murdermystery.admin.list")) {
      return;
    }
    sender.sendMessage(ChatManager.colorMessage("Commands.Admin-Commands.List-Command.Header"));
    int i = 0;
    for (Arena arena : ArenaRegistry.getArenas()) {
      sender.sendMessage(ChatManager.colorMessage("Commands.Admin-Commands.List-Command.Format").replace("%arena%", arena.getId())
          .replace("%status%", arena.getArenaState().getFormattedName()).replace("%players%", String.valueOf(arena.getPlayers().size()))
          .replace("%maxplayers%", String.valueOf(arena.getMaximumPlayers())));
      i++;
    }
    if (i == 0) {
      sender.sendMessage(ChatManager.colorMessage("Commands.Admin-Commands.List-Command.No-Arenas"));
    }
  }

  public void stopGame(CommandSender sender) {
    if (!checkSenderPlayer(sender) || !hasPermission(sender, "murdermystery.admin.stopgame")) {
      return;
    }
    if (!checkIsInGameInstance((Player) sender)) {
      return;
    }
    ArenaManager.stopGame(false, ArenaRegistry.getArena((Player) sender));
  }

  public void forceStartGame(CommandSender sender) {
    if (!checkSenderPlayer(sender) || !hasPermission(sender, "murdermystery.admin.forcestart")) {
      return;
    }
    if (!checkIsInGameInstance((Player) sender)) {
      return;
    }
    Arena arena = ArenaRegistry.getArena((Player) sender);
    if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
      arena.setArenaState(ArenaState.STARTING);
      arena.setForceStart(true);
      arena.setTimer(0);
      for (Player p : ArenaRegistry.getArena((Player) sender).getPlayers()) {
        p.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Admin-Messages.Set-Starting-In-To-0"));
      }
    }
  }

  public void reloadInstances(CommandSender sender) {
    if (!hasPermission(sender, "murdermystery.admin.reload")) {
      return;
    }
    ArenaRegistry.registerArenas();
    sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Admin-Commands.Success-Reload"));
  }

  public void deleteArena(CommandSender sender, String arenaString) {
    if (!checkSenderPlayer(sender) || !hasPermission(sender, "murdermystery.admin.delete")) {
      return;
    }
    Arena arena = ArenaRegistry.getArena(arenaString);
    if (arena == null) {
      sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Arena-Like-That"));
      return;
    }
    ArenaManager.stopGame(false, arena);
    FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
    config.set("instances." + arenaString, null);
    ConfigUtils.saveConfig(plugin, config, "arenas");
    ArenaRegistry.unregisterArena(arena);
    sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Removed-Game-Instance"));
  }

}
