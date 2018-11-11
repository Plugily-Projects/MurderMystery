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

package pl.plajer.murdermystery.commands.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.handlers.ChatManager;

/**
 * @author Plajer
 * <p>
 * Created at 31.10.2018
 */
public class ArgumentsRegistry implements CommandExecutor {

  private Map<String, List<CommandArgument>> mappedArguments = new HashMap<>();

  public ArgumentsRegistry() {
    //todo
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    for (String mainCommand : mappedArguments.keySet()) {
      if (cmd.getName().equals(mainCommand)) {
        for (CommandArgument argument : mappedArguments.get(mainCommand)) {
          if (argument.getArgumentName().equals(args[0])) {
            if (checkSenderIsExecutorType(sender, argument.getValidExecutors()) && hasPermission(sender, argument.getPermission())) {
              argument.execute(sender, args);
            }
            //return true even if sender is not good executor or hasn't got permission
            return true;
          }
        }
      }
    }
    return false;
  }

  boolean checkSenderIsExecutorType(CommandSender sender, CommandArgument.ExecutorType type) {
    switch (type) {
      case BOTH:
        return sender instanceof ConsoleCommandSender || sender instanceof Player;
      case CONSOLE:
        return sender instanceof ConsoleCommandSender;
      case PLAYER:
        if (sender instanceof Player) {
          return true;
        }
        sender.sendMessage(ChatManager.colorMessage("Commands.Only-By-Player"));
        return false;
      default:
        return false;
    }
  }

  boolean hasPermission(CommandSender sender, String perm) {
    if (sender.hasPermission(perm)) {
      return true;
    }
    sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Permission"));
    return false;
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

}
