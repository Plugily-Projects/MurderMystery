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

package plugily.projects.murdermystery.commands.arguments;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import pl.plajerlair.commonsbox.string.StringMatcher;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.commands.arguments.admin.ListArenasArgument;
import plugily.projects.murdermystery.commands.arguments.admin.arena.DeleteArgument;
import plugily.projects.murdermystery.commands.arguments.admin.arena.ForceStartArgument;
import plugily.projects.murdermystery.commands.arguments.admin.arena.ReloadArgument;
import plugily.projects.murdermystery.commands.arguments.admin.arena.SpecialBlockRemoverArgument;
import plugily.projects.murdermystery.commands.arguments.admin.arena.StopArgument;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.murdermystery.commands.arguments.game.ArenaSelectorArgument;
import plugily.projects.murdermystery.commands.arguments.game.CreateArgument;
import plugily.projects.murdermystery.commands.arguments.game.JoinArguments;
import plugily.projects.murdermystery.commands.arguments.game.LeaderboardArgument;
import plugily.projects.murdermystery.commands.arguments.game.LeaveArgument;
import plugily.projects.murdermystery.commands.arguments.game.StatsArgument;
import plugily.projects.murdermystery.commands.completion.TabCompletion;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.setup.SetupInventory;
import plugily.projects.murdermystery.utils.Debugger;
import plugily.projects.murdermystery.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 11.01.2019
 */
public class ArgumentsRegistry implements CommandExecutor {

  private final Map<String, List<CommandArgument>> mappedArguments = new HashMap<>();
  private final Main plugin;
  private final ChatManager chatManager;
  private final TabCompletion tabCompletion;

  public ArgumentsRegistry(Main plugin) {
    this.plugin = plugin;
    chatManager = plugin.getChatManager();
    tabCompletion = new TabCompletion(this);

    Optional.ofNullable(plugin.getCommand("murdermystery")).ifPresent(mm -> {
      mm.setExecutor(this);
      mm.setTabCompleter(tabCompletion);
    });
    Optional.ofNullable(plugin.getCommand("murdermysteryadmin")).ifPresent(mma -> {
      mma.setExecutor(this);
      mma.setTabCompleter(tabCompletion);
    });

    //register basic arugments
    new ArenaSelectorArgument(this, chatManager);
    new CreateArgument(this, chatManager);
    new JoinArguments(this, chatManager);
    new LeaderboardArgument(this, chatManager);
    new LeaveArgument(this, chatManager);
    new StatsArgument(this, chatManager);

    //register admin related arguments
    new DeleteArgument(this, chatManager);
    new ForceStartArgument(this, chatManager);
    new ListArenasArgument(this, chatManager);
    new SpecialBlockRemoverArgument(this);
    new ReloadArgument(this, chatManager);
    new StopArgument(this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    for(Map.Entry<String, List<CommandArgument>> entry : mappedArguments.entrySet()) {
      if(cmd.getName().equalsIgnoreCase(entry.getKey())) {
        if(cmd.getName().equalsIgnoreCase("murdermystery")) {
          if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpCommand(sender);
            return true;
          }
          if(args.length > 1 && args[1].equalsIgnoreCase("edit")) {
            if(!checkSenderIsExecutorType(sender, CommandArgument.ExecutorType.PLAYER)
              || !Utils.hasPermission(sender, "murdermystery.admin.create")) {
              return true;
            }
            if(ArenaRegistry.getArena(args[0]) == null) {
              sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.No-Arena-Like-That"));
              return true;
            }

            new SetupInventory(ArenaRegistry.getArena(args[0]), (Player) sender).openInventory();
            return true;
          }
        }
        if(cmd.getName().equalsIgnoreCase("murdermysteryadmin") && (args.length == 0 || args[0].equalsIgnoreCase("help"))) {
          if(!sender.hasPermission("murdermystery.admin")) {
            return true;
          }
          sendAdminHelpCommand(sender);
          return true;
        }
        for(CommandArgument argument : entry.getValue()) {
          if(argument.getArgumentName().equalsIgnoreCase(args[0])) {
            //does it make sense that it is a list?
            for(String perm : argument.getPermissions()) {
              if(perm.isEmpty() || Utils.hasPermission(sender, perm)) {
                break;
              }
              //user has no permission to execute command
              return true;
            }
            if(checkSenderIsExecutorType(sender, argument.getValidExecutors())) {
              argument.execute(sender, args);
            }
            //return true even if sender is not good executor or hasn't got permission
            return true;
          }
        }

        //sending did you mean help
        List<StringMatcher.Match> matches = StringMatcher.match(args[0], mappedArguments.get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName).collect(Collectors.toList()));
        if(!matches.isEmpty()) {
          sender.sendMessage(chatManager.colorMessage("Commands.Did-You-Mean").replace("%command%", label + " " + matches.get(0).getMatch()));
          return true;
        }
      }
    }
    return false;
  }

  private boolean checkSenderIsExecutorType(CommandSender sender, CommandArgument.ExecutorType type) {
    switch(type) {
      case BOTH:
        return sender instanceof ConsoleCommandSender || sender instanceof Player;
      case CONSOLE:
        return sender instanceof ConsoleCommandSender;
      case PLAYER:
        if(sender instanceof Player) {
          return true;
        }
        sender.sendMessage(chatManager.colorMessage("Commands.Only-By-Player"));
        return false;
      default:
        return false;
    }
  }

  private void sendHelpCommand(CommandSender sender) {
    sender.sendMessage(chatManager.colorMessage("Commands.Main-Command.Header"));
    sender.sendMessage(chatManager.colorMessage("Commands.Main-Command.Description"));
    if(sender.hasPermission("murdermystery.admin")) {
      sender.sendMessage(chatManager.colorMessage("Commands.Main-Command.Admin-Bonus-Description"));
    }
    sender.sendMessage(chatManager.colorMessage("Commands.Main-Command.Footer"));
  }

  private void sendAdminHelpCommand(CommandSender sender) {
    sender.sendMessage(ChatColor.GREEN + "  " + ChatColor.BOLD + "Murder Mystery " + ChatColor.GRAY + plugin.getDescription().getVersion());
    sender.sendMessage(ChatColor.RED + " []" + ChatColor.GRAY + " = optional  " + ChatColor.GOLD + "<>" + ChatColor.GRAY + " = required");
    if(sender instanceof Player) {
      sender.sendMessage(ChatColor.GRAY + "Hover command to see more, click command to suggest it.");
    }
    List<LabelData> data = mappedArguments.get("murdermysteryadmin").stream().filter(arg -> arg instanceof LabeledCommandArgument)
      .map(arg -> ((LabeledCommandArgument) arg).getLabelData()).collect(Collectors.toList());
    data.add(new LabelData("/mm &6<arena>&f edit", "/mm <arena> edit",
      "&7Edit existing arena\n&6Permission: &7murdermystery.admin.edit"));
    data.addAll(mappedArguments.get("murdermystery").stream().filter(arg -> arg instanceof LabeledCommandArgument)
      .map(arg -> ((LabeledCommandArgument) arg).getLabelData()).collect(Collectors.toList()));
    for(LabelData labelData : data) {
      if(sender instanceof Player) {
        TextComponent component = new TextComponent(labelData.getText());
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, labelData.getCommand()));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(labelData.getDescription()).create()));
        ((Player) sender).spigot().sendMessage(component);
      } else {
        //more descriptive for console - split at \n to show only basic description
        Debugger.sendConsoleMsg(labelData.getText() + " - " + labelData.getDescription().split("\n")[0]);
      }
    }
  }

  /**
   * Maps new argument to the main command
   *
   * @param mainCommand mother command ex. /mm
   * @param argument    argument to map ex. leave (for /mm leave)
   */
  public void mapArgument(String mainCommand, CommandArgument argument) {
    List<CommandArgument> args = mappedArguments.getOrDefault(mainCommand, new ArrayList<>());
    args.add(argument);
    mappedArguments.put(mainCommand, args);
  }

  public Map<String, List<CommandArgument>> getMappedArguments() {
    return mappedArguments;
  }

  public TabCompletion getTabCompletion() {
    return tabCompletion;
  }

  public Main getPlugin() {
    return plugin;
  }

}
