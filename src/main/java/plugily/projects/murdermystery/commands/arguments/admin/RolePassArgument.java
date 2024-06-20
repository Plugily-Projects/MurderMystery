/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (c) 2022  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.murdermystery.commands.arguments.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.api.user.IUser;
import plugily.projects.minigamesbox.classic.commands.arguments.data.CommandArgument;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabelData;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.number.NumberUtils;
import plugily.projects.murdermystery.arena.role.Role;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 13.06.2021
 */
public class RolePassArgument {

  public RolePassArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermysteryadmin", new LabeledCommandArgument("rolepass", "murdermystery.admin.rolepass", CommandArgument.ExecutorType.BOTH,
        new LabelData("/mma rolepass <add/set/remove> <role> <amount> [player]", "/mma rolepass <add/remove> <amount> [player] ", "&7Add or remove rolepass\n&6Permission: &7murdermystery.admin.rolepass")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if(args.length < 4) {
          sender.sendMessage(ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        //add/remove
        String addOrRemove = args[1];
        if(!addOrRemove.equalsIgnoreCase("add") && !addOrRemove.equalsIgnoreCase("remove") && !addOrRemove.equalsIgnoreCase("set")) {
          sender.sendMessage(ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        String roleArg = args[2];
        Role role = Role.MURDERER;
        try {
          role = Role.valueOf(roleArg.toUpperCase());
        } catch(IllegalArgumentException exception) {
          sender.sendMessage(ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        if(role != Role.MURDERER && role != Role.DETECTIVE) {
          sender.sendMessage(ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        java.util.Optional<Integer> opt = NumberUtils.parseInt(args[3]);
        if(!opt.isPresent()) {
          sender.sendMessage(ChatColor.RED + "Command: /mma rolepass <add/set/remove> <role> <amount> [player]");
          return;
        }
        int amount = opt.orElse(0);
        //player
        Player player = args.length == 5 ? Bukkit.getPlayerExact(args[4]) : (Player) sender;
        if(player == null) {
          new MessageBuilder("COMMANDS_PLAYER_NOT_FOUND").asKey().send(sender);
          return;
        }
        IUser user = registry.getPlugin().getUserManager().getUser(player);
        switch(addOrRemove.toLowerCase()) {
          case "add":
            if(role == Role.MURDERER) {
              user.adjustStatistic("PASS_MURDERER", amount);
            } else {
              user.adjustStatistic("PASS_DETECTIVE", amount);
            }
            break;
          case "remove":
            if(role == Role.MURDERER) {
              user.adjustStatistic("PASS_MURDERER", -amount);
            } else {
              user.adjustStatistic("PASS_DETECTIVE", -amount);
            }
            break;
          case "set":
            if(role == Role.MURDERER) {
              user.setStatistic("PASS_MURDERER", amount);
            } else {
              user.setStatistic("PASS_DETECTIVE", amount);
            }
            break;
          default:
            break;
        }
        if(role == Role.MURDERER) {
          new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_CHANGE").asKey().player(player).integer(user.getStatistic("PASS_MURDERER")).value(Role.MURDERER.name()).sendPlayer();
        } else {
          new MessageBuilder("IN_GAME_MESSAGES_ARENA_PASS_CHANGE").asKey().player(player).integer(user.getStatistic("PASS_DETECTIVE")).value(Role.DETECTIVE.name()).sendPlayer();
        }
      }
    });
  }

}
