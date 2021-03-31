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

package plugily.projects.murdermystery.commands.arguments.admin;

import org.bukkit.command.CommandSender;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.murdermystery.handlers.ChatManager;

/**
 * @author Plajer
 * <p>
 * Created at 18.05.2019
 */
public class ListArenasArgument {

  public ListArenasArgument(ArgumentsRegistry registry, ChatManager chatManager) {
    registry.mapArgument("murdermysteryadmin", new LabeledCommandArgument("list", "murdermystery.admin.list", CommandArgument.ExecutorType.BOTH,
      new LabelData("/mma list", "/mma list", "&7Shows list with all loaded arenas\n&6Permission: &7murdermystery.admin.list")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(chatManager.colorMessage("Commands.Admin-Commands.List-Command.Header"));
        int i = 0;
        for(Arena arena : ArenaRegistry.getArenas()) {
          sender.sendMessage(chatManager.colorMessage("Commands.Admin-Commands.List-Command.Format").replace("%arena%", arena.getId())
            .replace("%status%", arena.getArenaState().getFormattedName()).replace("%players%", Integer.toString(arena.getPlayers().size()))
            .replace("%maxplayers%", Integer.toString(arena.getMaximumPlayers())));
          i++;
        }
        if(i == 0) {
          sender.sendMessage(chatManager.colorMessage("Commands.Admin-Commands.List-Command.No-Arenas"));
        }
      }
    });
  }

}
