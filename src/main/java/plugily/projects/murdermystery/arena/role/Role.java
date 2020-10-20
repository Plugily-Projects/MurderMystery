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
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;

import java.util.Arrays;

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
  MURDERER;

  /**
   * Checks whether player is playing specified role or not
   *
   * @param role   role to check
   * @param player player to check
   * @return true if is playing it, false otherwise
   */
  public static boolean isRole(Role role, Player player) {
    Arena arena = ArenaRegistry.getArena(player);
    if (arena == null) {
      return false;
    }
    switch (role) {
      case DETECTIVE:
        if (!arena.isCharacterSet(Arena.CharacterType.DETECTIVE)) {
          return false;
        }
        return arena.getDetectiveList().contains(player);
      case FAKE_DETECTIVE:
        if (!arena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE)) {
          return false;
        }
        Player fakeDetective = arena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE);
        return fakeDetective != null && fakeDetective.equals(player);
      case MURDERER:
        if (!arena.isCharacterSet(Arena.CharacterType.MURDERER)) {
          return false;
        }
        return arena.getMurdererList().contains(player);
      case ANY_DETECTIVE:
        return isRole(Role.DETECTIVE, player) || isRole(Role.FAKE_DETECTIVE, player);
      case INNOCENT:
        return !isRole(Role.MURDERER, player) && !isRole(Role.ANY_DETECTIVE, player);
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
  public static boolean isAnyRole(Player player) {
    return ArenaRegistry.isInArena(player) && Arrays.stream(Role.values()).anyMatch(role -> isRole(role, player));
  }
}
