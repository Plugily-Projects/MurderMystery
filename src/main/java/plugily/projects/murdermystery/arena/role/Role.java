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

package plugily.projects.murdermystery.arena.role;

import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.old.arena.ArenaRegistry;

/**
 * @author Plajer
 * <p>
 * Created at 06.10.2018
 */
public enum Role {

  /**
   * Detective or fake detective role
   */
  ANY_DETECTIVE,
  /**
   * Detective role, he must kill murderer
   */
  DETECTIVE,
  /**
   * Detective role, innocent who picked up bow became fake detective because he wasn't
   * detective by default
   */
  FAKE_DETECTIVE,
  /**
   * Innocent player role, must survive to win
   */
  INNOCENT,
  /**
   * Murderer role, must kill everyone to win
   */
  MURDERER,
  /**
   * Spectator role, just look :D
   */
  SPECTATOR,
  /**
   * Death role, when everyone died
   */
  DEATH;

  /**
   * Checks whether player is playing specified role or not
   *
   * @param role   role to check
   * @param player player to check
   * @return true if is playing it, false otherwise
   */
  public static boolean isRole(Role role, User user) {
    return isRole(role, user, user.getArena());
  }

  /**
   * Checks whether player is playing specified role or not
   *
   * @param role   role to check
   * @param player player to check
   * @param arena  the arena where to check
   * @return true if is playing it, false otherwise
   */
  public static boolean isRole(Role role, User user, PluginArena arena) {
    if (arena == null)
      return false;
    Arena pluginArena = (Arena) arena.getPlugin().getArenaRegistry().getArena(arena.getId());
    if (pluginArena == null) {
      return false;
    }
Player player = user.getPlayer();
    switch(role) {
      case DETECTIVE:
        return pluginArena.isCharacterSet(Arena.CharacterType.DETECTIVE) && pluginArena.getDetectiveList().contains(player);
      case FAKE_DETECTIVE:
        return player.equals(pluginArena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE));
      case MURDERER:
        return pluginArena.isCharacterSet(Arena.CharacterType.MURDERER) && pluginArena.getMurdererList().contains(player);
      case ANY_DETECTIVE:
        return isRole(Role.DETECTIVE, user) || isRole(Role.FAKE_DETECTIVE, user);
      case INNOCENT:
        return !isRole(Role.MURDERER, user) && !isRole(Role.ANY_DETECTIVE, user);
      case DEATH:
        return pluginArena.isDeathPlayer(player);
      case SPECTATOR:
        return pluginArena.isSpectatorPlayer(player);
      default:
        return false;
    }
  }

  /**
   * Checks whether player is playing a role or not
   *
   * @param player player to check
   * @return true if is playing one role, false otherwise
   */
  public static boolean isAnyRole(User user) {
    return isAnyRole(user, user.getArena());
  }

  private static final Role[] roles = Role.values();

  /**
   * Checks whether player is playing a role or not
   *
   * @param player player to check
   * @param arena the player's arena
   * @return true if is playing one role, false otherwise
   */
  public static boolean isAnyRole(User user, PluginArena arena) {
    return arena != null && java.util.Arrays.stream(roles).anyMatch(role -> isRole(role, user, arena));
  }
}
