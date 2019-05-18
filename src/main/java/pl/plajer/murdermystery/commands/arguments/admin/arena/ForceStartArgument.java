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

package pl.plajer.murdermystery.commands.arguments.admin.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.commands.arguments.ArgumentsRegistry;
import pl.plajer.murdermystery.commands.arguments.data.CommandArgument;
import pl.plajer.murdermystery.commands.arguments.data.LabelData;
import pl.plajer.murdermystery.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 18.05.2019
 */
public class ForceStartArgument {

  public ForceStartArgument(ArgumentsRegistry registry) {
    registry.mapArgument("murdermysteryadmin", new LabeledCommandArgument("forcestart", "murdermystery.admin.forcestart", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/vda forcestart", "/vda forcestart", "&7Force starts arena you're in\n&6Permission: &7murdermystery.admin.forcestart")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (!Utils.checkIsInGameInstance((Player) sender)) {
          return;
        }
        Arena arena = ArenaRegistry.getArena((Player) sender);
        if (arena.getPlayers().size() < 2) {
          return;
        }
        if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
          arena.setArenaState(ArenaState.STARTING);
          arena.setForceStart(true);
          arena.setTimer(0);
          for (Player p : ArenaRegistry.getArena((Player) sender).getPlayers()) {
            p.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Admin-Messages.Set-Starting-In-To-0"));
          }
        }
      }
    });
  }

}
