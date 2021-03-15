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

package plugily.projects.murdermystery.commands.completion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class TabCompletion implements TabCompleter {

  private final List<CompletableArgument> registeredCompletions = new ArrayList<>();
  private final ArgumentsRegistry registry;

  public TabCompletion(ArgumentsRegistry registry) {
    this.registry = registry;
  }

  public void registerCompletion(CompletableArgument completion) {
    registeredCompletions.add(completion);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    List<String> cmds = new ArrayList<>();
    String partOfCommand = null;

    if(cmd.getName().equalsIgnoreCase("murdermysteryadmin")) {
      if(args.length == 1) {
        cmds.addAll(registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName)
          .collect(Collectors.toList()));
        partOfCommand = args[0];
      } else if(args.length == 2 && args[0].equalsIgnoreCase("delete")) {
        cmds.addAll(ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList()));
        partOfCommand = args[1];
      }
    }

    if(cmd.getName().equalsIgnoreCase("murdermystery")) {
      if(args.length == 2 && args[0].equalsIgnoreCase("join")) {
        cmds.addAll(ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList()));
        partOfCommand = args[1];
      } else if(args.length == 1) {
        cmds.addAll(registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName)
          .collect(Collectors.toList()));
        partOfCommand = args[0];
      }
    }

    // Completes the player names
    if(cmds.isEmpty() || partOfCommand == null) {
      for(CompletableArgument completion : registeredCompletions) {
        if(!cmd.getName().equalsIgnoreCase(completion.getMainCommand()) || !completion.getArgument().equalsIgnoreCase(args[0])) {
          continue;
        }
        return completion.getCompletions();
      }

      return null;
    }

    return cmds;
  }
}
