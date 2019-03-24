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

package pl.plajer.murdermystery.arena.role;

import org.bukkit.entity.Player;

import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;

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
        return arena.isCharacterSet(Arena.CharacterType.DETECTIVE) && arena.getCharacter(Arena.CharacterType.DETECTIVE).equals(player);
      case FAKE_DETECTIVE:
        return arena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE) && arena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE).equals(player);
      case MURDERER:
        return arena.isCharacterSet(Arena.CharacterType.MURDERER) && arena.getCharacter(Arena.CharacterType.MURDERER).equals(player);
      case ANY_DETECTIVE:
        return arena.isCharacterSet(Arena.CharacterType.DETECTIVE) && arena.getCharacter(Arena.CharacterType.DETECTIVE).equals(player)
            || (arena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE) && arena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE).equals(player));
      case INNOCENT:
        return (arena.isCharacterSet(Arena.CharacterType.DETECTIVE) && !arena.getCharacter(Arena.CharacterType.DETECTIVE).equals(player)) &&
            (arena.isCharacterSet(Arena.CharacterType.FAKE_DETECTIVE) && !arena.getCharacter(Arena.CharacterType.FAKE_DETECTIVE).equals(player)) &&
            (arena.isCharacterSet(Arena.CharacterType.MURDERER) && !arena.getCharacter(Arena.CharacterType.MURDERER).equals(player));
      default:
        return false;
    }
  }

}
