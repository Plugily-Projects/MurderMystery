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
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajerlair.core.services.exception.ReportedException;

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
   * @param role role to check
   * @param p    player to check
   * @return true if is playing it, false otherwise
   */
  public static boolean isRole(Role role, Player p) {
    try {
      Arena arena = ArenaRegistry.getArena(p);
      if (arena == null) {
        return false;
      }
      switch (role) {
        case DETECTIVE:
          return arena.getDetective() == p.getUniqueId();
        case FAKE_DETECTIVE:
          return arena.getFakeDetective() != null && arena.getFakeDetective() == p.getUniqueId();
        case MURDERER:
          return arena.getMurderer() == p.getUniqueId();
        case ANY_DETECTIVE:
          return arena.getDetective() == p.getUniqueId() || (arena.getFakeDetective() != null && arena.getFakeDetective() == p.getUniqueId());
        case INNOCENT:
          return arena.getDetective() != p.getUniqueId() && (arena.getFakeDetective() != null && arena.getFakeDetective() != p.getUniqueId()) && arena.getMurderer() != p.getUniqueId();
        default:
          return false;
      }
    } catch (Exception ex) {
      new ReportedException(JavaPlugin.getPlugin(Main.class), ex);
      return false;
    }
  }

}
