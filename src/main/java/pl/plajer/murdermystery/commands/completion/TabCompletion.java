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

package pl.plajer.murdermystery.commands.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.commands.arguments.ArgumentsRegistry;
import pl.plajer.murdermystery.commands.arguments.data.CommandArgument;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class TabCompletion implements TabCompleter {

  private List<CompletableArgument> registeredCompletions = new ArrayList<>();
  private ArgumentsRegistry registry;

  public TabCompletion(ArgumentsRegistry registry) {
    this.registry = registry;
  }

  public void registerCompletion(CompletableArgument completion) {
    registeredCompletions.add(completion);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return Collections.emptyList();
    }
    if (cmd.getName().equalsIgnoreCase("murdermysteryadmin") && args.length == 1) {
      return registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName).collect(Collectors.toList());
    }
    if (cmd.getName().equalsIgnoreCase("murdermystery")) {
      if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
        List<String> arenaIds = new ArrayList<>();
        for (Arena arena : ArenaRegistry.getArenas()) {
          arenaIds.add(arena.getId());
        }
        return arenaIds;
      }
      if (args.length == 1) {
        return registry.getMappedArguments().get(cmd.getName().toLowerCase()).stream().map(CommandArgument::getArgumentName).collect(Collectors.toList());
      }
    }
    if (args.length < 2) {
      return Collections.emptyList();
    }
    for (CompletableArgument completion : registeredCompletions) {
      if (!cmd.getName().equalsIgnoreCase(completion.getMainCommand()) || !completion.getArgument().equalsIgnoreCase(args[0])) {
        continue;
      }
      return completion.getCompletions();
    }
    return Collections.emptyList();
  }
}
