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

package plugily.projects.murdermystery.commands.arguments.admin.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaManager;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.ArenaState;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.murdermystery.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 18.05.2019
 */
public class StopArgument {

  public StopArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermysteryadmin", new LabeledCommandArgument("stop", "murdermystery.admin.stop", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/mma stop", "/mma stop", "&7Stops the arena you're in\n&7&lYou must be in target arena!\n&6Permission: &7murdermystery.admin.stop")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if(!Utils.checkIsInGameInstance((Player) sender)) {
          return;
        }
        Arena arena = ArenaRegistry.getArena((Player) sender);
        if(arena.getArenaState() != ArenaState.ENDING) {
          ArenaManager.stopGame(true, arena);
          //todo execute success command message
        }
      }
    });
  }

}
